package org.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.springboot.entity.Student;

@Mapper
public interface StudentMapper extends BaseMapper<Student> {
    
    /**
     * 检查指定用户ID是否存在关联的学生信息
     * @param userId 用户ID
     * @return 如果存在返回true，否则返回false
     */
    @Select("SELECT COUNT(*) > 0 FROM student WHERE user_id = #{userId}")
    boolean existsByUserId(@Param("userId") Long userId);
} 