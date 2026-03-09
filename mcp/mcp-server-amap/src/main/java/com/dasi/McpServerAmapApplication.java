package com.dasi;

import com.dasi.mcp.tool.AmapTool;
import com.dasi.sse.http.IAmapHttp;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

@SpringBootApplication
public class McpServerAmapApplication {

    private static final String AMAP_BASE_URL = "https://restapi.amap.com/v3/";

    public static void main(String[] args) {
        SpringApplication.run(McpServerAmapApplication.class, args);
    }

    // 把 HTTP 接口变成一个可以被 Spring 调用的 Java 对象
    @Bean
    public IAmapHttp createHttp() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(AMAP_BASE_URL)
                .addConverterFactory(JacksonConverterFactory.create())
                .build();
        return retrofit.create(IAmapHttp.class);
    }

    // 把 MCP 接口变成一个可以被 Spring 调用的 Java 对象
    @Bean
    public ToolCallbackProvider createTool(AmapTool amapTool) {
        return MethodToolCallbackProvider.builder().toolObjects(amapTool).build();
    }

}
