package shadow.plugin.outline;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

import shadow.plugin.ShadowPlugin;

public class ShadowContentProvider
implements ITreeContentProvider
{
	private Object root = null;
	private ITextEditor editor;
	private boolean hideFields = false;
	private boolean hideNonPublic = false; 
	private boolean sorted = false;

	public ShadowContentProvider(ITextEditor editor) {
		this.editor = editor;
	}
	
	public boolean isHideFields() {
		return hideFields;
	}
	
	public void setHideFields(boolean value) {
		hideFields = value; 
	}
	
	public boolean isHideNonPublic() {
		return hideNonPublic;
	}
	
	public void setHideNonPublic(boolean value) {
		hideNonPublic = value; 
	}
	
	public boolean isSorted() {
		return sorted;
	}
	
	public void setSorted(boolean value) {
		sorted = value; 
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
	{  
		if( newInput != null )	  
			root = ShadowPlugin.getDefault().getCompilerInterface().buildOutline((FileEditorInput) newInput, editor.getDocumentProvider().getDocument(newInput));
		else
			root = null;
	}

	@Override
	public Object[] getElements(Object input)
	{
		if( root == null )
			return new Object[] { new ShadowOutlineError() };

		if( root instanceof Tree )
			return ((Tree)root).getChildren();		  

		return new Object[] { root };
	}

	@Override
	public Object getParent(Object element)
	{
		if( element instanceof Tree )
			return ((Tree)element).getParent();

		return element;
	}

	@Override
	public boolean hasChildren(Object element)
	{
		if( element instanceof Tree )
			return ((Tree)element).hasChildren();

		return false;
	}

	@Override
	public Object[] getChildren(Object element)
	{
		if( element instanceof Tree )
			return ((Tree)element).getChildren();

		return new Object[] { element };
	}

	@Override
	public void dispose() { }
}
