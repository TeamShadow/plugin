package shadow.plugin.compiler;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ProgressMonitorWrapper;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.part.FileEditorInput;

import shadow.plugin.ShadowPlugin;
import shadow.plugin.editor.ShadowEditor;
import shadow.plugin.editor.ShadowSourceViewer;

/* 
 * Some code borrowed from the CeylonParserScheduler: 
 * https://github.com/ceylon/ceylon-ide-eclipse/blob/09ec642878392b614acf879a26afb1db3296cb2d/plugins/com.redhat.ceylon.eclipse.ui/src/com/redhat/ceylon/eclipse/code/parse/CeylonParserScheduler.java
 */


public class TypeCheckScheduler extends Job {

    private boolean canceling = false;
    
    private ShadowEditor editor;

    public TypeCheckScheduler(ShadowEditor editor) {
        super("Parsing and typechecking " + editor.getEditorInput().getName());
        setSystem(true); //do not show this job in the Progress view
        setPriority(SHORT);
        setRule(new ISchedulingRule() {            
            @Override
            public boolean isConflicting(ISchedulingRule rule) {
                return rule==this;
            }
            @Override
            public boolean contains(ISchedulingRule rule) {
                return rule==this;
            }
        });
       
        this.editor = editor;
    }

    @Override
    protected void canceling() {
        canceling = true;
    }

    public boolean isCanceling() {
        return canceling;
    }
  

    private boolean sourceStillExists() {    	
    	IEditorInput input = editor.getEditorInput();
		FileEditorInput fileInput = ((FileEditorInput)input);
		IFile file = fileInput.getFile();
		IProject project = file.getProject();
        if (project==null) {
            return true; // this wasn't a workspace resource to begin with
        }
        if (!project.exists()) {
            return false;
        }        
        return file.exists();
    }
    
    @Override
    public IStatus run(IProgressMonitor monitor) {
        try {
            if (canceling) {
                if (monitor!=null) {
                    monitor.setCanceled(true);
                }
                return Status.CANCEL_STATUS;
            }            

            IProgressMonitor wrappedMonitor = new ProgressMonitorWrapper(monitor) {
                @Override
                public boolean isCanceled() {
                    boolean isCanceled = false;
                    if (Job.getJobManager().currentJob() == TypeCheckScheduler.this) {
                        isCanceled = canceling;
                    }
                    return isCanceled || super.isCanceled();
                }
            };
            
            try {
                ShadowSourceViewer sourceViewer = editor.getShadowSourceViewer();
                IDocument document = sourceViewer==null ? null : sourceViewer.getDocument();
                // If we're editing a workspace resource, check   
                // to make sure that it still exists
                if (document==null || !sourceStillExists()) {
                    return Status.OK_STATUS;
                }              
                
                ShadowCompilerInterface.typeCheck((FileEditorInput)editor.getEditorInput(), document);
                
                Display.getDefault().asyncExec(new Runnable() {
                    public void run() {
                    	editor.updateOutline();
                    }
                } );
                
                
            } 
            catch (Exception e) {
                e.printStackTrace();
            }
            return wrappedMonitor.isCanceled() ? //&& sourceStillExists()
                    Status.OK_STATUS : 
                    Status.CANCEL_STATUS;
        }
        finally {
            canceling = false;
        }
    }
}