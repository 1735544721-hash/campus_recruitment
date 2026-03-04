package org.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.springboot.entity.Application;

@Mapper
public interface ApplicationMapper extends BaseMapper<Application> {
    // 使用MyBatis Plus提供的BaseMapper接口方法
    // 无需自定义方法
} 