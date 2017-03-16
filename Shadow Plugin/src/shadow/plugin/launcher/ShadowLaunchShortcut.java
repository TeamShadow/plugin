package shadow.plugin.launcher;

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

public class ShadowLaunchShortcut implements ILaunchShortcut {
	
	@Override
	public void launch(ISelection editor, String mode)  {
		IWorkbench wb = PlatformUI.getWorkbench();
		IWorkbenchWindow window = wb.getActiveWorkbenchWindow();

		if(window != null) {
			IWorkbenchPage page = window.getActivePage();
			if(page != null) {
				IEditorPart editorPart = page.getActiveEditor();
				IEditorInput input = editorPart.getEditorInput();
				IPath path = ((FileEditorInput)input).getPath();

				new CompileWorker(path, true).execute();								
			}
		}
	}
	
	@Override
	public void launch(IEditorPart selection, String mode) {
		// TODO Auto-generated method stub
		System.out.println("Editor Launcher");
	}	
}

