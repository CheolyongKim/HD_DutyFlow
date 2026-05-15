package exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

	// 공통
	INVALID_INPUT("INVALID_INPUT", "잘못된 입력입니다."), DATA_NOT_FOUND("DATA_NOT_FOUND", "데이터를 찾을 수 없습니다."),
	DUPLICATE_DATA("DUPLICATE_DATA", "이미 존재하는 데이터입니다."), UNAUTHORIZED("UNAUTHORIZED", "로그인이 필요합니다."),
	ACCESS_DENIED("ACCESS_DENIED", "접근 권한이 없습니다."), ILLEGAL_STATE("ILLEGAL_STATE", "시스템 검증 과정에 오류가 존재합니다."),
	// 이전에서 검증되었어야 할 조건을 만족하지 못했을 때

	// 장바구니
	INVALID_QUANTITY("INVALID_QUANTITY", "수량은 1개 이상이어야 합니다."),
	PRODUCT_NOT_FOUND("PRODUCT_NOT_FOUND", "상품 정보를 찾을 수 없습니다."),
	CART_PRODUCT_NOT_FOUND("CART_PRODUCT_NOT_FOUND", "장바구니에 존재하지 않는 상품입니다."),
	INVALID_PRODUCT_PRICE("INVALID_PRODUCT_PRICE", "상품 가격 정보가 올바르지 않습니다."),
	EMPTY_SELECTED_PRODUCTS("EMPTY_SELECTED_PRODUCTS", "선택된 상품이 없습니다."),

	// ORDER
	ORDER_INVALID_STATE("ORDER_INVALID_STATE", "현재 주문 상태에서는 수행할 수 없는 작업입니다."),
	ORDER_PAYMENT_FAILED("ORDER_PAYMENT_FAILED", "결제 처리 중 오류가 발생했습니다."),
	ORDER_NOT_FOUND("ORDER_NOT_FOUND", "주문을 찾을 수 없습니다"),

	// DB연결
	DB_CONNECTION("CONNECTION_ERROR", "DB 연결 실패"), 
  DB_DRIVER_NOT_FOUND("DRIVER_NOT_FOUND", "JDBC 드라이버를 찾을 수 없습니다"),

	// 규정
	DUTY_FREE_LIQUOR_EXCEEDED("DUTY_FREE_LIQUOR_EXCEEDED", "주류 면세 한도(용량/금액) 초과"),
	DUTY_FREE_PERFUME_EXCEEDED("DUTY_FREE_PERFUME_EXCEEDED", "향수 면세 한도(용량) 초과"),
	DUTY_FREE_TOTAL_LIMIT_EXCEEDED("DUTY_FREE_TOTAL_LIMIT_EXCEEDED", "전체 기본 면세 범위($800) 초과"),

	PAYMENT_FAILED("PAYMENT_FAILED", "주문 결제 실패"), 

	DENIED_PAY("DENIED_PAY", "결제 불가 상태"), 
	DENIED_CANCEL("DENIED_CANCEL", "취소 불가 상태"),
	DENIED_PICKUP("DENIED_PICKUP", "픽업 불가 상태"),

	// 결제
	INVALID_PAYMENT_REQUEST("INVALID_PAYMENT_REQUEST", "결제 요청 정보가 올바르지 않습니다."),
	INVALID_PAYMENT_AMOUNT("INVALID_PAYMENT_AMOUNT", "결제 금액이 올바르지 않습니다."),
	INVALID_CARD_NUMBER("INVALID_CARD_NUMBER", "카드번호 형식이 올바르지 않습니다."),
	EMPTY_PAYMENT_QUEUE("EMPTY_PAYMENT_QUEUE", "결제 대기열이 비어 있습니다."),

  
	// 멤버십
	MEMBER_NOT_FOUND("MEMBER_NOT_FOUND", "회원을 찾을 수 없습니다."),
	INVALID_PURCHASE_AMOUNT("INVALID_PURCHASE_AMOUNT", "구매 금액이 올바르지 않습니다."),
	INVALID_MEMBERSHIP_GRADE("INVALID_MEMBERSHIP_GRADE", "회원 등급 정보가 올바르지 않습니다."),
	MEMBERSHIP_NOT_FOUND("MEMBERSHIP_NOT_FOUND", "멤버십 정보를 찾을 수 없습니다."),
	
 	// 인도장
 	QUEUE_EMPTY("QUEUE_EMPTY", "대기 중인 고객이 없습니다."),
 	NO_SHOW("NO_SHOW", "출국 시간이 경과하여 노쇼(No-Show) 처리된 예약입니다."),
 	
 	// 인도장관리자
 	ALREADY_LOGGED_IN("ALREADY_LOGGED_IN", "이미 로그인된 상태입니다."),
 	NOT_LOGGED_IN("NOT_LOGGED_IN", "로그인 후 이용 가능합니다."),
 	INVALID_CREDENTIAL("INVALID_CREDENTIAL", "아이디 또는 비밀번호가 올바르지 않습니다."),
    ;
	
    private final String code;
    private final String message;
    
}