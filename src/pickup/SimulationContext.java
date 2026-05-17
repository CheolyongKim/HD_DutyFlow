package pickup;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import airplane.Airplane;
import common.CurrentTime;
import common.Grade;
import member.Member;

/**
 * MLPQ 시뮬레이션 전용 컨텍스트.
 * PickUpSystem이 제공하는 모든 큐 조작·타임아웃·노쇼 로직을
 * DB 의존성 없이 순수 Java로 재구현한다.
 *
 * <p>새로 만드는 파일: 이 파일 1개
 * <p>수정하는 파일: MLPQSimulationPanel.java 1개
 */
public class SimulationContext {

    /* ── 핵심 데이터 ── */
    public MLPQ pq;
    private PickUpTicket currentTicket;
    private LocalDateTime callTime;       // 고객 호출 시각
    private boolean isCounterOpen = false;

    /* ── 타임아웃 상수 (PickUpSystem과 동일) ── */
    private static final int CALL_TIMEOUT_LIMIT = 10;  // 기본 10분

    /* ── 가상 데이터 저장소 ── */
    private List<Member> members = new ArrayList<>();
    private List<Airplane> flights = new ArrayList<>();

    /* ── 이벤트 로그 (패널에서 표시) ── */
    private final List<String> eventLog = new ArrayList<>();

    /* ================================================================
     *  생성자
     * ================================================================ */
    public SimulationContext() {
        this.pq = new MLPQ();
        this.pq.makeMLPQ(new DepartureSoonSortStrategy(), new PrioritySortStrategy());
    }

    /* ================================================================
     *  시나리오 로딩
     * ================================================================ */
    public void loadScenario(int scenarioNum) {
        resetAll();
        switch (scenarioNum) {
            case 1: loadScenario1_NormalCall();      break;
            case 2: loadScenario2_CallNoArrival();   break;
            case 3: loadScenario3_NoShow();          break;
            case 4: loadScenario4_PriorityEntry();   break;
            case 5: loadScenario5_Starvation();      break;
            case 6: loadScenario6_FlightDelay();     break;
            case 7: loadScenario7_AutoNoShow();      break;
        }
    }

    public void resetAll() {
        pq.clearAll();
        members.clear();
        flights.clear();
        currentTicket = null;
        callTime = null;
        isCounterOpen = false;
        eventLog.clear();
    }

    /* ================================================================
     *  큐 조작 — PickUpSystem에서 DB 코드만 제거한 버전
     * ================================================================ */

    /** 창구 오픈 (= 업무 시작 + 첫 고객 자동 호출) */
    public void openCounter() {
        this.isCounterOpen = true;
        log("🏢 인도장 창구 업무가 시작되었습니다. (" + CurrentTime.curTime.toLocalTime() + ")");
        tryCallNextCustomer();
    }

    /** 현재 호출된 고객 반환 */
    public PickUpTicket getCurrentTicket() {
        return currentTicket;
    }

    /** 호출 시각 반환 */
    public LocalDateTime getCallTime() {
        return callTime;
    }

    /** 현재 호출 상태 비우기 (다음 자동 호출은 하지 않음) */
    public void clearCurrentTicket() {
        currentTicket = null;
        callTime = null;
    }

    /** 큐에서 꺼내서 호출 — 내부 전용 */
    private void tryCallNextCustomer() {
        if (isCounterOpen && currentTicket == null && pq.size() > 0) {
            currentTicket = pq.pop();
            callTime = CurrentTime.curTime;
            log("📢 띵동~ [" + currentTicket.getMember().getName() + "] 고객님, 창구로 와주세요!");
        }
    }

    /* ================================================================
     *  물품 인도 처리 (processPickUp) — DB 없는 버전
     * ================================================================ */

    /**
     * 호출된 고객이 실제로 도착해서 물품을 수령하는 처리.
     * GUI에서 "호출된 티켓 클릭" = 이 메서드 호출.
     */
    public void processPickUp() {
        if (currentTicket == null) {
            log("⚠️ 현재 호출된 고객이 없습니다.");
            return;
        }

        String name = currentTicket.getMember().getName();
        String grade = currentTicket.getMember().getGrade().name();
        String flight = currentTicket.getAirplane().getFlightCode();

        log("✅ [" + name + "] (" + grade + ", " + flight + ") 물품 인도 완료! ("
                + CurrentTime.curTime.toLocalTime() + ")");

        // 창구 비움 → 다음 사람 자동 호출
        currentTicket = null;
        callTime = null;
        tryCallNextCustomer();
    }

    /* ================================================================
     *  시간 경과 (passTime) — PickUpSystem.passTime() 완전 복제
     * ================================================================ */

    /**
     * 1분 경과 + 승격(promote) + 노쇼 자동 처리 + 타임아웃 감시 + 자동 호출.
     * PickUpSystem.passTime()과 동일한 순서로 동작한다.
     */
    public void passTime() {
        pq.passTime();  // 1분 전진 + promote
        log("⏳ " + CurrentTime.curTime.toLocalTime() + " 경과...");

        // 노쇼 자동 처리
        updateNoShowState();

        // 호출 타임아웃 검사
        checkCallingTimeout();

        // 비어있으면 다음 고객 자동 호출
        tryCallNextCustomer();
    }

    /* ================================================================
     *  노쇼 처리 — PickUpSystem.updateNoShowState() + processNoShow()
     * ================================================================ */

    private void updateNoShowState() {
        // 1. 현재 호출 중인 currentTicket 검사
        if (currentTicket != null) {
            LocalDateTime departure = currentTicket.getAirplane().getDepartureAt();
            if (CurrentTime.curTime.isAfter(departure)) {
                processNoShow(currentTicket);
                currentTicket = null;
                callTime = null;
            }
        }

        // 2. 큐 내 출국 경과 티켓 일괄 처리
        List<PickUpTicket> expiredTickets = pq.getExpiredTickets(CurrentTime.curTime);
        for (PickUpTicket ticket : expiredTickets) {
            processNoShow(ticket);
        }
        if (!expiredTickets.isEmpty()) {
            pq.removeExpiredTickets(expiredTickets);
        }
    }

    /** 개별 노쇼 처리 (DB 없는 버전 — 로그만 기록) */
    public void processNoShow(PickUpTicket ticket) {
        String name = ticket.getMember().getName();
        String flight = ticket.getAirplane().getFlightCode();
        log("🛫 [NO_SHOW] " + name + " (" + flight + ") — 출국 시간 경과로 미수령 처리 완료");
    }

    /* ================================================================
     *  호출 타임아웃 — PickUpSystem.checkCallingTimeout() 완전 복제
     * ================================================================ */

    private void checkCallingTimeout() {
        if (currentTicket == null || callTime == null) return;

        long waitedMinutes = Duration.between(callTime, CurrentTime.curTime).toMinutes();

        // 기본 타임아웃 10분
        int dynamicTimeout = CALL_TIMEOUT_LIMIT;

        // 다음 대기자의 출국이 15분(10 * 1.5) 미만이면 타임아웃 5분으로 단축
        if (pq.size() > 0) {
            PickUpTicket nextTicket = pq.peek();
            long minsLeft = Duration.between(
                    CurrentTime.curTime, nextTicket.getAirplane().getDepartureAt()).toMinutes();
            if (minsLeft < (CALL_TIMEOUT_LIMIT * 1.5)) {
                dynamicTimeout = (int) (CALL_TIMEOUT_LIMIT * 0.5);
            }
        }

        // 타임아웃 초과 시 호출 취소 + 큐에서 제거
        if (waitedMinutes >= dynamicTimeout) {
            String name = currentTicket.getMember().getName();
            log("⏰ [호출 타임아웃] " + name + " 고객님 미방문으로 호출 취소 (대기 "
                    + waitedMinutes + "분, 제한 " + dynamicTimeout + "분)");

            if (dynamicTimeout < CALL_TIMEOUT_LIMIT) {
                log("   🚨 사유: 다음 대기자 출국 임박 → 골든타임 보호 발동");
            }

            pq.removeTicket(currentTicket);
            currentTicket = null;
            callTime = null;
        }
    }

    /* ================================================================
     *  항공 지연 — DB 없는 버전
     * ================================================================ */

    public void delayFlight(String flightCode, LocalDateTime newTime) {
        boolean found = false;

        // AQ 검색
        for (PickUpTicket t : pq.getAllFromAq()) {
            if (t.getAirplane().getFlightCode().equals(flightCode)) {
                t.getAirplane().setDepartureAt(newTime);
                found = true;
                break;
            }
        }
        // BQ 검색
        if (!found) {
            for (PickUpTicket t : pq.getAllFromBq()) {
                if (t.getAirplane().getFlightCode().equals(flightCode)) {
                    t.getAirplane().setDepartureAt(newTime);
                    found = true;
                    break;
                }
            }
        }
        // currentTicket도 확인
        if (!found && currentTicket != null
                && currentTicket.getAirplane().getFlightCode().equals(flightCode)) {
            currentTicket.getAirplane().setDepartureAt(newTime);
            found = true;
        }

        if (found) {
            log("✈️ [항공 지연] " + flightCode + " → " + newTime.toLocalTime() + " 으로 변경");
        } else {
            log("⚠️ 해당 항공편(" + flightCode + ") 없음");
        }
    }

    /** 큐 재정렬 — AQ/BQ 전체를 꺼냈다가 다시 삽입 */
    public void rescheduledPq() {
        if (pq.size() == 0) return;

        List<PickUpTicket> all = new ArrayList<>();
        all.addAll(pq.getAllFromAq());
        all.addAll(pq.getAllFromBq());
        pq.clearAll();

        for (PickUpTicket t : all) {
            pq.enqueue(t);
        }
        log("🔄 대기열 재정렬 완료 (현재 " + pq.size() + "명)");
    }

    /* ================================================================
     *  이벤트 로그
     * ================================================================ */

    private void log(String message) {
        eventLog.add(message);
    }

    /** 패널에서 새 로그를 가져가고 비움 */
    public List<String> drainLog() {
        List<String> copy = new ArrayList<>(eventLog);
        eventLog.clear();
        return copy;
    }

    /* ================================================================
     *  가상 멤버/항공편 조회 (주문 목록 패널용)
     * ================================================================ */
    public List<Member> getMembers() { return members; }
    public List<Airplane> getFlights() { return flights; }

    /* ================================================================
     *  시나리오별 데이터 세팅
     * ================================================================ */

    /** 기본 대기열 6명 */
    private void loadDefaultPassengers() {
        LocalDateTime now = CurrentTime.curTime;

        Member m1 = new Member(1, "김민준", "M12345678", true, Grade.GOLD);
        Member m2 = new Member(2, "박서연", "M23456789", true, Grade.PRESTIGE);
        Member m3 = new Member(3, "이도윤", "M34567890", true, Grade.SILVER);
        Member m4 = new Member(4, "최수아", "M45678901", true, Grade.BLACK);
        Member m5 = new Member(5, "정하준", "M56789012", true, Grade.SILVER);
        Member m6 = new Member(6, "강지우", "M67890123", true, Grade.GOLD);
        members.addAll(List.of(m1, m2, m3, m4, m5, m6));

        Airplane a1 = new Airplane(1, "KE081", now.plusHours(1).plusMinutes(30));
        Airplane a2 = new Airplane(2, "OZ102", now.plusMinutes(25));   // AQ
        Airplane a3 = new Airplane(3, "KE651", now.plusHours(2));
        Airplane a4 = new Airplane(4, "OZ541", now.plusMinutes(20));   // AQ
        Airplane a5 = new Airplane(5, "KE305", now.plusHours(1));
        Airplane a6 = new Airplane(6, "OZ773", now.plusHours(3));
        flights.addAll(List.of(a1, a2, a3, a4, a5, a6));

        pq.enqueue(new PickUpTicket(m1, a1, pq.nextNum(), 0));
        pq.enqueue(new PickUpTicket(m2, a2, pq.nextNum(), 0));
        pq.enqueue(new PickUpTicket(m3, a3, pq.nextNum(), 0));
        pq.enqueue(new PickUpTicket(m4, a4, pq.nextNum(), 0));
        pq.enqueue(new PickUpTicket(m5, a5, pq.nextNum(), 0));
        pq.enqueue(new PickUpTicket(m6, a6, pq.nextNum(), 0));
    }

    // ── 시나리오 1: 정상 호출 ──
    private void loadScenario1_NormalCall() {
        loadDefaultPassengers();
    }

    // ── 시나리오 2: 호출 후 미도착 ──
    private void loadScenario2_CallNoArrival() {
        loadDefaultPassengers();
        openCounter();  // 미리 한 명 호출해둠
    }

    // ── 시나리오 3: 노쇼 처리 ──
    private void loadScenario3_NoShow() {
        loadDefaultPassengers();
        openCounter();  // 호출된 상태에서 시작
    }

    // ── 시나리오 4: 우선 등급 입장 (PRESTIGE 제외한 대기열) ──
    private void loadScenario4_PriorityEntry() {
        LocalDateTime now = CurrentTime.curTime;

        Member m1 = new Member(1, "김민준", "M12345678", true, Grade.GOLD);
        Member m3 = new Member(3, "이도윤", "M34567890", true, Grade.SILVER);
        Member m5 = new Member(5, "정하준", "M56789012", true, Grade.SILVER);
        Member m6 = new Member(6, "강지우", "M67890123", true, Grade.GOLD);
        members.addAll(List.of(m1, m3, m5, m6));

        Airplane a1 = new Airplane(1, "KE081", now.plusHours(1).plusMinutes(30));
        Airplane a3 = new Airplane(3, "KE651", now.plusHours(2));
        Airplane a5 = new Airplane(5, "KE305", now.plusHours(1));
        Airplane a6 = new Airplane(6, "OZ773", now.plusHours(3));
        flights.addAll(List.of(a1, a3, a5, a6));

        pq.enqueue(new PickUpTicket(m1, a1, pq.nextNum(), 0));
        pq.enqueue(new PickUpTicket(m3, a3, pq.nextNum(), 0));
        pq.enqueue(new PickUpTicket(m5, a5, pq.nextNum(), 0));
        pq.enqueue(new PickUpTicket(m6, a6, pq.nextNum(), 0));
    }

    // ── 시나리오 5: Starvation 방지 승급 ──
    private void loadScenario5_Starvation() {
        loadDefaultPassengers();
    }

    // ── 시나리오 6: 항공 지연 → 재정렬 ──
    private void loadScenario6_FlightDelay() {
        loadDefaultPassengers();
    }

    // ── 시나리오 7: 노쇼 자동 처리 ──
    private void loadScenario7_AutoNoShow() {
        LocalDateTime now = CurrentTime.curTime;

        // 기본 멤버 + 이미 출국시간이 지난 멤버 2명 포함
        Member m1 = new Member(1, "김민준", "M12345678", true, Grade.GOLD);
        Member m2 = new Member(2, "박서연", "M23456789", true, Grade.PRESTIGE);
        Member m3 = new Member(3, "이도윤", "M34567890", true, Grade.SILVER);
        Member m7 = new Member(7, "한지연", "M77777777", true, Grade.SILVER);
        Member m8 = new Member(8, "윤태호", "M88888888", true, Grade.GOLD);
        members.addAll(List.of(m1, m2, m3, m7, m8));

        Airplane a1 = new Airplane(1, "KE081", now.plusHours(1));
        Airplane a2 = new Airplane(2, "OZ102", now.plusMinutes(25));
        Airplane a3 = new Airplane(3, "KE651", now.plusHours(2));
        // 출국 시간이 이미 지난 항공편
        Airplane a7 = new Airplane(7, "KE999", now.minusMinutes(5));
        Airplane a8 = new Airplane(8, "OZ888", now.minusMinutes(10));
        flights.addAll(List.of(a1, a2, a3, a7, a8));

        pq.enqueue(new PickUpTicket(m1, a1, pq.nextNum(), 0));
        pq.enqueue(new PickUpTicket(m2, a2, pq.nextNum(), 0));
        pq.enqueue(new PickUpTicket(m3, a3, pq.nextNum(), 0));
        pq.enqueue(new PickUpTicket(m7, a7, pq.nextNum(), 0));
        pq.enqueue(new PickUpTicket(m8, a8, pq.nextNum(), 0));
    }
}