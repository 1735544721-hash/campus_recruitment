package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息实体类
 */
@Data
@TableName("message")
@Schema(description = "消息实体类")
public class Message {
    
    @TableId(type = IdType.AUTO)
    @Schema(description = "消息ID")
    private Long id;
    
    @Schema(description = "发送者ID")
    private Long senderId;
    
    @Schema(description = "接收者ID")
    private Long receiverId;
    
    @Schema(description = "消息类型")
    private String messageType;
    
    @Schema(description = "消息标题")
    private String title;
    
    @Schema(description = "消息内容")
    private String content;
    
    @Schema(description = "是否已读(0未读/1已读)")
    private Boolean isRead;
    
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
    
    // 非数据库字段
    @TableField(exist = false)
    @Schema(description = "发送者名称")
    private String senderName;
    
    @TableField(exist = false)
    @Schema(description = "接收者名称")
    private String receiverName;
    
    @TableField(exist = false)
    @Schema(description = "消息类型描述")
    private String messageTypeDesc;

    @TableField(exist = false)
    @Schema(description = "发送者头像")
    private String senderAvatar;
} 