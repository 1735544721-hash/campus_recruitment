package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.example.springboot.entity.Application;
import org.example.springboot.entity.Company;
import org.example.springboot.entity.Interview;
import org.example.springboot.entity.Position;
import org.example.springboot.entity.Student;
import org.example.springboot.entity.User;
import org.example.springboot.exception.ServiceException;
import org.example.springboot.mapper.ApplicationMapper;
import org.example.springboot.mapper.CompanyMapper;
import org.example.springboot.mapper.InterviewMapper;
import org.example.springboot.mapper.PositionMapper;
import org.example.springboot.mapper.StudentMapper;
import org.example.springboot.mapper.UserMapper;
import org.example.springboot.util.JwtTokenUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import org.example.springboot.DTO.MessageDTO;

@Service
public class InterviewService {
    
    private static final Logger log = LoggerFactory.getLogger(InterviewService.class);
    
    @Resource
    private InterviewMapper interviewMapper;
    
    @Resource
    private ApplicationMapper applicationMapper;
    
    @Resource
    private CompanyMapper companyMapper;
    
    @Resource
    private PositionMapper positionMapper;
    
    @Resource
    private StudentMapper studentMapper;
    
    @Resource
    private UserMapper userMapper;

    @Resource
    private MessageService messageService;
    

    @Transactional(rollbackFor = Exception.class)
    public Long createInterview(Interview interview) {
        // 检查投递记录是否存在
        Application application = applicationMapper.selectById(interview.getApplicationId());
        if (application == null) {
            throw new ServiceException("投递记录不存在");
        }
        
        // 检查当前用户是否有权限创建面试（必须是企业用户且是该职位所属企业）
        User currentUser = JwtTokenUtils.getCurrentUser();
        if (currentUser == null || !"COMPANY".equals(currentUser.getRoleCode())) {
            throw new ServiceException("无权操作");
        }
        
        // 获取职位信息
        Position position = positionMapper.selectById(application.getPositionId());
        if (position == null) {
            throw new ServiceException("职位不存在");
        }
        
        // 获取企业信息
        Company company = companyMapper.selectOne(new LambdaQueryWrapper<Company>()
                .eq(Company::getUserId, currentUser.getId()));
        if (company == null || !company.getId().equals(position.getCompanyId())) {
            throw new ServiceException("无权操作");
        }
        
        // 更新投递状态为"面试邀请"
        application.setStatus(3); // 3表示面试邀请
        application.setUpdateTime(LocalDateTime.now());
        applicationMapper.updateById(application);
        
        // 设置面试初始状态
        interview.setInterviewStatus(0); // 0表示待确认
        interview.setInterviewResult(0); // 0表示未知
        interview.setCreateTime(LocalDateTime.now());
        
        // 保存面试记录
        interviewMapper.insert(interview);
        
        log.info("创建面试邀请成功，面试ID：{}，投递ID：{}", interview.getId(), interview.getApplicationId());
        
        // 获取学生信息，发送面试邀请通知
        Student student = studentMapper.selectById(application.getStudentId());
        if (student != null && student.getUserId() != null) {
            // 构建面试通知
            MessageDTO messageDTO = new MessageDTO();
            messageDTO.setReceiverId(student.getUserId());
            messageDTO.setMessageType("INTERVIEW_NOTICE");
            
            // 构建通知标题和内容
            String interviewType = interview.getInterviewType() != null ? interview.getInterviewType() : "面试";
            String formattedDateTime = interview.getInterviewTime() != null ? 
                interview.getInterviewTime().toString().replace("T", " ") : "待定";
            
            messageDTO.setTitle(position.getPositionName() + "面试邀请");
            StringBuilder content = new StringBuilder();
            content.append("您好，您申请的")
                   .append(position.getPositionName())
                   .append("职位已通过简历筛选，")
                   .append(company.getCompanyName())
                   .append("诚邀您参加")
                   .append(interviewType)
                   .append("面试。\n");
            
            content.append("面试时间：").append(formattedDateTime).append("\n");
            
            if (interview.getInterviewAddress() != null && !interview.getInterviewAddress().isEmpty()) {
                content.append("面试地点：").append(interview.getInterviewAddress()).append("\n");
            }
            
            if (interview.getInterviewNote() != null && !interview.getInterviewNote().isEmpty()) {
                content.append("面试备注：").append(interview.getInterviewNote());
            }
            
            messageDTO.setContent(content.toString());
            
            // 发送通知
            messageService.sendMessage(messageDTO);
        }
        
        return interview.getId();
    }
    

    @Transactional(rollbackFor = Exception.class)
    public void confirmInterview(Long id) {
        // 获取面试信息
        Interview interview = interviewMapper.selectById(id);
        if (interview == null) {
            throw new ServiceException("面试记录不存在");
        }
        
        // 检查面试状态
        if (interview.getInterviewStatus() != 0) {
            throw new ServiceException("面试状态不正确，无法确认");
        }
        
        // 获取投递记录
        Application application = applicationMapper.selectById(interview.getApplicationId());
        if (application == null) {
            throw new ServiceException("投递记录不存在");
        }
        
        // 检查当前用户是否有权限确认面试（必须是学生用户且是该投递记录的学生）
        User currentUser = JwtTokenUtils.getCurrentUser();
        if (currentUser == null || !"STUDENT".equals(currentUser.getRoleCode())) {
            throw new ServiceException("无权操作");
        }
        
        Student student = studentMapper.selectOne(new LambdaQueryWrapper<Student>()
                .eq(Student::getUserId, currentUser.getId()));
        if (student == null || !student.getId().equals(application.getStudentId())) {
            throw new ServiceException("无权操作");
        }
        
        // 更新面试状态为"已确认"
        interview.setInterviewStatus(1);
        interview.setUpdateTime(LocalDateTime.now());
        interviewMapper.updateById(interview);
        
        // 获取职位和企业信息
        Position position = positionMapper.selectById(application.getPositionId());
        Company company = null;
        if (position != null) {
            company = companyMapper.selectById(position.getCompanyId());
        }
        
        // 发送确认通知给企业
        if (company != null && company.getUserId() != null && position != null) {
            // 构建面试确认通知
            MessageDTO messageDTO = new MessageDTO();
            messageDTO.setReceiverId(company.getUserId());
            messageDTO.setMessageType("INTERVIEW_NOTICE");
            
            String positionName = position.getPositionName();
            String studentName = currentUser.getName() != null ? currentUser.getName() : "学生";
            String formattedDateTime = interview.getInterviewTime() != null ? 
                interview.getInterviewTime().toString().replace("T", " ") : "待定";
            
            messageDTO.setTitle(studentName + "已确认" + positionName + "面试");
            
            StringBuilder content = new StringBuilder();
            content.append(studentName)
                   .append("已确认您安排的")
                   .append(positionName)
                   .append("职位面试邀请。\n");
            
            content.append("面试时间：").append(formattedDateTime).append("\n");
            
            if (interview.getInterviewAddress() != null && !interview.getInterviewAddress().isEmpty()) {
                content.append("面试地点：").append(interview.getInterviewAddress());
            }
            
            messageDTO.setContent(content.toString());
            
            // 发送通知
            messageService.sendMessage(messageDTO);
        }
        
        log.info("确认面试成功，面试ID：{}", id);
    }
    

    @Transactional(rollbackFor = Exception.class)
    public void completeInterview(Interview interview) {
        // 获取面试信息
        Interview existingInterview = interviewMapper.selectById(interview.getId());
        if (existingInterview == null) {
            throw new ServiceException("面试记录不存在");
        }
        
        // 检查面试状态
        if (existingInterview.getInterviewStatus() != 1) {
            throw new ServiceException("面试状态不正确，无法完成");
        }
        
        // 获取投递记录
        Application application = applicationMapper.selectById(existingInterview.getApplicationId());
        if (application == null) {
            throw new ServiceException("投递记录不存在");
        }
        
        // 检查当前用户是否有权限完成面试（必须是企业用户且是该职位所属企业）
        User currentUser = JwtTokenUtils.getCurrentUser();
        if (currentUser == null || !"COMPANY".equals(currentUser.getRoleCode())) {
            throw new ServiceException("无权操作");
        }
        
        // 获取职位信息
        Position position = positionMapper.selectById(application.getPositionId());
        if (position == null) {
            throw new ServiceException("职位不存在");
        }
        
        // 获取企业信息
        Company company = companyMapper.selectOne(new LambdaQueryWrapper<Company>()
                .eq(Company::getUserId, currentUser.getId()));
        if (company == null || !company.getId().equals(position.getCompanyId())) {
            throw new ServiceException("无权操作");
        }
        
        // 更新面试信息
        existingInterview.setInterviewStatus(2); // 2表示已完成
        existingInterview.setInterviewResult(interview.getInterviewResult());
        existingInterview.setFeedback(interview.getFeedback());
        existingInterview.setUpdateTime(LocalDateTime.now());
        interviewMapper.updateById(existingInterview);
        
        // 如果面试通过，更新投递状态为"已录用"；如果未通过，更新为"不合适"
        if (interview.getInterviewResult() == 1) {
            // 通过
            application.setStatus(5); // 5表示已录用
        } else if (interview.getInterviewResult() == 2) {
            // 未通过
            application.setStatus(4); // 4表示不合适
        }
        application.setUpdateTime(LocalDateTime.now());
        applicationMapper.updateById(application);
        
        // 获取学生信息，发送面试结果通知
        Student student = studentMapper.selectById(application.getStudentId());
        if (student != null && student.getUserId() != null) {
            // 构建面试结果通知
            MessageDTO messageDTO = new MessageDTO();
            messageDTO.setReceiverId(student.getUserId());
            messageDTO.setMessageType("INTERVIEW_NOTICE");
            
            // 根据面试结果设置不同的标题和内容
            String resultText = interview.getInterviewResult() == 1 ? "通过" : "未通过";
            
            messageDTO.setTitle(position.getPositionName() + "面试结果：" + resultText);
            
            StringBuilder content = new StringBuilder();
            content.append("您好，您参加的")
                   .append(company.getCompanyName())
                   .append("公司")
                   .append(position.getPositionName())
                   .append("职位面试")
                   .append(resultText)
                   .append("。\n");
            
            if (interview.getFeedback() != null && !interview.getFeedback().isEmpty()) {
                content.append("面试反馈：").append(interview.getFeedback());
            }
            
            messageDTO.setContent(content.toString());
            
            // 发送通知
            messageService.sendMessage(messageDTO);
        }
        
        log.info("完成面试成功，面试ID：{}", interview.getId());
    }
    

    @Transactional(rollbackFor = Exception.class)
    public void cancelInterview(Long id) {
        // 获取面试信息
        Interview interview = interviewMapper.selectById(id);
        if (interview == null) {
            throw new ServiceException("面试记录不存在");
        }
        
        // 检查面试状态
        if (interview.getInterviewStatus() == 2 || interview.getInterviewStatus() == 3) {
            throw new ServiceException("面试已完成或已取消，无法取消");
        }
        
        // 获取投递记录
        Application application = applicationMapper.selectById(interview.getApplicationId());
        if (application == null) {
            throw new ServiceException("投递记录不存在");
        }
        
        // 检查当前用户权限
        User currentUser = JwtTokenUtils.getCurrentUser();
        if (currentUser == null) {
            throw new ServiceException("用户未登录");
        }
        
        // 企业用户和学生用户都可以取消面试，但需要验证身份
        if ("COMPANY".equals(currentUser.getRoleCode())) {
            // 获取职位信息
            Position position = positionMapper.selectById(application.getPositionId());
            if (position == null) {
                throw new ServiceException("职位不存在");
            }
            
            // 获取企业信息
            Company company = companyMapper.selectOne(new LambdaQueryWrapper<Company>()
                    .eq(Company::getUserId, currentUser.getId()));
            if (company == null || !company.getId().equals(position.getCompanyId())) {
                throw new ServiceException("无权操作");
            }
        } else if ("STUDENT".equals(currentUser.getRoleCode())) {
            // 检查学生身份
            Student student = studentMapper.selectOne(new LambdaQueryWrapper<Student>()
                    .eq(Student::getUserId, currentUser.getId()));
            if (student == null || !student.getId().equals(application.getStudentId())) {
                throw new ServiceException("无权操作");
            }
        } else {
            throw new ServiceException("无权操作");
        }
        
        // 更新面试状态为"已取消"
        interview.setInterviewStatus(3); // 3表示已取消
        interview.setUpdateTime(LocalDateTime.now());
        interviewMapper.updateById(interview);
        
        // 更新投递状态回到"通过筛选"
        application.setStatus(2); // 2表示通过筛选
        application.setUpdateTime(LocalDateTime.now());
        applicationMapper.updateById(application);
        
        // 发送取消通知
        // 如果是企业取消，通知学生；如果是学生取消，通知企业
        if ("COMPANY".equals(currentUser.getRoleCode())) {
            // 获取学生信息，发送通知
            Student student = studentMapper.selectById(application.getStudentId());
            if (student != null && student.getUserId() != null) {
                // 获取职位和企业信息
                Position position = positionMapper.selectById(application.getPositionId());
                Company company = null;
                if (position != null) {
                    company = companyMapper.selectById(position.getCompanyId());
                }
                
                if (position != null) {
                    String companyName = company != null ? company.getCompanyName() : "企业";
                    
                    // 构建面试取消通知
                    MessageDTO messageDTO = new MessageDTO();
                    messageDTO.setReceiverId(student.getUserId());
                    messageDTO.setMessageType("INTERVIEW_NOTICE");
                    messageDTO.setTitle(position.getPositionName() + "面试已取消");
                    
                    StringBuilder content = new StringBuilder();
                    content.append("很抱歉，")
                           .append(companyName)
                           .append("已取消了您申请的")
                           .append(position.getPositionName())
                           .append("职位的面试。\n");
                    
                    content.append("取消时间：").append(LocalDateTime.now().toString().replace("T", " "));
                    
                    messageDTO.setContent(content.toString());
                    
                    // 发送通知
                    messageService.sendMessage(messageDTO);
                }
            }
        } else if ("STUDENT".equals(currentUser.getRoleCode())) {
            // 获取职位和企业信息，发送通知给企业
            Position position = positionMapper.selectById(application.getPositionId());
            Company company = null;
            if (position != null) {
                company = companyMapper.selectById(position.getCompanyId());
            }
            
            if (company != null && company.getUserId() != null && position != null) {
                String studentName = currentUser.getName() != null ? currentUser.getName() : "学生";
                
                // 构建面试取消通知
                MessageDTO messageDTO = new MessageDTO();
                messageDTO.setReceiverId(company.getUserId());
                messageDTO.setMessageType("INTERVIEW_NOTICE");
                messageDTO.setTitle(studentName + "已取消" + position.getPositionName() + "面试");
                
                StringBuilder content = new StringBuilder();
                content.append(studentName)
                       .append("已取消您安排的")
                       .append(position.getPositionName())
                       .append("职位面试。\n");
                
                content.append("取消时间：").append(LocalDateTime.now().toString().replace("T", " "));
                
                messageDTO.setContent(content.toString());
                
                // 发送通知
                messageService.sendMessage(messageDTO);
            }
        }
        
        log.info("取消面试成功，面试ID：{}", id);
    }
    

    public Interview getInterviewById(Long id) {
        Interview interview = interviewMapper.selectById(id);
        if (interview == null) {
            return null;
        }
        
        // 填充关联信息
        fillInterviewInfo(interview);
        
        return interview;
    }
    

    public Page<Interview> pageInterviews(Long companyId, Integer status, Integer currentPage, Integer size) {
        Page<Interview> page = new Page<>(currentPage, size);
        
        // 查询该企业下的所有职位ID
        LambdaQueryWrapper<Position> positionQuery = new LambdaQueryWrapper<>();
        if(companyId != null) {
            positionQuery.eq(Position::getCompanyId, companyId);
        }

        positionQuery.select(Position::getId);
        
        // 获取职位ID列表
        var positionIds = positionMapper.selectList(positionQuery)
                .stream()
                .map(Position::getId)
                .toList();
        
        if (positionIds.isEmpty()) {
            return new Page<>();
        }
        
        // 查询这些职位下的所有投递ID
        LambdaQueryWrapper<Application> applicationQuery = new LambdaQueryWrapper<>();
        applicationQuery.in(Application::getPositionId, positionIds);
        applicationQuery.select(Application::getId);
        
        // 获取投递ID列表
        var applicationIds = applicationMapper.selectList(applicationQuery)
                .stream()
                .map(Application::getId)
                .toList();
        
        if (applicationIds.isEmpty()) {
            return new Page<>();
        }
        
        // 查询面试记录
        LambdaQueryWrapper<Interview> interviewQuery = new LambdaQueryWrapper<>();
        interviewQuery.in(Interview::getApplicationId, applicationIds);
        
        // 根据状态筛选
        if (status != null) {
            interviewQuery.eq(Interview::getInterviewStatus, status);
        }
        
        // 按面试时间降序排序
        interviewQuery.orderByDesc(Interview::getInterviewTime);
        
        // 执行分页查询
        Page<Interview> result = interviewMapper.selectPage(page, interviewQuery);
        
        // 填充关联信息
        result.getRecords().forEach(this::fillInterviewInfo);
        
        return result;
    }
    

    public Page<Interview> pageStudentInterviews(Long studentId, Integer status, Integer currentPage, Integer size) {
        Page<Interview> page = new Page<>(currentPage, size);
        
        // 查询该学生的所有投递ID
        LambdaQueryWrapper<Application> applicationQuery = new LambdaQueryWrapper<>();
        applicationQuery.eq(Application::getStudentId, studentId);
        applicationQuery.select(Application::getId);
        
        // 获取投递ID列表
        var applicationIds = applicationMapper.selectList(applicationQuery)
                .stream()
                .map(Application::getId)
                .toList();
        
        if (applicationIds.isEmpty()) {
            return new Page<>();
        }
        
        // 查询面试记录
        LambdaQueryWrapper<Interview> interviewQuery = new LambdaQueryWrapper<>();
        interviewQuery.in(Interview::getApplicationId, applicationIds);
        
        // 根据状态筛选
        if (status != null) {
            interviewQuery.eq(Interview::getInterviewStatus, status);
        }
        
        // 按面试时间降序排序
        interviewQuery.orderByDesc(Interview::getInterviewTime);
        
        // 执行分页查询
        Page<Interview> result = interviewMapper.selectPage(page, interviewQuery);
        
        // 填充关联信息
        result.getRecords().forEach(this::fillInterviewInfo);
        
        return result;
    }
    
    /**
     * 填充面试关联信息
     * @param interview 面试信息
     */
    private void fillInterviewInfo(Interview interview) {
        // 获取投递记录
        Application application = applicationMapper.selectById(interview.getApplicationId());
        if (application != null) {
            interview.setApplication(application);
            
            // 获取学生信息
            Student student = studentMapper.selectById(application.getStudentId());
            if (student != null) {
                User studentUser = userMapper.selectById(student.getUserId());
                if (studentUser != null) {
                    interview.setStudentName(studentUser.getName());
                }
            }
            
            // 获取职位信息
            Position position = positionMapper.selectById(application.getPositionId());
            if (position != null) {
                interview.setPositionName(position.getPositionName());
                
                // 获取企业信息
                Company company = companyMapper.selectById(position.getCompanyId());
                if (company != null) {
                    interview.setCompanyName(company.getCompanyName());
                }
            }
        }
    }
} 