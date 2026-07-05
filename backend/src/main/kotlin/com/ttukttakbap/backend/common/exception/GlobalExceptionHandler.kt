package com.ttukttakbap.backend.common.exception

import com.ttukttakbap.backend.common.dto.ErrorResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(NotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleNotFoundException(e: NotFoundException): ErrorResponse =
        ErrorResponse(status = 404, message = e.message ?: "리소스를 찾을 수 없습니다.")

    @ExceptionHandler(IllegalArgumentException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleIllegalArgumentException(e: IllegalArgumentException): ErrorResponse =
        ErrorResponse(status = 400, message = e.message ?: "잘못된 요청입니다.")

    @ExceptionHandler(UnauthorizedException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun handleUnauthorizedException(e: UnauthorizedException): ErrorResponse =
        ErrorResponse(status = 401, message = e.message ?: "인증에 실패했습니다.")

    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleException(e: Exception): ErrorResponse {
        log.error("처리되지 않은 예외로 500 응답: ${e.message}", e)
        return ErrorResponse(status = 500, message = "서버 내부 오류가 발생했습니다.")
    }
}
