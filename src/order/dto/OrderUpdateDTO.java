package order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderUpdateDTO {
    private int orderId;
    private String newState; // DB에 들어갈 값 (예: "PICKED_UP")
}