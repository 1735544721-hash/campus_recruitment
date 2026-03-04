package org.example.springboot.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 消息DTO
 */
@Data
@Schema(description = "消息传输对象")
public class MessageDTO {
    
    @Schema(description = "消息ID")
    private Long id;
    
    @Schema(description = "发送者ID，系统消息可不填")
    private Long senderId;
    
    @Schema(description = "接收者ID")
    private Long receiverId;
    
    @Schema(description = "接收者ID列表，用于群发消息")
    private List<Long> receiverIds;
    
    @Schema(description = "接收者类型，用于按角色发送消息（STUDENT、COMPANY、ADMIN、ALL）")
    private String receiverType;
    
    @NotBlank(message = "消息类型不能为空")
    @Schema(description = "消息类型")
    private String messageType;
    
    @NotBlank(message = "消息标题不能为空")
    @Schema(description = "消息标题")
    private String title;
    
    @Schema(description = "消息内容")
    private String content;
} 