package shadow.plugin.outline;

import java.io.File;
import java.io.FileInputStream;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IPathEditorInput;

import shadow.plugin.ShadowPlugin;
import shadow.plugin.compiler.ShadowCompilerInterface;
import shadow.plugin.compiler.ShadowCompilerInterface.Tree;

public class ShadowContentProvider
  implements ITreeContentProvider
{
  private Tree root;
  private ShadowCompilerInterface compiler;
  
  public ShadowContentProvider()
  {
    this.compiler = ShadowPlugin.getDefault().getCompilerInterface();    
  }
  
  public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
  {
    this.root = null;
    try
    {
      File file = ((IPathEditorInput)newInput).getPath().toFile();
      this.root = this.compiler.compile(new FileInputStream(file));
    }
    catch (Exception localException) {}   
  }
  
  public Object[] getElements(Object input)
  {
	  if( root == null )	  
		  return new Object[] { new ShadowOutlineError(this.compiler.getErrorLine(), this.compiler.getErrorColumn(), this.compiler.getMessage()) };
	  else
		  return root.getChildren();
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
	
	return new Object[] { new ShadowOutlineError(this.compiler.getErrorLine(), this.compiler.getErrorColumn(), this.compiler.getMessage()) };
  }
  
  public void dispose() {}
}
