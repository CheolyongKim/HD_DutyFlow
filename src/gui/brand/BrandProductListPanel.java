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
import product.dto.ProductDTO;

public class BrandProductListPanel extends JPanel implements Refreshable {

    private final ScreenManager screenManager;
    private final BrandSystem brandSystem;

    private DefaultTableModel tableModel;
    private JTable productTable;

    public BrandProductListPanel(ScreenManager screenManager, BrandSystem brandSystem) {
        this.screenManager = screenManager;
        this.brandSystem = brandSystem;

        setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("브랜드 상품 목록 조회", SwingConstants.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 24));

        String[] columns = {
                "상품명",
                "브랜드",
                "카테고리",
                "용량",
                "가격($)",
                "가격(원)",
                "할인율",
                "행사 여부",
                "최종가($)",
                "최종가(원)",
                "임계값"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        productTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(productTable);

        JButton refreshButton = new JButton("새로고침");
        JButton backButton = new JButton("뒤로가기");

        refreshButton.addActionListener(e -> loadProducts());
        backButton.addActionListener(e -> screenManager.show("BRAND_MAIN"));

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(refreshButton);
        bottomPanel.add(backButton);

        add(titleLabel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        loadProducts();
    }

    private void loadProducts() {
        try {
            tableModel.setRowCount(0);

            List<ProductDTO> products = brandSystem.getProductsByBrandName();

            for (ProductDTO product : products) {
                tableModel.addRow(new Object[] {
                        product.getProductName(),
                        product.getBrandName(),
                        product.getCategory() != null ? product.getCategory().getCategoryName() : "",
                        product.getCapacity(),
                        product.getPriceUsd(),
                        product.getPriceKrw(),
                        product.getDiscountRate(),
                        product.isHasEvent() ? "Y" : "N",
                        product.getFinalPriceUsd(),
                        product.getFinalPriceKrw(),
                        product.getThresholdValue()
                });
            }

        } catch (DutyFreeException e) {
            JOptionPane.showMessageDialog(this, e.getErrorCode().getMessage());

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "브랜드 상품 목록을 불러오는 중 오류가 발생했습니다.");
            e.printStackTrace();
        }
    }

    @Override
    public void refresh() {
        loadProducts();
    }
}