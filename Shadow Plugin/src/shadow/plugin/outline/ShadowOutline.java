package shadow.plugin.outline;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;
import shadow.plugin.ShadowPlugin;
import shadow.plugin.compiler.ShadowCompilerInterface;

public class ShadowOutline
  extends ContentOutlinePage
{
  private ITextEditor editor;
  
  public ShadowOutline(ITextEditor textEditor)
  {
    this.editor = textEditor;
    update();
  }
  
  public void createControl(Composite parent)
  {
    super.createControl(parent);
    TreeViewer viewer = getTreeViewer();
    viewer.setContentProvider(new ShadowContentProvider());
    viewer.setLabelProvider(new ShadowLabelProvider());
    viewer.addSelectionChangedListener(this);
    update();
  }
  
  public void update()
  {
    TreeViewer viewer = getTreeViewer();
    if (viewer != null)
    {
      Control control = viewer.getControl();
      if ((control != null) && (!control.isDisposed()))
      {
        control.setRedraw(false);
        viewer.setInput(this.editor.getEditorInput());
        control.setRedraw(true);
      }
    }
  }
  
  public void selectionChanged(SelectionChangedEvent event)
  {
    super.selectionChanged(event);
    try
    {
      ISelection selection = event.getSelection();
      if (!selection.isEmpty())
      {
        Object element = ((IStructuredSelection)selection).getFirstElement();
        int column;
        int line;
  
        if ((element instanceof ShadowOutlineError))
        {
          ShadowOutlineError error = (ShadowOutlineError)element;
          line = error.getLine();
          column = error.getColumn();
        }
        else
        {
          ShadowCompilerInterface compiler = ShadowPlugin.getDefault().getCompilerInterface();
          line = compiler.getLine(element);
          column = compiler.getColumn(element);
        }
        IDocument doc = this.editor.getDocumentProvider().getDocument(this.editor.getEditorInput());
        int offset = doc.getLineOffset(line - 1);
        for (int i = 1; i < column; i++) {
          if (doc.getChar(offset++) == '\t') {
            i += 7;
          }
        }
        this.editor.selectAndReveal(offset, 1);
      }
    }
    catch (BadLocationException localBadLocationException) {}
  }
}
