/**
 *
 */
package com.argbox.model;

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

}
