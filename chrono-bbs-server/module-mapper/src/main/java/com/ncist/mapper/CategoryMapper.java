package com.ncist.mapper;

import com.ncist.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 分类数据访问
 */
@Mapper
public interface CategoryMapper {

    // 查询全部分类（用于内存组装三级树）
    @Select("SELECT id, name, parent_id AS parentId, level, sort_order AS sortOrder, icon, "
            + "created_at AS createdAt FROM category ORDER BY sort_order, id")
    List<Category> selectAll();

    // 按主键查询
    @Select("SELECT id, name, parent_id AS parentId, level, sort_order AS sortOrder, icon, "
            + "created_at AS createdAt FROM category WHERE id = #{id}")
    Category selectById(Long id);

    // 按父分类查询直接子分类
    @Select("SELECT id, name, parent_id AS parentId, level, sort_order AS sortOrder, icon, "
            + "created_at AS createdAt FROM category WHERE parent_id = #{parentId} ORDER BY sort_order, id")
    List<Category> selectByParentId(Long parentId);
}
