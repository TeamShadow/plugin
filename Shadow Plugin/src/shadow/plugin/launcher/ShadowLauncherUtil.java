package shadow.plugin.launcher;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;


public class ShadowLauncherUtil {

	public static void setConsole(PrintStream ps)
	{
		System.setOut(ps);		
		System.setErr(ps);
	}
	
	public CommandPromptOutput runProcess(ArrayList<String> inputArgs) throws IOException, InterruptedException
	{
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager conMan = plugin.getConsoleManager();
		IConsole[] existing = conMan.getConsoles();
		MessageConsole console = null;// new MessageConsole("", null);

		String name = "My Console";

		for (int i = 0; i < existing.length && console == null; i++)
			if (name.equals(existing[i].getName()))
				console = (MessageConsole) existing[i];

		if(console == null)
		{
			console = new MessageConsole("My Console", null);
			console.activate();
		}
		//no console found, so create a new one

		conMan.addConsoles(new IConsole[]{console});				
		MessageConsoleStream stream = console.newMessageStream(); 

		setConsole(new PrintStream(stream));


		// execute the command
		SystemCommandExecutor commandExecutor = new SystemCommandExecutor(inputArgs);
		int result = commandExecutor.executeCommand();


		// get the stdout and stderr from the command that was run
		StringBuilder stdout = commandExecutor.getStandardOutputFromCommand();
		StringBuilder stderr = commandExecutor.getStandardErrorFromCommand();

		CommandPromptOutput results = new CommandPromptOutput(stdout, stderr);		
		
		return results;
	}
	
	public class CommandPromptOutput
	{
		private StringBuilder stdout;
		private StringBuilder stderr;
		
		public CommandPromptOutput(StringBuilder stdout, StringBuilder stderr)
		{
			this.stdout = stdout;
			this.stderr = stderr;
		}
		
		public StringBuilder getStdout()
		{
			return this.stdout;
		}
		
		public StringBuilder getStderr()
		{
			return this.stderr;
		}
		
	}
}
