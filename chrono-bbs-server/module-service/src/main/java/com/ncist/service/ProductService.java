package com.ncist.service;

import com.ncist.common.result.PageResult;
import com.ncist.dto.ProductCreateDTO;
import com.ncist.vo.ProductDetailVO;
import com.ncist.vo.ProductVO;

/**
 * 商品服务。
 *
 * <p>面向游客提供上架商品的分页浏览与详情查询，面向商家提供商品的创建、
 * 修改、上下架与删除。商家操作要求其入驻审核已通过，且只能操作归属自己的商品。</p>
 *
 * <p>商品图片以 JSON 数组形式存储；创建、修改与删除涉及商品与 SKU 的多表写操作，
 * 由实现类以事务保证一致性。商品被购物车或订单引用时禁止删除。</p>
 */
public interface ProductService {

    /**
     * 分页查询上架商品（游客可访问）。
     *
     * @param categoryId 分类 ID 过滤，可为空
     * @param page       页码，从 1 开始；为空或小于 1 时按 1 处理
     * @param size       每页条数；为空或小于 1 时按 10 处理，最大 100
     * @return 商品分页结果，包含主图与 SKU 最低价
     */
    PageResult<ProductVO> listProducts(Long categoryId, Integer page, Integer size);

    /**
     * 查询商品详情，包含 SKU 列表、分类名与店铺名。
     *
     * <p>游客仅能查看上架商品，下架或不存在的商品统一视为不存在。</p>
     *
     * @param id 商品 ID
     * @return 商品详情
     * @throws BusinessException 商品不存在或已下架时抛出
     */
    ProductDetailVO getProductDetail(Long id);

    /**
     * 商家创建商品，初始状态为下架。
     *
     * @param dto    商品信息，包含名称、描述、图片、品牌、分类与 SKU 列表
     * @param userId 商家用户 ID，必须为审核通过的商家
     * @return 新增商品 ID
     * @throws BusinessException 用户非商家/审核未通过、分类不存在或 SKU 参数非法时抛出
     */
    Long createProduct(ProductCreateDTO dto, Long userId);

    /**
     * 商家修改商品。
     *
     * <p>基本信息按非空字段更新；提供的 SKU 列表非空时全量替换原有 SKU。</p>
     *
     * @param id     商品 ID，必须属于当前商家
     * @param dto    商品信息，字段为空表示不修改
     * @param userId 商家用户 ID，必须为审核通过的商家
     * @throws BusinessException 用户非商家/审核未通过、商品不存在/不属于该商家、
     *                           分类不存在或 SKU 参数非法时抛出
     */
    void updateProduct(Long id, ProductCreateDTO dto, Long userId);

    /**
     * 商家上架或下架商品。
     *
     * @param id     商品 ID，必须属于当前商家
     * @param status 目标状态：0-下架，1-上架
     * @param userId 商家用户 ID，必须为审核通过的商家
     * @throws BusinessException 状态非法、用户非商家/审核未通过或商品不属于该商家时抛出
     */
    void updateStatus(Long id, Integer status, Long userId);

    /**
     * 商家删除商品及其 SKU。
     *
     * <p>商品被购物车或订单引用时会因外键约束拒绝删除，建议改为下架。</p>
     *
     * @param id     商品 ID，必须属于当前商家
     * @param userId 商家用户 ID，必须为审核通过的商家
     * @throws BusinessException 用户非商家/审核未通过、商品不属于该商家
     *                           或商品存在交易引用时抛出
     */
    void deleteProduct(Long id, Long userId);
}
