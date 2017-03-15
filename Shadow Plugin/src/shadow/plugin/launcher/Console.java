package shadow.plugin.launcher;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IOConsole;

public class Console extends IOConsole {
	
	private volatile Action remove;
	private volatile Action removeAll;
	private volatile Action terminate;
	private volatile boolean terminated = false;
	private volatile Process process;
	
	public Action getRemove() {
		return remove;
	}
	
	public Action getRemoveAll() {
		return removeAll;
	}
	
	public Action getTerminate() {
		return terminate;
	}	

    private Action createTerminateButton() {
        ImageDescriptor imageDescriptor = ImageDescriptor.createFromFile(getClass(), "/icons/cancel.gif");
        
        Action action = new Action("Terminate", imageDescriptor) {
            public void run() {            	
            	terminate();
            }
        };
        
        imageDescriptor = ImageDescriptor.createFromFile(getClass(), "/icons/disabled_cancel.gif");
        action.setDisabledImageDescriptor(imageDescriptor);
        action.setEnabled(false);
        
        return action;
    }
    
    private Action createRemoveAllButton() {
        ImageDescriptor imageDescriptor = ImageDescriptor.createFromFile(getClass(), "/icons/remove_all.gif");
        Action action = new Action("Remove All Terminated Launches", imageDescriptor) {
            public void run() {
            	removeAll();
            }
        };
        imageDescriptor = ImageDescriptor.createFromFile(getClass(), "/icons/disabled_remove_all.gif");
        action.setDisabledImageDescriptor(imageDescriptor);
        action.setEnabled(false);
        
        return action;
    }

    private Action createRemoveButton() {
        ImageDescriptor imageDescriptor = ImageDescriptor.createFromFile(getClass(), "/icons/remove.gif");
        Action action = new Action("Remove Launch", imageDescriptor) {
            public void run() {
            	remove();
            }
        };
        
        imageDescriptor = ImageDescriptor.createFromFile(getClass(), "/icons/disabled_remove.gif");
        action.setDisabledImageDescriptor(imageDescriptor);
        action.setEnabled(false);
        
        return action;
    }
	
	public synchronized boolean isTerminated() {
		return terminated;
	}
	
	public synchronized void markTerminated() {
		terminated = true;		
		terminate.setEnabled(false);
		remove.setEnabled(true);
		
		for( IConsole console :  ConsolePlugin.getDefault().getConsoleManager().getConsoles() )
			if( console instanceof Console )
				((Console) console).removeAll.setEnabled(true);
	}
	
	public synchronized void terminate() {
		if( !terminated ) {
			if( process != null ) {				
				process.destroyForcibly();
				process = null;
			}
			
			markTerminated();
		}
	}
	
	public synchronized void remove() {
		if( terminated ) {
			ConsolePlugin.getDefault().getConsoleManager().removeConsoles(new IConsole[] { this } );
			this.destroy();
		}
	}
	
	public synchronized void removeAll() {		
		for( IConsole console :  ConsolePlugin.getDefault().getConsoleManager().getConsoles() )
			if( console instanceof Console ) {
				Console temp = (Console)console;
				if( temp.isTerminated() )
					temp.remove();
				else
					temp.removeAll.setEnabled(false);
			}
	}
	
	public synchronized void setProcess( Process process ) {		
		this.process = process;
		terminate.setEnabled(true);
	}
	
	public synchronized void setActions(Action remove, Action removeAll, Action terminate) {
		this.remove = remove;
		this.removeAll = removeAll;
		this.terminate = terminate;
	}

	public Console(String name) {
		super(name, null, null, true);
		remove = createRemoveButton();
		removeAll = createRemoveAllButton();
		terminate = createTerminateButton();
	}
	
	@Override
	public void dispose() {
		super.dispose();

		if( process != null ) {
			process.destroy();
			process = null;
		}
	}
}
