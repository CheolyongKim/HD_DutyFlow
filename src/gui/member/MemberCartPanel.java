package gui.member;

import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import gui.ScreenManager;
import gui.common.Refreshable;
import gui.fakedata.FakeCartItem;
import gui.fakedata.FakeMemberStore;

public class MemberCartPanel extends JPanel implements Refreshable {

    private final ScreenManager screenManager;

    private JTable cartTable;
    private DefaultTableModel tableModel;
    private JLabel totalQuantityLabel;
    private JLabel totalUsdLabel;
    private JLabel totalKrwLabel;

    public MemberCartPanel(ScreenManager screenManager) {
        this.screenManager = screenManager;

        setLayout(new BorderLayout());
        setBackground(new Color(245, 246, 250));
        setBorder(new EmptyBorder(30, 40, 30, 40));

        JLabel titleLabel = new JLabel("장바구니", SwingConstants.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 28));
        titleLabel.setBorder(new EmptyBorder(0, 0, 20, 0));

        add(titleLabel, BorderLayout.NORTH);

        String[] columns = {
                "상품명", "브랜드", "수량", "단가($)", "단가(원)", "합계($)", "합계(원)"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        cartTable = new JTable(tableModel);
        cartTable.setRowHeight(30);

        add(new JScrollPane(cartTable), BorderLayout.CENTER);

        add(createBottomPanel(), BorderLayout.SOUTH);
    }

    private JPanel createBottomPanel() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(20, 0, 0, 0));

        JPanel summaryPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        summaryPanel.setOpaque(false);

        totalQuantityLabel = createSummaryLabel("총 상품 수량: 0개");
        totalUsdLabel = createSummaryLabel("총 달러 금액: $0.00");
        totalKrwLabel = createSummaryLabel("총 원화 금액: 0원");

        summaryPanel.add(totalQuantityLabel);
        summaryPanel.add(totalUsdLabel);
        summaryPanel.add(totalKrwLabel);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        buttonPanel.setOpaque(false);

        JButton updateButton = createButton("수량 변경", new Color(52, 152, 219));
        JButton deleteButton = createButton("선택 삭제", new Color(231, 76, 60));
        JButton clearButton = createButton("전체 삭제", new Color(192, 57, 43));
        JButton orderButton = createButton("주문하기", new Color(46, 204, 113));
        JButton backButton = createButton("돌아가기", new Color(149, 165, 166));

        updateButton.addActionListener(e -> updateQuantity());
        deleteButton.addActionListener(e -> deleteSelectedItem());
        clearButton.addActionListener(e -> clearCart());
        orderButton.addActionListener(e -> order());
        backButton.addActionListener(e -> screenManager.show("MEMBER_MAIN"));

        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(orderButton);
        buttonPanel.add(backButton);

        wrapper.add(summaryPanel, BorderLayout.NORTH);
        wrapper.add(buttonPanel, BorderLayout.SOUTH);

        return wrapper;
    }

    private JLabel createSummaryLabel(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);

        label.setOpaque(true);
        label.setBackground(Color.WHITE);
        label.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        label.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));

        return label;
    }

    private JButton createButton(String text, Color color) {
        JButton button = new JButton(text);

        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(new Font("맑은 고딕", Font.BOLD, 13));

        return button;
    }

    private void loadCart() {
        tableModel.setRowCount(0);

        List<FakeCartItem> cartItems = FakeMemberStore.getCartItems();

        for (FakeCartItem item : cartItems) {
            tableModel.addRow(new Object[]{
                    item.getProduct().getProductName(),
                    item.getProduct().getBrandName(),
                    item.getQuantity(),
                    item.getProduct().getFinalPriceUsd(),
                    item.getProduct().getFinalPriceKrw(),
                    item.getTotalUsd(),
                    item.getTotalKrw()
            });
        }

        totalQuantityLabel.setText("총 상품 수량: " + FakeMemberStore.getTotalQuantity() + "개");
        totalUsdLabel.setText("총 달러 금액: $" + FakeMemberStore.getTotalUsd());
        totalKrwLabel.setText("총 원화 금액: " + FakeMemberStore.getTotalKrw() + "원");
    }

    private void updateQuantity() {
        int selectedRow = cartTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "수량을 변경할 상품을 선택해주세요.");
            return;
        }

        int modelRow = cartTable.convertRowIndexToModel(selectedRow);
        FakeCartItem item = FakeMemberStore.getCartItems().get(modelRow);

        String input = JOptionPane.showInputDialog(this, "변경할 수량을 입력하세요.", item.getQuantity());

        if (input == null) {
            return;
        }

        try {
            int newQuantity = Integer.parseInt(input);

            if (newQuantity <= 0) {
                JOptionPane.showMessageDialog(this, "수량은 1개 이상이어야 합니다.");
                return;
            }

            item.setQuantity(newQuantity);
            loadCart();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "수량은 숫자로 입력해야 합니다.");
        }
    }

    private void deleteSelectedItem() {
        int selectedRow = cartTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "삭제할 상품을 선택해주세요.");
            return;
        }

        int modelRow = cartTable.convertRowIndexToModel(selectedRow);
        FakeCartItem item = FakeMemberStore.getCartItems().get(modelRow);

        FakeMemberStore.removeCartItem(item.getProduct().getProductId());
        loadCart();
    }

    private void clearCart() {
        FakeMemberStore.clearCart();
        loadCart();
    }

    private void order() {
        if (FakeMemberStore.getCartItems().isEmpty()) {
            JOptionPane.showMessageDialog(this, "장바구니가 비어 있습니다.");
            return;
        }

        FakeMemberStore.createOrderFromCart();

        JOptionPane.showMessageDialog(this, "주문 및 결제가 완료되었습니다.");
        screenManager.show("MEMBER_ORDER_HISTORY");
    }

    @Override
    public void refresh() {
        loadCart();
    }
}