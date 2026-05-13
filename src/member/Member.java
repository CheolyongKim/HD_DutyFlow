package member;

import java.time.LocalDate;

import cart.Cart;

public class Member {
	private String loginId;
	private String pwd;
	private String memberName;
	private LocalDate birthdate;
	private String phoneNum;
	private String passportNum;
	private LocalDate passportExpiredDate;
	private boolean isAdult;
	private Membership membership;
	private Cart cart;
}
