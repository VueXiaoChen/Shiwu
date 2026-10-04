package com.fast.system.exception;

import com.fast.system.domain.AjaxResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理类
 *
 * 该类专门负责捕获系统中发生的各种异常
 *
 * 举个例子: 就像餐厅的后厨发生问题时, 服务员不会直接告诉顾客"厨师切到手了"
 * 而是说: "菜品需要的稍等一会"
 */
@Slf4j
@RestControllerAdvice //这是spring框架提供的注解, 表示这个类是一个全局的异常处理器
public class GlobalExceptionHandler {

    /**
     * 处理 RuntimeException异常
     * @param e 异常信息
     * @return 异常消息内容
     *
     * 这个方法专门处理运行时的异常
     * 运行时异常就像系统运行中突然出现的意外情况
     * 比如: 数据库突然断开、读取的文件不存在、网络连接超时或者用户名不存在等等
     */
    @ExceptionHandler(RuntimeException.class)
    public AjaxResult handleRuntimeException(RuntimeException e) {
        //1.首先在日志中详细记录异常情况
        log.info("运行时异常: ", e);

        //2.然后返回一个友好的错误消息给前端
        return AjaxResult.error(e.getMessage());
    }

    /**
     * 处理Exception异常
     * @param e 异常信息
     * @return 异常消息内容
     *
     * 可以这样理解:
     * - RuntimeException: 处理比较常见的或者说预期之内的运行时问题
     * - Exception: 处理其他所有未预料到的问题
     */
    @ExceptionHandler(Exception.class)
    public AjaxResult handleException(Exception e) {
        //1.首先在日志中详细记录异常情况
        log.info("系统异常: ", e);

        //2.然后返回一个友好的错误消息给前端
        return AjaxResult.error(e.getMessage());
    }

}
