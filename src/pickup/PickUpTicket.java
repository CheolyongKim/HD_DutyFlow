package pickup;

import java.time.LocalDateTime;

import airplane.Airplane;
import lombok.Data;
import member.Member;

@Data
public class PickUpTicket {
	private Member member;
	private Airplane airplane;
	private LocalDateTime ticketIssueTime;
	private int ticketNum;
	
	public PickUpTicket(Member member, Airplane airplane, int ticketNum) {
		this.member=member;
		this.airplane=airplane;
		this.ticketNum=ticketNum;
	}
}
