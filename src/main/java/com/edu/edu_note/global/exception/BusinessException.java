package com.edu.edu_note.global.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    // 에러 코드(ErrorCode)를 담을 그릇
    private final ErrorCode errorCode;

    // 생성자: 에러가 터질 때 ErrorCode를 받아서 저장함
    public BusinessException(ErrorCode errorCode) {
        // 부모(RuntimeException)에게 에러 메시지를 넘겨줌
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}