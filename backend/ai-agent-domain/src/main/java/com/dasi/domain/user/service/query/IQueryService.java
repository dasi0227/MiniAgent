package com.dasi.domain.user.service.query;

import com.dasi.domain.user.model.vo.*;

import java.util.List;
import java.util.Map;

public interface IQueryService {

    List<QueryChatClientVO> queryChatClientList();

    List<QueryMcpVO> queryMcpList();

    List<QueryRagVO> queryRagList();

    List<QueryWorkAgentVO> queryWorkAgentList();

    List<QueryModelVO> queryModelList();

    Map<String, QueryRoleVO> queryRoleMap();

}
