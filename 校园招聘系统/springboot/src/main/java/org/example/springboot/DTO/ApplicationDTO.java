package org.example.springboot.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "简历投递请求")
public class ApplicationDTO {
    
    @Schema(description = "职位ID")
    @NotNull(message = "职位ID不能为空")
    private Long positionId;
    
    @Schema(description = "简历ID")
    @NotNull(message = "简历ID不能为空")
    private Long resumeId;
} 