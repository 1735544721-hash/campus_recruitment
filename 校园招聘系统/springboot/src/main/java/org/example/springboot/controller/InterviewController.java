package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.entity.Company;
import org.example.springboot.entity.Interview;
import org.example.springboot.entity.Student;
import org.example.springboot.entity.User;
import org.example.springboot.service.CompanyService;
import org.example.springboot.service.InterviewService;
import org.example.springboot.service.StudentService;
import org.example.springboot.util.JwtTokenUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@Tag(name = "面试管理")
@RestController
@RequestMapping("/interview")
public class InterviewController {
    
    private static final Logger log = LoggerFactory.getLogger(InterviewController.class);
    
    @Resource
    private InterviewService interviewService;
    
    @Resource
    private CompanyService companyService;
    
    @Resource
    private StudentService studentService;
    
    @Operation(summary = "创建面试邀请")
    @PostMapping
    public Result<Long> createInterview(@RequestBody Interview interview) {
        log.info("创建面试邀请: {}", interview);
        Long id = interviewService.createInterview(interview);
        return Result.success(id);
    }
    
    @Operation(summary = "确认面试")
    @PutMapping("/confirm/{id}")
    public Result<?> confirmInterview(@PathVariable Long id) {
        log.info("确认面试: {}", id);
        interviewService.confirmInterview(id);
        return Result.success();
    }
    
    @Operation(summary = "完成面试")
    @PutMapping("/complete")
    public Result<?> completeInterview(@RequestBody Interview interview) {
        log.info("完成面试: {}", interview);
        interviewService.completeInterview(interview);
        return Result.success();
    }
    
    @Operation(summary = "取消面试")
    @PutMapping("/cancel/{id}")
    public Result<?> cancelInterview(@PathVariable Long id) {
        log.info("取消面试: {}", id);
        interviewService.cancelInterview(id);
        return Result.success();
    }
    
    @Operation(summary = "获取面试详情")
    @GetMapping("/{id}")
    public Result<Interview> getInterviewById(@PathVariable Long id) {
        log.info("获取面试详情: {}", id);
        Interview interview = interviewService.getInterviewById(id);
        return Result.success(interview);
    }
    
    @Operation(summary = "企业端分页查询面试列表")
    @GetMapping("/page")
    public Result<Page<Interview>> pageCompanyInterviews(
            @Parameter(description = "面试状态(0待确认/1已确认/2已完成/3已取消)") @RequestParam(required = false) Integer status,
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer currentPage,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size) {
        
        User currentUser = JwtTokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("无权操作");
        }

        Long companyId=null;
        if(currentUser.getRoleCode().equals("COMPANY")){
            Company company = companyService.getByUserId(currentUser.getId());
            if (company == null) {
                return Result.error("企业信息不存在");
            }
            companyId = company.getId();
        }

        
        log.info("企业端分页查询面试列表: companyId={}, status={}, currentPage={}, size={}",
                companyId, status, currentPage, size);
        
        Page<Interview> page = interviewService.pageInterviews(companyId, status, currentPage, size);
        return Result.success(page);
    }
    
    @Operation(summary = "学生端分页查询面试列表")
    @GetMapping("/student/page")
    public Result<Page<Interview>> pageStudentInterviews(
            @Parameter(description = "面试状态(0待确认/1已确认/2已完成/3已取消)") @RequestParam(required = false) Integer status,
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer currentPage,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size) {
        
        User currentUser = JwtTokenUtils.getCurrentUser();
        if (currentUser == null || !"STUDENT".equals(currentUser.getRoleCode())) {
            return Result.error("无权操作");
        }
        
        Student student = studentService.getByUserId(currentUser.getId());
        if (student == null) {
            return Result.error("学生信息不存在");
        }
        
        log.info("学生端分页查询面试列表: studentId={}, status={}, currentPage={}, size={}", 
                student.getId(), status, currentPage, size);
        
        Page<Interview> page = interviewService.pageStudentInterviews(student.getId(), status, currentPage, size);
        return Result.success(page);
    }
} 