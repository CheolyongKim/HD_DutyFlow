package gui.auth;

import java.awt.GridLayout;
import java.awt.Font;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import gui.ScreenManager;

public class LoginSelectPanel extends JPanel {

    private final ScreenManager screenManager;

    public LoginSelectPanel(ScreenManager screenManager) {
        this.screenManager = screenManager;

        setLayout(new GridLayout(6, 1, 10, 10));

        JLabel titleLabel = new JLabel("로그인 유형을 선택하세요", SwingConstants.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 24));

        JButton memberSignupButton = new JButton("회원가입");
        JButton memberLoginButton = new JButton("회원 로그인");
        JButton brandLoginButton = new JButton("브랜드 관리자 로그인");
        JButton dutyFreeManagerLoginButton = new JButton("면세 시스템 관리자 로그인");
        JButton backButton = new JButton("처음으로");

        memberLoginButton.setFont(new Font("맑은 고딕", Font.PLAIN, 18));
        memberSignupButton.setFont(new Font("맑은 고딕", Font.PLAIN, 18));
        brandLoginButton.setFont(new Font("맑은 고딕", Font.PLAIN, 18));
        dutyFreeManagerLoginButton.setFont(new Font("맑은 고딕", Font.PLAIN, 18));
        backButton.setFont(new Font("맑은 고딕", Font.PLAIN, 18));

        memberLoginButton.addActionListener(e -> screenManager.show("MEMBER_LOGIN"));
        memberSignupButton.addActionListener(e -> screenManager.show("MEMBER_SIGNUP"));

        brandLoginButton.addActionListener(e -> screenManager.show("BRAND_MANAGER_LOGIN"));

        dutyFreeManagerLoginButton.addActionListener(
                e -> screenManager.show("AIRPORT_MANAGER_LOGIN")
        );

        backButton.addActionListener(e -> screenManager.show("HOME"));

        add(titleLabel);
        add(memberSignupButton);
        add(memberLoginButton);
        add(brandLoginButton);
        add(dutyFreeManagerLoginButton);
        add(backButton);
    }
}