package com.dasi.domain.util.stat;

import java.util.List;

public interface IStatUtil {

    void recordChatUsage(String clientId, List<String> mcpIdList);

    void recordWorkUsage(String agentId);

}
