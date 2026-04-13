package com.blog.mapper;

import com.blog.entity.Tag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TagMapper {
    List<Tag> findAll();
    Tag findById(@Param("id") Long id);
    Tag findByName(@Param("name") String name);
    int insert(Tag tag);
    int delete(@Param("id") Long id);
    List<Tag> findTagsByNames(@Param("names") List<String> names);
}
