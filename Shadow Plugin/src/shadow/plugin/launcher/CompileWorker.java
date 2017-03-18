package shadow.plugin.launcher;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.console.IOConsoleOutputStream;

import shadow.plugin.ShadowPlugin;

public class CompileWorker extends SwingWorker<Integer, Void> {
	
	private IPath path;
	private Console console;	
	private boolean runAfterCompilation;
	private IPath executable;
	
	public CompileWorker(IPath path, boolean runAfterCompilation) {
		this.path = path;
		this.console = Console.getConsole("Shadow Build", false);
		this.runAfterCompilation = runAfterCompilation;
	}	

	@Override
	protected Integer doInBackground() throws Exception {				
		String pathName = path.toString();
		
		String osName = System.getProperty("os.name").toLowerCase();		

		if(osName.contains("windows"))
			executable = path.removeFileExtension().addFileExtension("exe");
		else
			executable = path.removeFileExtension();
		
		Path pathFile = Paths.get(pathName);
		Path executableFile = Paths.get(executable.toString());		
		
		// if we're running after compilation
		// and executable is newer than changes to file, there's no need to recompile
		// (unless some other file has changed, which we are not worrying about yet)
		if( runAfterCompilation && Files.exists(pathFile) && Files.exists(executableFile) &&
			Files.getLastModifiedTime(pathFile).compareTo(Files.getLastModifiedTime(executableFile)) <= 0 )
			return 0;		
		
		String cmd = "java";
		IPreferenceStore preferenceStore = ShadowPlugin.getDefault()
				.getPreferenceStore();
		String pathToJar = preferenceStore.getString("PATH");
		
		if( pathToJar == null || !pathToJar.toLowerCase().endsWith(".jar") || !Files.exists(Paths.get(pathToJar)) ) {			
			IOConsoleOutputStream stream = console.getError();			
			try {
				stream.write("Cannot compile: Invalid path to shadow.jar set in Shadow plug-in preferences\n");
				stream.flush();				
			} 
			catch (IOException e) 
			{}			
						
			return -1;
		}
					
		IOConsoleOutputStream stream = console.getOutput();
		try {
			stream.write("Compiling " + pathName + "\n");
			stream.flush();				
		} 
		catch (IOException e) 
		{}

		ArrayList<String> inputArgs = new ArrayList<String>();
		inputArgs.add(cmd);
		inputArgs.add("-jar");
		inputArgs.add(pathToJar);
		inputArgs.add(pathName);
		
		int value = runProcess(inputArgs, console);
		
		try {
			if( value == 0 )			
				stream.write("Compilation succeeded.\n");
			else
				stream.write("Compilation failed.\n");
			stream.flush();
		}
		catch (IOException e) 
		{}
		
		return value;
	}
	
	@Override
    public void done() {		
		console.markTerminated();		
		try {
			if(runAfterCompilation && get() == 0) {
				String osName = System.getProperty("os.name").toLowerCase();
				ArrayList<String> inputArgs = new ArrayList<String>();
				String executableName = executable.toString(); 

				if(osName.contains("windows")) 
					inputArgs.add(executableName);				
				else 					
					inputArgs.add("./" + executableName);
						
				Console programConsole = Console.getConsole(executableName);
				
				new SwingWorker<Integer, Void>() {
					@Override
					protected Integer doInBackground() throws Exception {
						return runProcess(inputArgs, programConsole);
					}
					
					@Override
					public void done() {
						programConsole.markTerminated();
					}
				}.execute();
			}
		}
		catch (InterruptedException | ExecutionException e) 
		{}
    }	
	
	public static int runProcess(ArrayList<String> inputArgs, Console console) {
		// execute the command		
		Process process = null;
		try {
			process = new ProcessBuilder(inputArgs).start();			
			console.setProcess( process );

			new Pipe( process.getInputStream(), console.getOutput() ).start();
			new Pipe( process.getErrorStream(), console.getError() ).start();	
			if( console.getInput() !=  null )
				new Pipe( console.getInput(), process.getOutputStream() ).start();
			
			return process.waitFor();			
		} 
		catch (IOException | InterruptedException e) {
			System.err.println("Process failed to run: " + e.getMessage());
		}
		finally {
			if( process != null ) {
				process.destroy();				
			}
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
				}
			} catch (IOException ex) { }
		}
	}
}
