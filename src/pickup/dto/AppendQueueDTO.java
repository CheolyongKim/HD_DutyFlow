package pickup.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import member.Grade;

@Getter
@Setter
@Builder
public class AppendQueueDTO {
	// enqueue 위해 Airplane, Member 넣어야 함
	// Airplane: flightResNum 으로 찾아온다 
	// 필요한거- flightCode, departureAt, isDelayed
	// Member: passportNum 으로 찾아온다 
	// 필요한거- memberId, grade, name
	// -> 한꺼번에 DTO로 찾아온다
	private String flightCode;
	private LocalDateTime departureAt;
	private int isDelayed;
	
	private int memberId;
	private Grade grade;
	private String name;
}
