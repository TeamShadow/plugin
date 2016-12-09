package shadow.plugin.launcher;

import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.jface.preference.IPreferenceStore;
//import org.eclipse.pde.internal.launching.launcher;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.part.FileEditorInput;

import shadow.plugin.ShadowPlugin;


public class ShadowLauncher extends LaunchConfigurationDelegate {

	@Override
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
			throws CoreException {
		// TODO Auto-generated method stub


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

				if( path instanceof Path)
					System.out.println("It is a path");


				try
				{
					IPreferenceStore preferenceStore = ShadowPlugin.getDefault()
							.getPreferenceStore();
					String pathToJar = preferenceStore.getString("PATH");			

					URL[] urls = { new URL("jar:file:" + pathToJar+"!/") };			

					URLClassLoader loader = URLClassLoader.newInstance(urls);

					Class<?> mainClass = loader.loadClass("shadow.Main");
					Class<?> argumentClass = loader.loadClass("shadow.Arguments");

					mainClass.getMethod("main", new Class[] {String.class} );


					System.out.println("launching");
				}
				catch (ClassNotFoundException | MalformedURLException ex)
				{

				}
				catch (NoSuchMethodException | SecurityException ex)
				{
					String problem = ex.getCause().toString();
				}

			}
		}





		//ExternalToolsUtil.getLocation(configuration);
		ILaunchConfigurationWorkingCopy stuffyStuff = configuration.getWorkingCopy();



		//File firstFile = LaunchArgumentsHelper.getWorkingDirectory(configuration);

		IFile file = configuration.getFile();
		String srcLocatorID = configuration.ATTR_SOURCE_LOCATOR_ID;
		String srcLocatorMemento = configuration.ATTR_SOURCE_LOCATOR_MEMENTO;
		String configurationFileExtension = configuration.LAUNCH_CONFIGURATION_FILE_EXTENSION;


		String configurationFileLocation = configuration.getAttribute(ShadowLaunchConfigurationAttributes.CONFIG_FILE_LOCATION, "");


		//
		/*
		this.preLaunchCheck(configuration, mode, monitor)
		 */


		if(!configurationFileLocation.isEmpty())
		{



		}

		System.out.println(configurationFileLocation);

		ISourceLocator thing = launch.getSourceLocator();

		String thing2 = launch.getLaunchMode();

		IProcess[] thing3 = launch.getProcesses();

		System.out.println("launching2");
	}

	


}
