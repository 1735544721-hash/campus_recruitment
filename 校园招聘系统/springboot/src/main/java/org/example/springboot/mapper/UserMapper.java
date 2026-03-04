package org.example.springboot.mapper;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.springboot.entity.User;

import java.util.List;
import java.util.stream.Collectors;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    
    /**
     * 根据姓名关键词查询用户ID列表
     * 默认实现，使用MyBatis Plus条件构造器
     * @param keyword 姓名关键词
     * @return 用户ID列表
     */
    default List<Long> getUserIdsByNameKeyword(String keyword) {
        LambdaQueryWrapper<User> query = new LambdaQueryWrapper<>();
        query.like(User::getName, keyword)
             .select(User::getId);
        return this.selectList(query)
                  .stream()
                  .map(User::getId)
                  .collect(Collectors.toList());
    }
}
