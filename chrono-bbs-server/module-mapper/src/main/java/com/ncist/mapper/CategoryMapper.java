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
 * 分类数据访问。
 *
 * <p>分类为三级树结构，查询时通常一次性取出全部分类后由业务层组装；
 * 新增时由调用方计算 {@code level} 与 {@code parentId}。</p>
 */
@Mapper
public interface CategoryMapper {

    /**
     * 查询全部分类，按排序序号、ID 升序，用于内存组装三级分类树。
     *
     * @return 全部分类列表，无记录时返回空列表
     */
    @Select("SELECT id, name, parent_id AS parentId, level, sort_order AS sortOrder, icon, "
            + "created_at AS createdAt FROM category ORDER BY sort_order, id")
    List<Category> selectAll();

    /**
     * 按主键查询分类。
     *
     * @param id 分类 ID
     * @return 分类记录，不存在时返回 null
     */
    @Select("SELECT id, name, parent_id AS parentId, level, sort_order AS sortOrder, icon, "
            + "created_at AS createdAt FROM category WHERE id = #{id}")
    Category selectById(Long id);

    /**
     * 查询指定父分类的直接子分类（仅下一级，不递归）。
     *
     * @param parentId 父分类 ID，0 表示查询一级分类
     * @return 子分类列表，按排序序号、ID 升序
     */
    @Select("SELECT id, name, parent_id AS parentId, level, sort_order AS sortOrder, icon, "
            + "created_at AS createdAt FROM category WHERE parent_id = #{parentId} ORDER BY sort_order, id")
    List<Category> selectByParentId(Long parentId);

    /**
     * 新增分类，并回填自增主键。
     *
     * @param category 分类信息，name/parentId/level 必填
     * @return 受影响行数
     */
    @Insert("INSERT INTO category (name, parent_id, level, sort_order, icon, created_at) "
            + "VALUES (#{name}, #{parentId}, #{level}, #{sortOrder}, #{icon}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Category category);

    /**
     * 动态更新分类，仅名称、排序与图标可改，不允许修改父级和层级。
     *
     * @param category 分类信息，id 必填，其余字段为 null 表示不修改
     * @return 受影响行数，0 表示分类不存在
     */
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

    /**
     * 按主键删除分类。
     *
     * <p>调用方须先通过 {@link #countChildren(Long)} 等校验分类未被商品或子分类引用。</p>
     *
     * @param id 分类 ID
     * @return 受影响行数，0 表示分类不存在
     */
    @Delete("DELETE FROM category WHERE id = #{id}")
    int deleteById(Long id);

    /**
     * 统计指定分类的直接子分类数量，用于删除前校验。
     *
     * @param parentId 父分类 ID
     * @return 子分类数量
     */
    @Select("SELECT COUNT(*) FROM category WHERE parent_id = #{parentId}")
    long countChildren(Long parentId);
}
