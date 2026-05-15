package main;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Scanner;
import common.CurrentTime;
import pickup.PickUpSystem;

public class CheolyongMain {

	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);

		System.out.println("==========================================");
		System.out.print("📦 물품 인도에 걸리는 시간을 설정해주세요 (분 단위, 예: 4) : ");
		int procTime = sc.nextInt();
		System.out.println("==========================================");

		try {
			// 1. 시뮬레이션 가상 시계 세팅
			LocalDateTime baseTime = LocalDateTime.of(2026, 5, 1, 9, 30, 0, 0);
			CurrentTime.curTime = baseTime;

			PickUpSystem ps = new PickUpSystem();
			ps.loadOrders(); 

			// 2. 창구 오픈 전 타임워프 큐 삽입 (기계에서 번호표만 뽑음)
			System.out.println("\nSYSTEM: (창구 오픈 전) 타임워프를 통해 고객들이 순차적으로 번호표를 뽑습니다...");
			CurrentTime.curTime = baseTime.minusMinutes(39); ps.appendQueue("M33333333", 3); // 김철용
			CurrentTime.curTime = baseTime.minusMinutes(25); ps.appendQueue("M44444444", 4); // 오블랙 (KE1090)
			CurrentTime.curTime = baseTime.minusMinutes(22); ps.appendQueue("M55555555", 5); // 최골드
			CurrentTime.curTime = baseTime.minusMinutes(20); ps.appendQueue("M66666666", 6); // 유실버
			CurrentTime.curTime = baseTime.minusMinutes(10); ps.appendQueue("M77777777", 7); // 약블랙
			CurrentTime.curTime = baseTime.minusMinutes(5);  ps.appendQueue("M88888888", 8); // 중블랙
			CurrentTime.curTime = baseTime.minusMinutes(2);  ps.appendQueue("M99999999", 9); // 강부자
			
			// 09:30 기준 도착자
			CurrentTime.curTime = baseTime;
			ps.appendQueue("M11111111", 1); // 이급박
			ps.appendQueue("M22222222", 2); // 박지각 (AQ 1순위)

			System.out.println("SYSTEM: 초기 발권 완료. 현재 큐 사이즈: " + ps.pq.size() + "명");

			// ==========================================================
			// 🚀 본격적인 시뮬레이션 시작!
			// ==========================================================

			// 1. 창구 오픈 (박지각 자동 호출)
			ps.openCounter();

			// 2. 박지각 처리 (그 사이 김철용 40분 대기 달성)
			ps.processPickUp("M22222222", 2, procTime);

			// ==========================================================
			// 🎬 [시나리오 1] Starvation 구제 시스템 가동
			// ==========================================================
			System.out.println("\n--- 🎬 [시나리오 1] Starvation 구제 시스템 가동 ---");
			// 박지각 업무 종료 후 시스템이 0순위(Starvation)인 '김철용'을 자동 호출
			ps.processPickUp("M33333333", 3, procTime); 

			// ==========================================================
			// [막간 타임] 남은 초임박자 이급박 처리
			// ==========================================================
			System.out.println("\n--- 🎬 남은 초임박자 처리 ---");
			ps.processPickUp("M11111111", 1, procTime);

			// ==========================================================
			// 🎬 [시나리오 3] 자본주의의 맛, VIP 평시 룰 자동 발동
			// ==========================================================
			System.out.println("\n--- 🎬 [시나리오 3] 자본주의의 맛, VIP 평시 룰 자동 발동 ---");
			
			// 강부자(PRESTIGE)가 호출된 사이, 구민 고객 도착
			System.out.println("\n--- 🎬 [시나리오 2] 출국 임박자 AQ 하이패스 가동 ---");
			System.out.println("SYSTEM: 구민 고객이 도착하여 조용히 번호표를 뽑습니다.");
			ps.appendQueue("M00000000", 10); 

			// 호출된 강부자 처리
			ps.processPickUp("M99999999", 9, procTime);

			// ==========================================================
			// 🎬 [시나리오 2 결과] AQ 하이패스의 위력
			// ==========================================================
			// 강부자 처리 중 구민의 출국 시간이 30분 미만이 되어 AQ로 승격, 즉시 호출
			ps.processPickUp("M00000000", 10, procTime);

			// ==========================================================
			// 🎬 [시나리오 4] 비행기 지연 → PQ 자동 재정렬
			// ==========================================================
			System.out.println("\n--- 🎬 [시나리오 4] KE1090 항공편 지연 발생 시뮬레이션 ---");
			
			// 지연 전 상태 출력 (오블랙이 상단에 위치)
			System.out.println("[ PickUpSystem ] 지연 발생 전 현재 대기열 상태:");
			ps.printCurrentQueue();

			System.out.println("\n[ PickUpSystem ] 알림: KE1090 항공편이 11:00 → 14:00으로 지연되었습니다.");
			// 지연 발생 시 옵저버 패턴에 의해 rescheduledPq()가 자동 호출됨
			ps.delayFlight("KE1090", LocalDateTime.of(2026, 5, 1, 14, 0, 0));

			// 지연 후 상태 출력 (오블랙이 뒤로 밀려남)
			System.out.println("\n[ PickUpSystem ] 지연 처리 후 현재 대기열 상태:");
			ps.printCurrentQueue();
			
			// ==========================================================
			// [막간] 나머지 대기자 처리 (오블랙 등급 순)
			// ==========================================================
			System.out.println("\n--- 🎬 남은 대기자 VIP 룰 처리 시작 ---");
			ps.processPickUp("M44444444", 4, procTime); // 지연되었지만 여전히 블랙 등급이므로 순서대로 처리

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			sc.close();
		}
	}
}