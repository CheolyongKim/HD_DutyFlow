package gui.member;

import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import gui.ScreenManager;

public class MemberMainPanel extends JPanel {

    private final ScreenManager screenManager;

    public MemberMainPanel(ScreenManager screenManager) {
        this.screenManager = screenManager;

        setLayout(new GridLayout(5, 1, 10, 10));

        JLabel titleLabel = new JLabel("회원 메인", SwingConstants.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 26));

        JButton passportButton = new JButton("여권 정보 등록/갱신");
        JButton productButton = new JButton("상품 조회");
        JButton cartButton = new JButton("장바구니");
        JButton logoutButton = new JButton("로그아웃");

        passportButton.addActionListener(e -> screenManager.show("MEMBER_PASSPORT"));
        productButton.addActionListener(e -> JOptionPane.showMessageDialog(this, "상품 조회 화면 연결 예정"));
        cartButton.addActionListener(e -> JOptionPane.showMessageDialog(this, "장바구니 화면 연결 예정"));

        logoutButton.addActionListener(e -> {
            screenManager.clearLoginMemberId();
            screenManager.show("LOGIN_SELECT");
        });

        add(titleLabel);
        add(passportButton);
        add(productButton);
        add(cartButton);
        add(logoutButton);
    }
}