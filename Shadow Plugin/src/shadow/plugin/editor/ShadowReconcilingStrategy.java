package shadow.plugin.editor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension;
import org.eclipse.swt.widgets.Display;

public class ShadowReconcilingStrategy implements IReconcilingStrategy, IReconcilingStrategyExtension {


	private ShadowEditor editor;
	private IDocument document;

	/**
	 * @return Returns the editor.
	 */
	public ShadowEditor getEditor() {
		return editor;
	}

	public void setEditor(ShadowEditor editor) {
		this.editor = editor;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.text.reconciler.IReconcilingStrategy#setDocument(org.eclipse.jface.text.IDocument)
	 */
	public void setDocument(IDocument document) {
		this.document = document;

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.text.reconciler.IReconcilingStrategy#reconcile(org.eclipse.jface.text.reconciler.DirtyRegion,
	 *      org.eclipse.jface.text.IRegion)
	 */
	public void reconcile(DirtyRegion dirtyRegion, IRegion subRegion) {
		initialReconcile();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.text.reconciler.IReconcilingStrategy#reconcile(org.eclipse.jface.text.IRegion)
	 */
	public void reconcile(IRegion partition) {
		initialReconcile();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension#setProgressMonitor(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void setProgressMonitor(IProgressMonitor monitor) {
		// TODO Auto-generated method stub
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension#initialReconcile()
	 */
	public void initialReconcile() {
		calculatePositions();
	}

	/**
	 * next character position - used locally and only valid while
	 * {@link #calculatePositions()} is in progress.
	 */
	private int nextPos = 0;

	private void calculatePositions() {
		/** holds the calculated positions */
		ArrayList<Position> positions = new ArrayList<Position>();
		nextPos = 0;

		try {                    
			visitTokens(positions, 0);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}

		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				editor.updateFoldingStructure(positions);
			}

		});
	}

	protected int visitTokens(List<Position> positions, int depth) throws BadLocationException {
		int newLines = 0;
		char lastCharacter = ' ';
		while( nextPos < document.getLength() ) {
			char ch = document.getChar(nextPos++);
			switch (ch) {
			case '=':
				if( nextPos < document.getLength() && document.getChar(nextPos) == '>' )
					newLines += visitMethod(positions);                	
				break;
			case '/':
				if( nextPos < document.getLength() && document.getChar(nextPos) == '*' )
					newLines += visitComment(positions);                	
				break;
			case '"':
				visitString();
			case 'c':
				if( Character.isWhitespace(lastCharacter) && matches("lass") )
					newLines += visitClass(positions, "class");
				break;
			case 'e':
				if( Character.isWhitespace(lastCharacter) ) {
					if( matches("num") )
						newLines += visitClass(positions, "enum");
					else if( matches("xception") ) 
						newLines += visitClass(positions, "exception");
				}
				break;
			case 'i':
				if( Character.isWhitespace(lastCharacter) && matches("nterface") )
					newLines += visitClass(positions, "interface");
				break;	
			case '{':
				depth++;
				break;
			case '}':
				depth--;
				if( depth <= 0 )
					return newLines;
				break;
			case '\n':
				newLines++;
				break;
			default:
				break;
			}
			lastCharacter = ch;
		}
		return newLines;
	}


	private Position makePosition(int start) throws BadLocationException {
		if( start < 0 )
			start = 0;

		int end = nextPos;
		if( end >= document.getLength() ) 
			end = document.getLength();
		else {
			char c = ' '; 
			while( end < document.getLength() && c != '\n' && Character.isWhitespace(c) ) {
				c = document.getChar(end); 
				end++;
			}

			if( c == '\n' )
				nextPos = end;
			else
				end = nextPos;
		}

		return new Position(start, end - start);
	}

	private int visitClass(List<Position> positions, String name) throws BadLocationException {
		int startOffset = nextPos - name.length();
		int newLines = 0;

		char c = ' '; 
		while( nextPos < document.getLength() && c != '{' ) {
			c = document.getChar(nextPos);
			if( c == '\n' )
				newLines++;
			nextPos++;
		}

		if( c == '{' ) {        
			newLines += visitTokens(positions, 1);
			if( newLines > 1 )
				positions.add(makePosition(startOffset));	        
		}

		return newLines;	
	}

	private int visitComment(List<Position> positions) throws BadLocationException {
		int startOffset = nextPos - 1;
		int newLines = 0;

		char c = ' ';
		char lastCharacter = ' ';
		while( nextPos < document.getLength() && !(lastCharacter == '*' && c == '/') ) {
			lastCharacter = c;
			c = document.getChar(nextPos);
			if( c == '\n' )
				newLines++;
			nextPos++;
		}

		if( lastCharacter == '*' && c == '/' ) {
			if( newLines > 1 )
				positions.add(makePosition(startOffset));	        
		}

		return newLines;	
	}

	private void visitString() throws BadLocationException {
		char c = ' ';
		while( nextPos < document.getLength() && c != '"' ) {
			c = document.getChar(nextPos);
			nextPos++;
		}		
	}

	private int visitMethod(List<Position> positions) throws BadLocationException {
		int startOffset = nextPos - 2;
		int newLines = 0;
		
		String prefix = "";
		boolean found = false;
		
		Pattern p = Pattern.compile("\\s(public|private|protected)\\s.*");
		
		while( startOffset >= 0 && !found ) {			
			if( p.matcher(prefix).matches() )
				found = true;			
			else {
				prefix = document.getChar(startOffset)  + prefix;
				startOffset--;
			}
		}
		//overshoots by one, either way
		startOffset++;			

		char c = ' '; 
		while( nextPos < document.getLength() && c != ';' && c != '{' ) {
			c = document.getChar(nextPos);
			if( c == '\n' )
				newLines++;
			nextPos++;
		}

		if( c == '{' ) {        
			newLines += visitTokens(positions, 1);
			if( newLines > 1 )
				positions.add(makePosition(startOffset));	        
		}

		return newLines;		
	}

	private boolean matches(String suffix) throws BadLocationException {
		if( nextPos + suffix.length() <= document.getLength() ) {
			for( int i = 0; i < suffix.length(); ++i )
				if( document.getChar(nextPos + i) != suffix.charAt(i) )
					return false;

			if( nextPos + suffix.length() == document.getLength() || Character.isWhitespace(document.getChar(nextPos + suffix.length()) ) ) {
				nextPos += suffix.length();
				return true;						
			}
		}		
		return false;
	}
}
