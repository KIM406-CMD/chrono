package com.ncist.service;

import com.ncist.common.result.PageResult;
import com.ncist.dto.CategoryUpsertDTO;
import com.ncist.dto.MerchantReviewDTO;
import com.ncist.vo.AdminMerchantVO;
import com.ncist.vo.AdminReportVO;
import com.ncist.vo.AdminUserVO;

import java.util.Map;

public interface AdminService {

    /** 用户分页列表 */
    PageResult<AdminUserVO> listUsers(String keyword, Integer role, Integer status, Integer page, Integer size);

    /** 启用/禁用用户 */
    void updateUserStatus(Long id, Integer status);

    /** 商家申请分页列表 */
    PageResult<AdminMerchantVO> listMerchants(Integer status, Integer page, Integer size);

    /** 审核商家（通过时同步升级用户角色为商家） */
    void reviewMerchant(Long id, MerchantReviewDTO dto);

    /** 新增分类 */
    Long createCategory(CategoryUpsertDTO dto);

    /** 修改分类（仅名称/排序/图标） */
    void updateCategory(Long id, CategoryUpsertDTO dto);

    /** 删除分类（有子分类或商品引用时拒绝） */
    void deleteCategory(Long id);

    /** 全平台商品分页 */
    PageResult<Map<String, Object>> listProducts(String keyword, Long categoryId, Long merchantId,
                                                  Integer status, Integer page, Integer size);

    /** 全平台订单分页 */
    PageResult<Map<String, Object>> listOrders(String orderNo, Integer status, Integer page, Integer size);

    /** 全局销售报表 */
    AdminReportVO getReport(String startDate, String endDate, String groupBy);
}
