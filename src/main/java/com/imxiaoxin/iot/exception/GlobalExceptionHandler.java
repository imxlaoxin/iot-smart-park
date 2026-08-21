package com.imxiaoxin.iot.exception;

import com.imxiaoxin.iot.common.R;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@ConditionalOnClass(DispatcherServlet.class)
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 404 资源不存在
     * @param e
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public void handleNoResourceFoundException(NoResourceFoundException e) {
        // 如果是图标缺失，直接忽略，不打印 ERROR 日志
        if (e.getResourcePath().contains("favicon.ico")) {
            return;
        }
        // 其他真正的 404 资源缺失再进行处理...
    }

    /**
     * 数据完整性异常
     */
    /* 这里SqlException异常被封装为了DataIntegrityViolationException,应该处理捕获这个异常 */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public R handleDataIntegrityViolationException(DataIntegrityViolationException e, HttpServletRequest request) {
        log.error("【DataIntegrityViolationException】请求地址: {} --> 发生数据完整性异常.", request.getRequestURI(), e);
        if (e.getMessage().contains("foreign")) {
            log.error("该数据被引用，无法删除");
            return R.fail("该数据被引用，无法删除");
        } else if (e.getMessage().contains("Duplicate")) {
            log.error("名称重复，请重新修改");
            return R.fail("名称重复，请重新修改");
        }
        log.error("数据完整性异常，请检查数据是否完整");
        return R.fail("数据完整性异常，请检查数据是否完整");
    }

    /**
     * 参数校验异常
     *
     * @param e
     * @return
     */
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public R handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
        log.error("【MethodArgumentNotValidException】请求地址: {} --> 发生参数校验异常.", request.getRequestURI(), e);
        // result 中封装了所有错误信息
        BindingResult result = e.getBindingResult();
        List<FieldError> errors = result.getFieldErrors();
        String errorMessage = errors.stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.joining("; "));
        return R.fail(HttpStatus.INTERNAL_SERVER_ERROR, errorMessage);
    }

    /**
     * 业务异常
     *
     * @param e
     * @return
     */
    @ExceptionHandler(BizException.class)
    public R handleServiceException(BizException e, HttpServletRequest request) {
        log.error("【BizException】请求地址: {} --> 发生业务异常.", request.getRequestURI(), e);
        return R.fail(e.getCode(), e.getMessage());
    }

    /**
     * 基础异常
     * @param e
     * @param request
     * @return
     */
    @ExceptionHandler(BaseException.class)
    public R handleBaseException(BaseException e, HttpServletRequest request) {
        log.error("【BaseException】请求地址: {} --> 发生基础异常.", request.getRequestURI(), e);
        return R.fail(e.getCode(), e.getMessage());
    }

    /**
     * 拦截未知的运行时异常和feign异常
     */
    @ExceptionHandler(RuntimeException.class)
    public R handleRuntimeException(RuntimeException e, HttpServletRequest request) {
      String requestURI = request.getRequestURI();
      log.error("【RuntimeException】请求地址: {} --> 发生未知的运行时异常.", requestURI, e);
      Throwable exp = e.getCause();
      if (exp != null) {
        String msg = exp.getMessage();
        // 处理feign抛出的异常
        return handelExp(e, msg);
      }
      return R.fail(e.getMessage());
    }

    /**
     * 系统异常和feign异常
     */
    @ExceptionHandler(Exception.class)
    public R handleException(Exception e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("【Exception】请求地址: {} --> 发生系统异常，异常类: {}", requestURI, e.getClass(), e);
        String msg = e.getMessage();
        // 处理feign抛出的异常
        return handelExp(e, msg);
    }

    private R handelExp(Exception e, String msg) {
        if (msg.contains("Exception")) {
            Map<String, Object> map = handleMsgExp(msg);
            Integer code = (Integer) map.get("code");
            String msg2 = (String) map.get("msg");
            return R.fail(code, msg2);
        }

        return R.fail(e.getMessage());
    }

    private Map<String, Object> handleMsgExp(String message) {
        // 正则表达式匹配 code 和 msg
        String regex = "code=(\\d+), msg=([^)]+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(message);
        Map<String, Object> map = new HashMap<>();
        if (matcher.find()) {
            String code = matcher.group(1); // 第一个分组是 code
            String msg = matcher.group(2).trim(); // 第二个分组是 msg
            map.put("code", Integer.parseInt(code));
            map.put("msg", msg);
        } else {
            map.put("code", HttpStatus.INTERNAL_SERVER_ERROR.value());
            map.put("msg", HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
        }
        return map;
    }
}