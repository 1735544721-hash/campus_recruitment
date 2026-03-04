package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("interview")
@Schema(description = "面试实体类")
public class Interview {
    
    @TableId(type = IdType.AUTO)
    @Schema(description = "面试ID")
    private Long id;
    
    @Schema(description = "投递ID")
    private Long applicationId;
    
    @Schema(description = "面试时间")
    private LocalDateTime interviewTime;
    
    @Schema(description = "面试地点")
    private String interviewAddress;
    
    @Schema(description = "面试类型(现场/视频/电话)")
    private String interviewType;
    
    @Schema(description = "面试备注")
    private String interviewNote;
    
    @Schema(description = "面试状态(0待确认/1已确认/2已完成/3已取消)")
    private Integer interviewStatus;
    
    @Schema(description = "面试结果(0未知/1通过/2未通过)")
    private Integer interviewResult;
    
    @Schema(description = "面试反馈")
    private String feedback;
    
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
    
    // 关联字段，不存在于数据库表中
    @TableField(exist = false)
    @Schema(description = "关联的投递记录")
    private Application application;
    
    @TableField(exist = false)
    @Schema(description = "学生姓名")
    private String studentName;
    
    @TableField(exist = false)
    @Schema(description = "职位名称")
    private String positionName;
    
    @TableField(exist = false)
    @Schema(description = "企业名称")
    private String companyName;
} 