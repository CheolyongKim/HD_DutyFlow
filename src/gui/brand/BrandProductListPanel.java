package gui.brand;

import java.awt.BorderLayout;
import java.awt.Font;
import java.text.DecimalFormat;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import brandSystem.BrandSystem;
import exception.DutyFreeException;
import gui.ScreenManager;
import gui.common.Refreshable;
import product.dto.ProductDTO;

public class BrandProductListPanel extends JPanel implements Refreshable {

    private final ScreenManager screenManager;
    private DefaultTableModel tableModel;
    private JTable productTable;

    // 금액 포맷을 위한 포맷터 (원화: 정수형 콤마, 달러: 소수점 2자리 콤마)
    private final DecimalFormat krwFormat = new DecimalFormat("#,###원");
    private final DecimalFormat usdFormat = new DecimalFormat("$#,##0.00");

    public BrandProductListPanel(ScreenManager screenManager) {
        this.screenManager = screenManager;

        setLayout(new BorderLayout());

        // 상단 타이틀
        JLabel titleLabel = new JLabel("브랜드 상품 목록 조회", SwingConstants.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 24));
        add(titleLabel, BorderLayout.NORTH);

        // 테이블 컬럼 정의
        String[] columns = {
                "상품명", "브랜드", "카테고리", "용량", "가격($)", "가격(원)", 
                "할인율", "행사", "최종가($)", "최종가(원)", "임계값"
        };

        // 테이블 모델 설정 (수정 불가)
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        productTable = new JTable(tableModel);
        
        // --- 테이블 가독성 향상 설정 ---
        setupTableUI();

        JScrollPane scrollPane = new JScrollPane(productTable);
        add(scrollPane, BorderLayout.CENTER);

        // 하단 버튼 패널
        JButton refreshButton = new JButton("새로고침");
        JButton backButton = new JButton("뒤로가기");

        refreshButton.addActionListener(e -> loadProducts());
        backButton.addActionListener(e -> screenManager.show("BRAND_MAIN"));

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(refreshButton);
        bottomPanel.add(backButton);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    /**
     * 테이블의 컬럼 너비 및 정렬을 설정합니다.
     */
    private void setupTableUI() {
        // 중앙 정렬 렌더러
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        // 우측 정렬 렌더러 (숫자/금액용)
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);

        // 컬럼별 설정
        for (int i = 0; i < productTable.getColumnCount(); i++) {
            if (i == 0) { // 상품명
                productTable.getColumnModel().getColumn(i).setPreferredWidth(150);
            } else if (i >= 4 && i <= 10 && i != 7) { // 가격, 할인율, 최종가, 임계값
                productTable.getColumnModel().getColumn(i).setCellRenderer(rightRenderer);
                productTable.getColumnModel().getColumn(i).setPreferredWidth(80);
            } else { // 브랜드, 카테고리, 용량, 행사 여부
                productTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
        }
    }

    private void loadProducts() {
        try {
            BrandSystem brandSystem = getLoginBrandSystem();
            if (brandSystem == null) return;

            tableModel.setRowCount(0);

            List<ProductDTO> products = brandSystem.getProductsByBrandName();

            if (products.isEmpty()) {
                return; // 상품이 없으면 비어있는 상태 유지
            }

            for (ProductDTO product : products) {
                tableModel.addRow(new Object[] {
                        product.getProductName(),
                        product.getBrandName(),
                        product.getCategory() != null ? product.getCategory().getCategoryName() : "-",
                        product.getCapacity(),
                        usdFormat.format(product.getPriceUsd()),
                        krwFormat.format(product.getPriceKrw()),
                        product.getDiscountRate() + "%",
                        product.isHasEvent() ? "Y" : "N",
                        usdFormat.format(product.getFinalPriceUsd()),
                        krwFormat.format(product.getFinalPriceKrw()),
                        product.getThresholdValue()
                });
            }

        } catch (DutyFreeException e) {
            JOptionPane.showMessageDialog(this, e.getErrorCode().getMessage(), "알림", JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "브랜드 상품 목록을 불러오는 중 오류가 발생했습니다.");
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
        loadProducts();
    }
}