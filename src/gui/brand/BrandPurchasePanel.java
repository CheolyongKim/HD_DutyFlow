package gui.brand;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import brandSystem.BrandSystem;
import exception.DutyFreeException;
import gui.ScreenManager;

public class BrandPurchasePanel extends JPanel {

    private final ScreenManager screenManager;

    private JTextField productNameField;
    private JTextField amountField;
    private JTextField cancelPurchaseIdField;

    public BrandPurchasePanel(ScreenManager screenManager) {
        this.screenManager = screenManager;

        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(30, 50, 30, 50)); // 화면 여백

        // ── 상단 타이틀 ──
        JLabel titleLabel = new JLabel("브랜드 발주 요청 및 취소", SwingConstants.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 28));
        titleLabel.setBorder(new EmptyBorder(0, 0, 30, 0));
        add(titleLabel, BorderLayout.NORTH);

        // ── 중앙 컨텐츠 패널 (발주 요청과 취소를 분리) ──
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 30, 0));
        centerPanel.setOpaque(false);

        // 1. 발주 요청 섹션
        JPanel requestPanel = createSectionPanel("새 발주 요청");
        JPanel requestForm = new JPanel(new GridLayout(2, 2, 10, 20));
        requestForm.setOpaque(false);

        productNameField = new JTextField();
        amountField = new JTextField();

        requestForm.add(createFieldLabel("상품명:"));
        requestForm.add(productNameField);
        requestForm.add(createFieldLabel("발주 수량:"));
        requestForm.add(amountField);

        JButton purchaseButton = createStyledButton("발주 요청하기", new Color(0x2D6CDF));
        purchaseButton.addActionListener(e -> requestPurchase());

        requestPanel.add(requestForm, BorderLayout.CENTER);
        requestPanel.add(purchaseButton, BorderLayout.SOUTH);

        // 2. 발주 취소 섹션
        JPanel cancelPanel = createSectionPanel("발주 취소");
        JPanel cancelForm = new JPanel(new GridLayout(2, 2, 10, 20));
        cancelForm.setOpaque(false);

        cancelPurchaseIdField = new JTextField();

        cancelForm.add(createFieldLabel("발주 ID:"));
        cancelForm.add(cancelPurchaseIdField);
        cancelForm.add(new JLabel("")); // 레이아웃 유지용 빈 라벨
        cancelForm.add(new JLabel("<html><font color='gray'>* 이력 조회에서 ID 확인</font></html>"));

        JButton cancelButton = createStyledButton("발주 취소하기", new Color(0xE74C3C));
        cancelButton.addActionListener(e -> cancelPurchase());

        cancelPanel.add(cancelForm, BorderLayout.CENTER);
        cancelPanel.add(cancelButton, BorderLayout.SOUTH);

        centerPanel.add(requestPanel);
        centerPanel.add(cancelPanel);
        add(centerPanel, BorderLayout.CENTER);

        // ── 하단 뒤로가기 버튼 ──
        JButton backButton = new JButton("뒤로가기");
        backButton.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        backButton.addActionListener(e -> screenManager.show("BRAND_MAIN"));
        
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setBorder(new EmptyBorder(20, 0, 0, 0));
        bottomPanel.add(backButton);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    // 섹션 패널 생성을 위한 헬퍼 메소드
    private JPanel createSectionPanel(String title) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(Color.LIGHT_GRAY), 
                        title, TitledBorder.LEFT, TitledBorder.TOP, 
                        new Font("맑은 고딕", Font.BOLD, 18)
                ),
                new EmptyBorder(20, 20, 20, 20)
        ));
        panel.setBackground(Color.WHITE);
        return panel;
    }

    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        return label;
    }

    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(0, 45));
        return btn;
    }

    private void requestPurchase() {
        try {
            BrandSystem brandSystem = getLoginBrandSystem();
            if (brandSystem == null) return;

            String productName = productNameField.getText().trim();
            String amountText = amountField.getText().trim();

            if (productName.isEmpty() || amountText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "상품명과 수량을 모두 입력하세요.");
                return;
            }

            int amount = Integer.parseInt(amountText);
            brandSystem.makePurchase(productName, amount);

            JOptionPane.showMessageDialog(this, "발주 요청이 완료되었습니다.");
            productNameField.setText("");
            amountField.setText("");

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "수량은 숫자로 입력해야 합니다.");
        } catch (DutyFreeException e) {
            JOptionPane.showMessageDialog(this, e.getErrorCode().getMessage(), "알림", JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "예상하지 못한 오류가 발생했습니다.");
            e.printStackTrace();
        }
    }

    private void cancelPurchase() {
        try {
            BrandSystem brandSystem = getLoginBrandSystem();
            if (brandSystem == null) return;

            String idText = cancelPurchaseIdField.getText().trim();
            if (idText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "취소할 발주 ID를 입력하세요.");
                return;
            }

            int purchaseId = Integer.parseInt(idText);
            brandSystem.cancelPurchase(purchaseId);

            JOptionPane.showMessageDialog(this, "발주 취소 요청이 완료되었습니다.");
            cancelPurchaseIdField.setText("");

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "발주 ID는 숫자로 입력해야 합니다.");
        } catch (DutyFreeException e) {
            JOptionPane.showMessageDialog(this, e.getErrorCode().getMessage(), "알림", JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "예상하지 못한 오류가 발생했습니다.");
            e.printStackTrace();
        }
    }

    private BrandSystem getLoginBrandSystem() {
        BrandSystem brandSystem = screenManager.getBrandSystem();
        if (brandSystem == null) {
            JOptionPane.showMessageDialog(this, "브랜드 관리자 로그인이 필요합니다.");
            screenManager.show("BRAND_MANAGER_LOGIN");
            return null;
        }
        return brandSystem;
    }
}