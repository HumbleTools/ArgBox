import java.util.function.Predicate;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Argument {

	private final String argName;

	private final String shortCall;

	private final String longCall;

	private final String helpLine;

	private final boolean mandatory;

	private final boolean valueNotRequired;

	private final Predicate<String> validator;

	public Argument(final String argName, final String shortCall, final String longCall, final String helpLine,
			final boolean mandatory, final boolean valueNotRequired, final Predicate<String> validator) {
		this.argName = argName;
		this.shortCall = shortCall;
		this.longCall = longCall;
		this.helpLine = helpLine;
		this.valueNotRequired = valueNotRequired;
		this.mandatory = mandatory;
		this.validator = validator;
	}

	public Predicate<String> getValidator() {
		return validator;
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
		return new HashCodeBuilder(3, 5)
				.append(argName)
				.append(shortCall)
				.append(longCall)
				.append(helpLine)
				.append(mandatory)
				.append(valueNotRequired)
				.append(validator)
				.toHashCode();
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
		return new EqualsBuilder()
				.append(argName, other.argName)
				.append(shortCall, other.shortCall)
				.append(longCall, other.longCall)
				.append(helpLine, other.helpLine)
				.append(mandatory, other.mandatory)
				.append(valueNotRequired, other.valueNotRequired)
				.append(validator, other.validator)
				.isEquals();
	}

}