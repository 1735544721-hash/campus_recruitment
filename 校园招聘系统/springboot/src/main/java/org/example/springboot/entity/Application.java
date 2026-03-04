package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 投递记录实体类
 */
@Data
@TableName("application")
@Schema(description = "投递记录实体类")
public class Application {
    
    @TableId(type = IdType.AUTO)
    @Schema(description = "投递ID")
    private Long id;
    
    @Schema(description = "学生ID")
    private Long studentId;
    
    @Schema(description = "职位ID")
    private Long positionId;
    
    @Schema(description = "简历ID")
    private Long resumeId;
    
    @Schema(description = "投递状态(0待查看/1已查看/2通过筛选/3面试邀请/4不合适/5已录用)")
    private Integer status;
    
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
    
    // 非数据库字段，用于前端展示
    @TableField(exist = false)
    @Schema(description = "职位名称")
    private String positionName;
    
    @TableField(exist = false)
    @Schema(description = "企业名称")
    private String companyName;
    
    @TableField(exist = false)
    @Schema(description = "企业Logo")
    private String companyLogo;
    
    @TableField(exist = false)
    @Schema(description = "简历名称")
    private String resumeName;
    
    @TableField(exist = false)
    @Schema(description = "状态名称")
    private String statusName;
    
    @TableField(exist = false)
    @Schema(description = "学生姓名")
    private String studentName;
    
    @TableField(exist = false)
    @Schema(description = "学生学号")
    private String studentNo;
    
    @TableField(exist = false)
    @Schema(description = "关联的职位信息")
    private Position position;
    
    @TableField(exist = false)
    @Schema(description = "关联的学生信息")
    private Student student;
    
    @TableField(exist = false)
    @Schema(description = "关联的简历信息")
    private Resume resume;
} 