package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.example.springboot.entity.Student;
import org.example.springboot.entity.User;
import org.example.springboot.exception.ServiceException;
import org.example.springboot.mapper.StudentMapper;
import org.example.springboot.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StudentService {
    @Resource
    private StudentMapper studentMapper;
    
    @Resource
    private UserMapper userMapper;
    
    /**
     * 根据用户ID获取学生信息
     * @param userId 用户ID
     * @return 学生信息
     */
    public Student getByUserId(Long userId) {
        Student student = studentMapper.selectOne(
            new LambdaQueryWrapper<Student>()
                .eq(Student::getUserId, userId)
        );
        
        if (student == null) {
            throw new ServiceException("未找到该用户的学生信息");
        }
        
        // 获取关联的用户信息
        fillUserInfo(student);
        
        return student;
    }
    
    /**
     * 创建学生信息
     * @param student 学生信息
     * @return 创建后的学生ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createStudent(Student student) {
        // 检查用户是否存在
        User user = userMapper.selectById(student.getUserId());
        if (user == null) {
            throw new ServiceException("关联的用户不存在");
        }
        
        // 检查学号是否已存在
        if (StringUtils.isNotBlank(student.getStudentNo()) && studentMapper.selectOne(
                new LambdaQueryWrapper<Student>()
                    .eq(Student::getStudentNo, student.getStudentNo())
            ) != null) {
            throw new ServiceException("学号已被使用");
        }
        
        // 检查用户是否已有学生信息
        if (studentMapper.existsByUserId(student.getUserId())) {
            throw new ServiceException("该用户已有学生信息");
        }
        
        if (studentMapper.insert(student) <= 0) {
            throw new ServiceException("创建学生信息失败");
        }
        
        return student.getId();
    }
    
    /**
     * 更新学生信息
     * @param id 学生ID
     * @param student 学生信息
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateStudent(Long id, Student student) {
        // 检查学生信息是否存在
        Student existStudent = studentMapper.selectById(id);
        if (existStudent == null) {
            throw new ServiceException("要更新的学生信息不存在");
        }
        
        // 检查学号是否与其他学生重复
        if (StringUtils.isNotBlank(student.getStudentNo())) {
            Student existByStudentNo = studentMapper.selectOne(
                new LambdaQueryWrapper<Student>()
                    .eq(Student::getStudentNo, student.getStudentNo())
            );
            if (existByStudentNo != null && !existByStudentNo.getId().equals(id)) {
                throw new ServiceException("学号已被其他学生使用");
            }
        }
        
        student.setId(id);
        if (studentMapper.updateById(student) <= 0) {
            throw new ServiceException("更新学生信息失败");
        }
    }
    
    /**
     * 删除学生信息
     * @param id 学生ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteStudent(Long id) {
        if (studentMapper.selectById(id) == null) {
            throw new ServiceException("要删除的学生信息不存在");
        }
        
        if (studentMapper.deleteById(id) <= 0) {
            throw new ServiceException("删除学生信息失败");
        }
    }
    
    /**
     * 根据ID获取学生信息
     * @param id 学生ID
     * @return 学生信息
     */
    public Student getById(Long id) {
        Student student = studentMapper.selectById(id);
        if (student == null) {
            throw new ServiceException("学生信息不存在");
        }
        fillUserInfo(student);

        return student;
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
    public Page<Student> getStudentsByPage(String studentNo, String college, String major, Integer currentPage, Integer size) {
        LambdaQueryWrapper<Student> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加查询条件
        if (StringUtils.isNotBlank(studentNo)) {
            queryWrapper.like(Student::getStudentNo, studentNo);
        }
        
        if (StringUtils.isNotBlank(college)) {
            queryWrapper.like(Student::getCollege, college);
        }
        
        if (StringUtils.isNotBlank(major)) {
            queryWrapper.like(Student::getMajor, major);
        }

        Page<Student> studentPage = studentMapper.selectPage(new Page<>(currentPage, size), queryWrapper);
        studentPage.getRecords().forEach(this::fillUserInfo);
        return studentPage;
    }


    void fillUserInfo(Student student) {
        // 获取关联的用户信息
        User user = userMapper.selectById(student.getUserId());
        if (user != null) {
            student.setUser(user);
        }

    }
    /**
     * 检查用户ID是否存在关联的学生信息
     * @param userId 用户ID
     * @return 存在返回true，不存在返回false
     */
    public boolean existsByUserId(Long userId) {
        return studentMapper.existsByUserId(userId);
    }
    
    /**
     * 根据关键词查找学生ID列表
     * 支持按学号、姓名进行模糊查询
     * @param keyword 关键词（学号或姓名）
     * @return 学生ID列表
     */
    public List<Long> getStudentIdsByKeyword(String keyword) {
        if (StringUtils.isBlank(keyword)) {
            return new ArrayList<>();
        }
        
        // 查询符合学号条件的学生
        LambdaQueryWrapper<Student> studentNoQuery = new LambdaQueryWrapper<>();
        studentNoQuery.like(Student::getStudentNo, keyword)
                     .select(Student::getId);
        List<Student> studentsByNo = studentMapper.selectList(studentNoQuery);
        
        // 获取符合姓名条件的用户ID
        List<Long> userIds = userMapper.getUserIdsByNameKeyword(keyword);
        
        // 查询这些用户ID关联的学生
        if (!userIds.isEmpty()) {
            LambdaQueryWrapper<Student> userIdQuery = new LambdaQueryWrapper<>();
            userIdQuery.in(Student::getUserId, userIds)
                      .select(Student::getId);
            List<Student> studentsByName = studentMapper.selectList(userIdQuery);
            
            // 合并两个查询结果
            studentsByNo.addAll(studentsByName);
        }
        
        // 提取学生ID并去重
        return studentsByNo.stream()
                .map(Student::getId)
                .distinct()
                .collect(Collectors.toList());
    }
} 