package com.lingecho.common.core.search.service;

import com.lingecho.common.core.search.HibiscusSearch;
import com.lingecho.common.core.search.model.Document;
import com.lingecho.common.core.search.model.SearchRequest;
import com.lingecho.common.core.search.model.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 * 搜索服务
 * 提供Spring集成的搜索功能
 *
 * @author HibiscusSearch Team
 * @version 1.0.0
 */
@Service
public class SearchService {

    @Autowired
    private HibiscusSearch hibiscusSearch;

    /**
     * 索引单个文档
     */
    public void indexDocument(String id, String title, String content) throws IOException {
        hibiscusSearch.indexDocument(id, title, content);
    }

    /**
     * 批量索引文档
     */
    public void indexDocuments(List<Document> documents) throws IOException {
        hibiscusSearch.indexDocuments(documents);
    }

    /**
     * 搜索文档
     */
    public SearchResult search(String query) throws IOException {
        return hibiscusSearch.search(query);
    }

    /**
     * 高级搜索
     */
    public SearchResult search(SearchRequest request) throws IOException {
        return hibiscusSearch.search(request);
    }

    /**
     * 删除文档
     */
    public void deleteDocument(String id) throws IOException {
        hibiscusSearch.deleteDocument(id);
    }

    /**
     * 批量删除文档
     */
    public void deleteDocuments(List<String> ids) throws IOException {
        hibiscusSearch.deleteDocuments(ids);
    }

    /**
     * 清空索引
     */
    public void clearIndex() throws IOException {
        hibiscusSearch.clearIndex();
    }

    /**
     * 获取索引统计信息
     */
    public Object getIndexStats() throws IOException {
        return hibiscusSearch.getIndexStats();
    }

    /**
     * 获取搜索建议
     */
    public List<String> getSuggestions(String query) throws IOException {
        return hibiscusSearch.getSuggestions(query);
    }

    /**
     * 优化索引
     */
    public void optimizeIndex() throws IOException {
        hibiscusSearch.optimizeIndex();
    }

    /**
     * 预热缓存
     */
    public void warmupCache(List<String> popularQueries) {
        hibiscusSearch.warmupCache(popularQueries);
    }
}
