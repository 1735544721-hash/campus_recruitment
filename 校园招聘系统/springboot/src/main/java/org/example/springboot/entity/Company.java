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
 * 企业实体类
 */
@Data
@TableName("company")
@Schema(description = "企业实体类")
public class Company {
    @TableId(type = IdType.AUTO)
    @Schema(description = "企业ID")
    private Long id;
    
    @Schema(description = "用户ID")
    @NotNull(message = "用户ID不能为空")
    private Long userId;
    
    @Schema(description = "企业名称")
    @NotBlank(message = "企业名称不能为空")
    private String companyName;
    
    @Schema(description = "所属行业")
    private String industry;
    
    @Schema(description = "企业规模")
    private String companySize;
    
    @Schema(description = "企业地址")
    private String companyAddress;
    
    @Schema(description = "企业Logo")
    private String companyLogo;
    
    @Schema(description = "企业简介")
    private String companyIntro;
    
    @Schema(description = "是否认证(0未认证/1已认证)")
    private Boolean verified;
    
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
    
    @TableField(exist = false)
    @Schema(description = "关联的用户信息")
    private User user;
} 