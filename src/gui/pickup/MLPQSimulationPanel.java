package gui.pickup;

import java.awt.*;
import java.awt.event.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import airplane.Airplane;
import common.CurrentTime;
import common.Grade;
import gui.ScreenManager;
import gui.common.Refreshable;
import member.Member;
import pickup.MLPQ;
import pickup.SimulationContext;
import pickup.PickUpTicket;

/**
 * MLPQ 시뮬레이션 화면 (화면설계서 섹션 4 — 시나리오 6-②③, 발표용)
 *
 * [좌측] 주문 목록 (시간창 -3h ~ +3h)
 * [중앙] AQ(우선큐) / BQ(일반큐) 이중 큐 카드
 * [우측] 시나리오 패널 (1~7번) + 컨트롤
 *
 * 키보드:
 *   Space     → 시간 1스텝(1분) 전진
 *   BackSpace → 시간 1스텝 후진
 *   Enter     → 현재 시나리오 전체 자동 실행
 *   ESC       → 자동 실행 중단 / 이전 화면 복귀
 */
public class MLPQSimulationPanel extends JPanel implements Refreshable {

    private final ScreenManager screenManager;

    private SimulationContext context;

    /* ── 시계 ── */
    private JLabel clockLabel;
    private JLabel modeLabel;
    private final Timer  clockTimer;

    /* ── 좌측: 주문 목록 ── */
    private DefaultListModel<String> orderListModel;
    private JList<String> orderList;

    /* ── 중앙: AQ / BQ 큐 표시 ── */
    private DefaultListModel<String> aqModel;
    private DefaultListModel<String> bqModel;
    private JList<String> aqList;
    private JList<String> bqList;
    private JLabel aqCountLabel;
    private JLabel bqCountLabel;

    /* ── 우측: 시나리오 ── */
    private JTextArea logArea;
    private Timer autoRunTimer;
    private boolean isAutoRunning = false;

    /* ── 항공 지연 알림 팝업 ── */
    private boolean delayDetected = false;

    /* ── 색상 ── */
    private static final Color BG          = new Color(0xF5F6FA);
    private static final Color BAR_BG      = new Color(0x1E293B);
    private static final Color CARD_BG     = Color.WHITE;
    private static final Color AQ_COLOR    = new Color(0xEF4444);
    private static final Color BQ_COLOR    = new Color(0x2563EB);
    private static final Color PRIMARY     = new Color(0x2D6CDF);
    private static final Color SUCCESS     = new Color(0x16A34A);
    private static final Color WARNING     = new Color(0xD97706);
    private static final Color SCENARIO_BG = new Color(0xF0FDF4);

    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd (E)  HH:mm:ss");
    private static final DateTimeFormatter SHORT_FMT =
            DateTimeFormatter.ofPattern("HH:mm");

    /* ── 등급 색상 ── */
    private static Color gradeColor(Grade g) {
        if (g == null) return new Color(0x9CA3AF);
        switch (g) {
            case PRESTIGE: return new Color(0x7C3AED);
            case BLACK:    return new Color(0x374151);
            case GOLD:     return new Color(0xD97706);
            default:       return new Color(0x9CA3AF);
        }
    }

    /* ============================================================ */

    public MLPQSimulationPanel(ScreenManager screenManager) {
    	this.context = new SimulationContext();
        this.screenManager = screenManager;

        setLayout(new BorderLayout());
        setBackground(BG);

        /* ── 상단: 모드 + 시계 ── */
        JPanel topBar = createTopBar();
        add(topBar, BorderLayout.NORTH);

        clockTimer = new Timer(1000, e -> updateClock());
        clockTimer.start();

        /* ── 3분할 본문 ── */
        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                createLeftPanel(), createCenterRightPanel());
        mainSplit.setDividerLocation(260);
        mainSplit.setResizeWeight(0.22);
        mainSplit.setBorder(null);

        add(mainSplit, BorderLayout.CENTER);

        /* ── 키보드 바인딩 ── */
        bindKeys();
    }

    /* ═══════════════════ 상단 바 ═══════════════════ */

    private JPanel createTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(BAR_BG);
        bar.setBorder(new EmptyBorder(6, 16, 6, 16));

        modeLabel = new JLabel("⏱ 시뮬레이션 모드");
        modeLabel.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        modeLabel.setForeground(new Color(0xFBBF24));

        clockLabel = new JLabel();
        clockLabel.setFont(new Font("D2Coding", Font.BOLD, 16));
        clockLabel.setForeground(Color.WHITE);
        clockLabel.setHorizontalAlignment(SwingConstants.CENTER);
        updateClock();

        JButton backBtn = new JButton("← 메인");
        backBtn.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        backBtn.setForeground(Color.WHITE);
        backBtn.setBackground(new Color(0x475569));
        backBtn.setBorderPainted(false);
        backBtn.setFocusPainted(false);
        backBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> screenManager.show("PICKUP_MAIN"));

        bar.add(modeLabel, BorderLayout.WEST);
        bar.add(clockLabel, BorderLayout.CENTER);
        bar.add(backBtn, BorderLayout.EAST);

        return bar;
    }

    /* ═══════════════════ 좌측: 주문 목록 ═══════════════════ */

    private JPanel createLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setBackground(BG);
        panel.setBorder(new EmptyBorder(10, 10, 10, 4));

        JLabel title = new JLabel("📋 주문 목록 (±3h)");
        title.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        panel.add(title, BorderLayout.NORTH);

        orderListModel = new DefaultListModel<>();
        orderList = new JList<>(orderListModel);
        orderList.setFont(new Font("D2Coding", Font.PLAIN, 12));
        orderList.setCellRenderer(new OrderCellRenderer());

        JScrollPane sp = new JScrollPane(orderList);
        sp.setBorder(new LineBorder(new Color(0xE2E8F0)));
        panel.add(sp, BorderLayout.CENTER);

        // 시간 기준선 안내
        JLabel hint = new JLabel("━ 굵은 선 = 현재 시각 기준");
        hint.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
        hint.setForeground(new Color(0x6B7280));
        panel.add(hint, BorderLayout.SOUTH);

        return panel;
    }

    /* ═══════════════════ 중앙+우측 ═══════════════════ */

    private JSplitPane createCenterRightPanel() {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                createCenterPanel(), createRightPanel());
        split.setDividerLocation(420);
        split.setResizeWeight(0.55);
        split.setBorder(null);
        return split;
    }

    /* ── 중앙: AQ / BQ 이중 큐 카드 ── */
    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 0, 8));
        panel.setBackground(BG);
        panel.setBorder(new EmptyBorder(10, 6, 10, 6));

        // AQ (우선 큐)
        aqModel = new DefaultListModel<>();
        aqList  = new JList<>(aqModel);
        aqList.setFont(new Font("D2Coding", Font.PLAIN, 12));
        aqList.setCellRenderer(new QueueCellRenderer(AQ_COLOR));
        aqCountLabel = new JLabel("AQ: 0명");

        JPanel aqPanel = createQueueCard("🔴  AQ — 우선 큐 (출국임박·Starvation)",
                aqList, aqCountLabel, AQ_COLOR);

        // BQ (일반 큐)
        bqModel = new DefaultListModel<>();
        bqList  = new JList<>(bqModel);
        bqList.setFont(new Font("D2Coding", Font.PLAIN, 12));
        bqList.setCellRenderer(new QueueCellRenderer(BQ_COLOR));
        bqCountLabel = new JLabel("BQ: 0명");

        JPanel bqPanel = createQueueCard("🔵  BQ — 일반 큐 (등급→대기순)",
                bqList, bqCountLabel, BQ_COLOR);

        panel.add(aqPanel);
        panel.add(bqPanel);
        return panel;
    }

    private JPanel createQueueCard(String title, JList<String> list,
                                    JLabel countLabel, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(accentColor, 2),
                new EmptyBorder(8, 10, 8, 10)
        ));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(CARD_BG);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        titleLbl.setForeground(accentColor);

        countLabel.setFont(new Font("D2Coding", Font.BOLD, 13));
        countLabel.setForeground(accentColor);

        header.add(titleLbl, BorderLayout.WEST);
        header.add(countLabel, BorderLayout.EAST);

        card.add(header, BorderLayout.NORTH);
        card.add(new JScrollPane(list), BorderLayout.CENTER);

        return card;
    }

    /* ── 우측: 시나리오 패널 ── */
    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(BG);
        panel.setBorder(new EmptyBorder(10, 4, 10, 10));

        JLabel title = new JLabel("🎬 시연 시나리오");
        title.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        panel.add(title, BorderLayout.NORTH);

        // 시나리오 버튼들
        JPanel btnPanel = new JPanel(new GridLayout(0, 1, 0, 4));
        btnPanel.setBackground(SCENARIO_BG);
        btnPanel.setBorder(new EmptyBorder(8, 8, 8, 8));

        String[] scenarios = {
                "1. 정상 호출",
                "2. 호출 후 미도착",
                "3. 노쇼 처리",
                "4. 우선 등급 입장",
                "5. Starvation 방지 승급",
                "6. 항공 지연 → 재정렬",
                "7. 노쇼 자동 처리",
        };

        for (int i = 0; i < scenarios.length; i++) {
            JButton btn = scenarioButton(scenarios[i]);
            final int idx = i + 1;
            btn.addActionListener(e -> runScenario(idx));
            btnPanel.add(btn);
        }

        JScrollPane scenarioScroll = new JScrollPane(btnPanel);
        scenarioScroll.setBorder(null);
        scenarioScroll.setPreferredSize(new Dimension(220, 240));

        // 컨트롤 안내
        JPanel controlHint = new JPanel(new GridLayout(0, 1, 0, 2));
        controlHint.setBackground(new Color(0xFEF3C7));
        controlHint.setBorder(new EmptyBorder(8, 10, 8, 10));
        controlHint.add(hintLabel("Space     → 시간 +1분"));
        controlHint.add(hintLabel("BackSpace → 시간 -1분"));
        controlHint.add(hintLabel("Enter     → 자동 실행"));
        controlHint.add(hintLabel("ESC       → 중단/복귀"));

        // 로그 영역
        logArea = new JTextArea(6, 20);
        logArea.setFont(new Font("D2Coding", Font.PLAIN, 12));
        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setBackground(new Color(0xF8FAFC));
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(new Color(0xCBD5E1)),
                "실행 로그",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("맑은 고딕", Font.BOLD, 12)));

        // 조합
        JPanel mid = new JPanel(new BorderLayout(0, 6));
        mid.setBackground(BG);
        mid.add(scenarioScroll, BorderLayout.CENTER);
        mid.add(controlHint, BorderLayout.SOUTH);

        panel.add(mid, BorderLayout.CENTER);
        panel.add(logScroll, BorderLayout.SOUTH);

        return panel;
    }

    /* ═══════════════════ 큐 화면 갱신 ═══════════════════ */

    private void refreshQueueDisplay() {
        if (context == null) return;

        MLPQ pq = context.pq;

        // AQ
        List<PickUpTicket> aqTickets = pq.getAllFromAq();
        aqModel.clear();
        int rank = 1;
        for (PickUpTicket t : aqTickets) {
            aqModel.addElement(formatTicket(rank++, t, true));
        }
        aqCountLabel.setText("AQ: " + aqTickets.size() + "명");

        // BQ
        List<PickUpTicket> bqTickets = pq.getAllFromBq();
        bqModel.clear();
        rank = 1;
        for (PickUpTicket t : bqTickets) {
            bqModel.addElement(formatTicket(rank++, t, false));
        }
        bqCountLabel.setText("BQ: " + bqTickets.size() + "명");

        // 주문 목록 (시간창)
        refreshOrderList();

        updateClock();
    }

    private String formatTicket(int rank, PickUpTicket t, boolean isAQ) {
        Member m  = t.getMember();
        Airplane a = t.getAirplane();
        long waitMin = Duration.between(t.getTicketIssueTime(), CurrentTime.curTime).toMinutes();
        long toDepart = Duration.between(CurrentTime.curTime, a.getDepartureAt()).toMinutes();

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("[%d] ", rank));
        sb.append(String.format("%-6s", m.getName()));
        sb.append(String.format(" | %s", m.getGrade()));
        sb.append(String.format(" | %s", a.getFlightCode()));
        sb.append(String.format(" | 출국 %s", a.getDepartureAt().format(SHORT_FMT)));
        sb.append(String.format(" (%+d분)", toDepart));
        sb.append(String.format(" | 대기 %d분", waitMin));

        if (waitMin >= 40) sb.append(" ⚠️STARV");
        if (toDepart <= 30 && !isAQ) sb.append(" 🔺승급대상");

        return sb.toString();
    }

    private void refreshOrderList() {
        orderListModel.clear();

        if (context == null) return;

        MLPQ pq = context.pq;
        List<PickUpTicket> all = new ArrayList<>();
        all.addAll(pq.getAllFromAq());
        all.addAll(pq.getAllFromBq());

        // 현재 호출된 고객도 표시
        PickUpTicket current = context.getCurrentTicket();

        // -3h ~ +3h 기준 필터
        LocalDateTime rangeStart = CurrentTime.curTime.minusHours(3);
        LocalDateTime rangeEnd   = CurrentTime.curTime.plusHours(3);

        // 현재 시각 기준선
        orderListModel.addElement("──── 현재 " + CurrentTime.curTime.format(SHORT_FMT) + " ────");

        if (current != null) {
            orderListModel.addElement("▶ [호출중] " + current.getMember().getName()
                    + " | " + current.getAirplane().getFlightCode()
                    + " | " + current.getAirplane().getDepartureAt().format(SHORT_FMT));
        }

        for (PickUpTicket t : all) {
            LocalDateTime dept = t.getAirplane().getDepartureAt();
            if (dept.isBefore(rangeStart) || dept.isAfter(rangeEnd)) continue;

            String status = "PICKUP_RESERVED";
            if (CurrentTime.curTime.isAfter(dept)) {
                status = "NO_SHOW";
            }

            orderListModel.addElement(String.format("  %s  %-6s  %s  [%s]  %s",
                    dept.format(SHORT_FMT),
                    t.getMember().getName(),
                    t.getAirplane().getFlightCode(),
                    t.getMember().getGrade(),
                    status));
        }
    }

    /* ═══════════════════ 시나리오 실행 ═══════════════════ */

    private void runScenario(int num) {
    	// 시나리오 버튼을 누르면 먼저 해당 시나리오 데이터를 로드 [cite: 159]
        context.loadScenario(num);
        appendLog("\n═══ 시나리오 " + num + " 실행 ═══");

        appendLog("\n═══ 시나리오 " + num + " 실행 ═══");

        try {
            switch (num) {
                case 1: scenarioNormalCall();       break;
                case 2: scenarioCallNoArrival();    break;
                case 3: scenarioNoShow();           break;
                case 4: scenarioPriorityEntry();    break;
                case 5: scenarioStarvation();       break;
                case 6: scenarioFlightDelay();      break;
                case 7: scenarioAutoNoShow();        break;
            }
        } catch (Exception ex) {
            appendLog("❌ 오류: " + ex.getMessage());
        }

        refreshQueueDisplay();
    }

    /** 1. 정상 호출 */
    private void scenarioNormalCall() {
        appendLog("대기열에서 다음 고객을 호출합니다.");
        try {
            context.openCounter();
            PickUpTicket current = context.getCurrentTicket();
            if (current != null) {
                appendLog("📢 호출: " + current.getMember().getName()
                        + " (" + current.getAirplane().getFlightCode() + ")");
            }
        } catch (Exception ex) {
            appendLog("호출 실패: " + ex.getMessage());
        }
    }

    /** 2. 호출 후 미도착 */
    private void scenarioCallNoArrival() {
        PickUpTicket current = context.getCurrentTicket();
        if (current == null) {
            appendLog("현재 호출된 고객이 없습니다. 먼저 시나리오 1을 실행하세요.");
            return;
        }
        appendLog("📢 " + current.getMember().getName() + " 고객 호출 중...");
        appendLog("⏳ 고객이 카운터에 도착하지 않았습니다.");
        appendLog("   (시나리오 3 '노쇼 처리'로 진행할 수 있습니다.)");
    }

    /** 3. 노쇼 처리 */
    private void scenarioNoShow() {
        PickUpTicket current = context.getCurrentTicket();
        if (current == null) {
            appendLog("현재 호출된 고객이 없습니다.");
            return;
        }
        String name = current.getMember().getName();
        appendLog("❌ " + name + " 고객 노쇼 처리");
        appendLog("   대기열에서 제거하고 다음 고객을 호출합니다.");
        context.pq.removeTicket(current);
    }

    /** 4. 우선 등급 입장 */
    private void scenarioPriorityEntry() {
        appendLog("🌟 프레스티지/블랙 등급 고객이 번호표를 뽑습니다.");
        appendLog("   등급 가중치에 따라 BQ 내 우선 배치됩니다.");

        // 데모용: 프레스티지 고객 추가
        Member vip = new Member(999, "VIP고객", "V99999999", true, Grade.PRESTIGE);
        Airplane ap = new Airplane(999, "KE999",
                CurrentTime.curTime.plusHours(2));
        PickUpTicket ticket = new PickUpTicket(vip, ap, context.pq.nextNum(), 999);
        context.pq.enqueue(ticket);

        appendLog("✅ VIP고객 (PRESTIGE) 추가 → BQ 상위 배치 확인");
    }

    /** 5. Starvation 방지 승급 */
    private void scenarioStarvation() {
        appendLog("⏰ 40분 이상 대기한 고객이 있는지 확인합니다.");
        appendLog("   시간을 40분 전진시켜 에이징 효과를 확인합니다.");

        for (int i = 0; i < 40; i++) {
            context.pq.passTime();
        }

        appendLog("✅ 40분 경과. pop() 시 Starvation 대상이 최우선 호출됩니다.");
    }

    /** 6. 항공 지연 → 재정렬 */
    private void scenarioFlightDelay() {
        appendLog("✈️ 항공편 지연 시나리오를 실행합니다.");

        List<PickUpTicket> allAq = context.pq.getAllFromAq();
        List<PickUpTicket> allBq = context.pq.getAllFromBq();

        PickUpTicket target = null;
        if (!allAq.isEmpty()) target = allAq.get(0);
        else if (!allBq.isEmpty()) target = allBq.get(0);

        if (target == null) {
            appendLog("대기열에 고객이 없습니다.");
            return;
        }

        String flightCode = target.getAirplane().getFlightCode();
        LocalDateTime oldTime = target.getAirplane().getDepartureAt();
        LocalDateTime newTime = oldTime.plusHours(2);

        appendLog("🔔 " + flightCode + "편 지연 감지: "
                + oldTime.format(SHORT_FMT) + " → " + newTime.format(SHORT_FMT));

        context.delayFlight(flightCode, newTime);
        context.rescheduledPq();

        appendLog("✅ 대기열이 재정렬되었습니다.");

        // 지연 알림 팝업 (화면설계서: 상단 모달)
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this,
                    "✈️ " + flightCode + "편 " +
                            Duration.between(oldTime, newTime).toMinutes() + "분 지연 감지\n" +
                            "— 픽업 큐가 재정렬되었습니다.",
                    "항공편 지연 알림",
                    JOptionPane.WARNING_MESSAGE);
        });
    }

    /** 7. 노쇼 자동 처리 */
    private void scenarioAutoNoShow() {
        appendLog("🕐 출국시간 경과 주문을 자동 NO_SHOW 처리합니다.");

        List<PickUpTicket> expired =
                context.pq.getExpiredTickets(CurrentTime.curTime);

        if (expired.isEmpty()) {
            appendLog("   현재 출국시간이 경과한 대기자가 없습니다.");
            appendLog("   시간을 3시간 전진시켜 경과 상황을 만듭니다.");

            for (int i = 0; i < 180; i++) {
                CurrentTime.curTime = CurrentTime.curTime.plusMinutes(1);
            }
            expired = context.pq.getExpiredTickets(CurrentTime.curTime);
        }

        if (!expired.isEmpty()) {
            for (PickUpTicket t : expired) {
                appendLog("   ❌ " + t.getMember().getName()
                        + " (" + t.getAirplane().getFlightCode() + ") → NO_SHOW");
            }
            context.pq.removeExpiredTickets(expired);
            appendLog("✅ 총 " + expired.size() + "건 자동 노쇼 처리 완료");
        } else {
            appendLog("   처리할 항목이 없습니다.");
        }
    }

    /* ═══════════════════ 시간 컨트롤 ═══════════════════ */

    private void stepForward() {
        if (context != null) {
            context.pq.passTime(); // 내부에서 +1분 + promote
        } else {
            CurrentTime.curTime = CurrentTime.curTime.plusMinutes(1);
        }
        appendLog("⏩ +1분 → " + CurrentTime.curTime.format(SHORT_FMT));
        refreshQueueDisplay();
    }

    private void stepBackward() {
        CurrentTime.curTime = CurrentTime.curTime.minusMinutes(1);
        appendLog("⏪ -1분 → " + CurrentTime.curTime.format(SHORT_FMT));
        refreshQueueDisplay();
    }

    private void toggleAutoRun() {
        if (isAutoRunning) {
            stopAutoRun();
        } else {
            isAutoRunning = true;
            appendLog("▶ 자동 실행 시작 (1초 간격)");
            autoRunTimer = new Timer(1000, e -> {
                stepForward();
            });
            autoRunTimer.start();
        }
    }

    private void stopAutoRun() {
        isAutoRunning = false;
        if (autoRunTimer != null) {
            autoRunTimer.stop();
            autoRunTimer = null;
        }
        appendLog("⏸ 자동 실행 중단");
    }

    /* ═══════════════════ 키보드 바인딩 ═══════════════════ */

    private void bindKeys() {
        InputMap im  = getInputMap(WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getActionMap();

        // 오른쪽키 → +1분
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_RIGHT, 0), "stepFwd");
        am.put("stepFwd", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { stepForward(); }
        });

        // 왼쪽키 → -1분
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_LEFT, 0), "stepBack");
        am.put("stepBack", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { stepBackward(); }
        });

        // Enter → 자동 실행 토글
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "autoRun");
        am.put("autoRun", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { toggleAutoRun(); }
        });

        // ESC → 자동 실행 중단 + 이전 화면
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "escape");
        am.put("escape", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                if (isAutoRunning) {
                    stopAutoRun();
                } else {
                    screenManager.show("PICKUP_MAIN");
                }
            }
        });
    }

    /* ═══════════════════ 유틸 ═══════════════════ */

    private void updateClock() {
        clockLabel.setText(CurrentTime.curTime.format(TIME_FMT));
    }

    private void appendLog(String msg) {
        logArea.append(msg + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    private JButton scenarioButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        btn.setForeground(new Color(0x1E293B));
        btn.setBackground(Color.WHITE);
        btn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(0xD1D5DB)),
                new EmptyBorder(6, 10, 6, 10)
        ));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setHorizontalAlignment(SwingConstants.LEFT);

        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(0xEFF6FF));
            }
            @Override public void mouseExited(MouseEvent e) {
                btn.setBackground(Color.WHITE);
            }
        });

        return btn;
    }

    private JLabel hintLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("D2Coding", Font.PLAIN, 12));
        lbl.setForeground(new Color(0x92400E));
        return lbl;
    }

    /* ═══════════════════ 셀 렌더러 ═══════════════════ */

    /** 주문 목록용 렌더러 */
    private static class OrderCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(
                JList<?> list, Object value, int idx, boolean sel, boolean foc) {
            JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, idx, sel, foc);
            String text = value.toString();

            if (text.startsWith("────")) {
                lbl.setFont(new Font("D2Coding", Font.BOLD, 12));
                lbl.setForeground(AQ_COLOR);
                lbl.setBackground(new Color(0xFEF2F2));
                lbl.setOpaque(true);
            } else if (text.contains("NO_SHOW")) {
                lbl.setForeground(new Color(0x9CA3AF));
                lbl.setFont(new Font("D2Coding", Font.ITALIC, 11));
            } else if (text.startsWith("▶")) {
                lbl.setForeground(SUCCESS);
                lbl.setFont(new Font("D2Coding", Font.BOLD, 12));
            }

            return lbl;
        }
    }

    /** 큐 카드용 렌더러 */
    private static class QueueCellRenderer extends DefaultListCellRenderer {
        private final Color accent;

        QueueCellRenderer(Color accent) {
            this.accent = accent;
        }

        @Override
        public Component getListCellRendererComponent(
                JList<?> list, Object value, int idx, boolean sel, boolean foc) {
            JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, idx, sel, foc);
            lbl.setFont(new Font("D2Coding", Font.PLAIN, 12));

            String text = value.toString();

            if (text.contains("⚠️STARV")) {
                lbl.setBackground(new Color(0xFEF3C7));
                lbl.setOpaque(true);
            } else if (text.contains("🔺승급대상")) {
                lbl.setBackground(new Color(0xFEE2E2));
                lbl.setOpaque(true);
            }

            if (idx == 0) {
                lbl.setForeground(accent);
                lbl.setFont(new Font("D2Coding", Font.BOLD, 13));
            }

            return lbl;
        }
    }

    /* ═══════════════════ Refreshable ═══════════════════ */

    @Override
    public void refresh() {
        logArea.setText("");
        appendLog("시뮬레이션 화면 진입 — " + CurrentTime.curTime.format(TIME_FMT));
        refreshQueueDisplay();
    }
}
