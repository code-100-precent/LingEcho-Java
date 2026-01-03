package com.lingecho.common.core.search.config;

import lombok.Data;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 搜索配置类
 * 用于配置HibiscusSearch的各种参数，包括索引路径、分析器类型、搜索参数等
 * 
 * @author HibiscusSearch Team
 * @version 1.0.0
 */
@Data
public class SearchConfig {

    // 默认的索引路径，表示索引文件存储的位置
    public static final String DEFAULT_INDEX_PATH = "./custom_index";

    // 默认的分析器类型，"standard" 表示使用标准分析器
    public static final String DEFAULT_ANALYZER_TYPE = "standard";

    // 默认的最大结果数，表示查询时返回的最大文档数
    public static final int DEFAULT_MAX_RESULTS = 100;

    // 默认的最小得分，查询时返回的文档得分必须大于此值
    public static final int DEFAULT_MIN_SCORE = 0;

    // 默认的高亮显示是否启用
    public static final boolean DEFAULT_HIGHLIGHT_ENABLED = true;

    // 默认的高亮标签，设置高亮部分前的标签
    public static final String DEFAULT_HIGHLIGHT_PRE_TAG = "<em>";

    // 默认的高亮标签，设置高亮部分后的标签
    public static final String DEFAULT_HIGHLIGHT_POST_TAG = "</em>";

    // 默认的高亮片段大小，即每个高亮片段的最大字符数
    public static final int DEFAULT_HIGHLIGHT_FRAGMENT_SIZE = 150;

    // 默认的高亮最大片段数，表示每个查询返回的高亮片段数
    public static final int DEFAULT_HIGHLIGHT_MAX_FRAGMENTS = 3;

    // 默认的建议功能是否启用
    public static final boolean DEFAULT_SUGGEST_ENABLED = true;

    // 默认的最大建议结果数，表示建议功能返回的最大结果数
    public static final int DEFAULT_SUGGEST_MAX_RESULTS = 10;

    // 索引文件路径，用于指定索引数据存储的具体位置
    private Path indexPath;

    // 分析器类型，控制使用哪种文本分析器来解析文本
    private String analyzerType;

    // 最大返回结果数，限制查询时返回的结果个数
    private int maxResults;

    // 最小得分，只有得分高于此值的文档才会被返回
    private float minScore;

    // 是否启用高亮显示功能
    private boolean highlightEnabled;

    // 高亮显示前标签，设置高亮内容的开始标签
    private String highlightPreTag;

    // 高亮显示后标签，设置高亮内容的结束标签
    private String highlightPostTag;

    // 高亮片段大小，控制每个高亮片段的字符数限制
    private int highlightFragmentSize;

    // 高亮最大片段数，限制返回的高亮片段的数量
    private int highlightMaxFragments;

    // 是否启用建议功能（如自动补全、拼写建议等）
    private boolean suggestEnabled;

    // 建议返回的最大结果数，控制建议功能返回的建议数量
    private int suggestMaxResults;

    // 是否启用自动提交功能
    private boolean autoCommit;

    // 提交间隔时间，单位是毫秒。表示在此时间间隔内提交一次索引
    private int commitInterval;

    // 存储类型，控制索引数据的存储方式
    private StorageType storageType;

    // JDBC连接URL，当使用JDBC存储时使用
    private String jdbcUrl;

    // JDBC用户名，当使用JDBC存储时使用
    private String jdbcUser;

    // JDBC密码，当使用JDBC存储时使用
    private String jdbcPassword;

    // JDBC索引表名，当使用JDBC存储时使用
    private String jdbcTable;

    // JDBC建议表名，当使用JDBC存储时使用
    private String jdbcSuggestTable;

    /**
     * 使用默认配置创建SearchConfig实例
     */
    public SearchConfig() {
        // 设置默认索引路径
        this.indexPath = Paths.get(DEFAULT_INDEX_PATH);

        // 设置默认分析器类型
        this.analyzerType = DEFAULT_ANALYZER_TYPE;

        // 设置默认最大结果数
        this.maxResults = DEFAULT_MAX_RESULTS;

        // 设置默认最小得分
        this.minScore = DEFAULT_MIN_SCORE;

        // 设置默认高亮显示启用状态
        this.highlightEnabled = DEFAULT_HIGHLIGHT_ENABLED;

        // 设置默认高亮前标签
        this.highlightPreTag = DEFAULT_HIGHLIGHT_PRE_TAG;

        // 设置默认高亮后标签
        this.highlightPostTag = DEFAULT_HIGHLIGHT_POST_TAG;

        // 设置默认高亮片段大小
        this.highlightFragmentSize = DEFAULT_HIGHLIGHT_FRAGMENT_SIZE;

        // 设置默认高亮最大片段数
        this.highlightMaxFragments = DEFAULT_HIGHLIGHT_MAX_FRAGMENTS;

        // 设置默认建议功能启用状态
        this.suggestEnabled = DEFAULT_SUGGEST_ENABLED;

        // 设置默认最大建议结果数
        this.suggestMaxResults = DEFAULT_SUGGEST_MAX_RESULTS;

        // 默认启用自动提交
        this.autoCommit = true;

        // 设置默认提交间隔时间为1000毫秒（即1秒）
        this.commitInterval = 1000;

        this.storageType = StorageType.FILESYSTEM; // 默认使用文件系统，避免数据库存储问题
        // 设置默认JDBC配置
        this.jdbcUrl = "jdbc:mysql://cd-cynosdbmysql-grp-lfa6zfg0.sql.tencentcdb.com:23771/co_code100_db?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&noAccessToProcedureBodies=false";

        this.jdbcUser = "root";

        this.jdbcPassword = "ct288513832##";

        this.jdbcTable = "co_lucene_index";

        this.jdbcSuggestTable = "co_lucene_suggest";
    }

    /**
     * 使用指定索引路径创建SearchConfig
     * 
     * @param indexPath 索引路径
     */
    public SearchConfig(String indexPath) {
        this();
        this.indexPath = Paths.get(indexPath);
    }
    
    /**
     * 使用指定索引路径创建SearchConfig
     * 
     * @param indexPath 索引路径
     */
    public SearchConfig(Path indexPath) {
        this();
        this.indexPath = indexPath;
    }

    public SearchConfig setIndexPath(Path indexPath) {
        this.indexPath = indexPath;
        return this;
    }
    
    public SearchConfig setIndexPath(String indexPath) {
        this.indexPath = Paths.get(indexPath);
        return this;
    }

    public SearchConfig setAnalyzerType(String analyzerType) {
        this.analyzerType = analyzerType;
        return this;
    }

    public SearchConfig setMaxResults(int maxResults) {
        this.maxResults = maxResults;
        return this;
    }

    public SearchConfig setMinScore(float minScore) {
        this.minScore = minScore;
        return this;
    }

    public SearchConfig setHighlightEnabled(boolean highlightEnabled) {
        this.highlightEnabled = highlightEnabled;
        return this;
    }

    public SearchConfig setHighlightPreTag(String highlightPreTag) {
        this.highlightPreTag = highlightPreTag;
        return this;
    }

    public SearchConfig setHighlightPostTag(String highlightPostTag) {
        this.highlightPostTag = highlightPostTag;
        return this;
    }

    public SearchConfig setHighlightFragmentSize(int highlightFragmentSize) {
        this.highlightFragmentSize = highlightFragmentSize;
        return this;
    }

    public SearchConfig setHighlightMaxFragments(int highlightMaxFragments) {
        this.highlightMaxFragments = highlightMaxFragments;
        return this;
    }

    public SearchConfig setSuggestEnabled(boolean suggestEnabled) {
        this.suggestEnabled = suggestEnabled;
        return this;
    }

    public SearchConfig setSuggestMaxResults(int suggestMaxResults) {
        this.suggestMaxResults = suggestMaxResults;
        return this;
    }

    public SearchConfig setAutoCommit(boolean autoCommit) {
        this.autoCommit = autoCommit;
        return this;
    }

    public SearchConfig setCommitInterval(int commitInterval) {
        this.commitInterval = commitInterval;
        return this;
    }

}
