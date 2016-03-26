package shadow.plugin.outline;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.graphics.Image;
import org.osgi.framework.Bundle;

import shadow.plugin.ShadowPlugin;
import shadow.plugin.compiler.ShadowCompilerInterface.Tree;
import shadow.plugin.compiler.ShadowCompilerInterface.Tree.Kind;

public class ShadowLabelProvider
extends BaseLabelProvider
implements ILabelProvider
{	

	enum ImageType
	{
		CLASS("class_obj.gif"),
		CONSTANT("field_public_obj.gif"),
		ENUM("enum_obj.gif"),
		EXCEPTION("class_obj.gif"),
		ERROR("error_obj.gif"),
		FIELD("field_private_obj.gif"),
		INTERFACE("int_obj.gif"),
		PACKAGE("package_obj.gif"),
		PRIVATE_METHOD("methpri_obj.gif"),
		PROTECTED_METHOD("methpro_obj.gif"),
		PUBLIC_METHOD("methpub_obj.gif"),
		SINGLETON("class_obj.gif");

		public Path path;

		private ImageType(String path)
		{
			this.path = new Path("/icons/" + path);
		}
	}

	private final Map<ImageType, Image> images = new HashMap<ImageType, Image>();

	public ShadowLabelProvider()
	{				
		Bundle bundle = ShadowPlugin.getDefault().getBundle();

		try
		{			
			for( ImageType type : ImageType.values() )
			{
				URL fileURL = FileLocator.find(bundle, type.path, null);
				URL url = FileLocator.resolve(fileURL);
				ImageDescriptor imageDescriptor = ImageDescriptor.createFromURL(url);
				images.put(type, imageDescriptor.createImage());
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
			switch( tree.getKind() )
			{
			case CLASS: return images.get(ImageType.CLASS);			
			case COMPILATION_UNIT: return null;
			case CONSTANT: return images.get(ImageType.CONSTANT);			
			case ENUM: return images.get(ImageType.ENUM);			
			case EXCEPTION: return images.get(ImageType.EXCEPTION);			
			case FIELD: return images.get(ImageType.FIELD);			
			case INTERFACE: return images.get(ImageType.INTERFACE);			
			case METHOD: return images.get(ImageType.PUBLIC_METHOD);			
			case SINGLETON: return images.get(ImageType.SINGLETON);			
			default: return null;
			}
		}
		else	  
			return images.get(ImageType.ERROR);
	}

	public void dispose()
	{
		super.dispose();
	}
}
