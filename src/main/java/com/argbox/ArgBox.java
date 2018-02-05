package com.argbox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;

/**
 * ArgBox is a class able to register, validate and manage your program
 * arguments. Its purpose is to be the simplest and most dynamic argument
 * manager possible, so you can focus on your program and not argument
 * management.
 */
public class ArgBox {

	// TODO collect errors instead of throwing right away each error. Build a
	// collection filled by only one method which will decide when shouting is
	// necessary. Then after parsing when there are errors, shout.

	/**
	 * Predicate used to always validate an argument, by default.
	 */
	private static final Predicate<String> DEFAULT_VALIDATOR = value -> true;

	private static final String MUST_START_WITH_MSG = "[%1s] %2s must start with '%3s' !";

	/**
	 * The map that will contain the parsed command line. Keys are the argument
	 * long calls and values are the values passed to those arguments.
	 */
	private final Map<String, String> parsedArguments = new LinkedHashMap<>();

	/**
	 * Set containing all the arguments created by the working program with the
	 * register methods, before parsing the command line.
	 */
	private final Set<Argument> registeredArguments = new TreeSet<>();

	/**
	 * List of String arguments on the command line that were not consumed by
	 * the registered arguments.
	 */
	private final List<String> leftovers = new ArrayList<>();

	/**
	 * Default constructor, provides the program with an automatic help
	 * argument.
	 */
	public ArgBox() {
		this.register("HELP", "-hlp", "--help",
				"If present on the command line, the program will print out the help manual and exit. #helpception");
	}

	/**
	 * Register an argument for the running program.
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
	 *            True if this argument does not need any value, like a flag, on
	 *            the command line.
	 * @param validator
	 *            A Predicate<String> providing some logical validation rule
	 *            that needs to be true for this argument's value to be a valid
	 *            one.
	 */
	public void register(final String argName, final String shortCall, final String longCall, final String helpLine,
			final boolean mandatory, final boolean valueNotRequired, final Predicate<String> validator) {
		shoutIfNotTrue(() -> !StringUtils.isAnyBlank(argName, shortCall, longCall, helpLine),
				"At least one of these parameters is null or empty : argName, shortCall, longCall, helpLine.");
		shoutIfNotTrue(() -> shortCall.startsWith("-"), String.format(MUST_START_WITH_MSG, argName, "shortCall", "-"));
		shoutIfNotTrue(() -> longCall.startsWith("--"), String.format(MUST_START_WITH_MSG, argName, "longCall", "--"));
		shoutIfExists(registeredArguments, arg -> arg.getArgName().equals(argName),
				String.format("An argument named %1s has already been registered !", argName));
		shoutIfExists(registeredArguments, arg -> arg.getShortCall().equals(shortCall),
				String.format("An argument using the shortCall %1s has already been registered !", shortCall));
		shoutIfExists(registeredArguments, arg -> arg.getLongCall().equals(longCall),
				String.format("An argument using the longCall %1s has already been registered !", longCall));
		registeredArguments.add(new Argument(argName, shortCall, longCall, helpLine, mandatory, valueNotRequired,
				null == validator ? DEFAULT_VALIDATOR : validator));
	}

	public void register(final String argName, final String shortCall, final String longCall, final String helpLine,
			final Boolean mandatory, final boolean valueNotRequired) {
		register(argName, shortCall, longCall, helpLine, mandatory, valueNotRequired, DEFAULT_VALIDATOR);
	}

	public void register(final String argName, final String shortCall, final String longCall, final String helpLine,
			final Boolean mandatory) {
		register(argName, shortCall, longCall, helpLine, mandatory, false, DEFAULT_VALIDATOR);
	}

	public void register(final String argName, final String shortCall, final String longCall, final String helpLine) {
		register(argName, shortCall, longCall, helpLine, false, false, DEFAULT_VALIDATOR);
	}

	public void resolveCommandLine(final boolean forbidLeftovers, final String... args) {
		if ((args != null) && (args.length > 0)) {
			// TODO check presence of --help argument and print help
			// immediately.
			final Iterator<String> it = Arrays.asList(args).iterator();
			while (it.hasNext()) {
				final String arg = it.next();
				final Argument argument = resolveArgument(arg);
				if (null != argument) {
					final String longCall = argument.getLongCall();
					if (!argument.isValueNotRequired() && it.hasNext()) {
						parsedArguments.put(longCall, it.next());
					} else {
						parsedArguments.put(longCall, null);
					}
				} else {
					leftovers.add(arg);
				}
			}
		}
		checkMandatoryArguments();
		validateArgumentValues();
		checkLeftovers(forbidLeftovers);
	}

	public void resolveCommandLine(final String... args) {
		resolveCommandLine(true, args);
	}

	/**
	 * Builds the help so the program using ArgBox can print it as it wishes,
	 * where it wishes.
	 */
	public String getHelp() {
		final StringBuilder helpBuilder = new StringBuilder("HELP MANUAL\n\n");
		for (final Argument arg : registeredArguments) {
			helpBuilder.append(String.format("- %1s : %2s | %3s\n", arg.getArgName(), arg.getShortCall(), arg
					.getLongCall()));
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
	 */
	private void checkMandatoryArguments() {
		registeredArguments.stream().filter(argument -> argument.isMandatory()).forEach(arg -> {
			shoutIfNotTrue(() -> parsedArguments.containsKey(arg.getLongCall()),
					String.format("The argument %1s is required !", arg.getLongCall()));
		});
	}

	private void validateArgumentValues() {
		parsedArguments.forEach((key, value) -> {
			final Argument argument = resolveArgument(key);
			if ((null != argument) && (null != argument.getValidator())) {
				if (!argument.isValueNotRequired() && (null == value)) {
					throwIllegalArgumentException(String.format(
							"The argument %1s requires a value but has an empty one : %2s", key,
							value));
				}
				shoutIfNotTrue(() -> argument.getValidator().test(value),
						String.format("The argument %1s has an incorrect value : %2s", argument.getLongCall(), value));
			}
		});
	}

	private void checkLeftovers(final boolean forbidLeftovers) {
		shoutIfNotTrue(() -> !(forbidLeftovers && !leftovers.isEmpty()),
				String.format("There are unused arguments on the command line : %1s", leftovers));
	}

	private Argument resolveArgument(final String argString) {
		return registeredArguments.stream()
				.filter(arg -> arg.getShortCall().equals(argString) || arg.getLongCall().equals(argString))
				.findFirst()
				.get();
	}

	private void shoutIfNotTrue(final BooleanSupplier supplier, final String shoutMessage) {
		if (!supplier.getAsBoolean()) {
			throwIllegalArgumentException(shoutMessage);
		}
	}

	private void shoutIfExists(final Collection<Argument> argCollection, final Predicate<Argument> predicate,
			final String shoutMessage) {
		if (argCollection.stream().anyMatch(predicate)) {
			throwIllegalArgumentException(shoutMessage);
		}
	}

	private void throwIllegalArgumentException(final String message) {
		throw new IllegalArgumentException(message);
	}

}