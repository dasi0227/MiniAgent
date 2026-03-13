package com.dasi.domain.workspace.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class PromptGenerator {

    private static final String GENERATOR_SYSTEM = """
            你是一个“角色边界约束文本生成器”。
            你的唯一任务：仅针对“当前角色”，生成一段简短、可直接贴进提示词的约束文本。

            强制规则：
            1) 只能写当前角色，严禁提及其他角色或流程编排；
            2) 文本必须贴合用户任务领域，禁止泛化空话；
            3) 必须包含“可做边界”和“禁止边界”；
            4) 只输出一段中文纯文本，不要标题、不分段、不编号、不Markdown、不代码块；
            5) 长度控制在 80-160 个中文字符，简洁但有约束力。
            """;

    private static final String GENERATOR_USER = """
            【当前策略的流程链路描述】
             %s
            【当前角色的功能定位描述】
             %s：%s
            【当前用户的任务需求描述】
             %s
            请仅为“当前角色”生成一段约束文本，必须满足：
            - 只能描述当前角色，不得出现其它角色名称；
            - 明确该角色在本任务下“该做什么/不该做什么”；
            - 必须体现任务领域关键词；
            - 输出单段纯文本，禁止任何解释性前缀。
            """;

    private final ChatClient generatorClient;

    public PromptGenerator(@Qualifier("SystemModel") OpenAiChatModel systemModel) {
        this.generatorClient = ChatClient.builder(systemModel).build();
    }

    public String generateRoleConstraint(String strategyDesc, String clientRole, String roleDesc, String agentDesc) {

        try {
            Prompt prompt = new Prompt(List.of(
                    new SystemMessage(GENERATOR_SYSTEM),
                    new UserMessage(GENERATOR_USER.formatted(strategyDesc, clientRole, roleDesc, agentDesc))
            ));
            return generatorClient
                    .prompt(prompt)
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("【提示词生成】生成补充文本错误", e);
            return "暂无补充";
        }
    }

}
