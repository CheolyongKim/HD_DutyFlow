package exchangeRate;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExchangeRate {
	private BigDecimal exchangeRate;
	private LocalDate exchangeDate;
	private char isLatest;
}
