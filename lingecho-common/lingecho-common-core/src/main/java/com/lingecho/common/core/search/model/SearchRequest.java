package com.lingecho.common.core.search.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 搜索请求模型类
 * 包含搜索查询、分页参数、过滤条件等搜索请求信息
 * 
 * @author heathcetide
 */
@Data
public class SearchRequest {

    /**
     * 搜索查询
     */
    private String query;

    /**
     * 页码
     */
    private int page = 1;

    /**
     * 每页大小
     */
    private int pageSize = 10;

    /**
     * 搜索字段
     */
    private List<String> fields = new ArrayList<>();

    /**
     * 过滤条件
     */
    private List<Filter> filters = new ArrayList<>();

    /**
     * 排序字段
     */
    private List<SortField> sortFields = new ArrayList<>();

    /**
     * 高亮
     */
    private boolean highlight = true;

    /**
     * 最大结果数
     */
    private int maxResults = 100;

    /**
     * 最小得分
     */
    private float minScore = 0.0f;
    
    /**
     * 创建搜索请求
     * 
     * @param query 搜索查询
     */
    public SearchRequest(String query) {
        this.query = query;
    }
    
    /**
     * 创建搜索请求
     * 
     * @param query 搜索查询
     * @param page 页码
     * @param pageSize 每页大小
     */
    public SearchRequest(String query, int page, int pageSize) {
        this.query = query;
        this.page = page;
        this.pageSize = pageSize;
    }
    
    // Getters and Setters

    public SearchRequest setQuery(String query) {
        this.query = query;
        return this;
    }

    public SearchRequest setPage(int page) {
        this.page = page;
        return this;
    }

    public SearchRequest setPageSize(int pageSize) {
        this.pageSize = pageSize;
        return this;
    }
    
    public List<String> getFields() {
        return new ArrayList<>(fields);
    }
    
    public SearchRequest setFields(List<String> fields) {
        this.fields = fields != null ? new ArrayList<>(fields) : new ArrayList<>();
        return this;
    }
    
    public List<Filter> getFilters() {
        return new ArrayList<>(filters);
    }
    
    public SearchRequest setFilters(List<Filter> filters) {
        this.filters = filters != null ? new ArrayList<>(filters) : new ArrayList<>();
        return this;
    }
    
    public List<SortField> getSortFields() {
        return new ArrayList<>(sortFields);
    }
    
    public SearchRequest setSortFields(List<SortField> sortFields) {
        this.sortFields = sortFields != null ? new ArrayList<>(sortFields) : new ArrayList<>();
        return this;
    }

    public SearchRequest setHighlight(boolean highlight) {
        this.highlight = highlight;
        return this;
    }

    public SearchRequest setMaxResults(int maxResults) {
        this.maxResults = maxResults;
        return this;
    }

    public SearchRequest setMinScore(float minScore) {
        this.minScore = minScore;
        return this;
    }
    
    /**
     * 添加搜索字段
     * 
     * @param field 字段名
     * @return 当前请求实例
     */
    public SearchRequest addField(String field) {
        this.fields.add(field);
        return this;
    }
    
    /**
     * 添加过滤条件
     * 
     * @param filter 过滤条件
     * @return 当前请求实例
     */
    public SearchRequest addFilter(Filter filter) {
        this.filters.add(filter);
        return this;
    }
    
    /**
     * 添加排序字段
     * 
     * @param sortField 排序字段
     * @return 当前请求实例
     */
    public SearchRequest addSortField(SortField sortField) {
        this.sortFields.add(sortField);
        return this;
    }
    
    /**
     * 获取偏移量
     * 
     * @return 偏移量
     */
    public int getOffset() {
        return (page - 1) * pageSize;
    }
    
    @Override
    public String toString() {
        return "SearchRequest{" +
                "query='" + query + '\'' +
                ", page=" + page +
                ", pageSize=" + pageSize +
                ", fields=" + fields +
                ", filters=" + filters +
                ", sortFields=" + sortFields +
                ", highlight=" + highlight +
                ", maxResults=" + maxResults +
                ", minScore=" + minScore +
                '}';
    }
    
    /**
     * 过滤条件
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Filter {
        private String field;
        private String value;
        private FilterType type;

        public enum FilterType {
            EQUALS, NOT_EQUALS, CONTAINS, NOT_CONTAINS, GREATER_THAN, LESS_THAN
        }
    }
    
    /**
     * 排序字段
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SortField {
        private String field;
        private SortOrder order;

        public enum SortOrder {
            ASC, DESC
        }
    }
}
