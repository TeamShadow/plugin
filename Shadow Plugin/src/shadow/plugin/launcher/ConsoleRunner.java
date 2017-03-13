package shadow.plugin.launcher;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleInputStream;
import org.eclipse.ui.console.IOConsoleOutputStream;

public class ConsoleRunner {
	
	public static IOConsole getConsole(String name) {
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager conMan = plugin.getConsoleManager();
		IConsole[] existing = conMan.getConsoles();
		IOConsole console = null;
				
		for( int i = 0; i < existing.length && console == null; i++ )
			if (existing[i].getName().startsWith(name) ) {
				console = (IOConsole) existing[i];
				console.clearConsole();		
				console.activate();
			}

		if( console == null ) {
			//no console found, so create a new one
			console = new IOConsole(name, null);
			console.activate();
		}	
				
		conMan.addConsoles(new IConsole[]{console});
		return console;
	}
	
	public int runProcess(ArrayList<String> inputArgs, String name) {
		IOConsole console = getConsole(name);
		
		IOConsoleOutputStream out = console.newOutputStream();
		IOConsoleOutputStream error = console.newOutputStream();
		IOConsoleInputStream in = console.getInputStream();
		
		error.setColor(new Color(null, 255, 0, 0));		
		in.setColor(new Color(null, 0, 255, 0));
		
		// execute the command		
		Process process = null;
		try {
			process = new ProcessBuilder(inputArgs).start();

			new Pipe( process.getInputStream(), out ).start();
			new Pipe( process.getErrorStream(), error ).start();			
			new Pipe( in, process.getOutputStream() ).start();
			
			return process.waitFor();			
		} 
		catch (IOException | InterruptedException e) {
			System.err.println("Process failed to run: " + e.getMessage());
		}
		finally {
			if( process != null )
				process.destroy();
		}

		return -1;
	}

	/* A simple class used to redirect an InputStream into a specified OutputStream */
	private static class Pipe extends Thread {
		private InputStream input;
		private OutputStream output;
		public Pipe(InputStream inputStream, OutputStream outputStream) {
			input = inputStream;
			output = outputStream;
		}
		@Override
		public void run() {
			try {
				try {
					byte[] buffer = new byte[1024];
					int read = input.read(buffer);
					while (read >= 0) {	
						output.write(buffer, 0, read);
						for( int i = 0; i < read; ++i )
							if( buffer[i] == '\n') {
								output.flush();								
								break;
							}
						read = input.read(buffer);
					}
				} finally {
					try {
						input.close();
					} catch (IOException ex) { }
					try {
						output.flush();
					} catch (IOException ex) { }
					try {
						output.close();
					} catch (IOException ex) { }
				}
			} catch (IOException ex) { }
		}
	}
	
}
