package com.dasi.sse.port;

import com.dasi.mcp.port.ICsdnPort;
import com.dasi.sse.dto.SaveArticleHttpRequest;
import com.dasi.sse.dto.SaveArticleHttpResponse;
import com.dasi.sse.http.ICsdnHttp;
import com.dasi.mcp.dto.SaveArticleToolRequest;
import com.dasi.mcp.dto.SaveArticleToolResponse;
import com.dasi.type.properties.CsdnProperties;
import com.dasi.type.util.SecretHeaderUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class CsdnPort implements ICsdnPort {

    @Resource
    private ICsdnHttp csdnHttp;

    @Resource
    private CsdnProperties csdnProperties;

    @Override
    public SaveArticleToolResponse saveArticle(SaveArticleToolRequest toolRequest) throws IOException {
        Map<String, String> secretMap = SecretHeaderUtil.getSecretMap();
        String cookie = SecretHeaderUtil.resolve(secretMap, "cookie", csdnProperties.getCookie());
        String coverUrl = SecretHeaderUtil.resolve(secretMap, "coverUrl", csdnProperties.getCoverUrl());
        String categories = SecretHeaderUtil.resolve(secretMap, "categories", csdnProperties.getCategories());
        String tags = SecretHeaderUtil.resolve(secretMap, "tags", csdnProperties.getTags());

        if (!StringUtils.hasText(cookie)) {
            SaveArticleToolResponse toolResponse = new SaveArticleToolResponse();
            toolResponse.setCode(500);
            toolResponse.setInfo("CSDN 参数未配置");
            return toolResponse;
        }

        SaveArticleHttpRequest httpRequest = new SaveArticleHttpRequest();
        httpRequest.setTitle(toolRequest.getTitle());
        httpRequest.setMarkdowncontent(toolRequest.getMarkdownContent());
        httpRequest.setContent(toolRequest.getHtmlContent());
        httpRequest.setCover_images(StringUtils.hasText(coverUrl) ? List.of(coverUrl) : List.of());
        httpRequest.setTags(tags);
        httpRequest.setCategories(categories);

        Call<SaveArticleHttpResponse> call = csdnHttp.saveArticle(httpRequest, cookie);
        Response<SaveArticleHttpResponse> result = call.execute();

        log.info("调用 HTTP 进行 CSDN 发帖：标题={}", toolRequest.getTitle());

        SaveArticleToolResponse toolResponse = new SaveArticleToolResponse();

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

}
