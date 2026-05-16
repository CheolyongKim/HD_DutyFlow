package gui.auth;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
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
        setLayout(new GridBagLayout()); // 중앙 배치를 위해 GridBagLayout 사용
        setBackground(new Color(236, 240, 241));

        // 로그인 카드 패널
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(200, 200, 200), 1),
            new EmptyBorder(40, 40, 40, 40)
        ));

        // 타이틀
        JLabel titleLabel = new JLabel("BRAND ADMIN LOGIN");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setBorder(new EmptyBorder(0, 0, 30, 0));

        // 입력 필드 세트
        managerIdField = new JTextField(15);
        passwordField = new JPasswordField(15);
        
        setupInput(card, "관리자 ID", managerIdField);
        setupInput(card, "비밀번호", passwordField);

        // 버튼 패널
        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        btnPanel.setOpaque(false);
        btnPanel.setMaximumSize(new Dimension(300, 45));

        JButton loginButton = new JButton("로그인");
        loginButton.setBackground(new Color(52, 152, 219));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        loginButton.addActionListener(e -> handleLogin());

        JButton backButton = new JButton("뒤로가기");
        backButton.addActionListener(e -> screenManager.show("LOGIN_SELECT"));

        btnPanel.add(loginButton);
        btnPanel.add(backButton);

        card.add(titleLabel);
        card.add(btnPanel);
        add(card);
    }

    private void setupInput(JPanel card, String labelText, JTextField field) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        field.setMaximumSize(new Dimension(300, 35));
        
        card.add(label);
        card.add(Box.createVerticalStrut(5));
        card.add(field);
        card.add(Box.createVerticalStrut(15));
    }

    // handleLogin() 로직은 기존 코드와 동일하게 유지
    private void handleLogin() { 
        /* 기존 로직 생략 */ 
        String managerIdText = managerIdField.getText();
        String password = new String(passwordField.getPassword());
        try {
            if (managerIdText.isEmpty() || password.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "ID와 비밀번호를 입력하세요.");
                return;
            }
            int managerId = Integer.parseInt(managerIdText);
            BrandManager brandManager = brandManagerService.login(managerId, password);
            BrandSystem brandSystem = new BrandSystem(brandManager.getBrandName());
            screenManager.setBrandSystem(brandSystem);
            JOptionPane.showMessageDialog(this, brandManager.getManagerName() + "님 환영합니다.");
            managerIdField.setText("");
            passwordField.setText("");
            screenManager.show("BRAND_MAIN");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "로그인 정보가 올바르지 않습니다.");
        }
    }
}