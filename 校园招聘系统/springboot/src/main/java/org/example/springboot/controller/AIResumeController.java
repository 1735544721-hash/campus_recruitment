package org.example.springboot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.example.springboot.DTO.ApplyPolishDTO;
import org.example.springboot.DTO.ResumePolishDTO;
import org.example.springboot.common.Result;
import org.example.springboot.service.ResumeService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "简历AI功能")
@RestController
@RequestMapping("/resume")
public class AIResumeController {

    @Resource
    private ResumeService resumeService;
    
    @Operation(summary = "AI润色简历")
    @PostMapping("/ai-polish")
    public Result<Map<String, Object>> aiPolish(@RequestBody @Valid ResumePolishDTO dto) {
        Map<String, Object> result = resumeService.polishResume(dto);
        return Result.success(result);
    }
    
    @Operation(summary = "应用AI润色结果")
    @PostMapping("/apply-polish")
    public Result<?> applyPolish(@RequestBody @Valid ApplyPolishDTO dto) {
        resumeService.applyPolish(dto);
        return Result.success();
    }
} 