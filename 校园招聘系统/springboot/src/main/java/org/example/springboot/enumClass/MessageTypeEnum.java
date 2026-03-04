package org.example.springboot.enumClass;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 消息类型枚举
 */
@Getter
@AllArgsConstructor
public enum MessageTypeEnum {
    
    SYSTEM_NOTICE("SYSTEM_NOTICE", "系统通知"),
    INTERVIEW_NOTICE("INTERVIEW_NOTICE", "面试通知"),
    RESUME_STATUS("RESUME_STATUS", "简历状态通知"),
    APPLICATION_STATUS("APPLICATION_STATUS", "投递状态通知"),
    VERIFICATION_NOTICE("VERIFICATION_NOTICE", "认证通知"),
    OTHER("OTHER", "其他通知");
    
    private final String code;
    private final String desc;
    
    /**
     * 根据code获取枚举
     */
    public static MessageTypeEnum getByCode(String code) {
        for (MessageTypeEnum type : MessageTypeEnum.values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return OTHER;
    }
} 