//package main;
//
//import java.time.LocalDate;
//
//import exchangeRate.ExchangeRateProvider;
//import exchangeRate.ExchangeRateService;
//
//public class JooyeongRateTestMain {
//    public static void main(String[] args) throws InterruptedException {
//
//        ExchangeRateService exchangeRateService =
//                new ExchangeRateService();
//
//        System.out.println("===== 환율 10초 갱신 테스트 시작 =====");
//
//        LocalDate startDate = LocalDate.now();
//
//        for (int i = 0; i < 4; i++) {
//            LocalDate testDate = startDate.plusDays(i);
//
//            exchangeRateService.updateExchangeRateForTest(testDate);
//
//            System.out.println("[" + testDate + "] 환율 갱신 완료");
//            System.out.println("현재 Provider 환율: "
//                    + ExchangeRateProvider.getInstance().getExchangeRate());
//            System.out.println("현재 Provider 날짜: "
//                    + ExchangeRateProvider.getInstance().getExchangeDate());
//
//            if (i < 3) {
//                Thread.sleep(10000);
//            }
//            
//            System.out.println("===============================");
//        }
//
//        System.out.println("===== 환율 10초 갱신 테스트 종료 =====");
//    }
//}
