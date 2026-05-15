package main;

import java.time.LocalDateTime;
import java.util.Scanner;
import common.CurrentTime;
import common.Grade;
import member.Member;
import pickup.PickUpSystem;

public class CheolyongMain {

	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		System.out.println("==========================================");
		System.out.print("📦 물품 인도에 걸리는 시간을 설정해주세요 (분 단위, 예: 3) : ");
		int procTime = sc.nextInt();
		System.out.println("==========================================");

		try {
			LocalDateTime baseTime = LocalDateTime.of(2026, 5, 1, 9, 30, 0, 0);
			CurrentTime.curTime = baseTime;
			PickUpSystem ps = new PickUpSystem();
			ps.loadOrders();

			System.out.println("\nSYSTEM: (창구 오픈 전) 타임워프를 통해 고객들이 순차적으로 번호표를 뽑습니다...");
			CurrentTime.curTime = baseTime.minusMinutes(39); ps.appendQueue("M33333333", 3); // 김철용
			CurrentTime.curTime = baseTime.minusMinutes(25); ps.appendQueue("M44444444", 4); // 오블랙
			CurrentTime.curTime = baseTime.minusMinutes(10); ps.appendQueue("M77777777", 7); // 약블랙
			CurrentTime.curTime = baseTime.minusMinutes(5);  ps.appendQueue("M88888888", 8); // 중블랙
			CurrentTime.curTime = baseTime.minusMinutes(2);  ps.appendQueue("M99999999", 9); // 강부자
			CurrentTime.curTime = baseTime.minusMinutes(1);  ps.appendQueue("M55555555", 5); // 최골드
			CurrentTime.curTime = baseTime;                  ps.appendQueue("M66666666", 6); // 유실버
			ps.appendQueue("M11111111", 1); // 이급박 (AQ, 25분 남음)
			ps.appendQueue("M22222222", 2); // 박지각 (AQ, 15분 남음, AQ 1순위)

			// ==========================================================
			// 🎬 [시나리오] 항공편 지연 — AQ에 있는 박지각이 BQ로 강등
			// ==========================================================
			System.out.println("\n--- 🎬 [시나리오] AQ 사람의 항공편 지연 ---");
			System.out.println("[ PickUpSystem ] 지연 전 대기열 상태 (박지각이 AQ 1순위):");
			ps.printCurrentQueue();

			System.out.println("\n[ PickUpSystem ] 알림: 박지각의 OZ1015 항공편이 14:00으로 지연되었습니다.");
			ps.delayFlight("OZ1015", LocalDateTime.of(2026, 5, 1, 14, 0, 0));

			System.out.println("\n[ PickUpSystem ] 지연 후 대기열 상태 (박지각이 AQ → BQ 맨 뒤로 강등!):");
			ps.printCurrentQueue();
			System.out.println("💡 분석: 출국 임박이라 AQ에 있던 박지각이 14:00으로 밀리면서 더 이상 긴급하지 않게 됨. AQ에서 탈출해 BQ의 일반 룰(등급>번호표)을 따라 SILVER 맨 뒤로 재배치.");

			ps.openCounter();  // 박지각 강등됐으니 AQ 1순위 = 이급박 자동호출

			// 🎬 이급박 10분 노쇼 (그 동안 김철용은 49분 대기 → Starvation 임계 돌파)
			System.out.println("\n--- 🎬 [시나리오] 이급박 10분 노쇼 대기 ---");
			for (int i = 0; i < 10; i++) {
				ps.passTime();
			}
			// 09:40에 이급박 호출 취소 → 즉시 자동호출 → pop에서 김철용(49분) Starvation 가로채기

			System.out.println("\n--- 🎬 [시나리오] 노쇼 후 김철용 에이징 자동 호출 ---");
			ps.processPickUp("M33333333", 3, procTime); // 김철용 (~09:43)

			// 강부자(PRESTIGE) 호출 → 처리 중 09:45에 오블랙 40분 도달
			ps.processPickUp("M99999999", 9, procTime); // 강부자 (~09:46)

			// 강부자 처리 끝나는 09:46 시점에 오블랙 41분 → Starvation 가로채기
			System.out.println("\n--- 🎬 [시나리오] 오블랙 에이징 가로채기 ---");
			ps.processPickUp("M44444444", 4, procTime); // 오블랙

			// 이후 BQ 평시 룰: BLACK(약블랙→중블랙) → GOLD(최골드) → SILVER(유실버→박지각)
			ps.processPickUp("M77777777", 7, procTime); // 약블랙
			ps.processPickUp("M88888888", 8, procTime); // 중블랙
			ps.processPickUp("M55555555", 5, procTime); // 최골드
			ps.processPickUp("M66666666", 6, procTime); // 유실버
			ps.processPickUp("M22222222", 2, procTime); // 강등된 박지각 (BQ 맨 뒤)

			System.out.println("\n🎉 [테스트 완료] 모든 시나리오가 예외 없이 종료되었습니다.");

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			sc.close();
		}
	}
}