package in.myfarm.api.delivery;

public final class ServiceAreaResponses {

	private ServiceAreaResponses() {
	}

	public record LocalizedText(String en, String te) {
	}

	public record ServiceArea(
			String code,
			LocalizedText name,
			String city,
			String state,
			String timezone,
			boolean subscriptionRequired) {
	}

	public record Eligibility(
			String requestedAreaCode,
			String pincode,
			boolean serviceable,
			ServiceArea serviceArea) {
	}
}
