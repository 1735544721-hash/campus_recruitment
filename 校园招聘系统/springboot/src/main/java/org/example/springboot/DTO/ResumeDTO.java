package org.example.springboot.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "简历数据传输对象")
public class ResumeDTO {
    
    @Schema(description = "简历名称")
    @NotBlank(message = "简历名称不能为空")
    private String resumeName;
    
    @Schema(description = "简历内容(JSON格式)")
    private ResumeContentDTO resumeContent;
    
    @Schema(description = "是否默认简历(0否/1是)")
    private Boolean isDefault;
    
    @Schema(description = "简历文件路径")
    private String resumeFile;
} 