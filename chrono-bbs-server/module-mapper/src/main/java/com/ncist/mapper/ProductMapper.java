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
 * 商品数据访问。
 *
 * <p>面向游客/买家的查询只返回上架商品；商家编辑与管理后台查询则包含下架商品。
 * 价格与库存不在本表，需通过 {@link ProductSkuMapper} 获取。</p>
 */
@Mapper
public interface ProductMapper {

    /**
     * 分页查询上架商品，供游客与买家浏览。
     *
     * @param categoryId 分类 ID；为 null 时查全部分类
     * @param offset 偏移量，从 0 开始
     * @param size 每页条数
     * @return 上架商品列表，按销量、ID 倒序
     */
    @Select("<script> SELECT id, name, description, images, brand, "
            + "category_id AS categoryId, merchant_id AS merchantId, status, sales, "
            + "created_at AS createdAt, updated_at AS updatedAt FROM product "
            + "WHERE status = 1 "
            + "<if test='categoryId != null'> AND category_id = #{categoryId} </if> "
            + "ORDER BY sales DESC, id DESC LIMIT #{offset}, #{size} </script>")
    List<Product> selectListPage(@Param("categoryId") Long categoryId, @Param("offset") int offset, @Param("size") int size);

    /**
     * 统计指定分类下的上架商品数量，用于分页。
     *
     * @param categoryId 分类 ID；为 null 时统计全部上架商品
     * @return 上架商品总数
     */
    @Select("<script> SELECT COUNT(*) FROM product WHERE status = 1 "
            + "<if test='categoryId != null'> AND category_id = #{categoryId} </if> </script>")
    long countByCategory(@Param("categoryId") Long categoryId);

    /**
     * 按主键查询商品，包含下架商品，供商家编辑与归属校验使用。
     *
     * @param id 商品 ID
     * @return 商品记录，不存在时返回 null
     */
    @Select("SELECT id, name, description, images, brand, "
            + "category_id AS categoryId, merchant_id AS merchantId, status, sales, "
            + "created_at AS createdAt, updated_at AS updatedAt FROM product WHERE id = #{id}")
    Product selectById(Long id);

    /**
     * 新增商品，并回填自增主键。
     *
     * @param product 商品信息；status 通常由业务层初始化为 0-下架，merchantId 必填
     * @return 受影响行数
     */
    @Insert("INSERT INTO product (name, description, images, brand, category_id, merchant_id, status) "
            + "VALUES (#{name}, #{description}, #{images}, #{brand}, #{categoryId}, #{merchantId}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Product product);

    /**
     * 动态更新商品基本信息，仅更新非 null 字段。
     *
     * <p>不允许通过此方法修改上架状态与销量。</p>
     *
     * @param product 商品信息，id 必填，其余字段为 null 表示不修改
     * @return 受影响行数，0 表示商品不存在
     */
    @Update("<script> UPDATE product <set> "
            + "<if test='name != null'> name = #{name}, </if> "
            + "<if test='description != null'> description = #{description}, </if> "
            + "<if test='images != null'> images = #{images}, </if> "
            + "<if test='brand != null'> brand = #{brand}, </if> "
            + "<if test='categoryId != null'> category_id = #{categoryId}, </if> "
            + "</set> WHERE id = #{id} </script>")
    int update(Product product);

    /**
     * 更新商品上下架状态。
     *
     * @param id 商品 ID
     * @param status 目标状态：0-下架，1-上架
     * @return 受影响行数，0 表示商品不存在
     */
    @Update("UPDATE product SET status = #{status}, updated_at = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 按主键删除商品（物理删除）。
     *
     * <p>调用方须同步清理 SKU 并确认无未完成订单引用。</p>
     *
     * @param id 商品 ID
     * @return 受影响行数，0 表示商品不存在
     */
    @Delete("DELETE FROM product WHERE id = #{id}")
    int deleteById(Long id);

    /**
     * 管理后台分页查询全平台商品。
     *
     * @param keyword 关键字，模糊匹配商品名称或品牌；为空时不过滤
     * @param categoryId 分类 ID，为 null 时不过滤
     * @param merchantId 商家 ID，为 null 时不过滤
     * @param status 上下架状态：0-下架，1-上架；为 null 时查全部
     * @param offset 偏移量，从 0 开始
     * @param size 每页条数
     * @return 商品列表，按 ID 倒序
     */
    @Select("""
            <script>
            SELECT id, name, description, images, brand,
                   category_id AS categoryId, merchant_id AS merchantId, status, sales,
                   created_at AS createdAt, updated_at AS updatedAt
            FROM product WHERE 1=1
            <if test="keyword != null and keyword != ''">
                AND (name LIKE CONCAT('%', #{keyword}, '%') OR brand LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            <if test="categoryId != null"> AND category_id = #{categoryId} </if>
            <if test="merchantId != null"> AND merchant_id = #{merchantId} </if>
            <if test="status != null"> AND status = #{status} </if>
            ORDER BY id DESC LIMIT #{offset}, #{size}
            </script>
            """)
    List<Product> selectAdminPage(@Param("keyword") String keyword, @Param("categoryId") Long categoryId,
                                  @Param("merchantId") Long merchantId, @Param("status") Integer status,
                                  @Param("offset") int offset, @Param("size") int size);

    /**
     * 统计管理后台商品数量，用于分页。
     *
     * @param keyword 关键字，模糊匹配商品名称或品牌；为空时不过滤
     * @param categoryId 分类 ID，为 null 时不过滤
     * @param merchantId 商家 ID，为 null 时不过滤
     * @param status 上下架状态，为 null 时不过滤
     * @return 商品总数
     */
    @Select("""
            <script>
            SELECT COUNT(*) FROM product WHERE 1=1
            <if test="keyword != null and keyword != ''">
                AND (name LIKE CONCAT('%', #{keyword}, '%') OR brand LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            <if test="categoryId != null"> AND category_id = #{categoryId} </if>
            <if test="merchantId != null"> AND merchant_id = #{merchantId} </if>
            <if test="status != null"> AND status = #{status} </if>
            </script>
            """)
    long countAdmin(@Param("keyword") String keyword, @Param("categoryId") Long categoryId,
                    @Param("merchantId") Long merchantId, @Param("status") Integer status);

    /**
     * 统计分类下的商品数量，用于删除分类前的引用校验。
     *
     * @param categoryId 分类 ID
     * @return 商品数量
     */
    @Select("SELECT COUNT(*) FROM product WHERE category_id = #{categoryId}")
    long countByCategoryId(Long categoryId);
}
