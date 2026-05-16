package gui.brand;

import java.awt.*;
import java.io.File;
import java.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import brandSystem.BrandSystem;
import exception.DutyFreeException;
import gui.ScreenManager;
import gui.common.Refreshable;
import stock.dto.StockPurchaseHistoryDto;

public class BrandPurchaseHistoryPanel extends JPanel implements Refreshable {

    private final ScreenManager screenManager;
    private DefaultTableModel tableModel;
    private JTable purchaseTable;

    public BrandPurchaseHistoryPanel(ScreenManager screenManager) {
        this.screenManager = screenManager;

        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(30, 30, 30, 30)); // 전체 여백 추가

        // ── 상단 타이틀 ──
        JLabel titleLabel = new JLabel("브랜드 발주 이력 조회", SwingConstants.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 28));
        titleLabel.setBorder(new EmptyBorder(0, 0, 20, 0));
        add(titleLabel, BorderLayout.NORTH);

        // ── 중앙 테이블 구성 ──
        String[] columns = {
                "발주ID", "상품ID", "상품명", "카테고리", 
                "가격($)", "가격(원)", "임계값", "발주일시", "수량", "상태"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        purchaseTable = new JTable(tableModel);
        purchaseTable.setRowHeight(25);
        purchaseTable.getTableHeader().setFont(new Font("맑은 고딕", Font.BOLD, 14));
        purchaseTable.setFont(new Font("맑은 고딕", Font.PLAIN, 14));

        // 숫자 및 상태 컬럼 중앙 정렬
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i : new int[]{0, 1, 3, 4, 5, 6, 8, 9}) {
            purchaseTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane scrollPane = new JScrollPane(purchaseTable);
        add(scrollPane, BorderLayout.CENTER);

        // ── 하단 버튼 패널 ──
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        
        JButton refreshButton = new JButton("새로고침");
        JButton exportButton = new JButton("파일로 저장 (CSV)");
        JButton backButton = new JButton("뒤로가기");

        // 버튼 폰트 설정
        Font btnFont = new Font("맑은 고딕", Font.BOLD, 16);
        refreshButton.setFont(btnFont);
        exportButton.setFont(btnFont);
        backButton.setFont(btnFont);

        refreshButton.addActionListener(e -> loadPurchaseHistory());
        exportButton.addActionListener(e -> exportPurchaseHistory());
        backButton.addActionListener(e -> screenManager.show("BRAND_MAIN"));

        bottomPanel.add(refreshButton);
        bottomPanel.add(exportButton);
        bottomPanel.add(backButton);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void loadPurchaseHistory() {
        try {
            BrandSystem brandSystem = getLoginBrandSystem();
            if (brandSystem == null) return;

            tableModel.setRowCount(0);

            List<StockPurchaseHistoryDto> purchases = brandSystem.getMyBrandPurchaseHistory();

            if (purchases.isEmpty()) {
                // 데이터가 없을 경우 사용자에게 알림을 줄 수도 있습니다.
            }

            for (StockPurchaseHistoryDto purchase : purchases) {
                tableModel.addRow(new Object[] {
                        purchase.getPurchaseId(),
                        purchase.getProductId(),
                        purchase.getProductName(),
                        purchase.getCategoryName(),
                        String.format("%,.2f", purchase.getPriceUsd()),
                        String.format("%,.0f", purchase.getPriceKrw()),
                        purchase.getThresholdValue(),
                        purchase.getPurchaseDate(),
                        purchase.getAmount(),
                        purchase.getStatus()
                });
            }

        } catch (DutyFreeException e) {
            JOptionPane.showMessageDialog(this, e.getErrorCode().getMessage(), "알림", JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "발주 이력을 불러오는 중 오류가 발생했습니다.");
            e.printStackTrace();
        }
    }

    private void exportPurchaseHistory() {
        try {
            BrandSystem brandSystem = getLoginBrandSystem();
            if (brandSystem == null) return;

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("발주 이력 저장 위치 선택");
            
            // 파일명에서 공백 제거 및 브랜드명 포함
            String defaultFileName = "purchase_history_" + brandSystem.getBrandName().replaceAll("\\s+", "_") + ".csv";
            fileChooser.setSelectedFile(new File(defaultFileName));

            int result = fileChooser.showSaveDialog(this);
            if (result != JFileChooser.APPROVE_OPTION) return;

            File selectedFile = fileChooser.getSelectedFile();
            if (!selectedFile.getName().toLowerCase().endsWith(".csv")) {
                selectedFile = new File(selectedFile.getAbsolutePath() + ".csv");
            }

            brandSystem.exportPurchaseHistoryToFile(selectedFile);

            JOptionPane.showMessageDialog(this, 
                "발주 이력이 성공적으로 저장되었습니다.\n경로: " + selectedFile.getAbsolutePath(),
                "저장 완료", JOptionPane.INFORMATION_MESSAGE);

        } catch (DutyFreeException e) {
            JOptionPane.showMessageDialog(this, e.getErrorCode().getMessage(), "알림", JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "파일 저장 중 오류가 발생했습니다.");
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
        loadPurchaseHistory();
    }
}