package gui.brand;

import java.awt.BorderLayout;
import java.awt.Font;
import java.io.File;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
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
import stock.dto.StockPurchaseHistoryDto;

public class BrandPurchaseHistoryPanel extends JPanel implements Refreshable {

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
                "발주ID",
                "상품ID",
                "상품명",
                "카테고리",
                "가격($)",
                "가격(원)",
                "임계값",
                "발주일시",
                "수량",
                "상태"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        purchaseTable = new JTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(purchaseTable);

        JButton refreshButton = new JButton("새로고침");
        JButton exportButton = new JButton("파일로 저장");
        JButton backButton = new JButton("뒤로가기");

        refreshButton.addActionListener(e -> loadPurchaseHistory());
        exportButton.addActionListener(e -> exportPurchaseHistory());
        backButton.addActionListener(e -> screenManager.show("BRAND_MAIN"));

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(refreshButton);
        bottomPanel.add(exportButton);
        bottomPanel.add(backButton);

        add(titleLabel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        loadPurchaseHistory();
    }

    private void loadPurchaseHistory() {
        try {
            tableModel.setRowCount(0);

            List<StockPurchaseHistoryDto> purchases =
                    brandSystem.getPurchaseService()
                            .getPurchaseHistoryDtoByBrandName(brandSystem.getBrandName());

            for (StockPurchaseHistoryDto purchase : purchases) {
                tableModel.addRow(new Object[] {
                        purchase.getPurchaseId(),
                        purchase.getProductId(),
                        purchase.getProductName(),
                        purchase.getCategoryName(),
                        purchase.getPriceUsd(),
                        purchase.getPriceKrw(),
                        purchase.getThresholdValue(),
                        purchase.getPurchaseDate(),
                        purchase.getAmount(),
                        purchase.getStatus()
                });
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "발주 이력을 불러오는 중 오류가 발생했습니다.");
            e.printStackTrace();
        }
    }

    private void exportPurchaseHistory() {
        JFileChooser fileChooser = new JFileChooser();

        fileChooser.setDialogTitle("발주 이력 저장 위치 선택");
        fileChooser.setSelectedFile(
                new File("purchase_history_"
                        + brandSystem.getBrandName().replaceAll("\\s+", "_")
                        + ".csv")
        );

        int result = fileChooser.showSaveDialog(this);

        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File selectedFile = fileChooser.getSelectedFile();

        if (!selectedFile.getName().toLowerCase().endsWith(".csv")) {
            selectedFile = new File(selectedFile.getAbsolutePath() + ".csv");
        }

        try {
            brandSystem.exportPurchaseHistoryToFile(selectedFile);

            JOptionPane.showMessageDialog(
                    this,
                    "발주 이력을 파일로 저장했습니다.\n저장 위치: " + selectedFile.getAbsolutePath()
            );

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "발주 이력 파일 저장 중 오류가 발생했습니다.");
            e.printStackTrace();
        }
    }

    @Override
    public void refresh() {
        loadPurchaseHistory();
    }
}