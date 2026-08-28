package in.myfarm.api.admin;

public final class AdminResponses {

	private AdminResponses() {
	}

	public record WhoAmI(String subject, String username) {
	}
}
