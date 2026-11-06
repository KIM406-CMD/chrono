package com.ncist.mapper;

import com.ncist.entity.Category;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

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

    // 新增分类
    @Insert("INSERT INTO category (name, parent_id, level, sort_order, icon, created_at) "
            + "VALUES (#{name}, #{parentId}, #{level}, #{sortOrder}, #{icon}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Category category);

    // 修改分类（仅名称/排序/图标，不允许改层级与父级）
    @Update("""
            <script>
            UPDATE category <set>
            <if test="name != null"> name = #{name}, </if>
            <if test="sortOrder != null"> sort_order = #{sortOrder}, </if>
            <if test="icon != null"> icon = #{icon}, </if>
            </set> WHERE id = #{id}
            </script>
            """)
    int update(Category category);

    @Delete("DELETE FROM category WHERE id = #{id}")
    int deleteById(Long id);

    // 子分类数量（删除前校验）
    @Select("SELECT COUNT(*) FROM category WHERE parent_id = #{parentId}")
    long countChildren(Long parentId);
}
