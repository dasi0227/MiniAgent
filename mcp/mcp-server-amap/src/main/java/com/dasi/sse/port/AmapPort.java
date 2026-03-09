package com.dasi.sse.port;

import com.dasi.credential.AmapCredential;
import com.dasi.credential.McpHeaderContext;
import com.dasi.mcp.port.IAmapPort;
import com.dasi.mcp.dto.CheckWeatherToolRequest;
import com.dasi.mcp.dto.CheckWeatherToolResponse;
import com.dasi.sse.dto.CheckWeatherHttpResponse;
import com.dasi.sse.dto.SearchAddressHttpResponse;
import com.dasi.sse.http.IAmapHttp;
import com.dasi.mcp.dto.WeatherCondition;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class AmapPort implements IAmapPort {

    @Resource
    private McpHeaderContext mcpHeaderContext;

    @Resource
    private IAmapHttp amapHttp;

    private String getAdcode(String address, AmapCredential credential) throws IOException {

        Map<String, String> httpParams = new HashMap<>();
        httpParams.put("key", credential.getApiKey());
        httpParams.put("address", address);

        Call<SearchAddressHttpResponse> call = amapHttp.searchAddress(httpParams);

        Response<SearchAddressHttpResponse> callResponse = call.execute();

        if (!callResponse.isSuccessful()) {
            String err = callResponse.errorBody() == null ? "<empty>" : callResponse.errorBody().string();
            log.error("AMAP 获取城市编码失败: {}", err);
            return null;
        }

        SearchAddressHttpResponse httpResponse = callResponse.body();

        if (httpResponse == null) {
            log.error("AMAP 获取城市编码的响应体为空");
            return null;
        }

        if (httpResponse.getStatus() != null && httpResponse.getStatus().equals("0")) {
            log.error("AMAP 获取城市编码失败: {}", httpResponse.getInfo());
            return null;
        }

        String adcode = httpResponse.getGeocodes().get(0).getAdcode();
        if (adcode == null || adcode.isBlank()) {
            log.error("AMAP 获取城市编码的内容为空");
            return null;
        }

        log.info("调用 HTTP 获取城市编码：adcode={}", adcode);

        return adcode;
    }

    @Override
    public CheckWeatherToolResponse checkWeather(CheckWeatherToolRequest toolRequest) throws IOException {

        CheckWeatherToolResponse toolResponse = new CheckWeatherToolResponse();
        AmapCredential credential = resolveCredential(toolResponse);
        if (credential == null) {
            return toolResponse;
        }

        String adcode = getAdcode(toolRequest.getAddress(), credential);
        if (adcode == null || adcode.isBlank()) {
            toolResponse.setCode("500");
            toolResponse.setInfo("获取城市编码 adcode 失败");
            return toolResponse;
        }

        Map<String, String> httpParams = new HashMap<>();
        httpParams.put("key", credential.getApiKey());
        httpParams.put("city", adcode);
        httpParams.put("extensions", "all");

        Call<CheckWeatherHttpResponse> call = amapHttp.checkWeather(httpParams);
        Response<CheckWeatherHttpResponse> callResponse = call.execute();
        log.info("调用 HTTP 进行高德地图获取天气情况：address={}, adcode={}", toolRequest.getAddress(), adcode);

        if (!callResponse.isSuccessful()) {
            String err = callResponse.errorBody() == null ? "<empty>" : callResponse.errorBody().string();
            toolResponse.setCode(String.valueOf(callResponse.code()));
            toolResponse.setInfo("AMAP HTTP 请求失败: " + err);
            return toolResponse;
        }

        CheckWeatherHttpResponse httpResponse = callResponse.body();

        if (httpResponse == null) {
            toolResponse.setCode("500");
            toolResponse.setInfo("AMAP HTTP 响应体为空");
            return toolResponse;
        }

        toolResponse.setCode(httpResponse.getInfocode() != null ? httpResponse.getInfocode() : httpResponse.getStatus());
        toolResponse.setInfo(httpResponse.getInfo());

        if (httpResponse.getForecasts() == null || httpResponse.getForecasts().isEmpty()) {
            return toolResponse;
        }

        CheckWeatherHttpResponse.Forecast forecast = httpResponse.getForecasts().get(0);
        toolResponse.setProvince(forecast.getProvince());
        toolResponse.setCity(forecast.getCity());
        toolResponse.setReportTime(forecast.getReporttime());

        if (forecast.getCasts() != null && !forecast.getCasts().isEmpty()) {
            CheckWeatherHttpResponse.Forecast.Cast today = forecast.getCasts().get(0);
            WeatherCondition todayWeather = WeatherCondition.builder()
                    .date(today.getDate())
                    .dayWeather(today.getDayweather())
                    .nightWeather(today.getNightweather())
                    .dayTemperature(today.getDaytemp())
                    .nightTemperature(today.getNighttemp())
                    .dayWindDirection(today.getDaywind())
                    .nightWindDirection(today.getNightwind())
                    .dayWindPower(today.getDaypower())
                    .nightWindPower(today.getNightpower())
                    .build();
            toolResponse.setTodayWeather(todayWeather);
        }

        if (forecast.getCasts() != null && forecast.getCasts().size() > 1) {
            CheckWeatherHttpResponse.Forecast.Cast tomorrow = forecast.getCasts().get(1);
            WeatherCondition tomorrowWeather = WeatherCondition.builder()
                    .date(tomorrow.getDate())
                    .dayWeather(tomorrow.getDayweather())
                    .nightWeather(tomorrow.getNightweather())
                    .dayTemperature(tomorrow.getDaytemp())
                    .nightTemperature(tomorrow.getNighttemp())
                    .dayWindDirection(tomorrow.getDaywind())
                    .nightWindDirection(tomorrow.getNightwind())
                    .dayWindPower(tomorrow.getDaypower())
                    .nightWindPower(tomorrow.getNightpower())
                    .build();
            toolResponse.setTomorrowWeather(tomorrowWeather);
        }

        return toolResponse;
    }

    private AmapCredential resolveCredential(CheckWeatherToolResponse toolResponse) {
        AmapCredential credential = mcpHeaderContext.getCredential();
        if (credential == null || !credential.checkValid()) {
            toolResponse.setCode("400");
            toolResponse.setInfo("头部信息缺失或非法，必须包含 apiKey/userId");
            return null;
        }
        return credential;
    }

}
