package org.example.springboot.util;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class SpringAITool {
    /**
     * 自我评价润色结果
     */
    @JsonPropertyOrder({"polishedContent","overallEvaluation","suggestion"})
    public record SelfEvaluationPolishResult(
            String polishedContent,
            String overallEvaluation,
            String suggestion
        ) {}


    /**
     * 工作经历润色结果
     */
    @JsonPropertyOrder({"polishedContent","overallEvaluation","suggestion"})
    public record WorkExperiencePolishResult(
         List<WorkExperienceItem> polishedContent, // 优化后的工作经历
         String overallEvaluation,                 // 总体评价
         String suggestion                         // 具体建议
    ) {}

    /**
     * 工作经历项
     */
    @JsonPropertyOrder({"company","position","startDate","endDate","description"})
    public record WorkExperienceItem (
         String company,       // 公司名称
         String position,      // 职位
         String startDate,     // 开始日期
         String endDate,       // 结束日期
         String description    // 工作描述（这是需要润色的部分）
    ) {}

    /**
     * 项目经历润色结果
     */
    @JsonPropertyOrder({"polishedContent","overallEvaluation","suggestion"})
    public record ProjectExperiencePolishResult (
        List<ProjectExperienceItem> polishedContent, // 优化后的项目经历
        String overallEvaluation,                    // 总体评价
        String suggestion                            // 具体建议
    ) {}

    /**
     * 项目经历项
     */
    @JsonPropertyOrder({"projectName","role","startDate","endDate","description"})
    public record ProjectExperienceItem (
        String projectName,   // 项目名称
        String role,          // 担任角色
        String startDate,     // 开始日期
        String endDate,       // 结束日期
        String description    // 项目描述（这是需要润色的部分）
    ) {}

    /**
     * 技能特长润色结果
     */
    @JsonPropertyOrder({"polishedContent","overallEvaluation","suggestion"})
    public record SkillsPolishResult (
        List<SkillItem> polishedContent,  // 优化后的技能特长
        String overallEvaluation,         // 总体评价
        String suggestion                 // 具体建议
    ) {}

    /**
     * 技能项
     */
    @JsonPropertyOrder({"name","proficiency","description"})
    public record SkillItem (
        String name,          // 技能名称
        String proficiency,   // 熟练程度
        String description    // 技能描述（这是需要润色的部分）
    ) {}
}
