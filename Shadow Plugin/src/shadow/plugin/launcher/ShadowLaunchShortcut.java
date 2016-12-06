package shadow.plugin.launcher;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.channels.Pipe;
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
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.part.FileEditorInput;

public class ShadowLaunchShortcut implements ILaunchShortcut {

	@Override
	public void launch(ISelection editor, String mode) {
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

				String cmd = "shadowc";
				String pathName = path.toString();
				//String[] inputArgs = { cmd, pathName };				

				ArrayList<String> inputArgs = new ArrayList<String>();
				inputArgs.add(cmd);
				inputArgs.add(pathName);


				//				String otherStuff = path.getDevice();
				//				String sruffy = path.getFileExtension();




				try {
					//Redirect outputConsole = new Redirect();



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

					ShadowLauncher.setConsole(new PrintStream(stream));

					Process compiler = new ProcessBuilder(inputArgs).start();

					//((ProcessBuilder) new Pipe(myConsole.getInputStream(), compiler.getOutputStream())).start();

					if(compiler.waitFor() != 0)
					{

						System.out.println("Success");


					}


				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}






				/*			

				if(path instanceof Path)
				{
					System.out.println("Is a Path.");
				}


				try
				{
					IPreferenceStore preferenceStore = ShadowPlugin.getDefault()
							.getPreferenceStore();
					String pathToJar = preferenceStore.getString("PATH");			

					URL[] urls = { new URL("jar:file:" + pathToJar+"!/") };			

					URLClassLoader loader = URLClassLoader.newInstance(urls);

					Class<?> mainClass = loader.loadClass("shadow.Main");
					Class<?> argumentClass = loader.loadClass("shadow.Arguments");
					Class<?> configurationClass = loader.loadClass("shadow.Configuration");
					Class<?> jobClass = loader.loadClass("shadow.Job");

					Class stringArrayClass = String[].class;
					Class tempStringClass = Class.forName("[Ljava.lang.String;");
					Class argsClass = inputArgs.getClass();
					//Class stringArrayClass = Array.newInstance(stringClass, 0).getClass();

					Constructor argumentConstructor = argumentClass.getConstructor(new Class[] { stringArrayClass } );					
					Object compilerArgs = argumentConstructor.newInstance(new Object[] { inputArgs } );

					Object config = configurationClass.getMethod("buildConfiguration", new Class[] { String.class, String.class, Boolean.TYPE })
										.invoke(null, new Object[] {  argumentClass.getMethod("getMainFileArg", null).invoke(compilerArgs, null),
												argumentClass.getMethod("getConfigFileArg", null).invoke(compilerArgs, null),
												new Boolean(false)} );

					Object currentJob = jobClass.getConstructor(new Class[] { compilerArgs.getClass() })
											.newInstance(new Object[] { compilerArgs });
				 */

				/*	System.out.println("launching shortcut");
				}
				catch (ClassNotFoundException | MalformedURLException ex)
				{
					String problem = ex.getCause().toString();
					System.out.println(problem);
				}
				catch (NoSuchMethodException | SecurityException ex)
				{
					String problem = ex.getCause().toString();
					System.out.println(problem);
				} catch (InstantiationException ex) {
					// TODO Auto-generated catch block					
					ex.printStackTrace();
					String problem = ex.getCause().toString();
					System.out.println(problem);
				} catch (IllegalAccessException ex) {
					// TODO Auto-generated catch block
					ex.printStackTrace();
					String problem = ex.getCause().toString();
					System.out.println(problem);
				} catch (IllegalArgumentException ex) {
					// TODO Auto-generated catch block
					ex.printStackTrace();
					String problem = ex.getCause().toString();
					System.out.println(problem);
				} catch (InvocationTargetException ex) {
					// TODO Auto-generated catch block
					ex.printStackTrace();
					String problem = ex.getCause().toString();
					System.out.println(problem);
				}*/



			}
		}


		System.out.println("launching shortcut2");





	}

	@Override
	public void launch(IEditorPart selection, String mode) {
		// TODO Auto-generated method stub
		System.out.println("Editor Launcher");
	}

}
