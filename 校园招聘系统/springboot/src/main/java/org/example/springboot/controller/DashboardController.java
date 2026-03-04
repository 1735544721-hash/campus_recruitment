package org.example.springboot.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.springboot.common.Result;
import org.example.springboot.mapper.ApplicationMapper;
import org.example.springboot.mapper.CompanyMapper;
import org.example.springboot.mapper.PositionMapper;
import org.example.springboot.mapper.StudentMapper;
import org.example.springboot.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/dashboard")
@Tag(name = "仪表盘接口")
public class DashboardController {

    @Autowired
    private StudentService studentService;

    @Autowired
    private CompanyService companyService;

    @Autowired
    private PositionService positionService;

    @Autowired
    private ApplicationService applicationService;
    @Autowired
    private StudentMapper studentMapper;
    @Autowired
    private CompanyMapper companyMapper;
    @Autowired
    private PositionMapper positionMapper;
    @Autowired
    private ApplicationMapper applicationMapper;

    @GetMapping("/stats")
    @Operation(summary = "获取统计数据")
    public Result getStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 获取各种统计数据
        stats.put("totalStudents", studentMapper.selectCount(null));
        stats.put("totalCompanies", companyMapper.selectCount(null));
        stats.put("totalPositions", positionMapper.selectCount(null));
        stats.put("totalApplications", applicationMapper.selectCount(null));
        
        // 获取最近7天的申请趋势
        stats.put("applicationTrend", applicationService.getRecentApplicationTrend());
        
        // 获取职位类别分布
        stats.put("positionCategories", positionService.getPositionCategoryDistribution());
        
        return Result.success(stats);
    }
} 