package gui.brand;

import java.awt.GridLayout;
import java.awt.Font;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;

import brandSystem.BrandSystem;
import gui.ScreenManager;
import gui.common.Refreshable;

public class BrandPurchasePanel extends JPanel {

    private final ScreenManager screenManager;
    private final BrandSystem brandSystem;

    private JTextField productNameField;
    private JTextField amountField;
    private JTextField cancelPurchaseIdField;

    public BrandPurchasePanel(ScreenManager screenManager, BrandSystem brandSystem) {
        this.screenManager = screenManager;
        this.brandSystem = brandSystem;

        setLayout(new GridLayout(8, 2, 10, 10));

        JLabel titleLabel = new JLabel("브랜드 발주 요청 / 취소", SwingConstants.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 22));

        productNameField = new JTextField();
        amountField = new JTextField();
        cancelPurchaseIdField = new JTextField();

        JButton purchaseButton = new JButton("발주 요청");
        JButton cancelButton = new JButton("발주 취소");
        JButton backButton = new JButton("뒤로가기");

        purchaseButton.addActionListener(e -> requestPurchase());
        cancelButton.addActionListener(e -> cancelPurchase());
        backButton.addActionListener(e -> screenManager.show("BRAND_MAIN"));

        add(titleLabel);
        add(new JLabel(""));

        add(new JLabel("상품명"));
        add(productNameField);

        add(new JLabel("발주 수량"));
        add(amountField);

        add(new JLabel(""));
        add(purchaseButton);

        add(new JLabel("취소할 발주 ID"));
        add(cancelPurchaseIdField);

        add(new JLabel(""));
        add(cancelButton);

        add(new JLabel(""));
        add(backButton);
    }

    private void requestPurchase() {
        try {
            String productName = productNameField.getText();
            int amount = Integer.parseInt(amountField.getText());

            brandSystem.makePurchase(productName, amount);

            JOptionPane.showMessageDialog(this, "발주 요청 완료");

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "수량은 숫자로 입력해야 합니다.");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "발주 요청 중 오류가 발생했습니다.");
            e.printStackTrace();
        }
    }

    private void cancelPurchase() {
        try {
            int purchaseId = Integer.parseInt(cancelPurchaseIdField.getText());

            brandSystem.cancelPurchase(purchaseId);

            JOptionPane.showMessageDialog(this, "발주 취소 요청 완료");

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "발주 ID는 숫자로 입력해야 합니다.");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "발주 취소 중 오류가 발생했습니다.");
            e.printStackTrace();
        }
    }

}