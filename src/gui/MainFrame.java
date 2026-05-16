package gui;

import java.awt.CardLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;

import gui.auth.BrandManagerLoginPanel;
import gui.auth.LoginSelectPanel;
import gui.auth.MemberLoginPanel;
import gui.brand.BrandMainPanel;
import gui.brand.BrandOrderHistoryPanel;
import gui.brand.BrandProductListPanel;
import gui.brand.BrandProductManagePanel;
import gui.brand.BrandPurchaseHistoryPanel;
import gui.brand.BrandPurchasePanel;
import gui.brand.BrandStockPanel;
import gui.home.HomePanel;
import gui.member.MemberMainPanel;
import gui.member.MemberPassportPanel;
import gui.member.MemberSignupPanel;
import gui.auth.AirportManagerLoginPanel;
import gui.pickup.PickupMainPanel;

public class MainFrame extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainPanel;
    private ScreenManager screenManager;

    public MainFrame() {
        setTitle("현대면세점 공항 인도장 픽업 예약 관리 시스템");
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        screenManager = new ScreenManager(cardLayout, mainPanel);

        initScreens();

        add(mainPanel);
        screenManager.show("HOME");

        setVisible(true);
    }

    private void initScreens() {
        screenManager.addScreen("HOME", new HomePanel(screenManager));
        screenManager.addScreen("LOGIN_SELECT", new LoginSelectPanel(screenManager));
        screenManager.addScreen("BRAND_MANAGER_LOGIN", new BrandManagerLoginPanel(screenManager));
        screenManager.addScreen("AIRPORT_MANAGER_LOGIN", new AirportManagerLoginPanel(screenManager));

        screenManager.addScreen("BRAND_MAIN", new BrandMainPanel(screenManager));
        screenManager.addScreen("BRAND_STOCK", new BrandStockPanel(screenManager));
        screenManager.addScreen("BRAND_PRODUCT_LIST", new BrandProductListPanel(screenManager));
        screenManager.addScreen("BRAND_ORDER_HISTORY", new BrandOrderHistoryPanel(screenManager));
        screenManager.addScreen("BRAND_PRODUCT_MANAGE", new BrandProductManagePanel(screenManager));
        screenManager.addScreen("BRAND_PURCHASE", new BrandPurchasePanel(screenManager));
        screenManager.addScreen("BRAND_PURCHASE_HISTORY", new BrandPurchaseHistoryPanel(screenManager));
        
        screenManager.addScreen("MEMBER_LOGIN", new MemberLoginPanel(screenManager));
        screenManager.addScreen("MEMBER_SIGNUP", new MemberSignupPanel(screenManager));
        screenManager.addScreen("MEMBER_MAIN", new MemberMainPanel(screenManager));
        screenManager.addScreen("MEMBER_PASSPORT", new MemberPassportPanel(screenManager));
        
        // ── 인도장 GUI 패널 등록 ──
        screenManager.addScreen("PICKUP_MAIN",       new PickupMainPanel(screenManager));
    }
}