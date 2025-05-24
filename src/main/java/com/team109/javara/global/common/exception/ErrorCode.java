package com.team109.javara.global.common.exception;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 인증/인가 관련 (401 Unauthorized, 403 Forbidden)
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED,  "유효하지 않은 리프레시 토큰입니다."),
    TOKEN_MISMATCH(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 일치하지 않습니다."),
    AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED,  "인증에 실패했습니다."),
    AUTHORIZATION_FAILED(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),

    // 사용자/리소스 관련 (404 Not Found, 409 Conflict, 400 Bad Request)
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND,  "해당 사용자를 찾을 수 없습니다."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND,  "요청한 리소스를 찾을 수 없습니다."),
    DUPLICATE_USERNAME(HttpStatus.CONFLICT,  "이미 사용 중인 아이디입니다."),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST,  "입력 값이 올바르지 않습니다."),
    WANTED_VEHICLE_NOT_FOUND(HttpStatus.NOT_FOUND,  "해당 수배차량이 없습니다."),
    PASSWORD_NOT_MATCH(HttpStatus.BAD_REQUEST, "현재 비밀번호가 일치하지 않습니다."),
    DUPLICATE_PASSWORD(HttpStatus.BAD_REQUEST, "비밀번호가 같습니다."),
    DUPLICATE_STATUS(HttpStatus.BAD_REQUEST, "상태가 같습니다."),
    DUPLICATE_EDGE_DEVICE_ID(HttpStatus.BAD_REQUEST, "엣지디바이스id가 같습니다."),
    //엣지디바이스 관련
    DEVICE_NOT_FOUND(HttpStatus.NOT_FOUND,  "엣지디바이스가 없습니다."),
    
    //이미지 관련
    INVALID_FILE_NAME(HttpStatus.BAD_REQUEST, "유요한 파일명이 아닙니다. 파일명을 변경하세요."),
    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "이미지 파일이 존제하지 않습니다. 파일이 서버로 보내졌는지 다시 확인하세요."),
    //수배차량 관련
    VEHICLE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 수배차량이 없습니다."),
    DUPLICATE_CASE_NUMBER(HttpStatus.CONFLICT, "이미 존재하는 사건 번호입니다."),

    //임무 관련
    TASK_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 임무가 없습니다."),
    TASK_NOT_ASSIGNED_TO_THIS_police(HttpStatus.CONFLICT, "임무가 해당 경찰관에게 할당되지 않았습니다."),
    TASK_ALREADY_ACCEPTED_BY_OTHER(HttpStatus.BAD_REQUEST,"다른 경찰관이 이미 수락했습니다."),
    INVALID_TASK_STATUS_FOR_ACCEPTANCE(HttpStatus.BAD_REQUEST, "임무 status 값이 잘못되었습니다"),
    INVALID_TASK_STATUS_FOR_REJECTION(HttpStatus.BAD_REQUEST, "거절은 assigned 상태(앱으로 알림 보낸 상태)에서만 가능합니다."),
    INVALID_TASK_STATUS_FOR_RESULT_PROCESSING(HttpStatus.BAD_REQUEST,"ACCEPT 상태의 임무만 완료 여부(COMPLETE, FAIL)를 설정할 수 있습니다. "),
    TASK_NOT_ASSIGNED_TO_THIS_police_FOR_EXECUTION(HttpStatus.BAD_REQUEST, "해당 임무 결과가 다른 경찰에 할당되어 있어 완료 할 수 없습니다"),
    TASK_ALREADY_ACCEPTED(HttpStatus.BAD_REQUEST,"경찰관이 이미 수락한 Task입니다."),

    // 서버 내부 오류 (500 Internal Server Error)
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),


    // 위치정보 오류
    LOCATION_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 위치 정보를 찾을 수 없습니다."),


    // 필요한 다른 오류 코드 추가...

    ;
    private final HttpStatus status;
    private final String message; //에러 메시지
}


//httpstatus 종류
/*
| 이름                            | code | Series                     | 설명
| ------------------------------ | --- | -------------------------- | ------------------------------------
| CONTINUE                       | 100 | INFORMATIONAL              | 계속
| SWITCHING_PROTOCOLS            | 101 | INFORMATIONAL              | 프로토콜 전환
| PROCESSING                     | 102 | INFORMATIONAL              | 처리 중
| EARLY_HINTS                    | 103 | INFORMATIONAL              | 초기 힌트
| CHECKPOINT                     | 103 | INFORMATIONAL              | 체크포인트 (더 이상 사용되지 않음)
| OK                             | 200 | SUCCESSFUL                 | 성공
| CREATED                        | 201 | SUCCESSFUL                 | 생성됨
| ACCEPTED                       | 202 | SUCCESSFUL                 | 수락됨
| NON_AUTHORITATIVE_INFORMATION  | 203 | SUCCESSFUL                 | 신뢰할 수 없는 정보
| NO_CONTENT                     | 204 | SUCCESSFUL                 | 콘텐츠 없음
| RESET_CONTENT                  | 205 | SUCCESSFUL                 | 콘텐츠 재설정
| PARTIAL_CONTENT                | 206 | SUCCESSFUL                 | 부분 콘텐츠
| MULTI_STATUS                   | 207 | SUCCESSFUL                 | 다중 상태
| ALREADY_REPORTED               | 208 | SUCCESSFUL                 | 이미 보고됨
| IM_USED                        | 226 | SUCCESSFUL                 | IM 사용
| MULTIPLE_CHOICES               | 300 | REDIRECTION                | 다중 선택
| MOVED_PERMANENTLY              | 301 | REDIRECTION                | 영구 이동
| FOUND                          | 302 | REDIRECTION                | 발견
| MOVED_TEMPORARILY              | 302 | REDIRECTION                | 임시 이동 (더 이상 사용되지 않음)
| SEE_OTHER                      | 303 | REDIRECTION                | 다른 위치 보기
| NOT_MODIFIED                   | 304 | REDIRECTION                | 수정되지 않음
| USE_PROXY                      | 305 | REDIRECTION                | 프록시 사용 (더 이상 사용되지 않음)
| TEMPORARY_REDIRECT             | 307 | REDIRECTION                | 임시 리다이렉션
| PERMANENT_REDIRECT             | 308 | REDIRECTION                | 영구 리다이렉션
| BAD_REQUEST                    | 400 | CLIENT_ERROR               | 잘못된 요청
| UNAUTHORIZED                   | 401 | CLIENT_ERROR               | 권한 없음
| PAYMENT_REQUIRED               | 402 | CLIENT_ERROR               | 결제 필요
| FORBIDDEN                      | 403 | CLIENT_ERROR               | 금지됨
| NOT_FOUND                      | 404 | CLIENT_ERROR               | 찾을 수 없음
| METHOD_NOT_ALLOWED             | 405 | CLIENT_ERROR               | 허용되지 않음
| NOT_ACCEPTABLE                 | 406 | CLIENT_ERROR               | 허용할 수 없음
| PROXY_AUTHENTICATION_REQUIRED  | 407 | CLIENT_ERROR               | 프록시 인증 필요
| REQUEST_TIMEOUT                | 408 | CLIENT_ERROR               | 요청 시간 초과
| CONFLICT                       | 409 | CLIENT_ERROR               | 충돌
| GONE                           | 410 | CLIENT_ERROR               | 사라짐
| LENGTH_REQUIRED                | 411 | CLIENT_ERROR               | 길이 필요
| PRECONDITION_FAILED            | 412 | CLIENT_ERROR               | 전제 조건 실패
| PAYLOAD_TOO_LARGE              | 413 | CLIENT_ERROR               | 페이로드 너무 큼
| REQUEST_ENTITY_TOO_LARGE       | 413 | CLIENT_ERROR               | 요청 엔티티 너무 큼 (더 이상 사용되지 않음)
| URI_TOO_LONG                   | 414 | CLIENT_ERROR               | URI 너무 김
| REQUEST_URI_TOO_LONG           | 414 | CLIENT_ERROR               | 요청 URI 너무 김 (더 이상 사용되지 않음)
| UNSUPPORTED_MEDIA_TYPE         | 415 | CLIENT_ERROR               | 지원되지 않는 미디어 유형
| REQUESTED_RANGE_NOT_SATISFIABLE| 416 | CLIENT_ERROR               | 요청 범위 만족하지 않음
| EXPECTATION_FAILED             | 417 | CLIENT_ERROR               | 기대 실패
| I_AM_A_TEAPOT                  | 418 | CLIENT_ERROR               | 나는 차 주전자입니다 (대다수 사용되고 있음)
| INSUFFICIENT_SPACE_ON_RESOURCE | 419 | CLIENT_ERROR               | 리소스의 공간 부족 (더 이상 사용되지 않음)
| METHOD_FAILURE                 | 420 | CLIENT_ERROR               | 메서드 실패 (더 이상 사용되지 않음)
| DESTINATION_LOCKED             | 421 | CLIENT_ERROR               | 대상 잠김 (더 이상 사용되지 않음)
| UNPROCESSABLE_ENTITY           | 422 | CLIENT_ERROR               | 처리할 수 없는 엔티티
| LOCKED                         | 423 | CLIENT_ERROR               | 잠김
| FAILED_DEPENDENCY              | 424 | CLIENT_ERROR               | 종속성 실패
| TOO_EARLY                      | 425 | CLIENT_ERROR               | 너무 이른
| UPGRADE_REQUIRED               | 426 | CLIENT_ERROR               | 업그레이드 필요
| PRECONDITION_REQUIRED          | 428 | CLIENT_ERROR               | 전제 조건 필요
| TOO_MANY_REQUESTS              | 429 | CLIENT_ERROR               | 너무 많은 요청
| REQUEST_HEADER_FIELDS_TOO_LARGE| 431 | CLIENT_ERROR               | 요청 헤더 필드 너무 큼
| UNAVAILABLE_FOR_LEGAL_REASONS  | 451 | CLIENT_ERROR               | 법적 이유로 이용 불가
| INTERNAL_SERVER_ERROR          | 500 | SERVER_ERROR               | 내부 서버 오류
| NOT_IMPLEMENTED                | 501 | SERVER_ERROR               | 구현되지 않음
| BAD_GATEWAY                    | 502 | SERVER_ERROR               | 불량 게이트웨이
| SERVICE_UNAVAILABLE            | 503 | SERVER_ERROR               | 서비스 불가
| GATEWAY_TIMEOUT                | 504 | SERVER_ERROR               | 게이트웨이 시간 초과
| HTTP_VERSION_NOT_SUPPORTED     | 505 | SERVER_ERROR               | HTTP 버전 지원되지 않음
| VARIANT_ALSO_NEGOTIATES        | 506 | SERVER_ERROR               | 변형 또한 협상됨
| INSUFFICIENT_STORAGE           | 507 | SERVER_ERROR               | 저장소 부족
| LOOP_DETECTED                  | 508 | SERVER_ERROR               | 루프 감지됨
| BANDWIDTH_LIMIT_EXCEEDED       | 509 | SERVER_ERROR               | 대역폭 한도 초과
| NOT_EXTENDED                   | 510 | SERVER_ERROR               | 확장되지 않음
| NETWORK_AUTHENTICATION_REQUIRED| 511 | SERVER_ERROR               | 네트워크 인증 필요
 */