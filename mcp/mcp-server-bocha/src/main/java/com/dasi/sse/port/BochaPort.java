package com.dasi.sse.port;

import com.dasi.credential.BochaCredential;
import com.dasi.credential.McpHeaderContext;
import com.dasi.mcp.dto.BochaSearchToolRequest;
import com.dasi.mcp.dto.BochaSearchToolResponse;
import com.dasi.mcp.port.IBochaPort;
import com.dasi.sse.dto.BochaWebSearchHttpRequest;
import com.dasi.sse.dto.BochaWebSearchHttpResponse;
import com.dasi.sse.http.IBochaHttp;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BochaPort implements IBochaPort {

    @Resource
    private McpHeaderContext mcpHeaderContext;

    @Resource
    private IBochaHttp bochaHttp;

    @Override
    public BochaSearchToolResponse webSearch(BochaSearchToolRequest toolRequest) throws IOException {

        BochaSearchToolResponse toolResponse = new BochaSearchToolResponse();
        toolResponse.setResults(Collections.emptyList());

        if (toolRequest == null) {
            toolResponse.setCode("400");
            toolResponse.setInfo("Bocha toolRequest 不能为空");
            return toolResponse;
        }

        if (toolRequest.getQuery() == null || toolRequest.getQuery().isBlank()) {
            toolResponse.setCode("400");
            toolResponse.setInfo("Bocha query 不能为空");
            return toolResponse;
        }

        if (toolRequest.getFreshness() == null || toolRequest.getFreshness().isBlank()) {
            toolResponse.setCode("400");
            toolResponse.setInfo("Bocha freshness 不能为空");
            return toolResponse;
        }

        BochaCredential credential = resolveCredential(toolResponse);
        if (credential == null) {
            return toolResponse;
        }

        BochaWebSearchHttpRequest httpRequest = BochaWebSearchHttpRequest.builder()
                .query(toolRequest.getQuery())
                .freshness(toolRequest.getFreshness())
                .build();

        Call<BochaWebSearchHttpResponse> call = bochaHttp.webSearch(
                "Bearer " + credential.getApiKey(),
                httpRequest
        );

        Response<BochaWebSearchHttpResponse> callResponse = call.execute();
        log.info("调用 HTTP 进行博查联网搜索：query={}, freshness={}", toolRequest.getQuery(), toolRequest.getFreshness());

        if (!callResponse.isSuccessful()) {
            String err = callResponse.errorBody() == null ? "<empty>" : callResponse.errorBody().string();
            toolResponse.setCode(String.valueOf(callResponse.code()));
            toolResponse.setInfo("Bocha HTTP 请求失败: " + err);
            return toolResponse;
        }

        BochaWebSearchHttpResponse httpResponse = callResponse.body();

        if (httpResponse == null) {
            toolResponse.setCode("500");
            toolResponse.setInfo("Bocha HTTP 响应体为空");
            return toolResponse;
        }

        String httpCode = httpResponse.getCode();
        String httpInfo = firstNonBlank(httpResponse.getMessage(), httpResponse.getMsg());

        if (httpCode != null && !httpCode.isBlank() && !"200".equals(httpCode)) {
            toolResponse.setCode(httpCode);
            toolResponse.setInfo(firstNonBlank(httpInfo, "Bocha 请求失败"));
            return toolResponse;
        }

        toolResponse.setCode(firstNonBlank(httpCode, "200"));
        toolResponse.setInfo(firstNonBlank(httpInfo, "ok"));

        BochaWebSearchHttpResponse searchData = httpResponse.getData() != null ? httpResponse.getData() : httpResponse;

        List<BochaWebSearchHttpResponse.WebPageValue> values =
                searchData.getWebPages() == null ? null : searchData.getWebPages().getValue();

        if (values == null || values.isEmpty()) {
            return toolResponse;
        }

        List<BochaSearchToolResponse.SearchResult> results = values.stream()
                .map(item -> {
                    BochaSearchToolResponse.SearchResult result = new BochaSearchToolResponse.SearchResult();
                    result.setName(item.getName());
                    result.setUrl(item.getUrl());
                    result.setSnippet(item.getSnippet());
                    result.setSummary(item.getSummary());
                    result.setDatePublished(item.getDatePublished());
                    return result;
                })
                .collect(Collectors.toList());

        toolResponse.setResults(results);
        return toolResponse;
    }

    private BochaCredential resolveCredential(BochaSearchToolResponse toolResponse) {
        BochaCredential credential = mcpHeaderContext.getCredential();
        if (credential == null || !credential.checkValid()) {
            toolResponse.setCode("400");
            toolResponse.setInfo("头部信息缺失或非法，必须包含 apiKey/userId");
            return null;
        }
        return credential;
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

}
