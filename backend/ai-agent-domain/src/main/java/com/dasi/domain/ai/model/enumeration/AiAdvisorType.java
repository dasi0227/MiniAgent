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
public enum AiAdvisorType {

    MEMORY("对话记忆", "Memory"),
    RAG("知识库回答", "Rag")
    ;

    private String name;

    private String type;


    public static AiAdvisorType fromString(String str) {
        if (str == null) {
            throw new MissingException(String.format(ENUM_STR_NULL, "AiAdvisorType"));
        }

        for (AiAdvisorType advisorType : values()) {
            if (advisorType.type.equals(str)) {
                return advisorType;
            }
        }

        throw new MissingException(String.format(ENUM_TYPE_UNKNOWN, "AiAdvisorType", str));
    }

}
