package shadow.plugin.outline;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;

public class SortingAction  extends Action {
	private final TreeViewer viewer;
    
    public static ViewerComparator ALPHABETIC_COMPARATOR = 
            new ViewerComparator() {
        @Override
        public int compare(Viewer viewer, Object e1, Object e2) {
            Tree t1 = (Tree) e1;
            Tree t2 = (Tree) e2;
            return t1.compareTo(t2);
        }
    };	

	public SortingAction(TreeViewer viewer) {
		super("Sort", Action.AS_CHECK_BOX);
		setImageDescriptor(ShadowOutline.getImage("alphab_sort_co.gif"));
		this.viewer = viewer;
	}

	@Override
	public void run() {
        Display display = 
                viewer.getControl()
                    .getDisplay();
        final ViewerComparator comparator;
        if (isChecked()) {
            comparator = ALPHABETIC_COMPARATOR;
        }
        else {
            comparator = null;
        }
        BusyIndicator.showWhile(display, 
                new Runnable() {
            @Override
            public void run() {
            	viewer.setComparator(comparator);
            }
        });
	}
}