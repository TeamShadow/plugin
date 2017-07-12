package shadow.plugin.outline;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;

public class HideFieldsAction  extends Action {
	private final TreeViewer viewer;	

	public HideFieldsAction(TreeViewer viewer) {
		super("Hide Fields", Action.AS_CHECK_BOX);
		setImageDescriptor(ShadowOutline.getImage("fields_co.gif"));
		this.viewer = viewer;
	}

	@Override
	public void run() {
        Display display = 
                viewer.getControl()
                    .getDisplay();
        BusyIndicator.showWhile(display, 
                new Runnable() {
            @Override
            public void run() {
                if (isChecked()) {
                	viewer.addFilter(FILTER);
                }
                else {
                	viewer.removeFilter(FILTER);
                }
            }
        });
    }
	
	 private static final ViewerFilter FILTER = 
	            new ViewerFilter() {
	        @Override
	        public boolean select(Viewer viewer, 
	                Object parentElement, Object element) {
	             if( element instanceof Tree )
	            	 return !((Tree)element).isField();	             
	            
	            return true;
	        }
	    };
	
}