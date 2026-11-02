package com.ncist.mapper;

import com.ncist.entity.CartItem;
import com.ncist.entity.CartItemDetail;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 购物车数据访问
 */
@Mapper
public interface CartItemMapper {

    // 联查用户购物车列表（含商品/SKU/商户信息）
    @Select("SELECT ci.id, ci.user_id AS userId, ci.product_id AS productId, ci.sku_id AS skuId, "
            + "ci.quantity, ci.selected, ci.created_at AS createdAt, "
            + "p.name AS productName, p.images AS productImages, p.status AS productStatus, "
            + "s.spec_text AS specText, s.price, s.stock, "
            + "m.id AS merchantId, m.shop_name AS merchantName "
            + "FROM cart_item ci "
            + "JOIN product p ON ci.product_id = p.id "
            + "JOIN product_sku s ON ci.sku_id = s.id "
            + "JOIN merchant m ON p.merchant_id = m.id "
            + "WHERE ci.user_id = #{userId} ORDER BY ci.id DESC")
    List<CartItemDetail> selectDetailByUserId(Long userId);

    // 查询用户某 SKU 的购物车项（同 SKU 累加用）
    @Select("SELECT id, user_id AS userId, product_id AS productId, sku_id AS skuId, "
            + "quantity, selected, created_at AS createdAt FROM cart_item "
            + "WHERE user_id = #{userId} AND sku_id = #{skuId}")
    CartItem selectByUserAndSku(@Param("userId") Long userId, @Param("skuId") Long skuId);

    // 查询单个购物车项（属主校验用）
    @Select("SELECT id, user_id AS userId, product_id AS productId, sku_id AS skuId, "
            + "quantity, selected, created_at AS createdAt FROM cart_item WHERE id = #{id}")
    CartItem selectById(Long id);

    // 查询单个购物车项（含 SKU/商品状态，校验库存与上架状态）
    @Select("SELECT ci.id, ci.user_id AS userId, ci.product_id AS productId, ci.sku_id AS skuId, "
            + "ci.quantity, ci.selected, ci.created_at AS createdAt, "
            + "p.status AS productStatus, s.price, s.stock "
            + "FROM cart_item ci "
            + "JOIN product p ON ci.product_id = p.id "
            + "JOIN product_sku s ON ci.sku_id = s.id "
            + "WHERE ci.id = #{id}")
    CartItemDetail selectDetailById(Long id);

    @Insert("INSERT INTO cart_item (user_id, product_id, sku_id, quantity, selected) "
            + "VALUES (#{userId}, #{productId}, #{skuId}, #{quantity}, 1)")
    @org.apache.ibatis.annotations.Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(CartItem cartItem);

    @Update("UPDATE cart_item SET quantity = #{quantity} WHERE id = #{id}")
    int updateQuantity(@Param("id") Long id, @Param("quantity") Integer quantity);

    @Update("UPDATE cart_item SET selected = #{selected} WHERE id = #{id}")
    int updateSelected(@Param("id") Long id, @Param("selected") Integer selected);

    @Update("UPDATE cart_item SET selected = #{selected} WHERE user_id = #{userId}")
    int updateSelectedByUser(@Param("userId") Long userId, @Param("selected") Integer selected);

    // 批量选中/取消选中（限定属主，防止越权改他人购物车）
    @Update("<script> UPDATE cart_item SET selected = #{selected} WHERE user_id = #{userId} AND id IN "
            + "<foreach collection='ids' item='id' open='(' separator=',' close=')'> #{id} </foreach> "
            + "</script>")
    int updateSelectedByIds(@Param("userId") Long userId, @Param("ids") List<Long> ids,
                            @Param("selected") Integer selected);

    @Delete("DELETE FROM cart_item WHERE id = #{id} AND user_id = #{userId}")
    int deleteById(@Param("userId") Long userId, @Param("id") Long id);

    @Delete("DELETE FROM cart_item WHERE user_id = #{userId}")
    int deleteByUserId(Long userId);
}
