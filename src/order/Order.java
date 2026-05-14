package order;

import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import product.Product;

@Getter
@Setter

public class Order {
	
	private OrderState state;
	
	private int flightResNum;
	private String loginId;
	private List<Map<Product, Integer>> products;
	private BigDecimal totalPrice;
	private int totalPerfume;
	private int totalAlcohol;
	private LocalDateTime orderedAt;
	private BigDecimal discountPrice; // 등급 할인 금액

	public Order() {
		this.state = new CheckState();
	}
	
	public void setOrderState(OrderState state) {
        this.state = state;
    }

    // 외부에서 호출하는 요청들 (상태 객체에 위임)
    public void requestPay() {
        state.handlePay(this);
    }

    public void requestCancel() {
        state.handleCancel(this);
    }

    public void requestPickup() {
        state.handlePickup(this);
    }

    // 결제 실패 시 호출할 직렬화 메서드 
    public void saveFailedOrder() {
        System.out.println("결제 실패: 현재 주문 상태를 직렬화하여 저장합니다...");
    }

}
