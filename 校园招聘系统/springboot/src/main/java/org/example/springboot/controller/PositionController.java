package org.example.springboot.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.example.springboot.common.Result;
import org.example.springboot.entity.Company;
import org.example.springboot.entity.Position;
import org.example.springboot.entity.User;
import org.example.springboot.service.CompanyService;
import org.example.springboot.service.PositionService;
import org.example.springboot.util.JwtTokenUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 职位管理控制器
 */
@Tag(name = "职位管理")
@RestController
@RequestMapping("/position")
public class PositionController {
    private static final Logger log = LoggerFactory.getLogger(PositionController.class);

    @Resource
    private PositionService positionService;

    @Autowired
    private CompanyService companyService;

    /**
     * 发布职位
     * @param position 职位信息
     * @return 结果
     */
    @Operation(summary = "发布职位")
    @PostMapping
    public Result<?> create(@Valid @RequestBody Position position) {
        log.info("发布职位: {}", position.getPositionName());
        Long id = positionService.createPosition(position);
        return Result.success("发布成功，等待审核");
    }

    /**
     * 更新职位
     * @param id 职位ID
     * @param position 职位信息
     * @return 结果
     */
    @Operation(summary = "更新职位")
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @Valid @RequestBody Position position) {
        log.info("更新职位: {}", id);
        position.setId(id);
        boolean result = positionService.updatePosition(position);
        return result ? Result.success("更新成功") : Result.error("更新失败");
    }

    /**
     * 删除职位
     * @param id 职位ID
     * @return 结果
     */
    @Operation(summary = "删除职位")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        log.info("删除职位: {}", id);
        boolean result = positionService.deletePosition(id);
        return result ? Result.success("删除成功") : Result.error("删除失败");
    }

    /**
     * 更新职位状态
     * @param id 职位ID
     * @param status 状态（0-下线，1-上线，2-审核中）
     * @return 结果
     */
    @Operation(summary = "更新职位状态")
    @PutMapping("/{id}/status")
    public Result<?> updateStatus(
            @PathVariable Long id,
            @RequestParam Integer status) {
        log.info("更新职位状态: {}, 状态: {}", id, status);
        boolean result = positionService.updatePositionStatus(id, status);
        return result ? Result.success("状态更新成功") : Result.error("状态更新失败");
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
     * @return 分页结果
     */
    @Operation(summary = "分页查询职位列表")
    @GetMapping("/page")
    public Result<?> page(
            @RequestParam(defaultValue = "1") Integer currentPage,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String positionName,
            @RequestParam(required = false) String positionType,
            @RequestParam(required = false) String workAddress,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long companyId,
            @RequestParam(required = false) String sortBy) {
        log.info("分页查询职位列表: currentPage={}, size={}, sortBy={}", currentPage, size, sortBy);
        User currentUser = JwtTokenUtils.getCurrentUser();
        if(currentUser == null) {
            return Result.error("用户未登录");
        }
        if(currentUser.getRoleCode().equals("COMPANY")) {
            Company currentCompany = companyService.getByUserId(currentUser.getId());
            if(currentCompany != null) {
                companyId=currentCompany.getId();
            }else{
                return Result.error("企业信息获取失败");
            }
        }
        IPage<Position> page = positionService.getPositionPage(
                currentPage, size, positionName, positionType, workAddress, status, companyId, sortBy);
        return Result.success(page);
    }

    /**
     * 获取职位详情
     * @param id 职位ID
     * @return 职位详情
     */
    @Operation(summary = "获取职位详情")
    @GetMapping("/{id}")
    public Result<?> detail(@PathVariable Long id) {
        log.info("获取职位详情: {}", id);
        Position position = positionService.getPositionDetail(id);
        return Result.success(position);
    }

    /**
     * 获取企业职位列表
     * @param companyId 企业ID
     * @return 职位列表
     */
    @Operation(summary = "获取企业职位列表")
    @GetMapping("/company/{companyId}")
    public Result<?> companyPositions(@PathVariable Long companyId) {
        log.info("获取企业职位列表: {}", companyId);
        List<Position> positions = positionService.getCompanyPositions(companyId);
        return Result.success(positions);
    }

    /**
     * 获取所有职位
     */
    @GetMapping("/list")
    @Operation(summary = "获取所有职位")
    public Result<?> getAll(@RequestParam(required = false) Long companyId,@RequestParam(required = false) Integer status){
        User currentUser = JwtTokenUtils.getCurrentUser();
        if(currentUser == null){
            return Result.error("用户未登录");
        }
        if(currentUser.getRoleCode().equals("COMPANY")) {
            Company currentCompany = companyService.getByUserId(currentUser.getId());
            if(currentCompany != null) {
                companyId=currentCompany.getId();
            }else{
                return Result.error("企业信息获取失败");
            }
        }

        return Result.success( positionService.getAll(companyId, status));

    }
} 