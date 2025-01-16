package com.bungeobbang.app.biz.advice;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class PointcutCommon {
    //로그인 확인, 작성자 일치확인, 권한 검증 등 AOP사용 포인트 컷
    @Pointcut("execution(boolean com.bungeobbang.app.biz..*Impl.*(..))")
    public void cudAllPointcut() {}

    //에러 발생 시 에러코드, service사용 로그 등 포인트 컷
    @Pointcut("execution(* com.bungeobbang.app.biz..*Impl.*(..))")
    public void allPointcut() {}
}
