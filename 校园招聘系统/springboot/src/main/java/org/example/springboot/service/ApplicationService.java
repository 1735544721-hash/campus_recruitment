package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.example.springboot.DTO.ApplicationDTO;
import org.example.springboot.entity.Application;
import org.example.springboot.entity.Position;
import org.example.springboot.entity.Resume;
import org.example.springboot.entity.Student;
import org.example.springboot.entity.User;
import org.example.springboot.exception.ServiceException;
import org.example.springboot.mapper.*;
import org.example.springboot.util.JwtTokenUtils;
import org.example.springboot.enumClass.ApplicationStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import org.example.springboot.DTO.MessageDTO;
import org.example.springboot.entity.Company;
import org.example.springboot.enumClass.MessageTypeEnum;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;

@Service
public class ApplicationService extends ServiceImpl<ApplicationMapper, Application> {
    
    @Resource
    private StudentService studentService;
    
    @Resource
    private PositionService positionService;
    
    @Resource
    private ResumeService resumeService;
    
    @Resource
    private ApplicationMapper applicationMapper;
    
    @Resource
    private PositionMapper positionMapper;

    @Resource
    private MessageService messageService;


    @Autowired
    private StudentMapper studentMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private CompanyService companyService;
    @Autowired
    private ResumeMapper resumeMapper;

    /**
     * 投递简历
     * @param dto 投递信息
     * @return 投递ID
     */
    @Transactional
    public Long apply(ApplicationDTO dto) {
        // 获取当前登录用户
        User currentUser = JwtTokenUtils.getCurrentUser();
        if (currentUser == null) {
            throw new ServiceException("用户未登录");
        }
        
        // 获取学生信息
        Student student = studentService.getByUserId(currentUser.getId());
        if (student == null) {
            throw new ServiceException("请先完善学生信息");
        }
        
        // 获取职位信息
        Position position = positionMapper.selectById(dto.getPositionId());
        if (position == null) {
            throw new ServiceException("职位不存在");
        }
        
        // 检查职位是否已下线
        if (position.getStatus() != 1) {
            throw new ServiceException("该职位已下线或正在审核中，无法投递");
        }
        
        // 检查职位是否已过期
        if (position.getDeadline() != null && position.getDeadline().isBefore(LocalDate.now())) {
            throw new ServiceException("该职位已过期，无法投递");
        }
        
        // 获取简历信息
        Resume resume = resumeService.getById(dto.getResumeId());
        if (resume == null) {
            throw new ServiceException("简历不存在");
        }
        
        // 检查简历是否属于当前学生
        if (!resume.getStudentId().equals(student.getId())) {
            throw new ServiceException("无权使用此简历");
        }
        
        // 检查是否已经投递过该职位
        LambdaQueryWrapper<Application> existQuery = new LambdaQueryWrapper<>();
        existQuery.eq(Application::getStudentId, student.getId())
                 .eq(Application::getPositionId, position.getId());
        long count = count(existQuery);
        if (count > 0) {
            throw new ServiceException("您已经投递过该职位，请勿重复投递");
        }
        
        // 创建投递记录
        Application application = new Application();
        application.setStudentId(student.getId());
        application.setPositionId(position.getId());
        application.setResumeId(resume.getId());
        application.setStatus(ApplicationStatusEnum.PENDING.getCode());
        application.setCreateTime(LocalDateTime.now());
        
        // 保存投递记录
        save(application);
        
        // 获取企业信息并发送通知
        Company company = positionService.getCompanyByPositionId(position.getId());
        if (company != null && company.getUserId() != null) {
            // 向企业发送简历投递通知
            MessageDTO messageDTO = new MessageDTO();
            messageDTO.setReceiverId(company.getUserId());
            messageDTO.setMessageType(MessageTypeEnum.RESUME_STATUS.getCode());
            messageDTO.setTitle("新简历投递通知");
            messageDTO.setContent("您发布的职位 " + position.getPositionName() + " 收到了来自" + currentUser.getName() + "的简历投递，请及时查看。");
            messageService.sendMessage(messageDTO);
        }
        
        return application.getId();
    }
    
    /**
     * 分页查询学生的投递记录
     * @param currentPage 当前页
     * @param size 每页大小
     * @param status 状态
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 分页结果
     */
    public Page<Application> pageStudentApplications(Integer currentPage, Integer size, Integer status, String startDate, String endDate) {
        // 获取当前登录用户
        User currentUser = JwtTokenUtils.getCurrentUser();
        if (currentUser == null) {
            throw new ServiceException("用户未登录");
        }
        
        // 获取学生信息
        Student student = studentService.getByUserId(currentUser.getId());
        if (student == null) {
            throw new ServiceException("请先完善学生信息");
        }
        
        // 创建分页对象
        Page<Application> page = new Page<>(currentPage, size);
        
        // 构建查询条件
        LambdaQueryWrapper<Application> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Application::getStudentId, student.getId());
        
        // 添加状态筛选条件
        if (status != null) {
            queryWrapper.eq(Application::getStatus, status);
        }
        
        // 添加日期筛选条件
        if (StringUtils.isNotBlank(startDate)) {
            queryWrapper.ge(Application::getCreateTime, startDate + " 00:00:00");
        }
        
        if (StringUtils.isNotBlank(endDate)) {
            queryWrapper.le(Application::getCreateTime, endDate + " 23:59:59");
        }
        
        // 按创建时间降序排序
        queryWrapper.orderByDesc(Application::getCreateTime);
        
        // 执行查询
        Page<Application> result = page(page, queryWrapper);
        
        // 填充关联信息
        fillApplicationInfo(result.getRecords());
        
        return result;
    }
    
    /**
     * 分页查询企业收到的投递记录
     * @param currentPage 当前页
     * @param size 每页大小
     * @param status 状态
     * @param positionId 职位ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param keyword 学生关键词
     * @return 分页结果
     */
    public Page<Application> pageCompanyApplications(Integer currentPage, Integer size, Integer status, Long positionId, String startDate, String endDate, String keyword) {
        // 获取当前登录用户关联的企业ID
        User currentUser = JwtTokenUtils.getCurrentUser();
        if(currentUser == null) {
            throw new ServiceException("用户未登录");
        }
        Long companyId=null;
        if(currentUser.getRoleCode().equals("COMPANY")) {
            companyId = positionService.getCurrentUserCompanyId();
        }

        
        // 创建分页对象
        Page<Application> page = new Page<>(currentPage, size);
        
        // 查询该企业所有职位ID
        LambdaQueryWrapper<Position> positionQuery = new LambdaQueryWrapper<>();
        // 构建查询条件
        LambdaQueryWrapper<Application> queryWrapper = new LambdaQueryWrapper<>();

        if(companyId!=null){
            positionQuery.eq(Position::getCompanyId, companyId).eq(Position::getStatus, 1);//上线职位
            List<Long> ids = positionMapper.selectList(positionQuery).stream().map(Position::getId).toList();
            if(!ids.isEmpty()) {
                queryWrapper.in(Application::getPositionId,ids);
            }else{
                return new Page<>(currentPage, size);
            }
        }

        

        
        // 添加状态筛选条件
        if (status != null) {
            queryWrapper.eq(Application::getStatus, status);
        }
        
        // 添加日期筛选条件
        if (StringUtils.isNotBlank(startDate)) {
            queryWrapper.ge(Application::getCreateTime, startDate + " 00:00:00");
        }
        
        if (StringUtils.isNotBlank(endDate)) {
            queryWrapper.le(Application::getCreateTime, endDate + " 23:59:59");
        }
        
        // 关键词搜索处理
        if (StringUtils.isNotBlank(keyword)) {
            // 先获取符合条件的学生ID
            List<Long> studentIds = studentService.getStudentIdsByKeyword(keyword);
            if (!studentIds.isEmpty()) {
                queryWrapper.in(Application::getStudentId, studentIds);
            } else {
                // 如果没有匹配的学生，则返回空结果
                return new Page<>();
            }
        }
        
        // 按创建时间降序排序
        queryWrapper.orderByDesc(Application::getCreateTime);
        
        // 执行查询
        Page<Application> result = page(page, queryWrapper);
        
        // 填充关联信息
        fillApplicationInfo(result.getRecords());
        
        return result;
    }
    
    /**
     * 填充投递记录的关联信息
     * @param applications 投递记录列表
     */
    private void fillApplicationInfo(List<Application> applications) {
        if (applications == null || applications.isEmpty()) {
            return;
        }
        
        for (Application application : applications) {
            // 填充职位信息
            Position position = positionService.getById(application.getPositionId());
            if (position != null) {
                application.setPositionName(position.getPositionName());
            }
            
            // 填充学生信息
            Student student = getStudentWithUser(application.getStudentId());

            if (student != null) {
                application.setStudentName(student.getUser().getName());
                application.setStudentNo(student.getStudentNo());
            }
            
            // 填充简历信息
            Resume resume = resumeMapper.selectById(application.getResumeId());
            if (resume != null) {
                application.setResumeName(resume.getResumeName());
            }
        }
    }
    
    /**
     * 更新投递状态
     * @param id 投递ID
     * @param status 新状态
     */
    @Transactional
    public void updateStatus(Long id, Integer status) {
        Application application = getById(id);
        if (application == null) {
            throw new ServiceException("投递记录不存在");
        }
        
        // 检查企业是否有权限操作
        Long companyId = positionService.getCurrentUserCompanyId();
        Position position = positionService.getById(application.getPositionId());
        if (position == null || !position.getCompanyId().equals(companyId)) {
            throw new ServiceException("无权操作此投递记录");
        }
        
        // 检查状态是否合法
        if (!ApplicationStatusEnum.isValidStatus(status)) {
            throw new ServiceException("无效的状态值");
        }
        
        // 更新状态
        application.setStatus(status);
        application.setUpdateTime(LocalDateTime.now());
        updateById(application);
        
        // 获取学生用户ID，发送通知
        Student student = studentService.getById(application.getStudentId());
        if (student != null && student.getUserId() != null) {
            // 获取状态名称
            String statusName = ApplicationStatusEnum.getNameByCode(status);
            
            // 获取职位名称
            String positionName = position.getPositionName();
            
            // 获取企业名称
            Company company = positionService.getCompanyById(position.getCompanyId());
            String companyName = company != null ? company.getCompanyName() : "企业";
            
            // 构建通知内容
            String title = "简历投递状态更新";
            String content = "您投递的 " + positionName + " 职位简历状态已更新为 " + statusName;
            
            // 如果是面试邀请，添加提示
            if (status.equals(ApplicationStatusEnum.INTERVIEW.getCode())) {
                content += "，请留意后续面试安排";
            }
            
            // 发送通知
            MessageDTO messageDTO = new MessageDTO();
            messageDTO.setReceiverId(student.getUserId());
            messageDTO.setMessageType(MessageTypeEnum.APPLICATION_STATUS.getCode());
            messageDTO.setTitle(title);
            messageDTO.setContent(content);
            messageService.sendMessage(messageDTO);
        }
    }
    
    /**
     * 获取投递统计信息
     * @return 统计信息
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> result = new HashMap<>();
        
        // 获取当前登录用户
        User currentUser = JwtTokenUtils.getCurrentUser();
        if (currentUser == null) {
            throw new ServiceException("用户未登录");
        }
        
        if ("STUDENT".equals(currentUser.getRoleCode())) {
            // 学生用户统计
            Student student = studentService.getByUserId(currentUser.getId());
            if (student == null) {
                throw new ServiceException("请先完善学生信息");
            }
            
            // 查询各状态的投递数量
            LambdaQueryWrapper<Application> query = new LambdaQueryWrapper<>();
            query.eq(Application::getStudentId, student.getId());
            
            // 总投递数
            result.put("totalCount", count(query));
            
            // 待查看数
            query.clear();
            query.eq(Application::getStudentId, student.getId());
            query.eq(Application::getStatus, ApplicationStatusEnum.PENDING.getCode());
            result.put("pendingCount", count(query));
            
            // 已查看数
            query.clear();
            query.eq(Application::getStudentId, student.getId());
            query.eq(Application::getStatus, ApplicationStatusEnum.VIEWED.getCode());
            result.put("viewedCount", count(query));
            
            // 通过筛选数
            query.clear();
            query.eq(Application::getStudentId, student.getId());
            query.eq(Application::getStatus, ApplicationStatusEnum.PASSED.getCode());
            result.put("passedCount", count(query));
            
            // 面试邀请数
            query.clear();
            query.eq(Application::getStudentId, student.getId());
            query.eq(Application::getStatus, ApplicationStatusEnum.INTERVIEW.getCode());
            result.put("interviewCount", count(query));
            
            // 不合适数
            query.clear();
            query.eq(Application::getStudentId, student.getId());
            query.eq(Application::getStatus, ApplicationStatusEnum.REJECTED.getCode());
            result.put("rejectedCount", count(query));
            
            // 已录用数
            query.clear();
            query.eq(Application::getStudentId, student.getId());
            query.eq(Application::getStatus, ApplicationStatusEnum.OFFERED.getCode());
            result.put("offeredCount", count(query));
        } else if ("COMPANY".equals(currentUser.getRoleCode())) {
            // 企业用户统计
            Long companyId = positionService.getCurrentUserCompanyId();
            
            // 查询该企业所有职位ID
            LambdaQueryWrapper<Position> positionQuery = new LambdaQueryWrapper<>();
            positionQuery.eq(Position::getCompanyId, companyId)
                        .select(Position::getId);
            List<Position> positions = positionMapper.selectList(positionQuery);
            
            if (positions.isEmpty()) {
                // 没有职位，返回0统计
                result.put("totalCount", 0);
                result.put("pendingCount", 0);
                result.put("viewedCount", 0);
                result.put("passedCount", 0);
                result.put("interviewCount", 0);
                result.put("rejectedCount", 0);
                result.put("offeredCount", 0);
                return result;
            }
            
            // 提取职位ID列表
            List<Long> positionIds = positions.stream().map(Position::getId).toList();
            
            // 查询各状态的投递数量
            LambdaQueryWrapper<Application> query = new LambdaQueryWrapper<>();
            query.in(Application::getPositionId, positionIds);
            
            // 总投递数
            result.put("totalCount", count(query));
            
            // 待查看数
            query.clear();
            query.in(Application::getPositionId, positionIds);
            query.eq(Application::getStatus, ApplicationStatusEnum.PENDING.getCode());
            result.put("pendingCount", count(query));
            
            // 已查看数
            query.clear();
            query.in(Application::getPositionId, positionIds);
            query.eq(Application::getStatus, ApplicationStatusEnum.VIEWED.getCode());
            result.put("viewedCount", count(query));
            
            // 通过筛选数
            query.clear();
            query.in(Application::getPositionId, positionIds);
            query.eq(Application::getStatus, ApplicationStatusEnum.PASSED.getCode());
            result.put("passedCount", count(query));
            
            // 面试邀请数
            query.clear();
            query.in(Application::getPositionId, positionIds);
            query.eq(Application::getStatus, ApplicationStatusEnum.INTERVIEW.getCode());
            result.put("interviewCount", count(query));
            
            // 不合适数
            query.clear();
            query.in(Application::getPositionId, positionIds);
            query.eq(Application::getStatus, ApplicationStatusEnum.REJECTED.getCode());
            result.put("rejectedCount", count(query));
            
            // 已录用数
            query.clear();
            query.in(Application::getPositionId, positionIds);
            query.eq(Application::getStatus, ApplicationStatusEnum.OFFERED.getCode());
            result.put("offeredCount", count(query));
        }
        
        return result;
    }

    /**
     * 获取最近1年的申请趋势
     * @return 日期和申请数量的映射
     */
    public Map<String, Long> getRecentApplicationTrend() {
        Map<String, Long> trend = new HashMap<>();
        LocalDateTime oneYearAgo = LocalDateTime.now().minusYears(1);

        // 创建查询条件
        LambdaQueryWrapper<Application> query = new LambdaQueryWrapper<>();
        query.ge(Application::getCreateTime, oneYearAgo)
                .orderByAsc(Application::getCreateTime);

        List<Application> applications = this.list(query);

        // 初始化过去12个月的数据
        LocalDateTime now = LocalDateTime.now();
        for (int i = 11; i >= 0; i--) {
            LocalDateTime month = now.minusMonths(i);
            String monthKey = month.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            trend.put(monthKey, 0L);
        }

        // 统计每月申请数量
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        for (Application application : applications) {
            String monthKey = application.getCreateTime().format(formatter);
            trend.merge(monthKey, 1L, Long::sum);
        }

        return trend;
    }

    /**
     * 添加辅助方法，根据关键词查找学生ID列表
     * 此方法实现需要在StudentService中
     */


    Student getStudentWithUser(Long studentId){
        Student student = studentMapper.selectById(studentId);
        if(student != null&&student.getUserId()!=null){
            student.setUser(userMapper.selectById(student.getUserId()));
        }
        return student;
    }
} 