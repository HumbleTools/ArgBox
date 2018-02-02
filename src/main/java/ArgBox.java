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

/**
 * ArgBox is a class able to register, validate and manage your program
 * arguments. Its purpose is to be the simplest and most dynamic argument
 * manager possible, so you can focus on your program and not argument
 * management.
 */
public class ArgBox {

	private static final String MUST_START_WITH_MSG = "[%1s] %2s must start with '%3s' !";

	/**
	 * The map that will contain the parsed command line. Keys are the argument
	 * long calls and values are the values passed to those arguments.
	 */
	private final Map<String, String> parsedArguments = new LinkedHashMap<>();

	/**
	 * Set containing all the arguments created by the working program with the
	 * register methods.
	 */
	private final Set<Argument> registeredArguments = new TreeSet<>();

	/**
	 * List of String arguments on the command line that were not consumed by
	 * the registered arguments.
	 */
	private final List<String> leftovers = new ArrayList<>();

	/**
	 * Builds the help so the program using ArgBox can print it as it wishes,
	 * where it wishes.
	 */
	public String getHelp() {
		final StringBuilder helpBuilder = new StringBuilder("HELP\n\n");
		for (final Argument arg : registeredArguments) {
			helpBuilder.append(String.format("%1s$20s%2s/%3s$10s ", arg.getArgName(), arg.getShortCall(), arg
					.getLongCall()));
			helpBuilder.append("This argument is mandatory ");
			helpBuilder.append(String.format("and must %1s be followed by a value.\n", arg.isValueNotRequired() ? "NOT"
					: ""));
			helpBuilder.append(arg.getHelpLine());
			helpBuilder.append("\n\n");
		}
		return helpBuilder.toString();
	}

	public void resolveCommandLine(final boolean forbidLeftovers, final String... args) {
		if ((args != null) && (args.length > 0)) {
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

	private void checkLeftovers(final boolean forbidLeftovers) {
		if (forbidLeftovers) {
			// TODO shout if leftovers, with output of the leftovers.
		}
	}

	private void validateArgumentValues() {
		// TODO provide a type for each argument when registering it and try to
		// validate the value against the type. Don't do this test is the type
		// is null.
		// TODO provide a sort of predicate to allow the developer to provide
		// his own validation rule.
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

	private Argument resolveArgument(final String argString) {
		return registeredArguments.stream()
				.filter(arg -> arg.getShortCall().equals(argString) || arg.getLongCall().equals(argString))
				.findFirst()
				.get();
	}

	public void register(final String argName, final String shortCall, final String longCall, final String helpLine) {
		register(argName, shortCall, longCall, helpLine, false, false);
	}

	public void register(final String argName, final String shortCall, final String longCall,
			final String helpLine, final Boolean mandatory) {
		register(argName, shortCall, longCall, helpLine, mandatory, false);
	}

	public void register(final String argName, final String shortCall, final String longCall,
			final String helpLine, final boolean mandatory, final boolean valueNotRequired) {
		shoutIfNotTrue(() -> !isAnyBlank(argName, shortCall, longCall, helpLine),
				"At least one of these parameters is null or empty : argName, shortCall, longCall, helpLine.");
		shoutIfNotTrue(() -> shortCall.startsWith("-"), String.format(MUST_START_WITH_MSG, argName, "shortCall", "-"));
		shoutIfNotTrue(() -> longCall.startsWith("--"), String.format(MUST_START_WITH_MSG, argName, "longCall", "--"));
		shoutIfExists(registeredArguments, arg -> arg.getArgName().equals(argName),
				String.format("An argument named %1s has already been registered !", argName));
		shoutIfExists(registeredArguments, arg -> arg.getShortCall().equals(shortCall),
				String.format("An argument using the shortCall %1s has already been registered !", shortCall));
		shoutIfExists(registeredArguments, arg -> arg.getLongCall().equals(longCall),
				String.format("An argument using the longCall %1s has already been registered !", longCall));
		registeredArguments.add(new Argument(argName, shortCall, longCall, helpLine, mandatory, valueNotRequired));
	}

	private void shoutIfNotTrue(final BooleanSupplier supplier, final String shoutMessage) {
		if (!supplier.getAsBoolean()) {
			throw new IllegalArgumentException(shoutMessage);
		}
	}

	private void shoutIfExists(final Collection<Argument> argCollection, final Predicate<Argument> predicate,
			final String shoutMessage) {
		if (argCollection.stream().anyMatch(predicate)) {
			throw new IllegalArgumentException(shoutMessage);
		}
	}

	/**
	 * Determines if any of the provided strings are null or blank blank.
	 *
	 * @param strings
	 *            The strings to test.
	 * @return True if at least one of the strings is null or blank, false
	 *         otherwise.
	 */
	private boolean isAnyBlank(final String... strings) {
		return Arrays.asList(strings).stream().anyMatch(str -> (null == str) || str.trim().isEmpty());
	}

}
