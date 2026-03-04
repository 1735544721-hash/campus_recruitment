package org.example.springboot.enumClass;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "账号状态枚举")
public enum AccountStatus {
    @Schema(description = "正常状态") NORMAL(1),
    @Schema(description = "禁用状态") DISABLED(0),
    @Schema(description = "待审核") PENDING_REVIEW(2),
    @Schema(description = "审核通过") REVIEW_SUCCESS(3),
    @Schema(description = "审核失败") REVIEW_FAILED(4);

    private final int value;

    AccountStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}