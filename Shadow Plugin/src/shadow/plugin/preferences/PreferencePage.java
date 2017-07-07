package shadow.plugin.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import shadow.plugin.ShadowPlugin;

public class PreferencePage extends FieldEditorPreferencePage  implements IWorkbenchPreferencePage {

	private static PreferencePage page;
	public static final String CONFIGURATION_PATH = "shadow.plugin.preferences.PreferencePage.ConfigurationPath";
	
	public PreferencePage() {
		super(FieldEditorPreferencePage.GRID);
		page = this;
		
		IPreferenceStore store =
				ShadowPlugin.getDefault().getPreferenceStore();
		setPreferenceStore(store);
	}
	
	public static PreferencePage getDefault() {
		return page;
	}
	
	@Override
	public void init(IWorkbench arg0) {	
		setDescription("General settings for Shadow plugin");

	}

	@Override
	protected void createFieldEditors() {
		FileFieldEditor field = new FileFieldEditor(CONFIGURATION_PATH, "&Path to configuration file:",
		        getFieldEditorParent());
		field.getTextControl(getFieldEditorParent()).setToolTipText("Setting the path to a configuration file should be unnecessary if the SHADOW_HOME environment variable is correctly set.  Shadow developers working on the standard library should use a configuration file that specifies the location of the standard library they are updating; otherwise, errors about duplicate copies of standard library source files will be reported."); 
		addField(field);
	}

	@Override
	protected IPreferenceStore doGetPreferenceStore() {
		return ShadowPlugin.getDefault().getPreferenceStore();
	}
	
	@Override
	public boolean performOk() {
		boolean value = super.performOk();
		ShadowPlugin.getDefault().resetCompilerInterface();
		return value;
	}
}
