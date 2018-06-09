package test;

import org.junit.Test;

import com.argbox.ArgBox;
import com.argbox.exception.ArgBoxException;

import junit.framework.Assert;

public class ArgBoxTester {

	@Test
	public void testGetHelp() throws ArgBoxException {
		final ArgBox argBox = new ArgBox();
		argBox.register("Test Arg", "-targ", "--testarg", "This is a test argument !");
		argBox.register("Test Arg2", "-targ2", "--testarg2", "This is another test argument !", true, true);
		final String helpText = argBox.getHelp();
		System.out.print(helpText);
		Assert.assertEquals(helpText,
				"HELP MANUAL\n\n- HELP : -hlp | --help\nIf present on the command line, the program will print out the help manual and exit. #helpception\n\n"
						+ "- Test Arg : -targ | --testarg\nThis is a test argument !\n\n"
						+ "- Test Arg2 : -targ2 | --testarg2\nThis is another test argument !\nThis argument is mandatory on the command line.\nThis argument has no value. If a value is present, it will be ignored.\n\n");
	}

}
