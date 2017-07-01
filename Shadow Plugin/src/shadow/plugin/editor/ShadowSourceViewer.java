/*
 * This code borrows heavily from the Ceylon source-handling portion of their plug-in:
 * https://github.com/ceylon/ceylon-ide-eclipse/blob/09ec642878392b614acf879a26afb1db3296cb2d/plugins/com.redhat.ceylon.eclipse.ui/src/com/redhat/ceylon/eclipse/code/editor/CeylonSourceViewer.java
 */

package shadow.plugin.editor;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.DocumentRewriteSession;
import org.eclipse.jface.text.DocumentRewriteSessionType;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension4;
import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

public class ShadowSourceViewer extends ProjectionViewer {
	
	private IAutoEditStrategy autoEditStrategy;

	public ShadowSourceViewer(Composite parent, IVerticalRuler ruler, IOverviewRuler overviewRuler, boolean showsAnnotationOverview, int styles) {
		super(parent, ruler, overviewRuler, showsAnnotationOverview, styles);
		
		autoEditStrategy = new ShadowAutoIndentStrategy();
	}

	public void correctIndentation() {

		IDocument doc = getDocument();
		DocumentRewriteSession rewriteSession = null;
		if (doc instanceof IDocumentExtension4) {
			rewriteSession = 
					((IDocumentExtension4) doc)
					.startRewriteSession(DocumentRewriteSessionType.SEQUENTIAL);
		}


		Point selectedRange = getSelectedRange();
		boolean emptySelection = 
				selectedRange==null || selectedRange.y==0;
		
		int offset = selectedRange.x;
		int len = selectedRange.y;		

		try {
			correctSourceIndentation(offset, len, doc);
		} 
		catch (BadLocationException e) {
			e.printStackTrace();
		}
		finally {
			if (doc instanceof IDocumentExtension4) {
				((IDocumentExtension4) doc)
				.stopRewriteSession(rewriteSession);
			}
			restoreSelection();
			if (emptySelection) {
				selectedRange = getSelectedRange();
				setSelectedRange(selectedRange.x, 0);
			}
		}
	}

	private boolean lookingAtLineEnd(IDocument doc, int pos) {
		String[] legalLineTerms = doc.getLegalLineDelimiters();
		try {
			for(String lineTerm: legalLineTerms) {
				int len = lineTerm.length();
				if (pos>len && 
						doc.get(pos-len,len).equals(lineTerm)) {
					return true;
				}
			}
		} 
		catch (BadLocationException e) {
			e.printStackTrace();
		}
		return false;
	}

	public int correctSourceIndentation(int selStart, int selLen, 
			IDocument doc)
					throws BadLocationException {
		int selEnd = selStart + selLen;
		int startLine = doc.getLineOfOffset(selStart);
		int endLine = doc.getLineOfOffset(selEnd);

		// If the selection extends just to the beginning of the next line, don't indent that one too
		if (selLen > 0 && 
				lookingAtLineEnd(doc, selEnd)) {
			endLine--;
		}

		int endOffset = selStart+selLen-1;
		// Indent each line using the AutoEditStrategy
		for (int line=startLine; line<=endLine; line++) {
			int lineStartOffset = doc.getLineOffset(line);

			// Replace the existing indentation with the desired indentation.
			// Use the language-specific AutoEditStrategy, which requires a DocumentCommand.
			DocumentCommand cmd = new DocumentCommand() { };
			cmd.offset = lineStartOffset;
			cmd.length = 0;
			cmd.text = Character.toString('\t');
			cmd.doit = true;
			cmd.shiftsCaret = false;
			autoEditStrategy.customizeDocumentCommand(doc, cmd);
			if (cmd.text!=null) {
				doc.replace(cmd.offset, cmd.length, cmd.text);
				endOffset += cmd.text.length()-cmd.length;
			}
		}
		return endOffset;
	}


	public void addBlockComment() {
		IDocument doc = this.getDocument();
		DocumentRewriteSession rewriteSession = null;
		Point p = this.getSelectedRange();

		if (doc instanceof IDocumentExtension4) {
			rewriteSession = 
					((IDocumentExtension4) doc)
					.startRewriteSession(DocumentRewriteSessionType.SEQUENTIAL);
		}

		try {
			final int selStart = p.x;
			final int selLen = p.y;
			final int selEnd = selStart+selLen;
			doc.replace(selStart, 0, "/*");
			doc.replace(selEnd+2, 0, "*/");
		} 
		catch (BadLocationException e) {
			e.printStackTrace();
		} 
		finally {
			if (doc instanceof IDocumentExtension4) {
				((IDocumentExtension4) doc)
				.stopRewriteSession(rewriteSession);
			}
			restoreSelection();
		}
	}

	public void removeBlockComment() {
		IDocument doc = this.getDocument();
		DocumentRewriteSession rewriteSession = null;
		Point p = this.getSelectedRange();

		if (doc instanceof IDocumentExtension4) {
			rewriteSession = 
					((IDocumentExtension4) doc)
					.startRewriteSession(DocumentRewriteSessionType.SEQUENTIAL);
		}

		try {
			final int selStart = p.x;
			final int selLen = p.y;
			final int selEnd = selStart+selLen;
			String text = doc.get();
			int open = text.indexOf("/*", selStart);
			if (open>selEnd) open = -1;
			if (open<0) {
				open = text.lastIndexOf("/*", selStart);
			}
			int close = -1;
			if (open>=0) {
				close = text.indexOf("*/", open);
			}
			if (close+2<selStart) close = -1;
			if (open>=0&&close>=0) {
				doc.replace(open, 2, "");
				doc.replace(close-2, 2, "");
			}
		}
		catch (BadLocationException e) {
			e.printStackTrace();
		}
		finally {
			if (doc instanceof IDocumentExtension4) {
				((IDocumentExtension4) doc)
				.stopRewriteSession(rewriteSession);
			}
			restoreSelection();
		}
	}
	
	private boolean linesHaveCommentPrefix(IDocument doc, 
            String lineCommentPrefix, int startLine, int endLine) {
        try {
            int docLen = doc.getLength();

            for (int line=startLine; line<=endLine; line++) {
                
                int lineStart = doc.getLineOffset(line);
                int lineEnd = lineStart + doc.getLineLength(line) - 1;
                int offset = lineStart;

                while( Character.isWhitespace(doc.getChar(offset)) && 
                        offset < lineEnd ) {
                    offset++;
                }
                
                if (docLen-offset > lineCommentPrefix.length() && 
                    doc.get(offset, lineCommentPrefix.length())
                            .equals(lineCommentPrefix)) {
                    // this line starts with the single-line comment prefix
                }
                else {
                    return false;
                }
            }
        }
        catch (BadLocationException e) {
            return false;
        }
        return true;
    }
	
	private int calculateLeadingSpace(IDocument doc, 
            int startLine, int endLine) {
        try {
            int result = Integer.MAX_VALUE;
            for (int line=startLine; line<=endLine; line++) {
                
                int lineStart = doc.getLineOffset(line);
                int lineEnd = lineStart + doc.getLineLength(line) - 1;
                int offset = lineStart;
                
                while( Character.isWhitespace(doc.getChar(offset)) && offset < lineEnd )
                    offset++;                
                
                int leadingSpaces = offset - lineStart;
                result = Math.min(result, leadingSpaces);
            }
            return result;
        }
        catch (BadLocationException e) {
            return 0;
        }
    }

	public void toggleComment() {
		IDocument doc = this.getDocument();
		DocumentRewriteSession rewriteSession = null;
		Point p = this.getSelectedRange();
		final String lineCommentPrefix = "//";

		if (doc instanceof IDocumentExtension4) {
			rewriteSession = 
					((IDocumentExtension4) doc)
					.startRewriteSession(DocumentRewriteSessionType.SEQUENTIAL);
		}

		try {
			final int selStart = p.x;
			final int selLen = p.y;
			final int selEnd = selStart+selLen;
			final int startLine = doc.getLineOfOffset(selStart);
			int endLine = doc.getLineOfOffset(selEnd);

			if (selLen>0 && lookingAtLineEnd(doc, selEnd))
				endLine--;

			boolean linesAllHaveCommentPrefix = 
					linesHaveCommentPrefix(doc, 
							lineCommentPrefix, 
							startLine, endLine);
			boolean useCommonLeadingSpace = true; // take from a preference?
					int leadingSpaceToUse = 
					useCommonLeadingSpace ? 
							calculateLeadingSpace(doc, 
									startLine, endLine) : 0;

							for (int line = startLine; line<=endLine; line++) {

								int lineStart = doc.getLineOffset(line);
								int lineEnd = lineStart+doc.getLineLength(line)-1;

								if (linesAllHaveCommentPrefix) {
									// remove the comment prefix from each line, wherever it occurs in the line
									int offset = lineStart;
									while( Character.isWhitespace(doc.getChar(offset)) && offset<lineEnd )
										offset++;
									
									// The first non-whitespace characters *must* be the single-line comment prefix
									doc.replace(offset, 
											lineCommentPrefix.length(), 
											"");
								}
								else {
									// add the comment prefix to each line, after however many spaces leadingSpaceToAdd indicates
									int offset = lineStart+leadingSpaceToUse;
									doc.replace(offset, 0, lineCommentPrefix);
								}
							}
		}
		catch (BadLocationException e) {
			e.printStackTrace();
		}
		finally {
			if (doc instanceof IDocumentExtension4) {
				((IDocumentExtension4) doc)
				.stopRewriteSession(rewriteSession);
			}
			restoreSelection();
		}
	}

}
