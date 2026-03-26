package fit.hutech.spring.viewmodels;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutVm {
	@NotBlank(message = "Receiver name is required")
	@Size(max = 100, message = "Receiver name must be at most 100 characters")
	private String receiverName;

	@NotBlank(message = "Receiver phone is required")
	@Pattern(regexp = "^[0-9]{10,11}$", message = "Phone must be 10-11 digits")
	private String receiverPhone;

	@NotBlank(message = "Shipping address is required")
	@Size(max = 255, message = "Address must be at most 255 characters")
	private String shippingAddress;

	@Size(max = 500, message = "Note must be at most 500 characters")
	private String note;
}

