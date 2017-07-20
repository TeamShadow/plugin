package shadow.plugin.wizards;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;

public class NewShadowProjectWizard extends Wizard implements INewWizard {
	
	private WizardNewProjectCreationPage pageOne;
	
	
	@Override
	public void addPages() {
	    super.addPages();
	 
	    pageOne = new WizardNewProjectCreationPage("Shadow Project Wizard");
	    pageOne.setTitle("Shadow Project");
	    pageOne.setDescription("Create a new Shadow project.");
	 
	    addPage(pageOne);	    
	}

	public NewShadowProjectWizard() {
		setWindowTitle("Shadow Project Wizard");
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean performFinish() {
		try { 
            getContainer().run(false, true, new WorkspaceModifyOperation() { 
 
                @Override 
                protected void execute(final IProgressMonitor monitor) { 
                    createProject(monitor != null ? monitor 
                            : new NullProgressMonitor()); 
                } 
            }); 
        }
		catch (InvocationTargetException | InterruptedException e) { 
            return false;
        }
		
        return true;
	}
	
	protected void createProject(final IProgressMonitor monitor) { 
        monitor.beginTask("Creating Shadow project", 30); 
        try { 
            final IWorkspaceRoot root = ResourcesPlugin.getWorkspace() 
                    .getRoot(); 
            monitor.subTask("Creating Shadow directories"); 
            final IProject project = root.getProject(pageOne.getProjectName()); 
            IProjectDescription description = ResourcesPlugin.getWorkspace() 
                    .newProjectDescription(project.getName());
            
            
            if (!Platform.getLocation().equals(pageOne.getLocationPath())) 
                description.setLocation(pageOne.getLocationPath()); 
                        
            project.create(description, monitor);
            monitor.worked(10);
            
            monitor.subTask("Opening project");
            project.open(monitor);
            monitor.worked(10);             
            
            
            monitor.subTask("Updating project nature"); 
            String[] natures = description.getNatureIds();
            String[] newNatures = new String[natures.length + 1];
            System.arraycopy(natures, 0, newNatures, 0, natures.length);
            newNatures[natures.length] = ShadowProjectNature.NATURE_ID;

            // validate the natures
            IWorkspace workspace = ResourcesPlugin.getWorkspace();
            IStatus status = workspace.validateNatureSet(newNatures);

            // only apply new nature, if the status is ok
            if (status.getCode() == IStatus.OK) {                     
	            description.setNatureIds(newNatures);
	            project.setDescription(description, null);
            }
            
            monitor.worked(10);
 
        } catch (final CoreException e) {
        	e.printStackTrace();
           
        } finally { 
            monitor.done(); 
        } 
    } 

}
