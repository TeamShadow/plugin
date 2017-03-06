package shadow.plugin.editor;

import org.eclipse.jface.text.DefaultIndentLineAutoEditStrategy;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.reconciler.MonoReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;

import shadow.plugin.ShadowPlugin;
import shadow.plugin.util.ShadowColorProvider;

public class ShadowSourceViewerConfiguration
  extends TextSourceViewerConfiguration
{
	private ShadowEditor editor;
	
	public ShadowSourceViewerConfiguration(ShadowEditor editor)
	{
		this.editor = editor;
	}
	
  public int getTabWidth(ISourceViewer sourceViewer)
  {
    return 4;
  }  
  
  public String getConfiguredDocumentPartitioning(ISourceViewer sourceViewer)
  {
    return ShadowPlugin.SHADOW_PARTITIONING;
  }
  
  public String[] getConfiguredContentTypes(ISourceViewer sourceViewer)
  {
    return new String[] { IDocument.DEFAULT_CONTENT_TYPE, ShadowPartitionScanner.SHADOX, ShadowPartitionScanner.SHADOW_MULTI_LINE_COMMENT };
  }
  
  @Override
	public IAutoEditStrategy[] getAutoEditStrategies(ISourceViewer sourceViewer, String contentType) {
		//IAutoEditStrategy strategy= (IDocument.DEFAULT_CONTENT_TYPE.equals(contentType) ? new ShadowAutoIndentStrategy() : new DefaultIndentLineAutoEditStrategy());
	  IAutoEditStrategy strategy= new DefaultIndentLineAutoEditStrategy();
		return new IAutoEditStrategy[] { strategy };
	}
	
  
  @Override
	public ITextDoubleClickStrategy getDoubleClickStrategy(ISourceViewer sourceViewer, String contentType) {
		return new ShadowDoubleClickSelector();
	}
  
  @Override
	public String[] getIndentPrefixes(ISourceViewer sourceViewer, String contentType) {
		return new String[] { "\t", "    " };
	}
  
  @Override
  public IReconciler getReconciler(ISourceViewer sourceViewer)
  {
	  ShadowReconcilingStrategy strategy = new ShadowReconcilingStrategy();
	  strategy.setEditor(editor);
	  
	  MonoReconciler reconciler = new MonoReconciler(strategy,false);
      
      return reconciler;
  }
  
  
  @Override
  public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer)
  {
    PresentationReconciler reconciler = new PresentationReconciler();
    reconciler.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));
    
    DefaultDamagerRepairer damagerRepairer = new DefaultDamagerRepairer(ShadowPlugin.getDefault().getCodeScanner());
    reconciler.setDamager(damagerRepairer, IDocument.DEFAULT_CONTENT_TYPE);
    reconciler.setRepairer(damagerRepairer, IDocument.DEFAULT_CONTENT_TYPE);
    
    DefaultDamagerRepairer shadoxDamagerRepairer = new DefaultDamagerRepairer(new ShadowColorScanner(ShadowColorProvider.COMMENT));
    reconciler.setDamager(shadoxDamagerRepairer, ShadowPartitionScanner.SHADOX);
    reconciler.setRepairer(shadoxDamagerRepairer, ShadowPartitionScanner.SHADOX);
    
    DefaultDamagerRepairer commentDamagerRepairer = new DefaultDamagerRepairer(new ShadowColorScanner(ShadowColorProvider.COMMENT));
    reconciler.setDamager(commentDamagerRepairer, ShadowPartitionScanner.SHADOW_MULTI_LINE_COMMENT);
    reconciler.setRepairer(commentDamagerRepairer, ShadowPartitionScanner.SHADOW_MULTI_LINE_COMMENT);
    
    
    return reconciler;
  }
  
  @Override
	public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType) {
		return new ShadowTextHover();
	}
}
