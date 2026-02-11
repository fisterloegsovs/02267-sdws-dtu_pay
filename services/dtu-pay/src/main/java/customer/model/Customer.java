package customer.model;

public record Customer(
		String cpr,
		String firstName,
		String lastName,
		String account,
		String address
) {
	public Customer {}

	/**
	 * @author Jonas Puidokas (137282)
	 */
	public void validate() {
		validateCpr(cpr);
		validateName(firstName, "First name");
		validateName(lastName, "Last name");
		validateNotBlank(account, "Account");
		validateNotBlank(address, "Address");
	}

	/**
	 * @author Jonas Puidokas (137282)
	 */
	private void validateCpr(String cpr) {
		if (cpr == null || !cpr.matches("\\d{10}")) {
			throw new IllegalArgumentException("CPR must be 10 digits");
		}
	}

	/**
	 * @author Jonas Puidokas (137282)
	 */
	private void validateName(String name, String fieldName) {
		if (name == null || name.trim().isEmpty()) {
			throw new IllegalArgumentException(fieldName + " cannot be blank");
		}
	}

	/**
	 * @author Jonas Puidokas (137282)
	 */
	private void validateNotBlank(String value, String fieldName) {
		if (value == null || value.trim().isEmpty()) {
			throw new IllegalArgumentException(fieldName + " cannot be blank");
		}
	}
}
