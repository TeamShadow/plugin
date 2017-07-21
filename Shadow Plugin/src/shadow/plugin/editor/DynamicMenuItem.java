package shadow.plugin.editor;

import java.util.Collections;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;

public class DynamicMenuItem extends CommandContributionItem {

	private boolean enabled;

	public DynamicMenuItem(String id, String label, boolean enabled) {
		super(new CommandContributionItemParameter(
				PlatformUI.getWorkbench().getActiveWorkbenchWindow(), 
				id + ".cci", id, Collections.emptyMap(), null, null, null, 
				label, null, null, CommandContributionItem.STYLE_PUSH, null, 
				false));
		this.enabled = enabled;
	}

	public DynamicMenuItem(String id, String label, boolean enabled, 
			ImageDescriptor image) {
		super(new CommandContributionItemParameter(
				PlatformUI.getWorkbench().getActiveWorkbenchWindow(), 
				id + ".cci", id, Collections.emptyMap(), image, null, null, 
				label, null, null, CommandContributionItem.STYLE_PUSH, null, 
				false));
		this.enabled = enabled;
	}

	@Override
	public boolean isEnabled() { 
		return super.isEnabled() && enabled; 
	}

	static boolean isContextMenu(IContributionManager parent) {
		if( parent instanceof IContributionItem ) { 
			String ID = ((IContributionItem) parent).getId();
			return ID != null && ID.equals("#TextEditorContext");
		}
		return false;        
	}

}