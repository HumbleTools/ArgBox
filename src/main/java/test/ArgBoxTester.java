package test;

import org.junit.Test;

import com.argbox.ArgBox;
import com.argbox.exception.ArgBoxException;
import com.argbox.model.Argument;

import junit.framework.Assert;

public class ArgBoxTester {

	private static final Argument nameArg = new Argument("Name", "-nm", "--name", "This is the name.", true, false,
			str -> str.startsWith("B"));

	private static final Argument ageArg = new Argument("Age", "-ag", "--age", "This is the age.", true, false, str -> {
		boolean isGood = true;
		int value = 0;
		try {
			value = Integer.parseInt(str);
		} catch (final NumberFormatException nfe) {
			isGood = false;
		}
		return isGood && (value > 0);
	});

	private static final Argument verboseArg = new Argument("Verbose", "-v", "--verbose",
			"Flag to trigger verbose mode.", false, true, null);

	@Test
	public void testGetHelp() throws ArgBoxException {
		final ArgBox argBox = new ArgBox();
		callLongRegister(argBox, nameArg);
		callLongRegister(argBox, ageArg);
		callLongRegister(argBox, verboseArg);
		final String helpText = argBox.getHelp();
		System.out.print(helpText);
		Assert.assertEquals(
				"HELP MANUAL\n\n- HELP : -hlp | --help\nIf present on the command line, the program will print out the help manual and exit. #helpception"
						+ "\nThis argument is not mandatory on the command line.\n\n"
						+ "- Name : -nm | --name\nThis is the name.\nThis argument is mandatory on the command line.\n\n"
						+ "- Age : -ag | --age\nThis is the age.\nThis argument is mandatory on the command line.\n\n"
						+ "- Verbose : -v | --verbose\nFlag to trigger verbose mode.\nThis argument is not mandatory on the command line.\n"
						+ "This argument has no value. If a value is present, it will be ignored.\n\n",
				helpText);
	}

	private void callLongRegister(final ArgBox argBox, final Argument arg) throws ArgBoxException {
		argBox.register(arg.getArgName(), arg.getShortCall(), arg.getLongCall(),
				arg.getHelpLine(), arg.isMandatory(), arg.isValueNotRequired(), arg.getValidator());
	}

}
