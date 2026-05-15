package exchangeRate;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
public class ExchangeRate {
	private BigDecimal exchangeRate;
	private LocalDate exchangeDate;
	private char isLatest;
	
    @Override
    public String toString() {
        return " 날짜 : " + exchangeDate + " |  환율 : " + exchangeRate;
    }
}
