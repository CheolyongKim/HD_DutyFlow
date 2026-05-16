package gui.pickup;

import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;

import admin.airportmanager.AirportManagerDao;
import admin.airportmanager.dto.PickUpListDTO;
import common.CurrentTime;
import gui.ScreenManager;
import gui.common.Refreshable;

/**
 * 픽업 리스트 조회 화면 (화면설계서 섹션 2 — 시나리오 6-①)
 *
 * 탭: 전체 조회 / 기간별 조회 / 멤버별 조회
 * 테이블: 고객명 | 항공편 | 출국시간 | 회원등급 | 주문ID | 상품상태 | 픽업가능시각 | 실제픽업
 */
public class PickupListPanel extends JPanel implements Refreshable {

    private final ScreenManager screenManager;
    private final AirportManagerDao dao = new AirportManagerDao();

    /* ── 테이블 ── */
    private DefaultTableModel tableModel;
    private JTable table;

    /* ── 필터 필드 ── */
    private JTextField startDateField;
    private JTextField endDateField;
    private JTextField memberSearchField;

    /* ── 시계 ── */
    private final JLabel clockLabel;
    private final Timer clockTimer;

    /* ── 색상 ── */
    private static final Color BG        = new Color(0xF5F6FA);
    private static final Color BAR_BG    = new Color(0x1E293B);
    private static final Color PRIMARY   = new Color(0x2D6CDF);

    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd (E)  HH:mm:ss");
    private static final DateTimeFormatter DT_FMT =
            DateTimeFormatter.ofPattern("MM-dd HH:mm");

    /* ── 등급 색상 맵 ── */
    private static final Map<String, Color> GRADE_COLORS = new LinkedHashMap<>();
    static {
        GRADE_COLORS.put("PRESTIGE", new Color(0x7C3AED));
        GRADE_COLORS.put("BLACK",    new Color(0x1E1E2F));
        GRADE_COLORS.put("GOLD",     new Color(0xD97706));
        GRADE_COLORS.put("SILVER",   new Color(0x9CA3AF));
    }

    /* ── 상태 색상 맵 ── */
    private static final Map<String, Color> STATE_COLORS = new LinkedHashMap<>();
    static {
        STATE_COLORS.put("PICKUP_RESERVED", new Color(0x2563EB));
        STATE_COLORS.put("PICKED_UP",       new Color(0x16A34A));
        STATE_COLORS.put("NO_SHOW",         new Color(0xEF4444));
        STATE_COLORS.put("CANCELED",        new Color(0x6B7280));
        STATE_COLORS.put("PAID",            new Color(0x0891B2));
        STATE_COLORS.put("ORDERED",         new Color(0x8B5CF6));
        STATE_COLORS.put("VERIFIED",        new Color(0x059669));
    }

    /* ============================================================ */

    public PickupListPanel(ScreenManager screenManager) {
        this.screenManager = screenManager;

        setLayout(new BorderLayout());
        setBackground(BG);

        /* ── 상단 시계 ── */
        clockLabel = new JLabel();
        clockLabel.setFont(new Font("D2Coding", Font.BOLD, 15));
        clockLabel.setForeground(Color.WHITE);
        updateClock();

        JPanel clockBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 6));
        clockBar.setBackground(BAR_BG);
        clockBar.add(clockLabel);
        add(clockBar, BorderLayout.NORTH);

        clockTimer = new Timer(1000, e -> updateClock());
        clockTimer.start();

        /* ── 중앙: 탭 + 테이블 ── */
        add(createMainContent(), BorderLayout.CENTER);

        /* ── 하단: 뒤로가기 ── */
        add(createBottomBar(), BorderLayout.SOUTH);

        /* ── ESC ── */
        bindEscKey();
    }

    /* ─────────────────── 메인 콘텐츠 ─────────────────── */

    private JPanel createMainContent() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(BG);
        panel.setBorder(new EmptyBorder(12, 20, 0, 20));

        // 타이틀
        JLabel title = new JLabel("📋  픽업 리스트 조회");
        title.setFont(new Font("맑은 고딕", Font.BOLD, 22));
        title.setBorder(new EmptyBorder(0, 0, 6, 0));
        panel.add(title, BorderLayout.NORTH);

        // 탭 패널
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        tabs.addTab("전체 조회", createAllTab());
        tabs.addTab("기간별 조회", createDateRangeTab());
        tabs.addTab("멤버별 조회", createMemberTab());
        panel.add(tabs, BorderLayout.CENTER);

        return panel;
    }

    /* ── 탭 1: 전체 조회 ── */
    private JPanel createAllTab() {
        JPanel tab = new JPanel(new BorderLayout(0, 8));
        tab.setBackground(Color.WHITE);
        tab.setBorder(new EmptyBorder(10, 10, 10, 10));

        JButton searchBtn = styledButton("전체 조회");
        searchBtn.addActionListener(e -> loadAll());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setBackground(Color.WHITE);
        top.add(searchBtn);
        tab.add(top, BorderLayout.NORTH);

        tab.add(createTablePanel(), BorderLayout.CENTER);
        return tab;
    }

    /* ── 탭 2: 기간별 조회 ── */
    private JPanel createDateRangeTab() {
        JPanel tab = new JPanel(new BorderLayout(0, 8));
        tab.setBackground(Color.WHITE);
        tab.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        form.setBackground(Color.WHITE);

        startDateField = new JTextField("2026-05-01", 10);
        endDateField   = new JTextField("2026-05-31", 10);
        startDateField.setFont(new Font("D2Coding", Font.PLAIN, 14));
        endDateField.setFont(new Font("D2Coding", Font.PLAIN, 14));

        JButton searchBtn = styledButton("조회");
        searchBtn.addActionListener(e -> loadByDateRange());

        form.add(new JLabel("시작일:"));
        form.add(startDateField);
        form.add(new JLabel("  종료일:"));
        form.add(endDateField);
        form.add(Box.createHorizontalStrut(8));
        form.add(searchBtn);
        tab.add(form, BorderLayout.NORTH);

        tab.add(createTablePanel(), BorderLayout.CENTER);
        return tab;
    }

    /* ── 탭 3: 멤버별 조회 ── */
    private JPanel createMemberTab() {
        JPanel tab = new JPanel(new BorderLayout(0, 8));
        tab.setBackground(Color.WHITE);
        tab.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        form.setBackground(Color.WHITE);

        memberSearchField = new JTextField(12);
        memberSearchField.setFont(new Font("D2Coding", Font.PLAIN, 14));

        JButton searchBtn = styledButton("조회");
        searchBtn.addActionListener(e -> loadByMember());

        form.add(new JLabel("회원번호:"));
        form.add(memberSearchField);
        form.add(Box.createHorizontalStrut(8));
        form.add(searchBtn);
        tab.add(form, BorderLayout.NORTH);

        tab.add(createTablePanel(), BorderLayout.CENTER);
        return tab;
    }

    /* ─────────────────── 테이블 ─────────────────── */

    private JScrollPane createTablePanel() {
        String[] cols = {
                "고객명", "항공편", "출국시간", "회원등급",
                "주문ID", "상태", "픽업가능시각", "실제픽업"
        };

        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
        table.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        table.setRowHeight(32);
        table.getTableHeader().setFont(new Font("맑은 고딕", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(0xE2E8F0));
        table.setSelectionBackground(new Color(0xDBEAFE));
        table.setGridColor(new Color(0xE5E7EB));

        // 컬럼 폭
        int[] widths = {80, 70, 110, 80, 60, 120, 110, 110};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        // 등급 칩 렌더러
        table.getColumnModel().getColumn(3).setCellRenderer(new ChipRenderer(GRADE_COLORS));

        // 상태 칩 렌더러
        table.getColumnModel().getColumn(5).setCellRenderer(new ChipRenderer(STATE_COLORS));

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(new Color(0xE2E8F0)));
        return sp;
    }

    /* ─────────────────── 데이터 로드 ─────────────────── */

    private void loadAll() {
        try {
            List<PickUpListDTO> list = dao.getAllPickUpList();
            fillTable(list);
        } catch (Exception ex) {
            showError("전체 조회 실패: " + ex.getMessage());
        }
    }

    private void loadByDateRange() {
        try {
            LocalDate start = LocalDate.parse(startDateField.getText().trim());
            LocalDate end   = LocalDate.parse(endDateField.getText().trim());
            List<PickUpListDTO> list = dao.getAllPickUpListByDateRange(start, end);
            fillTable(list);
        } catch (Exception ex) {
            showError("기간별 조회 실패: " + ex.getMessage());
        }
    }

    private void loadByMember() {
        try {
            String input = memberSearchField.getText().trim();
            if (input.isEmpty()) { showError("회원번호를 입력하세요."); return; }
            int memberId = Integer.parseInt(input);

            member.Member m = new member.Member();
            m.setMemberId(memberId);

            List<PickUpListDTO> list = dao.getAllPickUpListByMember(m);
            fillTable(list);
        } catch (NumberFormatException ex) {
            showError("회원번호는 숫자로 입력하세요.");
        } catch (Exception ex) {
            showError("멤버별 조회 실패: " + ex.getMessage());
        }
    }

    private void fillTable(List<PickUpListDTO> list) {
        tableModel.setRowCount(0);

        if (list == null || list.isEmpty()) {
            JOptionPane.showMessageDialog(this, "조회된 픽업 내역이 없습니다.",
                    "알림", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        for (PickUpListDTO dto : list) {
            tableModel.addRow(new Object[]{
                    dto.getMemberName(),
                    dto.getFlightCode(),
                    dto.getDepartureAt() != null ? dto.getDepartureAt().format(DT_FMT) : "-",
                    dto.getGrade() != null ? dto.getGrade() : "SILVER",
                    dto.getOrderId(),
                    dto.getOrderState(),
                    dto.getPickupAvailableAt() != null ? dto.getPickupAvailableAt().format(DT_FMT) : "-",
                    dto.getPickedUpAt() != null ? dto.getPickedUpAt().format(DT_FMT) : "미완료"
            });
        }
    }

    /* ─────────────────── 칩(Chip) 렌더러 ─────────────────── */

    private static class ChipRenderer extends DefaultTableCellRenderer {
        private final Map<String, Color> colorMap;

        ChipRenderer(Map<String, Color> colorMap) {
            this.colorMap = colorMap;
            setHorizontalAlignment(CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable t, Object val, boolean sel, boolean foc, int r, int c) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, val, sel, foc, r, c);
            String key = val != null ? val.toString().toUpperCase() : "";
            Color bg = colorMap.getOrDefault(key, new Color(0x9CA3AF));
            if (!sel) {
                lbl.setOpaque(true);
                lbl.setBackground(bg);
                lbl.setForeground(Color.WHITE);
            }
            lbl.setFont(new Font("맑은 고딕", Font.BOLD, 12));
            return lbl;
        }
    }

    /* ─────────────────── 하단 ─────────────────── */

    private JPanel createBottomBar() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 8));
        p.setBackground(BG);

        JButton back = styledButton("← 메인 메뉴");
        back.addActionListener(e -> screenManager.show("PICKUP_MAIN"));
        p.add(back);
        return p;
    }

    /* ─────────────────── 유틸 ─────────────────── */

    private JButton styledButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(PRIMARY);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(130, 34));
        return btn;
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "오류", JOptionPane.ERROR_MESSAGE);
    }

    private void updateClock() {
        clockLabel.setText(CurrentTime.curTime.format(TIME_FMT));
    }

    private void bindEscKey() {
        getInputMap(WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "goBack");
        getActionMap().put("goBack", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                screenManager.show("PICKUP_MAIN");
            }
        });
    }

    @Override
    public void refresh() {
        if (tableModel != null) tableModel.setRowCount(0);
    }
}
