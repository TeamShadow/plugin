package shadow.plugin.launcher;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.SwingWorker;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.console.IOConsoleOutputStream;

public class CompileWorker extends SwingWorker<Integer, Void> {
	
	private IPath path;
	private Console console;	
	private boolean compileOnly;
	private IPath executable;
	private String pathToCompiler;
	private List<String> arguments;
	private IProject project;	 
	
	public CompileWorker(IPath path, String pathToCompiler, String arguments, boolean compileOnly, IProject project) {
		this.path = path;
		this.console = Console.getConsole("Shadow Build", false);
		this.pathToCompiler = pathToCompiler;
		this.arguments = new ArrayList<String>();
		
		//turn whitespace delimited list (which might contain quoted phrases) into a list of strings
		Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(arguments);
		String executableName = null;
		boolean outputFlag = false;
		while( m.find() ) {
			String argument = m.group(1).replace("\"", "");			
		    this.arguments.add(argument);
		    
		    if( outputFlag )
		    	executableName = argument;
		    
		    if( argument.equals("-o") || argument.equals("--output") )
		    	outputFlag = true;
		    else
		    	outputFlag = false;
		}
		this.compileOnly = compileOnly;
		this.project = project;
		
		if( executableName != null ) {			
			if( Paths.get(executableName).isAbsolute() )			
				executable = new org.eclipse.core.runtime.Path(executableName);
			else
				executable =  path.removeLastSegments(1).append(executableName);
		}
		else {
			String osName = System.getProperty("os.name").toLowerCase();
			if( osName.contains("windows") )			
				executable = path.removeFileExtension().addFileExtension("exe");		
			else						
				executable = path.removeFileExtension();
		}
	}	

	@Override
	protected Integer doInBackground() throws Exception {				
		String pathName = path.toString();
		Path pathFile = Paths.get(pathName);			
		
		Path executableFile = Paths.get(executable.toString());
		
		// if we're running after compilation
		// and executable is newer than changes to file, there's no need to recompile
		// (unless some other file has changed, which we are not worrying about yet)
		if( !compileOnly && Files.exists(pathFile) && Files.exists(executableFile) &&
			Files.getLastModifiedTime(pathFile).compareTo(Files.getLastModifiedTime(executableFile)) <= 0 )
			return 0;
					
		IOConsoleOutputStream stream = console.getOutput();
		try {
			stream.write("Compiling " + pathName + "\n");
			stream.flush();				
		} 
		catch (IOException e) 
		{}

		ArrayList<String> inputArgs = new ArrayList<String>();
		inputArgs.add(pathToCompiler);		
		inputArgs.add(pathName);
		inputArgs.addAll(arguments);		
		
		int value = runProcess(inputArgs, console);
		
		if( project != null )
		    project.refreshLocal(IResource.DEPTH_INFINITE, null);		
		
		try {
			if( value == 0 )			
				stream.write("Compilation of " + pathName + " succeeded.\n");
			else
				stream.write("Compilation of " + pathName + " failed.\n");
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
			if(!compileOnly && get() == 0) {
				ArrayList<String> inputArgs = new ArrayList<String>();
				String executableName = executable.toString(); 
				inputArgs.add(executableName);				
						
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
			ProcessBuilder builder = new ProcessBuilder(inputArgs);
			process = builder.start();			
			console.setProcess( process );

			new Pipe( process.getInputStream(), console.getOutput() ).start();
			new Pipe( process.getErrorStream(), console.getError() ).start();	
			if( console.getInput() !=  null )
				new Pipe( console.getInput(), process.getOutputStream() ).start();
			
			return process.waitFor();			
		} 
		catch (IOException | InterruptedException e) {
			try {
				console.getError().write("Process failed to run: " + e.getLocalizedMessage() + System.lineSeparator());
			} catch (NullPointerException | IOException exception) {
				System.err.println("Process failed to run: " + e.getLocalizedMessage());
			}			
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
