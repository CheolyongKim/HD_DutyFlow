package member;

import java.time.LocalDate;

import cart.Cart;

public class Member {
	private String loginId;
	private String password;
	private String name;
	private LocalDate birthDate;
	private String phoneNumber;
	private String passportNum;
	private LocalDate passportExpiredDate;
	private boolean isAdult;
	private Grade grade;
	private Cart cart = new Cart();
	
	public Cart getCart() {
		return cart;
	}
}