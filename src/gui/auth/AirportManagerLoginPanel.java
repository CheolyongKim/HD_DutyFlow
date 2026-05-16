package gui.auth;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import gui.ScreenManager;

/**
 * 인도장 관리자 임시 로그인 패널
 * - 추후 실제 로그인 로직(AirportManagerService.login) 연동 예정
 * - 현재는 [입장] 버튼 클릭 시 바로 PICKUP_MAIN으로 이동
 */
public class AirportManagerLoginPanel extends JPanel {

    private final ScreenManager screenManager;

    private static final Color BG      = new Color(0xF5F6FA);
    private static final Color PRIMARY = new Color(0x2D6CDF);
    private static final Color HOVER   = new Color(0x1B4FAF);

    public AirportManagerLoginPanel(ScreenManager screenManager) {
        this.screenManager = screenManager;

        setLayout(new GridBagLayout());
        setBackground(BG);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE2E8F0), 1),
                new EmptyBorder(40, 60, 40, 60)
        ));

        JLabel icon = new JLabel("🛫");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("인도장 관리자 로그인");
        title.setFont(new Font("맑은 고딕", Font.BOLD, 24));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel notice = new JLabel("(임시) 로그인 기능 구현 전 — 바로 입장합니다");
        notice.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        notice.setForeground(new Color(0x9CA3AF));
        notice.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton enterBtn = new JButton("인도장 시스템 입장 →");
        enterBtn.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        enterBtn.setForeground(Color.WHITE);
        enterBtn.setBackground(PRIMARY);
        enterBtn.setFocusPainted(false);
        enterBtn.setBorderPainted(false);
        enterBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        enterBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        enterBtn.setMaximumSize(new Dimension(300, 48));
        enterBtn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { enterBtn.setBackground(HOVER); }
            @Override public void mouseExited(MouseEvent e)  { enterBtn.setBackground(PRIMARY); }
        });
        enterBtn.addActionListener(e -> screenManager.show("PICKUP_MAIN"));

        JButton backBtn = new JButton("← 돌아가기");
        backBtn.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        backBtn.setForeground(new Color(0x6B7280));
        backBtn.setBackground(BG);
        backBtn.setBorderPainted(false);
        backBtn.setFocusPainted(false);
        backBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        backBtn.addActionListener(e -> screenManager.show("LOGIN_SELECT"));

        card.add(icon);
        card.add(Box.createVerticalStrut(12));
        card.add(title);
        card.add(Box.createVerticalStrut(6));
        card.add(notice);
        card.add(Box.createVerticalStrut(28));
        card.add(enterBtn);
        card.add(Box.createVerticalStrut(12));
        card.add(backBtn);

        add(card);

        // ESC → 뒤로
        getInputMap(WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "goBack");
        getActionMap().put("goBack", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                screenManager.show("LOGIN_SELECT");
            }
        });
    }
}
