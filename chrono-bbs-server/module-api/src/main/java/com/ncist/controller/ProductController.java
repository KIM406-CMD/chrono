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

@Tag(name = "商品管理", description = "商品的增删改查、上下架、浏览等接口")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/product")
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "分页查询商品列表")
    @Parameter(name = "categoryId", description = "分类 ID，不传则查询全部", example = "1")
    @Public
    @GetMapping("/list")
    public Result<PageResult<ProductVO>> listProducts(@RequestParam(required = false) Long categoryId,
                                                       @RequestParam(required = false, defaultValue = "1") Integer page,
                                                       @RequestParam(required = false, defaultValue = "10") Integer size) {
        return Result.success(productService.listProducts(categoryId, page, size));
    }

    @Operation(summary = "查询商品详情")
    @Parameter(name = "id", description = "商品 ID", example = "1")
    @Public
    @GetMapping("/{id}")
    public Result<ProductDetailVO> getProductDetail(@PathVariable Long id) {
        return Result.success(productService.getProductDetail(id));
    }

    @Operation(summary = "创建商品（商家）")
    @PostMapping
    public Result<Long> createProduct(@Valid @RequestBody ProductCreateDTO dto) {
        return Result.success(productService.createProduct(dto, UserContext.getUserId()));
    }

    @Operation(summary = "修改商品（商家）")
    @Parameter(name = "id", description = "商品 ID", example = "1")
    @PutMapping("/{id}")
    public Result<Void> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductCreateDTO dto) {
        productService.updateProduct(id, dto, UserContext.getUserId());
        return Result.success();
    }

    @Operation(summary = "上架/下架商品（商家）")
    @Parameter(name = "id", description = "商品 ID", example = "1")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody ProductStatusDTO dto) {
        productService.updateStatus(id, dto.getStatus(), UserContext.getUserId());
        return Result.success();
    }

    @Operation(summary = "删除商品（商家）")
    @Parameter(name = "id", description = "商品 ID", example = "1")
    @DeleteMapping("/{id}")
    public Result<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id, UserContext.getUserId());
        return Result.success();
    }
}
