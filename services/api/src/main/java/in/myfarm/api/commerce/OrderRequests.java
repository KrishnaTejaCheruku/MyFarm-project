package in.myfarm.api.commerce;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class OrderRequests {

	private static final String CODE_PATTERN = "[a-z0-9]+(?:-[a-z0-9]+)*";
	private static final String PHONE_PATTERN = "[6-9][0-9]{9}";
	private static final String PINCODE_PATTERN = "[0-9]{6}";
	private static final String SKU_PATTERN = "[A-Za-z0-9][A-Za-z0-9._-]*";

	private OrderRequests() {
	}

	public record Item(
			@NotBlank @Size(max = 80) @Pattern(regexp = SKU_PATTERN) String sku,
			@Min(1) @Max(50) int quantity) {
	}

	public record PlaceOrder(
			@NotBlank @Size(max = 64) @Pattern(regexp = CODE_PATTERN)
			String serviceAreaCode,
			@NotBlank @Size(max = 64) @Pattern(regexp = CODE_PATTERN)
			String deliveryWindowCode,
			@NotBlank @Size(max = 120) String customerName,
			@NotBlank @Pattern(regexp = PHONE_PATTERN) String customerPhone,
			@NotBlank @Size(max = 200) String deliveryAddressLine1,
			@Size(max = 200) String deliveryAddressLine2,
			@NotBlank @Pattern(regexp = PINCODE_PATTERN) String deliveryPincode,
			@NotNull PaymentMethod paymentMethod,
			@NotEmpty @Size(max = 50) List<@Valid Item> items) {
	}
}
