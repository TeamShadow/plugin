package shadow.plugin.editor;


import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;

import shadow.plugin.ShadowPlugin;


public class SourceMenuItems extends CompoundContributionItem {    
    
    public SourceMenuItems() {}
    
    public SourceMenuItems(String id) {
        super(id);
    }
    
    @Override
    public IContributionItem[] getContributionItems() {
        IContributionItem[] items = getItems(ShadowEditor.getActiveEditor());    
        return items;
        /*
        
        if (DynamicMenuItem.isContextMenu(getParent())) {
            MenuManager submenu = new MenuManager("Source");
            submenu.setActionDefinitionId(ShadowEditor.SOURCE_MENU_ID);
            for (IContributionItem item: items) {
                submenu.add(item);
            }
            return new IContributionItem[] { submenu };
        }        
        else {
            return items;            
        } 
        */       
    }
    
    public static final String REMOVE_UNUSED_IMPORTS = "remove_unused_imports";    

    private IContributionItem[] getItems(IEditorPart editor) {
    	
    	ShadowEditor shadowEditor = (ShadowEditor) editor;
    	boolean addBlockSelected = false;
    	boolean enabled = false;
    	
    	ShadowSourceViewer viewer = shadowEditor.getShadowSourceViewer();
    	if( viewer != null ) {
    		Point selection = viewer.getSelectedRange();
    		addBlockSelected = selection.y > 0;
    		enabled = true;
    	}
        return new IContributionItem[] {
        		 new DynamicMenuItem(ShadowPlugin.PLUGIN_ID + ".commands.toggleComment", 
                         "Toggle &Comment", 
                         enabled),
                 new DynamicMenuItem(ShadowPlugin.PLUGIN_ID + ".commands.addBlockComment", 
                         "&Add Block Comment", 
                         addBlockSelected),
                 new DynamicMenuItem(ShadowPlugin.PLUGIN_ID + ".commands.removeBlockComment", 
                         "Re&move Block Comment", 
                         enabled),
                 new DynamicMenuItem(ShadowPlugin.PLUGIN_ID + ".commands.generateElementComment", 
                         "Generate &Element Comment", 
                         enabled),
                new Separator(),
                new DynamicMenuItem(ITextEditorActionDefinitionIds.SHIFT_LEFT, 
                        "Shift &Left", 
                        enabled),
                new DynamicMenuItem(ITextEditorActionDefinitionIds.SHIFT_RIGHT, 
                        "Shift &Right", 
                        enabled),
                new DynamicMenuItem(ShadowPlugin.PLUGIN_ID + ".commands.correctIndentation", 
                        "Correct &Indentation", 
                        enabled),
                new Separator(),
                new DynamicMenuItem(ShadowPlugin.PLUGIN_ID + ".commands.removeUnusedImports", 
                        "Remove &Unused Imports", 
                        enabled)
               
        };
    }

}