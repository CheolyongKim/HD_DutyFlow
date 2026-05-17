package gui.pickup;

import java.awt.*;
import java.awt.event.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;

import common.CurrentTime;
import common.Grade;
import gui.ScreenManager;
import gui.common.Refreshable;
import pickup.MLPQ;
import pickup.PickUpTicket;
import pickup.SimulationContext;

/**
 * MLPQ 시뮬레이션 패널 (화면설계서 v2 섹션 4)
 *
 * <p>DB 의존성 제로 — SimulationContext만 사용.
 * <p>PickUpSystem의 passTime, processPickUp, processNoShow,
 *    checkCallingTimeout, updateNoShowState 로직이 모두 반영되어 있다.
 *
 * <p>키 바인딩:
 *   → (Right) : 시간 +1분
 *   ← (Left)  : 시간 -1분
 *   Enter     : 자동 실행
 *   ESC       : 중단/복귀
 */
public class MLPQSimulationPanel extends JPanel implements Refreshable {

    private final ScreenManager screenManager;
    private final SimulationContext context;

    /* ── UI 컴포넌트 ── */
    private JLabel clockLabel;
    private Timer clockTimer;

    // 좌측: 주문 목록
    private JPanel orderListPanel;
    private JScrollPane orderScrollPane;

    // 중앙: AQ / BQ
    private JPanel aqPanel, bqPanel;
    private JLabel aqCountLabel, bqCountLabel;
    private JPanel aqCardContainer, bqCardContainer;

    // 호출된 고객 표시 영역
    private JPanel calledPanel;
    private JButton calledButton;   // ★ 클릭 = 고객 도착(processPickUp)

    // 우측: 시나리오 + 로그
    private JTextArea logArea;

    // 자동 실행
    private Timer autoTimer;
    private boolean autoRunning = false;

    /* ── 등급별 색상 ── */
    private static final Color PRESTIGE_BG  = new Color(0x7C3AED);
    private static final Color PRESTIGE_FG  = Color.WHITE;
    private static final Color BLACK_BG     = new Color(0x1E293B);
    private static final Color BLACK_FG     = Color.WHITE;
    private static final Color GOLD_BG      = new Color(0xF59E0B);
    private static final Color GOLD_FG      = new Color(0x451A03);
    private static final Color SILVER_BG    = new Color(0xCBD5E1);
    private static final Color SILVER_FG    = new Color(0x1E293B);

    /* ── 일반 색상 ── */
    private static final Color BG           = new Color(0xF1F5F9);
    private static final Color CARD_BG      = Color.WHITE;
    private static final Color BAR_BG       = new Color(0x1E293B);
    private static final Color AQ_BORDER    = new Color(0xEF4444);
    private static final Color BQ_BORDER    = new Color(0x3B82F6);
    private static final Color AQ_HEADER_BG = new Color(0xFEF2F2);
    private static final Color BQ_HEADER_BG = new Color(0xEFF6FF);
    private static final Color CALLED_BG    = new Color(0xFFFBEB);
    private static final Color CALLED_BTN   = new Color(0x059669);
    private static final Color SCENARIO_BG  = new Color(0xFFFDE7);
    private static final Color TEXT_DARK    = new Color(0x1E293B);
    private static final Color TEXT_SUB     = new Color(0x64748B);
    private static final Color NOSHOW_BG    = new Color(0xFEE2E2);
    private static final Color TIMEOUT_BG   = new Color(0xFFF7ED);

    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd (E)  HH:mm:ss");
    private static final DateTimeFormatter SHORT_TIME =
            DateTimeFormatter.ofPattern("HH:mm");

    /* ================================================================
     *  생성자
     * ================================================================ */
    public MLPQSimulationPanel(ScreenManager screenManager) {
        this.screenManager = screenManager;
        this.context = new SimulationContext();

        setLayout(new BorderLayout());
        setBackground(BG);

        add(createTopBar(), BorderLayout.NORTH);
        add(createMainContent(), BorderLayout.CENTER);

        bindKeys();

        clockTimer = new Timer(1000, e -> updateClock());
        clockTimer.start();
    }

    /** 하위 호환 — PickUpSystem을 받아도 무시 (다른 패널과 통합 시) */
    public void setPickUpSystem(Object ps) { /* 무시 */ }

    /* ================================================================
     *  상단 바 — 시계 + 모드 표시 + 메인 버튼
     * ================================================================ */
    private JPanel createTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(BAR_BG);
        bar.setBorder(new EmptyBorder(6, 16, 6, 16));

        JLabel modeLabel = new JLabel("◻ 시뮬레이션 모드");
        modeLabel.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        modeLabel.setForeground(new Color(0xFBBF24));
        bar.add(modeLabel, BorderLayout.WEST);

        clockLabel = new JLabel();
        clockLabel.setFont(new Font("D2Coding", Font.BOLD, 16));
        clockLabel.setForeground(Color.WHITE);
        clockLabel.setHorizontalAlignment(SwingConstants.CENTER);
        updateClock();
        bar.add(clockLabel, BorderLayout.CENTER);

        JButton mainBtn = new JButton("← 메인");
        mainBtn.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        mainBtn.setForeground(Color.WHITE);
        mainBtn.setBackground(new Color(0x475569));
        mainBtn.setFocusPainted(false);
        mainBtn.setBorderPainted(false);
        mainBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        mainBtn.addActionListener(e -> goBack());
        bar.add(mainBtn, BorderLayout.EAST);

        return bar;
    }

    private void updateClock() {
        if (clockLabel != null) {
            clockLabel.setText(CurrentTime.curTime.format(TIME_FMT));
        }
    }

    /* ================================================================
     *  메인 콘텐츠 — 3분할
     * ================================================================ */
    private JPanel createMainContent() {
        JPanel main = new JPanel(new BorderLayout(8, 0));
        main.setBackground(BG);
        main.setBorder(new EmptyBorder(8, 8, 8, 8));

        main.add(createLeftPanel(), BorderLayout.WEST);
        main.add(createCenterPanel(), BorderLayout.CENTER);
        main.add(createRightPanel(), BorderLayout.EAST);

        return main;
    }

    /* ── 좌측: 주문 목록 (시간창) ── */
    private JPanel createLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setPreferredSize(new Dimension(280, 0));
        panel.setBackground(BG);

        JLabel title = new JLabel("  ◻ 주문 목록 (±3h)");
        title.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        title.setForeground(TEXT_DARK);
        title.setBorder(new EmptyBorder(4, 0, 4, 0));
        panel.add(title, BorderLayout.NORTH);

        orderListPanel = new JPanel();
        orderListPanel.setLayout(new BoxLayout(orderListPanel, BoxLayout.Y_AXIS));
        orderListPanel.setBackground(CARD_BG);

        orderScrollPane = new JScrollPane(orderListPanel);
        orderScrollPane.setBorder(BorderFactory.createLineBorder(new Color(0xE2E8F0)));
        orderScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        orderScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(orderScrollPane, BorderLayout.CENTER);

        JLabel hint = new JLabel("  — 굵은 선 = 현재 시각 기준");
        hint.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
        hint.setForeground(TEXT_SUB);
        panel.add(hint, BorderLayout.SOUTH);

        return panel;
    }

    /* ── 중앙: 호출된 고객 + AQ + BQ ── */
    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setBackground(BG);

        // ── 호출된 고객 영역 ──
        calledPanel = new JPanel(new BorderLayout(8, 0));
        calledPanel.setBackground(CALLED_BG);
        calledPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xF59E0B), 2),
                new EmptyBorder(10, 14, 10, 14)
        ));

        JLabel calledTitle = new JLabel("📢 현재 호출");
        calledTitle.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        calledPanel.add(calledTitle, BorderLayout.WEST);

        calledButton = new JButton("대기 중...");
        calledButton.setFont(new Font("D2Coding", Font.BOLD, 13));
        calledButton.setBackground(new Color(0xE2E8F0));
        calledButton.setForeground(TEXT_SUB);
        calledButton.setFocusPainted(false);
        calledButton.setBorderPainted(false);
        calledButton.setEnabled(false);
        calledButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        calledButton.addActionListener(e -> onCalledTicketClicked());
        calledPanel.add(calledButton, BorderLayout.CENTER);

        panel.add(calledPanel, BorderLayout.NORTH);

        // ── AQ + BQ 분할 ──
        JPanel queuesPanel = new JPanel(new GridLayout(2, 1, 0, 6));
        queuesPanel.setBackground(BG);

        // AQ
        JPanel aqWrapper = new JPanel(new BorderLayout());
        aqWrapper.setBackground(CARD_BG);
        aqWrapper.setBorder(BorderFactory.createLineBorder(AQ_BORDER, 2));

        JPanel aqHeader = new JPanel(new BorderLayout());
        aqHeader.setBackground(AQ_HEADER_BG);
        aqHeader.setBorder(new EmptyBorder(6, 12, 6, 12));
        JLabel aqTitle = new JLabel("◻ AQ — 우선 큐 (출국임박·Starvation)");
        aqTitle.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        aqTitle.setForeground(AQ_BORDER);
        aqCountLabel = new JLabel("AQ: 0명");
        aqCountLabel.setFont(new Font("D2Coding", Font.BOLD, 12));
        aqCountLabel.setForeground(AQ_BORDER);
        aqHeader.add(aqTitle, BorderLayout.WEST);
        aqHeader.add(aqCountLabel, BorderLayout.EAST);
        aqWrapper.add(aqHeader, BorderLayout.NORTH);

        aqCardContainer = new JPanel();
        aqCardContainer.setLayout(new BoxLayout(aqCardContainer, BoxLayout.Y_AXIS));
        aqCardContainer.setBackground(CARD_BG);
        JScrollPane aqScroll = new JScrollPane(aqCardContainer);
        aqScroll.setBorder(null);
        aqScroll.getVerticalScrollBar().setUnitIncrement(16);
        aqWrapper.add(aqScroll, BorderLayout.CENTER);

        // BQ
        JPanel bqWrapper = new JPanel(new BorderLayout());
        bqWrapper.setBackground(CARD_BG);
        bqWrapper.setBorder(BorderFactory.createLineBorder(BQ_BORDER, 2));

        JPanel bqHeader = new JPanel(new BorderLayout());
        bqHeader.setBackground(BQ_HEADER_BG);
        bqHeader.setBorder(new EmptyBorder(6, 12, 6, 12));
        JLabel bqTitle = new JLabel("◻ BQ — 일반 큐 (등급→대기순)");
        bqTitle.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        bqTitle.setForeground(BQ_BORDER);
        bqCountLabel = new JLabel("BQ: 0명");
        bqCountLabel.setFont(new Font("D2Coding", Font.BOLD, 12));
        bqCountLabel.setForeground(BQ_BORDER);
        bqHeader.add(bqTitle, BorderLayout.WEST);
        bqHeader.add(bqCountLabel, BorderLayout.EAST);
        bqWrapper.add(bqHeader, BorderLayout.NORTH);

        bqCardContainer = new JPanel();
        bqCardContainer.setLayout(new BoxLayout(bqCardContainer, BoxLayout.Y_AXIS));
        bqCardContainer.setBackground(CARD_BG);
        JScrollPane bqScroll = new JScrollPane(bqCardContainer);
        bqScroll.setBorder(null);
        bqScroll.getVerticalScrollBar().setUnitIncrement(16);
        bqWrapper.add(bqScroll, BorderLayout.CENTER);

        queuesPanel.add(aqWrapper);
        queuesPanel.add(bqWrapper);
        panel.add(queuesPanel, BorderLayout.CENTER);

        return panel;
    }

    /* ── 우측: 시나리오 버튼 + 키 안내 + 로그 ── */
    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setPreferredSize(new Dimension(320, 0));
        panel.setBackground(BG);

        // 시나리오 패널
        JPanel scenarioPanel = new JPanel();
        scenarioPanel.setLayout(new BoxLayout(scenarioPanel, BoxLayout.Y_AXIS));
        scenarioPanel.setBackground(CARD_BG);
        scenarioPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE2E8F0)),
                new EmptyBorder(12, 12, 12, 12)
        ));

        JLabel scenTitle = new JLabel("◻ 시연 시나리오");
        scenTitle.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        scenTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        scenarioPanel.add(scenTitle);
        scenarioPanel.add(Box.createVerticalStrut(8));

        String[] scenarios = {
            "1. 정상 호출",
            "2. 호출 후 미도착",
            "3. 노쇼 처리",
            "4. 우선 등급 입장",
            "5. Starvation 방지 승급",
            "6. 항공 지연 → 재정렬",
            "7. 노쇼 자동 처리"
        };

        for (int i = 0; i < scenarios.length; i++) {
            final int num = i + 1;
            JButton btn = new JButton(scenarios[i]);
            btn.setFont(new Font("맑은 고딕", Font.BOLD, 13));
            btn.setBackground(SCENARIO_BG);
            btn.setForeground(TEXT_DARK);
            btn.setFocusPainted(false);
            btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(0xFDE68A)),
                    new EmptyBorder(8, 12, 8, 12)
            ));
            btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
            btn.setAlignmentX(Component.LEFT_ALIGNMENT);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.addActionListener(e -> runScenario(num));

            btn.addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) {
                    btn.setBackground(new Color(0xFEF3C7));
                }
                @Override public void mouseExited(MouseEvent e) {
                    btn.setBackground(SCENARIO_BG);
                }
            });

            scenarioPanel.add(btn);
            scenarioPanel.add(Box.createVerticalStrut(4));
        }

        panel.add(scenarioPanel, BorderLayout.NORTH);

        // 키 안내
        JPanel keyPanel = new JPanel(new GridLayout(4, 1, 0, 2));
        keyPanel.setBackground(new Color(0xFEF9C3));
        keyPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xFDE68A)),
                new EmptyBorder(8, 12, 8, 12)
        ));
        keyPanel.add(keyLabel("→ (Right)", "시간 +1분"));
        keyPanel.add(keyLabel("← (Left)", "시간 -1분"));
        keyPanel.add(keyLabel("Enter", "자동 실행"));
        keyPanel.add(keyLabel("ESC", "중단/복귀"));
        panel.add(keyPanel, BorderLayout.CENTER);

        // 실행 로그
        JPanel logPanel = new JPanel(new BorderLayout(0, 4));
        logPanel.setBackground(BG);

        JLabel logTitle = new JLabel("  실행 로그");
        logTitle.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        logPanel.add(logTitle, BorderLayout.NORTH);

        logArea = new JTextArea();
        logArea.setFont(new Font("D2Coding", Font.PLAIN, 12));
        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logArea.setBackground(new Color(0xF8FAFC));
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setPreferredSize(new Dimension(0, 200));
        logScroll.setBorder(BorderFactory.createLineBorder(new Color(0xE2E8F0)));
        logPanel.add(logScroll, BorderLayout.CENTER);

        panel.add(logPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JLabel keyLabel(String key, String desc) {
        JLabel lbl = new JLabel(key + "    → " + desc);
        lbl.setFont(new Font("D2Coding", Font.BOLD, 12));
        lbl.setForeground(new Color(0x92400E));
        return lbl;
    }

    /* ================================================================
     *  키 바인딩  (Space → Right Arrow 로 변경)
     * ================================================================ */
    private void bindKeys() {
        InputMap im = getInputMap(WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getActionMap();

        // → : 시간 +1분
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "stepForward");
        am.put("stepForward", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { stepForward(); }
        });

        // ← : 시간 -1분
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "stepBackward");
        am.put("stepBackward", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { stepBackward(); }
        });

        // Enter : 자동 실행
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "autoRun");
        am.put("autoRun", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { toggleAutoRun(); }
        });

        // ESC : 중단 / 복귀
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "escape");
        am.put("escape", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                if (autoRunning) { stopAutoRun(); }
                else { goBack(); }
            }
        });
    }

    /* ================================================================
     *  시간 조작
     * ================================================================ */

    private void stepForward() {
        context.passTime();   // ★ 타임아웃 + 노쇼 + promote 모두 포함
        flushContextLog();
        refreshQueueDisplay();
        updateClock();
    }

    private void stepBackward() {
        CurrentTime.curTime = CurrentTime.curTime.minusMinutes(1);
        appendLog("⏪ " + CurrentTime.curTime.toLocalTime() + " (1분 후진)");
        refreshQueueDisplay();
        updateClock();
    }

    private void toggleAutoRun() {
        if (autoRunning) {
            stopAutoRun();
        } else {
            autoRunning = true;
            appendLog("▶ 자동 실행 시작");
            autoTimer = new Timer(500, e -> {
                stepForward();
                // 큐가 비고 호출 중인 고객도 없으면 자동 정지
                if (context.pq.size() == 0 && context.getCurrentTicket() == null) {
                    stopAutoRun();
                    appendLog("■ 자동 실행 완료 (대기열 소진)");
                }
            });
            autoTimer.start();
        }
    }

    private void stopAutoRun() {
        autoRunning = false;
        if (autoTimer != null) {
            autoTimer.stop();
            autoTimer = null;
        }
        appendLog("■ 자동 실행 중단");
    }

    /* ================================================================
     *  시나리오 실행
     * ================================================================ */

    private void runScenario(int num) {
        if (autoRunning) stopAutoRun();

        context.loadScenario(num);

        appendLog("\n═══════════════════════════════════");
        appendLog("   시나리오 " + num + " 실행");
        appendLog("═══════════════════════════════════");

        flushContextLog();

        try {
            switch (num) {
                case 1: scenarioNormalCall();      break;
                case 2: scenarioCallNoArrival();   break;
                case 3: scenarioNoShow();          break;
                case 4: scenarioPriorityEntry();   break;
                case 5: scenarioStarvation();      break;
                case 6: scenarioFlightDelay();     break;
                case 7: scenarioAutoNoShow();      break;
            }
        } catch (Exception ex) {
            appendLog("❌ 오류: " + ex.getMessage());
        }

        flushContextLog();
        refreshQueueDisplay();
    }

    private void scenarioNormalCall() {
        appendLog("대기열에서 다음 고객을 호출합니다.");
        context.openCounter();
        flushContextLog();

        PickUpTicket current = context.getCurrentTicket();
        if (current != null) {
            appendLog("  호출: " + current.getMember().getName()
                    + " (" + current.getAirplane().getFlightCode() + ")");
        }
    }

    private void scenarioCallNoArrival() {
        // loadScenario에서 이미 openCounter()까지 실행됨
        PickUpTicket current = context.getCurrentTicket();
        if (current != null) {
            appendLog("📢 호출된 고객: " + current.getMember().getName());
            appendLog("   → 고객이 아직 도착하지 않았습니다.");
            appendLog("   → 시간을 전진(→)시키면 타임아웃이 작동합니다.");
            appendLog("   (기본 " + 10 + "분, 다음 대기자 출국 임박 시 5분으로 단축)");
        }
    }

    private void scenarioNoShow() {
        PickUpTicket current = context.getCurrentTicket();
        if (current != null) {
            String name = current.getMember().getName();
            appendLog("📢 호출된 고객: " + name);
            appendLog("   → 노쇼(No-Show) 처리합니다.");
            context.processNoShow(current);
            context.pq.removeTicket(current);
            context.clearCurrentTicket();
            flushContextLog();
            appendLog("   ✅ " + name + " 고객 대기열에서 제거 완료");
        }
    }

    private void scenarioPriorityEntry() {
        appendLog("현재 대기열 (SILVER, GOLD 위주)을 확인하세요.");
        appendLog("프레스티지 고객을 추가합니다...");

        LocalDateTime now = CurrentTime.curTime;
        member.Member vip = new member.Member(999, "VIP고객", "M99999999", true, Grade.PRESTIGE);
        airplane.Airplane vipFlight = new airplane.Airplane(999, "KE777", now.plusHours(1));
        PickUpTicket vipTicket = new PickUpTicket(vip, vipFlight, context.pq.nextNum(), 0);
        context.pq.enqueue(vipTicket);

        appendLog("   ✅ [VIP고객] PRESTIGE 등급 추가 → BQ 최상위 확인!");
    }

    private void scenarioStarvation() {
        appendLog("40분을 전진시켜 에이징(Starvation) 효과를 확인합니다...");
        for (int i = 0; i < 40; i++) {
            context.passTime();
        }
        flushContextLog();
        appendLog("   → 40분 경과 후 pop() 시 에이징 대상자가 최우선 호출됩니다.");
        appendLog("   → '정상 호출' 시나리오를 실행하거나 → 키로 확인하세요.");
    }

    private void scenarioFlightDelay() {
        appendLog("KE081 항공편을 3시간 지연시킵니다...");
        LocalDateTime newTime = CurrentTime.curTime.plusHours(4);
        context.delayFlight("KE081", newTime);
        flushContextLog();

        appendLog("대기열을 재정렬합니다...");
        context.rescheduledPq();
        flushContextLog();

        appendLog("   → KE081(김민준)의 우선순위가 변경된 것을 확인하세요.");
    }

    private void scenarioAutoNoShow() {
        appendLog("출국 시간이 경과한 티켓을 자동 NO_SHOW 처리합니다...");

        List<PickUpTicket> expired = context.pq.getExpiredTickets(CurrentTime.curTime);
        if (expired.isEmpty()) {
            appendLog("   → 현재 출국 경과 티켓 없음. 시간을 전진시키세요.");
        } else {
            for (PickUpTicket t : expired) {
                context.processNoShow(t);
            }
            context.pq.removeExpiredTickets(expired);
            flushContextLog();
            appendLog("   ✅ " + expired.size() + "건 NO_SHOW 처리 완료");
        }
    }

    /* ================================================================
     *  호출된 티켓 클릭 → 물품 인도 (processPickUp)
     * ================================================================ */
    private void onCalledTicketClicked() {
        if (context.getCurrentTicket() == null) return;
        context.processPickUp();
        flushContextLog();
        refreshQueueDisplay();
    }

    /* ================================================================
     *  화면 갱신
     * ================================================================ */

    private void refreshQueueDisplay() {
        // ── 호출된 고객 표시 ──
        PickUpTicket called = context.getCurrentTicket();
        if (called != null) {
            String name = called.getMember().getName();
            Grade grade = called.getMember().getGrade();
            String flight = called.getAirplane().getFlightCode();
            String depTime = called.getAirplane().getDepartureAt().format(SHORT_TIME);

            long waitMin = Duration.between(called.getTicketIssueTime(), CurrentTime.curTime)
                    .toMinutes();

            calledButton.setText("  [" + name + "]  " + grade.name() + "  |  " + flight
                    + "  출국 " + depTime + "  |  대기 " + waitMin + "분  — 클릭: 물품 전달");
            calledButton.setBackground(CALLED_BTN);
            calledButton.setForeground(Color.WHITE);
            calledButton.setEnabled(true);
            calledPanel.setBackground(CALLED_BG);

            // 호출 후 대기 시간 표시
            if (context.getCallTime() != null) {
                long callWait = Duration.between(context.getCallTime(), CurrentTime.curTime).toMinutes();
                calledButton.setToolTipText("호출 후 " + callWait + "분 경과 (타임아웃: 10분)");
            }
        } else {
            calledButton.setText("  대기 중... (시나리오를 실행하세요)");
            calledButton.setBackground(new Color(0xE2E8F0));
            calledButton.setForeground(TEXT_SUB);
            calledButton.setEnabled(false);
            calledPanel.setBackground(new Color(0xF8FAFC));
        }

        // ── AQ 카드 ──
        aqCardContainer.removeAll();
        List<PickUpTicket> aqList = context.pq.getAllFromAq();
        aqList.sort(Comparator.comparing((PickUpTicket t) -> t.getAirplane().getDepartureAt())
                .thenComparingInt(PickUpTicket::getTicketNum));
        aqCountLabel.setText("AQ: " + aqList.size() + "명");

        int rank = 1;
        for (PickUpTicket t : aqList) {
            aqCardContainer.add(createTicketCard(t, rank++, true));
            aqCardContainer.add(Box.createVerticalStrut(2));
        }

        // ── BQ 카드 ──
        bqCardContainer.removeAll();
        List<PickUpTicket> bqList = context.pq.getAllFromBq();
        bqList.sort(Comparator.comparingInt((PickUpTicket t) -> t.getMember().getGrade().getPriority())
                .thenComparingInt(PickUpTicket::getTicketNum));
        bqCountLabel.setText("BQ: " + bqList.size() + "명");

        rank = 1;
        for (PickUpTicket t : bqList) {
            bqCardContainer.add(createTicketCard(t, rank++, false));
            bqCardContainer.add(Box.createVerticalStrut(2));
        }

        // ── 주문 목록 (좌측) ──
        refreshOrderList();

        // 리페인트
        aqCardContainer.revalidate();
        aqCardContainer.repaint();
        bqCardContainer.revalidate();
        bqCardContainer.repaint();
        calledPanel.revalidate();
        calledPanel.repaint();

        updateClock();
    }

    /* ── 티켓 카드 생성 (등급별 색상 적용) ── */
    private JPanel createTicketCard(PickUpTicket t, int rank, boolean isAq) {
        Grade grade = t.getMember().getGrade();
        Color gradeBg = getGradeBackground(grade);
        Color gradeFg = getGradeForeground(grade);

        JPanel card = new JPanel(new BorderLayout(8, 0));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, gradeBg),
                new EmptyBorder(6, 10, 6, 10)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        // 순번
        JLabel rankLabel = new JLabel("[" + rank + "]");
        rankLabel.setFont(new Font("D2Coding", Font.BOLD, 12));
        rankLabel.setForeground(TEXT_SUB);
        rankLabel.setPreferredSize(new Dimension(30, 20));
        card.add(rankLabel, BorderLayout.WEST);

        // 정보
        String name = t.getMember().getName();
        String flight = t.getAirplane().getFlightCode();
        String depTime = t.getAirplane().getDepartureAt().format(SHORT_TIME);
        long minsLeft = Duration.between(CurrentTime.curTime, t.getAirplane().getDepartureAt())
                .toMinutes();
        long waitMin = Duration.between(t.getTicketIssueTime(), CurrentTime.curTime).toMinutes();

        String info = name + "  ";

        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        infoPanel.setBackground(CARD_BG);

        // 이름
        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        infoPanel.add(nameLabel);

        // 등급 칩
        JLabel gradeChip = new JLabel(" " + grade.name() + " ");
        gradeChip.setFont(new Font("D2Coding", Font.BOLD, 10));
        gradeChip.setOpaque(true);
        gradeChip.setBackground(gradeBg);
        gradeChip.setForeground(gradeFg);
        gradeChip.setBorder(new EmptyBorder(1, 4, 1, 4));
        infoPanel.add(gradeChip);

        // 항공편
        JLabel flightLabel = new JLabel(flight);
        flightLabel.setFont(new Font("D2Coding", Font.PLAIN, 11));
        flightLabel.setForeground(TEXT_SUB);
        infoPanel.add(flightLabel);

        // 출국 시간
        String timeStr = "출국 " + depTime + " (";
        if (minsLeft > 0) timeStr += "+" + minsLeft + "분)";
        else timeStr += minsLeft + "분)";

        JLabel timeLabel = new JLabel(timeStr);
        timeLabel.setFont(new Font("D2Coding", Font.PLAIN, 11));
        timeLabel.setForeground(minsLeft <= 30 ? AQ_BORDER : TEXT_SUB);
        infoPanel.add(timeLabel);

        card.add(infoPanel, BorderLayout.CENTER);

        // 대기 시간
        JLabel waitLabel = new JLabel("대기 " + waitMin + "분");
        waitLabel.setFont(new Font("D2Coding", Font.PLAIN, 11));
        if (waitMin >= 40) {
            waitLabel.setForeground(new Color(0xDC2626));
            waitLabel.setText("⚠ " + waitMin + "분");
        } else {
            waitLabel.setForeground(TEXT_SUB);
        }
        card.add(waitLabel, BorderLayout.EAST);

        return card;
    }

    /* ── 주문 목록 갱신 ── */
    private void refreshOrderList() {
        orderListPanel.removeAll();

        // AQ + BQ + 호출 중인 티켓 전부 모으기
        java.util.List<PickUpTicket> all = new java.util.ArrayList<>();
        if (context.getCurrentTicket() != null) {
            all.add(context.getCurrentTicket());
        }
        all.addAll(context.pq.getAllFromAq());
        all.addAll(context.pq.getAllFromBq());

        // 출국 시간 순 정렬
        all.sort(Comparator.comparing(t -> t.getAirplane().getDepartureAt()));

        LocalDateTime now = CurrentTime.curTime;
        boolean lineDrawn = false;

        for (PickUpTicket t : all) {
            LocalDateTime dep = t.getAirplane().getDepartureAt();

            // 현재 시각 기준선
            if (!lineDrawn && dep.isAfter(now)) {
                orderListPanel.add(createTimeLine());
                lineDrawn = true;
            }

            orderListPanel.add(createOrderRow(t, t == context.getCurrentTicket()));
        }
        if (!lineDrawn) {
            orderListPanel.add(createTimeLine());
        }

        orderListPanel.revalidate();
        orderListPanel.repaint();
    }

    private JPanel createTimeLine() {
        JPanel line = new JPanel(new BorderLayout());
        line.setBackground(CARD_BG);
        line.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));

        JLabel lbl = new JLabel("── 현재 " + CurrentTime.curTime.format(SHORT_TIME) + " ──");
        lbl.setFont(new Font("D2Coding", Font.BOLD, 11));
        lbl.setForeground(AQ_BORDER);
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        line.add(lbl);
        return line;
    }

    private JPanel createOrderRow(PickUpTicket t, boolean isCalled) {
        Grade grade = t.getMember().getGrade();
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));

        if (isCalled) {
            row.setBackground(CALLED_BG);
        } else if (CurrentTime.curTime.isAfter(t.getAirplane().getDepartureAt())) {
            row.setBackground(NOSHOW_BG);
        } else {
            row.setBackground(CARD_BG);
        }

        String depTime = t.getAirplane().getDepartureAt().format(SHORT_TIME);
        String status = isCalled ? "호출중"
                : CurrentTime.curTime.isAfter(t.getAirplane().getDepartureAt()) ? "NO_SHOW"
                : "PICKUP_RESERVED";

        JLabel timeLbl = new JLabel(depTime);
        timeLbl.setFont(new Font("D2Coding", Font.BOLD, 11));
        row.add(timeLbl);

        JLabel nameLbl = new JLabel(t.getMember().getName());
        nameLbl.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
        row.add(nameLbl);

        JLabel flightLbl = new JLabel(t.getAirplane().getFlightCode());
        flightLbl.setFont(new Font("D2Coding", Font.PLAIN, 10));
        flightLbl.setForeground(TEXT_SUB);
        row.add(flightLbl);

        // 등급 칩 (작은 버전)
        JLabel chip = new JLabel(grade.name());
        chip.setFont(new Font("D2Coding", Font.BOLD, 9));
        chip.setOpaque(true);
        chip.setBackground(getGradeBackground(grade));
        chip.setForeground(getGradeForeground(grade));
        chip.setBorder(new EmptyBorder(0, 3, 0, 3));
        row.add(chip);

        JLabel statusLbl = new JLabel(status);
        statusLbl.setFont(new Font("D2Coding", Font.PLAIN, 9));
        statusLbl.setForeground(
                "NO_SHOW".equals(status) ? AQ_BORDER :
                "호출중".equals(status) ? CALLED_BTN : TEXT_SUB
        );
        row.add(statusLbl);

        return row;
    }

    /* ── 등급별 색상 유틸 ── */
    private Color getGradeBackground(Grade grade) {
        switch (grade) {
            case PRESTIGE: return PRESTIGE_BG;
            case BLACK:    return BLACK_BG;
            case GOLD:     return GOLD_BG;
            case SILVER:   return SILVER_BG;
            default:       return SILVER_BG;
        }
    }

    private Color getGradeForeground(Grade grade) {
        switch (grade) {
            case PRESTIGE: return PRESTIGE_FG;
            case BLACK:    return BLACK_FG;
            case GOLD:     return GOLD_FG;
            case SILVER:   return SILVER_FG;
            default:       return SILVER_FG;
        }
    }

    /* ================================================================
     *  로그 관리
     * ================================================================ */
    private void appendLog(String msg) {
        logArea.append(msg + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    /** SimulationContext 내부 로그를 패널 로그로 옮김 */
    private void flushContextLog() {
        for (String msg : context.drainLog()) {
            appendLog("  " + msg);
        }
    }

    /* ================================================================
     *  네비게이션
     * ================================================================ */
    private void goBack() {
        if (autoRunning) stopAutoRun();
        screenManager.show("PICKUP_MAIN");
    }

    @Override
    public void refresh() {
        if (autoRunning) stopAutoRun();
        refreshQueueDisplay();
        updateClock();
    }
}