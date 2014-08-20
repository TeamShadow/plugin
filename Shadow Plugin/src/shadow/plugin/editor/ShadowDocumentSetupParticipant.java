package shadow.plugin.editor;

import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;
import shadow.plugin.ShadowPlugin;

public class ShadowDocumentSetupParticipant
  implements IDocumentSetupParticipant
{
  public void setup(IDocument document)
  {
    IDocumentPartitioner partitioner = new FastPartitioner(ShadowPlugin.getDefault().getPartitionScanner(), ShadowPartitionScanner.SHADOW_PARTITION_TYPES);
    if ((document instanceof IDocumentExtension3)) {
      ((IDocumentExtension3)document).setDocumentPartitioner("__shadow_partioning", partitioner);
    }
    partitioner.connect(document);
  }
}
