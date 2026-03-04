package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.example.springboot.DTO.ApplyPolishDTO;
import org.example.springboot.DTO.ResumeDTO;
import org.example.springboot.DTO.ResumePolishDTO;
import org.example.springboot.DTO.ResumeShareDTO;
import org.example.springboot.DTO.ResumeTemplateDTO;
import org.example.springboot.common.Result;
import org.example.springboot.entity.Resume;
import org.example.springboot.service.ResumeService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;


@Tag(name = "简历管理")
@RestController
@RequestMapping("/resume")
public class ResumeController {

    @Resource
    private ResumeService resumeService;
    
    @Operation(summary = "创建简历")
    @PostMapping
    public Result<Long> create(@RequestBody @Valid ResumeDTO resumeDTO) {
        return Result.success(resumeService.create(resumeDTO));
    }
    
    @Operation(summary = "获取简历列表")
    @GetMapping("/list")
    public Result<List<Resume>> list() {
        return Result.success(resumeService.listByStudent());
    }
    
    @Operation(summary = "获取简历详情")
    @GetMapping("/{id}")
    public Result<Resume> getById(@Parameter(description = "简历ID") @PathVariable Long id) {
        return Result.success(resumeService.getById(id));
    }
    
    @Operation(summary = "更新简历")
    @PutMapping("/{id}")
    public Result<?> update(
            @Parameter(description = "简历ID") @PathVariable Long id,
            @RequestBody @Valid ResumeDTO resumeDTO) {
        resumeService.update(id, resumeDTO);
        return Result.success();
    }
    
    @Operation(summary = "删除简历")
    @DeleteMapping("/{id}")
    public Result<?> delete(@Parameter(description = "简历ID") @PathVariable Long id) {
        resumeService.delete(id);
        return Result.success();
    }
    
    @Operation(summary = "设置默认简历")
    @PutMapping("/default/{id}")
    public Result<?> setDefault(@Parameter(description = "简历ID") @PathVariable Long id) {
        resumeService.setDefault(id);
        return Result.success();
    }
    
    @Operation(summary = "上传简历附件")
    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        // 实际应用中需要实现文件上传逻辑
        // 这里简单返回模拟路径
        return Result.success("/api/files/resume/sample.pdf");
    }

    
    @Operation(summary = "简历预览")
    @GetMapping("/preview/{id}")
    public Result<Resume> preview(@Parameter(description = "简历ID") @PathVariable Long id) {
        // 直接返回简历内容，不再返回HTML
        return Result.success(resumeService.getById(id));
    }
    
    @Operation(summary = "管理员分页查询所有简历")
    @GetMapping("/admin/page")
    public Result<Page<Resume>> adminPage(
            @RequestParam(defaultValue = "1") Integer currentPage,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {

        Page<Resume> page = resumeService.adminPage(currentPage, size, keyword);
        return Result.success(page);
    }



} 