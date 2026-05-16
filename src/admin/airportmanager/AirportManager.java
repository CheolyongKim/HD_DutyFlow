package admin.airportmanager;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class AirportManager {
	private int managerId;
	private LocalDateTime shiftTime;
	
	private String managerName;
    private String managerType;
    private String password;
}
