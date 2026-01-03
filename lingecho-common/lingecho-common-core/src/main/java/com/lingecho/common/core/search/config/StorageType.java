package com.lingecho.common.core.search.config;

/**
 * 存储类型枚举
 * 群的存储类型，包括内存、磁盘、NIO、MMAP和JDBC等
 * 
 * @author HibiscusSearch Team
 * @version 1.0.0
 */
public enum StorageType {
    /**
     * 内存存储 - 使用RAMDirectory，数据存储在内存中，速度快但重启后丢失
     */
    MEMORY,
    
    /**
     * 磁盘存储 - 使用FSDirectory，数据存储在文件系统中
     */
    DISK,
    
    /**
     * NIO文件系统存储 - 使用NIOFSDirectory，基于NIO的文件系统访问
     */
    NIOFS,
    
    /**
     * 内存映射存储 - 使用MMapDirectory，通过内存映射提高大文件访问性能
     */
    MMAP,
    
    /**
     * JDBC数据库存储 - 使用JdbcDirectory，数据存储在关系数据库中
     */
    JDBC,

    /**
     * 文件存储
     */
    FILESYSTEM
}
