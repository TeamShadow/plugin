package shadow.plugin.editor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
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
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;
import org.eclipse.ui.texteditor.TextEditorAction;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

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


	private ShadowOutline outline;
	private ProjectionSupport projectionSupport;
	private ProjectionAnnotationModel annotationModel;

	
	@Override
	protected void createActions() {
		super.createActions();

		IAction a= new DefineFoldingRegionAction(ShadowEditorMessages.getResourceBundle(), "DefineFoldingRegion.", this);
		setAction("DefineFoldingRegion", a);
	}

	@Override
	public void dispose()
	{
		if (this.outline != null) {
			this.outline.update();
		}
		super.dispose();
	}

	@Override
	public void doRevertToSaved()
	{
		super.doRevertToSaved();
		if (this.outline != null) {
			this.outline.update();
		}
	}


	@Override
	public void doSave(IProgressMonitor monitor)
	{
		super.doSave(monitor);
		if (this.outline != null) {
			this.outline.setSelection(null);
			this.outline.update();
		}
	}


	@Override
	public void doSaveAs()
	{
		super.doSaveAs();
		if (this.outline != null) {
			this.outline.update();
		}
	}

	@Override
	public void doSetInput(IEditorInput input)
			throws CoreException
	{
		super.doSetInput(input);
		if (this.outline != null) {
			this.outline.update();
		}
	}


	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class required)
	{
		if (IContentOutlinePage.class.equals(required))
		{
			/*super.createAnnotationAccess();// change
    	super.*/

			if (this.outline == null) {
				this.outline = new ShadowOutline(this);
			}
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

		ISourceViewer viewer= new ProjectionViewer(parent, ruler, getOverviewRuler(), isOverviewRulerVisible(), styles);
		// ensure decoration support has been created and configured.
		getSourceViewerDecorationSupport(viewer);

		return viewer;
	}

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		ProjectionViewer viewer= (ProjectionViewer) getSourceViewer();
		projectionSupport= new ProjectionSupport(viewer, getAnnotationAccess(), getSharedColors());
		projectionSupport.addSummarizableAnnotationType("org.eclipse.ui.workbench.texteditor.error"); //$NON-NLS-1$
		projectionSupport.addSummarizableAnnotationType("org.eclipse.ui.workbench.texteditor.warning"); //$NON-NLS-1$
		projectionSupport.install();
		viewer.doOperation(ProjectionViewer.TOGGLE);
		
		annotationModel = viewer.getProjectionAnnotationModel();
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

	@Override
	protected void adjustHighlightRange(int offset, int length) {
		ISourceViewer viewer= getSourceViewer();
		if (viewer instanceof ITextViewerExtension5) {
			ITextViewerExtension5 extension= (ITextViewerExtension5) viewer;
			extension.exposeModelRange(new Region(offset, length));
		}
	}
	
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
}
