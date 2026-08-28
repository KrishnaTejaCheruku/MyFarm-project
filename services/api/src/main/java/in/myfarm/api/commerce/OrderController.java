package in.myfarm.api.commerce;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

	private static final String ORDER_NUMBER_PATTERN = "MF-[A-F0-9]{8}";

	private final OrderService orderService;

	OrderController(OrderService orderService) {
		this.orderService = orderService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public OrderResponses.Order placeOrder(
			@Valid @RequestBody OrderRequests.PlaceOrder request) {
		return orderService.placeOrder(request);
	}

	@GetMapping("/{orderNumber}")
	public OrderResponses.Order order(
			@PathVariable @Pattern(regexp = ORDER_NUMBER_PATTERN)
			String orderNumber) {
		return orderService.order(orderNumber);
	}
}
