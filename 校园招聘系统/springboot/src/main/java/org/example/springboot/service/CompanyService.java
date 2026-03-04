package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.example.springboot.entity.Company;
import org.example.springboot.entity.User;
import org.example.springboot.exception.ServiceException;
import org.example.springboot.mapper.CompanyMapper;
import org.example.springboot.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompanyService {
    @Resource
    private CompanyMapper companyMapper;
    
    @Resource
    private UserMapper userMapper;
    
    /**
     * 根据用户ID获取企业信息
     * @param userId 用户ID
     * @return 企业信息
     */
    public Company getByUserId(Long userId) {
        Company company = companyMapper.selectOne(
            new LambdaQueryWrapper<Company>()
                .eq(Company::getUserId, userId)
        );
        
        if (company == null) {
            throw new ServiceException("未找到该用户的企业信息");
        }
        
        // 获取关联的用户信息
        User user = userMapper.selectById(userId);
        if (user != null) {
            company.setUser(user);
        }
        
        return company;
    }
    
    /**
     * 创建企业信息
     * @param company 企业信息
     * @return 创建后的企业ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createCompany(Company company) {
        // 检查用户是否存在
        User user = userMapper.selectById(company.getUserId());
        if (user == null) {
            throw new ServiceException("关联的用户不存在");
        }
        
        // 检查企业名称是否已存在
        if (StringUtils.isNotBlank(company.getCompanyName()) && companyMapper.selectOne(
                new LambdaQueryWrapper<Company>()
                    .eq(Company::getCompanyName, company.getCompanyName())
            ) != null) {
            throw new ServiceException("企业名称已被使用");
        }
        
        // 检查用户是否已有企业信息
        if (companyMapper.existsByUserId(company.getUserId())) {
            throw new ServiceException("该用户已有企业信息");
        }
        
        // 设置默认认证状态
        if (company.getVerified() == null) {
            company.setVerified(false);
        }
        
        if (companyMapper.insert(company) <= 0) {
            throw new ServiceException("创建企业信息失败");
        }
        
        return company.getId();
    }
    
    /**
     * 更新企业信息
     * @param id 企业ID
     * @param company 企业信息
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateCompany(Long id, Company company) {
        // 检查企业信息是否存在
        Company existCompany = companyMapper.selectById(id);
        if (existCompany == null) {
            throw new ServiceException("要更新的企业信息不存在");
        }
        
        // 检查企业名称是否与其他企业重复
        if (StringUtils.isNotBlank(company.getCompanyName())) {
            Company existByCompanyName = companyMapper.selectOne(
                new LambdaQueryWrapper<Company>()
                    .eq(Company::getCompanyName, company.getCompanyName())
            );
            if (existByCompanyName != null && !existByCompanyName.getId().equals(id)) {
                throw new ServiceException("企业名称已被其他企业使用");
            }
        }
        
        company.setId(id);
        if (companyMapper.updateById(company) <= 0) {
            throw new ServiceException("更新企业信息失败");
        }
    }
    
    /**
     * 删除企业信息
     * @param id 企业ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteCompany(Long id) {
        if (companyMapper.selectById(id) == null) {
            throw new ServiceException("要删除的企业信息不存在");
        }
        
        if (companyMapper.deleteById(id) <= 0) {
            throw new ServiceException("删除企业信息失败");
        }
    }
    
    /**
     * 根据ID获取企业信息
     * @param id 企业ID
     * @return 企业信息
     */
    public Company getById(Long id) {
        Company company = companyMapper.selectById(id);
        if (company == null) {
            throw new ServiceException("企业信息不存在");
        }
        
        // 获取关联的用户信息
        User user = userMapper.selectById(company.getUserId());
        if (user != null) {
            company.setUser(user);
        }
        
        return company;
    }
    
    /**
     * 分页查询企业信息
     * @param companyName 企业名称
     * @param industry 所属行业
     * @param verified 是否认证
     * @param currentPage 当前页
     * @param size 每页大小
     * @return 分页结果
     */
    public Page<Company> getCompaniesByPage(String companyName, String industry, Boolean verified, Integer currentPage, Integer size) {
        LambdaQueryWrapper<Company> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加查询条件
        if (StringUtils.isNotBlank(companyName)) {
            queryWrapper.like(Company::getCompanyName, companyName);
        }
        
        if (StringUtils.isNotBlank(industry)) {
            queryWrapper.like(Company::getIndustry, industry);
        }
        
        if (verified != null) {
            queryWrapper.eq(Company::getVerified, verified);
        }
        
        return companyMapper.selectPage(new Page<>(currentPage, size), queryWrapper);
    }
    
    /**
     * 更新企业认证状态
     * @param id 企业ID
     * @param verified 认证状态
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateVerificationStatus(Long id, Boolean verified) {
        Company company = companyMapper.selectById(id);
        if (company == null) {
            throw new ServiceException("企业信息不存在");
        }
        
        company.setVerified(verified);
        if (companyMapper.updateById(company) <= 0) {
            throw new ServiceException("更新认证状态失败");
        }
    }
    
    /**
     * 检查用户ID是否存在关联的企业信息
     * @param userId 用户ID
     * @return 存在返回true，不存在返回false
     */
    public boolean existsByUserId(Long userId) {
        return companyMapper.existsByUserId(userId);
    }
} 