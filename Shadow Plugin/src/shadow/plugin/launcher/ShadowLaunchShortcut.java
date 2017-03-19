package shadow.plugin.launcher;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

public class ShadowLaunchShortcut implements ILaunchShortcut {	
	
	@Override
	public void launch(ISelection selection, String mode)  {		
		IPath path = null;		
		if( selection instanceof TreeSelection ) {
			TreeSelection treeSelection = (TreeSelection) selection;			
			Object element = treeSelection.getFirstElement();
			
			if( element instanceof IFile ) {
				IFile file = (IFile) element;
				path = file.getLocation();
			}
		}
		
		if( path == null ) {			
			//otherwise, run whatever file is open		
			IWorkbench wb = PlatformUI.getWorkbench();
			IWorkbenchWindow window = wb.getActiveWorkbenchWindow();
	
			if(window != null) {
				IWorkbenchPage page = window.getActivePage();
				if(page != null) {
					IEditorPart editorPart = page.getActiveEditor();
					IEditorInput input = editorPart.getEditorInput();
					path = ((FileEditorInput)input).getPath();
				}
			}
		}
		
		if( path != null )
			new CompileWorker(path, ShadowLaunchConfigurationDelegate.getDefaultCompiler(), "", false).execute();
	}
	
	@Override
	public void launch(IEditorPart selection, String mode) {		
		IEditorInput input = selection.getEditorInput();		
		IPath path = null;
		
		if( input instanceof IFileEditorInput  )		
			path = ((IFileEditorInput)input).getFile().getFullPath();
		else if( input instanceof IPathEditorInput )
			path = ((IPathEditorInput)input).getPath();
		
		if( path != null )
			new CompileWorker(path, ShadowLaunchConfigurationDelegate.getDefaultCompiler(), "", false).execute();											
	}	
}

