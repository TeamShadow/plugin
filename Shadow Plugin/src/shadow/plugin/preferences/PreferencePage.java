package shadow.plugin.preferences;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import shadow.plugin.ShadowPlugin;

public class PreferencePage extends FieldEditorPreferencePage  implements IWorkbenchPreferencePage {

	private FileFieldEditor field;
	public static final String CONFIGURATION_PATH = "shadow.plugin.preferences.PreferencePage.ConfigurationPath";
	
	public PreferencePage() {
		super(FieldEditorPreferencePage.GRID);
		
		IPreferenceStore store =
				ShadowPlugin.getDefault().getPreferenceStore();
		setPreferenceStore(store);
	}

	@Override
	public void init(IWorkbench arg0) {	
		setDescription("General settings for Shadow plugin");

	}
	
	@Override
	public boolean performOk() {
		if( isValid() ) {
			field.store();
			return super.performOk();
		}
		
		return false;
	}
	
	@Override
    protected void performDefaults() {
        field.getTextControl(getFieldEditorParent()).setText("");
        super.performDefaults();
    }

	@Override
	protected void createFieldEditors() {
		field = new FileFieldEditor(CONFIGURATION_PATH, "&Path to configuration file:",
		        getFieldEditorParent());
				
		field.getLabelControl(getFieldEditorParent()).setToolTipText("Setting the path to a configuration file should be unnecessary if the SHADOW_HOME environment variable is correctly set.\nShadow developers working on the standard library should use a configuration file that specifies the location of the standard library they are updating;\notherwise, errors about duplicate copies of standard library source files will be reported.");
		
		addField(field);		
	}
	
	@Override
	protected void checkState() {
        super.checkState();
        String text = field.getStringValue();
		
		if( text == null || text.trim().isEmpty() )
			setValid(true);
		else {
			Path path = Paths.get(text);			
			
			if( Files.exists(path) && text.trim().toLowerCase().endsWith(".xml") )
				setValid(true);
			else {
				setErrorMessage("Path must be to a valid XML file");				
				setValid(false);
			}
		}
	}
	
	public void propertyChange(PropertyChangeEvent event) {
        super.propertyChange(event);
        if (event.getProperty().equals(FieldEditor.VALUE)) {
                  checkState();
        }        
	}
	

	@Override
	protected IPreferenceStore doGetPreferenceStore() {
		return ShadowPlugin.getDefault().getPreferenceStore();
	}
	
}
