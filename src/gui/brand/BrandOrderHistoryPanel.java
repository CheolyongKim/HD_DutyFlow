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
import order.dto.OrderDTO;

public class BrandOrderHistoryPanel extends JPanel implements Refreshable {

    private final ScreenManager screenManager;

    private DefaultTableModel tableModel;
    private JTable orderTable;

    public BrandOrderHistoryPanel(ScreenManager screenManager) {
        this.screenManager = screenManager;

        setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("브랜드 판매 내역 조회", SwingConstants.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 24));

        String[] columns = {
                "주문ID",
                "회원ID",
                "예약ID",
                "상품ID",
                "상품명",
                "카테고리",
                "용량",
                "수량",
                "달러 단가",
                "할인율",
                "할인 적용 단가",
                "라인 금액",
                "주문일시",
                "주문상태"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        orderTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(orderTable);

        JButton refreshButton = new JButton("새로고침");
        JButton backButton = new JButton("뒤로가기");

        refreshButton.addActionListener(e -> loadOrders());
        backButton.addActionListener(e -> screenManager.show("BRAND_MAIN"));

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(refreshButton);
        bottomPanel.add(backButton);

        add(titleLabel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        // 로그인 전에는 BrandSystem이 없을 수 있으므로 생성자에서 바로 loadOrders() 호출하지 않음
    }

    private void loadOrders() {
        try {
            BrandSystem brandSystem = getLoginBrandSystem();

            if (brandSystem == null) {
                return;
            }

            tableModel.setRowCount(0);

            List<OrderDTO> orders = brandSystem.getOrdersByBrandName();

            for (OrderDTO order : orders) {
                tableModel.addRow(new Object[] {
                        order.getOrderId(),
                        order.getMemberId(),
                        order.getReservationId(),
                        order.getProductId(),
                        order.getProductName(),
                        order.getCategoryName(),
                        order.getCapacity(),
                        order.getQuantity(),
                        order.getDollarPrice(),
                        order.getDiscountPrice(),
                        order.getDiscountedUnitPrice(),
                        order.getTotalLinePrice(),
                        order.getOrderedAt(),
                        order.getOrderState()
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
            JOptionPane.showMessageDialog(this, "브랜드 판매 내역을 불러오는 중 오류가 발생했습니다.");
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
        loadOrders();
    }
}