package gui.brand;

import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import gui.ScreenManager;

public class BrandMainPanel extends JPanel {

    private final ScreenManager screenManager;

    public BrandMainPanel(ScreenManager screenManager) {
        this.screenManager = screenManager;

        setLayout(new GridLayout(7, 1, 10, 10));

        JLabel titleLabel = new JLabel("브랜드 관리자 메인", SwingConstants.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 24));

        JButton stockButton = new JButton("내 브랜드 재고 조회");
        JButton productManageButton = new JButton("상품 등록 / 삭제");
        JButton purchaseButton = new JButton("발주 요청 / 취소");
        JButton purchaseHistoryButton = new JButton("발주 이력 조회");
        JButton logoutButton = new JButton("로그아웃");

        stockButton.setFont(new Font("맑은 고딕", Font.PLAIN, 18));
        productManageButton.setFont(new Font("맑은 고딕", Font.PLAIN, 18));
        purchaseButton.setFont(new Font("맑은 고딕", Font.PLAIN, 18));
        purchaseHistoryButton.setFont(new Font("맑은 고딕", Font.PLAIN, 18));
        logoutButton.setFont(new Font("맑은 고딕", Font.PLAIN, 18));

        stockButton.addActionListener(e -> screenManager.show("BRAND_STOCK"));
        productManageButton.addActionListener(e -> screenManager.show("BRAND_PRODUCT_MANAGE"));
        purchaseButton.addActionListener(e -> screenManager.show("BRAND_PURCHASE"));
        purchaseHistoryButton.addActionListener(e -> screenManager.show("BRAND_PURCHASE_HISTORY"));
        logoutButton.addActionListener(e -> screenManager.show("LOGIN_SELECT"));

        add(titleLabel);
        add(stockButton);
        add(productManageButton);
        add(purchaseButton);
        add(purchaseHistoryButton);
        add(logoutButton);
    }
}