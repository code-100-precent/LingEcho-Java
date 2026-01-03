package com.lingecho.billing.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lingecho.billing.entity.UsageRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * UsageRecord Mapper
 */
@Mapper
public interface UsageRecordMapper extends BaseMapper<UsageRecord> {

    /**
     * 获取使用统计
     */
    @Select("SELECT usage_type, SUM(total_tokens) as total_tokens, SUM(call_duration) as call_duration, " +
            "SUM(audio_duration) as audio_duration, SUM(storage_size) as storage_size, SUM(api_call_count) as api_call_count " +
            "FROM usage_records " +
            "WHERE user_id = #{userId} AND usage_time >= #{startTime} AND usage_time <= #{endTime} " +
            "GROUP BY usage_type")
    List<Map<String, Object>> getUsageStatistics(Long userId, LocalDateTime startTime, LocalDateTime endTime);
}

