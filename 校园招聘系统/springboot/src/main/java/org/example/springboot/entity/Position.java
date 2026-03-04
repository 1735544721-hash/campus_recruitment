package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 职位实体类
 */
@Data
@TableName("position")
@Schema(description = "职位实体类")
public class Position {
    @TableId(type = IdType.AUTO)
    @Schema(description = "职位ID")
    private Long id;
    
    @Schema(description = "企业ID")
    @NotNull(message = "企业ID不能为空")
    private Long companyId;
    
    @Schema(description = "职位名称")
    @NotBlank(message = "职位名称不能为空")
    private String positionName;
    
    @Schema(description = "职位类型")
    private String positionType;
    
    @Schema(description = "职位类别")
    private String positionCategory;
    
    @Schema(description = "最低薪资")
    private Integer salaryMin;
    
    @Schema(description = "最高薪资")
    private Integer salaryMax;
    
    @Schema(description = "学历要求")
    private String educationRequirement;
    
    @Schema(description = "经验要求")
    private String experienceRequirement;
    
    @Schema(description = "工作地点")
    private String workAddress;
    
    @Schema(description = "职位描述")
    private String positionDescription;
    
    @Schema(description = "职位要求")
    private String positionRequirement;
    
    @Schema(description = "招聘人数")
    private Integer positionCount;
    
    @Schema(description = "职位状态(0下线/1上线/2审核中)")
    private Integer status;
    
    @Schema(description = "截止日期")
    private LocalDate deadline;
    
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
    
    @TableField(exist = false)
    @Schema(description = "关联的企业信息")
    private Company company;
    
    @TableField(exist = false)
    @Schema(description = "企业名称")
    private String companyName;
    
    @TableField(exist = false)
    @Schema(description = "企业Logo")
    private String companyLogo;
} 