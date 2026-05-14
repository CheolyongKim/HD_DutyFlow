package main;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import airplane.Airplane;
import common.CurrentTime;
import member.Grade;
import member.Member;
import pickup.PickUpSystem;
import pickup.PickUpTicket;
import shoppingCart.ShoppingCart;

public class CheolyongMain {

	public static void main(String[] args) {
		// 초기 세팅
		/*
		 * aq에는 고객 적고 bq에는 고 등급 고객이 많은 상황,
		 * bq에는 꽤 오래 전에 김철용씨가 들어가 있음
		 * (세부적으로 - 김철용씨는 번호표를 1번으로 뽑았음에도 등급이 실버라 39분째 대기중)
		 */
		PickUpSystem ps = new PickUpSystem();
		LocalDateTime t = CurrentTime.curTime;
		Airplane air1 = new Airplane(1, "KE1025", t.plusMinutes(25));
		Airplane air2 = new Airplane(2, "OZ1015", t.plusMinutes(15));
		Airplane air3 = new Airplane(3, "7C1050", t.plusMinutes(50));
		Airplane air4 = new Airplane(4, "KE1090", t.plusMinutes(90));
		Airplane air5 = new Airplane(5, "OZ1100", t.plusMinutes(100));
		Airplane air6 = new Airplane(6, "LJ1080", t.plusMinutes(80));
		Airplane air7 = new Airplane(7, "KE1120", t.plusMinutes(120));
		Airplane air8 = new Airplane(8, "OZ1160", t.plusMinutes(160));
		Airplane air9 = new Airplane(9, "KE1150", t.plusMinutes(150));
		//Airplane air10 = new Airplane(10, "TW1030", t.plusMinutes(30));
		// 공통으로 사용할 여권 만료일 (시뮬레이션 기준일 + 1000일)
        LocalDate expireDate = LocalDate.of(2029, 1, 25);

        // Member 객체 생성 (순서: id, loginId, password, name, birth, phone, passport, expire, isAdult, grade, cart)
        Member member1 = new Member(1, "user_lee", "pass1234", "이급박", 
            LocalDate.of(1992, 5, 15), "010-1111-2222", "M11111111", expireDate, true, Grade.GOLD, new ShoppingCart());
            
        Member member2 = new Member(2, "user_park", "pass1234", "박지각", 
            LocalDate.of(1995, 8, 20), "010-2222-3333", "M22222222", expireDate, true, Grade.SILVER, new ShoppingCart());
            
        Member member3 = new Member(3, "user_kim", "pass1234", "김철용", 
            LocalDate.of(1988, 11, 11), "010-3333-4444", "M33333333", expireDate, true, Grade.SILVER, new ShoppingCart());
            
        Member member4 = new Member(4, "user_oh", "pass1234", "오블랙", 
            LocalDate.of(1985, 3, 1), "010-4444-5555", "M44444444", expireDate, true, Grade.BLACK, new ShoppingCart());
            
        Member member5 = new Member(5, "user_choi", "pass1234", "최골드", 
            LocalDate.of(1990, 7, 7), "010-5555-6666", "M55555555", expireDate, true, Grade.GOLD, new ShoppingCart());
            
        Member member6 = new Member(6, "user_yoo", "pass1234", "유실버", 
            LocalDate.of(1998, 12, 25), "010-6666-7777", "M66666666", expireDate, true, Grade.SILVER, new ShoppingCart());
            
        Member member7 = new Member(7, "user_yak", "pass1234", "약블랙", 
            LocalDate.of(1982, 4, 14), "010-7777-8888", "M77777777", expireDate, true, Grade.BLACK, new ShoppingCart());
            
        Member member8 = new Member(8, "user_joong", "pass1234", "중블랙", 
            LocalDate.of(1979, 9, 9), "010-8888-9999", "M88888888", expireDate, true, Grade.BLACK, new ShoppingCart());
            
        Member member9 = new Member(9, "user_kang", "pass1234", "강부자", 
            LocalDate.of(1975, 1, 1), "010-9999-0000", "M99999999", expireDate, true, Grade.PRESTIGE, new ShoppingCart());
            
        Member member10 = new Member(10, "user_gu", "pass1234", "구민", 
            LocalDate.of(2000, 10, 10), "010-0000-1111", "M00000000", expireDate, true, Grade.SILVER, new ShoppingCart());

        
        		// 1. 시뮬레이션 기준 시간 (2026년 5월 1일 09:30)
        		LocalDateTime baseTime = LocalDateTime.of(2026, 5, 1, 9, 30, 0, 0);

        		// ==========================================================
        		// [과거로 타임워프] 각 고객이 실제로 인도장에 도착했던 시간으로 조작
        		// ==========================================================

        		// 1) 김철용 (39분 전 도착 -> 대기 39분)
        		CurrentTime.curTime = baseTime.minusMinutes(39);
        		ps.pq.enqueue(air3, member3);

        		// 2) 오블랙 (25분 전 도착 -> 대기 25분)
        		CurrentTime.curTime = baseTime.minusMinutes(25);
        		ps.pq.enqueue(air4, member4);

        		// 3) 최골드 (22분 전 도착 -> 대기 22분)
        		CurrentTime.curTime = baseTime.minusMinutes(22);
        		ps.pq.enqueue(air5, member5);

        		// 4) 유실버 (20분 전 도착 -> 대기 20분)
        		CurrentTime.curTime = baseTime.minusMinutes(20);
        		ps.pq.enqueue(air6, member6);

        		// 5) 약블랙 (10분 전 도착 -> 대기 10분)
        		CurrentTime.curTime = baseTime.minusMinutes(10);
        		ps.pq.enqueue(air7, member7);

        		// 6) 중블랙 (5분 전 도착 -> 대기 5분)
        		CurrentTime.curTime = baseTime.minusMinutes(5);
        		ps.pq.enqueue(air8, member8);

        		// 7) 강부자 (2분 전 도착 -> 대기 2분)
        		CurrentTime.curTime = baseTime.minusMinutes(2);
        		ps.pq.enqueue(air9, member9);

        		// 8) 이급박 & 박지각 (방금 도착 -> 대기 0분, 바로 AQ 직행)
        		CurrentTime.curTime = baseTime;
        		ps.pq.enqueue(air1, member1);
        		ps.pq.enqueue(air2, member2);

        		// ==========================================================
        		// [현재로 복귀] 10명의 발권이 끝난 후, 시계를 다시 09:30으로 맞춤
        		// ==========================================================
        		CurrentTime.curTime = baseTime;

        		// (참고) 구민(member10)은 시나리오 흐름상 '김철용 처리 이후'에 도착하는 설정이므로
        		// 처음 1~9번 세팅할 때는 빼두었다가, 시나리오 2가 시작될 때 enqueue 하는 것이 더 자연스럽습니다!

        
		
        	// 시나리오 1
     		/*
     		 * bq에서 멤버십에 밀려서 한참 대기하던
     		 * 김철용씨가 starvation 방지 덕분에 0순위로 poll 되는 시나리오
     		 */
     		System.out.println("\n--- 🎬 [시나리오 1] Starvation 구제 시스템 가동 ---");
     		System.out.println("SYSTEM: 1분이 경과합니다...");
     		PickUpTicket scenario1Ticket = ps.pq.oneMinutePassed(); // 김철용 대기 40분 달성
     		
     		if (scenario1Ticket != null) {
     			System.out.println("📢 창구 호출: [" + scenario1Ticket.getMember().getName() + "] 고객님!");
     			System.out.println("💡 호출 사유: 대기시간 40분 초과로 인한 0순위 자동 호출 (Starvation 방지)");
     		}


     		// 시나리오 2
     		/*
     		 * (시나리오 1 진행 이후)
     		 * 구민 씨의 번호표가 발행 됨 - 25번, 출국시간 정확히 30분 남았음
     		 * 멤버십이 아닌 구민씨가 출국시간이 임박한채로 인도장에 도착해
     		 * aq로 넘어가 빠르게 poll되는 시나리오
     		 */
     	// ==========================================================
    		// [막간 타임] 시나리오 1 이후, 원래 AQ에 있던 초임박자들 정상 처리
    		// ==========================================================
    		System.out.println("\nSYSTEM: (시간 경과) 남아있던 출국 초임박자들이 물건을 찾아갑니다...");
    		PickUpTicket clear1 = ps.pq.oneMinutePassed(); // 박지각 처리
    		PickUpTicket clear2 = ps.pq.oneMinutePassed(); // 이급박 처리
    		System.out.println("✔️ 처리 완료: " + clear1.getMember().getName() + ", " + clear2.getMember().getName());
    		System.out.println("✔️ 현재 AQ 상태: 텅 빔");
    		
    		// ==========================================================
    		// 🎬 [시나리오 2] 구민씨의 패닉 하이패스 (AQ 진입)
    		// ==========================================================
    		System.out.println("\n--- 🎬 [시나리오 2] 출국 임박자 AQ 하이패스 가동 ---");
    		
    		// 1. 구민 씨 인도장 도착 (현재 시계 기준 딱 30분 뒤 비행기)
    		System.out.println("SYSTEM: 구민 고객이 숨을 헐떡이며 도착했습니다.");
    		Airplane air10 = new Airplane(10, "TW1030", CurrentTime.curTime.plusMinutes(30));
    		
    		// 2. 발권 및 Enqueue (30분 미만이 아니므로 일단 BQ로 들어감)
    		ps.pq.enqueue(air10, member10);
    		System.out.println("SYSTEM: 구민 고객 발권 완료. (출국까지 남은 시간: 딱 30분 -> BQ 대기)");

    		// 3. 운명의 1분 경과
    		System.out.println("SYSTEM: 1분이 경과합니다...");
    		PickUpTicket scenario2Ticket = ps.pq.oneMinutePassed();
    		/* 
    		 * [내부 로직 작동 순서]
    		 * 1) 시간 1분 증가 -> 구민 남은시간 29분 됨
    		 * 2) promote() 발동 -> 구민이 BQ에서 AQ로 승격!
    		 * 3) handleStarvation() -> 해당자 없음
    		 * 4) pop() -> AQ에 방금 들어온 구민이 즉시 호출됨!
    		 */
    		
    		if (scenario2Ticket != null) {
    			System.out.println("📢 창구 호출: [" + scenario2Ticket.getMember().getName() + "] 고객님!");
    			System.out.println("💡 호출 사유: 출국시간 30분 미만(29분) 진입으로 인한 AQ 강제 승격 및 최우선 호출");
    		}

     		// 시나리오 3
     		/*
     		 * (시나리오 2 진행 이후)
     		 * aq에는 사람이 거의 다 빠졌음
     		 * 인도를 기다리는 남은 사람들이 출국시간이 여유로운 사람들 뿐이라
     		 * 프레스티지등급인 강부자씨가 거의 1순위로 poll되는 시나리오
     		 */
    		// ==========================================================
    		// 🎬 [시나리오 3] 자본주의의 맛 (평시 룰: 멤버십>번호표)
    		// ==========================================================
    		System.out.println("\n--- 🎬 [시나리오 3] 자본주의의 맛, VIP 평시 룰 적용 ---");
    		System.out.println("SYSTEM: AQ가 비어있고, 40분 대기자도 없습니다. 평시 룰이 적용되며 1분이 경과합니다...");
    		
    		// 운명의 1분 경과 (BQ에 남아있는 사람들끼리 경쟁)
    		PickUpTicket scenario3Ticket = ps.pq.oneMinutePassed();
    		/* 
    		 * [남은 BQ 경쟁자 상황]
    		 * 오블랙 (블랙, 가장 일찍 옴) vs 최골드 (골드) vs ... vs 강부자 (프레스티지, 가장 늦게 옴)
    		 * 멤버십 정렬 우선순위로 인해 번호표가 가장 늦은 강부자가 최상단에 위치함
    		 */

    		if (scenario3Ticket != null) {
    			System.out.println("📢 창구 호출: [" + scenario3Ticket.getMember().getName() + "] 고객님!");
    			System.out.println("💡 호출 사유: BQ 평시 룰 적용 (프레스티지 등급 최우선)");
    		}
	}

}
