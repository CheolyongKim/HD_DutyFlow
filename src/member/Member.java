package member;

import java.time.LocalDate;

import common.Grade;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import shoppingCart.ShoppingCart;

@AllArgsConstructor
@NoArgsConstructor
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
	private LocalDate gradeSelectionDate;
	private ShoppingCart cart = new ShoppingCart();
	
	public ShoppingCart getCart() {
		return cart;
	}

}
