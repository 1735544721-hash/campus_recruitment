package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;

import org.example.springboot.entity.User;
import org.example.springboot.DTO.UserPasswordUpdateDTO;
import org.example.springboot.enumClass.AccountStatus;
import org.example.springboot.exception.ServiceException;
import org.example.springboot.mapper.CompanyMapper;
import org.example.springboot.mapper.StudentMapper;
import org.example.springboot.mapper.UserMapper;
import org.example.springboot.util.JwtTokenUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class UserService {
    @Resource
    private UserMapper userMapper;
    
    @Resource
    private StudentMapper studentMapper;
    
    @Resource
    private CompanyMapper companyMapper;
    
    // 定义有效的角色代码集合
    private static final Set<String> VALID_ROLE_CODES = new HashSet<>(
            Arrays.asList("STUDENT", "COMPANY", "ADMIN")
    );
    
    @Value("${user.defaultPassword}")
    private String DEFAULT_PWD;

    @Resource
    private PasswordEncoder bCryptPasswordEncoder;

    public User getByEmail(String email) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, email));
        if (user == null) {
            throw new ServiceException("邮箱不存在");
        }
        return user;
    }

    public User login(User user) {
        User dbUser = getByUsername(user.getUsername());
        // 用户存在性检查已经在 getByUsername 中处理
        if (dbUser.getStatus().equals(AccountStatus.PENDING_REVIEW.getValue())) {
            throw new ServiceException("账号正在审核");
        }
        if (dbUser.getStatus().equals(AccountStatus.REVIEW_FAILED.getValue())) {
            throw new ServiceException("账号审核不通过，请修改个人信息");
        }
        if (!bCryptPasswordEncoder.matches(user.getPassword(), dbUser.getPassword())) {
            throw new ServiceException("用户名或密码错误");
        }
        

        String token = JwtTokenUtils.genToken(String.valueOf(dbUser.getId()), dbUser.getPassword());
        

        dbUser.setToken(token);
        return dbUser;
    }

    public List<User> getUserByRole(String roleCode) {
        // 验证角色代码是否有效
        validateRoleCode(roleCode);
        
        List<User> users = userMapper.selectList(
            new LambdaQueryWrapper<User>()
                .eq(User::getRoleCode, roleCode)
        );
        if (users.isEmpty()) {
            throw new ServiceException("未找到该角色的用户");
        }
        return users;
    }

    /**
     * 创建用户
     * @param user 用户信息
     */
    @Transactional(rollbackFor = Exception.class)
    public void createUser(User user) {
        // 检查用户名是否存在
        if (userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                    .eq(User::getUsername, user.getUsername())
            ) != null) {
            throw new ServiceException("用户名已存在");
        }
        
        // 检查邮箱是否被使用
        if (StringUtils.isNotBlank(user.getEmail()) && userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                    .eq(User::getEmail, user.getEmail())
            ) != null) {
            throw new ServiceException("邮箱已被使用");
        }
        
        // 检查手机号是否被使用
        if (StringUtils.isNotBlank(user.getPhone()) && userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                    .eq(User::getPhone, user.getPhone())
            ) != null) {
            throw new ServiceException("手机号已被使用");
        }

        // 验证角色代码
        validateRoleCode(user.getRoleCode());
        
        // 设置默认密码或加密用户提供的密码
        user.setPassword(StringUtils.isNotBlank(user.getPassword()) ? user.getPassword() : DEFAULT_PWD);
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        
        // 设置默认状态
        if (user.getStatus() == null) {
            user.setStatus(AccountStatus.NORMAL.getValue());
        }
        
        if (userMapper.insert(user) <= 0) {
            throw new ServiceException("用户创建失败");
        }
    }

    public void updateUser(Long id, User user) {
        // 检查用户是否存在
        if (userMapper.selectById(id) == null) {
            throw new ServiceException("要更新的用户不存在");
        }
        
        // 检查新用户名是否与其他用户重复
        if (user.getUsername() != null) {
            User existUser = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                    .eq(User::getUsername, user.getUsername())
            );
            if (existUser != null && !existUser.getId().equals(id)) {
                throw new ServiceException("新用户名已被使用");
            }
        }
        
        // 检查新邮箱是否与其他用户重复
        if (StringUtils.isNotBlank(user.getEmail())) {
            User existUser = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                    .eq(User::getEmail, user.getEmail())
            );
            if (existUser != null && !existUser.getId().equals(id)) {
                throw new ServiceException("新邮箱已被使用");
            }
        }
        
        // 检查新手机号是否与其他用户重复
        if (StringUtils.isNotBlank(user.getPhone())) {
            User existUser = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                    .eq(User::getPhone, user.getPhone())
            );
            if (existUser != null && !existUser.getId().equals(id)) {
                throw new ServiceException("新手机号已被使用");
            }
        }
        
        // 如果更新角色代码，验证角色代码是否有效
        if (StringUtils.isNotBlank(user.getRoleCode())) {
            validateRoleCode(user.getRoleCode());
        }
        
        user.setId(id);
        if (userMapper.updateById(user) <= 0) {
            throw new ServiceException("用户更新失败");
        }
    }

    public User getByUsername(String username) {
        User user = userMapper.selectOne(
            new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)
        );
        if (user == null) {
            throw new ServiceException("用户不存在");
        }
        return user;
    }

    public void deleteBatch(List<Integer> ids) {
        if (userMapper.deleteByIds(ids) <= 0) {
            throw new ServiceException("批量删除失败");
        }
    }

    public List<User> getUserList() {
        List<User> users = userMapper.selectList(new LambdaQueryWrapper<>());
        if (users.isEmpty()) {
            throw new ServiceException("未找到任何用户");
        }
        return users;
    }

    public User getUserById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new ServiceException("用户不存在");
        }
        return user;
    }

    public Page<User> getUsersByPage(String username,  String name, String roleCode, Integer currentPage, Integer size) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加查询条件
        if (StringUtils.isNotBlank(username)) {
            queryWrapper.like(User::getUsername, username);
        }

        if (StringUtils.isNotBlank(name)) {
            queryWrapper.like(User::getName, name);
        }
        if (StringUtils.isNotBlank(roleCode)) {
            queryWrapper.eq(User::getRoleCode, roleCode);
        }
        
        return userMapper.selectPage(new Page<>(currentPage, size), queryWrapper);
    }

    public void updatePassword(Long id, UserPasswordUpdateDTO update) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new ServiceException("用户不存在");
        }
        
        // 验证旧密码
        if (!bCryptPasswordEncoder.matches(update.getOldPassword(), user.getPassword())) {
            throw new ServiceException("原密码错误");
        }
        
        // 更新新密码
        user.setPassword(bCryptPasswordEncoder.encode(update.getNewPassword()));
        if (userMapper.updateById(user) <= 0) {
            throw new ServiceException("密码修改失败");
        }
    }

    public void forgetPassword(String email, String newPassword) {
        User user = userMapper.selectOne(
            new LambdaQueryWrapper<User>()
                .eq(User::getEmail, email)
        );
        if (user == null) {
            throw new ServiceException("邮箱不存在");
        }
        
        user.setPassword(bCryptPasswordEncoder.encode(newPassword));
        if (userMapper.updateById(user) <= 0) {
            throw new ServiceException("密码重置失败");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteUserById(Long id) {
        // 检查是否存在关联的学生或企业信息
        checkRelated(id);
        
        if (userMapper.deleteById(id) <= 0) {
            throw new ServiceException("删除失败");
        }
    }
    
    /**
     * 检查用户是否有关联的学生或企业信息
     * @param userId 用户ID
     */
    private void checkRelated(Long userId) {
        // 检查是否存在关联的学生信息
        if (studentMapper.existsByUserId(userId)) {
            throw new ServiceException("该用户存在关联的学生信息，请先删除学生信息");
        }
        
        // 检查是否存在关联的企业信息
        if (companyMapper.existsByUserId(userId)) {
            throw new ServiceException("该用户存在关联的企业信息，请先删除企业信息");
        }
    }
    
    /**
     * 验证角色代码是否有效
     * @param roleCode 角色代码
     */
    private void validateRoleCode(String roleCode) {
        if (StringUtils.isBlank(roleCode) || !VALID_ROLE_CODES.contains(roleCode)) {
            throw new ServiceException("无效的角色代码: " + roleCode);
        }
    }

    /**
     * 根据关键词搜索用户
     * @param keyword 搜索关键词
     * @return 用户列表
     */
    public List<User> searchUsers(String keyword) {
        if (StringUtils.isBlank(keyword)) {
            throw new ServiceException("搜索关键词不能为空");
        }
        
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(User::getUsername, keyword)
                .or()
                .like(User::getName, keyword)
                .or()
                .like(User::getEmail, keyword)
                .last("LIMIT 20"); // 限制返回结果数量
        
        return userMapper.selectList(queryWrapper);
    }
}
