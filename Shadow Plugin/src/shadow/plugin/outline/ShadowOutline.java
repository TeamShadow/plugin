package shadow.plugin.outline;

import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.SubToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;
import org.osgi.framework.Bundle;

import shadow.plugin.ShadowPlugin;

public class ShadowOutline
extends ContentOutlinePage
{
	private ITextEditor editor;

	public ShadowOutline(ITextEditor textEditor)
	{
		this.editor = textEditor;
		update();		
	}
	
	@Override
    public void makeContributions(IMenuManager menuManager,
            IToolBarManager toolBarManager, 
            IStatusLineManager statusLineManager) {
        super.makeContributions(menuManager, toolBarManager, 
                statusLineManager);
        /* For context menu for outline. Nice, but not needed now.
        MenuManager contextMenu = new MenuManager();
        //JavaPlugin.createStandardGroups(contextMenu);        
               
        Control control = treeViewer.getControl();
        control.setMenu(contextMenu.createContextMenu(control));
        */       
        
        TreeViewer treeViewer = getTreeViewer();
        
        toolBarManager.add(new CollapseAllAction(treeViewer));
        toolBarManager.add(new SortingAction(treeViewer));
        toolBarManager.add(new HideFieldsAction(treeViewer));
        toolBarManager.add(new HideNonPublicAction(treeViewer));
    }

	public void createControl(Composite parent)
	{
		super.createControl(parent);
		TreeViewer viewer = getTreeViewer();		
		viewer.setContentProvider(new ShadowContentProvider(editor));
		viewer.setLabelProvider(new ShadowLabelProvider());		
		viewer.addSelectionChangedListener(this);
		viewer.setAutoExpandLevel(TreeViewer.ALL_LEVELS);
		update();
	}
	
	public static ImageDescriptor getImage(String name) {
		Bundle bundle = ShadowPlugin.getDefault().getBundle();
		Path path = new Path("/icons/" + name);
		
		URL fileURL = FileLocator.find(bundle, path, null);
		URL url;
		try {
			url = FileLocator.resolve(fileURL);
			return ImageDescriptor.createFromURL(url);
		} catch (IOException e) {
			return null;
		}		
	}	

	public void update()
	{
		TreeViewer viewer = getTreeViewer();
		if (viewer != null) {
			Control control = viewer.getControl();
			if ((control != null) && (!control.isDisposed())) {
				control.setRedraw(false);
				
				IEditorInput input = editor.getEditorInput();
				viewer.setInput(input);
				control.setRedraw(true);
			}
		}
	}

	public void selectionChanged(SelectionChangedEvent event) {
		super.selectionChanged(event);
		try {
			ISelection selection = event.getSelection();
			if (!selection.isEmpty()) {
				Object element = ((IStructuredSelection)selection).getFirstElement();
				int column = 0;
				int line = 1;
				int length = 1;

				if ((element instanceof ShadowOutlineError)) {
					ShadowOutlineError error = (ShadowOutlineError)element;
					line = error.getLine();
					column = error.getColumn();
				}
				else if( element instanceof Tree ) {
					Tree tree = (Tree) element;
					line = tree.lineStart();
					column = tree.columnStart();
					length = tree.getLength();
				}
				IDocument doc = this.editor.getDocumentProvider().getDocument(this.editor.getEditorInput());
				int offset = doc.getLineOffset(line - 1) + column;
				this.editor.selectAndReveal(offset, length);
			}
		}
		catch (BadLocationException localBadLocationException) {}
	}
}
