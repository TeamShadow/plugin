package shadow.plugin.outline;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.part.FileEditorInput;

import shadow.plugin.ShadowPlugin;
import shadow.plugin.compiler.ShadowCompilerInterface.Tree;

public class ShadowContentProvider
  implements ITreeContentProvider
{
  private Object root = null;
  
  public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
  {
	  if( newInput != null )	  
		  root = ShadowPlugin.getDefault().getCompilerInterface().buildOutline((FileEditorInput) newInput);
	  else
		  root = null;
  }
  
  public Object[] getElements(Object input)
  {
	  if( root == null )
		  return null;
	  
	  if( root instanceof Tree )
		  return ((Tree)root).getChildren();		  
	  
	  return new Object[] { root };
  }
  
  public Object getParent(Object element)
  {
	 if( element instanceof Tree )
		 return ((Tree)element).getParent();
	 
	 return element;
  }
  
  public boolean hasChildren(Object element)
  {
	  if( element instanceof Tree )
		  return ((Tree)element).hasChildren();
	  
	  return false;
  }
  
  public Object[] getChildren(Object element)
  {
	if( element instanceof Tree )
		return ((Tree)element).getChildren();
	
	return new Object[] { element };
  }
  
  public void dispose() {}
}
