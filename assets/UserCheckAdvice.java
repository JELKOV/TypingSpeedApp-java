package com.bungeobbang.app.biz.advice;

import com.bungeobbang.app.biz.board.BoardDTO;
import com.bungeobbang.app.biz.reply.ReplyDTO;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@Aspect
public class UserCheckAdvice {
    @Autowired
    private HttpSession session;

    //session
    private final String MEMBER_PK = "userPK"; //세션에 저장된 memberPK

    //로그인 확인
    @Before("PointcutCommon.cudAllPointcut()")
    @Order(1) //첫번째로 실행
    public void loginCheck(JoinPoint joinPoint) {
        log.info("AOP: loginCheck start");
        //로그인 여부 확인 (member:회원가입 비밀번호변경, product c는 검사하지 않음 : 크롤링...)
        if(!joinPoint.getSignature().toString().contains("MemberService") && !joinPoint.getSignature().toString().contains("ProductService.insert") ) {
            if(session.getAttribute(MEMBER_PK) == null) {
                //로그인 되지 않은 상태라면 예외처리
                log.error("AOP try : {} / error not login", joinPoint.getSignature());
                throw new RuntimeException("try : " + joinPoint.getSignature() + " / error not login");
            }
        }
        log.info("AOP: loginCheck end");
    }

    //유효한 사용자인지 확인 (해당 데이터에 접근할 수 있는 사용자인지 검증
    @Before("PointcutCommon.cudAllPointcut()")
    public void userCheck(JoinPoint joinPoint) {
        log.info("AOP: userCheck start");
        Object MemberNum = null; //작성자 정보
        boolean flag = false;
        String signature = joinPoint.getSignature().toString(); //해당 메서드 경로 및 시그니처
        if(signature.contains("board")){
            log.info("AOP: userCheck board");
            //만약 board쪽 cud라면 작성자인지 확인
            for(Object o : joinPoint.getArgs()){ //파라미터에서 BoardDTO 찾기
                if(o instanceof BoardDTO){
                    MemberNum = ((BoardDTO) o).getMemberNum(); //작성자 정보 저장
                    flag = true;
                    break;
                }
            }
        }
        else if(signature.contains("reply")){
            log.info("AOP: userCheck reply");
            //만약 reply cud라면 작성자인지 확인
            for(Object o : joinPoint.getArgs()){ //파라미터에서 ReplyDTO 찾기
                if(o instanceof ReplyDTO){
                    MemberNum = ((ReplyDTO) o).getMemberNum(); //작성자 정보저장
                    flag = true;
                    break;
                }
            }
        }
        if(flag){
            log.info("AOP: MemberNum [{}]", MemberNum);
            if(session.getAttribute(MEMBER_PK) != MemberNum) { //작성자와 로그인한 사용자가 일치하지 않을 경우
                //예외처리
                log.error("AOP try : {} / error not writer", joinPoint.getSignature());
                throw new RuntimeException("try : " + joinPoint.getSignature() + " / error not writer");
            }
            log.info("AOP: userCheck end");
        }
    }
}
