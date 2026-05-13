package pickup;

import java.util.Comparator;

public class DepartureSoonSortStrategy implements SortStrategy {

	@Override
    public Comparator<PickUpTicket> getComparator() {

        return Comparator
                .comparingLong(ticket -> {

                    LocalDateTime departureAt =
                            ticket.getMember()
                                  .getAirplane()
                                  .getDepartureAt();

                    return Duration.between(
                            Main.curTime,
                            departureAt
                    ).toMinutes();
                })
                .thenComparingInt(PickUpTicket::getTicketNum)
                .thenComparingInt(ticket ->
                        ticket.getMember()
                              .getMembership()
                              .getPriority()
                );
    }
	
}
