package pickup; 

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import common.CurrentTime;
import common.Grade;
import member.Member;
import airplane.Airplane;

public class SimulationContext {

    public MLPQ pq; // 
    private PickUpTicket currentTicket; // 
    private boolean isCounterOpen = false; // [cite: 127]

    // 현재 시나리오에 등장하는 가상 데이터 (조회용)
    private List<Member> members = new ArrayList<>(); // [cite: 127]
    private List<Airplane> flights = new ArrayList<>(); // [cite: 128]

    public SimulationContext() {
        this.pq = new MLPQ(); // [cite: 128]
        this.pq.makeMLPQ(new DepartureSoonSortStrategy(), new PrioritySortStrategy()); // [cite: 128]
    }

    public void loadScenario(int scenarioNum) {
        // 1) 이전 데이터 완전 초기화
        resetAll(); // [cite: 129]
        
        // 2) 시나리오별 가상 데이터 생성
        loadDefaultPassengers(); // 기본적으로 공통 데이터를 로드합니다[cite: 133].
        
        switch (scenarioNum) { // [cite: 130]
            case 1: 
                // 시나리오 1: 정상 호출 [cite: 130]
                break;
            case 2:
            case 3:
                // 시나리오 2, 3: 호출 후 미도착 / 노쇼 처리 [cite: 131, 174]
                openCounter(); // 이미 한 명 호출된 상태로 세팅 [cite: 174]
                break;
            // 기타 시나리오(4~7)에 대한 추가 로딩이 필요하다면 여기에 작성
        }
    }

    public void resetAll() {
        pq.clearAll(); // [cite: 131]
        members.clear(); // [cite: 131]
        flights.clear(); // [cite: 132]
        currentTicket = null; // [cite: 132]
        isCounterOpen = false; // [cite: 132]
    }

    private void loadDefaultPassengers() {
        LocalDateTime now = CurrentTime.curTime; // [cite: 133]
        
        // 가상 멤버 생성 (경량 생성자 사용) [cite: 134]
        Member m1 = new Member(1, "김민준", "M12345678", true, Grade.GOLD); // [cite: 134]
        Member m2 = new Member(2, "박서연", "M23456789", true, Grade.PRESTIGE); // [cite: 135]
        Member m3 = new Member(3, "이도윤", "M34567890", true, Grade.SILVER); // [cite: 135]
        Member m4 = new Member(4, "최수아", "M45678901", true, Grade.BLACK); // [cite: 136]
        Member m5 = new Member(5, "정하준", "M56789012", true, Grade.SILVER); // [cite: 136]
        Member m6 = new Member(6, "강지우", "M67890123", true, Grade.GOLD); // [cite: 137]

        members.addAll(List.of(m1, m2, m3, m4, m5, m6)); // [cite: 137]

        // 가상 항공편 생성 [cite: 138]
        Airplane a1 = new Airplane(1, "KE081", now.plusHours(1).plusMinutes(30)); // [cite: 138]
        Airplane a2 = new Airplane(2, "OZ102", now.plusMinutes(25));   // 출국 임박 -> AQ행 [cite: 139]
        Airplane a3 = new Airplane(3, "KE651", now.plusHours(2)); // [cite: 139]
        Airplane a4 = new Airplane(4, "OZ541", now.plusMinutes(20));   // 출국 임박 -> AQ행 [cite: 140]
        Airplane a5 = new Airplane(5, "KE305", now.plusHours(1)); // [cite: 140]
        Airplane a6 = new Airplane(6, "OZ773", now.plusHours(3)); // [cite: 141]

        flights.addAll(List.of(a1, a2, a3, a4, a5, a6)); // [cite: 141]

        // 번호표 발행 및 큐 삽입 [cite: 142]
        pq.enqueue(new PickUpTicket(m1, a1, pq.nextNum(), 0)); // [cite: 142]
        pq.enqueue(new PickUpTicket(m2, a2, pq.nextNum(), 0)); // [cite: 143]
        pq.enqueue(new PickUpTicket(m3, a3, pq.nextNum(), 0)); // [cite: 143]
        pq.enqueue(new PickUpTicket(m4, a4, pq.nextNum(), 0)); // [cite: 143]
        pq.enqueue(new PickUpTicket(m5, a5, pq.nextNum(), 0)); // [cite: 143]
        pq.enqueue(new PickUpTicket(m6, a6, pq.nextNum(), 0)); // [cite: 144]
    }

    // 창구 오픈 (= 다음 고객 호출)
    public void openCounter() {
        this.isCounterOpen = true; // [cite: 144]
        tryCallNextCustomer(); // [cite: 145]
    }

    private void tryCallNextCustomer() {
        if (isCounterOpen && currentTicket == null && pq.size() > 0) { // [cite: 145]
            currentTicket = pq.pop(); // [cite: 145]
        }
    }

    public PickUpTicket getCurrentTicket() {
        return currentTicket; // [cite: 146]
    }

    public void clearCurrentTicket() {
        currentTicket = null; // [cite: 147]
        tryCallNextCustomer(); // 비우면 자동으로 다음 사람 호출 [cite: 147]
    }

    // 항공 지연 - DB 없는 버전 [cite: 148]
    public void delayFlight(String flightCode, LocalDateTime newTime) {
        for (PickUpTicket t : pq.getAllFromAq()) { // [cite: 148]
            if (t.getAirplane().getFlightCode().equals(flightCode)) { // [cite: 148]
                t.getAirplane().setDepartureAt(newTime); // [cite: 148]
                return; // [cite: 148]
            }
        }
        for (PickUpTicket t : pq.getAllFromBq()) { // [cite: 149]
            if (t.getAirplane().getFlightCode().equals(flightCode)) { // [cite: 149]
                t.getAirplane().setDepartureAt(newTime); // [cite: 149]
                return; // [cite: 149]
            }
        }
    }

    // 재정렬 (PickUpSystem.rescheduledPq()와 동일) [cite: 150]
    public void rescheduledPq() {
        if (pq.size() == 0) return; // [cite: 151]
        List<PickUpTicket> all = new ArrayList<>(); // [cite: 151]
        all.addAll(pq.getAllFromAq()); // [cite: 151]
        all.addAll(pq.getAllFromBq()); // [cite: 152]
        pq.clearAll(); // [cite: 152]
        for (PickUpTicket t : all) { // [cite: 152]
            pq.enqueue(t); // [cite: 152]
        }
    }
}