package com.lingecho.knowledge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lingecho.knowledge.entity.Knowledge;
import org.apache.ibatis.annotations.Mapper;

/**
 * Knowledge Mapper
 */
@Mapper
public interface KnowledgeMapper extends BaseMapper<Knowledge> {
}

