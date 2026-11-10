package com.ncist.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 搜索历史数据访问
 */
@Mapper
public interface SearchHistoryMapper {

    // 新增一条搜索记录
    @Insert("INSERT INTO search_history (user_id, keyword, searched_at) VALUES (#{userId}, #{keyword}, NOW())")
    int insert(@Param("userId") Long userId, @Param("keyword") String keyword);

    // 删除该用户的同关键词旧记录（保证历史列表去重且按最近搜索排序）
    @Delete("DELETE FROM search_history WHERE user_id = #{userId} AND keyword = #{keyword}")
    int deleteByUserAndKeyword(@Param("userId") Long userId, @Param("keyword") String keyword);

    // 最近搜索记录（去重后取最近 N 条）
    @Select("SELECT keyword FROM search_history WHERE user_id = #{userId} "
            + "ORDER BY searched_at DESC, id DESC LIMIT #{limit}")
    List<String> selectRecent(@Param("userId") Long userId, @Param("limit") int limit);

    // 清空该用户的搜索历史
    @Delete("DELETE FROM search_history WHERE user_id = #{userId}")
    int deleteByUserId(Long userId);

    // 近 N 天热搜词（按搜索次数倒序）
    @Select("SELECT keyword FROM search_history WHERE searched_at >= DATE_SUB(NOW(), INTERVAL #{days} DAY) "
            + "GROUP BY keyword ORDER BY COUNT(*) DESC, MAX(searched_at) DESC LIMIT #{limit}")
    List<String> selectHot(@Param("days") int days, @Param("limit") int limit);
}
