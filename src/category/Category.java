package category;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder

public class Category {
	private String categoryName;
	private int depth;
}
