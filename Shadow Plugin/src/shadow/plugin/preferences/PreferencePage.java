package shadow.plugin.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import shadow.plugin.ShadowPlugin;

public class PreferencePage extends FieldEditorPreferencePage  implements IWorkbenchPreferencePage {

	private static PreferencePage page;
	public static final String COMPILER_PATH = "shadow.plugin.preferences.PreferencePage.CompilerPath";
	
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
		addField(new FileFieldEditor(COMPILER_PATH, "&Path to shadow.jar:",
		        getFieldEditorParent()));
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
