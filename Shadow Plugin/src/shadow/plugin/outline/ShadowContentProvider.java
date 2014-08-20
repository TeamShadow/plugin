package shadow.plugin.outline;

import java.io.File;
import java.io.FileInputStream;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IPathEditorInput;
import shadow.plugin.ShadowPlugin;
import shadow.plugin.compiler.ShadowCompilerInterface;

public class ShadowContentProvider
  implements ITreeContentProvider
{
  private Object root;
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
    if (this.root == null) {
      this.root = new ShadowOutlineError(this.compiler.getErrorLine(), this.compiler.getErrorColumn());
    }
  }
  
  public Object[] getElements(Object input)
  {
    return new Object[] { this.root };
  }
  
  public Object getParent(Object element)
  {
    return this.compiler.getParent(element);
  }
  
  public boolean hasChildren(Object element)
  {
    return this.compiler.hasChildren(element);
  }
  
  public Object[] getChildren(Object element)
  {
    return this.compiler.getChildren(element);
  }
  
  public void dispose() {}
}
