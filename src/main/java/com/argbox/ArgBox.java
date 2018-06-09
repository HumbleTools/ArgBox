package com.argbox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.argbox.exception.ArgBoxException;
import com.argbox.model.Argument;
import com.argbox.model.ParsedArgument;

/**
 * ArgBox is a class able to register, validate and manage your program
 * arguments. Its purpose is to be the simplest and most dynamic argument
 * manager possible, so you can focus on your program and not argument
 * management.
 */
public class ArgBox {

	private static final String HELP_LONG_CALL = "--help";

	private static final String HELP_SHORT_CALL = "-hlp";

	// TODO correct javadoc.
	// TODO provide possibility to personalize the validation error message
	// TODO JUnits

	/**
	 * Predicate used to always validate an argument, by default.
	 */
	private static final Predicate<String> DEFAULT_VALIDATOR = value -> true;

	private static final String MUST_START_WITH_MSG = "[%1$s] %2$s must start with '%3$s' !";

	/**
	 * The map that will contain the parsed command line. Keys are the argument long
	 * calls and values are the values passed to those arguments.
	 */
	private final Map<Argument, ParsedArgument> parsedArguments = new LinkedHashMap<>();

	/**
	 * Set containing all the arguments created by the working program with the
	 * register methods, before parsing the command line.
	 */
	private final Set<Argument> registeredArguments = new TreeSet<>();

	/**
	 * List of String arguments on the command line that were not consumed by the
	 * registered arguments.
	 */
	private final List<String> leftovers = new ArrayList<>();

	private final String[] args;

	/**
	 * Default constructor, provides the program with an automatic help argument.
	 */
	public ArgBox(final String... args) {
		try {
			this.args = args;
			register("HELP", HELP_SHORT_CALL, HELP_LONG_CALL,
					"If present on the command line, the program will print out the help manual and exit. #helpception");
		} catch (final ArgBoxException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Registers an argument for the running program.
	 *
	 * @param argName
	 *            The name for this argument.
	 * @param shortCall
	 *            The short version of this argument on the command line.
	 * @param longCall
	 *            The long version of this argument on the command line.
	 * @param helpLine
	 *            The help line to display if the user calls for help.
	 * @param mandatory
	 *            True is this argument is mandatory on the command line.
	 * @param valueNotRequired
	 *            True if this argument does not need any value, like a flag, on the
	 *            command line.
	 * @param validator
	 *            A Predicate<String> providing some logical validation rule that
	 *            needs to be true for this argument's value to be a valid one.
	 * @throws ArgBoxException
	 */
	public void register(final String argName, final String shortCall, final String longCall, final String helpLine,
			final boolean mandatory, final boolean valueNotRequired, final Predicate<String> validator)
			throws ArgBoxException {
		String message = "At least one of these parameters is null or empty : argName, shortCall, longCall, helpLine.";
		throwException(() -> StringUtils.isAnyBlank(argName, shortCall, longCall, helpLine),
				getArgBoxExceptionSupplier(message));

		message = String.format(MUST_START_WITH_MSG, argName, "shortCall", "-");
		throwException(() -> !shortCall.startsWith("-"), getArgBoxExceptionSupplier(message));

		message = String.format(MUST_START_WITH_MSG, argName, "longCall", "--");
		throwException(() -> !longCall.startsWith("--"), getArgBoxExceptionSupplier(message));

		message = String.format("An argument named %1$s has already been registered !", argName);
		throwException(() -> registeredArguments.stream().anyMatch(arg -> arg.getArgName().equals(argName)),
				getArgBoxExceptionSupplier(message));

		message = String.format("An argument using the shortCall %1$s has already been registered !", shortCall);
		throwException(() -> registeredArguments.stream().anyMatch(arg -> arg.getShortCall().equals(shortCall)),
				getArgBoxExceptionSupplier(message));

		message = String.format("An argument using the longCall %1$s has already been registered !", longCall);
		throwException(() -> registeredArguments.stream().anyMatch(arg -> arg.getLongCall().equals(longCall)),
				getArgBoxExceptionSupplier(message));

		registeredArguments.add(new Argument(argName, shortCall, longCall, helpLine, mandatory, valueNotRequired,
				null == validator ? DEFAULT_VALIDATOR : validator));
	}

	public void register(final String argName, final String shortCall, final String longCall, final String helpLine,
			final Boolean mandatory, final boolean valueNotRequired) throws ArgBoxException {
		register(argName, shortCall, longCall, helpLine, mandatory, valueNotRequired, DEFAULT_VALIDATOR);
	}

	public void register(final String argName, final String shortCall, final String longCall, final String helpLine,
			final Boolean mandatory) throws ArgBoxException {
		register(argName, shortCall, longCall, helpLine, mandatory, false, DEFAULT_VALIDATOR);
	}

	public void register(final String argName, final String shortCall, final String longCall, final String helpLine)
			throws ArgBoxException {
		register(argName, shortCall, longCall, helpLine, false, false, DEFAULT_VALIDATOR);
	}

	/**
	 * Calls {@link #getArgBoxExceptionSupplier(String, List)} with no errors in the
	 * second argument.
	 *
	 * @param message
	 *            See {@link #getArgBoxExceptionSupplier(String, List)}.
	 * @return The exception Supplier.
	 */
	private Supplier<ArgBoxException> getArgBoxExceptionSupplier(final String message) {
		return () -> new ArgBoxException(message);
	}

	/**
	 * Provides an {@link ArgBoxException} Supplier for a message and error list.
	 *
	 * @param message
	 *            The message of the Exception.
	 * @param errors
	 *            The list of errors that justify the exception.
	 * @return The exception supplier.
	 */
	private Supplier<ArgBoxException> getArgBoxExceptionSupplier(final String message, final List<String> errors) {
		return () -> new ArgBoxException(message, errors);
	}

	/**
	 * Returns true if the HELP argument is present on the command line. Call this
	 * method before parsing the command line, so you know if you need to print out
	 * help and exit or parse the whole command line.
	 *
	 * @param args
	 *            The whole command line.
	 * @return True is the help has been asked, false otherwise.
	 */
	public boolean isHelpNeeded(final String... args) {
		return StringUtils.containsAny(StringUtils.join(args), HELP_SHORT_CALL, HELP_LONG_CALL);
	}

	/**
	 * Resolves the command line by parsing the different arguments, listing lef
	 * over strings, checking if the mandatory arguments are all there, and
	 * validating all the values.
	 *
	 * @param forbidLeftovers
	 *            Indicates if useless arguments are allowed on the command line.
	 * @param args
	 *            This parameter should contain the main program's arguments.
	 * @throws ArgBoxException
	 *             If a problem or more are detected on the command line.
	 */
	public void resolveCommandLine(final boolean forbidLeftovers, final String... args) throws ArgBoxException {
		if ((args != null) && (args.length > 0)) {
			final Iterator<String> it = Arrays.asList(args).iterator();
			while (it.hasNext()) {
				final String argString = it.next();
				final Argument argument = resolveArgument(argString);
				if (null == argument) {
					leftovers.add(argString);
				} else if (argument.isValueRequired() && it.hasNext()) {
					parsedArguments.put(argument, new ParsedArgument(argument, argString, it.next()));
				} else {
					parsedArguments.put(argument, new ParsedArgument(argument, argString, null));
				}
			}
		}
		checkMandatoryArguments();
		validateArgumentValues();
		checkLeftovers(forbidLeftovers);
	}

	/**
	 * Calls {@link #resolveCommandLine(boolean, String...)} with forbidLeftovers to
	 * true.
	 *
	 * @param args
	 *            See {@link #resolveCommandLine(boolean, String...)}.
	 * @throws ArgBoxException
	 *             See {@link #resolveCommandLine(boolean, String...)}.
	 */
	public void resolveCommandLine(final String... args) throws ArgBoxException {
		resolveCommandLine(true, args);
	}

	/**
	 * Builds the help String so the program using ArgBox can print it.
	 */
	public String getHelp() {
		final StringBuilder helpBuilder = new StringBuilder("HELP MANUAL\n\n");
		for (final Argument arg : registeredArguments) {
			helpBuilder.append(
					String.format("- %1s : %2s | %3s\n", arg.getArgName(), arg.getShortCall(), arg.getLongCall()));
			helpBuilder.append(arg.getHelpLine());
			helpBuilder.append("\n");
			if (arg.isMandatory()) {
				helpBuilder.append("This argument is mandatory on the command line.\n");
			}
			if (arg.isValueNotRequired()) {
				helpBuilder.append("This argument has no value. If a value is present, it will be ignored.\n");
			}
			helpBuilder.append("\n");
		}
		return helpBuilder.toString();
	}

	/**
	 * Checks if every mandatory argument is present on the command line.
	 *
	 * @throws ArgBoxException
	 *             If some mandatory arguments are not found on the command line.
	 */
	private void checkMandatoryArguments() throws ArgBoxException {
		final List<String> errors = registeredArguments.stream()
				.filter(arg -> arg.isMandatory())
				.filter(arg -> !parsedArguments.containsKey(arg))
				.map(arg -> String.format("The argument %1s is required !", arg.getLongCall()))
				.collect(Collectors.toList());
		throwException(() -> !errors.isEmpty(), getArgBoxExceptionSupplier("Some arguments are missing !", errors));
	}

	/**
	 * Validates the values of the arguments on the command line. For each argument
	 * that requires a value, it checks if the value is present and if the validator
	 * of the registered argument validates it or not.
	 *
	 * @throws ArgBoxException
	 *             If the argument's value is invalid, for one or more reasons.
	 */
	private void validateArgumentValues() throws ArgBoxException {
		final List<String> errorMessages = new ArrayList<>();
		parsedArguments.values().stream()
				.filter(parsedArg -> parsedArg.isValueRequired())
				.filter(parsedArg -> {
					final boolean emptyValue = null == parsedArg.getValue();
					if (emptyValue) {
						errorMessages.add(String.format("The argument %1s has no value !", parsedArg.getCommandArg()));
					}
					return !emptyValue;
				})
				.peek(parsedArg -> {
					if (parsedArg.getValidator().negate().test(parsedArg.getValue())) {
						errorMessages.add(String.format("The value %1s for the argument %2s is not valid !",
								parsedArg.getValue(), parsedArg.getCommandArg()));
					}
				})
				.close();
		throwException(() -> CollectionUtils.isNotEmpty(errorMessages),
				getArgBoxExceptionSupplier("One or more arguments have errors with their values !", errorMessages));
	}

	/**
	 * Checks if there are left over Strings in the command line after parsing it.
	 * If forbidLeftovers is false, this method does nothing.
	 *
	 * @param forbidLeftovers
	 *            If leftovers are forbidden or not.
	 * @throws ArgBoxException
	 *             If there are leftovers when they are forbidden.
	 */
	private void checkLeftovers(final boolean forbidLeftovers) throws ArgBoxException {
		if (forbidLeftovers) {
			final List<String> leftoverMessages = leftovers.stream()
					.map(str -> String.format("The argument %s was not used.", str))
					.collect(Collectors.toList());
			throwException(() -> CollectionUtils.isNotEmpty(leftovers),
					getArgBoxExceptionSupplier("There are unused arguments on the command line !", leftoverMessages));
		}
	}

	/**
	 * Finds an argument on the command line, from its short or long version, given
	 * the String parameter. The first occurrence is returned.
	 *
	 * @param argString
	 *            The string representation of the argument.
	 * @return The first occurrence of the searched argument.
	 */
	private Argument resolveArgument(final String argString) {
		return registeredArguments.stream()
				.filter(arg -> StringUtils.equalsAny(argString, arg.getShortCall(), arg.getLongCall()))
				.findFirst()
				.get();
	}

	/**
	 * Throws the exception supplied by the Exception Supplier if the boolean
	 * Supplier returns true.
	 *
	 * @param expSupplier
	 *            The exception Supplier.
	 * @param BooleanSupplier
	 *            The boolean Supplier.
	 * @throws <E>
	 *             The exception type supplied by the Supplier.
	 */
	private <E extends Exception> void throwException(final BooleanSupplier boolSupplier, final Supplier<E> expSupplier)
			throws E {
		if (boolSupplier.getAsBoolean()) {
			throw expSupplier.get();
		}
	}

	/**
	 * Provides a copy of the arguments passed to the {@link #ArgBox(String...)}.
	 *
	 * @return The arguments of the program.
	 */
	public String[] getArgs() {
		return Arrays.copyOf(args, args.length);
	}

}
