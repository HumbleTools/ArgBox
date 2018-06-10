/**
 *
 */
package com.argbox.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * @author Lewis
 *
 */
public class ParsedArgument extends Argument {

	String commandArg;

	String value;

	public ParsedArgument(final Argument arg, final String commandArg, final String value) {
		super(arg.getArgName(), arg.getShortCall(), arg.getLongCall(), arg.getHelpLine(), arg.isMandatory(),
				arg.isValueNotRequired(), arg.getValidator());
		this.commandArg = commandArg;
		this.value = value;
	}

	/**
	 * @return the commandArg
	 */
	public String getCommandArg() {
		return commandArg;
	}

	/**
	 * @param commandArg
	 *            the commandArg to set
	 */
	public void setCommandArg(final String commandArg) {
		this.commandArg = commandArg;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param value
	 *            the value to set
	 */
	public void setValue(final String value) {
		this.value = value;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(9, 5)
				.appendSuper(super.hashCode())
				.append(value)
				.append(commandArg)
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
		if (!(obj instanceof ParsedArgument)) {
			return false;
		}
		final ParsedArgument other = (ParsedArgument) obj;
		return new EqualsBuilder()
				.appendSuper(super.equals(other))
				.append(value, other.value)
				.append(commandArg, other.commandArg)
				.isEquals();
	}

}
