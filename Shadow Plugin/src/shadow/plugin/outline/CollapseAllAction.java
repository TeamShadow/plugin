package shadow.plugin.outline;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TreeViewer;

public class CollapseAllAction  extends Action {
	private final TreeViewer viewer;

	public CollapseAllAction(TreeViewer viewer) {
		
		super("Collapse All", ShadowOutline.getImage("collapseall.gif"));
		//setToolTipText(ActionMessages.CollapsAllAction_tooltip);
		//setDescription(ActionMessages.CollapsAllAction_description);
		this.viewer = viewer;
	}

	@Override
	public void run() {
		try {
			viewer.getControl().setRedraw(false);
			viewer.collapseAll();			
		} finally {
			viewer.getControl().setRedraw(true);
		}
	}

}