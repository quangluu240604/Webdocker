package fit.hutech.spring.daos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Item {
	private Long bookId;
	private String bookName;
	private Double price;
	private int quantity;
}
