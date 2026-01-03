package com.lingecho.common.core.search;

import com.lingecho.common.core.search.config.SearchConfig;
import com.lingecho.common.core.search.core.SearchEngine;
import com.lingecho.common.core.search.model.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;

/**
 * HibiscusSearch - 基于Lucene的高效搜索引擎
 * 使用示例：
 * <pre>
 * // 创建搜索引擎实例
 * HibiscusSearch search = new HibiscusSearch();
 *
 * // 索引文档
 * search.indexDocument("id1", "title1", "content1");
 * search.indexDocument("id2", "title2", "content2");
 *
 * // 搜索
 * SearchResult result = search.search("关键词");
 *
 * // 关闭搜索引擎
 * search.close();
 * </pre>
 *
 * @author HibiscusSearch Team
 * @version 1.0.0
 */
@Slf4j
public class HibiscusSearch implements AutoCloseable {

    private SearchEngine searchEngine;
    /**
     * -- GETTER --
     *  获取搜索配置
     */
    @Getter
    private final SearchConfig config;

    /**
     * 使用默认配置创建搜索引擎
     */
    public HibiscusSearch() {
        this(new SearchConfig());
    }

    /**
     * 使用自定义配置创建搜索引擎
     *
     * @param config 搜索配置
     */
    public HibiscusSearch(SearchConfig config) {
        this.config = config;
        // 延迟初始化搜索引擎，避免在构造函数中初始化失败
        this.searchEngine = null;
        log.info("HibiscusSearch created with config: {}", config);
    }
    
    /**
     * 延迟初始化搜索引擎
     */
    private synchronized void initializeSearchEngine() {
        if (this.searchEngine == null) {
            try {
                this.searchEngine = new SearchEngine(config);
                log.info("SearchEngine initialized successfully");
            } catch (Exception e) {
                log.error("Failed to initialize SearchEngine", e);
                throw new RuntimeException("Failed to initialize SearchEngine", e);
            }
        }
    }
    
    /**
     * 获取搜索引擎实例，如果未初始化则进行初始化
     */
    private SearchEngine getSearchEngine() {
        if (this.searchEngine == null) {
            initializeSearchEngine();
        }
        return this.searchEngine;
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
        getSearchEngine().indexDocument(id, title, content);
        log.debug("Indexed document: id={}, title={}", id, title);
    }

    /**
     * 索引单个文档（带类型）
     *
     * @param id 文档ID
     * @param type 文档类型
     * @param title 文档标题
     * @param content 文档内容
     * @throws IOException 索引异常
     */
    public void indexDocument(String id, String type, String title, String content) throws IOException {
        getSearchEngine().indexDocument(id, type, title, content);
        log.debug("Indexed document: id={}, type={}, title={}", id, type, title);
    }

    /**
     * 批量索引文档
     *
     * @param documents 文档列表
     * @throws IOException 索引异常
     */
    public void indexDocuments(List<Document> documents) throws IOException {
        getSearchEngine().indexDocuments(documents);
        log.info("Indexed {} documents", documents.size());
    }

    /**
     * 搜索文档
     *
     * @param query 搜索查询
     * @return 搜索结果
     * @throws IOException 搜索异常
     */
    public SearchResult search(String query) throws IOException {
        SearchRequest request = new SearchRequest(query);
        return getSearchEngine().search(request);
    }

    /**
     * 搜索文档
     *
     * @param request 搜索请求
     * @return 搜索结果
     * @throws IOException 搜索异常
     */
    public SearchResult search(SearchRequest request) throws IOException {
        SearchResult result = getSearchEngine().search(request);
        log.debug("Search completed: query={}, hits={}", request.getQuery(), result.getTotalHits());
        return result;
    }

    /**
     * 删除文档
     *
     * @param id 文档ID
     * @throws IOException 删除异常
     */
    public void deleteDocument(String id) throws IOException {
        getSearchEngine().deleteDocument(id);
        log.debug("Deleted document: id={}", id);
    }

    /**
     * 删除文档（带类型）
     *
     * @param id 文档ID
     * @param type 文档类型
     * @throws IOException 删除异常
     */
    public void deleteDocument(String id, String type) throws IOException {
        getSearchEngine().deleteDocument(id, type);
        log.debug("Deleted document: id={}, type={}", id, type);
    }

    /**
     * 批量删除文档
     *
     * @param ids 文档ID列表
     * @throws IOException 删除异常
     */
    public void deleteDocuments(List<String> ids) throws IOException {
        getSearchEngine().deleteDocuments(ids);
        log.info("Deleted {} documents", ids.size());
    }

    /**
     * 根据条件删除文档
     *
     * @param field 字段名
     * @param value 字段值
     * @throws IOException 删除异常
     */
    public void deleteDocumentsByField(String field, String value) throws IOException {
        getSearchEngine().deleteDocumentsByField(field, value);
        log.info("Deleted documents by field: {}={}", field, value);
    }

    /**
     * 清空索引
     *
     * @throws IOException 清空异常
     */
    public void clearIndex() throws IOException {
        getSearchEngine().clearIndex();
        log.info("Index cleared");
    }

    /**
     * 获取索引统计信息
     *
     * @return 索引统计信息
     * @throws IOException 获取异常
     */
    public IndexStats getIndexStats() throws IOException {
        return getSearchEngine().getIndexStats();
    }

    /**
     * 获取索引健康状态
     *
     * @return 索引健康状态信息
     * @throws IOException 获取异常
     */
    public IndexHealthInfo getIndexHealthInfo() throws IOException {
        return getSearchEngine().getIndexHealthInfo();
    }

    /**
     * 检查索引是否需要优化
     *
     * @return 是否需要优化
     * @throws IOException 检查异常
     */
    public boolean needsOptimization() throws IOException {
        return getSearchEngine().needsOptimization();
    }

    /**
     * 获取搜索建议
     *
     * @param query 查询前缀
     * @return 搜索建议列表
     * @throws IOException 获取异常
     */
    public List<String> getSuggestions(String query) throws IOException {
        return getSearchEngine().getSuggestions(query);
    }

    /**
     * 优化索引
     *
     * @throws IOException 优化异常
     */
    public void optimizeIndex() throws IOException {
        getSearchEngine().optimizeIndex();
        log.info("Index optimized");
    }

    /**
     * 关闭搜索引擎，释放资源
     */
    @Override
    public void close() throws IOException {
        if (searchEngine != null) {
            getSearchEngine().close();
            log.info("HibiscusSearch closed");
        }
    }

    /**
     * 预热缓存
     *
     * @return 预热缓存
     *
     * @param popularQueries 热门查询列表
     */
    public void warmupCache(List<String> popularQueries) {
        getSearchEngine().warmupCache(popularQueries);
    }
}
