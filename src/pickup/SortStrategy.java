package pickup;

import java.util.Comparator;
import java.util.List;

public interface SortStrategy {
	Comparator<List<Airplane, Member>> getComparator();
}
