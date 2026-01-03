package com.lingecho.knowledge.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lingecho.common.core.BaseEntity;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Knowledge 实体类
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("knowledge")
@EqualsAndHashCode(callSuper = true)
public class Knowledge extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long userId;

    private Long groupId;

    private String knowledgeKey;

    private String knowledgeName;

    private String provider; // aliyun, milvus, pinecone, qdrant, elasticsearch

    private String config; // JSON格式的配置信息
}

