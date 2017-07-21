package shadow.plugin.launcher;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.part.FileEditorInput;

public class ShadowLaunchPropertyTester extends PropertyTester {
	
	private static final String PROPERTY_CAN_LAUNCH_AS_SHADOW= "canLaunchAsShadow"; //$NON-NLS-1$
	
	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (!(receiver instanceof IAdaptable)) {
			throw new IllegalArgumentException("Element must be of type 'IAdaptable', is " + (receiver == null ? "null" : receiver.getClass().getName())); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		IAdaptable element = (IAdaptable) receiver;

		if (PROPERTY_CAN_LAUNCH_AS_SHADOW.equals(property)) {
			return canLaunchAsShadow(element);
		}
		throw new IllegalArgumentException("Unknown test property '" + property + "'"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private boolean canLaunchAsShadow(IAdaptable element) {
		try{
			
			if( element instanceof FileEditorInput ) {
				FileEditorInput fileInput = (FileEditorInput) element;			
				return fileInput.getPath().getFileExtension().toLowerCase().equals("shadow");
			}
			else if( element instanceof IFile ) {
				IFile file = (IFile) element;
				return file.getFullPath().getFileExtension().toLowerCase().equals("shadow");
			}
			//covers projects and folders
			else if( element instanceof IContainer ) {
				IContainer container = (IContainer) element;
				IResource[] resources = container.members();
				if( resources.length == 1 )
					return canLaunchAsShadow( resources[0] );
			}
		}
		catch(CoreException e) {}
		
		return false;		
	}

}
