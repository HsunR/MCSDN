package com.blog.mapper;

import com.blog.entity.CsdnSyncConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

@Mapper
public interface CsdnSyncConfigMapper {
    CsdnSyncConfig findLatest();
    int insert(CsdnSyncConfig config);
    int update(CsdnSyncConfig config);
    int updateLastSyncAt(@Param("lastSyncAt") LocalDateTime lastSyncAt);
}
