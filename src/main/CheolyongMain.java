package main;

import java.time.LocalDateTime;

import common.CurrentTime;
import pickup.PickUpSystem;

public class CheolyongMain {

	public static void main(String[] args) {
		try {
			// 1. 시뮬레이션 기준 시간 세팅 (2026년 5월 1일 09:30)
			LocalDateTime baseTime = LocalDateTime.of(2026, 5, 1, 9, 30, 0, 0);
			CurrentTime.curTime = baseTime;
			PickUpSystem ps = new PickUpSystem();

			// 2. DB에서 픽업 대기 중인 전체 주문 로드
			ps.loadOrders();

			// ==========================================================
			// [과거로 타임워프] 각 고객 도착 시간에 맞춰 appendQueue 호출
			// ==========================================================
			System.out.println("\nSYSTEM: 타임워프를 통해 고객 9명이 순차적으로 번호표를 뽑습니다...");
			
			CurrentTime.curTime = baseTime.minusMinutes(39); ps.appendQueue("M33333333", 3); // 김철용 (39분 전)
			CurrentTime.curTime = baseTime.minusMinutes(25); ps.appendQueue("M44444444", 4); // 오블랙 (25분 전)
			CurrentTime.curTime = baseTime.minusMinutes(22); ps.appendQueue("M55555555", 5); // 최골드 (22분 전)
			CurrentTime.curTime = baseTime.minusMinutes(20); ps.appendQueue("M66666666", 6); // 유실버 (20분 전)
			CurrentTime.curTime = baseTime.minusMinutes(10); ps.appendQueue("M77777777", 7); // 약블랙 (10분 전)
			CurrentTime.curTime = baseTime.minusMinutes(5);  ps.appendQueue("M88888888", 8); // 중블랙 (5분 전)
			CurrentTime.curTime = baseTime.minusMinutes(2);  ps.appendQueue("M99999999", 9); // 강부자 (2분 전)
			
			// 이급박 & 박지각 (현재 09:30 도착)
			CurrentTime.curTime = baseTime;
			ps.appendQueue("M11111111", 1);
			ps.appendQueue("M22222222", 2);

			System.out.println("SYSTEM: 초기 발권 완료. 현재 큐 사이즈: " + ps.pq.size() + "명");

			
			// ==========================================================
			// 🎬 [시나리오 1] 김철용의 대역전극 (Starvation 방지)
			// ==========================================================
			System.out.println("\n--- 🎬 [시나리오 1] Starvation 구제 시스템 가동 ---");
			ps.passTime(); // 1분 경과 -> 김철용 40분 대기 달성
			
			// 창구에 김철용이 와서 인도 요청을 함!
			ps.realPickUp("M33333333", 3); 


			// ==========================================================
			// [막간 타임] 원래 AQ에 있던 초임박자들(박지각, 이급박) 정상 처리
			// ==========================================================
			System.out.println("\nSYSTEM: 남아있던 출국 초임박자들이 물건을 찾아갑니다...");
			ps.realPickUp("M22222222", 2); // 박지각 탈출
			ps.realPickUp("M11111111", 1); // 이급박 탈출


			// ==========================================================
			// 🎬 [시나리오 2] 구민씨의 패닉 하이패스 (AQ 진입)
			// ==========================================================
			System.out.println("\n--- 🎬 [시나리오 2] 출국 임박자 AQ 하이패스 가동 ---");
			System.out.println("SYSTEM: 구민 고객이 도착했습니다. (출국까지 딱 30분 남음)");
			ps.appendQueue("M00000000", 10);
			
			ps.passTime(); // 1분 경과 -> 남은 시간 29분, AQ 강제 승격!
			
			// 창구에 구민이 숨을 헐떡이며 와서 인도 요청을 함!
			ps.realPickUp("M00000000", 10);


			// ==========================================================
			// 🎬 [시나리오 3] 자본주의의 맛 (평시 룰: 멤버십>번호표)
			// ==========================================================
			System.out.println("\n--- 🎬 [시나리오 3] 자본주의의 맛, VIP 평시 룰 적용 ---");
			System.out.println("SYSTEM: AQ가 비어있습니다. 평시 룰이 적용됩니다...");
			
			ps.passTime(); // 1분 경과
			
			// 창구에 프레스티지 등급 강부자가 와서 인도 요청을 함!
			ps.realPickUp("M99999999", 9);
			
		} catch (Exception e) {
			System.out.println("🚨 시스템 오류 발생: " + e.getMessage());
			e.printStackTrace();
		}
	}
}