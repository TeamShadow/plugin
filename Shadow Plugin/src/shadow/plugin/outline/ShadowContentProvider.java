package shadow.plugin.outline;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

import shadow.plugin.compiler.ShadowCompilerInterface;

public class ShadowContentProvider
implements ITreeContentProvider
{
	private Object root = null;
	private ITextEditor editor;	

	public ShadowContentProvider(ITextEditor editor) {
		this.editor = editor;
	}
	
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
	{  
		if( newInput != null )	  
			root = ShadowCompilerInterface.buildOutline((FileEditorInput) newInput, editor.getDocumentProvider().getDocument(newInput));
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
