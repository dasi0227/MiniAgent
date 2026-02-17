package com.dasi.sse.port;

import com.dasi.mcp.dto.SendTextToolRequest;
import com.dasi.mcp.port.IWeComPort;
import com.dasi.mcp.dto.SendTextCardToolRequest;
import com.dasi.mcp.dto.SendMessageToolResponse;
import com.dasi.sse.dto.GetAccessTokenResponse;
import com.dasi.sse.dto.SendTextCardHttpRequest;
import com.dasi.sse.dto.SendMessageHttpResponse;
import com.dasi.sse.dto.SendTextHttpRequest;
import com.dasi.sse.http.IWeComHttp;
import com.dasi.type.properties.WeComProperties;
import com.dasi.type.util.CacheUtil;
import com.dasi.type.util.SecretHeaderUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
public class WeComPort implements IWeComPort {

    private static final String ACCESS_TOKEN_CACHE_KEY = "WeComAccessToken";
    private static final int ACCESS_TOKEN_SKEW_SECONDS = 60;
    private static final int ACCESS_TOKEN_FALLBACK_SECONDS = 60;

    @Resource
    private WeComProperties weComProperties;

    @Resource
    private IWeComHttp weComHttp;

    @Resource
    private CacheUtil cacheUtil;

    private String getAccessToken(String corpid, String corpsecret) throws IOException {

        String cacheKey = buildAccessTokenCacheKey(corpid, corpsecret);
        String cachedToken = cacheUtil.getWithTtl(cacheKey, String.class);
        if (cachedToken != null && !cachedToken.isBlank()) {
            return cachedToken;
        }

        Call<GetAccessTokenResponse> call = weComHttp.getAccessToken(
                corpid,
                corpsecret
        );

        Response<GetAccessTokenResponse> callResponse = call.execute();

        if (!callResponse.isSuccessful()) {
            String err = callResponse.errorBody() == null ? "<empty>" : callResponse.errorBody().string();
            log.error("WeCom 获取 access_token 失败: {}", err);
            return null;
        }

        GetAccessTokenResponse httpResponse = callResponse.body();

        if (httpResponse == null) {
            log.error("WeCom 获取 access_token 的响应体为空");
            return null;
        }

        if (httpResponse.getErrcode() != null && httpResponse.getErrcode() != 0) {
            log.error("WeCom 获取 access_token 失败: {}", httpResponse.getErrmsg());
            return null;
        }

        String accessToken = httpResponse.getAccess_token();
        if (accessToken == null || accessToken.isBlank()) {
            log.error("WeCom 获取 access_token 的内容为空");
            return null;
        }

        long ttlSeconds;
        Integer expiresIn = httpResponse.getExpires_in();
        if (expiresIn == null || expiresIn <= 0) {
            ttlSeconds = ACCESS_TOKEN_FALLBACK_SECONDS;
        } else {
            ttlSeconds = Math.max(1L, expiresIn - ACCESS_TOKEN_SKEW_SECONDS);
        }

        cacheUtil.putWithTtl(cacheKey, accessToken, Duration.ofSeconds(ttlSeconds));

        log.info("调用 HTTP 获取企业微信令牌成功");

        return accessToken;
    }

    @Override
    public SendMessageToolResponse sendTextCard(SendTextCardToolRequest toolRequest) throws IOException {

        SendMessageToolResponse toolResponse = new SendMessageToolResponse();
        WeComConfig weComConfig = resolveConfig();
        if (!StringUtils.hasText(weComConfig.corpid()) || !StringUtils.hasText(weComConfig.corpsecret()) || weComConfig.agentid() == null) {
            toolResponse.setCode(500);
            toolResponse.setInfo("WeCom 参数未配置");
            return toolResponse;
        }

        // 获取令牌
        String accessToken = getAccessToken(weComConfig.corpid(), weComConfig.corpsecret());
        if (accessToken == null || accessToken.isBlank()) {
            toolResponse.setCode(500);
            toolResponse.setInfo("WeCom 获取 access_token 失败");
            return toolResponse;
        }

        // 构造文本卡片数据
        SendTextCardHttpRequest.TextCard textCard = new SendTextCardHttpRequest.TextCard();
        textCard.setTitle(toolRequest.getTitle());
        textCard.setDescription(toolRequest.getDescription());
        textCard.setUrl(toolRequest.getUrl());

        // 构造网络请求体
        SendTextCardHttpRequest httpRequest = new SendTextCardHttpRequest();
        httpRequest.setAgentid(weComConfig.agentid());
        httpRequest.setTextcard(textCard);

        // 发送网络请求
        Call<SendMessageHttpResponse> call = weComHttp.sendTextCard(httpRequest, accessToken);
        Response<SendMessageHttpResponse> callResponse = call.execute();
        log.info("WeCom 发送文本卡片消息：标题={} 概述={} 链接={}", toolRequest.getTitle(), toolRequest.getDescription(), toolRequest.getUrl());

        return buildToolResponseFromCallResponse(callResponse);
    }

    @Override
    public SendMessageToolResponse sendText(SendTextToolRequest toolRequest) throws IOException {

        SendMessageToolResponse toolResponse = new SendMessageToolResponse();
        WeComConfig weComConfig = resolveConfig();
        if (!StringUtils.hasText(weComConfig.corpid()) || !StringUtils.hasText(weComConfig.corpsecret()) || weComConfig.agentid() == null) {
            toolResponse.setCode(500);
            toolResponse.setInfo("WeCom 参数未配置");
            return toolResponse;
        }

        // 获取令牌
        String accessToken = getAccessToken(weComConfig.corpid(), weComConfig.corpsecret());
        if (accessToken == null || accessToken.isBlank()) {
            toolResponse.setCode(500);
            toolResponse.setInfo("WeCom access_token 获取失败");
            return toolResponse;
        }

        // 构造文本卡片数据
        SendTextHttpRequest.Text text = new SendTextHttpRequest.Text();
        text.setContent(toolRequest.getContent());

        // 构造网络请求体
        SendTextHttpRequest httpRequest = new SendTextHttpRequest();
        httpRequest.setAgentid(weComConfig.agentid());
        httpRequest.setText(text);

        // 发送网络请求
        Call<SendMessageHttpResponse> call = weComHttp.sendText(httpRequest, accessToken);
        Response<SendMessageHttpResponse> callResponse = call.execute();
        log.info("WeCom 发送文本消息：内容={}", toolRequest.getContent());

        return buildToolResponseFromCallResponse(callResponse);

    }

    private SendMessageToolResponse buildToolResponseFromCallResponse(Response<SendMessageHttpResponse> callResponse) throws IOException {

        SendMessageToolResponse toolResponse = new SendMessageToolResponse();

        if (!callResponse.isSuccessful()) {
            String err = callResponse.errorBody() == null ? "<empty>" : callResponse.errorBody().string();
            toolResponse.setCode(callResponse.code());
            toolResponse.setInfo("WeCom HTTP 请求失败: " + err);
            return toolResponse;
        }

        SendMessageHttpResponse httpResponse = callResponse.body();

        if (httpResponse == null) {
            toolResponse.setCode(500);
            toolResponse.setInfo("WeCom HTTP 响应体为空");
            return toolResponse;
        }

        log.info("WeCom HTTP 请求成功：{}", httpResponse);

        toolResponse.setCode(httpResponse.getErrcode());
        toolResponse.setInfo(httpResponse.getErrmsg());
        toolResponse.setMsgId(httpResponse.getMsgid());

        return toolResponse;
    }

    private WeComConfig resolveConfig() {
        Map<String, String> secretMap = SecretHeaderUtil.getSecretMap();
        String corpid = SecretHeaderUtil.resolve(secretMap, "corpid", weComProperties.getCorpid());
        String corpsecret = SecretHeaderUtil.resolve(secretMap, "corpsecret", weComProperties.getCorpsecret());
        String agentidRaw = SecretHeaderUtil.resolve(secretMap, "agentid", weComProperties.getAgentid() == null ? null : weComProperties.getAgentid().toString());
        Integer agentid = parseInteger(agentidRaw);
        return new WeComConfig(corpid, corpsecret, agentid);
    }

    private Integer parseInteger(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception ignored) {
            return null;
        }
    }

    private String buildAccessTokenCacheKey(String corpid, String corpsecret) {
        int hash = Objects.hash(corpid, corpsecret);
        return ACCESS_TOKEN_CACHE_KEY + ":" + Integer.toHexString(hash);
    }

    private record WeComConfig(String corpid, String corpsecret, Integer agentid) {
    }
}
