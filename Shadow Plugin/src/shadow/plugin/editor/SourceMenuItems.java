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
    }
    
    public static final String REMOVE_UNUSED_IMPORTS = "remove_unused_imports";    

    private IContributionItem[] getItems(IEditorPart editor) {
    	
    	ShadowEditor shadowEditor = (ShadowEditor) editor;
    	Point selection = shadowEditor.getShadowSourceViewer().getSelectedRange();
    	boolean isSelected = selection.y > 0;    	
        return new IContributionItem[] {
        		 new DynamicMenuItem(ShadowPlugin.PLUGIN_ID + ".commands.toggleComment", 
                         "Toggle &Comment", 
                         true),
                 new DynamicMenuItem(ShadowPlugin.PLUGIN_ID + ".commands.addBlockComment", 
                         "&Add Block Comment", 
                         isSelected),
                 new DynamicMenuItem(ShadowPlugin.PLUGIN_ID + ".commands.removeBlockComment", 
                         "Re&move Block Comment", 
                         true),
                 new DynamicMenuItem(ShadowPlugin.PLUGIN_ID + ".commands.generateElementComment", 
                         "Generate &Element Comment", 
                         true),
                new Separator(),
                new DynamicMenuItem(ITextEditorActionDefinitionIds.SHIFT_LEFT, 
                        "Shift &Left", 
                        true),
                new DynamicMenuItem(ITextEditorActionDefinitionIds.SHIFT_RIGHT, 
                        "Shift &Right", 
                        true),
                new DynamicMenuItem(ShadowPlugin.PLUGIN_ID + ".commands.correctIndentation", 
                        "Correct &Indentation", 
                        true),
                new Separator(),
                new DynamicMenuItem(ShadowPlugin.PLUGIN_ID + ".commands.removeUnusedImports", 
                        "Remove &Unused Imports", 
                        true)
               
        };
    }

}