package shadow.plugin.launcher;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

public class ShadowLaunchShortcut extends ShadowCompileShortcut {
	@Override
	public void runCompiler(IPath path, IProject project) {
		if( path != null )
			new CompileWorker(path, ShadowLaunchConfigurationDelegate.getDefaultCompiler(), "", false, project).execute();
	}
}

