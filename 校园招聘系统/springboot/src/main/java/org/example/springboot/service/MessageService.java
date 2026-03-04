package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.springboot.DTO.MessageDTO;

import org.example.springboot.entity.Message;
import org.example.springboot.entity.User;
import org.example.springboot.enumClass.MessageTypeEnum;
import org.example.springboot.exception.ServiceException;
import org.example.springboot.mapper.MessageMapper;
import org.example.springboot.mapper.UserMapper;
import org.example.springboot.util.JwtTokenUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 消息服务实现类
 */
@Service
public class MessageService {
    
    private static final Logger log = LoggerFactory.getLogger(MessageService.class);
    
    @Resource
    private MessageMapper messageMapper;
    
    @Resource
    private UserMapper userMapper;
    

    @Transactional(rollbackFor = Exception.class)
    public Long sendMessage(MessageDTO messageDTO) {
        // 验证接收者是否存在
        User receiver = userMapper.selectById(messageDTO.getReceiverId());
        if (receiver == null) {
            throw new ServiceException("接收者不存在");
        }
        
        // 获取当前用户作为发送者
        User currentUser = JwtTokenUtils.getCurrentUser();
        Long senderId = null;
        if (currentUser != null) {
            senderId = currentUser.getId();
        }
        
        // 创建消息实体
        Message message = new Message();
        message.setSenderId(senderId);
        message.setReceiverId(messageDTO.getReceiverId());
        message.setMessageType(messageDTO.getMessageType());
        message.setTitle(messageDTO.getTitle());
        message.setContent(messageDTO.getContent());
        message.setIsRead(false);
        message.setCreateTime(LocalDateTime.now());
        
        // 保存消息
        messageMapper.insert(message);
        log.info("发送消息成功，消息ID：{}", message.getId());
        
        return message.getId();
    }
    

    @Transactional(rollbackFor = Exception.class)
    public Integer sendBatchMessages(MessageDTO messageDTO) {
        if (messageDTO.getReceiverIds() == null || messageDTO.getReceiverIds().isEmpty()) {
            throw new ServiceException("接收者列表不能为空");
        }
        
        // 获取当前用户作为发送者
        User currentUser = JwtTokenUtils.getCurrentUser();
        Long senderId = null;
        if (currentUser != null) {
            senderId = currentUser.getId();
        }
        
        // 批量创建消息
        List<Message> messageList = new ArrayList<>();
        for (Long receiverId : messageDTO.getReceiverIds()) {
            Message message = new Message();
            message.setSenderId(senderId);
            message.setReceiverId(receiverId);
            message.setMessageType(messageDTO.getMessageType());
            message.setTitle(messageDTO.getTitle());
            message.setContent(messageDTO.getContent());
            message.setIsRead(false);
            message.setCreateTime(LocalDateTime.now());
            messageList.add(message);
        }
        
        // 批量保存消息
        int successCount = 0;
        for (Message message : messageList) {
            try {
                messageMapper.insert(message);
                successCount++;
            } catch (Exception e) {
                log.error("发送消息失败，接收者ID：{}", message.getReceiverId(), e);
            }
        }
        
        log.info("批量发送消息成功，成功数量：{}", successCount);
        return successCount;
    }
    

    @Transactional(rollbackFor = Exception.class)
    public Integer sendBatchMessagesByRole(MessageDTO messageDTO) {
        if (messageDTO.getReceiverType() == null || messageDTO.getReceiverType().isEmpty()) {
            throw new ServiceException("接收者类型不能为空");
        }
        
        // 获取当前用户作为发送者
        User currentUser = JwtTokenUtils.getCurrentUser();
        Long senderId = null;
        if (currentUser != null) {
            senderId = currentUser.getId();
        }
        
        // 查询符合条件的用户
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        if ("ALL".equals(messageDTO.getReceiverType())) {
            // 发送给所有用户，不添加角色筛选条件
        } else {
            // 发送给特定角色的用户
            queryWrapper.eq(User::getRoleCode, messageDTO.getReceiverType());
        }
        
        List<User> users = userMapper.selectList(queryWrapper);
        if (users.isEmpty()) {
            throw new ServiceException("未找到符合条件的接收者");
        }
        
        // 批量创建消息
        List<Message> messageList = new ArrayList<>();
        for (User user : users) {
            // 跳过发送者自己
            if (senderId != null && senderId.equals(user.getId())) {
                continue;
            }
            
            Message message = new Message();
            message.setSenderId(senderId);
            message.setReceiverId(user.getId());
            message.setMessageType(messageDTO.getMessageType());
            message.setTitle(messageDTO.getTitle());
            message.setContent(messageDTO.getContent());
            message.setIsRead(false);
            message.setCreateTime(LocalDateTime.now());
            messageList.add(message);
        }
        
        // 批量保存消息
        int successCount = 0;
        for (Message message : messageList) {
            try {
                messageMapper.insert(message);
                successCount++;
            } catch (Exception e) {
                log.error("发送消息失败，接收者ID：{}", message.getReceiverId(), e);
            }
        }
        
        log.info("根据角色批量发送消息成功，成功数量：{}", successCount);
        return successCount;
    }
    

    public Page<Message> getMessagePage(String receiverName,Long userId, String messageType, Boolean isRead, Integer currentPage, Integer size) {

        LambdaQueryWrapper<Message> messageLambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(receiverName)) {
            List<Long> receiverIds = userMapper
                    .selectList(new LambdaQueryWrapper<User>()
                            .like(User::getName, receiverName))
                    .stream()
                    .map(User::getId)
                    .collect(Collectors.toList());

            // 没查到用户直接返回空页
            if (receiverIds.isEmpty()) {
                return new Page<>(currentPage, size);
            }
            messageLambdaQueryWrapper.in(Message::getReceiverId, receiverIds);
        }
        if(userId!=null) {
            messageLambdaQueryWrapper.eq(Message::getReceiverId, userId);
        }
        if(StringUtils.isNotBlank(messageType)) {
            messageLambdaQueryWrapper.eq(Message::getMessageType, messageType);
        }
        if(isRead!=null) {
            messageLambdaQueryWrapper.eq(Message::getIsRead, isRead);
        }
        Page<Message> pageData = messageMapper.selectPage(new Page<>(currentPage,size), messageLambdaQueryWrapper);

        List<Message> records = pageData.getRecords();
        records.forEach(this::fillInfo);
        pageData.setRecords(records);


        
        return pageData;
    }

    void fillInfo(Message message) {
        if(message.getReceiverId()!=null) {
            User recv = userMapper.selectById(message.getReceiverId());
            if(recv!=null) {
                message.setReceiverName(recv.getName());
            }


        }
        if(message.getSenderId()!=null) {
            User sender = userMapper.selectById(message.getSenderId());
            if(sender!=null) {
                message.setSenderName(sender.getName());
                message.setSenderAvatar(sender.getAvatar());
            }else{
                message.setSenderName("系统");
                message.setSenderAvatar("/api/files/img/system_avatar.png");
            }

        }else{

                message.setSenderName("系统");
                message.setSenderAvatar("/api/files/img/system_avatar.png");

        }
           MessageTypeEnum typeEnum = MessageTypeEnum.getByCode(message.getMessageType());
        message.setMessageTypeDesc(typeEnum.getDesc());

    }


    

    public Message getMessageById(Long id) {
        // 查询消息
        Message message = messageMapper.selectById(id);
        if (message == null) {
            throw new ServiceException("消息不存在");
        }
        
        // 验证当前用户是否有权限查看该消息
        User currentUser = JwtTokenUtils.getCurrentUser();
        if (currentUser == null || !Objects.equals(message.getReceiverId(), currentUser.getId())) {
            throw new ServiceException("无权限查看该消息");
        }
        
    fillInfo(message);

        
        // 标记为已读
        if (!message.getIsRead()) {
            markAsRead(id);
        }
        
        return message;
    }
    

    @Transactional(rollbackFor = Exception.class)
    public Boolean markAsRead(Long id) {
        // 查询消息
        Message message = messageMapper.selectById(id);
        if (message == null) {
            throw new ServiceException("消息不存在");
        }
        
        // 验证当前用户是否有权限标记该消息
        User currentUser = JwtTokenUtils.getCurrentUser();
        if (currentUser == null || !Objects.equals(message.getReceiverId(), currentUser.getId())) {
            throw new ServiceException("无权限标记该消息");
        }
        
        // 已读则不需要更新
        if (message.getIsRead()) {
            return true;
        }
        
        // 更新消息状态
        Message updateMessage = new Message();
        updateMessage.setId(id);
        updateMessage.setIsRead(true);
        updateMessage.setUpdateTime(LocalDateTime.now());
        
        int result = messageMapper.updateById(updateMessage);
        return result > 0;
    }
    

    @Transactional(rollbackFor = Exception.class)
    public Integer markBatchAsRead(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        
        // 获取当前用户
        User currentUser = JwtTokenUtils.getCurrentUser();
        if (currentUser == null) {
            throw new ServiceException("用户未登录");
        }
        
        // 查询属于当前用户的未读消息
        LambdaQueryWrapper<Message> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Message::getId, ids)
                .eq(Message::getReceiverId, currentUser.getId())
                .eq(Message::getIsRead, false);
        List<Message> messageList = messageMapper.selectList(queryWrapper);
        
        if (messageList.isEmpty()) {
            return 0;
        }
        
        // 批量更新消息状态
        LambdaUpdateWrapper<Message> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(Message::getId, messageList.stream().map(Message::getId).toList())
                .set(Message::getIsRead, true)
                .set(Message::getUpdateTime, LocalDateTime.now());
        
        int result = messageMapper.update(null, updateWrapper);
        return result;
    }
    

    @Transactional(rollbackFor = Exception.class)
    public Integer markAllAsRead(Long userId) {
        // 查询该用户的所有未读消息
        LambdaQueryWrapper<Message> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Message::getReceiverId, userId)
                .eq(Message::getIsRead, false);
        List<Message> messageList = messageMapper.selectList(queryWrapper);
        
        if (messageList.isEmpty()) {
            return 0;
        }
        
        // 批量更新消息状态
        LambdaUpdateWrapper<Message> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Message::getReceiverId, userId)
                .eq(Message::getIsRead, false)
                .set(Message::getIsRead, true)
                .set(Message::getUpdateTime, LocalDateTime.now());
        
        int result = messageMapper.update(null, updateWrapper);
        return result;
    }
    

    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteMessage(Long id) {
        // 查询消息
        Message message = messageMapper.selectById(id);
        if (message == null) {
            throw new ServiceException("消息不存在");
        }
        
        // 验证当前用户是否有权限删除该消息
        User currentUser = JwtTokenUtils.getCurrentUser();
        if(currentUser==null) {
            throw new ServiceException("用户未登录");
        }
        if (!currentUser.getRoleCode().equals("ADMIN")&&( !Objects.equals(message.getReceiverId(), currentUser.getId()))) {
            throw new ServiceException("无权限删除该消息");
        }
        
        // 删除消息
        int result = messageMapper.deleteById(id);
        return result > 0;
    }
    

    @Transactional(rollbackFor = Exception.class)
    public Integer deleteBatchMessages(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        
        // 获取当前用户
        User currentUser = JwtTokenUtils.getCurrentUser();
        if (currentUser == null) {
            throw new ServiceException("用户未登录");
        }
        
        // 查询属于当前用户的消息
        LambdaQueryWrapper<Message> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Message::getId, ids)
                .eq(Message::getReceiverId, currentUser.getId());
        List<Message> messageList = messageMapper.selectList(queryWrapper);
        
        if (messageList.isEmpty()) {
            return 0;
        }
        
        // 批量删除消息
        int result = messageMapper.deleteBatchIds(messageList.stream().map(Message::getId).toList());
        return result;
    }
    

    public HashMap<String, Long> getMessageCount(Long userId) {
        // 查询总消息数
        LambdaQueryWrapper<Message> totalQuery = new LambdaQueryWrapper<>();
        totalQuery.eq(Message::getReceiverId, userId);
        Long totalCount =messageMapper.selectCount(totalQuery);
        
        // 查询未读消息数
        Long unreadCount = messageMapper.selectCount(new LambdaQueryWrapper<Message>().eq(Message::getReceiverId,userId).eq(Message::getIsRead,0));
        HashMap<String, Long> MessageCountRes = new HashMap<>();
        MessageCountRes.put("totalCount", totalCount);
        MessageCountRes.put("unreadCount", unreadCount);
        return MessageCountRes;
    }
} 