package shadow.plugin.launcher;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

import shadow.plugin.ShadowPlugin;

public class ShadowLaunchConfigurationDelegate implements ILaunchConfigurationDelegate  {
	
	public static String getOpenFile() {
    	IWorkbench wb = PlatformUI.getWorkbench();
		IWorkbenchWindow window = wb.getActiveWorkbenchWindow();

		if(window != null) {
			IWorkbenchPage page = window.getActivePage();
			if(page != null) {
				IEditorPart editorPart = page.getActiveEditor();
				IEditorInput input = editorPart.getEditorInput();
				IPath path = ((FileEditorInput)input).getPath();
				return path.toString();
			}
		}
		
		return "";    	
    }	

    public static String getDefaultCompiler() {
    	IPreferenceStore preferenceStore = ShadowPlugin.getDefault()
				.getPreferenceStore();
		return preferenceStore.getString(shadow.plugin.preferences.PreferencePage.COMPILER_PATH);
    }
	

	@Override
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
			throws CoreException {
		String mainFile = configuration.getAttribute(ShadowLaunchConfigurationAttributes.MAIN_FILE, getOpenFile() );
		boolean compileOnly = configuration.getAttribute(ShadowLaunchConfigurationAttributes.COMPILE_ONLY, false );
		String compiler = configuration.getAttribute(ShadowLaunchConfigurationAttributes.COMPILER, getDefaultCompiler() );
		String arguments = configuration.getAttribute(ShadowLaunchConfigurationAttributes.ARGUMENTS, "");

		IPath path = new Path(mainFile);		
		
		new CompileWorker(path, compiler, arguments, compileOnly).execute();		
	}
}
