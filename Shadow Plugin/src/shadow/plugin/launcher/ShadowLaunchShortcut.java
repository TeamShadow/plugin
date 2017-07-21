package shadow.plugin.launcher;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.Launch;

public class ShadowLaunchShortcut extends ShadowCompileShortcut {
	@Override
	public void runCompiler(IPath path, IProject project) {
		if( path != null ) {
			try {
				ILaunchManager mgr = DebugPlugin.getDefault().getLaunchManager();
				ILaunchConfigurationType lct = mgr.getLaunchConfigurationType("shadow.plugin.launcher.launchConfigurationType");
				ILaunchConfigurationWorkingCopy configuration = lct.newInstance(project, "Shadow Launch Shortcut Configuration");				

				configuration.setAttribute(ShadowLaunchConfigurationAttributes.MAIN_FILE, path.makeAbsolute().toString());
				configuration.setAttribute(ShadowLaunchConfigurationAttributes.COMPILE_ONLY, false);
				configuration.setAttribute(ShadowLaunchConfigurationAttributes.COMPILER, "");
				configuration.setAttribute(ShadowLaunchConfigurationAttributes.ARGUMENTS, "");  

				ShadowLaunchConfigurationDelegate launcher = new ShadowLaunchConfigurationDelegate();

				launcher.launch(configuration, ILaunchManager.RUN_MODE, new Launch(configuration, ILaunchManager.RUN_MODE, null), new NullProgressMonitor());
			} catch (CoreException e) {
				e.printStackTrace();
			}

			//new CompileWorker(path, ShadowLaunchConfigurationDelegate.getDefaultCompiler(), "", false, project).execute();
		}
	}
}

