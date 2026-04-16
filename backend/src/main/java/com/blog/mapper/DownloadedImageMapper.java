package com.blog.mapper;

import com.blog.entity.DownloadedImage;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DownloadedImageMapper {
    DownloadedImage findByUrlHash(@Param("urlHash") String urlHash);
    void insert(DownloadedImage image);
}
