package main;

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
			ps.loadOrders(); // (주의: OrderDAO에서 앞뒤 24시간 로드하게 수정한 것 유지!)

			// 2. 창구 오픈 전 타임워프 큐 삽입 (기계에서 번호표만 뽑음)
			System.out.println("\nSYSTEM: (창구 오픈 전) 타임워프를 통해 고객들이 순차적으로 번호표를 뽑습니다...");
			CurrentTime.curTime = baseTime.minusMinutes(39);
			ps.appendQueue("M33333333", 3); // 김철용
			CurrentTime.curTime = baseTime.minusMinutes(25);
			ps.appendQueue("M44444444", 4); // 오블랙
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
			ps.appendQueue("M11111111", 1); // 이급박 (출국 25분 남음 -> AQ)
			ps.appendQueue("M22222222", 2); // 박지각 (출국 15분 남음 -> AQ 1순위)

			System.out.println("SYSTEM: 초기 발권 완료. 현재 큐 사이즈: " + ps.pq.size() + "명");

			// ==========================================================
			// 🚀 본격적인 시뮬레이션 시작!
			// ==========================================================

			// 1. 창구를 오픈하면 밀려있던 1순위를 시스템이 똑똑하게 호출합니다. (박지각이 불림)
			ps.openCounter();

			// 2. 직원이 호출된 박지각을 처리합니다. 처리하느라 설정한 시간(예: 4~5분)이 흐릅니다.
			ps.processPickUp("M22222222", 2, procTime);

			// ==========================================================
			// 🎬 [시나리오 1] Starvation 구제 시스템 가동
			// ==========================================================
			System.out.println("\n--- 🎬 [시나리오 1] Starvation 구제 시스템 가동 ---");

			// 박지각을 처리하는 동안 시간이 흘러 김철용이 대기시간 40분을 초과했습니다!
			// 그래서 박지각 업무가 끝나자마자 시스템이 '김철용'을 자동 호출했습니다.
			ps.processPickUp("M33333333", 3, procTime); // 김철용 고객 물품 인도

			// ==========================================================
			// [막간 타임] 남은 초임박자 이급박 처리
			// ==========================================================
			System.out.println("\n--- 🎬 남은 초임박자 처리 ---");
			ps.processPickUp("M11111111", 1, procTime);

			// 💡 이급박 처리가 끝나자마자, 급한 임박자(AQ)가 텅 비었으므로
			// 시스템은 대기자 중 등급이 가장 높은 VVIP 강부자(PRESTIGE)를 알아서 호출했습니다!

			// ==========================================================
			// 🎬 [시나리오 3] 자본주의의 맛, VIP 평시 룰 우선 적용!
			// ==========================================================
			System.out.println("\n--- 🎬 [시나리오 3] 자본주의의 맛, VIP 평시 룰 자동 발동 ---");

			// 강부자가 창구로 걸어가는 사이, 구민이 헐레벌떡 도착합니다.
			System.out.println("\n--- 🎬 [시나리오 2] 출국 임박자 AQ 하이패스 가동 ---");
			System.out.println("SYSTEM: 구민 고객이 도착하여 조용히 번호표를 뽑습니다.");
			ps.appendQueue("M00000000", 10);
			// (현재 09:39. 구민의 출국 시간은 10:00이므로 남은 시간 21분 -> 뽑자마자 즉시 AQ 진입!)

			// 직원은 이미 호출되어 창구에 온 VIP 강부자의 물품을 먼저 내어줍니다.
			ps.processPickUp("M99999999", 9, procTime);

			// ==========================================================
			// 🎬 [시나리오 2 결과] AQ 하이패스의 위력
			// ==========================================================
			// 강부자 처리가 끝나자마자 시스템은 BQ에 남아있는 오블랙(BLACK) 등급들을 무시하고,
			// 방금 전 AQ로 직행했던 초임박자 '구민'을 최우선으로 부릅니다!
			ps.processPickUp("M00000000", 10, procTime);

			// ==========================================================
			// [막간] 나머지 대기자 처리 (등급 순)
			// ==========================================================
			System.out.println("\n--- 🎬 남은 대기자 VIP 룰 처리 ---");
			// 구민까지 보내고 나면, 다시 BQ 룰에 따라 다음 VIP인 블랙 등급들이 순서대로 불립니다.
			ps.processPickUp("M44444444", 4, procTime); // 오블랙 인도

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			sc.close();
		}
	}
}