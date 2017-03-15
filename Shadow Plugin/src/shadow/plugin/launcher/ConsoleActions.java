package shadow.plugin.launcher;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsolePageParticipant;
import org.eclipse.ui.part.IPageBookViewPage;
import org.eclipse.ui.part.IPageSite;

public class ConsoleActions implements IConsolePageParticipant {

    private IPageBookViewPage page;
    //private Action remove, removeAll, terminate;
    private IActionBars bars;
    //private IConsole console;

    @Override
    public void init(final IPageBookViewPage page, final IConsole consoleInterface) {
    	
    	this.page = page;
        IPageSite site = page.getSite();
        this.bars = site.getActionBars();
    	
    	if( consoleInterface instanceof Console ) {
    		Console console = (Console) consoleInterface;
	                
	
	        Action terminate = console.getTerminate();        
	        Action remove = console.getRemove();
	        Action removeAll = console.getRemoveAll();
	
	        bars.getMenuManager().add(new Separator());
	        bars.getMenuManager().add(terminate);
	        bars.getMenuManager().add(remove);
	        bars.getMenuManager().add(removeAll);
	
	        IToolBarManager toolbarManager = bars.getToolBarManager();
	        
	        if( console instanceof Console ) {
	        	((Console)console).setActions(remove, removeAll, terminate);
	        }
	
	        toolbarManager.appendToGroup(IConsoleConstants.LAUNCH_GROUP, terminate);
	        toolbarManager.appendToGroup(IConsoleConstants.LAUNCH_GROUP,remove);
	        toolbarManager.appendToGroup(IConsoleConstants.LAUNCH_GROUP,removeAll);
    	}
    	
    	bars.updateActionBars();
    }


    @Override
    public void dispose() {
        //remove= null;
        //terminate = null;
        bars = null;
        page = null;
    }
    
	@Override
    public <T> T getAdapter(Class<T> adapter) {
        return null;
    }

    @Override
    public void activated() {
        updateVis();
    }

    @Override
    public void deactivated() {
        updateVis();
    }

    private void updateVis() {

        if (page == null)
            return;
        //boolean isEnabled = true;
        //terminate.setEnabled(isEnabled);
        //remove.setEnabled(isEnabled);
        bars.updateActionBars();
    }

}