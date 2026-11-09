package com.ncist.controller;

import com.ncist.common.annotation.Public;
import com.ncist.common.context.UserContext;
import com.ncist.common.result.PageResult;
import com.ncist.common.result.Result;
import com.ncist.dto.ProductCreateDTO;
import com.ncist.dto.ProductStatusDTO;
import com.ncist.service.ProductService;
import com.ncist.vo.ProductDetailVO;
import com.ncist.vo.ProductVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商品控制器。
 *
 * <p>提供商城前台商品浏览与商家端商品维护能力。商品列表与详情为公开接口；
 * 创建、修改、上下架与删除要求登录且为商品所属商家。接口前缀 {@code /api/product}。</p>
 */
@Tag(name = "商品管理", description = "商品的增删改查、上下架、浏览等接口")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/product")
public class ProductController {

    private final ProductService productService;

    /**
     * 分页查询在售商品列表。
     *
     * <p>公开接口，仅返回上架商品，供商城首页与分类页使用。</p>
     *
     * @param categoryId 分类 ID，可为 {@code null} 表示查询全部
     * @param page       页码，从 1 开始，默认 1
     * @param size       每页条数，默认 10
     * @return 商品分页结果
     */
    @Operation(summary = "分页查询商品列表")
    @Parameter(name = "categoryId", description = "分类 ID，不传则查询全部", example = "1")
    @Public
    @GetMapping("/list")
    public Result<PageResult<ProductVO>> listProducts(@RequestParam(required = false) Long categoryId,
                                                       @RequestParam(required = false, defaultValue = "1") Integer page,
                                                       @RequestParam(required = false, defaultValue = "10") Integer size) {
        return Result.success(productService.listProducts(categoryId, page, size));
    }

    /**
     * 查询商品详情。
     *
     * <p>公开接口，返回商品基本信息、SKU 规格与库存等。</p>
     *
     * @param id 商品 ID
     * @return 商品详情
     */
    @Operation(summary = "查询商品详情")
    @Parameter(name = "id", description = "商品 ID", example = "1")
    @Public
    @GetMapping("/{id}")
    public Result<ProductDetailVO> getProductDetail(@PathVariable Long id) {
        return Result.success(productService.getProductDetail(id));
    }

    /**
     * 创建商品。
     *
     * <p>要求登录且为审核通过的商家，商品归属当前登录商家，创建后默认为下架状态。</p>
     *
     * @param dto 商品参数，包含名称、描述、图片、品牌、分类与至少一个 SKU 规格
     * @return 新增商品 ID
     */
    @Operation(summary = "创建商品（商家）")
    @PostMapping
    public Result<Long> createProduct(@Valid @RequestBody ProductCreateDTO dto) {
        return Result.success(productService.createProduct(dto, UserContext.getUserId()));
    }

    /**
     * 修改商品。
     *
     * <p>要求登录且为商品所属商家，不允许修改他人商品；传入 SKU 列表时会覆盖原有规格。</p>
     *
     * @param id  商品 ID
     * @param dto 商品参数，结构同创建
     * @return 无业务数据的成功响应
     */
    @Operation(summary = "修改商品（商家）")
    @Parameter(name = "id", description = "商品 ID", example = "1")
    @PutMapping("/{id}")
    public Result<Void> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductCreateDTO dto) {
        productService.updateProduct(id, dto, UserContext.getUserId());
        return Result.success();
    }

    /**
     * 上架或下架商品。
     *
     * @param id  商品 ID，必须属于当前登录商家
     * @param dto 状态参数，{@code status} 取值 0-下架 1-上架
     * @return 无业务数据的成功响应
     */
    @Operation(summary = "上架/下架商品（商家）")
    @Parameter(name = "id", description = "商品 ID", example = "1")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody ProductStatusDTO dto) {
        productService.updateStatus(id, dto.getStatus(), UserContext.getUserId());
        return Result.success();
    }

    /**
     * 删除商品。
     *
     * <p>要求登录且为商品所属商家；商品已被购物车或订单引用时拒绝删除。</p>
     *
     * @param id 商品 ID
     * @return 无业务数据的成功响应
     */
    @Operation(summary = "删除商品（商家）")
    @Parameter(name = "id", description = "商品 ID", example = "1")
    @DeleteMapping("/{id}")
    public Result<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id, UserContext.getUserId());
        return Result.success();
    }
}
