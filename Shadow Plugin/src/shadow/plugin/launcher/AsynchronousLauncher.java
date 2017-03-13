package shadow.plugin.launcher;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleOutputStream;

import shadow.plugin.ShadowPlugin;

public class AsynchronousLauncher extends Thread {
	
	private IPath path;
	
	public AsynchronousLauncher(IPath path) {
		this.path = path;
	}

	@Override
	public void run() {		
		String cmd = "java";
		String pathName = path.toString();			
		
		IPreferenceStore preferenceStore = ShadowPlugin.getDefault()
				.getPreferenceStore();
		String pathToJar = preferenceStore.getString("PATH");
		
		if( pathToJar == null || !pathToJar.toLowerCase().endsWith(".jar") || !Files.exists(Paths.get(pathToJar)) ) {
			IOConsole console = ConsoleRunner.getConsole("Shadow Build");
			IOConsoleOutputStream stream = console.newOutputStream();
			stream.setColor(new Color(null, 255, 0, 0));
			try {
				stream.write("Cannot compile: Invalid path to shadow.jar set in Shadow plug-in preferences");
				stream.flush();
				stream.close();
			} 
			catch (IOException e) 
			{}
			return;
		}

		ArrayList<String> inputArgs = new ArrayList<String>();
		inputArgs.add(cmd);
		inputArgs.add("-jar");
		inputArgs.add(pathToJar);
		inputArgs.add(pathName);
		
		ConsoleRunner consoleRunner = new ConsoleRunner(); 
		int result = consoleRunner.runProcess(inputArgs, "Shadow Build");

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
