package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.example.springboot.common.Result;
import org.example.springboot.entity.Company;
import org.example.springboot.entity.User;
import org.example.springboot.service.CompanyService;
import org.example.springboot.util.JwtTokenUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

/**
 * 企业管理控制器
 */
@Tag(name = "企业管理")
@RestController
@RequestMapping("/company")
public class CompanyController {
    private static final Logger log = LoggerFactory.getLogger(CompanyController.class);
    
    @Resource
    private CompanyService companyService;
    
    @Operation(summary = "分页查询企业信息")
    @GetMapping("/page")
    public Result<?> getCompaniesByPage(
            @Parameter(description = "企业名称") @RequestParam(required = false) String companyName,
            @Parameter(description = "所属行业") @RequestParam(required = false) String industry,
            @Parameter(description = "是否认证") @RequestParam(required = false) Boolean verified,
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer currentPage,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size) {
        
        log.info("分页查询企业信息: companyName={}, industry={}, verified={}, page={}, size={}", 
                companyName, industry, verified, currentPage, size);
        
        Page<Company> page = companyService.getCompaniesByPage(companyName, industry, verified, currentPage, size);
        return Result.success(page);
    }
    
    @Operation(summary = "获取企业详情")
    @GetMapping("/{id}")
    public Result<?> getCompanyById(@Parameter(description = "企业ID") @PathVariable Long id) {
        log.info("获取企业详情: id={}", id);
        Company company = companyService.getById(id);
        return Result.success(company);
    }
    
    @Operation(summary = "获取当前用户的企业信息")
    @GetMapping("/current")
    public Result<?> getCurrentCompany() {
        User currentUser = JwtTokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("未获取到当前用户信息");
        }
        
        try {
            Company company = companyService.getByUserId(currentUser.getId());
            return Result.success(company);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @Operation(summary = "根据用户ID获取企业信息")
    @GetMapping("/user/{userId}")
    public Result<?> getCompanyByUserId(@Parameter(description = "用户ID") @PathVariable Long userId) {
        log.info("根据用户ID获取企业信息: userId={}", userId);
        
        try {
            Company company = companyService.getByUserId(userId);
            return Result.success(company);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @Operation(summary = "创建企业信息")
    @PostMapping
    public Result<?> createCompany(@Valid @RequestBody Company company) {
        log.info("创建企业信息: {}", company);
        
        // 如果没有设置用户ID，则使用当前登录用户的ID
        if (company.getUserId() == null) {
            User currentUser = JwtTokenUtils.getCurrentUser();
            if (currentUser == null) {
                return Result.error("未获取到当前用户信息");
            }
            company.setUserId(currentUser.getId());
        }
        
        Long id = companyService.createCompany(company);
        return Result.success(id);
    }
    
    @Operation(summary = "更新企业信息")
    @PutMapping("/{id}")
    public Result<?> updateCompany(
            @Parameter(description = "企业ID") @PathVariable Long id,
            @Valid @RequestBody Company company) {
        log.info("更新企业信息: id={}, company={}", id, company);
        
        companyService.updateCompany(id, company);
        return Result.success();
    }
    
    @Operation(summary = "删除企业信息")
    @DeleteMapping("/{id}")
    public Result<?> deleteCompany(@Parameter(description = "企业ID") @PathVariable Long id) {
        log.info("删除企业信息: id={}", id);
        
        companyService.deleteCompany(id);
        return Result.success();
    }
    
    @Operation(summary = "更新企业认证状态")
    @PutMapping("/{id}/verify")
    public Result<?> updateVerificationStatus(
            @Parameter(description = "企业ID") @PathVariable Long id,
            @Parameter(description = "认证状态") @RequestParam Boolean verified) {
        log.info("更新企业认证状态: id={}, verified={}", id, verified);
        
        companyService.updateVerificationStatus(id, verified);
        return Result.success();
    }
    
    @Operation(summary = "检查用户是否已有企业信息")
    @GetMapping("/check/{userId}")
    public Result<?> checkUserHasCompany(@Parameter(description = "用户ID") @PathVariable Long userId) {
        log.info("检查用户是否已有企业信息: userId={}", userId);
        
        boolean exists = companyService.existsByUserId(userId);
        return Result.success(exists);
    }
} 