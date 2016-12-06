package shadow.plugin.launcher;

import org.eclipse.ui.console.*;

public class ConsoleFactory implements IConsoleFactory 
{

	private IConsoleManager fConsoleManager = null;
	
	
	@Override
	public void openConsole() {
		// TODO Auto-generated method stub
		IConsoleManager consoleManager = ConsolePlugin.getDefault().getConsoleManager();
		MessageConsole console = new MessageConsole("My Console", null);
		
		consoleManager.addConsoles( new IConsole[] { null } );
		consoleManager.showConsoleView( console );
		
	}

}
