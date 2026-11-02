package com.ncist.mapper;

import com.ncist.entity.Product;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.UpdateProvider;

import java.util.List;

/**
 * 商品数据访问
 */
@Mapper
public interface ProductMapper {

    // 分页查询上架商品（游客浏览），可按分类过滤，销量倒序
    @Select("<script> SELECT id, name, description, images, brand, "
            + "category_id AS categoryId, merchant_id AS merchantId, status, sales, "
            + "created_at AS createdAt, updated_at AS updatedAt FROM product "
            + "WHERE status = 1 "
            + "<if test='categoryId != null'> AND category_id = #{categoryId} </if> "
            + "ORDER BY sales DESC, id DESC LIMIT #{offset}, #{size} </script>")
    List<Product> selectListPage(@Param("categoryId") Long categoryId, @Param("offset") int offset, @Param("size") int size);

    // 统计上架商品总数（分页）
    @Select("<script> SELECT COUNT(*) FROM product WHERE status = 1 "
            + "<if test='categoryId != null'> AND category_id = #{categoryId} </if> </script>")
    long countByCategory(@Param("categoryId") Long categoryId);

    // 按主键查询（含下架商品，用于商家编辑/归属校验）
    @Select("SELECT id, name, description, images, brand, "
            + "category_id AS categoryId, merchant_id AS merchantId, status, sales, "
            + "created_at AS createdAt, updated_at AS updatedAt FROM product WHERE id = #{id}")
    Product selectById(Long id);

    // 新增商品（默认下架状态），回填自增主键
    @Insert("INSERT INTO product (name, description, images, brand, category_id, merchant_id, status) "
            + "VALUES (#{name}, #{description}, #{images}, #{brand}, #{categoryId}, #{merchantId}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Product product);

    // 更新商品基本信息（动态 SET）
    @Update("<script> UPDATE product <set> "
            + "<if test='name != null'> name = #{name}, </if> "
            + "<if test='description != null'> description = #{description}, </if> "
            + "<if test='images != null'> images = #{images}, </if> "
            + "<if test='brand != null'> brand = #{brand}, </if> "
            + "<if test='categoryId != null'> category_id = #{categoryId}, </if> "
            + "</set> WHERE id = #{id} </script>")
    int update(Product product);

    // 更新商品上下架状态
    @Update("UPDATE product SET status = #{status}, updated_at = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    // 删除商品
    @Delete("DELETE FROM product WHERE id = #{id}")
    int deleteById(Long id);
}
