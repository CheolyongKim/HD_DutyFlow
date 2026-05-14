package member;

import java.time.LocalDate;

import lombok.Data;
import shoppingCart.ShoppingCart;

@Data
public class Member {
	private int memberId;
	private String loginId;
	private String password;
	private String name;
	private LocalDate birthDate;
	private String phoneNumber;
	private String passportNum;
	private LocalDate passportExpiredDate;
	private boolean isAdult;
	private Grade grade;
	private ShoppingCart cart = new ShoppingCart();
	
	public ShoppingCart getCart() {
		return cart;
	}
}
