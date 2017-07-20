package shadow.plugin.compiler;

import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

import shadow.plugin.wizards.ShadowProjectNature;

public class Builder extends IncrementalProjectBuilder {
	
	public static final String BUILDER_ID = "shadow.plugin.builder";

	public Builder() {		
	}

	@Override
	protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor) throws CoreException {
			
		IProject project = getProject();		
		
		if( project.hasNature(ShadowProjectNature.NATURE_ID) ) {
			if( kind == CLEAN_BUILD )
				clean(project, monitor);
		}	
		
		return null;
	}
	
	@Override
	protected void clean(IProgressMonitor monitor) {
		IProject project = getProject();
		
		try {
			if( project.hasNature(ShadowProjectNature.NATURE_ID) )
				clean(project, monitor);
			else
				super.clean(monitor);
		} catch (CoreException e) {			
		}
	}
	
	
	private void clean(IContainer container, IProgressMonitor monitor) {
		
		boolean windows = System.getProperty("os.name").toLowerCase().contains("windows");
		
		try {
			for( IResource resource : container.members() ) {
				if( resource instanceof IContainer )
					clean( (IContainer) resource, monitor );
				else if( resource instanceof IFile ) {					
					IFile file = (IFile) resource;					
					if( file.exists() && !file.getResourceAttributes().isHidden() && !file.getResourceAttributes().isReadOnly() ) {
						IPath path = file.getFullPath();
						String extension = path.getFileExtension();
						
						if( "bc".equalsIgnoreCase(extension) || "meta".equalsIgnoreCase(extension) )
							file.delete(true, monitor);
						else {
							if( windows ) {
								if( "exe".equalsIgnoreCase(extension) )
									file.delete(true, monitor);
							}
							else {
								if( extension == null && file.getResourceAttributes().isExecutable() )
									file.delete(true, monitor);
							}
						}
					}
				}						
			}
			
		} catch (CoreException e) {
			
		}
	}
}
