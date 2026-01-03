package com.lingecho.common.core.search.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 索引健康状态信息
 * 
 * @author heathcetide
 */
@Data
@AllArgsConstructor
public class IndexHealthInfo {

    /**
     * 索引健康状态
     */
    private final boolean healthy;

    /**
     * 索引状态
     */
    private final String status;

    /**
     * 文档数量
     */
    private final int numDocs;

    /**
     * 删除文档数量
     */
    private final int deletedDocs;

    /**
     * 健康得分
     */
    private final double healthScore;
    
    public IndexHealthInfo(boolean healthy, String status, int numDocs, int deletedDocs) {
        this.healthy = healthy;
        this.status = status;
        this.numDocs = numDocs;
        this.deletedDocs = deletedDocs;
        this.healthScore = numDocs > 0 ? (double) numDocs / (numDocs + deletedDocs) : 1.0;
    }
}
