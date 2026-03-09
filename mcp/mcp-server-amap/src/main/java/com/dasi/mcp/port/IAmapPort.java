package com.dasi.mcp.port;

import com.dasi.mcp.dto.CheckWeatherToolRequest;
import com.dasi.mcp.dto.CheckWeatherToolResponse;

import java.io.IOException;

public interface IAmapPort {

    CheckWeatherToolResponse checkWeather(CheckWeatherToolRequest toolRequest) throws IOException;

}
