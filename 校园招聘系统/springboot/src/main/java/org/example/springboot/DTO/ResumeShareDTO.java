package org.example.springboot.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "简历分享请求")
public class ResumeShareDTO {
    
    @Schema(description = "有效天数")
    @NotNull(message = "有效天数不能为空")
    @Min(value = 1, message = "有效天数最少为1天")
    @Max(value = 30, message = "有效天数最多为30天")
    private Integer validDays;
    
    @Schema(description = "访问密码")
    private String accessPassword;
} 