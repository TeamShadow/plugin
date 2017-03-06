package shadow.plugin.launcher;

import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

import shadow.plugin.launcher.ShadowLauncherUtil.CommandPromptOutput;

public class ShadowLaunchShortcut implements ILaunchShortcut {

	@Override
	public void launch(ISelection editor, String mode) 
	{
		IWorkbench wb = PlatformUI.getWorkbench();
		IWorkbenchWindow window = wb.getActiveWorkbenchWindow();

		if(window != null)
		{
			IWorkbenchPage page = window.getActivePage();
			if(page != null)
			{
				IEditorPart editorPart = page.getActiveEditor();
				IEditorInput input = editorPart.getEditorInput();
				IPath path = ((FileEditorInput)input).getPath();

				String cmd = "shadowc";
				String pathName = path.toString();																			

				ArrayList<String> inputArgs = new ArrayList<String>();
				inputArgs.add(cmd);				
				inputArgs.add(pathName);

				try {

					ShadowLauncherUtil launchProcesses = new ShadowLauncherUtil(); 
					CommandPromptOutput results = launchProcesses.runProcess(inputArgs);
					
					System.out.println(results.getStdout());					
					System.err.println(results.getStderr());					

					if(results.getStatus() == 0)
					{
						/* TODO: The execution of .exe files are only proven to work on Windows machines.
						 * Additions need to be made to make sure that they run on Linux and Mac.
						 */ 

						ShadowLauncherUtil.resetConsole();
						
						IPath executableFile; 


						String osName = System.getProperty("os.name").toLowerCase();
						inputArgs = new ArrayList<String>();


						if(osName.contains("windows"))
						{
							executableFile = path.removeFileExtension().addFileExtension("exe");
							inputArgs.add(executableFile.toString());
						}
						else
						{
							executableFile = path.removeFileExtension();
							inputArgs.add("./" + executableFile.toString());
						}		

						results = launchProcesses.runProcess(inputArgs);
						
						System.out.println(results.getStdout());
						System.err.println(results.getStderr());
					}					

					ShadowLauncherUtil.resetConsole();

				}
				catch (IOException | InterruptedException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
	}

	@Override
	public void launch(IEditorPart selection, String mode) {
		// TODO Auto-generated method stub
		System.out.println("Editor Launcher");
	}

}

