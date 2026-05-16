package gui;

import java.awt.CardLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;

import brandSystem.BrandSystem;
import gui.common.Refreshable;

public class ScreenManager {

    private final CardLayout cardLayout;
    private final JPanel mainPanel;
    private final Map<String, JPanel> screens = new HashMap<>();

    private BrandSystem brandSystem;
	
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

    public void setBrandSystem(BrandSystem brandSystem) {
        this.brandSystem = brandSystem;
    }

    public BrandSystem getBrandSystem() {
        return brandSystem;
    }

    public void clearBrandSystem() {
        this.brandSystem = null;
    }
}