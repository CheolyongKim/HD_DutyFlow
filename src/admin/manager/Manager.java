package admin.manager;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Manager {
	private int managerId;
	private String managerName;
	private String managerType; // "AIRPORT" or "SHOP"
	private String password;
}
