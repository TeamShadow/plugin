package shadow.plugin.launcher;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate2;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.part.FileEditorInput;

import shadow.plugin.editor.ShadowEditor;
import shadow.plugin.wizards.ShadowProjectNature;

public class ShadowLaunchConfigurationDelegate extends LaunchConfigurationDelegate implements ILaunchConfigurationDelegate2  {

	public static String getPathName() {
		IPath path = getPath();
		if( path == null )
			return "";
		else
			return path.toString();    	
	}

	public static IPath getPath() {
		IEditorPart editorPart = ShadowEditor.getActiveEditor();
		if( editorPart != null ) {
			IEditorInput input = editorPart.getEditorInput();
			return ((FileEditorInput)input).getPath();					
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
		IEditorPart editorPart = ShadowEditor.getActiveEditor();
		if( editorPart != null ) {
			IEditorInput input = editorPart.getEditorInput();
			return ((FileEditorInput)input).getFile().getProject();					
		}

		return null;
	}    

	@Override
	protected IProject[] getBuildOrder(ILaunchConfiguration configuration, String mode) {
			
		IFile file = configuration.getFile();
		if( file != null && file.getProject() != null )
			return new IProject[] { file.getProject() };
		
		//No known, project, get all open Shadow projects
		List<IProject> projects = new ArrayList<IProject>();
		try {
			for( IProject project :  ResourcesPlugin.getWorkspace().getRoot().getProjects() )			
				if( project.isOpen() && project.hasNature(ShadowProjectNature.NATURE_ID) )
					projects.add(project);
		} 
		catch (CoreException e) {
			e.printStackTrace();
		}

		if( projects.isEmpty() )
			return null;
		else {
			IProject[] array = new IProject[projects.size()];
			return projects.toArray(array);
		}    
	}
	
	protected IProject[] getProjectsForProblemSearch(ILaunchConfiguration configuration, String mode) throws CoreException {
		return getBuildOrder(configuration, mode);
	}

	@Override
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
			throws CoreException {		

		if( saveBeforeLaunch(configuration, mode, monitor) && finalLaunchCheck(configuration, mode, monitor) ) {
			String mainFile = configuration.getAttribute(ShadowLaunchConfigurationAttributes.MAIN_FILE, getPathName() );
			boolean compileOnly = configuration.getAttribute(ShadowLaunchConfigurationAttributes.COMPILE_ONLY, false );
			String compiler = configuration.getAttribute(ShadowLaunchConfigurationAttributes.COMPILER, getDefaultCompiler() );
			if( compiler.trim().isEmpty() )
				compiler = getDefaultCompiler();
			String arguments = configuration.getAttribute(ShadowLaunchConfigurationAttributes.ARGUMENTS, "");

			IPath path = new Path(mainFile);		


			new CompileWorker(path, compiler, arguments, compileOnly, getProject()).execute();
		}
	}
}
