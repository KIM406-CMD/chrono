package com.ncist.service;

import com.ncist.common.result.PageResult;
import com.ncist.dto.CategoryUpsertDTO;
import com.ncist.dto.MerchantReviewDTO;
import com.ncist.vo.AdminMerchantVO;
import com.ncist.vo.AdminReportVO;
import com.ncist.vo.AdminUserVO;

import java.util.Map;

/**
 * 管理后台服务。
 *
 * <p>提供用户、商家、分类、商品、订单与销售报表等全平台管理能力，
 * 所有方法都要求调用者具备管理员角色，具体校验由实现类通过
 * {@code UserContext} 完成。</p>
 *
 * <p>商家审核与分类维护等写操作由实现类以事务保证一致性；
 * 列表查询统一返回分页结果，页码不合法时回退为第 1 页，每页条数默认 10。</p>
 */
public interface AdminService {

    /**
     * 分页查询用户列表。
     *
     * @param keyword 用户名/昵称模糊查询关键字，可为空
     * @param role    角色过滤：1-普通用户，2-商家，3-管理员，为空时不过滤
     * @param status  状态过滤：0-禁用，1-启用，为空时不过滤
     * @param page    页码，从 1 开始；为空或小于 1 时按 1 处理
     * @param size    每页条数；为空或小于 1 时按 10 处理
     * @return 用户分页结果
     */
    PageResult<AdminUserVO> listUsers(String keyword, Integer role, Integer status, Integer page, Integer size);

    /**
     * 启用或禁用用户。
     *
     * @param id     用户 ID
     * @param status 目标状态：0-禁用，1-启用
     * @throws BusinessException 状态值非法、用户不存在或目标为管理员时抛出
     */
    void updateUserStatus(Long id, Integer status);

    /**
     * 分页查询商家入驻申请。
     *
     * @param status 审核状态过滤：0-待审核，1-通过，2-拒绝，为空时不过滤
     * @param page   页码，从 1 开始；为空或小于 1 时按 1 处理
     * @param size   每页条数；为空或小于 1 时按 10 处理
     * @return 商家申请分页结果
     */
    PageResult<AdminMerchantVO> listMerchants(Integer status, Integer page, Integer size);

    /**
     * 审核商家入驻申请。
     *
     * <p>审核通过时同步将申请人角色升级为商家；已审核过的申请不可重复审核。</p>
     *
     * @param id  商家申请 ID
     * @param dto 审核参数，包含是否通过及审核原因
     * @throws BusinessException 申请不存在或已被审核时抛出
     */
    void reviewMerchant(Long id, MerchantReviewDTO dto);

    /**
     * 新增商品分类。
     *
     * <p>仅允许 1~3 级分类；一级分类的父 ID 强制为 0，
     * 非一级分类要求父分类存在且层级恰好比当前分类小 1。</p>
     *
     * @param dto 分类信息，包含名称、父分类、层级、排序与图标
     * @return 新增分类的 ID
     * @throws BusinessException 分类名称/层级非法或父分类不合法时抛出
     */
    Long createCategory(CategoryUpsertDTO dto);

    /**
     * 修改分类的展示信息（仅名称、排序与图标，不支持变更层级与父分类）。
     *
     * @param id  分类 ID
     * @param dto 待更新字段；三个字段全为空时视为非法请求
     * @throws BusinessException 分类不存在或没有可更新字段时抛出
     */
    void updateCategory(Long id, CategoryUpsertDTO dto);

    /**
     * 删除商品分类。
     *
     * @param id 分类 ID
     * @throws BusinessException 分类不存在、存在子分类或被商品引用时抛出
     */
    void deleteCategory(Long id);

    /**
     * 分页查询全平台商品。
     *
     * @param keyword    商品名称模糊查询关键字，可为空
     * @param categoryId 分类 ID 过滤，可为空
     * @param merchantId 商家 ID 过滤，可为空
     * @param status     商品状态过滤：0-下架，1-上架，可为空
     * @param page       页码，从 1 开始；为空或小于 1 时按 1 处理
     * @param size       每页条数；为空或小于 1 时按 10 处理
     * @return 商品分页结果，每项以 Map 形式返回商品字段
     */
    PageResult<Map<String, Object>> listProducts(String keyword, Long categoryId, Long merchantId,
                                                  Integer status, Integer page, Integer size);

    /**
     * 管理员修改任意商品。
     *
     * <p>可更新商品基础信息；SKU 列表非空时全量替换原有 SKU。
     * 仅管理员可操作，不校验商品归属商家。</p>
     *
     * @param id  商品 ID
     * @param dto 商品更新参数，字段为 null 表示不修改；SKU 列表非空时全量替换
     * @throws com.ncist.common.exception.BusinessException 商品/分类不存在或 SKU 参数非法时抛出
     */
    void updateProduct(Long id, com.ncist.dto.ProductCreateDTO dto);

    /**
     * 管理员上架或下架任意商品。
     *
     * <p>不校验商品归属商家，操作后同步 Elasticsearch 索引（上架写入、下架移除）。</p>
     *
     * @param id     商品 ID
     * @param status 商品状态：0-下架，1-上架
     * @throws com.ncist.common.exception.BusinessException 商品不存在或状态非法时抛出
     */
    void updateProductStatus(Long id, Integer status);

    /**
     * 管理员删除任意商品。
     *
     * <p>商品与 SKU 在同一事务中删除；商品已被购物车或订单引用时拒绝删除。
     * 删除成功后移除 Elasticsearch 索引。</p>
     *
     * @param id 商品 ID
     * @throws com.ncist.common.exception.BusinessException 商品不存在或存在交易引用时抛出
     */
    void deleteProduct(Long id);

    /**
     * 分页查询全平台订单。
     *
     * @param orderNo 订单号模糊查询关键字，可为空
     * @param status  订单状态过滤，为空时不过滤
     * @param page    页码，从 1 开始；为空或小于 1 时按 1 处理
     * @param size    每页条数；为空或小于 1 时按 10 处理
     * @return 订单分页结果，每项以 Map 形式返回订单字段
     */
    PageResult<Map<String, Object>> listOrders(String orderNo, Integer status, Integer page, Integer size);

    /**
     * 查询全局销售报表。
     *
     * <p>统计总销售额、订单数、客单价、热销商品、分类/商家销售分布、
     * 销售趋势与订单状态分布。</p>
     *
     * @param startDate 统计开始日期，格式 yyyy-MM-dd，可为空表示不限
     * @param endDate   统计结束日期，格式 yyyy-MM-dd，可为空表示不限
     * @param groupBy   趋势分组粒度：day/week/month，为空时默认 day
     * @return 全局销售报表数据
     */
    AdminReportVO getReport(String startDate, String endDate, String groupBy);
}
