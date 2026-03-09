package com.dasi.sse.port;

import com.dasi.credential.CsdnCredential;
import com.dasi.credential.McpHeaderContext;
import com.dasi.mcp.port.ICsdnPort;
import com.dasi.sse.dto.SaveArticleHttpRequest;
import com.dasi.sse.dto.SaveArticleHttpResponse;
import com.dasi.sse.http.ICsdnHttp;
import com.dasi.mcp.dto.SaveArticleToolRequest;
import com.dasi.mcp.dto.SaveArticleToolResponse;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
public class CsdnPort implements ICsdnPort {

    @Resource
    private ICsdnHttp csdnHttp;

    @Resource
    private McpHeaderContext mcpHeaderContext;

    @Override
    public SaveArticleToolResponse saveArticle(SaveArticleToolRequest toolRequest) throws IOException {

        SaveArticleToolResponse toolResponse = new SaveArticleToolResponse();
        CsdnCredential credential = resolveCredential(toolResponse);
        if (credential == null) {
            return toolResponse;
        }

        SaveArticleHttpRequest httpRequest = new SaveArticleHttpRequest();
        httpRequest.setTitle(toolRequest.getTitle());
        httpRequest.setMarkdowncontent(toolRequest.getMarkdownContent());
        httpRequest.setContent(toolRequest.getHtmlContent());
        httpRequest.setCover_images(List.of(credential.getCoverUrl()));
        httpRequest.setTags(credential.getTags());
        httpRequest.setCategories(credential.getCategories());

        Call<SaveArticleHttpResponse> call = csdnHttp.saveArticle(httpRequest, credential.getCookie());
        Response<SaveArticleHttpResponse> result = call.execute();

        log.info("调用 HTTP 进行 CSDN 发帖：标题={}", toolRequest.getTitle());

        if (!result.isSuccessful()) {
            String err = result.errorBody() == null ? "<empty>" : result.errorBody().string();
            toolResponse.setCode(result.code());
            toolResponse.setInfo("CSDN HTTP 请求失败: " + err);
            return toolResponse;
        }

        SaveArticleHttpResponse httpResponse = result.body();

        if (httpResponse == null) {
            toolResponse.setCode(500);
            toolResponse.setInfo("CSDN HTTP 响应体为空");
            return toolResponse;
        }
        if (httpResponse.getData() == null) {
            toolResponse.setCode(httpResponse.getCode());
            toolResponse.setInfo("CSDN HTTP 响应数据为空: " + httpResponse.getMsg());
            return toolResponse;
        }

        toolResponse.setCode(httpResponse.getCode());
        toolResponse.setInfo(httpResponse.getMsg());
        toolResponse.setUrl(httpResponse.getData().getUrl());
        toolResponse.setArticleId(httpResponse.getData().getId());
        toolResponse.setQrcode(httpResponse.getData().getQrcode());

        return toolResponse;
    }

    private CsdnCredential resolveCredential(SaveArticleToolResponse toolResponse) {
        CsdnCredential credential = mcpHeaderContext.getCredential();
        if (credential == null || !credential.checkValid()) {
            toolResponse.setCode(400);
            toolResponse.setInfo("头部信息缺失或非法，必须包含 cookie/categories/tags/coverUrl/userId");
            return null;
        }
        if (!StringUtils.hasText(credential.getCoverUrl())) {
            toolResponse.setCode(400);
            toolResponse.setInfo("头部信息缺失，coverUrl 不能为空");
            return null;
        }
        return credential;
    }

}
