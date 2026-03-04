package org.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.springboot.entity.Company;

@Mapper
public interface CompanyMapper extends BaseMapper<Company> {
    
    /**
     * 检查指定用户ID是否存在关联的企业信息
     * @param userId 用户ID
     * @return 如果存在返回true，否则返回false
     */
    @Select("SELECT COUNT(*) > 0 FROM company WHERE user_id = #{userId}")
    boolean existsByUserId(@Param("userId") Long userId);
    
    /**
     * 根据用户ID查询企业信息
     * @param userId 用户ID
     * @return 企业信息
     */
    @Select("SELECT * FROM company WHERE user_id = #{userId} LIMIT 1")
    Company getCompanyByUserId(@Param("userId") Long userId);
} 