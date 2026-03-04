package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.example.springboot.entity.Company;
import org.example.springboot.entity.Position;
import org.example.springboot.exception.ServiceException;
import org.example.springboot.mapper.CompanyMapper;
import org.example.springboot.mapper.PositionMapper;
import org.example.springboot.util.JwtTokenUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import org.example.springboot.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * 职位服务类
 */
@Service
public class PositionService {
    private static final Logger log = LoggerFactory.getLogger(PositionService.class);

    @Resource
    private PositionMapper positionMapper;

    @Resource
    private CompanyMapper companyMapper;
    @Autowired
    private JwtTokenUtils jwtTokenUtils;

    /**
     * 发布职位
     * @param position 职位信息
     * @return 职位ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createPosition(Position position) {
        // 获取当前登录用户的企业信息
        Company company = companyMapper.getCompanyByUserId(JwtTokenUtils.getCurrentUser().getId());
        if (company == null) {
            throw new ServiceException("请先完善企业信息");
        }
        
        // 设置职位关联的企业ID
        position.setCompanyId(company.getId());
        
        // 设置初始状态为审核中(2)
        position.setStatus(2);
        
        // 设置创建时间
        position.setCreateTime(LocalDateTime.now());
        
        // 插入数据库
        positionMapper.insert(position);
        
        log.info("职位发布成功，ID: {}, 名称: {}", position.getId(), position.getPositionName());
        
        return position.getId();
    }

    /**
     * 更新职位信息
     * @param position 职位信息
     * @return 更新结果
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updatePosition(Position position) {
        // 获取原职位信息
        Position originalPosition = positionMapper.selectById(position.getId());
        if (originalPosition == null) {
            throw new ServiceException("职位不存在");
        }
        
        // 检查权限：只能修改自己企业的职位
        checkCompanyPermission(originalPosition.getCompanyId());
        
        // 设置更新时间
        position.setUpdateTime(LocalDateTime.now());
        
        // 如果职位已经审核通过或下线，修改后需要重新审核
        if (originalPosition.getStatus() != 2) {
            position.setStatus(2);
        }
        
        // 更新数据库
        int result = positionMapper.updateById(position);
        
        log.info("职位更新成功，ID: {}, 名称: {}", position.getId(), position.getPositionName());
        
        return result > 0;
    }

    /**
     * 删除职位
     * @param id 职位ID
     * @return 删除结果
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deletePosition(Long id) {
        // 获取职位信息
        Position position = positionMapper.selectById(id);
        if (position == null) {
            throw new ServiceException("职位不存在");
        }
        
        // 检查权限：只能删除自己企业的职位
        checkCompanyPermission(position.getCompanyId());
        
        // 删除数据库记录
        int result = positionMapper.deleteById(id);
        
        log.info("职位删除成功，ID: {}", id);
        
        return result > 0;
    }

    /**
     * 更新职位状态（审核）
     * @param id 职位ID
     * @param status 状态（0-下线，1-上线，2-审核中）
     * @return 更新结果
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updatePositionStatus(Long id, Integer status) {
        // 获取职位信息
        Position position = positionMapper.selectById(id);
        if (position == null) {
            throw new ServiceException("职位不存在");
        }
        
        // 设置状态和更新时间
        position.setStatus(status);
        position.setUpdateTime(LocalDateTime.now());
        
        // 更新数据库
        int result = positionMapper.updateById(position);
        
        log.info("职位状态更新成功，ID: {}, 新状态: {}", id, status);
        
        return result > 0;
    }

    /**
     * 分页查询职位列表
     * @param currentPage 当前页
     * @param size 页大小
     * @param positionName 职位名称
     * @param positionType 职位类型
     * @param workAddress 工作地点
     * @param status 状态
     * @param companyId 企业ID
     * @param sortBy 排序字段
     * @return 分页结果
     */
    public IPage<Position> getPositionPage(Integer currentPage, Integer size, 
                                        String positionName, String positionType, 
                                        String workAddress, Integer status, Long companyId, String sortBy) {
        // 创建分页对象
        Page<Position> page = new Page<>(currentPage, size);
        
        // 创建查询条件
        LambdaQueryWrapper<Position> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加查询条件
        if (StringUtils.isNotBlank(positionName)) {
            queryWrapper.like(Position::getPositionName, positionName);
        }
        
        if (StringUtils.isNotBlank(positionType)) {
            queryWrapper.eq(Position::getPositionType, positionType);
        }
        
        if (StringUtils.isNotBlank(workAddress)) {
            queryWrapper.like(Position::getWorkAddress, workAddress);
        }
        
        if (status != null) {
            queryWrapper.eq(Position::getStatus, status);
        }
        
        if (companyId != null) {
            queryWrapper.eq(Position::getCompanyId, companyId);
        }
        
        // 按指定字段排序，默认按创建时间降序
        if ("createTime".equals(sortBy)) {
            queryWrapper.orderByDesc(Position::getCreateTime);
        } else {
            // 默认按创建时间降序排序
        queryWrapper.orderByDesc(Position::getCreateTime);
        }
        
        // 执行查询
        IPage<Position> resultPage = positionMapper.selectPage(page, queryWrapper);
        
        // 填充企业信息
        fillCompanyInfo(resultPage.getRecords());
        
        return resultPage;
    }

    /**
     * 获取职位详情
     * @param id 职位ID
     * @return 职位详情
     */
    public Position getPositionDetail(Long id) {
        // 查询职位信息
        Position position = positionMapper.selectById(id);
        if (position == null) {
            throw new ServiceException("职位不存在");
        }
        
        // 填充企业信息
        fillCompanyInfo(List.of(position));
        
        return position;
    }

    /**
     * 获取企业职位列表
     * @param companyId 企业ID
     * @return 职位列表
     */
    public List<Position> getCompanyPositions(Long companyId) {
        // 查询企业职位列表
        List<Position> positions = positionMapper.findByCompanyId(companyId);
        
        // 填充企业信息
        fillCompanyInfo(positions);
        
        return positions;
    }


    public List<Position> getAll(Long companyId,Integer status){
        LambdaQueryWrapper<Position> positionLambdaQueryWrapper = new LambdaQueryWrapper<>();

        if(companyId != null){
            positionLambdaQueryWrapper.eq(Position::getCompanyId,companyId);
        }
        if(status != null){
            positionLambdaQueryWrapper.eq(Position::getStatus,status);
        }
        List<Position> positions = positionMapper.selectList(positionLambdaQueryWrapper);
        fillCompanyInfo(positions);
        return positions;
    }



    /**
     * 检查是否有权限操作该企业的职位
     * @param companyId 企业ID
     */
    private void checkCompanyPermission(Long companyId) {
        // 获取当前用户
        var currentUser = JwtTokenUtils.getCurrentUser();
        
        // 管理员可以操作所有企业的职位
        if ("ADMIN".equals(currentUser.getRoleCode())) {
            return;
        }
        
        // 企业用户只能操作自己企业的职位
        if ("COMPANY".equals(currentUser.getRoleCode())) {
            Company company = companyMapper.getCompanyByUserId(currentUser.getId());
            if (company == null || !company.getId().equals(companyId)) {
                throw new ServiceException("无权操作该企业的职位");
            }
        } else {
            throw new ServiceException("无权操作职位");
        }
    }

    /**
     * 填充职位关联的企业信息
     * @param positions 职位列表
     */
    private void fillCompanyInfo(List<Position> positions) {
        if (positions == null || positions.isEmpty()) {
            return;
        }
        
        for (Position position : positions) {
            Company company = companyMapper.selectById(position.getCompanyId());
            if (company != null) {
                position.setCompany(company);
                position.setCompanyName(company.getCompanyName());
                position.setCompanyLogo(company.getCompanyLogo());
            }
        }
    }
    
    /**
     * 获取当前登录用户的企业ID
     * @return 企业ID
     */
    public Long getCurrentUserCompanyId() {
        // 获取当前用户
        User currentUser = JwtTokenUtils.getCurrentUser();
        if (currentUser == null) {
            throw new ServiceException("用户未登录");
        }
        
        // 检查用户角色
        if (!"COMPANY".equals(currentUser.getRoleCode()) && !"ADMIN".equals(currentUser.getRoleCode())) {
            throw new ServiceException("当前用户不是企业用户");
        }
        
        // 如果是管理员，需要特殊处理
        if ("ADMIN".equals(currentUser.getRoleCode())) {
            // 管理员可以查看所有企业的申请，这里可以返回null或特定值
            // 在ApplicationMapper中需要相应处理
            return null;
        }
        
        // 获取企业信息
        Company company = companyMapper.getCompanyByUserId(currentUser.getId());
        if (company == null) {
            throw new ServiceException("请先完善企业信息");
        }
        
        return company.getId();
    }
    
    /**
     * 根据ID获取职位信息
     * @param id 职位ID
     * @return 职位信息
     */
    public Position getById(Long id) {
        if (id == null) {
            return null;
        }
        return positionMapper.selectById(id);
    }

    /**
     * 根据企业ID获取企业信息
     * @param companyId 企业ID
     * @return 企业信息
     */
    public Company getCompanyById(Long companyId) {
        if (companyId == null) {
            return null;
        }
        return companyMapper.selectById(companyId);
    }

    /**
     * 根据职位ID获取关联的企业信息
     * @param positionId 职位ID
     * @return 企业信息
     */
    public Company getCompanyByPositionId(Long positionId) {
        if (positionId == null) {
            return null;
        }
        
        // 先获取职位信息
        Position position = positionMapper.selectById(positionId);
        if (position == null || position.getCompanyId() == null) {
            return null;
        }
        
        // 根据职位中的企业ID获取企业信息
        return companyMapper.selectById(position.getCompanyId());
    }


    public Map<String, Long> getPositionCategoryDistribution() {
        List<Map<String, Object>> results = positionMapper.getPositionCategoryDistribution();
        Map<String, Long> distribution = new HashMap<>();

        for (Map<String, Object> result : results) {
            String category = (String) result.get("category");
            // 处理空类别
            if (category == null || category.trim().isEmpty()) {
                category = "其他";
            }
            Long count = ((Number) result.get("count")).longValue();
            distribution.put(category, count);
        }

        return distribution;
    }
} 