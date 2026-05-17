package gui;

import java.awt.CardLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;

import brandSystem.BrandSystem;
import gui.common.Refreshable;
import gui.fakedata.FakeOrder;
import gui.fakedata.FakeProduct;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScreenManager {

    private final CardLayout cardLayout;
    private final JPanel mainPanel;
    private final Map<String, JPanel> screens = new HashMap<>();
    
    // 여권정보 입력 유무 확인
    private boolean passportAfterSignup;

    private BrandSystem brandSystem;
    private Integer loginMemberId;
    private FakeProduct selectedProduct;
    private FakeOrder selectedOrder;

	
    public ScreenManager(CardLayout cardLayout, JPanel mainPanel) {
        this.cardLayout = cardLayout;
        this.mainPanel = mainPanel;
    }

    public void addScreen(String name, JPanel panel) {
        screens.put(name, panel);
        mainPanel.add(panel, name);
    }

    public void show(String name) {
        JPanel panel = screens.get(name);

        if (panel instanceof Refreshable) {
            ((Refreshable) panel).refresh();
        }

        cardLayout.show(mainPanel, name);
    }

    public void clearBrandSystem() {
        this.brandSystem = null;
    }
    
    public void clearLoginMemberId() {
        this.loginMemberId = null;
    }

    public boolean isMemberLoggedIn() {
        return loginMemberId != null;
    }
    public boolean isPassportAfterSignup() {
        return passportAfterSignup;
    }

    public void setPassportAfterSignup(boolean passportAfterSignup) {
        this.passportAfterSignup = passportAfterSignup;
    }
    
    
}