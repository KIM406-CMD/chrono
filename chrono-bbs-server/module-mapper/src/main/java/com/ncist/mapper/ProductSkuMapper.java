
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
 * 商品 SKU 数据访问
 */
@Mapper
public interface ProductSkuMapper {

    // 按主键查询 SKU（加购/库存校验用）
    @Select("SELECT id, product_id AS productId, spec_ids AS specIds, spec_text AS specText, "
            + "price, stock, status, created_at AS createdAt FROM product_sku WHERE id = #{id}")
    ProductSku selectById(Long id);

    // 按商品查询启用的 SKU 列表
    @Select("SELECT id, product_id AS productId, spec_ids AS specIds, spec_text AS specText, "
            + "price, stock, status, created_at AS createdAt FROM product_sku "
            + "WHERE product_id = #{productId} AND status = 1 ORDER BY id")
    List<ProductSku> selectByProductId(Long productId);

    // 批量插入 SKU（建品/改品时）
    @Insert("<script> INSERT INTO product_sku (product_id, spec_ids, spec_text, price, stock, status) VALUES "
            + "<foreach collection='list' item='sku' separator=','> "
            + "(#{sku.productId}, #{sku.specIds}, #{sku.specText}, #{sku.price}, #{sku.stock}, 1) "
            + "</foreach> </script>")
    int insertBatch(@Param("list") List<ProductSku> skus);

    // 删除商品的全部 SKU（改品全量替换/删品时）
    @Delete("DELETE FROM product_sku WHERE product_id = #{productId}")
    int deleteByProductId(Long productId);

    // 批量查询商品最低价（列表页组装用）
    @Select("<script> SELECT product_id AS productId, MIN(price) AS minPrice FROM product_sku "
            + "WHERE status = 1 AND product_id IN "
            + "<foreach collection='productIds' item='pid' open='(' separator=',' close=')'> #{pid} </foreach> "
            + "GROUP BY product_id </script>")
    List<ProductMinPrice> selectMinPrices(@Param("productIds") List<Long> productIds);

    // 原子扣减库存（WHERE stock >= 数量，影响行数 0 即库存不足），下单锁库存用
    @Update("UPDATE product_sku SET stock = stock - #{quantity} "
            + "WHERE id = #{id} AND stock >= #{quantity}")
    int deductStock(@Param("id") Long id, @Param("quantity") Integer quantity);

    // 回补库存（取消订单用）
    @Update("UPDATE product_sku SET stock = stock + #{quantity} WHERE id = #{id}")
    int restoreStock(@Param("id") Long id, @Param("quantity") Integer quantity);
}
