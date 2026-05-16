package gui.brand;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import brandSystem.BrandSystem;
import exception.DutyFreeException;
import gui.ScreenManager;
import gui.common.Refreshable;

public class BrandProductManagePanel extends JPanel implements Refreshable {

    private final ScreenManager screenManager;

    private JTextField categoryNameField;
    private JTextField productNameField;
    private JTextField capacityField;
    private JTextField priceUsdField;
    private JTextField priceKrwField;
    private JTextField thresholdField;
    private JTextField purchaseAmountField;
    private JTextField deleteProductNameField;

    public BrandProductManagePanel(ScreenManager screenManager) {
        this.screenManager = screenManager;
        setLayout(new BorderLayout(20, 20));
        setBorder(new EmptyBorder(30, 50, 30, 50));

        // 상단 타이틀
        JLabel titleLabel = new JLabel("상품 등록 및 관리", SwingConstants.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 28));
        add(titleLabel, BorderLayout.NORTH);

        // 중앙 폼 패널 (GridBagLayout 사용으로 정렬 최적화)
        JPanel centerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        // --- 등록 폼 그룹 ---
        JPanel registerGroup = new JPanel(new GridBagLayout());
        registerGroup.setBorder(new TitledBorder(null, "신규 상품 정보", TitledBorder.LEADING, TitledBorder.TOP, new Font("맑은 고딕", Font.BOLD, 14), Color.BLUE));

        categoryNameField = new JTextField(15);
        productNameField = new JTextField(15);
        capacityField = new JTextField(15);
        priceUsdField = new JTextField(15);
        priceKrwField = new JTextField(15);
        thresholdField = new JTextField(15);
        purchaseAmountField = new JTextField(15);

        addFormField(registerGroup, "카테고리명", categoryNameField, 0);
        addFormField(registerGroup, "상품명", productNameField, 1);
        addFormField(registerGroup, "용량 (ml)", capacityField, 2);
        addFormField(registerGroup, "달러 가격 ($)", priceUsdField, 3);
        addFormField(registerGroup, "원화 가격 (￦)", priceKrwField, 4);
        addFormField(registerGroup, "재고 임계값", thresholdField, 5);
        addFormField(registerGroup, "초기 발주 수량", purchaseAmountField, 6);

        // --- 삭제 폼 그룹 ---
        JPanel deleteGroup = new JPanel(new GridBagLayout());
        deleteGroup.setBorder(new TitledBorder(null, "상품 삭제", TitledBorder.LEADING, TitledBorder.TOP, new Font("맑은 고딕", Font.BOLD, 14), Color.RED));

        deleteProductNameField = new JTextField(15);
        addFormField(deleteGroup, "삭제할 상품명", deleteProductNameField, 0);

        // 메인 센터에 그룹들 배치
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1.0;
        centerPanel.add(registerGroup, gbc);
        gbc.gridy = 1;
        centerPanel.add(deleteGroup, gbc);

        add(centerPanel, BorderLayout.CENTER);

        // 하단 버튼 패널
        JPanel buttonPanel = new JPanel();
        JButton registerButton = new JButton("상품 등록");
        JButton registerAndPurchaseButton = new JButton("등록 + 즉시 발주");
        JButton deleteButton = new JButton("상품 삭제");
        JButton backButton = new JButton("뒤로가기");

        // 버튼 스타일 (선택 사항)
        registerAndPurchaseButton.setBackground(new Color(230, 242, 255));
        deleteButton.setForeground(Color.RED);

        registerButton.addActionListener(e -> registerNewProduct());
        registerAndPurchaseButton.addActionListener(e -> registerNewProductAndPurchase());
        deleteButton.addActionListener(e -> deleteProduct());
        backButton.addActionListener(e -> screenManager.show("BRAND_MAIN"));

        buttonPanel.add(registerButton);
        buttonPanel.add(registerAndPurchaseButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(backButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void addFormField(JPanel panel, String labelText, JTextField field, int row) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = row;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 10, 5, 5);
        panel.add(new JLabel(labelText), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 10);
        panel.add(field, gbc);
    }

    @Override
    public void refresh() {
        resetForm();
    }

    private void resetForm() {
        categoryNameField.setText("");
        productNameField.setText("");
        capacityField.setText("");
        priceUsdField.setText("");
        priceKrwField.setText("");
        thresholdField.setText("");
        purchaseAmountField.setText("");
        deleteProductNameField.setText("");
    }

    private void registerNewProduct() {
        try {
            BrandSystem brandSystem = getLoginBrandSystem();
            if (brandSystem == null) return;

            validateRegistrationFields();

            brandSystem.registerNewProduct(
                    categoryNameField.getText().trim(),
                    productNameField.getText().trim(),
                    Integer.parseInt(capacityField.getText().trim()),
                    new BigDecimal(priceUsdField.getText().trim()),
                    new BigDecimal(priceKrwField.getText().trim()),
                    Integer.parseInt(thresholdField.getText().trim())
            );

            JOptionPane.showMessageDialog(this, "신규 상품이 성공적으로 등록되었습니다.");
            resetForm();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "수치 데이터(용량, 가격, 임계값)를 올바르게 입력해주세요.");
        } catch (DutyFreeException e) {
            JOptionPane.showMessageDialog(this, e.getErrorCode().getMessage(), "등록 실패", JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "오류가 발생했습니다: " + e.getMessage());
        }
    }

    private void registerNewProductAndPurchase() {
        try {
            BrandSystem brandSystem = getLoginBrandSystem();
            if (brandSystem == null) return;

            validateRegistrationFields();
            if (purchaseAmountField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "발주 수량을 입력해주세요.");
                return;
            }

            brandSystem.registerNewProductAndPurchase(
                    categoryNameField.getText().trim(),
                    productNameField.getText().trim(),
                    Integer.parseInt(capacityField.getText().trim()),
                    new BigDecimal(priceUsdField.getText().trim()),
                    new BigDecimal(priceKrwField.getText().trim()),
                    Integer.parseInt(thresholdField.getText().trim()),
                    Integer.parseInt(purchaseAmountField.getText().trim())
            );

            JOptionPane.showMessageDialog(this, "상품 등록 및 발주 요청이 완료되었습니다.");
            resetForm();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "숫자 입력란을 다시 확인해주세요.");
        } catch (DutyFreeException e) {
            JOptionPane.showMessageDialog(this, e.getErrorCode().getMessage(), "실패", JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "오류가 발생했습니다.");
        }
    }

    private void deleteProduct() {
        try {
            BrandSystem brandSystem = getLoginBrandSystem();
            if (brandSystem == null) return;

            String productName = deleteProductNameField.getText().trim();
            if (productName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "삭제할 상품명을 입력하세요.");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this, 
                " 정말로 '" + productName + "' 상품을 삭제하시겠습니까?", "삭제 확인", JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                brandSystem.deleteProduct(productName);
                JOptionPane.showMessageDialog(this, "상품 삭제가 완료되었습니다.");
                resetForm();
            }
        } catch (DutyFreeException e) {
            JOptionPane.showMessageDialog(this, e.getErrorCode().getMessage(), "삭제 실패", JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "오류가 발생했습니다.");
        }
    }

    private void validateRegistrationFields() {
        if (categoryNameField.getText().trim().isEmpty() || productNameField.getText().trim().isEmpty()) {
            throw new IllegalArgumentException("카테고리와 상품명은 필수 입력 항목입니다.");
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