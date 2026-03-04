package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("resume")
@Schema(description = "简历实体类")
public class Resume {
    
    @TableId(type = IdType.AUTO)
    @Schema(description = "简历ID")
    private Long id;
    
    @Schema(description = "学生ID")
    private Long studentId;
    
    @Schema(description = "简历名称")
    private String resumeName;
    
    @Schema(description = "简历内容(JSON格式)")
    private String resumeContent;
    
    @Schema(description = "简历文件路径")
    private String resumeFile;
    

    
    @Schema(description = "是否默认简历(0否/1是)")
    private Boolean isDefault;
    
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
    
    @TableField(exist = false)
    @Schema(description = "学生姓名")
    private String studentName;
    
    @TableField(exist = false)
    @Schema(description = "学生学号")
    private String studentNo;
} 