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

import admin.brandmanager.dto.BrandManager;
import admin.brandmanager.service.BrandManagerService;
import brandSystem.BrandSystem;
import exception.DutyFreeException;
import gui.ScreenManager;

public class BrandManagerLoginPanel extends JPanel {

    private final ScreenManager screenManager;
    private final BrandManagerService brandManagerService = new BrandManagerService();

    private JTextField managerIdField;
    private JPasswordField passwordField;

    public BrandManagerLoginPanel(ScreenManager screenManager) {
        this.screenManager = screenManager;

        setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("브랜드 관리자 로그인", SwingConstants.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 26));

        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));

        managerIdField = new JTextField();
        passwordField = new JPasswordField();

        JButton loginButton = new JButton("로그인");
        JButton backButton = new JButton("뒤로가기");

        loginButton.addActionListener(e -> login());
        backButton.addActionListener(e -> screenManager.show("LOGIN_SELECT"));

        formPanel.add(new JLabel("관리자 ID"));
        formPanel.add(managerIdField);

        formPanel.add(new JLabel("비밀번호"));
        formPanel.add(passwordField);

        formPanel.add(new JLabel(""));
        formPanel.add(loginButton);

        formPanel.add(new JLabel(""));
        formPanel.add(backButton);

        add(titleLabel, BorderLayout.NORTH);
        add(formPanel, BorderLayout.CENTER);
    }

    private void login() {
        try {
            String managerIdText = managerIdField.getText().trim();
            String password = new String(passwordField.getPassword());

            if (managerIdText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "관리자 ID를 입력하세요.");
                return;
            }

            if (password.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "비밀번호를 입력하세요.");
                return;
            }

            int managerId = Integer.parseInt(managerIdText);

            BrandManager brandManager =
                    brandManagerService.login(managerId, password);

            BrandSystem brandSystem =
                    new BrandSystem(brandManager.getBrandName());

            screenManager.setBrandSystem(brandSystem);

            JOptionPane.showMessageDialog(
                    this,
                    brandManager.getManagerName()
                            + "님 로그인 성공\n브랜드: "
                            + brandManager.getBrandName()
            );

            clearFields();
            screenManager.show("BRAND_MAIN");

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "관리자 ID는 숫자로 입력해야 합니다.");

        } catch (DutyFreeException e) {
            JOptionPane.showMessageDialog(this, e.getErrorCode().getMessage());

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "로그인 중 오류가 발생했습니다.");
            e.printStackTrace();
        }
    }

    private void clearFields() {
        managerIdField.setText("");
        passwordField.setText("");
    }
}