package org.example.springboot.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "基于模板创建简历的数据传输对象")
public class ResumeTemplateDTO {
    
    @Schema(description = "模板ID")
    @NotNull(message = "模板ID不能为空")
    private Long templateId;
    
    @Schema(description = "简历名称")
    @NotBlank(message = "简历名称不能为空")
    private String resumeName;
} 