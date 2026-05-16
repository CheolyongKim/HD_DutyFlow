package main;

import javax.swing.SwingUtilities;

import brandSystem.BrandSystem;
import gui.MainFrame;
import stock.scheduler.StockCleanupScheduler;
import stock.scheduler.StockPurchaseScheduler;

public class KooMain {

    public static void main(String[] args) {

        BrandSystem brandSystem = new BrandSystem("Brand_01");

        StockPurchaseScheduler purchaseScheduler =
                new StockPurchaseScheduler(brandSystem.getPurchaseService());

        StockCleanupScheduler cleanupScheduler =
                new StockCleanupScheduler(brandSystem.getStockService());

        purchaseScheduler.start();
        cleanupScheduler.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            purchaseScheduler.stop();
            cleanupScheduler.stop();
            System.out.println("===== 스케줄러 종료 =====");
        }));

        SwingUtilities.invokeLater(() -> new MainFrame(brandSystem));
    }
}