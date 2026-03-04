package org.example.springboot.service;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.springboot.DTO.*;
import org.example.springboot.entity.Resume;
import org.example.springboot.entity.Student;
import org.example.springboot.entity.User;
import org.example.springboot.exception.ServiceException;
import org.example.springboot.mapper.ResumeMapper;
import org.example.springboot.mapper.StudentMapper;
import org.example.springboot.mapper.UserMapper;
import org.example.springboot.util.JwtTokenUtils;
import org.example.springboot.util.SpringAITool;

import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ai.chat.client.ChatClient;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.springboot.mapper.ApplicationMapper;

import java.util.stream.Collectors;

@Service
@Slf4j
public class ResumeService {

    @Resource
    private ResumeMapper resumeMapper;
    
    @Resource
    private StudentMapper studentMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ApplicationMapper applicationMapper;

    @Autowired
    @Qualifier("open-ai")
    private ChatClient chatClient;
    
    @Autowired
    private SpringAITool springAITool;

    /**
     * 创建简历
     * @param resumeDTO 简历数据
     * @return 简历ID
     */
    @Transactional
    public Long create(ResumeDTO resumeDTO) {
        // 获取当前登录学生
        Student student = getCurrentStudent();
        
        // 如果设置为默认简历，则将其他简历设置为非默认
        if (resumeDTO.getIsDefault() != null && resumeDTO.getIsDefault()) {
            resetDefaultResume(student.getId());
        }
        
        Resume resume = new Resume();
        resume.setStudentId(student.getId());
        resume.setResumeName(resumeDTO.getResumeName());
        resume.setResumeContent(new JSONObject(resumeDTO.getResumeContent()).toString());
        resume.setResumeFile(resumeDTO.getResumeFile());
        resume.setIsDefault(resumeDTO.getIsDefault() != null ? resumeDTO.getIsDefault() : false);
        resume.setCreateTime(LocalDateTime.now());
        
        resumeMapper.insert(resume);
        return resume.getId();
    }
    
    /**
     * 根据ID获取简历详情
     * @param id 简历ID
     * @return 简历详情
     */
    public Resume getById(Long id) {
        Resume resume = resumeMapper.selectById(id);
        if (resume == null) {
            throw new ServiceException("简历不存在");
        }
        
        // 填充学生信息
        Student student = studentMapper.selectById(resume.getStudentId());
        if (student != null) {
            User user = userMapper.selectById(student.getUserId());
            resume.setStudentName(user.getName());
            resume.setStudentNo(student.getStudentNo());
        }
        
        return resume;
    }
    
    /**
     * 更新简历
     * @param id 简历ID
     * @param resumeDTO 简历数据
     */
    @Transactional
    public void update(Long id, ResumeDTO resumeDTO) {
        Resume resume = getById(id);
        checkResumeOwnership(resume);
        
        // 如果设置为默认简历，则将其他简历设置为非默认
        if (resumeDTO.getIsDefault() != null && resumeDTO.getIsDefault()) {
            resetDefaultResume(resume.getStudentId());
        }
        
        resume.setResumeName(resumeDTO.getResumeName());
        if (ObjectUtils.isNotEmpty(resumeDTO.getResumeContent())) {
            resume.setResumeContent(new JSONObject(resumeDTO.getResumeContent()).toString());
        }
        if (ObjectUtils.isNotEmpty(resumeDTO.getResumeFile())) {
            resume.setResumeFile(resumeDTO.getResumeFile());
        }
        resume.setIsDefault(resumeDTO.getIsDefault() != null ? resumeDTO.getIsDefault() : resume.getIsDefault());
        resume.setUpdateTime(LocalDateTime.now());
        
        resumeMapper.updateById(resume);
    }
    
    /**
     * 删除简历
     * @param id 简历ID
     */
    @Transactional
    public void delete(Long id) {
        Resume resume = getById(id);
        checkResumeOwnership(resume);
        // 检查是否有投递记录
//        LambdaQueryWrapper<Application> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(Application::getResumeId, id);
//        long count = applicationMapper.selectCount(queryWrapper);
//        if (count > 0) {
//            throw new ServiceException("此简历有投递记录，不能删除");
//        }
        resumeMapper.deleteById(id);
        
        // 如果删除的是默认简历，则将最新的一条简历设置为默认
        if (resume.getIsDefault()) {
            setNewDefaultResume(resume.getStudentId());
        }
    }
    
    /**
     * 获取学生的简历列表
     * @return 简历列表
     */
    public List<Resume> listByStudent() {
        Student student = getCurrentStudent();
        
        LambdaQueryWrapper<Resume> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Resume::getStudentId, student.getId())
                    .orderByDesc(Resume::getIsDefault)
                    .orderByDesc(Resume::getUpdateTime);
        
        return resumeMapper.selectList(queryWrapper);
    }
    
    /**
     * 设置默认简历
     * @param id 简历ID
     */
    @Transactional
    public void setDefault(Long id) {
        Resume resume = getById(id);
        checkResumeOwnership(resume);
        
        // 将其他简历设置为非默认
        resetDefaultResume(resume.getStudentId());
        
        // 设置当前简历为默认
        resume.setIsDefault(true);
        resume.setUpdateTime(LocalDateTime.now());
        resumeMapper.updateById(resume);
    }

    
    /**
     * 从简历内容中提取需要润色的部分
     */
    private String extractTargetContent(String resumeContent, String polishType, String targetPart) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> contentMap = objectMapper.readValue(resumeContent, Map.class);
            
            // 提取指定部分
            Map<String, Object> extractedContent = new HashMap<>();
            if (contentMap.containsKey(targetPart)) {
                extractedContent.put(targetPart, contentMap.get(targetPart));
            }
            
            return objectMapper.writeValueAsString(extractedContent);
        } catch (Exception e) {
            throw new ServiceException("简历内容解析失败");
        }
    }
    
    /**
     * 简历润色（AI优化）
     * @param dto 润色请求
     * @return 润色结果
     */
    public Map<String, Object> polishResume(ResumePolishDTO dto) {
        // 获取简历
        Resume resume = getById(dto.getResumeId());
        checkResumeOwnership(resume);
        
        // 从简历内容中提取要润色的部分
        String resumeContent = resume.getResumeContent();
        String targetContent = extractTargetContent(resumeContent, dto.getPolishType(), dto.getTargetPart());
        
        // 检查内容长度，如果过长可能导致超时
        if (targetContent.length() > 5000) {
            log.warn("润色内容过长({}字符)，可能导致超时", targetContent.length());
        }
        log.info("需要润色的内容为：{}", targetContent);
        
        try {
            // 解析原始简历内容，用于后续比对和补全
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> originalContentMap = objectMapper.readValue(resumeContent, Map.class);
            
            // 确保targetContent包含所有必要字段，即使是空的
            Map<String, Object> targetContentMap = objectMapper.readValue(targetContent, Map.class);
            
            // 更新targetContent
            targetContent = objectMapper.writeValueAsString(targetContentMap);
            log.info("增强后的目标内容为：{}", targetContent);
        } catch (Exception e) {
            log.error("增强目标内容失败", e);
            // 继续使用原始targetContent
        }
        
        // 构建提示词模板

        String promptTemplate = buildPrompt(resume,targetContent, dto.getTargetPart(), dto.getStyleType(), dto.getTargetPosition(), dto.getCustomPrompt());
        
        // 使用SpringAI的ChatClient根据目标部分调用相应的Tool
        Map<String, Object> responseResult = new HashMap<>();
        
        try {
            log.info("使用SpringAI进行简历润色，目标部分: {}", dto.getTargetPart());
            Prompt prompt = Prompt.builder().messages(new SystemMessage(promptTemplate)).build();
            // 根据不同的目标部分调用不同的工具方法
            switch (dto.getTargetPart()) {
                case "selfEvaluation" -> {
                    // 调用自我评价润色工具

                    SpringAITool.SelfEvaluationPolishResult result = chatClient
                            .prompt(prompt)
                            .call()

                            .entity(SpringAITool.SelfEvaluationPolishResult.class);

                    
                    // 构建返回结果
                    responseResult.put("targetPart", dto.getTargetPart());
                    responseResult.put("polishedContent", result.polishedContent());
                    responseResult.put("overallEvaluation", result.overallEvaluation());
                    responseResult.put("suggestion", result.suggestion());
                }
                case "workExperience" -> {
                    // 调用工作经历润色工具

                    SpringAITool.WorkExperiencePolishResult result = chatClient
                            .prompt(prompt)
                            .call()
                            .entity(SpringAITool.WorkExperiencePolishResult.class);

                    
                    // 构建返回结果

                    responseResult.put("targetPart", dto.getTargetPart());
                    responseResult.put("polishedContent", result.polishedContent());
                    responseResult.put("overallEvaluation", result.overallEvaluation());
                    responseResult.put("suggestion", result.suggestion());
                }
                case "projectExperience" -> {
                    // 调用项目经历润色工具
                    SpringAITool.ProjectExperiencePolishResult result = chatClient
                            .prompt(prompt)
                            .call()
                            .entity(SpringAITool.ProjectExperiencePolishResult.class);
                    
                    // 构建返回结果
                    responseResult.put("targetPart", dto.getTargetPart());
                    responseResult.put("polishedContent", result.polishedContent());
                    responseResult.put("overallEvaluation", result.overallEvaluation());
                    responseResult.put("suggestion", result.suggestion());
                }
                case "skills" -> {
                    // 调用技能特长润色工具
                    SpringAITool.SkillsPolishResult result = chatClient.prompt(prompt).call().entity(SpringAITool.SkillsPolishResult.class);
                    // 构建返回结果
                    responseResult.put("targetPart", dto.getTargetPart());
                    responseResult.put("polishedContent", result.polishedContent());
                    responseResult.put("overallEvaluation", result.overallEvaluation());
                    responseResult.put("suggestion", result.suggestion());
                }
                default -> throw new ServiceException("不支持的简历部分: " + dto.getTargetPart());
            }
            

            
            log.info("结构化的润色结果: {}", responseResult);
            return responseResult;
            
        } catch (Exception e) {
            log.error("调用AI服务失败", e);
            throw new ServiceException("AI服务调用失败: " + e.getMessage());
        }
    }
    

    
    /**
     * 应用润色结果
     * @param dto 应用润色请求
     */
    @Transactional
    public void applyPolish(ApplyPolishDTO dto) {
        // 获取简历
        Resume resume = getById(dto.getResumeId());
        checkResumeOwnership(resume);
        
        // 应用润色结果
        if (dto.getPolishedContent() != null) {
            try {
                // 将原始简历内容解析为对象
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, Object> originalContent = objectMapper.readValue(
                    resume.getResumeContent(), Map.class);
                
                // 获取润色后的内容
                Object polishedContentObj = dto.getPolishedContent();
                
                // 只更新指定部分
                if (dto.getTargetPart() != null && !dto.getTargetPart().isEmpty()) {
                    Map<String, Object> polishedMap;
                    if (polishedContentObj instanceof Map) {
                        // 如果已经是Map类型，直接使用
                        polishedMap = (Map<String, Object>) polishedContentObj;
                    } else {
                        // 否则尝试将其解析为Map
                        try {
                            polishedMap = objectMapper.readValue(
                                objectMapper.writeValueAsString(polishedContentObj), Map.class);
                        } catch (Exception e) {
                            throw new ServiceException("润色内容格式错误");
                        }
                    }
                    
                    // 更新指定部分
                    String part = dto.getTargetPart();
                    if (polishedMap.containsKey(part)) {
                        // 只合并description
                        if (part.equals("selfEvaluation")) {
                            originalContent.put(part, polishedMap.get(part));
                        } else {
                            List<Map<String, Object>> originalList = (List<Map<String, Object>>) originalContent.get(part);
                            List<Map<String, Object>> polishedList = (List<Map<String, Object>>) polishedMap.get(part);
                            
                            // 确保两个列表长度相同
                            if (originalList != null && polishedList != null && originalList.size() == polishedList.size()) {
                                for (int i = 0; i < originalList.size(); i++) {
                                    // 仅更新description字段
                                    if (polishedList.get(i).containsKey("description")) {
                                        originalList.get(i).put("description", polishedList.get(i).get("description"));
                                    }
                                }
                            } else {
                                log.warn("原始列表与润色列表长度不一致，无法更新");
                            }
                        }
                    } else {
                        log.warn("润色内容中未找到目标部分: {}", part);
                    }
                }
                
                // 将合并后的内容转换回JSON字符串
                resume.setResumeContent(objectMapper.writeValueAsString(originalContent));
                resume.setUpdateTime(LocalDateTime.now());
                resumeMapper.updateById(resume);
                
                log.info("成功应用润色结果到简历: {}", resume.getId());
            } catch (Exception e) {
                log.error("应用润色结果失败", e);
                throw new ServiceException("应用润色结果失败: " + e.getMessage());
            }
        } else {
            log.warn("没有提供润色内容，无法应用");
        }
    }

    public Page<Resume> adminPage(Integer currentPage, Integer size, String keyword) {
        Page<Resume> page = new Page<>(currentPage, size);
        LambdaQueryWrapper<Resume> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(keyword)) {
            // 搜索匹配姓名的用户
            LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
            userWrapper.like(User::getName, keyword);
            List<User> users = userMapper.selectList(userWrapper);
            List<Long> userIds = users.stream().map(User::getId).collect(Collectors.toList());
            if (!userIds.isEmpty()) {
                // 搜索匹配的学生
                LambdaQueryWrapper<Student> studentWrapper = new LambdaQueryWrapper<>();
                studentWrapper.in(Student::getUserId, userIds);
                List<Student> students = studentMapper.selectList(studentWrapper);
                List<Long> studentIds = students.stream().map(Student::getId).collect(Collectors.toList());
                if (!studentIds.isEmpty()) {
                    queryWrapper.or().in(Resume::getStudentId, studentIds);
                }
            }
            queryWrapper.or().like(Resume::getResumeName, keyword);
        }
        queryWrapper.orderByDesc(Resume::getUpdateTime);
        Page<Resume> resultPage = resumeMapper.selectPage(page, queryWrapper);
        // 填充学生姓名
        for (Resume resume : resultPage.getRecords()) {
            Student student = studentMapper.selectById(resume.getStudentId());
            if (student != null) {
                User user = userMapper.selectById(student.getUserId());
                resume.setStudentName(user != null ? user.getName() : "未知");
            }
        }
        return resultPage;
    }
    
    // 辅助方法：重置所有简历为非默认状态
    private void resetDefaultResume(Long studentId) {
        LambdaQueryWrapper<Resume> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Resume::getStudentId, studentId)
                    .eq(Resume::getIsDefault, true);
        
        List<Resume> defaultResumes = resumeMapper.selectList(queryWrapper);
        for (Resume resume : defaultResumes) {
            resume.setIsDefault(false);
            resume.setUpdateTime(LocalDateTime.now());
            resumeMapper.updateById(resume);
        }
    }
    
    // 辅助方法：设置新的默认简历
    private void setNewDefaultResume(Long studentId) {
        LambdaQueryWrapper<Resume> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Resume::getStudentId, studentId)
                    .orderByDesc(Resume::getUpdateTime);
        
        Resume resume = resumeMapper.selectOne(queryWrapper);
        if (resume != null) {
            resume.setIsDefault(true);
            resume.setUpdateTime(LocalDateTime.now());
            resumeMapper.updateById(resume);
        }
    }
    
    // 辅助方法：检查简历所有权
    private void checkResumeOwnership(Resume resume) {

        User currentUser=JwtTokenUtils.getCurrentUser();
        if(currentUser==null){
            throw new ServiceException("用户未登录");
        }
        // 检查是否是学生角色
        if (!"STUDENT".equals(currentUser.getRoleCode())&&!"ADMIN".equals(currentUser.getRoleCode())) {
            throw new ServiceException("非学生或管理员用户不能操作简历");
        }
        if("STUDENT".equals(currentUser.getRoleCode())){
        Student student = getCurrentStudent();
        if (!("STUDENT".equals(currentUser.getRoleCode())&&resume.getStudentId().equals(student.getId()))) {
            throw new ServiceException("无权操作此简历:");
        }
        }
    }
    
    // 辅助方法：获取当前登录学生
    private Student getCurrentStudent() {
        // 获取当前登录用户
        User currentUser = JwtTokenUtils.getCurrentUser();


        
        // 获取学生信息
        LambdaQueryWrapper<Student> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Student::getUserId, currentUser.getId());
        Student student = studentMapper.selectOne(queryWrapper);
        
        if (student == null) {
            throw new ServiceException("学生信息不存在");
        }
        
        return student;
    }
    
    /**
     * 构建提示词
     */
    private String buildPrompt(Resume resume,String content, String targetPart, String styleType, String targetPosition, String customPrompt) {
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("你是一位专业的简历优化顾问，请对以下简历内容进行简洁优化和润色，使其更加专业、精炼和有竞争力。并且所用语言必须和原文一致，比如原文是中文，你的输出也必须是中文\n\n");
        promptBuilder.append("简历的原始内容是：").append(resume.getResumeContent()).append("\n");
        // 添加样式类型指导
        if ("professional".equals(styleType)) {
            promptBuilder.append("请使用专业、正式的语言风格，突出求职者的专业能力和成就。\n");
        } else if ("technical".equals(styleType)) {
            promptBuilder.append("请使用技术导向的语言风格，突出求职者的技术能力和专业知识。\n");
        } else if ("management".equals(styleType)) {
            promptBuilder.append("请使用管理导向的语言风格，突出求职者的领导能力、团队协作和项目管理经验。\n");
        } else if ("creative".equals(styleType)) {
            promptBuilder.append("请使用创意导向的语言风格，突出求职者的创新思维和解决问题的能力。\n");
        }
        
        // 添加目标职位
        if (targetPosition != null && !targetPosition.isEmpty()) {
            promptBuilder.append("目标职位是: ").append(targetPosition).append("，请针对这个职位进行优化，添加相关的关键词和技能描述。\n");
        }
        
        // 添加自定义提示
        if (customPrompt != null && !customPrompt.isEmpty()) {
            promptBuilder.append("额外要求: ").append(customPrompt).append("\n\n");
        }

        // 根据不同部分添加特定指导
        switch (targetPart) {
            case "selfEvaluation" -> {
                promptBuilder.append("【自我评价润色指南】\n");
                promptBuilder.append("1. 避免过于主观或空洞的表述，使用具体事例和成就支撑观点\n");
                promptBuilder.append("2. 强调与目标职位最相关的个人特质和能力\n");
                promptBuilder.append("请润色以下自我评价内容:\n").append(content).append("\n");
                promptBuilder.append("输出结构参数说明:\n");
                promptBuilder.append("1.polishedContent，优化后的自我评价内容，原内容为：").append(content).append("\n");
                promptBuilder.append("2.overallEvaluation，总体评价，对你所优化的内容做整体的评价\n");
                promptBuilder.append("3.suggestion，修改建议，对简历修改提出一定的建议\n\n");
            }
            case "workExperience" -> {
                promptBuilder.append("【工作经历润色指南】\n");
                promptBuilder.append("1. 使用STAR法则（情境、任务、行动、结果）描述工作成就\n");
                promptBuilder.append("2. 突出可量化的成就和业绩，如增长率、效率提升等\n");
                promptBuilder.append("3. 使用有力的行动动词开头，如'领导'、'开发'、'优化'等\n");
                promptBuilder.append("4. 保持日期、公司名称等事实信息不变，只优化description部分\n");
                promptBuilder.append("5. 强调与目标职位最相关的工作经历\n");
                promptBuilder.append("请润色以下工作经历每项的description内容:\n").append(content).append("\n");
                promptBuilder.append("输出结构参数说明:\n");
                promptBuilder.append("1.polishedContent，优化后的工作经历的内容，为数组结构，每一项均是WorkExperienceItem，其结构为：\n");
                promptBuilder.append("\t\tcompany:公司名称，保持不变\n");
                promptBuilder.append("\t\tposition:职位名称，保持不变\n");
                promptBuilder.append("\t\tstartDate:开始日期，保持不变\n");
                promptBuilder.append("\t\tendDate:结束日期，保持不变\n");
                promptBuilder.append("\t\tdescription:工作描述，这是需要优化的部分\n");
                promptBuilder.append("原内容为：").append(content).append("\n");
                promptBuilder.append("2.overallEvaluation，总体评价，对你所优化的内容做整体的评价，非空\n");
                promptBuilder.append("3.suggestion，修改建议，对简历修改提出一定的建议，非空\n");
            }
            case "projectExperience" -> {
                promptBuilder.append("【项目经历润色指南】\n");
                promptBuilder.append("1. 清晰描述项目背景、目标、自身角色和贡献\n");
                promptBuilder.append("2. 强调项目成果和影响，特别是量化指标\n");
                promptBuilder.append("3. 突出使用的技术和解决的问题\n");
                promptBuilder.append("4. 保持项目名称、时间等事实信息不变，只优化description部分\n");
                promptBuilder.append("5. 强调与目标职位最相关的项目经历\n");
                promptBuilder.append("请润色以下项目经历内容:\n").append(content).append("\n\n");
                promptBuilder.append("输出结构参数说明:\n");
                promptBuilder.append("1.polishedContent，优化后的项目经历的内容，为数组结构，每一项均是ProjectExperienceItem，其结构为：\n");
                promptBuilder.append("\t\tprojectName:项目名称，保持不变\n");
                promptBuilder.append("\t\trole:担任角色，保持不变\n");
                promptBuilder.append("\t\tstartDate:开始日期，保持不变\n");
                promptBuilder.append("\t\tendDate:结束日期，保持不变\n");
                promptBuilder.append("\t\tdescription:项目描述，这是需要优化的部分\n");
                promptBuilder.append("原内容为：").append(content).append("\n");
                promptBuilder.append("2.overallEvaluation，总体评价，对你所优化的内容做整体的评价，非空\n");
                promptBuilder.append("3.suggestion，修改建议，对简历修改提出一定的建议，非空\n");
            }
            case "skills" -> {
                promptBuilder.append("【技能特长润色指南】\n");
                promptBuilder.append("1. 具体说明技能应用场景和熟练程度\n");
                promptBuilder.append("2. 与目标职位要求相关的技能应重点强调\n");
                promptBuilder.append("3. 可添加相关认证或成就作为佐证\n");
                promptBuilder.append("4. 保持技能名称和熟练度不变，只优化description部分\n");
                promptBuilder.append("5. 强调与目标职位最相关的技能\n");
                promptBuilder.append("请润色以下技能特长内容:\n").append(content).append("\n\n");
                promptBuilder.append("输出结构参数说明:\n");
                promptBuilder.append("1.polishedContent，优化后的技能特长的内容，为数组结构，每一项均是SkillItem，其结构为：\n");
                promptBuilder.append("\t\tname:技能名称，保持不变\n");
                promptBuilder.append("\t\tproficiency:熟练程度，保持不变\n");
                promptBuilder.append("\t\tdescription:项目描述，这是需要优化的部分\n");
                promptBuilder.append("原内容为：").append(content).append("\n");
                promptBuilder.append("2.overallEvaluation，总体评价，对你所优化的内容做整体的评价，非空\n");
                promptBuilder.append("3.suggestion，修改建议，对简历修改提出一定的建议，非空\n");

            }
            default -> {
                promptBuilder.append("请润色以下内容:\n").append(content).append("\n\n");
            }

        }
        promptBuilder.append("注意事项：\n");
        promptBuilder.append("1.结构参数每一项都必须有内容，不得为空 \n");
        promptBuilder.append("2.输出语言要和原语言保持一致 \n");

        return promptBuilder.toString();
    }
} 