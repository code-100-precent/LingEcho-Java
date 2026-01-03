package com.lingecho.common.core.search.model;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 文档模型类
 * 表示要索引的文档，包含文档ID、标题、内容和可选的元数据
 * 
 * @author heathcetide
 */
@Data
public class Document {

    /**
     * 文档ID
     */
    private String id;

    /**
     * 文档类型（如：article、user、course等）
     */
    private String type;

    /**
     * 文档标题
     */
    private String title;

    /**
     * 文档内容
     */
    private String content;

    /**
     * 元数据
     */
    private Map<String, String> metadata;

    /**
     * 文档创建时间戳
     */
    private long timestamp;
    
    /**
     * 默认构造函数 - 用于 Jackson 反序列化
     */
    public Document() {
        this.metadata = new HashMap<>();
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * 创建文档
     * 
     * @param id 文档ID
     * @param type 文档类型
     * @param title 文档标题
     * @param content 文档内容
     */
    public Document(String id, String type, String title, String content) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.content = content;
        this.metadata = new HashMap<>();
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * 创建文档
     * 
     * @param id 文档ID
     * @param type 文档类型
     * @param title 文档标题
     * @param content 文档内容
     * @param metadata 元数据
     */
    public Document(String id, String type, String title, String content, Map<String, String> metadata) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.content = content;
        this.metadata = metadata != null ? new HashMap<>(metadata) : new HashMap<>();
        this.timestamp = System.currentTimeMillis();
    }
    
    // Getters and Setters

    public Document setId(String id) {
        this.id = id;
        return this;
    }

    public Document setType(String type) {
        this.type = type;
        return this;
    }

    public Document setTitle(String title) {
        this.title = title;
        return this;
    }

    public Document setContent(String content) {
        this.content = content;
        return this;
    }
    
    public Map<String, String> getMetadata() {
        return new HashMap<>(metadata);
    }
    
    public Document setMetadata(Map<String, String> metadata) {
        this.metadata = metadata != null ? new HashMap<>(metadata) : new HashMap<>();
        return this;
    }

    public Document setTimestamp(long timestamp) {
        this.timestamp = timestamp;
        return this;
    }
    
    /**
     * 添加元数据
     * 
     * @param key 键
     * @param value 值
     * @return 当前文档实例
     */
    public Document addMetadata(String key, String value) {
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
    
    /**
     * 移除元数据
     * 
     * @param key 键
     * @return 当前文档实例
     */
    public Document removeMetadata(String key) {
        this.metadata.remove(key);
        return this;
    }
    
    @Override
    public String toString() {
        return "Document{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", title='" + title + '\'' +
                ", contentLength=" + (content != null ? content.length() : 0) +
                ", metadataSize=" + metadata.size() +
                ", timestamp=" + timestamp +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        Document document = (Document) o;
        if (id == null || document.id == null) return false;
        if (type == null || document.type == null) return false;
        return id.equals(document.id) && type.equals(document.type);
    }
    
    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }
}
