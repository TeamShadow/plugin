package shadow.plugin.outline;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.graphics.Image;
import org.osgi.framework.Bundle;

import shadow.plugin.ShadowPlugin;
import shadow.plugin.compiler.ShadowCompilerInterface.Tree;

public class ShadowLabelProvider
extends BaseLabelProvider
implements ILabelProvider
{	
	private final Map<ShadowLabel, Image> images = new HashMap<ShadowLabel, Image>();

	public ShadowLabelProvider()
	{				
		Bundle bundle = ShadowPlugin.getDefault().getBundle();

		try
		{			
			for( ShadowLabel label : ShadowLabel.values() )
			{
				URL fileURL = FileLocator.find(bundle, label.path, null);
				URL url = FileLocator.resolve(fileURL);
				ImageDescriptor imageDescriptor = ImageDescriptor.createFromURL(url);
				images.put(label, imageDescriptor.createImage());
			}

		} catch (IOException e) 
		{		    
		}	
	}


	public String getText(Object element)
	{
		if ((element == null) || ((element instanceof String)) || 
				((element instanceof ShadowOutlineError))) {
			return String.valueOf(element);
		}
		return element.toString();
	}

	public Image getImage(Object element)
	{
		if( element instanceof Tree )
		{
			Tree tree = (Tree) element;			
			return images.get(tree.getLabel());			
		}
		else	  
			return images.get(ShadowLabel.ERROR);
	}

	public void dispose()
	{
		super.dispose();
	}
}
