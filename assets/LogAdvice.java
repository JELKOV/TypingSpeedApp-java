package com.bungeobbang.app.biz.advice;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@Aspect
public class LogAdvice {
    //Service 사용 시작 로그
    @Before("PointcutCommon.allPointcut()")
    public void beforeLogger(JoinPoint joinPoint) throws Throwable {
        log.info("\u001B[33m---- AOP: start Service : {}\u001B[0m", joinPoint.getSignature());
    }
    //Service 사용 종료 로그
    @After("PointcutCommon.allPointcut()")
    public void afterLogger(JoinPoint joinPoint) throws Throwable {
        log.info("\u001B[33m---- AOP: end Service : {}\u001B[0m", joinPoint.getSignature());
    }
}
