package pickup;

import java.util.Comparator;

public class PrioritySortStrategy implements SortStrategy {

    @Override
    public Comparator<PickUpTicket> getComparator() {

        return Comparator
                .comparingInt((PickUpTicket ticket) ->
                        ticket.getMember()
                              .getMembership()
                              .getPriority()
                )
                .thenComparingInt(PickUpTicket::getTicketNum);
    }
}