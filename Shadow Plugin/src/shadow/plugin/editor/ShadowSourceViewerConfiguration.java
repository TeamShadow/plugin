package shadow.plugin.editor;

import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;

import shadow.plugin.ShadowPlugin;
import shadow.plugin.util.ShadowColorProvider;

public class ShadowSourceViewerConfiguration
  extends TextSourceViewerConfiguration
{
  public int getTabWidth(ISourceViewer sourceViewer)
  {
    return 4;
  }  
  
  public String getConfiguredDocumentPartitioning(ISourceViewer sourceViewer)
  {
    return "__shadow_partioning";
  }
  
  public String[] getConfiguredContentTypes(ISourceViewer sourceViewer)
  {
    return new String[] { "__dftl_partition_content_type", "__shadow_multiline_comment" };
  }
  
  public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer)
  {
    PresentationReconciler reconciler = new PresentationReconciler();
    reconciler.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));
    
    DefaultDamagerRepairer damagerRepairer = new DefaultDamagerRepairer(ShadowPlugin.getDefault().getCodeScanner());
    reconciler.setDamager(damagerRepairer, "__dftl_partition_content_type");
    reconciler.setRepairer(damagerRepairer, "__dftl_partition_content_type");
    
    DefaultDamagerRepairer commentDamagerRepairer = new DefaultDamagerRepairer(new ShadowColorScanner(ShadowColorProvider.COMMENT));
    reconciler.setDamager(commentDamagerRepairer, "__shadow_multiline_comment");
    reconciler.setRepairer(commentDamagerRepairer, "__shadow_multiline_comment");
    
    return reconciler;
  }
  
  @Override
	public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType) {
		return new ShadowTextHover();
	}
}
