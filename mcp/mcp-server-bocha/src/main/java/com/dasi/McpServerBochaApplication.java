package com.dasi;

import com.dasi.mcp.tool.BochaTool;
import com.dasi.sse.http.IBochaHttp;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

@SpringBootApplication
public class McpServerBochaApplication {

    private static final String BOCHA_BASE_URL = "https://api.bocha.cn/";

    public static void main(String[] args) {
        SpringApplication.run(McpServerBochaApplication.class, args);
    }

    // 把 HTTP 接口变成一个可以被 Spring 调用的 Java 对象
    @Bean
    public IBochaHttp createHttp() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BOCHA_BASE_URL)
                .addConverterFactory(JacksonConverterFactory.create())
                .build();
        return retrofit.create(IBochaHttp.class);
    }

    // 把 MCP 接口变成一个可以被 Spring 调用的 Java 对象
    @Bean
    public ToolCallbackProvider createTool(BochaTool bochaTool) {
        return MethodToolCallbackProvider.builder().toolObjects(bochaTool).build();
    }

}
