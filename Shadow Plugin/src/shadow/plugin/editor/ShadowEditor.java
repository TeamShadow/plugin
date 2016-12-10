package shadow.plugin.editor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.IVerticalRulerColumn;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import shadow.plugin.outline.ShadowOutline;

public class ShadowEditor
  extends TextEditor
{
  private ShadowOutline outline;
  
  protected void initializeEditor()
  {	
    super.initializeEditor();       
    setSourceViewerConfiguration(new ShadowSourceViewerConfiguration());
    
  }   
  
  public void dispose()
  {
    if (this.outline != null) {
      this.outline.update();
    }
    super.dispose();
  }
  
  public void doRevertToSaved()
  {
    super.doRevertToSaved();
    if (this.outline != null) {
      this.outline.update();
    }
  }
  
  public void doSave(IProgressMonitor monitor)
  {
    super.doSave(monitor);
    if (this.outline != null) {
      this.outline.update();
    }
  }
  
  public void doSaveAs()
  {
    super.doSaveAs();
    if (this.outline != null) {
      this.outline.update();
    }
  }
  
  public void doSetInput(IEditorInput input)
    throws CoreException
  {
    super.doSetInput(input);
    if (this.outline != null) {
      this.outline.update();
    }
  }
  
  @SuppressWarnings("rawtypes")
public Object getAdapter(Class adapter)
  {
    if (IContentOutlinePage.class.equals(adapter))
    {
    	/*super.createAnnotationAccess();// change
    	super.*/
    	
      if (this.outline == null) {
        this.outline = new ShadowOutline(this);
      }
      return this.outline;
    }
    return super.getAdapter(adapter);
  }
}
