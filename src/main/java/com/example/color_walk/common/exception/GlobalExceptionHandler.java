package com.example.color_walk.common.exception;

import com.amazonaws.services.s3.model.AmazonS3Exception;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("IllegalArgumentException 발생: {}", e.getMessage());

        Map<String, Object> errorResponse = createErrorResponse(
                "INVALID_REQUEST",
                e.getMessage(),
                HttpStatus.BAD_REQUEST
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * S3 관련 예외 처리
     * - 파일 업로드/삭제 실패, ACL 설정 오류 등
     */
    @ExceptionHandler(AmazonS3Exception.class)
    public ResponseEntity<Map<String, Object>> handleAmazonS3Exception(AmazonS3Exception e) {
        log.error("AmazonS3Exception 발생 - Error Code: {}, Message: {}", e.getErrorCode(), e.getMessage());

        String userMessage;
        HttpStatus status;

        switch (e.getErrorCode()) {
            case "AccessControlListNotSupported":
                userMessage = "파일 업로드 설정에 문제가 있습니다. 관리자에게 문의하세요.";
                status = HttpStatus.INTERNAL_SERVER_ERROR;
                break;
            case "InvalidBucketName":
            case "NoSuchBucket":
                userMessage = "저장소 설정에 문제가 있습니다. 관리자에게 문의하세요.";
                status = HttpStatus.INTERNAL_SERVER_ERROR;
                break;
            default:
                userMessage = "파일 처리 중 오류가 발생했습니다. 다시 시도해주세요.";
                status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        Map<String, Object> errorResponse = createErrorResponse(
                "S3_ERROR",
                userMessage,
                status
        );
        errorResponse.put("s3ErrorCode", e.getErrorCode());

        return ResponseEntity.status(status).body(errorResponse);
    }

    /**
     * 파일 크기 초과 예외 처리
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        log.error("MaxUploadSizeExceededException 발생: {}", e.getMessage());

        Map<String, Object> errorResponse = createErrorResponse(
                "FILE_SIZE_EXCEEDED",
                "파일 크기가 너무 큽니다. 최대 10MB까지 업로드 가능합니다.",
                HttpStatus.PAYLOAD_TOO_LARGE
        );
        errorResponse.put("maxSize", "10MB");

        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(errorResponse);
    }

    /**
     * ResponseStatusException 처리
     * - S3Service에서 잘못된 파일 형식 등
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatusException(ResponseStatusException e) {
        log.error("ResponseStatusException 발생 - Status: {}, Message: {}", e.getStatusCode(), e.getReason());

        Map<String, Object> errorResponse = createErrorResponse(
                "RESPONSE_ERROR",
                e.getReason() != null ? e.getReason() : "요청 처리 중 오류가 발생했습니다.",
                HttpStatus.valueOf(e.getStatusCode().value())
        );

        return ResponseEntity.status(e.getStatusCode()).body(errorResponse);
    }

    /**
     * Runtime 예외 처리 (PhotoService에서 발생할 수 있는 일반적인 런타임 에러)
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException e) {
        log.error("RuntimeException 발생: {}", e.getMessage(), e);

        // 특정 메시지 패턴에 따라 다르게 처리
        String message = e.getMessage();
        if (message != null && message.contains("사진 업로드 실패")) {
            Map<String, Object> errorResponse = createErrorResponse(
                    "PHOTO_UPLOAD_FAILED",
                    "사진 업로드에 실패했습니다. 다시 시도해주세요.",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }

        Map<String, Object> errorResponse = createErrorResponse(
                "RUNTIME_ERROR",
                "서버에서 오류가 발생했습니다. 다시 시도해주세요.",
                HttpStatus.INTERNAL_SERVER_ERROR
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * 일반적인 Exception 처리 (최후의 수단)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception e) {
        log.error("예상치 못한 예외 발생: {}", e.getMessage(), e);

        Map<String, Object> errorResponse = createErrorResponse(
                "INTERNAL_SERVER_ERROR",
                "서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
                HttpStatus.INTERNAL_SERVER_ERROR
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * 에러 응답 생성 헬퍼 메서드
     */
    private Map<String, Object> createErrorResponse(String errorCode, String message, HttpStatus status) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", errorCode);
        errorResponse.put("message", message);
        errorResponse.put("status", status.value());
        errorResponse.put("timestamp", System.currentTimeMillis());
        return errorResponse;
    }
}
