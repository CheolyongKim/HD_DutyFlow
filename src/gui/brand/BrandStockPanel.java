package gui.brand;

import java.awt.BorderLayout;
import java.awt.Font;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

import brandSystem.BrandSystem;
import gui.ScreenManager;
import gui.common.Refreshable;
import stock.dto.StockProductDto;

public class BrandStockPanel extends JPanel implements Refreshable {

    private final ScreenManager screenManager;
    private final BrandSystem brandSystem;

    private JTable stockTable;
    private DefaultTableModel tableModel;

    public BrandStockPanel(ScreenManager screenManager, BrandSystem brandSystem) {
        this.screenManager = screenManager;
        this.brandSystem = brandSystem;

        setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("브랜드 재고 조회", SwingConstants.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 24));

        String[] columnNames = {
                "상품명",
                "브랜드",
                "카테고리",
                "용량",
                "가격($)",
                "가격(원)",
                "제조일자",
                "재고수량",
                "임계값",
                "상태"
        };

        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        stockTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(stockTable);

        JButton refreshButton = new JButton("새로고침");
        JButton backButton = new JButton("뒤로가기");

        refreshButton.addActionListener(e -> loadStockData());
        backButton.addActionListener(e -> screenManager.show("BRAND_MAIN"));

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(refreshButton);
        bottomPanel.add(backButton);

        add(titleLabel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        loadStockData();
    }

    private void loadStockData() {
        try {
            tableModel.setRowCount(0);

            List<StockProductDto> stockList = brandSystem.getMyBrandStocks();

            for (StockProductDto stock : stockList) {
                int amount = stock.getAmount();
                int thresholdValue = stock.getThresholdValue();

                String status;

                if (amount == 0) {
                    status = "품절";
                } else if (amount <= thresholdValue) {
                    status = "재고 부족";
                } else {
                    status = "판매중";
                }

                tableModel.addRow(new Object[] {
                        stock.getProductName(),
                        stock.getBrandName(),
                        stock.getCategory().getCategoryName(),
                        stock.getCapacity(),
                        stock.getPriceUsd(),
                        stock.getPriceKrw(),
                        stock.getManufacturedDate(),
                        stock.getAmount(),
                        stock.getThresholdValue(),
                        status
                });
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "재고 데이터를 불러오는 중 오류가 발생했습니다.");
            e.printStackTrace();
        }
    }
    @Override
    public void refresh() {
    	loadStockData();
    }
    
}