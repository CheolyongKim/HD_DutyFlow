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
import exception.DutyFreeException;
import gui.ScreenManager;
import gui.common.Refreshable;
import stock.dto.StockProductDto;

public class BrandStockPanel extends JPanel implements Refreshable {

    private final ScreenManager screenManager;

    private JTable stockTable;
    private DefaultTableModel tableModel;

    public BrandStockPanel(ScreenManager screenManager) {
        this.screenManager = screenManager;

        setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("브랜드 재고 조회", SwingConstants.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 24));

        String[] columnNames = {
                "상품명",
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

        // 여기서 loadStockData()를 바로 호출하지 않는 게 좋음
        // 아직 로그인 전이면 BrandSystem이 null일 수 있음
    }

    private void loadStockData() {
        try {
            BrandSystem brandSystem = getLoginBrandSystem();

            if (brandSystem == null) {
                return;
            }

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
                        stock.getCategory() != null ? stock.getCategory().getCategoryName() : "",
                        stock.getCapacity(),
                        stock.getPriceUsd(),
                        stock.getPriceKrw(),
                        stock.getManufacturedDate(),
                        stock.getAmount(),
                        stock.getThresholdValue(),
                        status
                });
            }

        } catch (DutyFreeException e) {
            JOptionPane.showMessageDialog(
                    this,
                    e.getErrorCode().getMessage(),
                    "알림",
                    JOptionPane.WARNING_MESSAGE
            );

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

    @Override
    public void refresh() {
        loadStockData();
    }
}