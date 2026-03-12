package com.dasi.trigger.handler;

import com.dasi.types.exception.*;
import com.dasi.types.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;

import java.io.EOFException;
import java.io.IOException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({AsyncRequestNotUsableException.class, HttpRequestMethodNotSupportedException.class, EOFException.class, IOException.class})
    public void handleAsyncRequestNotUsable(Exception e) {
        // ignore，暂不处理 SSE/HTTP 的莫名错误
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValid(MethodArgumentNotValidException e) {
        log.error("参数传递非法：{}", e.getMessage());
        return Result.error(e.getMessage());
    }

    @ExceptionHandler(DependencyConflictException.class)
    public Result<Void> handleConflict(DependencyConflictException e) {
        log.error("依赖冲突: {}", e.getMessage());
        return Result.error(e.getMessage());
    }

    @ExceptionHandler(AuthException.class)
    public Result<Void> handleConflict(AuthException e) {
        log.error("鉴权错误: {}", e.getMessage());
        return Result.error(e.getMessage());
    }

    @ExceptionHandler(SessionException.class)
    public Result<Void> handleSession(SessionException e) {
        log.error("会话异常: {}", e.getMessage());
        return Result.error(e.getMessage());
    }

    @ExceptionHandler(MissingException.class)
    public Result<Void> handleMissing(MissingException e) {
        log.error("缺失异常: {}", e.getMessage());
        return Result.error(e.getMessage());
    }

    @ExceptionHandler(WorkException.class)
    public Result<Void> handleException(WorkException e) {
        log.error("系统异常: {}", e.getMessage());
        return Result.error("系统异常，请检查输入是否正确或联系管理员处理");
    }

    @ExceptionHandler(MiniAgentException.class)
    public Result<Void> handleException(MiniAgentException e) {
        log.error("系统异常: {}", e.getMessage(), e);
        return Result.error("系统异常，请联系管理员处理");
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error(e.getMessage(), e);
        return Result.error("未知异常，请联系管理员处理");
    }
}
