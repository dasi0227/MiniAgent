package com.dasi.domain.ai.service.execute;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MatchChecker {

    private static final String MATCH_SYSTEM = """
            你是任务匹配审核器。你只做一件事：判断“用户任务需求”是否与“智能体定位描述”匹配。
            输出规则必须严格遵守：
            1) 只能输出 YES 或 NO，禁止输出任何其他内容；
            2) 只有在任务与智能体定位完全不相关、或按定位完全不可能完成时，才输出 NO；
            3) 只要存在合理完成路径（即使需要拆解、联网、工具协作、迭代），都输出 YES。
            """;

    private static final String MATCH_USER = """
            【智能体定位描述】
            %s
            【用户任务需求】
            %s
            现在只输出 YES 或 NO，严禁输出任何解释、代码和 markdown。
            """;

    private final ChatClient matchClient;

    public MatchChecker(@Qualifier("SystemModel") OpenAiChatModel systemModel) {
        this.matchClient = ChatClient.builder(systemModel).build();
    }

    public boolean isTaskMatched(String agentDesc, String userMessage) {
        try {
            Prompt prompt = new Prompt()
                    .augmentSystemMessage(MATCH_SYSTEM)
                    .augmentUserMessage(MATCH_USER.formatted(agentDesc, userMessage));
            String result = matchClient.prompt(prompt).call().content();
            if (StringUtils.isBlank(result)) {
                log.warn("【MatchChecker】匹配结果为空，默认拒绝");
                return false;
            } else {
                return result.trim().toUpperCase().contains("YES");
            }
        } catch (Exception e) {
            log.warn("【MatchChecker】匹配校验失败，默认拒绝：{}", e.getMessage());
            return false;
        }
    }

}
