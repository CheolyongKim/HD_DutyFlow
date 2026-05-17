package gui.exchangerate;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import gui.ScreenManager;
import gui.common.Refreshable;

public class ExchangeRatePanel extends JPanel implements Refreshable {

    private final ScreenManager screenManager;

    private JLabel todayLabel;
    private JTextArea graphArea;
    private JLabel summaryLabel;

    public ExchangeRatePanel(ScreenManager screenManager) {
        this.screenManager = screenManager;

        setLayout(new BorderLayout());
        setBackground(new Color(245, 246, 250));
        setBorder(new EmptyBorder(30, 40, 30, 40));

        JLabel titleLabel = new JLabel("환율 조회", SwingConstants.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 28));
        titleLabel.setBorder(new EmptyBorder(0, 0, 20, 0));

        add(titleLabel, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.addTab("오늘 환율", createTodayPanel());
        tabbedPane.addTab("최근 1주일", createGraphPanel("최근 1주일 환율 변화"));
        tabbedPane.addTab("최근 1개월", createGraphPanel("최근 1개월 환율 변화"));

        add(tabbedPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        bottomPanel.setOpaque(false);

        JButton backButton = new JButton("돌아가기");
        backButton.addActionListener(e -> screenManager.show("MEMBER_MAIN"));

        bottomPanel.add(backButton);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createTodayPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        todayLabel = new JLabel("오늘 환율: 1 USD = 1,354.20 KRW", SwingConstants.CENTER);
        todayLabel.setFont(new Font("맑은 고딕", Font.BOLD, 26));

        panel.add(todayLabel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createGraphPanel(String title) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 20));

        graphArea = new JTextArea();
        graphArea.setEditable(false);
        graphArea.setFont(new Font("Consolas", Font.PLAIN, 16));
        graphArea.setText(
                "2026-05-10 | 1342.10 | ████████\n" +
                "2026-05-11 | 1346.40 | █████████\n" +
                "2026-05-12 | 1349.20 | ██████████\n" +
                "2026-05-13 | 1351.00 | ███████████\n" +
                "2026-05-14 | 1350.70 | ███████████\n" +
                "2026-05-15 | 1353.10 | ████████████\n" +
                "2026-05-16 | 1354.20 | █████████████\n"
        );

        summaryLabel = new JLabel("최고: 1,354.20 / 최저: 1,342.10 / 평균: 1,349.53", SwingConstants.CENTER);
        summaryLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(new JScrollPane(graphArea), BorderLayout.CENTER);
        panel.add(summaryLabel, BorderLayout.SOUTH);

        return panel;
    }

    @Override
    public void refresh() {
        // 나중에 DutyFlowSystem 환율 조회 연결 예정
    }
}
