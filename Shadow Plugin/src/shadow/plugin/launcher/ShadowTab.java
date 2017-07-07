package shadow.plugin.launcher;

import java.nio.file.Files;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ResourceListSelectionDialog;

import shadow.plugin.ShadowPlugin;


public class ShadowTab extends AbstractLaunchConfigurationTab 
{	
	private Text fileText;
	private Button compileOnlyButton;	
	private Text compilerText;	
	private Text argumentsText;
	
    @Override
    public void createControl(Composite parent) {    	
    	Font font = parent.getFont();
		
		Composite comp = new Composite(parent, SWT.NONE);
		setControl(comp);
		GridLayout topLayout = new GridLayout();
		topLayout.verticalSpacing = 0;
		topLayout.numColumns = 1;
		comp.setLayout(topLayout);
		comp.setFont(font);
		
		
		Group fileGroup = new Group(comp, SWT.NONE );
		fileGroup.setLayoutData(new GridData( SWT.FILL, SWT.BEGINNING, true, false, 1, 1));
		fileGroup.setText("Main File:");
		GridLayout fileLayout = new GridLayout();		
		fileLayout.verticalSpacing = 5;
		fileLayout.numColumns = 2;
		fileGroup.setLayout(fileLayout);
		
		
		fileText = new Text(fileGroup, SWT.SINGLE | SWT.BORDER);
		fileText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		fileText.setFont(font);
		
		fileText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateLaunchConfigurationDialog();
			}
		});
		
		Button fileButton = createPushButton(fileGroup, "&Browse...", null); //$NON-NLS-1$		
		
		fileButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				browseForFile();
			}
		});
		
		compileOnlyButton = new Button( fileGroup, SWT.CHECK );
		compileOnlyButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		compileOnlyButton.setText("&Compile only");
		
		compileOnlyButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				updateLaunchConfigurationDialog();
				
			}

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				updateLaunchConfigurationDialog();
				
			}
		});		
		
		Group compilerGroup = new Group(comp, SWT.NONE );
		compilerGroup.setLayoutData(new GridData( SWT.FILL, SWT.BEGINNING, true, false, 1, 1));
		compilerGroup.setText("Compiler:");
		GridLayout compilerLayout = new GridLayout();		
		compilerLayout.verticalSpacing = 5;
		compilerLayout.numColumns = 3;
		compilerGroup.setLayout(compilerLayout);
		
		
		compilerText = new Text(compilerGroup, SWT.SINGLE | SWT.BORDER);
		compilerText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		compilerText.setFont(font);
		
		
		compilerText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateLaunchConfigurationDialog();
			}
		});
		
		
		Button compilerButton = createPushButton(compilerGroup, "&Browse...", null); //$NON-NLS-1$		
		
		compilerButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				browseForCompiler();
			}
		});
		
		Label argumentsLabel = new Label(compilerGroup, SWT.NONE);
		argumentsLabel.setFont(font);
		argumentsLabel.setText("&Arguments:");
		
		argumentsText = new Text(compilerGroup, SWT.SINGLE | SWT.BORDER);
		argumentsText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		argumentsText.setFont(font);	
		
		argumentsText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateLaunchConfigurationDialog();
			}
		});
		
		comp.pack();
    }
    
    /**
	 * Open a resource chooser to select a file to compile 
	 */
	protected void browseForFile() {
		ResourceListSelectionDialog dialog = new ResourceListSelectionDialog(getShell(), ResourcesPlugin.getWorkspace().getRoot(), IResource.FILE);
		dialog.setTitle("Main File");
		dialog.setMessage("Select file to compile");
		
		if (dialog.open() == Window.OK) {
			Object[] files = dialog.getResult();
			IFile file = (IFile) files[0];
			fileText.setText(file.getLocation().toString());
		}		
	}
	
	 /**
	 * Open a resource chooser to select a JAR to use as a shadow compiler 
	 */
	protected void browseForCompiler() {
		FileDialog dialog = new FileDialog(getShell());
		dialog.setFilterExtensions(new String[] { "*.jar"} );
		dialog.setFileName("shadow.jar");
		
		String file = dialog.open(); 		
		if( file != null )			
			compilerText.setText(file);		
	}

    @Override
    public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {    	
    	configuration.setAttribute(ShadowLaunchConfigurationAttributes.MAIN_FILE, ShadowLaunchConfigurationDelegate.getPathName());
		configuration.setAttribute(ShadowLaunchConfigurationAttributes.COMPILE_ONLY, false);
		configuration.setAttribute(ShadowLaunchConfigurationAttributes.COMPILER, ShadowLaunchConfigurationDelegate.getDefaultCompiler());
		configuration.setAttribute(ShadowLaunchConfigurationAttributes.ARGUMENTS, "");    	
    }
   
    @Override
    public void initializeFrom(ILaunchConfiguration configuration) {
        try {
			String mainFile = configuration.getAttribute(ShadowLaunchConfigurationAttributes.MAIN_FILE, ShadowLaunchConfigurationDelegate.getPathName());
			fileText.setText(mainFile);
			    			
			boolean compileOnly = configuration.getAttribute(ShadowLaunchConfigurationAttributes.COMPILE_ONLY, false);
			compileOnlyButton.setSelection(compileOnly);
			
			String compiler = configuration.getAttribute(ShadowLaunchConfigurationAttributes.COMPILER, ShadowLaunchConfigurationDelegate.getDefaultCompiler());
			compilerText.setText(compiler);
			
			String arguments = configuration.getAttribute(ShadowLaunchConfigurationAttributes.ARGUMENTS, "");
			argumentsText.setText(arguments);    			
        }
        catch (CoreException e)
        {}
    }

    @Override
    public void performApply(ILaunchConfigurationWorkingCopy configuration) {
    	configuration.setAttribute(ShadowLaunchConfigurationAttributes.MAIN_FILE, fileText.getText().trim());
		configuration.setAttribute(ShadowLaunchConfigurationAttributes.COMPILE_ONLY, compileOnlyButton.getSelection());
		configuration.setAttribute(ShadowLaunchConfigurationAttributes.COMPILER, compilerText.getText().trim());
		configuration.setAttribute(ShadowLaunchConfigurationAttributes.ARGUMENTS, argumentsText.getText().trim());   
    }

    @Override
    public String getName() {
            return "Shadow";
    }
    
    @Override
    public Image getImage() {
		return ShadowPlugin.getDefault().getImageRegistry().get(ShadowPlugin.SHADOW_ICON);
	}
    
    @Override
    public boolean isValid(ILaunchConfiguration configuration) {
    	try {
			return checkFileAndCompiler(configuration.getAttribute(ShadowLaunchConfigurationAttributes.MAIN_FILE, ""), configuration.getAttribute(ShadowLaunchConfigurationAttributes.COMPILER, ""));
		} catch (CoreException e) 
    	{}
    	
    	return false;
    }
    
    private boolean checkFileAndCompiler(String filePath, String compilerPath) {
    	IPath file = new Path(filePath);
    	if( file == null || !file.toFile().exists() || !file.getFileExtension().toLowerCase().equals("shadow") )
    		return false;
    	  	
    	java.nio.file.Path compiler = java.nio.file.Paths.get(compilerPath);
    	
    	if( compiler == null || !Files.exists(compiler) || !java.nio.file.Files.isExecutable(compiler) )
    		return false;
    	
    	return true; 
    }
    
    @Override
    public boolean canSave() {    	
    	return checkFileAndCompiler(fileText.getText().trim(), compilerText.getText().trim());    	
    }
}
