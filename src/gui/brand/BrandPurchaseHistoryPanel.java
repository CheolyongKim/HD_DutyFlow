package gui.brand;

import java.awt.BorderLayout;
import java.awt.Font;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

import brandSystem.BrandSystem;
import gui.ScreenManager;
import gui.common.Refreshable;
import stock.domain.StockPurchase;

public class BrandPurchaseHistoryPanel extends JPanel  implements Refreshable {

    private final ScreenManager screenManager;
    private final BrandSystem brandSystem;

    private DefaultTableModel tableModel;
    private JTable purchaseTable;

    public BrandPurchaseHistoryPanel(ScreenManager screenManager, BrandSystem brandSystem) {
        this.screenManager = screenManager;
        this.brandSystem = brandSystem;

        setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("브랜드 발주 이력 조회", SwingConstants.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 24));

        String[] columns = {
                "발주ID", "상품ID", "발주일시", "수량", "상태"
        };

        tableModel = new DefaultTableModel(columns, 0);
        purchaseTable = new JTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(purchaseTable);

        JButton refreshButton = new JButton("새로고침");
        JButton backButton = new JButton("뒤로가기");

        refreshButton.addActionListener(e -> loadPurchaseHistory());
        backButton.addActionListener(e -> screenManager.show("BRAND_MAIN"));

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(refreshButton);
        bottomPanel.add(backButton);

        add(titleLabel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        loadPurchaseHistory();
    }

    private void loadPurchaseHistory() {
        try {
            tableModel.setRowCount(0);

            List<StockPurchase> purchases =
                    brandSystem.getPurchaseService()
                            .getPurchaseHistoryByBrandName(brandSystem.getBrandName());

            for (StockPurchase purchase : purchases) {
                tableModel.addRow(new Object[] {
                        purchase.getPurchaseId(),
                        purchase.getProductId(),
                        purchase.getPurchaseDate(),
                        purchase.getAmount(),
                        purchase.getStatus()
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void refresh() {
    	loadPurchaseHistory();
    }
}