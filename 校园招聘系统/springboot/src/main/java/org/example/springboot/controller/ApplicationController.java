package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.example.springboot.DTO.ApplicationDTO;
import org.example.springboot.common.Result;
import org.example.springboot.entity.Application;
import org.example.springboot.service.ApplicationService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "简历投递")
@RestController
@RequestMapping("/application")
public class ApplicationController {
    
    @Resource
    private ApplicationService applicationService;
    
    @Operation(summary = "投递简历")
    @PostMapping
    public Result<?> apply(@RequestBody @Valid ApplicationDTO dto) {

        return Result.success(        applicationService.apply(dto));
    }
    
    @Operation(summary = "获取学生投递列表")
    @GetMapping("/student")
    public Result<Page<Application>> pageStudentApplications(
            @Parameter(description = "投递状态") @RequestParam(required = false) Integer status,
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate,
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Integer currentPage,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size) {
        Page<Application> page = applicationService.pageStudentApplications(currentPage, size, status, startDate, endDate);
        return Result.success(page);
    }
    
    @Operation(summary = "获取企业收到的投递列表")
    @GetMapping("/page")
    public Result<Page<Application>> pageCompanyApplications(
            @Parameter(description = "投递状态") @RequestParam(required = false) Integer status,
            @Parameter(description = "职位ID") @RequestParam(required = false) Long positionId,
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate,
            @Parameter(description = "学生关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Integer currentPage,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size) {
        Page<Application> page = applicationService.pageCompanyApplications(currentPage, size, status, positionId, startDate, endDate, keyword);
        return Result.success(page);
    }
    
    @Operation(summary = "更新投递状态")
    @PutMapping("/status/{id}")
    public Result<?> updateStatus(
            @Parameter(description = "投递ID") @PathVariable Long id,
            @Parameter(description = "新状态") @RequestParam Integer status) {
        applicationService.updateStatus(id, status);
        return Result.success();
    }
    
    @Operation(summary = "获取投递统计信息")
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics() {
        return Result.success(applicationService.getStatistics());
    }
} 