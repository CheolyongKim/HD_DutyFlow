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
	
	// 장바구니
	INVALID_QUANTITY("INVALID_QUANTITY", "수량은 1개 이상이어야 합니다."),
	PRODUCT_NOT_FOUND("PRODUCT_NOT_FOUND", "상품 정보를 찾을 수 없습니다."),
	CART_PRODUCT_NOT_FOUND("CART_PRODUCT_NOT_FOUND","장바구니에 존재하지 않는 상품입니다."),
	INVALID_PRODUCT_PRICE("INVALID_PRODUCT_PRICE", "상품 가격 정보가 올바르지 않습니다."),
	EMPTY_SELECTED_PRODUCTS("EMPTY_SELECTED_PRODUCTS", "선택된 상품이 없습니다."),
	;

	// DB연결
	DB_CONNECTION("CONNECTION_ERROR","DB 연결 실패"),
    DB_DRIVER_NOT_FOUND("DRIVER_NOT_FOUND", "JDBC 드라이버를 찾을 수 없습니다");
	
    private final String code;
    private final String message;
    
}