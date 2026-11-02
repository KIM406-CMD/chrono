package com.ncist.service;

import com.ncist.common.result.PageResult;
import com.ncist.dto.ProductCreateDTO;
import com.ncist.vo.ProductDetailVO;
import com.ncist.vo.ProductVO;

/**
 * 商品服务
 */
public interface ProductService {

    // 分页查询上架商品（游客可访问），可按分类过滤
    PageResult<ProductVO> listProducts(Long categoryId, Integer page, Integer size);

    // 查询商品详情（含 SKU 列表、分类名、店铺名；仅上架商品对游客可见）
    ProductDetailVO getProductDetail(Long id);

    // 商家创建商品（默认下架状态，需入驻审核通过）
    Long createProduct(ProductCreateDTO dto, Long userId);

    // 商家修改商品（基本信息 + SKU 全量替换）
    void updateProduct(Long id, ProductCreateDTO dto, Long userId);

    // 商家上架/下架商品（1-上架，0-下架）
    void updateStatus(Long id, Integer status, Long userId);

    // 商家删除商品（被购物车/订单引用时拒绝，建议下架）
    void deleteProduct(Long id, Long userId);
}
