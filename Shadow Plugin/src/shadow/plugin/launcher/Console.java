package shadow.plugin.launcher;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleInputStream;
import org.eclipse.ui.console.IOConsoleOutputStream;
import org.eclipse.ui.internal.console.IOConsolePage;
import org.eclipse.ui.part.IPageBookViewPage;

@SuppressWarnings("restriction")
public class Console extends IOConsole {
	
	private Action remove;
	private Action removeAll;
	private Action terminate;
	
	private IOConsoleOutputStream output;
	private IOConsoleOutputStream error;
	private IOConsoleInputStream input;	
	
	private boolean terminated = false;
	private Process process;
	private boolean acceptsInput;
	
	// Should only be run on Event Dispatch Thread
	public static Console getConsole(String name) {
		return getConsole(name, true);
	}	
	
	// Should only be run on Event Dispatch Thread
	public static Console getConsole(String name, boolean acceptsInput) {
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager conMan = plugin.getConsoleManager();
		IConsole[] existing = conMan.getConsoles();
		Console console = null;
		
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(" (MMM d, yyyy, h:mm:ss a)");
		LocalDateTime dateTime = LocalDateTime.now();
				
		for( int i = 0; i < existing.length && console == null; i++ )
			if (existing[i] instanceof Console && existing[i].getName().contains(name) ) {
				console = (Console) existing[i];
				console.reset(name + dateTime.format(formatter));				
			}

		//no console found, so create a new one
		if( console == null )			
			console = new Console(name + dateTime.format(formatter), acceptsInput);
		
		console.activate();
		conMan.addConsoles(new IConsole[]{console});
		return console;
	}
	
	@Override
	public IPageBookViewPage createPage(IConsoleView view) {
		IOConsolePage page = (IOConsolePage)super.createPage(view);				
		if( !acceptsInput )
			page.setReadOnly();
		
		return page;
	}
	
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
	
	public boolean isTerminated() {
		return terminated;
	}
	
	public void markTerminated() {
		terminated = true;		
		terminate.setEnabled(false);
		remove.setEnabled(true);		

		Display.getDefault().asyncExec(				
				new Runnable() {
					@Override
					public void run() {
						setName("<terminated> " + getName());						
					}					
				}				
	    );
		
		
		for( IConsole console :  ConsolePlugin.getDefault().getConsoleManager().getConsoles() )
			if( console instanceof Console )
				((Console) console).removeAll.setEnabled(true);
	}
	
	public void terminate() {
		if( !terminated ) {
			if( process != null ) {				
				process.destroyForcibly();
				process = null;
			}
			
			markTerminated();
		}
	}
	
	public void remove() {
		if( terminated ) {			
			int totalTerminated = 0;
			
			for( IConsole console :  ConsolePlugin.getDefault().getConsoleManager().getConsoles() )
				if( console instanceof Console && ((Console) console).isTerminated() )
					totalTerminated++; 
			
			ConsolePlugin.getDefault().getConsoleManager().removeConsoles(new IConsole[] { this } );
			this.destroy();
			
			//only the current console was terminated so the other removeAll buttons need to be disabled
			if( totalTerminated == 1 )
				for( IConsole console :  ConsolePlugin.getDefault().getConsoleManager().getConsoles() )
					if( console instanceof Console )
						((Console) console).removeAll.setEnabled(false);
		}
	}
	
	public void removeAll() {		
		for( IConsole console :  ConsolePlugin.getDefault().getConsoleManager().getConsoles() )
			if( console instanceof Console ) {
				Console temp = (Console)console;
				if( temp.isTerminated() )
					temp.remove();
				else
					temp.removeAll.setEnabled(false);
			}
	}
	
	public void setProcess( Process process ) {		
		this.process = process;
		terminate.setEnabled(true);
	}
	
	public void setActions(Action remove, Action removeAll, Action terminate) {
		this.remove = remove;
		this.removeAll = removeAll;
		this.terminate = terminate;
	}

	public Console(String name, boolean acceptsInput) {
		super(name, null, null, true);
		remove = createRemoveButton();
		removeAll = createRemoveAllButton();
		terminate = createTerminateButton();
		
		output = newOutputStream();
		error = newOutputStream();
		error.setColor(new Color(null, 255, 0, 0));
		
		if( acceptsInput ) {
			input = getInputStream();
			input.setColor(new Color(null, 0, 255, 0));
		}
		
		this.acceptsInput = acceptsInput;
	}
	
	public IOConsoleOutputStream getOutput() {
		return output;
	}
	
	public IOConsoleOutputStream getError() {
		return error;
	}
	
	public IOConsoleInputStream getInput() {
		return input;
	}
	
	@Override
	public void dispose() {
		super.dispose();

		if( process != null ) {
			process.destroy();
			process = null;
		}
	}

	public void reset(String name) {		
		Display.getDefault().asyncExec(				
				new Runnable() {
					@Override
					public void run() {
						clearConsole();
						setName(name);
						terminated = false;						
					}					
				}				
	    );
	}
}
