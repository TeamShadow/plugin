package shadow.plugin.editor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.DefaultCharacterPairMatcher;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ICharacterPairMatcher;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;
import org.eclipse.ui.texteditor.TextEditorAction;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import shadow.plugin.ShadowPlugin;
import shadow.plugin.compiler.ShadowCompilerInterface;
import shadow.plugin.compiler.TypeCheckScheduler;
import shadow.plugin.outline.ShadowOutline;

public class ShadowEditor
extends TextEditor
{	
	private class DefineFoldingRegionAction extends TextEditorAction {

		public DefineFoldingRegionAction(ResourceBundle bundle, String prefix, ITextEditor editor) {
			super(bundle, prefix, editor);
		}

		private IAnnotationModel getAnnotationModel(ITextEditor editor) {
			return editor.getAdapter(ProjectionAnnotationModel.class);
		}

		@Override
		public void run() {
			ITextEditor editor= getTextEditor();			
			ISelection selection= editor.getSelectionProvider().getSelection();
			if (selection instanceof ITextSelection) {
				ITextSelection textSelection= (ITextSelection) selection;
				if (!textSelection.isEmpty()) {
					IAnnotationModel model= getAnnotationModel(editor);
					if (model != null) {

						int start= textSelection.getStartLine();
						int end= textSelection.getEndLine();

						try {
							IDocument document= editor.getDocumentProvider().getDocument(editor.getEditorInput());
							int offset= document.getLineOffset(start);
							int endOffset= document.getLineOffset(end + 1);
							Position position= new Position(offset, endOffset - offset);
							model.addAnnotation(new ProjectionAnnotation(), position);
						} catch (BadLocationException x) {
							// ignore
						}
					}
				}
			}
		}
	}
	
	private class DocumentListener implements IDocumentListener {
		private IDocument document;
		
		public DocumentListener(IDocument document) {
			this.document = document;
		}
		
		public void documentAboutToBeChanged(DocumentEvent event) {	            
        }
        public void documentChanged(DocumentEvent event) {
            synchronized (ShadowEditor.this) {
            	scheduleParsing();
            }
        }
        
        public IDocument getDocument() {
        	return document;
        }
        
        
        public void setDocument(IDocument document) {
        	this.document = document;
        }
	}
	
	private DocumentListener documentListener;
	    
    private IPropertyListener editorInputPropertyListener = 
            new IPropertyListener() {
        public void propertyChanged(Object source, int propId) {
            if (source == ShadowEditor.this && 
                    propId == IEditorPart.PROP_INPUT && documentListener != null) {
                IDocument oldDoc =
                        documentListener.getDocument();
                IDocument newDoc = 
                        getDocumentProvider()
                            .getDocument(getEditorInput()); 
                if( newDoc != oldDoc ) {
                    // Need to unwatch the old document 
                    // and watch the new document
                    if( oldDoc != null )
                        oldDoc.removeDocumentListener(documentListener);
                    
                    
                    documentListener.setDocument(newDoc);
                    
                    newDoc.addDocumentListener(
                            documentListener);
                }
                
                scheduleParsing();
            }
        }
    };

    private static final int TYPECHECK_SCHEDULE_DELAY = 500;

	private ShadowOutline outline;	
	private ProjectionSupport projectionSupport;
	private ProjectionAnnotationModel annotationModel;
	private TypeCheckScheduler typeCheckScheduler;
	
	public static IEditorPart getActiveEditor() {
		IWorkbench wb = PlatformUI.getWorkbench();
		IWorkbenchWindow window = wb.getActiveWorkbenchWindow();

		if(window != null) {
			IWorkbenchPage page = window.getActivePage();			
			if(page != null)
				return page.getActiveEditor();
		}
		
		return null;
	}
	
		
	@Override
	protected void createActions() {
		super.createActions();

		IAction a= new DefineFoldingRegionAction(ShadowEditorMessages.getResourceBundle(), "DefineFoldingRegion.", this);
		setAction("DefineFoldingRegion", a);
	}
	
	private void updateErrors() {
		//run updates in another thread for better responsiveness
		new Thread() {
			@Override
			public void run() {
				ShadowCompilerInterface.reportTypeCheckErrors((FileEditorInput)getEditorInput(), getDocumentProvider().getDocument(getEditorInput()));
			}
		}.start();
	}
	
	
	public void updateOutline() {
		if (outline != null)
			outline.update();		
	}

	@Override
	public void dispose()
	{
		outline.dispose();
		if (typeCheckScheduler!=null) {
			typeCheckScheduler.cancel(); // avoid unnecessary work after the editor is asked to close down
			typeCheckScheduler = null;
        }
		
		 IDocument document = getSourceViewer().getDocument();
	     if( document != null ) 
	    	 document.removeDocumentListener(documentListener);
	        
	     removePropertyListener(editorInputPropertyListener);
		
		super.dispose();
	}

	@Override
	public void doRevertToSaved()
	{
		super.doRevertToSaved();
		updateOutline();
		updateErrors();
	}
	
	private void scheduleParsing() {
		if( typeCheckScheduler != null ) {
			typeCheckScheduler.cancel();            
			typeCheckScheduler.schedule(TYPECHECK_SCHEDULE_DELAY);
        }
	}


	@Override
	public void doSave(IProgressMonitor monitor) {
		super.doSave(monitor);
		if (this.outline != null) {
			this.outline.setSelection(null);			
		}
		updateOutline();
		updateErrors();
	}


	@Override
	public void doSaveAs() {
		super.doSaveAs();
		updateOutline();
		updateErrors();
	}

	@Override
	public void doSetInput(IEditorInput input)
			throws CoreException {
		super.doSetInput(input);
		updateOutline();
		updateErrors();
	}


	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Object getAdapter(Class required) {
		if (IContentOutlinePage.class.equals(required)) {			
			if (this.outline == null)
				this.outline = new ShadowOutline(this);			
			return this.outline;
		}

		if (projectionSupport != null) {
			Object adapter= projectionSupport.getAdapter(getSourceViewer(), required);
			if (adapter != null)
				return adapter;
		}

		return super.getAdapter(required);
	}

	@Override
	protected void initializeEditor() {	
		super.initializeEditor();       
		setSourceViewerConfiguration(new ShadowSourceViewerConfiguration(this));
	}

	@Override
	protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {

		fAnnotationAccess= createAnnotationAccess();
		fOverviewRuler= createOverviewRuler(getSharedColors());

		ISourceViewer viewer = new ShadowSourceViewer(parent, ruler, getOverviewRuler(), isOverviewRulerVisible(), styles, this);
		// ensure decoration support has been created and configured.
		getSourceViewerDecorationSupport(viewer);

		return viewer;
	}

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		
		typeCheckScheduler = new TypeCheckScheduler(this);
		
		ProjectionViewer viewer= (ProjectionViewer) getSourceViewer();
		projectionSupport= new ProjectionSupport(viewer, getAnnotationAccess(), getSharedColors());
		projectionSupport.addSummarizableAnnotationType("org.eclipse.ui.workbench.texteditor.error"); //$NON-NLS-1$
		projectionSupport.addSummarizableAnnotationType("org.eclipse.ui.workbench.texteditor.warning"); //$NON-NLS-1$
		projectionSupport.install();
		viewer.doOperation(ProjectionViewer.TOGGLE);
		
		annotationModel = viewer.getProjectionAnnotationModel();
		
		IDocument document = getSourceViewer().getDocument();
		documentListener =  new DocumentListener(document);
		
        if( document!=null )
            document.addDocumentListener(documentListener);
        
        addPropertyListener(editorInputPropertyListener);
        
        //initial typecheck
        //typeCheckScheduler.schedule();		
	}
	
	private Annotation[] oldAnnotations;	
	public void updateFoldingStructure(ArrayList<Position> positions)
	{
	   Annotation[] annotations = new Annotation[positions.size()];

	   //this will hold the new annotations along
	   //with their corresponding positions
	   HashMap<ProjectionAnnotation, Position > newAnnotations = new HashMap<ProjectionAnnotation, Position>();

	   for(int i = 0; i < positions.size();i++)
	   {
	      ProjectionAnnotation annotation = new ProjectionAnnotation();

	      newAnnotations.put(annotation, positions.get(i));

	      annotations[i] = annotation;
	   }

	   annotationModel.modifyAnnotations(oldAnnotations, newAnnotations,null);

	   oldAnnotations = annotations;
	}
	
	
	public ShadowSourceViewer getShadowSourceViewer() {
		return (ShadowSourceViewer)getSourceViewer();
	}

	@Override
	protected void adjustHighlightRange(int offset, int length) {
		ISourceViewer viewer= getSourceViewer();
		if (viewer instanceof ITextViewerExtension5) {
			ITextViewerExtension5 extension= (ITextViewerExtension5) viewer;
			extension.exposeModelRange(new Region(offset, length));
		}
	}
	
	
	public final static String SOURCE_MENU_ID = ShadowPlugin.PLUGIN_ID + ".menu.sourceMenu";	
	
	public final static String EDITOR_MATCHING_BRACKETS = "matchingBrackets";
	public final static String EDITOR_MATCHING_BRACKETS_COLOR= "matchingBracketsColor";

	@Override
	protected void configureSourceViewerDecorationSupport (SourceViewerDecorationSupport support) {
	    super.configureSourceViewerDecorationSupport(support);      

	    char[] matchChars = {'(', ')', '[', ']', '{', '}'}; //which brackets to match     
	    ICharacterPairMatcher matcher = new DefaultCharacterPairMatcher(matchChars ,
	            IDocumentExtension3.DEFAULT_PARTITIONING, true);
	    support.setCharacterPairMatcher(matcher);
	    support.setMatchingCharacterPainterPreferenceKeys(EDITOR_MATCHING_BRACKETS,EDITOR_MATCHING_BRACKETS_COLOR);

	    //Enable bracket highlighting in the preference store
	    IPreferenceStore store = getPreferenceStore();
	    store.setDefault(EDITOR_MATCHING_BRACKETS, true);
	    store.setDefault(EDITOR_MATCHING_BRACKETS_COLOR, "192,192,192");
	}
	
	@Override
    protected void editorContextMenuAboutToShow(IMenuManager menu) {
        super.editorContextMenuAboutToShow(menu);        
        menu.remove(ITextEditorActionConstants.SHIFT_LEFT);
        menu.remove(ITextEditorActionConstants.SHIFT_RIGHT); 
    }	
}
