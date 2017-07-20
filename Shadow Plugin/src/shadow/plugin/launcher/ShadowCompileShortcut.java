package shadow.plugin.launcher;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.part.FileEditorInput;

public class ShadowCompileShortcut implements ILaunchShortcut {

	@Override
	public void launch(ISelection selection, String mode)  {
		
		if( selection instanceof TreeSelection ) {
			TreeSelection tree = (TreeSelection) selection;			
			Object[] objects = tree.toArray();
			compile(objects);
		}
		else {	
			IPath path = ShadowLaunchConfigurationDelegate.getPath();
			IProject project = ShadowLaunchConfigurationDelegate.getProject();
			runCompiler(path, project);
		}		
	}	
	
	
	private void compile(Object[] objects) {
		for( Object object : objects ) {				
			if( object instanceof IFile ) {
				IFile file = (IFile) object;
				if( "shadow".equalsIgnoreCase(file.getFullPath().getFileExtension()) ) {						
					IPath path = file.getLocation();
					IProject project = file.getProject();
					runCompiler(path, project);	
				}
			}
			else if( object instanceof IContainer ) {
				IContainer container = (IContainer) object;
				IResource[] resources;
				try {
					resources = container.members();
					compile(resources);
				} catch (CoreException e) {}				
			}
		}
	}	
	
	@Override
	public void launch(IEditorPart editorPart, String mode) {				
		IEditorInput input = editorPart.getEditorInput();
		FileEditorInput fileInput = ((FileEditorInput)input);
		IProject project = fileInput.getFile().getProject();
		IPath path = fileInput.getPath();

		runCompiler(path, project);								
	}
	
	public void runCompiler(IPath path, IProject project) {
		if( path != null )
			new CompileWorker(path.makeAbsolute(), ShadowLaunchConfigurationDelegate.getDefaultCompiler(), "", true, project).execute();
	}
}
