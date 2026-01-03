package com.lingecho.common.core.search.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 搜索结果模型类
 * 包含搜索结果列表、总命中数、分页信息等
 * 
 * @author heathcetide
 */
@Data
public class SearchResult {

    /**
     * 搜索结果列表
     */
    private List<SearchHit> hits = new ArrayList<>();

    /**
     * 总命中数
     */
    private long totalHits;

    /**
     * 当前页码
     */
    private int page;

    /**
     * 每页数量
     */
    private int pageSize;

    /**
     * 搜索耗时
     */
    private long searchTime;

    /**
     * 查询
     */
    private String query;

    /**
     * 建议列表
     */
    private List<String> suggestions = new ArrayList<>();
    
    /**
     * 创建搜索结果
     */
    public SearchResult() {
    }
    
    /**
     * 创建搜索结果
     * 
     * @param query 搜索查询
     * @param totalHits 总命中数
     */
    public SearchResult(String query, long totalHits) {
        this.query = query;
        this.totalHits = totalHits;
    }
    
    // Getters and Setters
    
    public List<SearchHit> getHits() {
        return new ArrayList<>(hits);
    }
    
    public SearchResult setHits(List<SearchHit> hits) {
        this.hits = hits != null ? new ArrayList<>(hits) : new ArrayList<>();
        return this;
    }

    public SearchResult setTotalHits(long totalHits) {
        this.totalHits = totalHits;
        return this;
    }

    public SearchResult setPage(int page) {
        this.page = page;
        return this;
    }

    public SearchResult setPageSize(int pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    public SearchResult setSearchTime(long searchTime) {
        this.searchTime = searchTime;
        return this;
    }

    public SearchResult setQuery(String query) {
        this.query = query;
        return this;
    }
    
    public List<String> getSuggestions() {
        return new ArrayList<>(suggestions);
    }
    
    public SearchResult setSuggestions(List<String> suggestions) {
        this.suggestions = suggestions != null ? new ArrayList<>(suggestions) : new ArrayList<>();
        return this;
    }
    
    /**
     * 添加搜索结果
     * 
     * @param hit 搜索结果
     * @return 当前结果实例
     */
    public SearchResult addHit(SearchHit hit) {
        this.hits.add(hit);
        return this;
    }
    
    /**
     * 添加建议
     * 
     * @param suggestion 建议
     * @return 当前结果实例
     */
    public SearchResult addSuggestion(String suggestion) {
        this.suggestions.add(suggestion);
        return this;
    }
    
    /**
     * 获取总页数
     * 
     * @return 总页数
     */
    public int getTotalPages() {
        if (pageSize <= 0) return 0;
        return (int) Math.ceil((double) totalHits / pageSize);
    }
    
    /**
     * 是否有下一页
     * 
     * @return 是否有下一页
     */
    public boolean hasNextPage() {
        return page < getTotalPages();
    }
    
    /**
     * 是否有上一页
     * 
     * @return 是否有上一页
     */
    public boolean hasPreviousPage() {
        return page > 1;
    }
    
    /**
     * 获取当前页的结果数量
     * 
     * @return 当前页结果数量
     */
    public int getCurrentPageSize() {
        return hits.size();
    }
    
    @Override
    public String toString() {
        return "SearchResult{" +
                "totalHits=" + totalHits +
                ", page=" + page +
                ", pageSize=" + pageSize +
                ", searchTime=" + searchTime + "ms" +
                ", query='" + query + '\'' +
                ", hitsSize=" + hits.size() +
                ", suggestionsSize=" + suggestions.size() +
                '}';
    }
    
    /**
     * 搜索结果项
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SearchHit {
        private String id;
        private String title;
        private String content;
        private String type;
        private float score;
        private String highlightedTitle;
        private String highlightedContent;
        private java.util.Map<String, String> metadata = new java.util.HashMap<>();
        
        public SearchHit(String id, String title, String content, float score) {
            this.id = id;
            this.title = title;
            this.content = content;
            this.score = score;
        }

        public SearchHit setType(String type) {
            this.type = type;
            return this;
        }


        public SearchHit setId(String id) {
            this.id = id;
            return this;
        }

        public SearchHit setTitle(String title) {
            this.title = title;
            return this;
        }

        public SearchHit setContent(String content) {
            this.content = content;
            return this;
        }

        public SearchHit setScore(float score) {
            this.score = score;
            return this;
        }

        public SearchHit setHighlightedTitle(String highlightedTitle) {
            this.highlightedTitle = highlightedTitle;
            return this;
        }

        public SearchHit setHighlightedContent(String highlightedContent) {
            this.highlightedContent = highlightedContent;
            return this;
        }
        
        public java.util.Map<String, String> getMetadata() {
            return new java.util.HashMap<>(metadata);
        }
        
        public SearchHit setMetadata(java.util.Map<String, String> metadata) {
            this.metadata = metadata != null ? new java.util.HashMap<>(metadata) : new java.util.HashMap<>();
            return this;
        }
        
        /**
         * 添加元数据
         * 
         * @param key 键
         * @param value 值
         * @return 当前结果项实例
         */
        public SearchHit addMetadata(String key, String value) {
            this.metadata.put(key, value);
            return this;
        }
        
        /**
         * 获取元数据值
         * 
         * @param key 键
         * @return 值，如果不存在返回null
         */
        public String getMetadata(String key) {
            return this.metadata.get(key);
        }
        
        @Override
        public String toString() {
            return "SearchHit{" +
                    "id='" + id + '\'' +
                    ", title='" + title + '\'' +
                    ", score=" + score +
                    ", contentLength=" + (content != null ? content.length() : 0) +
                    '}';
        }
    }
}
