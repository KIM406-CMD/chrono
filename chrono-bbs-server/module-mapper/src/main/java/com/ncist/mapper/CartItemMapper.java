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
 * 购物车数据访问。
 *
 * <p>单条更新/删除多在不带属主条件的方法中完成，调用方须先校验归属；
 * 批量操作则直接在 SQL 中以 {@code userId} 限定属主，天然防止越权。</p>
 */
@Mapper
public interface CartItemMapper {

    /**
     * 联查用户的购物车列表，附带商品、SKU 与商家信息。
     *
     * @param userId 用户 ID，通常为当前登录用户
     * @return 购物车明细列表，按加入时间倒序；无记录时返回空列表
     */
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

    /**
     * 查询用户某一 SKU 对应的购物车项，用于重复加购时合并数量。
     *
     * @param userId 用户 ID
     * @param skuId SKU ID
     * @return 购物车项，不存在时返回 null
     */
    @Select("SELECT id, user_id AS userId, product_id AS productId, sku_id AS skuId, "
            + "quantity, selected, created_at AS createdAt FROM cart_item "
            + "WHERE user_id = #{userId} AND sku_id = #{skuId}")
    CartItem selectByUserAndSku(@Param("userId") Long userId, @Param("skuId") Long skuId);

    /**
     * 按主键查询购物车项，用于属主校验。
     *
     * @param id 购物车项 ID
     * @return 购物车项，不存在时返回 null
     */
    @Select("SELECT id, user_id AS userId, product_id AS productId, sku_id AS skuId, "
            + "quantity, selected, created_at AS createdAt FROM cart_item WHERE id = #{id}")
    CartItem selectById(Long id);

    /**
     * 按主键查询购物车项并附带 SKU 价格、库存与商品上架状态。
     *
     * <p>用于修改数量或结算前校验库存与上下架状态。</p>
     *
     * @param id 购物车项 ID
     * @return 购物车明细，不存在时返回 null
     */
    @Select("SELECT ci.id, ci.user_id AS userId, ci.product_id AS productId, ci.sku_id AS skuId, "
            + "ci.quantity, ci.selected, ci.created_at AS createdAt, "
            + "p.status AS productStatus, s.price, s.stock "
            + "FROM cart_item ci "
            + "JOIN product p ON ci.product_id = p.id "
            + "JOIN product_sku s ON ci.sku_id = s.id "
            + "WHERE ci.id = #{id}")
    CartItemDetail selectDetailById(Long id);

    /**
     * 按 ID 批量查询购物车项，用于下单结算。
     *
     * <p>SQL 同时以 {@code userId} 限定属主，只有属于该用户的记录会被返回。</p>
     *
     * @param userId 用户 ID
     * @param ids 购物车项 ID 列表，不能为空
     * @return 购物车明细列表
     */
    @Select("<script> SELECT ci.id, ci.user_id AS userId, ci.product_id AS productId, ci.sku_id AS skuId, "
            + "ci.quantity, ci.selected, ci.created_at AS createdAt, "
            + "p.status AS productStatus, s.price, s.stock "
            + "FROM cart_item ci "
            + "JOIN product p ON ci.product_id = p.id "
            + "JOIN product_sku s ON ci.sku_id = s.id "
            + "WHERE ci.user_id = #{userId} AND ci.id IN "
            + "<foreach collection='ids' item='id' open='(' separator=',' close=')'> #{id} </foreach> </script>")
    List<CartItemDetail> selectDetailByIds(@Param("userId") Long userId, @Param("ids") List<Long> ids);

    /**
     * 新增购物车项，默认选中，并回填自增主键。
     *
     * @param cartItem 购物车项，userId/productId/skuId/quantity 必填
     * @return 受影响行数
     */
    @Insert("INSERT INTO cart_item (user_id, product_id, sku_id, quantity, selected) "
            + "VALUES (#{userId}, #{productId}, #{skuId}, #{quantity}, 1)")
    @org.apache.ibatis.annotations.Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(CartItem cartItem);

    /**
     * 更新购物车项数量。
     *
     * <p>不校验属主，调用方须先确认该项属于当前用户。</p>
     *
     * @param id 购物车项 ID
     * @param quantity 新数量，必须大于 0
     * @return 受影响行数，0 表示记录不存在
     */
    @Update("UPDATE cart_item SET quantity = #{quantity} WHERE id = #{id}")
    int updateQuantity(@Param("id") Long id, @Param("quantity") Integer quantity);

    /**
     * 更新单个购物车项的选中状态。
     *
     * <p>不校验属主，调用方须先确认该项属于当前用户。</p>
     *
     * @param id 购物车项 ID
     * @param selected 选中状态：0-未选中，1-已选中
     * @return 受影响行数，0 表示记录不存在
     */
    @Update("UPDATE cart_item SET selected = #{selected} WHERE id = #{id}")
    int updateSelected(@Param("id") Long id, @Param("selected") Integer selected);

    /**
     * 更新用户全部购物车项的选中状态（全选/取消全选）。
     *
     * @param userId 用户 ID，限定属主
     * @param selected 选中状态：0-未选中，1-已选中
     * @return 受影响行数
     */
    @Update("UPDATE cart_item SET selected = #{selected} WHERE user_id = #{userId}")
    int updateSelectedByUser(@Param("userId") Long userId, @Param("selected") Integer selected);

    /**
     * 批量更新指定购物车项的选中状态。
     *
     * <p>SQL 以 {@code userId} 限定属主，防止越权修改他人购物车。</p>
     *
     * @param userId 用户 ID
     * @param ids 购物车项 ID 列表，不能为空
     * @param selected 选中状态：0-未选中，1-已选中
     * @return 受影响行数
     */
    @Update("<script> UPDATE cart_item SET selected = #{selected} WHERE user_id = #{userId} AND id IN "
            + "<foreach collection='ids' item='id' open='(' separator=',' close=')'> #{id} </foreach> "
            + "</script>")
    int updateSelectedByIds(@Param("userId") Long userId, @Param("ids") List<Long> ids,
                            @Param("selected") Integer selected);

    /**
     * 删除单个购物车项，限定属主。
     *
     * @param userId 用户 ID
     * @param id 购物车项 ID
     * @return 受影响行数，0 表示记录不存在或不属于该用户
     */
    @Delete("DELETE FROM cart_item WHERE id = #{id} AND user_id = #{userId}")
    int deleteById(@Param("userId") Long userId, @Param("id") Long id);

    /**
     * 批量删除购物车项，用于下单成功后清理已购项。
     *
     * @param userId 用户 ID，限定属主
     * @param ids 购物车项 ID 列表，不能为空
     * @return 受影响行数
     */
    @Delete("<script> DELETE FROM cart_item WHERE user_id = #{userId} AND id IN "
            + "<foreach collection='ids' item='id' open='(' separator=',' close=')'> #{id} </foreach> </script>")
    int deleteByIds(@Param("userId") Long userId, @Param("ids") List<Long> ids);

    /**
     * 清空用户的购物车。
     *
     * @param userId 用户 ID
     * @return 受影响行数
     */
    @Delete("DELETE FROM cart_item WHERE user_id = #{userId}")
    int deleteByUserId(Long userId);
}
