package gui.brand;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
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
    
    // 요약 정보를 표시할 라벨들
    private JLabel totalCountLabel;
    private JLabel lowStockLabel;
    private JLabel outOfStockLabel;

    public BrandStockPanel(ScreenManager screenManager) {
        this.screenManager = screenManager;

        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(20, 30, 20, 30));

        // ── 상단 타이틀 및 요약 대시보드 ──
        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("내 브랜드 재고 현황", SwingConstants.LEFT);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 28));
        titleLabel.setBorder(new EmptyBorder(0, 0, 20, 0));
        northPanel.add(titleLabel, BorderLayout.NORTH);

        // 요약 카드 패널
        JPanel summaryPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        summaryPanel.setBorder(new EmptyBorder(0, 0, 20, 0));
        
        totalCountLabel = createSummaryCard(summaryPanel, "전체 상품", Color.GRAY);
        lowStockLabel = createSummaryCard(summaryPanel, "재고 부족", new Color(0xE67E22));
        outOfStockLabel = createSummaryCard(summaryPanel, "품절", new Color(0xE74C3C));
        
        northPanel.add(summaryPanel, BorderLayout.CENTER);
        add(northPanel, BorderLayout.NORTH);

        // ── 중앙 테이블 영역 ──
        String[] columnNames = { "상품명", "카테고리", "용량", "가격($)", "가격(원)", "제조일자", "재고수량", "임계값", "상태" };
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        stockTable = new JTable(tableModel);
        stockTable.setRowHeight(30);
        stockTable.getTableHeader().setFont(new Font("맑은 고딕", Font.BOLD, 14));
        
        // 상태에 따른 렌더링 설정 (재고 부족/품절 강조)
        applyCustomRenderer();

        JScrollPane scrollPane = new JScrollPane(stockTable);
        add(scrollPane, BorderLayout.CENTER);

        // ── 하단 버튼 영역 ──
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        
        JButton refreshButton = new JButton("새로고침");
        JButton backButton = new JButton("뒤로가기");

        refreshButton.setFont(new Font("맑은 고딕", Font.PLAIN, 16));
        backButton.setFont(new Font("맑은 고딕", Font.PLAIN, 16));

        refreshButton.addActionListener(e -> loadStockData());
        backButton.addActionListener(e -> screenManager.show("BRAND_MAIN"));

        bottomPanel.add(refreshButton);
        bottomPanel.add(backButton);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JLabel createSummaryCard(JPanel parent, String title, Color titleColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(new Color(0xEEEEEE), 1));
        
        JLabel titleLbl = new JLabel(title, SwingConstants.CENTER);
        titleLbl.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        titleLbl.setForeground(titleColor);
        
        JLabel valueLbl = new JLabel("0", SwingConstants.CENTER);
        valueLbl.setFont(new Font("맑은 고딕", Font.BOLD, 22));
        
        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valueLbl, BorderLayout.CENTER);
        card.setBorder(BorderFactory.createCompoundBorder(card.getBorder(), new EmptyBorder(10,10,10,10)));
        
        parent.add(card);
        return valueLbl;
    }

    private void applyCustomRenderer() {
        stockTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                String status = (String) table.getValueAt(row, 8); // '상태' 열
                
                if (isSelected) {
                    c.setBackground(table.getSelectionBackground());
                } else {
                    if ("품절".equals(status)) {
                        c.setBackground(new Color(0xFFE5E5)); // 연빨강
                    } else if ("재고 부족".equals(status)) {
                        c.setBackground(new Color(0xFFF4E5)); // 연주황
                    } else {
                        c.setBackground(Color.WHITE);
                    }
                }
                return c;
            }
        });
    }

    private void loadStockData() {
        try {
            BrandSystem brandSystem = getLoginBrandSystem();
            if (brandSystem == null) return;

            tableModel.setRowCount(0);
            List<StockProductDto> stockList = brandSystem.getMyBrandStocks();

            int lowStockCount = 0;
            int outOfStockCount = 0;

            for (StockProductDto stock : stockList) {
                int amount = stock.getAmount();
                int thresholdValue = stock.getThresholdValue();
                String status;

                if (amount == 0) {
                    status = "품절";
                    outOfStockCount++;
                } else if (amount <= thresholdValue) {
                    status = "재고 부족";
                    lowStockCount++;
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
            
            // 요약 정보 업데이트
            totalCountLabel.setText(String.valueOf(stockList.size()));
            lowStockLabel.setText(String.valueOf(lowStockCount));
            outOfStockLabel.setText(String.valueOf(outOfStockCount));

        } catch (DutyFreeException e) {
            JOptionPane.showMessageDialog(this, e.getErrorCode().getMessage(), "알림", JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "재고 데이터를 불러오는 중 오류가 발생했습니다.");
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