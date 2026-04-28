package com.blog.mapper;

import com.blog.entity.Portfolio;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PortfolioMapper {
    List<Portfolio> findAllOrderBySortOrder();
    Portfolio findById(@Param("id") Long id);
    int insert(Portfolio portfolio);
    int update(Portfolio portfolio);
    int delete(@Param("id") Long id);
    int count();
    List<Portfolio> findAllPublished();
}
