package gui.member;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDate;

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
import member.MemberSignupDTO;

public class MemberSignupPanel extends JPanel {

    private final ScreenManager screenManager;
    private final MemberService memberService = new MemberService();

    private JTextField loginIdField;
    private JPasswordField passwordField;
    private JTextField nameField;
    private JTextField birthDateField;
    private JTextField phoneNumberField;

    public MemberSignupPanel(ScreenManager screenManager) {
        this.screenManager = screenManager;

        setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("회원가입", SwingConstants.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 26));

        JPanel formPanel = new JPanel(new GridLayout(6, 2, 10, 10));

        loginIdField = new JTextField();
        passwordField = new JPasswordField();
        nameField = new JTextField();
        birthDateField = new JTextField("1999-10-22");
        phoneNumberField = new JTextField("010-1234-5678");

        formPanel.add(new JLabel("아이디"));
        formPanel.add(loginIdField);

        formPanel.add(new JLabel("비밀번호"));
        formPanel.add(passwordField);

        formPanel.add(new JLabel("이름"));
        formPanel.add(nameField);

        formPanel.add(new JLabel("생년월일(yyyy-MM-dd)"));
        formPanel.add(birthDateField);

        formPanel.add(new JLabel("전화번호"));
        formPanel.add(phoneNumberField);

        JButton signupButton = new JButton("회원가입");
        JButton backButton = new JButton("뒤로가기");

        signupButton.addActionListener(e -> signup());
        backButton.addActionListener(e -> screenManager.show("LOGIN_SELECT"));

        formPanel.add(signupButton);
        formPanel.add(backButton);

        add(titleLabel, BorderLayout.NORTH);
        add(formPanel, BorderLayout.CENTER);
    }

    private void signup() {
        try {
            String loginId = loginIdField.getText().trim();
            String password = new String(passwordField.getPassword());
            String name = nameField.getText().trim();
            LocalDate birthDate = LocalDate.parse(birthDateField.getText().trim());
            String phoneNumber = phoneNumberField.getText().trim();

            MemberSignupDTO dto = new MemberSignupDTO(
                    loginId,
                    password,
                    name,
                    birthDate,
                    phoneNumber
            );

            memberService.signup(dto);

            JOptionPane.showMessageDialog(this, "회원가입이 완료되었습니다. 로그인 화면으로 이동합니다.");

            clearFields();
            screenManager.show("MEMBER_LOGIN");

        } catch (DutyFreeException e) {
            JOptionPane.showMessageDialog(this, e.getErrorCode().getMessage());

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "회원가입 중 오류가 발생했습니다. 입력값을 확인해주세요.");
            e.printStackTrace();
        }
    }

    private void clearFields() {
        loginIdField.setText("");
        passwordField.setText("");
        nameField.setText("");
        birthDateField.setText("1999-10-22");
        phoneNumberField.setText("010-1234-5678");
    }
}