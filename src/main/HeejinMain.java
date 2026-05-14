import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import exception.ErrorCode;
import exception.SystemException;
import exception.ValidationException;
import order.Order;
import regulation.RegulationDAO;
import regulation.RegulationDTO;
import tax.AlcoholTaxStrategy;
import tax.GeneralTaxStrategy;
import tax.PerfumeTaxStrategy;
import tax.TaxCalculator;
import tax.TaxStrategy;

public class HeejinMain {

    public static void main(String[] args) {

        RegulationDAO regulationDAO = new RegulationDAO();

        // CategoryId (일반상품 - 1, 주류 - 2, 향수 - 3)
        RegulationDTO generalRegulationDTO = regulationDAO.getRegulationByCategoryId(1);
        RegulationDTO alcoholRegulationDTO = regulationDAO.getRegulationByCategoryId(2);
        RegulationDTO perfumeRegulationDTO = regulationDAO.getRegulationByCategoryId(3);

        // =========================
        // 케이스 1. 일반상품만 - 한도 이하 (면세)
        // =========================
        System.out.println("===== 케이스 1: 일반상품 700달러 (면세) =====");

        Order order1 = new Order();
        order1.setTotalPrice(new BigDecimal("700"));
        order1.setTotalAlcohol(0);
        order1.setTotalPerfume(0);

        BigDecimal tax1 = calculate(order1, generalRegulationDTO, alcoholRegulationDTO, perfumeRegulationDTO);
        System.out.println("총 세금: " + tax1);

        // =========================
        // 케이스 2. 일반상품만 - 한도 초과
        // =========================
        System.out.println("\n===== 케이스 2: 일반상품 1000달러 (초과 :200 * 0.15 = 30달러) =====");

        Order order2 = new Order();
        order2.setTotalPrice(new BigDecimal("1000"));
        order2.setTotalAlcohol(0);
        order2.setTotalPerfume(0);

        BigDecimal tax2 = calculate(order2, generalRegulationDTO, alcoholRegulationDTO, perfumeRegulationDTO);
        System.out.println("총 세금: " + tax2);

        // =========================
        // 케이스 3. 주류 초과
        // =========================
        System.out.println("\n===== 케이스 3: 일반상품 500달러 + 주류 5리터 (주류 초과 : 3 * 0.70 = 2.10달러) =====");

        Order order3 = new Order();
        order3.setTotalPrice(new BigDecimal("500"));
        order3.setTotalAlcohol(5);
        order3.setTotalPerfume(0);

        BigDecimal tax3 = calculate(order3, generalRegulationDTO, alcoholRegulationDTO, perfumeRegulationDTO);
        System.out.println("총 세금: " + tax3);

        // =========================
        // 케이스 4. 향수 초과
        // =========================
        System.out.println("\n===== 케이스 4: 일반상품 500달러 + 향수 200ml (향수 초과 : 100 * 0.15 = 15달러) =====");

        Order order4 = new Order();
        order4.setTotalPrice(new BigDecimal("500"));
        order4.setTotalAlcohol(0);
        order4.setTotalPerfume(200);

        BigDecimal tax4 = calculate(order4, generalRegulationDTO, alcoholRegulationDTO, perfumeRegulationDTO);
        System.out.println("총 세금: " + tax4);

        // =========================
        // 케이스 5. 전부 초과 (복합)
        // =========================
        System.out.println("\n===== 케이스 5: 일반상품 1200달러 + 주류 4리터 + 향수 300ml (전부 초과) =====");
        System.out.println("기대값: (1200-800) * 0.15 + (4-2) * 0.70 + (300-100) * 0.15 = 60 + 1.40 + 30 = 91.40");

        Order order5 = new Order();
        order5.setTotalPrice(new BigDecimal("1200"));
        order5.setTotalAlcohol(4);
        order5.setTotalPerfume(300);

        BigDecimal tax5 = calculate(order5, generalRegulationDTO, alcoholRegulationDTO, perfumeRegulationDTO);
        System.out.println("총 세금: " + tax5);
        
      //-------------------------SystemLog--------------------------
        
        // =========================
        // 케이스 1. errorCode만 던지는 경우
        // =========================
        try {
            throw new SystemException(ErrorCode.DB_CONNECTION);
        } catch (SystemException e1) {
            System.out.println("catch됨: " + e1.getMessage());
        }
        
        
        // =========================
        // 케이스 2. 원인 예외까지 넘기는 경우
        // =========================
        try {
            SQLException message = new SQLException("ORA-00001 실제 DB 오류");
            throw new SystemException(ErrorCode.DB_CONNECTION, message);
        } catch (SystemException e2) {
            System.out.println("catch됨: " + e2.getMessage());
        }

        
        
        // =========================
        // 케이스 3. 다른 예외 타입
        // =========================
        try {
            throw new ValidationException(ErrorCode.ILLEGAL_STATE);
        } catch (ValidationException e3) {
            System.out.println("catch됨: " + e3.getMessage());
        }

        System.out.println("테스트 완료 - DB에서 SystemLog 테이블 확인");
    }
    
    

    // 어떤 전략 넣을지 전략 리스트 구성 
    private static BigDecimal calculate(Order order,
                                        RegulationDTO generalRegulationDTO,
                                        RegulationDTO alcoholRegulationDTO,
                                        RegulationDTO perfumeRegulationDTO) {

        List<TaxStrategy> strategies = new ArrayList<>();

        // 일반상품은 항상 포함
        strategies.add(new GeneralTaxStrategy(generalRegulationDTO));

        // 주류 한도 초과 시에만 추가
        if (order.getTotalAlcohol() > alcoholRegulationDTO.getLimitCapacity()) {
            strategies.add(new AlcoholTaxStrategy(alcoholRegulationDTO));
        }

        // 향수 한도 초과 시에만 추가
        if (order.getTotalPerfume() > perfumeRegulationDTO.getLimitCapacity()) {
            strategies.add(new PerfumeTaxStrategy(perfumeRegulationDTO));
        }

        TaxCalculator calculator = new TaxCalculator(strategies);
        return calculator.calculateTax(order);
    }
    
    
}