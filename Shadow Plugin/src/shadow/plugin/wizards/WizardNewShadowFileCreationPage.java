package shadow.plugin.wizards;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;

public class WizardNewShadowFileCreationPage extends WizardNewFileCreationPage {

	public WizardNewShadowFileCreationPage(String pageName, IStructuredSelection selection) {
		super(pageName, selection);		
	}

	@Override
	protected InputStream getInitialContents() {		

		IPath containerPath = getContainerFullPath(); 

		IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember(containerPath); 
		IProject project = resource.getProject(); 
		
		boolean shadowProject = false;
		
		try {
			 shadowProject = project.getNature("shadow.plugin.shadownature") != null;
		} catch (CoreException e) {}


		String newline = System.lineSeparator();		
		String name = this.getFileName();

		if( name == null )
			return null;

		if( name.contains(".") )
			name = name.substring(0, name.indexOf('.'));

		name = name.trim();
		if( name.isEmpty() )
			return null;

		StringBuilder output = new StringBuilder();
		
		
		if( name.endsWith("Exception") )
			output.append("exception ");
		else if( name.startsWith("Can") && name.length() >= 4 && Character.isUpperCase(name.charAt(3)) )
			output.append("interface ");
		else
			output.append("class ");
		
		StringBuilder fullName = new StringBuilder();
		
		if( shadowProject ) {
			boolean done = false;
			int index = containerPath.segmentCount() - 1;
			
			Pattern pattern = Pattern.compile("[a-zA-Z][a-zA-Z0-9]*$");
			
			while( !done && index > 0 ) {
				String text = containerPath.segment(index);
				if( pattern.matcher(text).matches() ) {
					if( fullName.length() > 0  )
						fullName.insert(0, ':');
					fullName.insert(0, text);
				}
				else
					done = true;
				index--;			
			}
		}
		
		if( fullName.length() > 0 )
			output.append(fullName.toString()).append('@').append(newline);		
		output.append(name);
		output.append(newline);
		output.append('{');
		output.append(newline).append(newline);
		output.append('}');
		output.append(newline);

		InputStream stream = new ByteArrayInputStream( output.toString().getBytes( Charset.forName("UTF-8") ) );
		return stream;
	}
}
