package com.lingecho.common.core.search.core;

import com.lingecho.common.core.search.config.SearchConfig;
import org.apache.lucene.store.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 目录提供者
 * 根据配置创建不同类型的Lucene Directory实例
 * 
 * @author HibiscusSearch Team
 * @version 1.0.0
 */
public final class DirectoryProvider {
    
    private DirectoryProvider() {
        // 工具类，不允许实例化
    }

    /**
     * 根据配置创建索引目录
     * 
     * @param cfg 搜索配置
     * @return Lucene Directory实例
     * @throws IOException 创建目录时可能抛出的异常
     */
    public static Directory createIndexDirectory(SearchConfig cfg) throws IOException {
        return switch (cfg.getStorageType()) {
            case MEMORY -> new RAMDirectory();
            case DISK -> FSDirectory.open(ensurePath(cfg.getIndexPath().toString()));
            case NIOFS -> new NIOFSDirectory(ensurePath(cfg.getIndexPath().toString()));
            case MMAP -> new MMapDirectory(ensurePath(cfg.getIndexPath().toString()));
            case JDBC -> new JdbcDirectory(cfg.getJdbcUrl(), cfg.getJdbcUser(),
                    cfg.getJdbcPassword(), cfg.getJdbcTable());
            default -> new MMapDirectory(ensurePath(cfg.getIndexPath().toString()));
        };
    }

    /**
     * 根据配置创建建议目录
     * 
     * @param cfg 搜索配置
     * @return Lucene Directory实例
     * @throws IOException 创建目录时可能抛出的异常
     */
    public static Directory createSuggestDirectory(SearchConfig cfg) throws IOException {
        return switch (cfg.getStorageType()) {
            case MEMORY -> new RAMDirectory();
            case DISK, NIOFS, MMAP -> {
                Path p = ensurePath(cfg.getIndexPath().resolve("suggest").toString());
                yield new MMapDirectory(p);
            }
            case JDBC -> new JdbcDirectory(cfg.getJdbcUrl(), cfg.getJdbcUser(),
                    cfg.getJdbcPassword(), cfg.getJdbcSuggestTable());
            default -> new RAMDirectory();
        };
    }

    /**
     * 确保路径存在，如果不存在则创建
     * 
     * @param path 路径字符串
     * @return 路径对象
     * @throws IOException 创建目录时可能抛出的异常
     */
    private static Path ensurePath(String path) throws IOException {
        Path p = Paths.get(path);
        java.nio.file.Files.createDirectories(p);
        return p;
    }
}
