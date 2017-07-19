package shadow.plugin.commands;


import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import shadow.plugin.editor.ShadowEditor;


public class RemovedUnusedImportsHandler extends AbstractHandler {
    
    @Override
    public Object execute(ExecutionEvent event) 
            throws ExecutionException {
    	ShadowEditor editor = (ShadowEditor)ShadowEditor.getActiveEditor();
        editor.getShadowSourceViewer().removeUnusedImports();
        return null;
    }
}