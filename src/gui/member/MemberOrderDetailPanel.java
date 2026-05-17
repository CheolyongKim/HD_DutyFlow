package gui.member;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import gui.ScreenManager;
import gui.common.Refreshable;
import gui.fakedata.FakeCartItem;
import gui.fakedata.FakeOrder;

public class MemberOrderDetailPanel extends JPanel implements Refreshable {

    private final ScreenManager screenManager;

    private JLabel orderInfoLabel;
    private DefaultTableModel tableModel;
    private JTable itemTable;

    public MemberOrderDetailPanel(ScreenManager screenManager) {
        this.screenManager = screenManager;

        setLayout(new BorderLayout());
        setBackground(new Color(245, 246, 250));
        setBorder(new EmptyBorder(30, 40, 30, 40));

        JLabel titleLabel = new JLabel("주문 상세", SwingConstants.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 28));

        add(titleLabel, BorderLayout.NORTH);

        orderInfoLabel = new JLabel();
        orderInfoLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        orderInfoLabel.setBorder(new EmptyBorder(20, 0, 20, 0));

        String[] columns = {
                "상품명", "수량", "단가($)", "단가(원)", "합계($)", "합계(원)"
        };

        tableModel = new DefaultTableModel(columns, 0);

        itemTable = new JTable(tableModel);
        itemTable.setRowHeight(30);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(orderInfoLabel, BorderLayout.NORTH);
        centerPanel.add(new JScrollPane(itemTable), BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        bottomPanel.setOpaque(false);

        JButton pickupButton = new JButton("픽업 예약");
        JButton cancelButton = new JButton("주문 취소");
        JButton backButton = new JButton("뒤로가기");

        pickupButton.addActionListener(e -> screenManager.show("MEMBER_PICKUP_RESERVATION"));

        cancelButton.addActionListener(e -> {
            FakeOrder order = screenManager.getSelectedOrder();

            if (order != null) {
                order.setStatus("CANCELED");
                JOptionPane.showMessageDialog(this, "주문이 취소되었습니다.");
                screenManager.show("MEMBER_ORDER_HISTORY");
            }
        });

        backButton.addActionListener(e -> screenManager.show("MEMBER_ORDER_HISTORY"));

        bottomPanel.add(pickupButton);
        bottomPanel.add(cancelButton);
        bottomPanel.add(backButton);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void loadOrderDetail() {
        FakeOrder order = screenManager.getSelectedOrder();

        if (order == null) {
            JOptionPane.showMessageDialog(this, "선택된 주문이 없습니다.");
            screenManager.show("MEMBER_ORDER_HISTORY");
            return;
        }

        orderInfoLabel.setText(
                "주문번호: " + order.getOrderId()
                        + " / 주문일시: " + order.getOrderedAt()
                        + " / 현재 상태: " + order.getStatus()
                        + " / 결제 금액: " + order.getTotalKrw() + "원"
        );

        tableModel.setRowCount(0);

        for (FakeCartItem item : order.getItems()) {
            tableModel.addRow(new Object[]{
                    item.getProduct().getProductName(),
                    item.getQuantity(),
                    item.getProduct().getFinalPriceUsd(),
                    item.getProduct().getFinalPriceKrw(),
                    item.getTotalUsd(),
                    item.getTotalKrw()
            });
        }
    }

    @Override
    public void refresh() {
        loadOrderDetail();
    }
}