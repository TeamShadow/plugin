package shadow.plugin.wizards;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
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
		return true;
	}

}
