package shadow.plugin.launcher;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.part.FileEditorInput;

public class ShadowCompileShortcut implements ILaunchShortcut {

	@Override
	public void launch(ISelection editor, String mode)  {
		
		IPath path = null;
		IProject project = null;
		
		if( editor instanceof TreeSelection ) {
			TreeSelection selection = (TreeSelection) editor;			
			Object element = selection.getFirstElement();
			
			if( element instanceof IFile ) {
				IFile file = (IFile) element;
				path = file.getLocation();
				project = file.getProject();				
			}
		}
		
		if( path == null ) {			
			path = ShadowLaunchConfigurationDelegate.getPath();
			project = ShadowLaunchConfigurationDelegate.getProject();
		}
		
		runCompiler(path, project);		
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
			new CompileWorker(path, ShadowLaunchConfigurationDelegate.getDefaultCompiler(), "", true, project).execute();
	}
}
