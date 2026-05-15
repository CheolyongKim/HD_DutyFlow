package gui.brand;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.math.BigDecimal;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import brandSystem.BrandSystem;
import gui.ScreenManager;
import gui.common.Refreshable;

public class BrandProductManagePanel extends JPanel implements Refreshable {

    private final ScreenManager screenManager;
    private final BrandSystem brandSystem;

    private JTextField categoryNameField;
    private JTextField productNameField;
    private JTextField capacityField;
    private JTextField priceUsdField;
    private JTextField priceKrwField;
    private JTextField thresholdField;
    private JTextField purchaseAmountField;
    private JTextField deleteProductNameField;

    public BrandProductManagePanel(ScreenManager screenManager, BrandSystem brandSystem) {
        this.screenManager = screenManager;
        this.brandSystem = brandSystem;

        setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("상품 등록 / 삭제", SwingConstants.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 24));

        JPanel formPanel = new JPanel(new GridLayout(9, 2, 10, 10));

        categoryNameField = new JTextField("위스키");
        productNameField = new JTextField();
        capacityField = new JTextField("700");
        priceUsdField = new JTextField("80");
        priceKrwField = new JTextField("108000");
        thresholdField = new JTextField("10");
        purchaseAmountField = new JTextField("20");
        deleteProductNameField = new JTextField();

        formPanel.add(new JLabel("카테고리명"));
        formPanel.add(categoryNameField);

        formPanel.add(new JLabel("상품명"));
        formPanel.add(productNameField);

        formPanel.add(new JLabel("용량"));
        formPanel.add(capacityField);

        formPanel.add(new JLabel("달러 가격"));
        formPanel.add(priceUsdField);

        formPanel.add(new JLabel("원화 가격"));
        formPanel.add(priceKrwField);

        formPanel.add(new JLabel("재고 임계값"));
        formPanel.add(thresholdField);

        formPanel.add(new JLabel("발주 수량"));
        formPanel.add(purchaseAmountField);

        formPanel.add(new JLabel("삭제할 상품명"));
        formPanel.add(deleteProductNameField);

        JPanel buttonPanel = new JPanel();

        JButton registerButton = new JButton("신규 상품 등록");
        JButton registerAndPurchaseButton = new JButton("신규 상품 등록 + 발주");
        JButton deleteButton = new JButton("상품 삭제");
        JButton backButton = new JButton("뒤로가기");

        registerButton.addActionListener(e -> registerNewProduct());
        registerAndPurchaseButton.addActionListener(e -> registerNewProductAndPurchase());
        deleteButton.addActionListener(e -> deleteProduct());

        backButton.addActionListener(e -> {
            resetForm();
            screenManager.show("BRAND_MAIN");
        });

        buttonPanel.add(registerButton);
        buttonPanel.add(registerAndPurchaseButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(backButton);

        add(titleLabel, BorderLayout.NORTH);
        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    @Override
    public void refresh() {
        resetForm();
    }

    private void resetForm() {
        categoryNameField.setText("위스키");
        productNameField.setText("");
        capacityField.setText("700");
        priceUsdField.setText("80");
        priceKrwField.setText("108000");
        thresholdField.setText("10");
        purchaseAmountField.setText("20");
        deleteProductNameField.setText("");
    }

    private void registerNewProduct() {
        try {
            String categoryName = categoryNameField.getText();
            String productName = productNameField.getText();
            int capacity = Integer.parseInt(capacityField.getText());
            BigDecimal priceUsd = new BigDecimal(priceUsdField.getText());
            BigDecimal priceKrw = new BigDecimal(priceKrwField.getText());
            int thresholdValue = Integer.parseInt(thresholdField.getText());

            brandSystem.registerNewProduct(
                    categoryName,
                    productName,
                    capacity,
                    priceUsd,
                    priceKrw,
                    thresholdValue
            );

            JOptionPane.showMessageDialog(this, "신규 상품 등록 요청 완료");
            resetForm();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "용량, 가격, 임계값은 숫자로 입력해야 합니다.");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "신규 상품 등록 중 오류가 발생했습니다.");
            e.printStackTrace();
        }
    }

    private void registerNewProductAndPurchase() {
        try {
            String categoryName = categoryNameField.getText();
            String productName = productNameField.getText();
            int capacity = Integer.parseInt(capacityField.getText());
            BigDecimal priceUsd = new BigDecimal(priceUsdField.getText());
            BigDecimal priceKrw = new BigDecimal(priceKrwField.getText());
            int thresholdValue = Integer.parseInt(thresholdField.getText());
            int purchaseAmount = Integer.parseInt(purchaseAmountField.getText());

            brandSystem.registerNewProductAndPurchase(
                    categoryName,
                    productName,
                    capacity,
                    priceUsd,
                    priceKrw,
                    thresholdValue,
                    purchaseAmount
            );

            JOptionPane.showMessageDialog(this, "신규 상품 등록 및 발주 요청 완료");
            resetForm();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "용량, 가격, 임계값, 발주 수량은 숫자로 입력해야 합니다.");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "신규 상품 등록 및 발주 중 오류가 발생했습니다.");
            e.printStackTrace();
        }
    }

    private void deleteProduct() {
        try {
            String productName = deleteProductNameField.getText();

            if (productName == null || productName.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "삭제할 상품명을 입력하세요.");
                return;
            }

            brandSystem.deleteProduct(productName);

            JOptionPane.showMessageDialog(this, "상품 삭제 요청 완료");
            resetForm();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "상품 삭제 중 오류가 발생했습니다.");
            e.printStackTrace();
        }
    }
}