package gui.auth;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import exception.DutyFreeException;
import gui.ScreenManager;
import member.MemberService;

public class MemberLoginPanel extends JPanel {

    private final ScreenManager screenManager;
    private final MemberService memberService = new MemberService();

    private JTextField loginIdField;
    private JPasswordField passwordField;

    public MemberLoginPanel(ScreenManager screenManager) {
        this.screenManager = screenManager;

        setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("회원 로그인", SwingConstants.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 26));

        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));

        loginIdField = new JTextField();
        passwordField = new JPasswordField();

        JButton loginButton = new JButton("로그인");
        JButton signupButton = new JButton("회원가입");
        JButton backButton = new JButton("뒤로가기");

        loginButton.addActionListener(e -> login());
        signupButton.addActionListener(e -> screenManager.show("MEMBER_SIGNUP"));
        backButton.addActionListener(e -> screenManager.show("LOGIN_SELECT"));

        formPanel.add(new JLabel("아이디"));
        formPanel.add(loginIdField);

        formPanel.add(new JLabel("비밀번호"));
        formPanel.add(passwordField);

        formPanel.add(loginButton);
        formPanel.add(signupButton);

        formPanel.add(new JLabel(""));
        formPanel.add(backButton);

        add(titleLabel, BorderLayout.NORTH);
        add(formPanel, BorderLayout.CENTER);
    }

    private void login() {
        try {
            String loginId = loginIdField.getText().trim();
            String password = new String(passwordField.getPassword());

            int memberId = memberService.login(loginId, password);

            screenManager.setLoginMemberId(memberId);

            JOptionPane.showMessageDialog(this, "회원 로그인 성공");

            clearFields();
            screenManager.show("MEMBER_MAIN");

        } catch (DutyFreeException e) {
            JOptionPane.showMessageDialog(this, e.getErrorCode().getMessage());

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "로그인 중 오류가 발생했습니다.");
            e.printStackTrace();
        }
    }

    private void clearFields() {
        loginIdField.setText("");
        passwordField.setText("");
    }
}
