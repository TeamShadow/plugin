package shadow.plugin;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class ShadowPerspectiveFactory implements IPerspectiveFactory {
	
	public static final String ID = "shadow.plugin.perspective";

	@Override
	public void createInitialLayout(IPageLayout layout) {
		layout.addNewWizardShortcut("shadow.plugin.newProjectWizard");
        layout.addNewWizardShortcut("shadow.plugin.newFileWizard");
	}

}
