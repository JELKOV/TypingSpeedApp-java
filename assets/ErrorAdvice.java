package com.bungeobbang.app.biz.advice;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@Aspect
public class ErrorAdvice {
    //발생한 에러 확인로그 출력
    @AfterThrowing(pointcut = "PointcutCommon.allPointcut()", throwing = "ex")
    public void afterThrowing(JoinPoint joinPoint, Throwable ex) {
        log.error("\u001B[33mAOP: errorThrowing : {}\n\terrorMsg : {}\n\tStackTrace : {}\u001B[0m",
                //에러가 난 메서드 시그니처, 에러 메세지, stackTrace (replace는 줄바꿈 처리, ExceptionUtils를 통해 stackTrace를 문자열로 반환)
                joinPoint.getSignature(), ex.getMessage(), ExceptionUtils.getStackTrace(ex).replace("\n", "\n\t\t"));
    }
}
