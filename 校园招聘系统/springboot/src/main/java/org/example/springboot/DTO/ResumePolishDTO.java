package org.example.springboot.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "简历润色请求")
public class ResumePolishDTO {
    
    @Schema(description = "简历ID")
    @NotNull(message = "简历ID不能为空")
    private Long resumeId;
    
    @Schema(description = "润色类型(full:整体润色, partial:部分润色)")
    private String polishType;
    
    @Schema(description = "需要润色的部分，当polishType为partial时必填")
    private String targetPart;
    
    @Schema(description = "风格类型(professional:专业型, technical:技术型, management:管理型, creative:创意型)")
    private String styleType;
    
    @Schema(description = "目标职位，用于关键词优化")
    private String targetPosition;
    
    @Schema(description = "自定义提示(可选)")
    private String customPrompt;
} 