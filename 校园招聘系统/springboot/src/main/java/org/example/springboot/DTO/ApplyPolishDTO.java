package org.example.springboot.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
@Schema(description = "应用简历润色结果请求")
public class ApplyPolishDTO {
    
    @Schema(description = "简历ID")
    @NotNull(message = "简历ID不能为空")
    private Long resumeId;
    
    @Schema(description = "润色后的内容")
    private Object polishedContent;
    
    @Schema(description = "应用的目标部分")
    private String targetPart;
} 