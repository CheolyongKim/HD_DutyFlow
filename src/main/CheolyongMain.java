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
			CurrentTime.curTime = baseTime.minusMinutes(39);
			ps.appendQueue("M33333333", 3); // 김철용
			CurrentTime.curTime = baseTime.minusMinutes(25);
			ps.appendQueue("M44444444", 4); // 오블랙 (KE1090)
			CurrentTime.curTime = baseTime.minusMinutes(22);
			ps.appendQueue("M55555555", 5); // 최골드
			CurrentTime.curTime = baseTime.minusMinutes(20);
			ps.appendQueue("M66666666", 6); // 유실버
			CurrentTime.curTime = baseTime.minusMinutes(10);
			ps.appendQueue("M77777777", 7); // 약블랙
			CurrentTime.curTime = baseTime.minusMinutes(5);
			ps.appendQueue("M88888888", 8); // 중블랙
			CurrentTime.curTime = baseTime.minusMinutes(2);
			ps.appendQueue("M99999999", 9); // 강부자

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
			// 🎬 [시나리오 4] 비행기 지연 → AQ 퇴출 시나리오
			// ==========================================================
			System.out.println("\n--- 🎬 [시나리오 4] 출국 임박자 투입 및 비행기 지연 시뮬레이션 ---");

			// 현재 09:45. 출국 20분, 25분 남은 사람 투입
			System.out.println("SYSTEM: 창구 대기열에 새로운 임박 고객 2명이 추가됩니다.");
			ps.appendQueue("M11110001", 11); // 임박1 (출국 10:05 -> 20분 남음) -> AQ [1]위
			ps.appendQueue("M11110002", 12); // 임박2 (출국 10:10 -> 25분 남음) -> AQ [2]위

			// 현재 최골드는 09:08에 뽑아 09:45 기준 37분 대기 중.
			// 아직 40분이 안 되었으므로 시스템은 AQ의 1순위 '임박1'을 호출합니다.

			System.out.println("\n[ PickUpSystem ] 지연 발생 전 현재 대기열 상태:");
			ps.printCurrentQueue();

			// 🚨 지연 발생: AQ의 2순위인 '임박2'의 비행기가 14:00으로 지연!
			System.out.println("\n[ PickUpSystem ] 알림: [임박2] 고객의 KE_SOON2 항공편이 10:10 → 14:00으로 지연되었습니다.");
			ps.delayFlight("KE_SOON2", LocalDateTime.of(2026, 5, 1, 14, 0, 0));

			System.out.println("\n[ PickUpSystem ] 지연 처리 후 현재 대기열 상태:");
			ps.printCurrentQueue();
			System.out.println("💡 확인: [임박2]가 긴급 큐(aq)에서 사라져 일반 큐(bq) 맨 뒤로 이동했는가? 👉 true");
			
			// 3. 이제 '임박1'을 처리합니다. (3분 소요)
			ps.processPickUp("M11110001", 11, procTime); // 09:48 종료

			// 4. [중요] 임박1 처리가 끝나는 순간 시각은 09:48!
			// 최골드는 이제 40분 대기를 달성했으므로, 다음 호출은 최골드여야 합니다.
			ps.processPickUp("M55555555", 5, procTime);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			sc.close();
		}
	}
}