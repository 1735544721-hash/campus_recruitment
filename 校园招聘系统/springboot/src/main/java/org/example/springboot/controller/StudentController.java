package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.example.springboot.common.Result;
import org.example.springboot.entity.Student;
import org.example.springboot.entity.User;
import org.example.springboot.service.StudentService;
import org.example.springboot.util.JwtTokenUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

/**
 * 学生信息控制器
 */
@Tag(name = "学生管理")
@RestController
@RequestMapping("/student")
public class StudentController {
    private static final Logger log = LoggerFactory.getLogger(StudentController.class);
    
    @Resource
    private StudentService studentService;
    
    /**
     * 创建学生信息
     * @param student 学生信息
     * @return 创建结果
     */
    @Operation(summary = "创建学生信息")
    @PostMapping
    public Result<?> createStudent(@RequestBody @Valid Student student) {
        log.info("创建学生信息: {}", student);
        
        // 如果没有设置用户ID，则使用当前登录用户的ID
        if (student.getUserId() == null) {
            User currentUser = JwtTokenUtils.getCurrentUser();
            if (currentUser == null) {
                return Result.error("未获取到当前用户信息");
            }
            student.setUserId(currentUser.getId());
        }
        
        Long studentId = studentService.createStudent(student);
        return Result.success(studentId);
    }
    
    /**
     * 根据ID获取学生信息
     * @param id 学生ID
     * @return 学生信息
     */
    @Operation(summary = "根据ID获取学生信息")
    @GetMapping("/{id}")
    public Result<Student> getStudentById(@PathVariable @Parameter(description = "学生ID") Long id) {
        log.info("获取学生信息: id={}", id);
        
        Student student = studentService.getById(id);
        return Result.success(student);
    }
    
    /**
     * 根据用户ID获取学生信息
     * @param userId 用户ID
     * @return 学生信息
     */
    @Operation(summary = "根据用户ID获取学生信息")
    @GetMapping("/user/{userId}")
    public Result<Student> getStudentByUserId(@PathVariable @Parameter(description = "用户ID") Long userId) {
        log.info("根据用户ID获取学生信息: userId={}", userId);
        
        try {
            Student student = studentService.getByUserId(userId);
            return Result.success(student);
        } catch (Exception e) {
            log.error("获取学生信息失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取当前登录用户的学生信息
     * @return 学生信息
     */
    @Operation(summary = "获取当前登录用户的学生信息")
    @GetMapping("/current")
    public Result<Student> getCurrentStudent() {
        User currentUser = JwtTokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("未获取到当前用户信息");
        }
        
        log.info("获取当前登录用户的学生信息: userId={}", currentUser.getId());
        
        try {
            Student student = studentService.getByUserId(currentUser.getId());
            return Result.success(student);
        } catch (Exception e) {
            log.error("获取学生信息失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 更新学生信息
     * @param id 学生ID
     * @param student 学生信息
     * @return 更新结果
     */
    @Operation(summary = "更新学生信息")
    @PutMapping("/{id}")
    public Result<?> updateStudent(
            @PathVariable @Parameter(description = "学生ID") Long id,
            @RequestBody @Valid Student student) {
        log.info("更新学生信息: id={}, student={}", id, student);
        
        studentService.updateStudent(id, student);
        return Result.success();
    }
    
    /**
     * 删除学生信息
     * @param id 学生ID
     * @return 删除结果
     */
    @Operation(summary = "删除学生信息")
    @DeleteMapping("/{id}")
    public Result<?> deleteStudent(@PathVariable @Parameter(description = "学生ID") Long id) {
        log.info("删除学生信息: id={}", id);
        
        studentService.deleteStudent(id);
        return Result.success();
    }
    
    /**
     * 分页查询学生信息
     * @param studentNo 学号
     * @param college 学院
     * @param major 专业
     * @param currentPage 当前页
     * @param size 每页大小
     * @return 分页结果
     */
    @Operation(summary = "分页查询学生信息")
    @GetMapping("/page")
    public Result<Page<Student>> getStudentsByPage(
            @RequestParam(required = false) @Parameter(description = "学号") String studentNo,
            @RequestParam(required = false) @Parameter(description = "学院") String college,
            @RequestParam(required = false) @Parameter(description = "专业") String major,
            @RequestParam(defaultValue = "1") @Parameter(description = "当前页") Integer currentPage,
            @RequestParam(defaultValue = "10") @Parameter(description = "每页大小") Integer size) {
        
        log.info("分页查询学生信息: studentNo={}, college={}, major={}, page={}, size={}", 
                studentNo, college, major, currentPage, size);
        
        Page<Student> page = studentService.getStudentsByPage(studentNo, college, major, currentPage, size);
        return Result.success(page);
    }
    
    /**
     * 检查用户是否已有学生信息
     * @param userId 用户ID
     * @return 是否存在
     */
    @Operation(summary = "检查用户是否已有学生信息")
    @GetMapping("/exists/{userId}")
    public Result<Boolean> checkStudentExists(@PathVariable @Parameter(description = "用户ID") Long userId) {
        log.info("检查用户是否已有学生信息: userId={}", userId);
        
        boolean exists = studentService.existsByUserId(userId);
        return Result.success(exists);
    }
} 