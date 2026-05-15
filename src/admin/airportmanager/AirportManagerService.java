package admin.airportmanager;

import java.time.LocalDate;
import java.util.List;

import admin.airportmanager.dto.AirportManagerLoginDto;
import admin.airportmanager.dto.PickUpListDTO;
import exception.BusinessException;
import exception.ErrorCode;
import member.Member;

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
	
	// 전체 픽업 목록 처리
	public void printAllPickUpList() {
		checkLoggedIn();
		
		List<PickUpListDTO> list = airportManagerDao.getAllPickUpList();
		System.out.println("===== [전체 픽업 목록] =====");
        printList(list);
	}
	
	// 특정 회원 픽업 목록 출력
	public void printAllPickUpList(Member member) {
        checkLoggedIn();

        if (member == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT,
                new Exception("Member 객체가 null입니다."));
        }

        List<PickUpListDTO> list = airportManagerDao.getAllPickUpListByMember(member);

        System.out.println("===== [회원별 픽업 목록] 회원ID: "
            + member.getMemberId() + " =====");
        printList(list);
    }
	
	// 기간별 픽업 목록 출력
	public void printAllPickUpList(LocalDate start, LocalDate end) {
		checkLoggedIn();

        if (start == null || end == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT,
                new Exception("start 또는 end 날짜가 null입니다."));
        }
        if (start.isAfter(end)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT,
                new Exception("start 날짜가 end 날짜보다 늦을 수 없습니다."));
        }

        List<PickUpListDTO> list = airportManagerDao.getAllPickUpListByDateRange(start, end);

        System.out.println("===== [기간별 픽업 목록] "
            + start + " ~ " + end + " =====");
        printList(list);
    }
	
	private void printList(List<PickUpListDTO> list) {
        if (list.isEmpty()) {
            System.out.println("  조회된 픽업 내역이 없습니다.");
            return;
        }

        int no = 1;
        for (PickUpListDTO dto : list) {
            System.out.printf(
                "  [%d] 픽업ID: %-4d | 회원: %-10s | 항공편: %-8s | "
                + "픽업가능: %-20s | 상태: %-15s | 실제픽업: %s%n",
                no++,
                dto.getPickupId(),
                dto.getMemberName(),
                dto.getFlightCode(),
                dto.getPickupAvailableAt(),
                dto.getOrderState(),
                dto.getPickedUpAt() != null ? dto.getPickedUpAt().toString() : "미완료"
            );
        }
        System.out.println("  총 " + list.size() + "건");
    }
	
	private void checkLoggedIn() {
        if (airportManagerLoginDto == null) {
            throw new BusinessException(ErrorCode.NOT_LOGGED_IN,
                new Exception("로그인 후 이용 가능합니다."));
        }
    }

	

}
