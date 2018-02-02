public class Argument {

	private final String argName;

	private final String shortCall;

	private final String longCall;

	private final String helpLine;

	private final boolean mandatory;

	private final boolean valueNotRequired;

	public Argument(final String argName, final String shortCall, final String longCall, final String helpLine,
			final boolean mandatory, final boolean valueNotRequired) {
		this.argName = argName;
		this.shortCall = shortCall;
		this.longCall = longCall;
		this.helpLine = helpLine;
		this.valueNotRequired = valueNotRequired;
		this.mandatory = mandatory;
	}

	public String getArgName() {
		return argName;
	}

	public String getShortCall() {
		return shortCall;
	}

	public String getLongCall() {
		return longCall;
	}

	public String getHelpLine() {
		return helpLine;
	}

	public boolean isMandatory() {
		return mandatory;
	}

	public boolean isValueNotRequired() {
		return valueNotRequired;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((argName == null) ? 0 : argName.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Argument)) {
			return false;
		}
		final Argument other = (Argument) obj;
		if (argName == null) {
			if (other.argName != null) {
				return false;
			}
		} else if (!argName.equals(other.argName)) {
			return false;
		}
		return true;
	}

}