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
import product.Product;
import product.dto.ProductDTO;

public class BrandProductListPanel extends JPanel implements Refreshable {

    private final ScreenManager screenManager;

    private DefaultTableModel tableModel;
    private JTable productTable;

    public BrandProductListPanel(ScreenManager screenManager) {
        this.screenManager = screenManager;

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

        // 생성자에서 바로 loadProducts() 호출하지 않는 것을 추천
        // 로그인 전에는 BrandSystem이 null일 수 있음
    }

    private void loadProducts() {
        try {
            BrandSystem brandSystem = getLoginBrandSystem();

            if (brandSystem == null) {
                return;
            }

            tableModel.setRowCount(0);

            List<Product> products = brandSystem.getProductsByBrandName();

            for (Product product : products) {
                tableModel.addRow(new Object[] {
                        product.getProductName(),
                        product.getBrandName(),
                        product.getCategory() != null ? product.getCategory().getCategoryName() : "",
                        product.getCapacity(),
                        product.getPriceUsd(),
                        product.getPriceKrw(),
                        product.getDiscountRate(),
                        product.getThresholdValue()
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