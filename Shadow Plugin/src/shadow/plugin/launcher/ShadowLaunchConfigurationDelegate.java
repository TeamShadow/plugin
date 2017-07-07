package shadow.plugin.launcher;

import java.nio.file.Paths;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

import shadow.plugin.ShadowPlugin;

public class ShadowLaunchConfigurationDelegate implements ILaunchConfigurationDelegate  {
	
	public static String getPathName() {
    	IPath path = getPath();
    	if( path == null )
    		return "";
    	else
    		return path.toString();    	
    }
	
	public static IPath getPath() {
    	IWorkbench wb = PlatformUI.getWorkbench();
		IWorkbenchWindow window = wb.getActiveWorkbenchWindow();

		if(window != null) {
			IWorkbenchPage page = window.getActivePage();
			if(page != null) {
				IEditorPart editorPart = page.getActiveEditor();
				if( editorPart != null ) {
					IEditorInput input = editorPart.getEditorInput();
					return ((FileEditorInput)input).getPath();					
				}
			}
		}
		
		return null;    	
    }

    public static String getDefaultCompiler() {
    	String pathToCompiler = System.getenv("SHADOW_HOME");    	
		String compiler = "shadowc";			
			
		if( pathToCompiler != null && !pathToCompiler.trim().isEmpty())				
			compiler = Paths.get(pathToCompiler, compiler).toString();    	
    	
		return compiler;
    }
    
    public static IProject getProject() {
    	IWorkbench wb = PlatformUI.getWorkbench();
		IWorkbenchWindow window = wb.getActiveWorkbenchWindow();

		if(window != null) {
			IWorkbenchPage page = window.getActivePage();
			if(page != null) {
				IEditorPart editorPart = page.getActiveEditor();
				if( editorPart != null ) {
					IEditorInput input = editorPart.getEditorInput();
					return ((FileEditorInput)input).getFile().getProject();					
				}
			}
		}
		
		return null;
    }
	

	@Override
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
			throws CoreException {
		String mainFile = configuration.getAttribute(ShadowLaunchConfigurationAttributes.MAIN_FILE, getPathName() );
		boolean compileOnly = configuration.getAttribute(ShadowLaunchConfigurationAttributes.COMPILE_ONLY, false );
		String compiler = configuration.getAttribute(ShadowLaunchConfigurationAttributes.COMPILER, getDefaultCompiler() );
		String arguments = configuration.getAttribute(ShadowLaunchConfigurationAttributes.ARGUMENTS, "");

		IPath path = new Path(mainFile);		
		
		new CompileWorker(path, compiler, arguments, compileOnly, getProject()).execute();		
	}
}
