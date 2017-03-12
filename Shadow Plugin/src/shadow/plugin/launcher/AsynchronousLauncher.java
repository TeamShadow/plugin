package shadow.plugin.launcher;

import java.util.ArrayList;

import org.eclipse.core.runtime.IPath;

public class AsynchronousLauncher extends Thread {
	
	private IPath path;
	
	public AsynchronousLauncher(IPath path) {
		this.path = path;
	}

	@Override
	public void run() {		
		String cmd = "shadowc";
		String pathName = path.toString();																			

		ArrayList<String> inputArgs = new ArrayList<String>();
		inputArgs.add(cmd);				
		inputArgs.add(pathName);
		
		ConsoleRunner consoleRunner = new ConsoleRunner(); 
		int result = consoleRunner.runProcess(inputArgs, "Shadow Build Console");

		if(result == 0) {
			IPath executableFile; 

			String osName = System.getProperty("os.name").toLowerCase();
			inputArgs = new ArrayList<String>();


			if(osName.contains("windows")) {
				executableFile = path.removeFileExtension().addFileExtension("exe");
				inputArgs.add(executableFile.toString());
			}
			else {
				executableFile = path.removeFileExtension();
				inputArgs.add("./" + executableFile.toString());
			}		

			consoleRunner.runProcess(inputArgs, executableFile.toString());
		}
	}	
}
