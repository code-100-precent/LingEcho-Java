package com.lingecho.common.core.search.core;

import com.lingecho.common.core.search.analyzer.ChineseAnalyzer;
import com.lingecho.common.core.search.config.SearchConfig;
import com.lingecho.common.core.search.config.StorageType;
import com.lingecho.common.core.search.model.*;
import com.lingecho.common.core.search.model.Document;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.SimpleSpanFragmenter;
import org.apache.lucene.search.suggest.InputIterator;
import org.apache.lucene.search.suggest.Lookup;
import org.apache.lucene.search.suggest.analyzing.AnalyzingInfixSuggester;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 搜索引擎核心实现类
 * 基于Apache Lucene实现的高效搜索引擎
 *
 * @author HibiscusSearch Team
 * @version 1.0.0
 */
public class SearchEngine implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(SearchEngine.class);

    private static final ObjectMapper mapper = new ObjectMapper();

    // 新增一个字段名（复合主键）
    private static final String FIELD_UID = "_uid";
    private static final String FIELD_ID = "id";
    private static final String FIELD_TYPE = "type";
    private static final String FIELD_TITLE = "title";
    private static final String FIELD_CONTENT = "content";
    private static final String FIELD_TIMESTAMP = "timestamp";

    private final SearchConfig config;
    private Directory directory;
    private Analyzer analyzer;
    private IndexWriter indexWriter;
    private IndexReader indexReader;
    private IndexSearcher indexSearcher;
    private ScheduledExecutorService commitExecutor;
    private AnalyzingInfixSuggester suggester;
    
    // 性能优化：智能缓存系统
    private final Map<String, Query> queryCache = new ConcurrentHashMap<>();
    private final Map<String, List<String>> suggestionCache = new ConcurrentHashMap<>();
    private final Map<String, Long> queryTimestamps = new ConcurrentHashMap<>(); // 缓存时间戳
    private final Map<String, Integer> queryHitCounts = new ConcurrentHashMap<>(); // 查询命中次数
    private QueryParser titleParser;
    private QueryParser contentParser;
    
    // 性能优化：缓存配置
    private static final int MAX_QUERY_CACHE_SIZE = 2000;
    private static final int MAX_SUGGESTION_CACHE_SIZE = 1000;
    private static final long CACHE_TTL_MS = 300000; // 5分钟缓存过期时间
    private static final int MIN_HIT_COUNT_FOR_CACHE = 2; // 最少命中次数才缓存

    /**
     * 创建搜索引擎
     *
     * @param config 搜索配置
     */
    public SearchEngine(SearchConfig config) {
        this.config = config;
        initialize();
    }

    /**
     * 初始化搜索引擎
     */
    private void initialize() {
        try {
            // 创建索引目录
            Path indexPath = config.getIndexPath();
            if (!Files.exists(indexPath)) {
                Files.createDirectories(indexPath);
            }

            // 初始化Lucene组件
            if(config.getStorageType() == StorageType.JDBC){
                directory = DirectoryProvider.createIndexDirectory(config);
            } else {
                directory = FSDirectory.open(indexPath);
            }
            analyzer = createAnalyzer();

            // 创建索引写入器，添加索引损坏检测和修复
            IndexWriterConfig writerConfig = new IndexWriterConfig(analyzer);
            
            // 检查索引是否损坏，如果损坏则重新创建
            try {
                if (DirectoryReader.indexExists(directory)) {
                    // 尝试打开索引读取器来验证索引完整性
                    try (DirectoryReader testReader = DirectoryReader.open(directory)) {
                        // 索引正常，使用追加模式
                        writerConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
                        logger.info("Existing index is valid, using append mode");
                    } catch (Exception e) {
                        logger.warn("Index appears to be corrupted, will recreate: {}", e.getMessage());
                        // 索引损坏，删除并重新创建
                        clearCorruptedIndex();
                        writerConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
                    }
                } else {
                    // 索引不存在，使用创建模式
                    writerConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
                    logger.info("No existing index found, using create mode");
                }
            } catch (Exception e) {
                logger.warn("Failed to check index integrity, using create mode: {}", e.getMessage());
                clearCorruptedIndex();
                writerConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
            }
            
            // 性能优化：设置合并策略和缓冲区大小
            writerConfig.setRAMBufferSizeMB(512); // 增加RAM缓冲区到512MB
            writerConfig.setMaxBufferedDocs(20000); // 增加缓冲文档数
            writerConfig.setUseCompoundFile(false); // 不使用复合文件，提高写入速度
            writerConfig.setMergePolicy(new TieredMergePolicy()); // 使用分层合并策略
            
            try {
                indexWriter = new IndexWriter(directory, writerConfig);
                logger.info("IndexWriter created successfully");
            } catch (Exception e) {
                logger.error("Failed to create IndexWriter, clearing directory and retrying", e);
                clearCorruptedIndex();
                writerConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
                indexWriter = new IndexWriter(directory, writerConfig);
                logger.info("IndexWriter created successfully after clearing corrupted index");
            }

            // 预创建查询解析器，避免重复创建
            titleParser = new QueryParser(FIELD_TITLE, analyzer);
            contentParser = new QueryParser(FIELD_CONTENT, analyzer);

            // 初始化搜索建议器
            if (config.isSuggestEnabled()) {
                try {
                    Path suggestPath = indexPath.resolve("suggest");
                    if (!Files.exists(suggestPath)) {
                        Files.createDirectories(suggestPath);
                    }
                    Directory suggestDir;
                    if(config.getStorageType() == StorageType.JDBC){
                        suggestDir = DirectoryProvider.createSuggestDirectory(config);
                    } else {
                        suggestDir = FSDirectory.open(suggestPath);
                    }
                    suggester = new AnalyzingInfixSuggester(suggestDir, analyzer);
                    
                    // 重要：构建建议器（使用空的迭代器初始化）
                    suggester.build(new InputIterator() {
                        @Override
                        public long weight() { return 1; }
                        @Override
                        public Set<BytesRef> contexts() { return null; }
                        @Override
                        public boolean hasContexts() { return false; }
                        @Override
                        public boolean hasPayloads() { return false; }
                        @Override
                        public BytesRef payload() { return null; }
                        @Override
                        public BytesRef next() { return null; }
                    });
                    
                    // 添加一些默认建议
                    try {
                        suggester.add(new BytesRef("搜索"), null, 1, new BytesRef("搜索"));
                        suggester.add(new BytesRef("文档"), null, 1, new BytesRef("文档"));
                        suggester.add(new BytesRef("索引"), null, 1, new BytesRef("索引"));
                        suggester.add(new BytesRef("查询"), null, 1, new BytesRef("查询"));
                        suggester.add(new BytesRef("Java"), null, 1, new BytesRef("Java"));
                        suggester.add(new BytesRef("Spring"), null, 1, new BytesRef("Spring"));
                        suggester.add(new BytesRef("数据库"), null, 1, new BytesRef("数据库"));
                        suggester.add(new BytesRef("前端"), null, 1, new BytesRef("前端"));
                        suggester.add(new BytesRef("后端"), null, 1, new BytesRef("后端"));
                        suggester.add(new BytesRef("微服务"), null, 1, new BytesRef("微服务"));
                        
                        // 重新构建建议器以包含新添加的建议
                        suggester.build(new InputIterator() {
                            @Override
                            public long weight() { return 1; }
                            @Override
                            public Set<BytesRef> contexts() { return null; }
                            @Override
                            public boolean hasContexts() { return false; }
                            @Override
                            public boolean hasPayloads() { return false; }
                            @Override
                            public BytesRef payload() { return null; }
                            @Override
                            public BytesRef next() { return null; }
                        });
                        
                        logger.info("Default suggestions added and suggester rebuilt successfully");
                    } catch (Exception e) {
                        logger.warn("Failed to add default suggestions", e);
                    }
                    
                    logger.info("Search suggester initialized successfully");
                } catch (Exception e) {
                    logger.warn("Failed to initialize search suggester, suggestions will be disabled", e);
                    suggester = null;
                }
            }

            // 初始化搜索器
            refreshSearcher();

            // 启动自动提交任务
            if (config.isAutoCommit()) {
                startAutoCommit();
            }

            logger.info("SearchEngine initialized successfully with index path: {}", indexPath);

        } catch (IOException e) {
            logger.error("Failed to initialize SearchEngine", e);
            throw new RuntimeException("Failed to initialize SearchEngine", e);
        }
    }

    /**
     * 创建分析器
     *
     * @return 分析器实例
     */
    private Analyzer createAnalyzer() {
        return switch (config.getAnalyzerType().toLowerCase()) {
            case "chinese" -> new ChineseAnalyzer();
            case "ik" -> new IKAnalyzer();
            default -> new StandardAnalyzer();
        };
    }

    /**
     * 刷新搜索器
     */
    private void refreshSearcher() throws IOException {
        if (indexReader != null) {
            indexReader.close();
        }

        // 检查索引是否为空，添加重试机制和更好的错误处理
        try {
            if (DirectoryReader.indexExists(directory)) {
                indexReader = DirectoryReader.open(directory);
                indexSearcher = new IndexSearcher(indexReader);
            } else {
                // 索引为空，创建一个空的搜索器
                indexReader = null;
                indexSearcher = null;
                logger.debug("Index is empty, searcher not initialized");
            }
        } catch (IOException e) {
            logger.warn("Failed to check index existence, retrying once: {}", e.getMessage());
            
            // 重试一次
            try {
                Thread.sleep(1000); // 等待1秒
                if (DirectoryReader.indexExists(directory)) {
                    indexReader = DirectoryReader.open(directory);
                    indexSearcher = new IndexSearcher(indexReader);
                } else {
                    indexReader = null;
                    indexSearcher = null;
                    logger.debug("Index is empty after retry, searcher not initialized");
                }
            } catch (Exception retryEx) {
                logger.error("Failed to initialize searcher after retry", retryEx);
                // 如果重试失败，设置默认值
                indexReader = null;
                indexSearcher = null;
            }
        }
    }

    /**
     * 启动自动提交任务
     */
    private void startAutoCommit() {
        commitExecutor = Executors.newSingleThreadScheduledExecutor();
        commitExecutor.scheduleAtFixedRate(() -> {
            try {
                if (indexWriter != null) {
                    indexWriter.commit();
                    refreshSearcher();
                    logger.debug("Auto commit completed");
                }
            } catch (IOException e) {
                logger.error("Auto commit failed", e);
            }
        }, config.getCommitInterval(), config.getCommitInterval(), TimeUnit.MILLISECONDS);
    }

    /**
     * 索引单个文档
     *
     * @param id 文档ID
     * @param title 文档标题
     * @param content 文档内容
     * @throws IOException 索引异常
     */
    public void indexDocument(String id, String title, String content) throws IOException {
        indexDocument(id, "default", title, content);
    }



    private static String uid(String id, String type) {
        return type + "#" + id;
    }

    /**
     * 索引单个文档（带类型）—— 幂等防重复，永不报“未存在”的错
     */
    public void indexDocument(String id, String type, String title, String content) throws IOException {
        if (id == null || id.isBlank())  throw new IllegalArgumentException("Document ID cannot be null or empty");
        if (type == null || type.isBlank()) throw new IllegalArgumentException("Document type cannot be null or empty");

        String safeTitle = title != null ? title.trim() : "";
        String safeContent = content != null ? content.trim() : "";
        if (safeTitle.isEmpty() && safeContent.isEmpty()) safeTitle = id;

        long now = System.currentTimeMillis();

        org.apache.lucene.document.Document doc = new org.apache.lucene.document.Document();
        // 关键：写入复合主键字段
        doc.add(new StringField(FIELD_UID, uid(id, type), Field.Store.YES));

        // 其它检索/展示字段
        doc.add(new StringField(FIELD_ID, id, Field.Store.YES));
        doc.add(new StringField(FIELD_TYPE, type, Field.Store.YES));
        doc.add(new TextField(FIELD_TITLE, safeTitle, Field.Store.YES));
        doc.add(new SortedDocValuesField(FIELD_TITLE, new BytesRef(safeTitle)));
        doc.add(new TextField(FIELD_CONTENT, safeContent, Field.Store.YES));
        doc.add(new LongPoint(FIELD_TIMESTAMP, now));
        doc.add(new StoredField(FIELD_TIMESTAMP, now));

        // ✅ upsert：不存在就新增，存在就覆盖，绝不会因为“没有旧文档”报错
        indexWriter.updateDocument(new Term(FIELD_UID, uid(id, type)), doc);

        // 建议逻辑保持不变
        if (!config.isAutoCommit() || indexSearcher == null) {
            indexWriter.commit();
            refreshSearcher();
        }
        clearCaches();

        // 建议器逻辑：添加文档到建议器
        if (suggester != null && config.isSuggestEnabled()) {
            try {
                // 添加标题作为建议
                if (!safeTitle.isEmpty()) {
                    suggester.add(new BytesRef(safeTitle), null, 1, new BytesRef(safeTitle));
                    logger.debug("Added title suggestion: {}", safeTitle);
                }
                
                // 添加内容关键词作为建议（取前100个字符）
                if (!safeContent.isEmpty()) {
                    String shortContent = safeContent.length() > 100 ? 
                        safeContent.substring(0, 100) : safeContent;
                    suggester.add(new BytesRef(shortContent), null, 1, new BytesRef(shortContent));
                    logger.debug("Added content suggestion: {}", shortContent);
                }
                
                // 定期重建建议器以确保建议的准确性
                if (Math.random() < 0.05) { // 降低重建频率到5%
                    rebuildSuggester();
                }
            } catch (Exception e) {
                logger.warn("Failed to add suggestion for document: {}", id, e);
                // 如果添加失败，尝试重建建议器
                try { 
                    rebuildSuggester(); 
                } catch (Exception ignore) {
                    logger.debug("Failed to rebuild suggester after error", ignore);
                }
            }
        }
    }

    /**
     * 检查文档是否已存在
     *
     * @param id 文档ID
     * @param type 文档类型
     * @return 是否存在
     * @throws IOException 检查异常
     */
    private boolean documentExists(String id, String type) throws IOException {
        if (indexSearcher == null) {
            return false;
        }
        
        try {
            // 创建复合查询：ID + 类型
            BooleanQuery.Builder queryBuilder = new BooleanQuery.Builder();
            queryBuilder.add(new TermQuery(new Term(FIELD_ID, id)), BooleanClause.Occur.MUST);
            queryBuilder.add(new TermQuery(new Term(FIELD_TYPE, type)), BooleanClause.Occur.MUST);
            
            TopDocs topDocs = indexSearcher.search(queryBuilder.build(), 1);
            return topDocs.totalHits.value > 0;
        } catch (Exception e) {
            logger.warn("Failed to check document existence: id={}, type={}", id, type, e);
            return false;
        }
    }

    /**
     * 更新已存在的文档
     *
     * @param id 文档ID
     * @param type 文档类型
     * @param title 文档标题
     * @param content 文档内容
     * @throws IOException 更新异常
     */
    private void updateDocument(String id, String type, String title, String content) throws IOException {
        try {
            // 删除旧文档
            deleteDocument(id, type);
            
            // 直接创建并索引新文档，避免递归调用
            org.apache.lucene.document.Document doc = new org.apache.lucene.document.Document();
            
            // 确保标题和内容不为 null
            String safeTitle = title != null ? title.trim() : "";
            String safeContent = content != null ? content.trim() : "";
            
            // 如果标题和内容都为空，使用ID作为标题
            if (safeTitle.isEmpty() && safeContent.isEmpty()) {
                safeTitle = id;
            }
            
            // 添加字段
            doc.add(new StringField(FIELD_ID, id, Field.Store.YES));
            doc.add(new StringField(FIELD_TYPE, type, Field.Store.YES));
            doc.add(new TextField(FIELD_TITLE, safeTitle, Field.Store.YES));
            doc.add(new SortedDocValuesField(FIELD_TITLE, new BytesRef(safeTitle)));
            doc.add(new TextField(FIELD_CONTENT, safeContent, Field.Store.YES));
            doc.add(new LongPoint(FIELD_TIMESTAMP, System.currentTimeMillis()));
            doc.add(new StoredField(FIELD_TIMESTAMP, System.currentTimeMillis()));
            
            // 写入索引
            indexWriter.addDocument(doc);
            
            // 添加到搜索建议器
            if (suggester != null && config.isSuggestEnabled()) {
                try {
                    if (!safeTitle.isEmpty()) {
                        suggester.add(new BytesRef(safeTitle), null, 1, new BytesRef(safeTitle));
                    }
                    if (!safeContent.isEmpty()) {
                        suggester.add(new BytesRef(safeContent), null, 1, new BytesRef(safeContent));
                    }
                } catch (Exception e) {
                    logger.warn("Failed to add suggestion for updated document: {}", id, e);
                }
            }
            
            if (!config.isAutoCommit()) {
                indexWriter.commit();
                refreshSearcher();
            } else if (indexSearcher == null) {
                indexWriter.commit();
                refreshSearcher();
            }
            
            logger.debug("Updated existing document: id={}, type={}", id, type);
        } catch (Exception e) {
            logger.error("Failed to update document: id={}, type={}", id, type, e);
            throw e;
        }
    }

    /**
     * 批量索引文档
     *
     * @throws IOException 索引异常
     */
    public void indexDocuments(List<Document> docs) throws IOException {
        // 同批去重：最后一次出现覆盖之前
        Map<String, Document> uniq = new LinkedHashMap<>();
        for (Document d : docs) {
            if (d.getId() == null || d.getId().isBlank()) {
                throw new IllegalArgumentException("Document ID cannot be null or empty");
            }
            String type = d.getType() == null ? "" : d.getType();
            uniq.put(uid(d.getId(), type), d);
        }

        for (Document d : uniq.values()) {
            org.apache.lucene.document.Document luceneDoc = createLuceneDocumentWithUid(d); // 见下
            indexWriter.updateDocument(new Term(FIELD_UID, uid(d.getId(), d.getType())), luceneDoc);
        }

        if (!config.isAutoCommit() || indexSearcher == null) {
            indexWriter.commit();
            refreshSearcher();
        }
        clearCaches();
        logger.info("Upserted {} documents", uniq.size());
    }

    private org.apache.lucene.document.Document createLuceneDocumentWithUid(Document d) {
        try{
            String safeTitle = d.getTitle() != null ? d.getTitle().trim() : "";
            String safeContent = d.getContent() != null ? d.getContent().trim() : "";
            if (safeTitle.isEmpty() && safeContent.isEmpty()) safeTitle = d.getId();

            org.apache.lucene.document.Document doc = new org.apache.lucene.document.Document();
            doc.add(new StringField(FIELD_UID, uid(d.getId(), d.getType()), Field.Store.YES));
            doc.add(new StringField(FIELD_ID, d.getId(), Field.Store.YES));
            doc.add(new StringField(FIELD_TYPE, d.getType(), Field.Store.YES));
            doc.add(new TextField(FIELD_TITLE, safeTitle, Field.Store.YES));
            doc.add(new SortedDocValuesField(FIELD_TITLE, new BytesRef(safeTitle)));
            doc.add(new TextField(FIELD_CONTENT, safeContent, Field.Store.YES));
            doc.add(new LongPoint(FIELD_TIMESTAMP, d.getTimestamp()));
            doc.add(new StoredField(FIELD_TIMESTAMP, d.getTimestamp()));
            return doc;
        } catch (Exception e) {
            logger.error("Failed to create Lucene document: {}", d, e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 创建Lucene文档
     */
    private org.apache.lucene.document.Document createLuceneDocument(Document doc) {
        // 验证输入参数
        if (doc.getId() == null || doc.getId().trim().isEmpty()) {
            throw new IllegalArgumentException("Document ID cannot be null or empty");
        }
        if (doc.getType() == null || doc.getType().trim().isEmpty()) {
            throw new IllegalArgumentException("Document type cannot be null or empty");
        }
        
        // 确保标题和内容不为 null
        String safeTitle = doc.getTitle() != null ? doc.getTitle().trim() : "";
        String safeContent = doc.getContent() != null ? doc.getContent().trim() : "";
        
        // 如果标题和内容都为空，使用ID作为标题
        if (safeTitle.isEmpty() && safeContent.isEmpty()) {
            safeTitle = doc.getId();
        }
        
        org.apache.lucene.document.Document luceneDoc = new org.apache.lucene.document.Document();

        // 添加字段
        luceneDoc.add(new StringField(FIELD_ID, doc.getId(), Field.Store.YES));
        luceneDoc.add(new StringField(FIELD_TYPE, doc.getType(), Field.Store.YES));
        luceneDoc.add(new TextField(FIELD_TITLE, safeTitle, Field.Store.YES));
        // 为排序添加SortedDocValuesField
        luceneDoc.add(new SortedDocValuesField(FIELD_TITLE, new BytesRef(safeTitle)));
        luceneDoc.add(new TextField(FIELD_CONTENT, safeContent, Field.Store.YES));
        luceneDoc.add(new LongPoint(FIELD_TIMESTAMP, doc.getTimestamp()));
        luceneDoc.add(new StoredField(FIELD_TIMESTAMP, doc.getTimestamp()));

        // 添加元数据
        for (Map.Entry<String, String> entry : doc.getMetadata().entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
                luceneDoc.add(new StringField("meta_" + entry.getKey(), entry.getValue(), Field.Store.YES));
            }
        }

        return luceneDoc;
    }

    /**
     * 搜索文档
     *
     * @param request 搜索请求
     * @return 搜索结果
     * @throws IOException 搜索异常
     */
    public SearchResult search(SearchRequest request) throws IOException {
        long startTime = System.currentTimeMillis();

        // 检查搜索器是否可用
        if (indexSearcher == null) {
            // 索引为空，返回空结果
            SearchResult result = new SearchResult(request.getQuery(), 0);
            result.setPage(request.getPage());
            result.setPageSize(request.getPageSize());
            result.setSearchTime(System.currentTimeMillis() - startTime);
            return result;
        }

        // 性能优化：智能查询缓存
        String cacheKey = generateCacheKey(request);
        Query query = getCachedQuery(cacheKey);
        if (query == null) {
            query = createQuery(request);
            // 智能缓存策略：只缓存热门查询
            updateQueryCache(cacheKey, query, request);
        } else {
            // 更新命中次数
            queryHitCounts.merge(cacheKey, 1, Integer::sum);
        }

        // 创建排序
        Sort sort = createSort(request);

        // 性能优化：批量获取文档
        TopDocs topDocs = indexSearcher.search(query, request.getMaxResults(), sort);

        // 构建搜索结果
        SearchResult result = new SearchResult(request.getQuery(), topDocs.totalHits.value);
        result.setPage(request.getPage());
        result.setPageSize(request.getPageSize());
        result.setSearchTime(System.currentTimeMillis() - startTime);

        // 性能优化：批量获取文档内容
        int start = request.getOffset();
        int end = Math.min(start + request.getPageSize(), topDocs.scoreDocs.length);
        
        logger.debug("Search result processing: totalHits={}, scoreDocs.length={}, start={}, end={}, minScore={}", 
                    topDocs.totalHits.value, topDocs.scoreDocs.length, start, end, request.getMinScore());
        
        // 修复：确保有结果可以处理
        if (topDocs.scoreDocs.length > 0) {
            // 修复：确保分页范围有效
            if (start >= topDocs.scoreDocs.length) {
                logger.warn("Start offset {} is beyond available results {}", start, topDocs.scoreDocs.length);
                start = 0;
                end = Math.min(request.getPageSize(), topDocs.scoreDocs.length);
            }
            
            if (end > start) {
                // 批量获取文档，减少I/O操作
                int[] docIds = new int[end - start];
                for (int i = start; i < end; i++) {
                    docIds[i - start] = topDocs.scoreDocs[i].doc;
                }
                
                // 批量获取文档
                org.apache.lucene.document.Document[] docs = new org.apache.lucene.document.Document[docIds.length];
                for (int i = 0; i < docIds.length; i++) {
                    try {
                        docs[i] = indexSearcher.doc(docIds[i]);
                    } catch (Exception e) {
                        logger.warn("Failed to get document with id: {}", docIds[i], e);
                        continue;
                    }
                }
                
                int addedHits = 0;
                // 在处理每个 scoreDoc 时，打印评分并检查是否为 NaN
                for (int i = 0; i < docs.length; i++) {
                    if (docs[i] == null) continue;

                    ScoreDoc scoreDoc = topDocs.scoreDocs[start + i];
                    logger.debug("Processing hit: docId={}, score={}, minScore={}",
                            docIds[i], scoreDoc.score, request.getMinScore());

                    // 修复评分问题：处理 NaN 和无效评分
                    float score = scoreDoc.score;
                    if (Float.isNaN(score) || Float.isInfinite(score)) {
                        // 如果评分为 NaN 或无穷大，使用默认评分
                        score = 1.0f;
                        logger.debug("Fixed invalid score for docId: {}, new score: {}", docIds[i], score);
                    }

                    // 放宽评分过滤条件，避免所有结果被过滤
                    if (score >= request.getMinScore()) {
                        // 创建并添加 SearchHit
                        SearchResult.SearchHit hit = createSearchHit(docs[i], score, request);
                        result.addHit(hit); // 将结果添加到 SearchResult 的 hits 列表中
                        addedHits++;
                    } else {
                        logger.debug("Hit filtered out due to low score: docId={}, score={}, minScore={}",
                                docIds[i], score, request.getMinScore());
                    }
                }

                
                logger.debug("Added {} hits to result (filtered by minScore)", addedHits);
                
                // 修复：如果没有结果被添加，但总命中数>0，记录警告
                if (addedHits == 0 && topDocs.totalHits.value > 0) {
                    logger.warn("No hits added despite {} total hits. Possible issues: minScore too high, pagination, or document retrieval failure", 
                               topDocs.totalHits.value);
                }
            } else {
                logger.debug("Invalid pagination range: start={}, end={}", start, end);
            }
        } else {
            logger.debug("No documents to process: scoreDocs.length={}", topDocs.scoreDocs.length);
        }

        // 性能监控日志
        long searchTime = System.currentTimeMillis() - startTime;
        if (searchTime > 100) { // 记录慢查询
            logger.warn("Slow query detected: {}ms for query: '{}'", searchTime, request.getQuery());
        } else {
            logger.debug("Query completed in {}ms: '{}'", searchTime, request.getQuery());
        }

        return result;
    }

    /**
     * 生成缓存键
     *
     * @param request 搜索请求
     * @return 缓存键
     */
    private String generateCacheKey(SearchRequest request) {
        StringBuilder key = new StringBuilder();
        key.append(request.getQuery()).append("|");
        key.append(request.getMaxResults()).append("|");
        key.append(request.getMinScore()).append("|");
        
        // 添加过滤条件到缓存键
        for (SearchRequest.Filter filter : request.getFilters()) {
            key.append(filter.getField()).append("=").append(filter.getValue()).append("|");
        }
        
        return key.toString();
    }

    /**
     * 获取缓存的查询
     */
    private Query getCachedQuery(String cacheKey) {
        Query query = queryCache.get(cacheKey);
        if (query != null) {
            Long timestamp = queryTimestamps.get(cacheKey);
            // 检查缓存是否过期
            if (timestamp != null && System.currentTimeMillis() - timestamp < CACHE_TTL_MS) {
                return query;
            } else {
                // 缓存过期，清理
                queryCache.remove(cacheKey);
                queryTimestamps.remove(cacheKey);
                queryHitCounts.remove(cacheKey);
            }
        }
        return null;
    }

    /**
     * 更新查询缓存
     */
    private void updateQueryCache(String cacheKey, Query query, SearchRequest request) {
        // 智能缓存策略
        boolean shouldCache = shouldCacheQuery(cacheKey, request);
        if (shouldCache) {
            queryCache.put(cacheKey, query);
            queryTimestamps.put(cacheKey, System.currentTimeMillis());
            queryHitCounts.put(cacheKey, 1);
            
            // 如果缓存过大，清理最少使用的缓存
            if (queryCache.size() > MAX_QUERY_CACHE_SIZE) {
                cleanupLeastUsedCache();
            }
        }
    }

    /**
     * 判断是否应该缓存查询
     */
    private boolean shouldCacheQuery(String cacheKey, SearchRequest request) {
        // 不缓存复杂查询
        if (!request.getFilters().isEmpty() || cacheKey.length() > 200) {
            return false;
        }
        
        // 不缓存太短的查询
        if (request.getQuery().length() < 2) {
            return false;
        }
        
        // 检查是否是热门查询模式
        String query = request.getQuery().toLowerCase();
        return query.matches(".*[a-zA-Z\u4e00-\u9fa5]+.*"); // 包含字母或中文
    }

    /**
     * 清理最少使用的缓存
     */
    private void cleanupLeastUsedCache() {
        // 按命中次数排序，保留最热门的缓存
        queryHitCounts.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .limit(queryCache.size() / 2) // 清理一半的缓存
                .forEach(entry -> {
                    String key = entry.getKey();
                    queryCache.remove(key);
                    queryTimestamps.remove(key);
                    queryHitCounts.remove(key);
                });
        
        logger.debug("Cleaned up {} least used cache entries", queryCache.size() / 2);
    }

    /**
     * 清理缓存
     */
    private void clearCaches() {
        // 智能清理：只清理过期的缓存
        long currentTime = System.currentTimeMillis();
        queryTimestamps.entrySet().removeIf(entry -> 
            currentTime - entry.getValue() > CACHE_TTL_MS);
        
        // 清理对应的查询缓存
        queryCache.entrySet().removeIf(entry -> 
            !queryTimestamps.containsKey(entry.getKey()));
        
        // 清理建议缓存
        if (suggestionCache.size() > MAX_SUGGESTION_CACHE_SIZE) {
            suggestionCache.clear();
            logger.debug("Suggestion cache cleared due to size limit");
        }
    }

    /**
     * 创建查询
     *
     * @param request 搜索请求
     * @return Lucene查询
     */
    private Query createQuery(SearchRequest request) throws IOException {
        String queryString = request.getQuery();

        try {
            // 验证查询字符串
            if (queryString == null || queryString.trim().isEmpty()) {
                logger.warn("Query string is null or empty, using MatchAllDocsQuery");
                return new MatchAllDocsQuery();
            }

            // 清理查询字符串，移除可能导致解析问题的特殊字符
            String cleanQuery = queryString.trim().replaceAll("[\\p{Cntrl}\\p{Space}]+", " ");
            
            // 性能优化：使用预创建的解析器
            BooleanQuery.Builder queryBuilder = new BooleanQuery.Builder();

            try {
                // 标题查询（权重更高）
                Query titleQuery = titleParser.parse(cleanQuery);
                titleQuery = new BoostQuery(titleQuery, 2.0f);
                queryBuilder.add(titleQuery, BooleanClause.Occur.SHOULD);
            } catch (Exception e) {
                logger.warn("Failed to parse title query: '{}', using wildcard query", cleanQuery, e);
                // 回退到通配符查询
                Query titleQuery = new WildcardQuery(new Term(FIELD_TITLE, "*" + cleanQuery + "*"));
                titleQuery = new BoostQuery(titleQuery, 2.0f);
                queryBuilder.add(titleQuery, BooleanClause.Occur.SHOULD);
            }

            try {
                // 内容查询
                Query contentQuery = contentParser.parse(cleanQuery);
                queryBuilder.add(contentQuery, BooleanClause.Occur.SHOULD);
            } catch (Exception e) {
                logger.warn("Failed to parse content query: '{}', using wildcard query", cleanQuery, e);
                // 回退到通配符查询
                Query contentQuery = new WildcardQuery(new Term(FIELD_CONTENT, "*" + cleanQuery + "*"));
                queryBuilder.add(contentQuery, BooleanClause.Occur.SHOULD);
            }

            // 添加过滤条件
            for (SearchRequest.Filter filter : request.getFilters()) {
                Query filterQuery = createFilterQuery(filter);
                queryBuilder.add(filterQuery, BooleanClause.Occur.FILTER);
            }

            Query finalQuery = queryBuilder.build();
            
            // 验证查询是否为空
            if (finalQuery instanceof BooleanQuery && ((BooleanQuery) finalQuery).clauses().isEmpty()) {
                logger.warn("Query builder resulted in empty query, using MatchAllDocsQuery");
                return new MatchAllDocsQuery();
            }
            
            return finalQuery;

        } catch (Exception e) {
            logger.error("Failed to create query for: '{}', using fallback query", queryString, e);
            // 如果所有解析都失败，回退到通配符查询
            return new WildcardQuery(new Term(FIELD_CONTENT, "*" + queryString + "*"));
        }
    }

    /**
     * 创建过滤查询
     *
     * @param filter 过滤条件
     * @return 过滤查询
     */
    private Query createFilterQuery(SearchRequest.Filter filter) {
        String field = "meta_" + filter.getField();
        String value = filter.getValue();

        switch (filter.getType()) {
            case EQUALS:
                return new TermQuery(new Term(field, value));
            case NOT_EQUALS:
                return new BooleanQuery.Builder()
                        .add(new TermQuery(new Term(field, value)), BooleanClause.Occur.MUST_NOT)
                        .build();
            case CONTAINS:
                return new WildcardQuery(new Term(field, "*" + value + "*"));
            case NOT_CONTAINS:
                return new BooleanQuery.Builder()
                        .add(new WildcardQuery(new Term(field, "*" + value + "*")), BooleanClause.Occur.MUST_NOT)
                        .build();
            case GREATER_THAN:
                try {
                    long longValue = Long.parseLong(value);
                    return LongPoint.newRangeQuery(field, longValue + 1, Long.MAX_VALUE);
                } catch (NumberFormatException e) {
                    // 如果不是数字，使用字符串比较
                    return new TermRangeQuery(field, new BytesRef(value), null, false, true);
                }
            case LESS_THAN:
                try {
                    long longValue = Long.parseLong(value);
                    return LongPoint.newRangeQuery(field, Long.MIN_VALUE, longValue - 1);
                } catch (NumberFormatException e) {
                    // 如果不是数字，使用字符串比较
                    return new TermRangeQuery(field, null, new BytesRef(value), true, false);
                }
            default:
                return new MatchAllDocsQuery();
        }
    }

    /**
     * 创建排序
     *
     * @param request 搜索请求
     * @return 排序对象
     */
    private Sort createSort(SearchRequest request) {
        if (request.getSortFields().isEmpty()) {
            return Sort.RELEVANCE;
        }

        List<SortField> sortFields = new ArrayList<>();
        for (SearchRequest.SortField sortField : request.getSortFields()) {
            SortField.Type type = SortField.Type.STRING;
            if (sortField.getField().equals(FIELD_TIMESTAMP)) {
                type = SortField.Type.LONG;
            }

            SortField field = new SortField(sortField.getField(), type,
                    sortField.getOrder() == SearchRequest.SortField.SortOrder.DESC);
            sortFields.add(field);
        }

        return new Sort(sortFields.toArray(new SortField[0]));
    }

    /**
     * 创建搜索结果项
     *
     * @param doc Lucene文档
     * @param score 评分
     * @param request 搜索请求
     * @return 搜索结果项
     */
    private SearchResult.SearchHit createSearchHit(org.apache.lucene.document.Document doc, float score, SearchRequest request) {
        SearchResult.SearchHit hit = new SearchResult.SearchHit();
        
        // 安全获取字段值
        String id = doc.get(FIELD_ID);
        String title = doc.get(FIELD_TITLE);
        String content = doc.get(FIELD_CONTENT);
        hit.setType(doc.get(FIELD_TYPE));
        hit.setId(id != null ? id : "");
        hit.setTitle(title != null ? title : "");
        hit.setContent(content != null ? content : "");
        hit.setScore(score);
        // 高亮处理
        if (request.isHighlight() && config.isHighlightEnabled()) {
            try {
                if (title != null && !title.isEmpty()) {
                    hit.setHighlightedTitle(highlightText(title, request.getQuery()));
                } else {
                    hit.setHighlightedTitle("");
                }
                
                if (content != null && !content.isEmpty()) {
                    hit.setHighlightedContent(highlightText(content, request.getQuery()));
                } else {
                    hit.setHighlightedContent("");
                }
            } catch (Exception e) {
                logger.warn("Failed to highlight text for document: {}", id, e);
                // 设置默认值
                hit.setHighlightedTitle(title != null ? title : "");
                hit.setHighlightedContent(content != null ? content : "");
            }
        }

        // 提取元数据
        Map<String, String> metadata = new HashMap<>();
        for (IndexableField field : doc.getFields()) {
            String name = field.name();
            if (name.startsWith("meta_") && field.stringValue() != null) {
                metadata.put(name.substring(5), field.stringValue());
            }
        }
        hit.setMetadata(metadata);

        return hit;
    }

    /**
     * 高亮文本
     *
     * @param text 原文本
     * @param query 查询
     * @return 高亮后的文本
     */
    private String highlightText(String text, String query) {
        if (text == null || text.isEmpty() || query == null || query.trim().isEmpty()) {
            return text != null ? text : "";
        }

        try {
            Query highlightQuery = new TermQuery(new Term(FIELD_CONTENT, query.trim()));
            QueryScorer queryScorer = new QueryScorer(highlightQuery, FIELD_CONTENT); // 指定字段
            Highlighter highlighter = new Highlighter(
                    new SimpleHTMLFormatter(config.getHighlightPreTag(), config.getHighlightPostTag()),
                    queryScorer
            );

            highlighter.setTextFragmenter(
                    new SimpleSpanFragmenter(queryScorer, config.getHighlightFragmentSize())
            );

            String highlighted = highlighter.getBestFragment(analyzer, FIELD_CONTENT, text);
            return highlighted != null ? highlighted : text;
        } catch (Exception e) {
            logger.warn("高亮失败: {}", text, e);
            return text;
        }
    }

    /**
     * 删除文档
     *
     * @param id 文档ID
     * @throws IOException 删除异常
     */
    public void deleteDocument(String id) throws IOException {
        deleteDocument(id, null);
    }

    /**
     * 删除文档（带类型）
     *
     * @param id 文档ID
     * @param type 文档类型，如果为null则删除所有类型的该ID文档
     * @throws IOException 删除异常
     */
    public void deleteDocument(String id, String type) throws IOException {
        indexWriter.deleteDocuments(new Term(FIELD_UID, uid(id, type)));
        if (!config.isAutoCommit()) {
            indexWriter.commit();
            refreshSearcher();
        }
    }


    /**
     * 批量删除文档
     *
     * @param ids 文档ID列表
     * @throws IOException 删除异常
     */
    public void deleteDocuments(List<String> ids) throws IOException {
        for (String id : ids) {
            indexWriter.deleteDocuments(new Term(FIELD_ID, id));
        }

        if (!config.isAutoCommit()) {
            indexWriter.commit();
            refreshSearcher();
        }
    }

    /**
     * 根据条件删除文档
     *
     * @param field 字段名
     * @param value 字段值
     * @throws IOException 删除异常
     */
    public void deleteDocumentsByField(String field, String value) throws IOException {
        String metaField = "meta_" + field;
        indexWriter.deleteDocuments(new Term(metaField, value));

        if (!config.isAutoCommit()) {
            indexWriter.commit();
            refreshSearcher();
        }
    }

    /**
     * 清空索引
     *
     * @throws IOException 清空异常
     */
    public void clearIndex() throws IOException {
        indexWriter.deleteAll();
        indexWriter.commit();
        refreshSearcher();
    }

    /**
     * 优化索引
     *
     * @throws IOException 优化异常
     */
    public void optimizeIndex() throws IOException {
        indexWriter.forceMerge(1);
        indexWriter.commit();
        refreshSearcher();
    }

    /**
     * 获取索引统计信息
     *
     * @return 索引统计信息
     * @throws IOException 获取异常
     */
    public IndexStats getIndexStats() throws IOException {
        return new IndexStats(indexReader.numDocs(), indexReader.maxDoc());
    }

    /**
     * 获取索引健康状态
     *
     * @return 索引健康状态信息
     * @throws IOException 获取异常
     */
    public IndexHealthInfo getIndexHealthInfo() throws IOException {
        if (indexReader == null) {
            return new IndexHealthInfo(true, "Index is empty", 0, 0);
        }

        int numDocs = indexReader.numDocs();
        int maxDoc = indexReader.maxDoc();
        int deletedDocs = maxDoc - numDocs;
        double healthScore = numDocs > 0 ? (double) numDocs / maxDoc : 1.0;

        String status = healthScore > 0.8 ? "Healthy" :
                       healthScore > 0.5 ? "Warning" : "Critical";

        return new IndexHealthInfo(healthScore > 0.5, status, numDocs, deletedDocs);
    }

    /**
     * 检查索引是否需要优化
     *
     * @return 是否需要优化
     * @throws IOException 检查异常
     */
    public boolean needsOptimization() throws IOException {
        if (indexReader == null) {
            return false;
        }

        // 如果删除的文档数量超过总文档数的30%，建议优化
        int numDocs = indexReader.numDocs();
        int maxDoc = indexReader.maxDoc();
        return maxDoc > 0 && (double) (maxDoc - numDocs) / maxDoc > 0.3;
    }

    /**
     * 获取搜索建议
     *
     * @param query 查询前缀
     * @return 搜索建议列表
     * @throws IOException 获取异常
     */
    public List<String> getSuggestions(String query) throws IOException {
        if (suggester == null || !config.isSuggestEnabled() || query == null || query.trim().isEmpty()) {
            logger.debug("Suggester disabled or query empty, returning empty suggestions");
            return new ArrayList<>();
        }

        // 检查建议器是否已构建
        try {
            // 尝试获取建议器的状态信息，如果失败说明建议器未正确构建
            long count = suggester.getCount();
            logger.debug("Suggester count: {} for query: {}", count, query);
            
            if (count == 0) {
                logger.warn("Suggester has no suggestions, attempting to rebuild");
                rebuildSuggester();
                // 重新获取计数
                count = suggester.getCount();
                logger.debug("After rebuild, suggester count: {}", count);
            }
        } catch (Exception e) {
            logger.warn("Suggester is not properly built, attempting to rebuild for query: {}", query, e);
            try {
                rebuildSuggester();
            } catch (Exception rebuildEx) {
                logger.error("Failed to rebuild suggester", rebuildEx);
                return new ArrayList<>();
            }
        }

        // 性能优化：搜索建议缓存
        String cacheKey = query.toLowerCase().trim();
        List<String> cachedSuggestions = suggestionCache.get(cacheKey);
        if (cachedSuggestions != null) {
            logger.debug("Returning cached suggestions for query: {}", query);
            return new ArrayList<>(cachedSuggestions);
        }

        try {
            logger.debug("Looking up suggestions for query: '{}' with max results: {}", 
                        query, config.getSuggestMaxResults());
            
            List<Lookup.LookupResult> results = suggester.lookup(query, config.getSuggestMaxResults(), true, true);
            List<String> suggestions = new ArrayList<>();
            
            for (Lookup.LookupResult result : results) {
                suggestions.add(result.key.toString());
            }
            
            logger.debug("Found {} suggestions for query: '{}': {}", suggestions.size(), query, suggestions);
            
            // 缓存建议结果
            suggestionCache.put(cacheKey, new ArrayList<>(suggestions));
            
            return suggestions;
        } catch (Exception e) {
            logger.warn("Failed to get suggestions for query: '{}'", query, e);
            
            // 如果查询失败，尝试重建建议器
            try {
                logger.info("Attempting to rebuild suggester after lookup failure");
                rebuildSuggester();
                
                // 重新尝试查询
                List<Lookup.LookupResult> results = suggester.lookup(query, config.getSuggestMaxResults(), true, true);
                List<String> suggestions = new ArrayList<>();
                
                for (Lookup.LookupResult result : results) {
                    suggestions.add(result.key.toString());
                }
                
                logger.info("Successfully retrieved {} suggestions after rebuild for query: '{}'", 
                           suggestions.size(), query);
                
                return suggestions;
            } catch (Exception rebuildEx) {
                logger.error("Failed to get suggestions even after rebuild for query: '{}'", query, rebuildEx);
                return new ArrayList<>();
            }
        }
    }

    /**
     * 预热缓存（预加载热门查询）
     */
    public void warmupCache(List<String> popularQueries) {
        logger.info("Starting cache warmup with {} queries", popularQueries.size());
        for (String query : popularQueries) {
            try {
                SearchRequest request = new SearchRequest(query);
                String cacheKey = generateCacheKey(request);
                Query luceneQuery = createQuery(request);
                updateQueryCache(cacheKey, luceneQuery, request);
            } catch (Exception e) {
                logger.warn("Failed to warmup cache for query: {}", query, e);
            }
        }
        logger.info("Cache warmup completed");
    }

    /**
     * 调试搜索（用于诊断问题）
     */
    public void debugSearch(SearchRequest request) throws IOException {
        logger.info("=== 搜索调试信息 ===");
        logger.info("查询: {}", request.getQuery());
        logger.info("页码: {}, 每页大小: {}", request.getPage(), request.getPageSize());
        logger.info("最大结果数: {}, 最小评分: {}", request.getMaxResults(), request.getMinScore());
        logger.info("过滤条件: {}", request.getFilters());
        
        if (indexSearcher == null) {
            logger.info("搜索器为空，索引可能为空");
            return;
        }
        
        // 创建查询
        Query query = createQuery(request);
        logger.info("创建的查询: {}", query);
        
        // 执行搜索
        TopDocs topDocs = indexSearcher.search(query, request.getMaxResults());
        logger.info("搜索结果: totalHits={}, scoreDocs.length={}", 
                   topDocs.totalHits.value, topDocs.scoreDocs.length);
        
        // 显示前几个结果的评分
        for (int i = 0; i < Math.min(5, topDocs.scoreDocs.length); i++) {
            ScoreDoc scoreDoc = topDocs.scoreDocs[i];
            logger.info("结果 {}: docId={}, score={}", i, scoreDoc.doc, scoreDoc.score);
            
            // 检查评分是否为 NaN 或无穷大
            if (Float.isNaN(scoreDoc.score)) {
                logger.warn("发现 NaN 评分: docId={}", scoreDoc.doc);
            } else if (Float.isInfinite(scoreDoc.score)) {
                logger.warn("发现无穷大评分: docId={}, score={}", scoreDoc.doc, scoreDoc.score);
            }
        }
        
        logger.info("=== 调试信息结束 ===");
    }
    
    /**
     * 清理损坏的索引
     */
    private void clearCorruptedIndex() {
        try {
            logger.warn("Clearing corrupted index directory");
            
            if (indexWriter != null) {
                try {
                    indexWriter.close();
                } catch (Exception e) {
                    logger.debug("Error closing corrupted indexWriter", e);
                }
                indexWriter = null;
            }
            
            if (indexReader != null) {
                try {
                    indexReader.close();
                } catch (Exception e) {
                    logger.debug("Error closing corrupted indexReader", e);
                }
                indexReader = null;
            }
            
            // 删除所有索引文件
            try {
                String[] files = directory.listAll();
                for (String file : files) {
                    try {
                        directory.deleteFile(file);
                        logger.debug("Deleted corrupted index file: {}", file);
                    } catch (Exception e) {
                        logger.debug("Failed to delete file: {}", file, e);
                    }
                }
            } catch (Exception e) {
                logger.debug("Failed to list directory files", e);
            }
            
            logger.info("Corrupted index cleared successfully");
        } catch (Exception e) {
            logger.error("Failed to clear corrupted index", e);
        }
    }

    /**
     * 重新构建建议器
     */
    private void rebuildSuggester() {
        if (suggester == null || !config.isSuggestEnabled()) {
            return;
        }
        
        try {
            // 从索引中读取所有文档来重建建议器
            if (indexReader != null && indexReader.numDocs() > 0) {
                // 创建文档迭代器
                InputIterator docIterator = new InputIterator() {
                    private int docId = 0;
                    private final int maxDoc = indexReader.numDocs();
                    
                    @Override
                    public long weight() { return 1; }
                    
                    @Override
                    public Set<BytesRef> contexts() { return null; }
                    
                    @Override
                    public boolean hasContexts() { return false; }
                    
                    @Override
                    public boolean hasPayloads() { return false; }
                    
                    @Override
                    public BytesRef payload() { return null; }
                    
                    @Override
                    public BytesRef next() {
                        if (docId >= maxDoc) {
                            return null;
                        }
                        
                        try {
                            // 获取文档
                            org.apache.lucene.document.Document doc = indexReader.document(docId);
                            docId++;
                            
                            // 提取标题和内容作为建议
                            String title = doc.get(FIELD_TITLE);
                            String content = doc.get(FIELD_CONTENT);
                            
                            if (title != null && !title.trim().isEmpty()) {
                                return new BytesRef(title.trim());
                            } else if (content != null && !content.trim().isEmpty()) {
                                // 取内容的前50个字符作为建议
                                String shortContent = content.trim().substring(0, Math.min(50, content.trim().length()));
                                return new BytesRef(shortContent);
                            }
                            
                            return null;
                        } catch (Exception e) {
                            logger.warn("Failed to read document {} for suggester rebuild", docId, e);
                            docId++;
                            return null;
                        }
                    }
                };
                
                // 重建建议器
                suggester.build(docIterator);
                logger.info("Suggester rebuilt successfully with {} documents", indexReader.numDocs());
            } else {
                // 索引为空，使用默认建议
                suggester.build(new InputIterator() {
                    @Override
                    public long weight() { return 1; }
                    @Override
                    public Set<BytesRef> contexts() { return null; }
                    @Override
                    public boolean hasContexts() { return false; }
                    @Override
                    public boolean hasPayloads() { return false; }
                    @Override
                    public BytesRef payload() { return null; }
                    @Override
                    public BytesRef next() { return null; }
                });
                
                // 添加默认建议
                String[] defaultSuggestions = {
                    "搜索", "文档", "索引", "查询", "Java", "Spring", 
                    "数据库", "前端", "后端", "微服务", "编程", "开发"
                };
                
                for (String suggestion : defaultSuggestions) {
                    try {
                        suggester.add(new BytesRef(suggestion), null, 1, new BytesRef(suggestion));
                    } catch (Exception e) {
                        logger.debug("Failed to add default suggestion: {}", suggestion, e);
                    }
                }
                
                logger.info("Suggester rebuilt with default suggestions");
            }
        } catch (Exception e) {
            logger.warn("Failed to rebuild suggester", e);
        }
    }

    /**
     * 诊断索引问题
     */
    public void diagnoseIndex() throws IOException {
        logger.info("=== 索引诊断信息 ===");
        
        if (indexReader == null) {
            logger.info("索引读取器为空");
            return;
        }
        
        logger.info("索引统计: numDocs={}, maxDoc={}", 
                   indexReader.numDocs(), indexReader.maxDoc());
        
        logger.info("=== 索引诊断结束 ===");
    }

    /**
     * 关闭搜索引擎
     */
    @Override
    public void close() throws IOException {
        if (commitExecutor != null) {
            commitExecutor.shutdown();
            try {
                if (!commitExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    commitExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                commitExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        if (indexWriter != null) {
            indexWriter.commit();
            indexWriter.close();
        }

        if (indexReader != null) {
            indexReader.close();
        }

        if (suggester != null) {
            suggester.close();
        }

        if (directory != null) {
            directory.close();
        }

        if (analyzer != null) {
            analyzer.close();
        }

        // 清理缓存
        queryCache.clear();
        suggestionCache.clear();

        logger.info("SearchEngine closed");
    }
}
