package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 学生实体类
 */
@Data
@TableName("student")
@Schema(description = "学生实体类")
public class Student {
    @TableId(type = IdType.AUTO)
    @Schema(description = "学生ID")
    private Long id;
    
    @Schema(description = "用户ID")
    @NotNull(message = "用户ID不能为空")
    private Long userId;
    
    @Schema(description = "学号")
    @NotBlank(message = "学号不能为空")
    private String studentNo;
    
    @Schema(description = "学院")
    private String college;
    
    @Schema(description = "专业")
    private String major;
    
    @Schema(description = "学历")
    private String education;
    
    @Schema(description = "毕业年份")
    private Integer graduationYear;
    
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
    
    @TableField(exist = false)
    @Schema(description = "关联的用户信息")
    private User user;
} 