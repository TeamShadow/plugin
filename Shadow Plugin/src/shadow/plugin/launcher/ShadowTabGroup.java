package shadow.plugin.launcher;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;

public class ShadowTabGroup extends AbstractLaunchConfigurationTabGroup {
	 

	@Override
	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
		// TODO Auto-generated method stub
		setTabs(new ILaunchConfigurationTab[] { new ShadowTab() ,new CommonTab()});
		
	}
}
