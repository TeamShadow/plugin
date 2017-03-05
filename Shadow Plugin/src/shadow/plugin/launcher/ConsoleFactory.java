package shadow.plugin.launcher;

import org.eclipse.ui.console.*;

public class ConsoleFactory implements IConsoleFactory 
{

	private IConsoleManager consoleManager = null;
	
	
	@Override
	public void openConsole() {
		// TODO Auto-generated method stub
		consoleManager = ConsolePlugin.getDefault().getConsoleManager();
		MessageConsole console = new MessageConsole("Shadow Build", null);
		
		consoleManager.addConsoles( new IConsole[] { null } );
		consoleManager.showConsoleView( console );
		
	}

}
