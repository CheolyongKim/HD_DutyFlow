package gui.member;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDate;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import exception.DutyFreeException;
import gui.ScreenManager;
import member.MemberService;

public class MemberPassportPanel extends JPanel {

    private final ScreenManager screenManager;
    private final MemberService memberService = new MemberService();

    private JTextField passportNumberField;
    private JTextField passportExpiryDateField;

    public MemberPassportPanel(ScreenManager screenManager) {
        this.screenManager = screenManager;

        setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("여권 정보 등록", SwingConstants.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 26));

        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));

        passportNumberField = new JTextField("M123A4567");
        passportExpiryDateField = new JTextField("2030-12-31");

        JButton registerButton = new JButton("여권 등록");
        JButton backButton = new JButton("뒤로가기");

        registerButton.addActionListener(e -> registerPassport());
        backButton.addActionListener(e -> screenManager.show("MEMBER_MAIN"));

        formPanel.add(new JLabel("여권번호"));
        formPanel.add(passportNumberField);

        formPanel.add(new JLabel("여권 만료일(yyyy-MM-dd)"));
        formPanel.add(passportExpiryDateField);

        formPanel.add(registerButton);
        formPanel.add(backButton);

        add(titleLabel, BorderLayout.NORTH);
        add(formPanel, BorderLayout.CENTER);
    }

    private void registerPassport() {
        try {
            Integer memberId = screenManager.getLoginMemberId();

            if (memberId == null) {
                JOptionPane.showMessageDialog(this, "회원 로그인이 필요합니다.");
                screenManager.show("MEMBER_LOGIN");
                return;
            }

            String passportNum = passportNumberField.getText().trim();
            LocalDate passportExpiredDate = LocalDate.parse(passportExpiryDateField.getText().trim());

            memberService.registerPassport(memberId, passportNum, passportExpiredDate);

            JOptionPane.showMessageDialog(this, "여권 정보가 등록되었습니다.");

            screenManager.show("MEMBER_MAIN");

        } catch (DutyFreeException e) {
            JOptionPane.showMessageDialog(this, e.getErrorCode().getMessage());

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "여권 정보 등록 중 오류가 발생했습니다. 입력값을 확인해주세요.");
            e.printStackTrace();
        }
    }
}