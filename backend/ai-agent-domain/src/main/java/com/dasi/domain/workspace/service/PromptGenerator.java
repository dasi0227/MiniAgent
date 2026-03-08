package com.dasi.domain.workspace.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PromptGenerator {

    private static final String GENERATOR_SYSTEM = """
            你是一个提示词补充助手。你的任务是：为给定策略下的每个角色，生成“执行边界约束文本”。
            约束文本必须让每个角色根据任务需求只做自己该做的工作，禁止输出与角色职责无关的内容。
            你只需要输出最终的纯文本内容，禁止输出任何解释、Markdown、代码块。
            """;

    private static final String GENERATOR_USER = """
            【当前策略的流程链路描述】
             %s
             【当前角色】
             %s
            【当前角色的功能定位描述】
             %s
            【当前用户的任务需求描述】
             %s
            请为每个角色生成一段纯文本内容，要求：
            1) 严格贴合角色职责；
            2) 约束文本必须强调任务边界；
            3) 约束文本要精炼，同时必须符合策略；
            4) 禁止输出任何解释、Markdown、代码块。
            """;

    private final ChatClient generatorClient;

    public PromptGenerator(@Qualifier("SystemModel") OpenAiChatModel systemModel) {
        this.generatorClient = ChatClient.builder(systemModel).build();
    }

    public String generateRoleConstraint(String strategyDesc, String clientRole, String roleDesc, String agentDesc) {

        try {
            Prompt prompt = new Prompt()
                    .augmentSystemMessage(GENERATOR_SYSTEM)
                    .augmentUserMessage(GENERATOR_USER.formatted(strategyDesc, clientRole, roleDesc, agentDesc));
            return generatorClient
                    .prompt(prompt)
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("【workspace】ai 生成补充文本错误：{}", e.getMessage(), e);
            return "暂无补充";
        }
    }

}
