package pickup;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;

import main.main;

public class DepartureSoonSortStrategy implements SortStrategy {

	@Override
    public Comparator<PickUpTicket> getComparator() {

        return Comparator
                .comparingLong((PickUpTicket ticket) -> {

                    LocalDateTime departureAt =
                            ticket.getAirplane()
                                  .getDepartureAt();

                    return Duration.between(
                            main.curTime,
                            departureAt
                    ).toMinutes();
                })
                .thenComparingInt((PickUpTicket ticket) ->
                				ticket.getTicketNum())
                .thenComparingInt(ticket ->
                        ticket.getMember()
                              .getMembership()
                              .getPriority()
                );
    }
	
}
