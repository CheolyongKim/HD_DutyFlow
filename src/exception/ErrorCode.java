package exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

	// 공통
	INVALID_INPUT("INVALID_INPUT", "잘못된 입력입니다."),
	DATA_NOT_FOUND("DATA_NOT_FOUND", "데이터를 찾을 수 없습니다."),
	DUPLICATE_DATA("DUPLICATE_DATA", "이미 존재하는 데이터입니다."),
	UNAUTHORIZED("UNAUTHORIZED", "로그인이 필요합니다."),
	ACCESS_DENIED("ACCESS_DENIED", "접근 권한이 없습니다."),
	ILLEGAL_STATE("ILLEGAL_STATE", "시스템 검증 과정에 오류가 존재합니다."), // 이전에서 검증되었어야 할 조건을 만족하지 못했을 때
	
	// 장바구니
	INVALID_QUANTITY("INVALID_QUANTITY", "수량은 1개 이상이어야 합니다."),
	CART_PRODUCT_NOT_FOUND("CART_PRODUCT_NOT_FOUND","장바구니에 존재하지 않는 상품입니다."),
	INVALID_PRODUCT_PRICE("INVALID_PRODUCT_PRICE", "상품 가격 정보가 올바르지 않습니다."),
	EMPTY_SELECTED_PRODUCTS("EMPTY_SELECTED_PRODUCTS", "선택된 상품이 없습니다."),

	// DB연결
	DB_CONNECTION("CONNECTION_ERROR","DB 연결 실패"),
    DB_DRIVER_NOT_FOUND("DRIVER_NOT_FOUND", "JDBC 드라이버를 찾을 수 없습니다"),
    
 	// 인도장
 	QUEUE_EMPTY("QUEUE_EMPTY", "대기 중인 고객이 없습니다."),
 	NO_SHOW("NO_SHOW", "출국 시간이 경과하여 노쇼(No-Show) 처리된 예약입니다."),
    
	
	// 재고
	STOCK_NOT_ENOUGH("S001", "재고가 부족합니다."),
	INVALID_STOCK_AMOUNT("S002", "재고 수량은 1개 이상이어야 합니다."),
	STOCK_NOT_FOUND("S003", "재고 정보를 찾을 수 없습니다."),
	STOCK_UPDATE_FAILED("S004", "재고 수정에 실패했습니다."),
	
	// 발주
	PURCHASE_NOT_FOUND("P001", "발주 정보를 찾을 수 없습니다."),
	INVALID_PURCHASE_AMOUNT("P002", "발주 수량은 1개 이상이어야 합니다."),
	INVALID_PURCHASE_STATUS("P003", "현재 상태에서는 발주를 처리할 수 없습니다."),
	PURCHASE_REQUEST_FAILED("P004", "발주 요청에 실패했습니다."),
	PURCHASE_CANCEL_FAILED("P005", "발주 취소에 실패했습니다."),
	PURCHASE_RECEIVE_FAILED("P006", "발주 입고 처리에 실패했습니다."),
	
	// 상품
	PRODUCT_NOT_FOUND("PRODUCT_NOT_FOUND", "상품 정보를 찾을 수 없습니다."),
	PRODUCT_ALREADY_EXISTS("PR002", "이미 등록된 상품입니다."),
	NOT_MY_BRAND_PRODUCT("PR003", "해당 브랜드의 상품이 아닙니다."),
	INVALID_PRODUCT_INPUT("PR004", "상품 입력값이 올바르지 않습니다."),
	
	// 파일
	FILE_SAVE_FAILED("FILE_SAVE_FAILED", "파일 저장 중 오류가 발생했습니다."),
	;
    private final String code;
    private final String message;
    
}