package main;

import javax.swing.SwingUtilities;

import gui.MainFrame;
import stock.scheduler.StockCleanupScheduler;
import stock.scheduler.StockPurchaseScheduler;
import stock.service.StockPurchaseService;
import stock.service.StockService;

public class KooMain {

    public static void main(String[] args) {

        StockService stockService = new StockService();
        StockPurchaseService purchaseService = new StockPurchaseService();

        StockPurchaseScheduler purchaseScheduler =
                new StockPurchaseScheduler(purchaseService);

        StockCleanupScheduler cleanupScheduler =
                new StockCleanupScheduler(stockService);

        purchaseScheduler.start();
        cleanupScheduler.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            purchaseScheduler.stop();
            cleanupScheduler.stop();
            System.out.println("===== 스케줄러 종료 =====");
        }));

        SwingUtilities.invokeLater(() -> new MainFrame());
    }
}