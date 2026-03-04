package org.example.springboot.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.springboot.entity.Position;

import java.util.List;
import java.util.Map;

@Mapper
public interface PositionMapper extends BaseMapper<Position> {
    
    /**
     * 检查指定企业ID是否存在关联的职位信息
     * @param companyId 企业ID
     * @return 如果存在返回true，否则返回false
     */
    @Select("SELECT COUNT(*) > 0 FROM position WHERE company_id = #{companyId}")
    boolean existsByCompanyId(@Param("companyId") Long companyId);
    
    /**
     * 根据企业ID查询职位列表
     * @param companyId 企业ID
     * @return 职位列表
     */

    default List<Position> findByCompanyId(@Param("companyId") Long companyId) {
        LambdaQueryWrapper<Position> positionLambdaQueryWrapper = new LambdaQueryWrapper<>();
        positionLambdaQueryWrapper.eq(Position::getCompanyId, companyId);
        return selectList(positionLambdaQueryWrapper);
    }


    /**
     * 获取职位类别分布统计
     * @return 职位类别和对应数量的映射
     */
    @Select("SELECT position_category as category, COUNT(*) as count " +
            "FROM position " +
            "WHERE position_category IS NOT NULL AND position_category != '' " +
            "GROUP BY position_category")
    List<Map<String, Object>> getPositionCategoryDistribution();
} 