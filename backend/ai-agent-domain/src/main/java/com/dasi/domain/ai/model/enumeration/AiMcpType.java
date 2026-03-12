package com.dasi.domain.ai.model.enumeration;

import com.dasi.types.exception.MissingException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static com.dasi.types.constant.ExceptionMessage.ENUM_STR_NULL;
import static com.dasi.types.constant.ExceptionMessage.ENUM_TYPE_UNKNOWN;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum AiMcpType {

    SSE("服务器发送事件", "sse"),
    STDIO("标准输入输出", "stdio")
    ;

    private String name;

    private String type;

    public static AiMcpType fromString(String str) {
        if (str == null) {
            throw new MissingException(String.format(ENUM_STR_NULL, "AiMcpType"));
        }

        for (AiMcpType mcpType : values()) {
            if (mcpType.type.equals(str)) {
                return mcpType;
            }
        }

        throw new MissingException(String.format(ENUM_TYPE_UNKNOWN, "AiMcpType", str));
    }
}
