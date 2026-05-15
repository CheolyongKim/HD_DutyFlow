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
		// 1. 시뮬레이션 기준 시간 (2026년 5월 1일 09:30)
		LocalDateTime baseTime = LocalDateTime.of(2026, 5, 1, 9, 30, 0, 0);
		PickUpSystem ps = new PickUpSystem();

		// ==========================================================
		// [과거로 타임워프] 각 고객 도착 시간에 맞춰 appendQueue 호출
		// ==========================================================
		System.out.println("SYSTEM: 타임워프를 통해 고객 9명이 순차적으로 번호표를 뽑습니다...");

		// 1) 김철용 (39분 전) - 여권: M33333333, 탑승번호: 3
		CurrentTime.curTime = baseTime.minusMinutes(39);
		ps.appendQueue("M33333333", 3);

		// 2) 오블랙 (25분 전) - 여권: M44444444, 탑승번호: 4
		CurrentTime.curTime = baseTime.minusMinutes(25);
		ps.appendQueue("M44444444", 4);

		// 3) 최골드 (22분 전) - 여권: M55555555, 탑승번호: 5
		CurrentTime.curTime = baseTime.minusMinutes(22);
		ps.appendQueue("M55555555", 5);

		// 4) 유실버 (20분 전) - 여권: M66666666, 탑승번호: 6
		CurrentTime.curTime = baseTime.minusMinutes(20);
		ps.appendQueue("M66666666", 6);

		// 5) 약블랙 (10분 전) - 여권: M77777777, 탑승번호: 7
		CurrentTime.curTime = baseTime.minusMinutes(10);
		ps.appendQueue("M77777777", 7);

		// 6) 중블랙 (5분 전) - 여권: M88888888, 탑승번호: 8
		CurrentTime.curTime = baseTime.minusMinutes(5);
		ps.appendQueue("M88888888", 8);

		// 7) 강부자 (2분 전) - 여권: M99999999, 탑승번호: 9
		CurrentTime.curTime = baseTime.minusMinutes(2);
		ps.appendQueue("M99999999", 9);

		// 8) 이급박 & 박지각 (현재 09:30 도착) - 여권: 1번, 2번
		CurrentTime.curTime = baseTime;
		ps.appendQueue("M11111111", 1);
		ps.appendQueue("M22222222", 2);

		// 현재 시간 복구 확인
		CurrentTime.curTime = baseTime;
		System.out.println("SYSTEM: 초기 발권 완료. 현재 큐 사이즈: " + ps.pq.size() + "명");
		if (ps.pq.size() == 0) {
			System.out.println("🚨 큐가 비어있습니다! DB 연결이나 예외 처리를 꼭 확인하세요.");
		}

		// ==========================================================
		// 🎬 [시나리오 1] 김철용의 대역전극 (Starvation 방지)
		// ==========================================================
		System.out.println("\n--- 🎬 [시나리오 1] Starvation 구제 시스템 가동 ---");
		System.out.println("SYSTEM: 1분이 경과합니다...");

		PickUpTicket t1 = ps.pq.oneMinutePassed(); // 김철용 40분 대기 달성

		if (t1 != null) {
			System.out.println("📢 창구 호출: [" + t1.getMember().getName() + "] 고객님!");
			System.out.println(
					"💡 실제 발동 확인: 김철용(대기 40분)이 이급박(AQ)을 이기고 0순위로 나왔는가? 👉 " + t1.getMember().getName().equals("김철용"));
		}

		// ==========================================================
		// [막간 타임] 시나리오 1 이후, 원래 AQ에 있던 초임박자들 정상 처리
		// ==========================================================
		System.out.println("\nSYSTEM: (시간 경과) 남아있던 출국 초임박자들이 물건을 찾아갑니다...");
		PickUpTicket clear1 = ps.pq.oneMinutePassed(); // 박지각 처리
		PickUpTicket clear2 = ps.pq.oneMinutePassed(); // 이급박 처리
		if (clear1 != null && clear2 != null) {
			System.out.println("✔️ 처리 완료: " + clear1.getMember().getName() + ", " + clear2.getMember().getName());
		}

		// ==========================================================
		// 🎬 [시나리오 2] 구민씨의 패닉 하이패스 (AQ 진입)
		// ==========================================================
		System.out.println("\n--- 🎬 [시나리오 2] 출국 임박자 AQ 하이패 가동 ---");

		// 구민 (출국 딱 30분 남은 상태로 도착) - 여권: M00000000, 탑승번호: 10
		System.out.println("SYSTEM: 구민 고객이 도착했습니다. (출국까지 30분 남음)");
		ps.appendQueue("M00000000", 10);

		System.out.println("SYSTEM: 1분이 경과합니다...");
		PickUpTicket t2 = ps.pq.oneMinutePassed();

		if (t2 != null) {
			System.out.println("📢 창구 호출: [" + t2.getMember().getName() + "] 고객님!");
			System.out.println(
					"💡 실제 발동 확인: 구민이 29분 진입으로 AQ 승격 후 최우선 호출되었는가? 👉 " + t2.getMember().getName().equals("구민"));
		}

		// ==========================================================
		// 🎬 [시나리오 3] 자본주의의 맛 (평시 룰: 멤버십>번호표)
		// ==========================================================
		System.out.println("\n--- 🎬 [시나리오 3] 자본주의의 맛, VIP 평시 룰 적용 ---");
		System.out.println("SYSTEM: AQ가 비어있고, 40분 대기자도 없습니다. 평시 룰이 적용되며 1분이 경과합니다...");

		PickUpTicket t3 = ps.pq.oneMinutePassed();

		if (t3 != null) {
			System.out.println("📢 창구 호출: [" + t3.getMember().getName() + "] 고객님!");
			System.out.println(
					"💡 실제 발동 확인: 번호표가 늦은 강부자가 평시 룰(프레스티지)로 1순위로 나왔는가? 👉 " + t3.getMember().getName().equals("강부자"));
		}

		try {
			// 1. 가상 시계 세팅 (DB의 픽업 데이터들이 2026-05-01 자정(00시)으로 되어있음)
			baseTime = LocalDateTime.of(2026, 5, 1, 9, 30, 0, 0);
			CurrentTime.curTime = baseTime;

			ps = new PickUpSystem();

			// [테스트 1] 주문 로드 (loadOrders)
			// 현재시간(09:30) 기준 앞뒤 3시간 조건 통과
			ps.loadOrders();

			// [테스트 2] 큐에 넣기 (appendQueue)
			System.out.println("\nSYSTEM: 고객들이 번호표를 뽑습니다...");
			CurrentTime.curTime = baseTime.minusMinutes(5);
			ps.appendQueue("M11111111", 1); // 이급박 (VIP, 일찍 옴)

			CurrentTime.curTime = baseTime; // 다시 현재 시간으로 복구
			System.out.println("SYSTEM: 현재 대기 인원 = " + ps.pq.size() + "명");

			// [테스트 3] 실제 픽업 진행 (realPickUp & updateOrderState)
			// 창구 직원이 "M11111111", 예약번호 1번의 픽업을 시도함
			ps.realPickUp("M11111111", 1);

			// 결과 확인: SQL 디벨로퍼에서 아래 쿼리를 돌려보세요!
			// SELECT orderState FROM Orders WHERE orderId = 1; -> "PICKED_UP"
			// SELECT pickedUpAt FROM Pickup WHERE orderId = 1; -> 시간 찍힘

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
