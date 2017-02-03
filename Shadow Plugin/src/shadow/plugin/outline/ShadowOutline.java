package shadow.plugin.outline;

import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
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
				int length = 1;

				if ((element instanceof ShadowOutlineError))
				{
					ShadowOutlineError error = (ShadowOutlineError)element;
					line = error.getLine();
					column = error.getColumn();

					if( !error.hasError() )
					{

						IWorkbench wb = PlatformUI.getWorkbench();
						IWorkbenchWindow window = wb.getActiveWorkbenchWindow();



						PreferenceDialog dialog = 
								PreferencesUtil.createPreferenceDialogOn(window.getShell(), 
										"shadow.plugin.preferences.PreferencePage", null, null);
						if (dialog != null)
							dialog.open();

						/*  
        	PreferencePage page = PreferencePage.getDefault();
        	PreferenceManager manager = new PreferenceManager();
        	IPreferenceNode node = new PreferenceNode("1", page);
        	manager.addToRoot(node);

        	 IWorkbench wb = PlatformUI.getWorkbench();
        	   IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
        	   IWorkbenchPage activePage = win.getActivePage();

        	PreferenceDialog dialog = new PreferenceDialog(new Shell(), manager);
        	dialog.create();
        	dialog.setMessage(page.getTitle());
        	dialog.open();
						 */        	  
					}
				}
				else
				{
					ShadowCompilerInterface compiler = ShadowPlugin.getDefault().getCompilerInterface();
					line = compiler.getLine(element);
					column = compiler.getColumn(element);
					length = compiler.getLength(element);
				}
				IDocument doc = this.editor.getDocumentProvider().getDocument(this.editor.getEditorInput());
				int offset = doc.getLineOffset(line - 1);
				for (int i = 1; i < column; i++) {
					if (doc.getChar(offset++) == '\t') {
						i += 7;
					}
				}
				this.editor.selectAndReveal(offset, length);
			}
		}
		catch (BadLocationException localBadLocationException) {}
	}
}
