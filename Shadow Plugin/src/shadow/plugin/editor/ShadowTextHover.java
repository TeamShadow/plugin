package shadow.plugin.editor;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.texteditor.MarkerUtilities;

public class ShadowTextHover implements ITextHover {

	@Override
	public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
		if (hoverRegion != null) {
			try {
				if (hoverRegion.getLength() > -1){
					ITextFileBufferManager bufferManager = FileBuffers.getTextFileBufferManager();		
					ITextFileBuffer buffer = bufferManager.getTextFileBuffer(textViewer.getDocument());
					IPath path = buffer.getLocation();
					
					IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember(path);
					Region region = (Region) hoverRegion;
					
					IMarker[] markers = resource.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
					for( IMarker marker : markers )
						if( region.getOffset() < MarkerUtilities.getCharEnd(marker) && region.getOffset() + region.getLength() >= MarkerUtilities.getCharStart(marker) )
							return MarkerUtilities.getMessage(marker);
				}				
			} catch ( CoreException x) {
			}
		}
		return ShadowEditorMessages.getString("ShadowTextHover.emptySelection"); 
	}
	
	@Override
	public IRegion getHoverRegion(ITextViewer textViewer, int offset) {
		Point selection= textViewer.getSelectedRange();
		if (selection.x <= offset && offset < selection.x + selection.y)
			return new Region(selection.x, selection.y);
		return new Region(offset, 0);
	}

}
