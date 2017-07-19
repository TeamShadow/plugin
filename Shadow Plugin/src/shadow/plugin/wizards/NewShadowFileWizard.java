package shadow.plugin.wizards;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

public class NewShadowFileWizard extends Wizard implements INewWizard {

	private WizardNewShadowFileCreationPage pageOne;
	private IStructuredSelection selection;
	
	
	@Override
	public void addPages() {
	    super.addPages();
	 
	    pageOne = new WizardNewShadowFileCreationPage("Shadow Source File Wizard", selection);
	    pageOne.setTitle("Shadow Source File");
	    pageOne.setDescription("Create a new Shadow source file.");
	    pageOne.setFileExtension("shadow");
	    
	    addPage(pageOne);	    
	}

	public NewShadowFileWizard() {
		setWindowTitle("Shadow Source File Wizard");
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;		
	}
	
	@Override
	public boolean canFinish() {
		
		IPath containerPath = pageOne.getContainerFullPath(); 
        if (containerPath == null) 
            return false;
        
        String name = pageOne.getFileName(); 
                        
        if( name == null || name.isEmpty() || name.contains(" ") || name.contains("\t") || name.contains("\r") || name.contains("\n")) {
        
        	pageOne.setErrorMessage("File name must not be empty and must not contain whitespace"); 
        	return false;
        }
        
        if( !Character.isLetter(name.codePointAt(0)) || Character.isLowerCase(name.codePointAt(0)) ) {
        	pageOne.setErrorMessage("File name must begin with an uppercase letter"); 
        	return false;
        }
		
		return true;		
	}

	@Override
	public boolean performFinish() {
		
		IFile file = pageOne.createNewFile();
		if( file == null )
			return false;
		
	
		if( file.exists() ) {		    
		    IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		    try {
				IDE.openEditor(page, file);
			} catch (PartInitException e) {				
			}		 
		}
		
		/*try { 
			
			
            getContainer().run(false, true, new WorkspaceModifyOperation() { 
 
                @Override 
                protected void execute(final IProgressMonitor monitor) {
                	pageOne.createNewFile();                     
                } 
            }); 
        }
		catch (InvocationTargetException | InterruptedException e) { 
            return false;
        }*/
		
        return true;
	}

}
