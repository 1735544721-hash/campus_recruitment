package org.example.springboot.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResumeContentDTO implements Serializable {
    private BasicInfoDTO basicInfo;
    private List<EducationDTO> education;
    private List<WorkExperienceDTO> workExperience;
    private List<ProjectExperienceDTO> projectExperience;
    private List<SkillDTO> skills;
    private String selfEvaluation;
    private JobIntentionDTO jobIntention;
    private List<CertificateDTO> certificates;
    private List<LanguageDTO> languages;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BasicInfoDTO implements Serializable {
        private String name;
        private String gender;
        private String birthDate;
        private String email;
        private String phone;
        private String address;
        private String photo;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EducationDTO implements Serializable {
        private String school;
        private String major;
        private String degree;
        private String startDate;
        private String endDate;
        private String gpa;
        private String description;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkExperienceDTO implements Serializable {
        private String company;
        private String position;
        private String startDate;
        private String endDate;
        private String description;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectExperienceDTO implements Serializable {
        private String name;
        private String role;
        private String startDate;
        private String endDate;
        private String description;
        private String techStack;
        private String link;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkillDTO implements Serializable {
        private String name;
        private Integer proficiency;
        private String description;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JobIntentionDTO implements Serializable {
        private String position;
        private String industry;
        private String location;
        private String salaryExpectation;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CertificateDTO implements Serializable {
        private String name;
        private String issueDate;
        private String issuingOrganization;
        private String description;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LanguageDTO implements Serializable {
        private String name;
        private String proficiency;
        private String description;
    }
}
