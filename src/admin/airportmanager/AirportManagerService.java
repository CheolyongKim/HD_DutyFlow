package admin.airportmanager;

import admin.airportmanager.dto.AirportManagerLoginDto;
import exception.BusinessException;
import exception.ErrorCode;

public class AirportManagerService {
	
	private final AirportManagerDao airportManagerDao;
	
	private AirportManagerLoginDto airportManagerLoginDto;
	 
	public AirportManagerService(AirportManagerDao airportManagerDao) {
		this.airportManagerDao = airportManagerDao;
	}
	
	// 로그인 
	public void login(int managerId, String password) {
		 
		if (airportManagerLoginDto != null) {
			throw new BusinessException(ErrorCode.ALREADY_LOGGED_IN,
				new Exception("이미 로그인된 상태입니다. 현재 관리자: " + airportManagerLoginDto.getManagerName()));
		}
 
		AirportManagerLoginDto result = airportManagerDao.getLoginInfo(managerId, password);
 
		if (result == null) {
			throw new BusinessException(ErrorCode.INVALID_CREDENTIAL,
				new Exception("관리자 ID 또는 비밀번호가 올바르지 않습니다."));
		}
 
		// 로그인 성공
		this.airportManagerLoginDto = result;
		System.out.println("[로그인] 안녕하세요, " + result.getManagerName() + result.getManagerType()+ " 관리자님");
	}
	
	// 로그아웃 
	public void logout() {
	    checkLoggedIn(); // 미로그인 시 NOT_LOGGED_IN 예외

	    System.out.println("[로그아웃] "
	        + airportManagerLoginDto.getManagerName() + " 관리자님 로그아웃되었습니다.");

	    this.airportManagerLoginDto = null; // 세션 초기화
	}
	
	private void checkLoggedIn() {
        if (airportManagerLoginDto == null) {
            throw new BusinessException(ErrorCode.NOT_LOGGED_IN,
                new Exception("로그인 후 이용 가능합니다."));
        }
    }

	

}
