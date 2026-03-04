package org.example.springboot.enumClass;

import lombok.Getter;

/**
 * 投递状态枚举
 */
@Getter
public enum ApplicationStatusEnum {
    
    PENDING(0, "待查看"),
    VIEWED(1, "已查看"),
    PASSED(2, "通过筛选"),
    INTERVIEW(3, "面试邀请"),
    REJECTED(4, "不合适"),
    OFFERED(5, "已录用");
    
    private final Integer code;
    private final String name;
    
    ApplicationStatusEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }
    
    /**
     * 根据状态码获取状态名称
     * @param code 状态码
     * @return 状态名称
     */
    public static String getNameByCode(Integer code) {
        for (ApplicationStatusEnum status : ApplicationStatusEnum.values()) {
            if (status.getCode().equals(code)) {
                return status.getName();
            }
        }
        return "未知状态";
    }
    
    /**
     * 检查状态码是否合法
     * @param code 状态码
     * @return 是否合法
     */
    public static boolean isValidStatus(Integer code) {
        for (ApplicationStatusEnum status : ApplicationStatusEnum.values()) {
            if (status.getCode().equals(code)) {
                return true;
            }
        }
        return false;
    }
} 