
package com.ncist.mapper;

import com.ncist.entity.ProductMinPrice;
import com.ncist.entity.ProductSku;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 商品 SKU 数据访问。
 *
 * <p>SKU 是价格与库存的最小粒度。库存扣减采用 {@code WHERE stock >= quantity}
 * 的条件更新，以受影响行数判断是否扣减成功，避免超卖。</p>
 */
@Mapper
public interface ProductSkuMapper {

    /**
     * 按主键查询 SKU，包含禁用的 SKU，供加购与库存校验使用。
     *
     * @param id SKU ID
     * @return SKU 记录，不存在时返回 null
     */
    @Select("SELECT id, product_id AS productId, spec_ids AS specIds, spec_text AS specText, "
            + "price, stock, status, created_at AS createdAt FROM product_sku WHERE id = #{id}")
    ProductSku selectById(Long id);

    /**
     * 查询商品下所有启用的 SKU。
     *
     * @param productId 商品 ID
     * @return 启用的 SKU 列表，按 ID 升序；无记录时返回空列表
     */
    @Select("SELECT id, product_id AS productId, spec_ids AS specIds, spec_text AS specText, "
            + "price, stock, status, created_at AS createdAt FROM product_sku "
            + "WHERE product_id = #{productId} AND status = 1 ORDER BY id")
    List<ProductSku> selectByProductId(Long productId);

    /**
     * 批量插入 SKU，新建时状态统一为启用（1）。
     *
     * @param skus SKU 列表，不能为空；各元素的 productId 必须已存在
     * @return 受影响行数
     */
    @Insert("<script> INSERT INTO product_sku (product_id, spec_ids, spec_text, price, stock, status) VALUES "
            + "<foreach collection='list' item='sku' separator=','> "
            + "(#{sku.productId}, #{sku.specIds}, #{sku.specText}, #{sku.price}, #{sku.stock}, 1) "
            + "</foreach> </script>")
    int insertBatch(@Param("list") List<ProductSku> skus);

    /**
     * 删除商品的全部 SKU，用于改品时全量替换或删除商品。
     *
     * @param productId 商品 ID
     * @return 受影响行数
     */
    @Delete("DELETE FROM product_sku WHERE product_id = #{productId}")
    int deleteByProductId(Long productId);

    /**
     * 批量查询多个商品启用 SKU 的最低价，用于列表页展示起售价。
     *
     * @param productIds 商品 ID 列表，不能为空
     * @return 商品 ID 与最低价（单位：分）的列表；无启用 SKU 的商品不会返回
     */
    @Select("<script> SELECT product_id AS productId, MIN(price) AS minPrice FROM product_sku "
            + "WHERE status = 1 AND product_id IN "
            + "<foreach collection='productIds' item='pid' open='(' separator=',' close=')'> #{pid} </foreach> "
            + "GROUP BY product_id </script>")
    List<ProductMinPrice> selectMinPrices(@Param("productIds") List<Long> productIds);

    /**
     * 原子扣减 SKU 库存，用于下单锁定库存。
     *
     * @param id SKU ID
     * @param quantity 扣减数量，必须大于 0
     * @return 受影响行数；0 表示 SKU 不存在或库存不足
     */
    @Update("UPDATE product_sku SET stock = stock - #{quantity} "
            + "WHERE id = #{id} AND stock >= #{quantity}")
    int deductStock(@Param("id") Long id, @Param("quantity") Integer quantity);

    /**
     * 回补 SKU 库存，用于取消订单或退款。
     *
     * @param id SKU ID
     * @param quantity 回补数量，必须大于 0
     * @return 受影响行数，0 表示 SKU 不存在
     */
    @Update("UPDATE product_sku SET stock = stock + #{quantity} WHERE id = #{id}")
    int restoreStock(@Param("id") Long id, @Param("quantity") Integer quantity);
}
