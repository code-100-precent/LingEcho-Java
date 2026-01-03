package com.lingecho.common.core.search.model;

import lombok.Data;

/**
 * 索引统计信息模型类
 * 包含索引文档数量、最大文档ID等统计信息
 * 
 * @author heathcetide
 */
@Data
public class IndexStats {

    /**
     * 文档数量
     */
    private final int numDocs;

    /**
     * 最大文档ID
     */
    private final int maxDoc;

    /**
     * 索引大小（字节）
     */
    private final long indexSize;

    /**
     * 最后修改时间
     */
    private final long lastModified;
    
    /**
     * 创建索引统计信息
     * 
     * @param numDocs 文档数量
     * @param maxDoc 最大文档ID
     */
    public IndexStats(int numDocs, int maxDoc) {
        this(numDocs, maxDoc, 0, System.currentTimeMillis());
    }
    
    /**
     * 创建索引统计信息
     * 
     * @param numDocs 文档数量
     * @param maxDoc 最大文档ID
     * @param indexSize 索引大小（字节）
     * @param lastModified 最后修改时间
     */
    public IndexStats(int numDocs, int maxDoc, long indexSize, long lastModified) {
        this.numDocs = numDocs;
        this.maxDoc = maxDoc;
        this.indexSize = indexSize;
        this.lastModified = lastModified;
    }

    /**
     * 获取索引大小（格式化）
     * 
     * @return 格式化的索引大小
     */
    public String getFormattedIndexSize() {
        if (indexSize < 1024) {
            return indexSize + " B";
        } else if (indexSize < 1024 * 1024) {
            return String.format("%.2f KB", indexSize / 1024.0);
        } else if (indexSize < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", indexSize / (1024.0 * 1024.0));
        } else {
            return String.format("%.2f GB", indexSize / (1024.0 * 1024.0 * 1024.0));
        }
    }
    
    /**
     * 获取最后修改时间（格式化）
     * 
     * @return 格式化的最后修改时间
     */
    public String getFormattedLastModified() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                .format(new java.util.Date(lastModified));
    }
    
    /**
     * 检查索引是否为空
     * 
     * @return 如果索引为空返回true
     */
    public boolean isEmpty() {
        return numDocs == 0;
    }
    
    /**
     * 获取删除的文档数量
     * 
     * @return 删除的文档数量
     */
    public int getDeletedDocs() {
        return maxDoc - numDocs;
    }

    @Override
    public String toString() {
        return "IndexStats{" +
                "numDocs=" + numDocs +
                ", maxDoc=" + maxDoc +
                ", indexSize=" + getFormattedIndexSize() +
                ", lastModified=" + getFormattedLastModified() +
                ", deletedDocs=" + getDeletedDocs() +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        IndexStats that = (IndexStats) o;
        return numDocs == that.numDocs && maxDoc == that.maxDoc;
    }
    
    @Override
    public int hashCode() {
        int result = numDocs;
        result = 31 * result + maxDoc;
        return result;
    }
}
