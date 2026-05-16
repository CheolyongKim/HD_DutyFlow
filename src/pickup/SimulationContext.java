package pickup; 

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import common.CurrentTime;
import common.Grade;
import member.Member;
import airplane.Airplane;

public class SimulationContext {

    public MLPQ pq; 
    private PickUpTicket currentTicket; 
    private boolean isCounterOpen = false; 

    // 현재 시나리오에 등장하는 가상 데이터 (조회용)
    private List<Member> members = new ArrayList<>(); 
    private List<Airplane> flights = new ArrayList<>(); 
    
    public SimulationContext() {
        this.pq = new MLPQ();
        this.pq.makeMLPQ(new DepartureSoonSortStrategy(), new PrioritySortStrategy()); 
    }

    public void loadScenario(int scenarioNum) {
        // 1) 이전 데이터 완전 초기화
        resetAll(); 
        
        // 2) 시나리오별 가상 데이터 생성
        loadDefaultPassengers(); // 기본적으로 공통 데이터를 로드합니다
        
        switch (scenarioNum) {
            case 1: 
                // 시나리오 1: 정상 호출 
                break;
            case 2:
            case 3:
                // 시나리오 2, 3: 호출 후 미도착 / 노쇼 처리 
                openCounter(); // 이미 한 명 호출된 상태로 세팅 
                break;
            // 기타 시나리오(4~7)에 대한 추가 로딩이 필요하다면 여기에 작성
        }
    }

    public void resetAll() {
        pq.clearAll(); 
        members.clear(); 
        flights.clear(); 
        currentTicket = null; 
        isCounterOpen = false; 
    }

    private void loadDefaultPassengers() {
        LocalDateTime now = CurrentTime.curTime; 
        
        // 가상 멤버 생성 (경량 생성자 사용) 
        Member m1 = new Member(1, "이급박", "M11111111", true, Grade.GOLD); 
        Member m2 = new Member(2, "박지각", "M22222222", true, Grade.SILVER);
        Member m3 = new Member(3, "김철용", "M33333333", true, Grade.SILVER);
        Member m4 = new Member(4, "오블랙", "M44444444", true, Grade.BLACK);
        Member m5 = new Member(5, "최골드", "M55555555", true, Grade.GOLD);
        Member m6 = new Member(6, "유실버", "M66666666", true, Grade.SILVER);
        Member m7 = new Member(7, "약블랙", "M77777777", true, Grade.BLACK);
        Member m8 = new Member(8, "중블랙", "M88888888", true, Grade.BLACK);
        Member m9 = new Member(9, "강부자", "M99999999", true, Grade.PRESTIGE);
        Member m10 = new Member(10, "구민", "M00000000", true, Grade.SILVER);

        members.addAll(List.of(m1, m2, m3, m4, m5, m6, m7, m8, m9, m10));

        // 가상 항공편 생성 [cite: 138]
        Airplane a1 = new Airplane(1, "KE1025", now.plusMinutes(25));
        Airplane a2 = new Airplane(2, "OZ1015", now.plusMinutes(15));   
        Airplane a3 = new Airplane(3, "7C1050", now.plusHours(50));
        Airplane a4 = new Airplane(4, "KE1090", now.plusMinutes(90));  
        Airplane a5 = new Airplane(5, "OZ1100", now.plusHours(100)); 
        Airplane a6 = new Airplane(6, "LJ1080", now.plusHours(80)); 
        Airplane a7 = new Airplane(7, "KE1120", now.plusHours(120)); 
        Airplane a8 = new Airplane(8, "OZ1160", now.plusHours(160)); 
        Airplane a9 = new Airplane(9, "KE1150", now.plusHours(150)); 
        Airplane a10 = new Airplane(10, "TW1030", now.plusHours(30)); 

        flights.addAll(List.of(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10)); 

        // 번호표 발행 및 큐 삽입 
        pq.enqueue(new PickUpTicket(m1, a1, pq.nextNum(), 0));
        pq.enqueue(new PickUpTicket(m2, a2, pq.nextNum(), 0));
        pq.enqueue(new PickUpTicket(m3, a3, pq.nextNum(), 0));
        pq.enqueue(new PickUpTicket(m4, a4, pq.nextNum(), 0));
        pq.enqueue(new PickUpTicket(m5, a5, pq.nextNum(), 0));
        pq.enqueue(new PickUpTicket(m6, a6, pq.nextNum(), 0));
        pq.enqueue(new PickUpTicket(m7, a7, pq.nextNum(), 0));
        pq.enqueue(new PickUpTicket(m8, a8, pq.nextNum(), 0));
        pq.enqueue(new PickUpTicket(m9, a9, pq.nextNum(), 0));
        pq.enqueue(new PickUpTicket(m10, a10, pq.nextNum(), 0));
    }

    // 창구 오픈 (= 다음 고객 호출)
    public void openCounter() {
        this.isCounterOpen = true; 
        tryCallNextCustomer(); 
    }

    private void tryCallNextCustomer() {
        if (isCounterOpen && currentTicket == null && pq.size() > 0) { 
            currentTicket = pq.pop();
        }
    }

    public PickUpTicket getCurrentTicket() {
        return currentTicket; 
    }

    public void clearCurrentTicket() {
        currentTicket = null; 
        tryCallNextCustomer(); // 비우면 자동으로 다음 사람 호출 
    }

    // 항공 지연 - DB 없는 버전 [cite: 148]
    public void delayFlight(String flightCode, LocalDateTime newTime) {
        for (PickUpTicket t : pq.getAllFromAq()) { 
            if (t.getAirplane().getFlightCode().equals(flightCode)) { 
                t.getAirplane().setDepartureAt(newTime); 
                return; 
            }
        }
        for (PickUpTicket t : pq.getAllFromBq()) { 
            if (t.getAirplane().getFlightCode().equals(flightCode)) { 
                t.getAirplane().setDepartureAt(newTime); 
                return; 
            }
        }
    }

    // 재정렬 (PickUpSystem.rescheduledPq()와 동일) 
    public void rescheduledPq() {
        if (pq.size() == 0) return; 
        List<PickUpTicket> all = new ArrayList<>(); 
        all.addAll(pq.getAllFromAq());
        all.addAll(pq.getAllFromBq()); 
        pq.clearAll(); 
        for (PickUpTicket t : all) { 
            pq.enqueue(t); 
        }
    }
}