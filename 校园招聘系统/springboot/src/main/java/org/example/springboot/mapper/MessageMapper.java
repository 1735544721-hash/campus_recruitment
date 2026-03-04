package org.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.springboot.entity.Message;

/**
 * 消息Mapper接口
 */
@Mapper
public interface MessageMapper extends BaseMapper<Message> {
    

    
    /**
     * 统计用户未读消息数
     *
     * @param userId 用户ID
     * @return 未读消息数
     */
    Integer countUnreadMessages(@Param("userId") Long userId);
} 