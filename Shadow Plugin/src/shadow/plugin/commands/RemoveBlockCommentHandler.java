package shadow.plugin.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;

import shadow.plugin.editor.ShadowEditor;


public class RemoveBlockCommentHandler extends AbstractHandler implements IHandler {
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ShadowEditor editor = (ShadowEditor)ShadowEditor.getActiveEditor();
        editor.getShadowSourceViewer().removeBlockComment();		
		return null;
	}
}
