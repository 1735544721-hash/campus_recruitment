package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.springboot.DTO.MessageDTO;

import org.example.springboot.common.Result;
import org.example.springboot.entity.Message;
import org.example.springboot.entity.User;
import org.example.springboot.service.MessageService;
import org.example.springboot.util.JwtTokenUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

import java.util.HashMap;
import java.util.List;

/**
 * 消息控制器
 */
@Tag(name = "消息管理")
@RestController
@RequestMapping("/message")
public class MessageController {
    
    private static final Logger log = LoggerFactory.getLogger(MessageController.class);
    
    @Resource
    private MessageService messageService;
    
    @Operation(summary = "发送消息")
    @PostMapping
    public Result<?> sendMessage(@RequestBody @Valid MessageDTO messageDTO) {
        log.info("发送消息：{}", messageDTO);
        Long messageId = messageService.sendMessage(messageDTO);
        return Result.success(messageId);
    }
    
    @Operation(summary = "批量发送消息")
    @PostMapping("/batch")
    public Result<?> sendBatchMessages(@RequestBody @Valid MessageDTO messageDTO) {
        log.info("批量发送消息：{}", messageDTO);
        Integer count = messageService.sendBatchMessages(messageDTO);
        return Result.success(count);
    }
    
    @Operation(summary = "根据角色批量发送消息")
    @PostMapping("/batch/role")
    public Result<?> sendBatchMessagesByRole(@RequestBody @Valid MessageDTO messageDTO) {
        log.info("根据角色批量发送消息：{}", messageDTO);
        Integer count = messageService.sendBatchMessagesByRole(messageDTO);
        return Result.success(count);
    }
    
    @Operation(summary = "分页查询消息")
    @GetMapping("/page")
    public Result<Page<Message>> getMessagePage(
            @Parameter(description = "接收人姓名") @RequestParam(required = false) String receiverName,
            @Parameter(description = "消息类型") @RequestParam(required = false) String messageType,
            @Parameter(description = "是否已读") @RequestParam(required = false) Boolean isRead,
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer currentPage,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size) {
        
        // 获取当前用户
        User currentUser = JwtTokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }
        
        log.info("查询消息列表：userId={}, messageType={}, isRead={}, currentPage={}, size={}", 
                currentUser.getId(), messageType, isRead, currentPage, size);
        
        Page<Message> page = messageService.getMessagePage(receiverName,currentUser.getId(), messageType, isRead, currentPage, size);
        return Result.success(page);
    }


    @Operation(summary = "分页查询消息")
    @GetMapping("/page/admin")
    public Result<Page<Message>> getMessagePageAdmin(
            @Parameter(description = "接收人姓名") @RequestParam(required = false) String receiverName,
            @Parameter(description = "消息类型") @RequestParam(required = false) String messageType,
            @Parameter(description = "是否已读") @RequestParam(required = false) Boolean isRead,
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer currentPage,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size) {

        // 获取当前用户
        User currentUser = JwtTokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        log.info("管理员查询消息列表：messageType={}, isRead={}, currentPage={}, size={}",
                 messageType, isRead, currentPage, size);

        Page<Message> page = messageService.getMessagePage(receiverName,null, messageType, isRead, currentPage, size);
        return Result.success(page);
    }
    
    @Operation(summary = "获取消息详情")
    @GetMapping("/{id}")
    public Result<Message> getMessageById(@PathVariable Long id) {
        log.info("获取消息详情：id={}", id);
        Message messageVO = messageService.getMessageById(id);
        return Result.success(messageVO);
    }
    
    @Operation(summary = "标记消息为已读")
    @PutMapping("/read/{id}")
    public Result<?> markAsRead(@PathVariable Long id) {
        log.info("标记消息为已读：id={}", id);
        Boolean result = messageService.markAsRead(id);
        return Result.success(result);
    }
    
    @Operation(summary = "批量标记消息为已读")
    @PutMapping("/read/batch")
    public Result<?> markBatchAsRead(@RequestBody List<Long> ids) {
        log.info("批量标记消息为已读：ids={}", ids);
        Integer count = messageService.markBatchAsRead(ids);
        return Result.success(count);
    }
    
    @Operation(summary = "标记所有消息为已读")
    @PutMapping("/read/all")
    public Result<?> markAllAsRead() {
        // 获取当前用户
        User currentUser = JwtTokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }
        
        log.info("标记所有消息为已读：userId={}", currentUser.getId());
        Integer count = messageService.markAllAsRead(currentUser.getId());
        return Result.success(count);
    }
    
    @Operation(summary = "删除消息")
    @DeleteMapping("/{id}")
    public Result<?> deleteMessage(@PathVariable Long id) {
        log.info("删除消息：id={}", id);
        Boolean result = messageService.deleteMessage(id);
        return Result.success(result);
    }
    
    @Operation(summary = "批量删除消息")
    @DeleteMapping("/batch")
    public Result<?> deleteBatchMessages(@RequestBody List<Long> ids) {
        log.info("批量删除消息：ids={}", ids);
        Integer count = messageService.deleteBatchMessages(ids);
        return Result.success(count);
    }
    
    @Operation(summary = "获取消息统计")
    @GetMapping("/count")
    public Result<HashMap<String, Long>> getMessageCount() {
        // 获取当前用户
        User currentUser = JwtTokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }
        
        log.info("获取消息统计：userId={}", currentUser.getId());

        return Result.success(messageService.getMessageCount(currentUser.getId()));
    }
} 