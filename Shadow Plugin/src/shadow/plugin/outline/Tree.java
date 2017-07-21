package shadow.plugin.outline;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.eclipse.jface.viewers.StyledString;

import shadow.parse.Context;

public class Tree implements Comparable<Tree> {  
	private ParseTree node;
	private List<Tree> children;
	private Tree parent;
	private ShadowLabel label;
	private String name;
	private String extra;


	public Tree(ParseTree node, Tree parent, String name, ShadowLabel label) {
		this(node, parent, name, "", label);
	}

	public Tree(ParseTree node, Tree parent, String name, String extra, ShadowLabel label) {
		this.node = node;
		this.parent = parent;
		this.label = label;	
		this.extra = extra;	
		this.name = name;
		
		children = new ArrayList<Tree>();
		
		if( parent != null )
			parent.children.add(this);
	}

	public Object[] getChildren() {	
		return children.toArray();
	}

	public boolean hasChildren() {
		return children.size() > 0;			
	}

	public Tree getParent() {
		return parent;
	}
	
	public boolean isField() {
		return label == ShadowLabel.FIELD;
	}
	
	public boolean isNonPublic() {
		return 	label == ShadowLabel.PRIVATE_CLASS || label == ShadowLabel.PROTECTED_CLASS ||				
				label == ShadowLabel.FIELD || label == ShadowLabel.PRIVATE_CONSTANT ||
				label == ShadowLabel.PRIVATE_METHOD || label == ShadowLabel.PROTECTED_CONSTANT ||
				label == ShadowLabel.PROTECTED_METHOD;
	}

	public String toString() {
		if( extra != null && !extra.isEmpty() ) {
			if( label == ShadowLabel.FIELD || label == ShadowLabel.PUBLIC_CONSTANT || label == ShadowLabel.PRIVATE_CONSTANT || label == ShadowLabel.PROTECTED_CONSTANT ) 
				return name + " : " + extra;

			else 
				return name + extra;
		}			

		return name;
	}

	public StyledString toStyledString() {
		if( extra != null && !extra.isEmpty() ) {
			if( label == ShadowLabel.FIELD || label == ShadowLabel.PUBLIC_CONSTANT || label == ShadowLabel.PRIVATE_CONSTANT || label == ShadowLabel.PROTECTED_CONSTANT ) 
				return new StyledString(name).append( " : " + extra, StyledString.DECORATIONS_STYLER);

			else 
				return new StyledString(name + extra);
		}			

		return new StyledString(name);
	}

	public int lineStart() {
		if( node instanceof Context )
			return ((Context)node).lineStart();
		else if( node instanceof TerminalNode )
			return ((TerminalNode)node).getSymbol().getLine();
		else
			return 1;
	}
	
	public int lineEnd() {
		if( node instanceof Context )
			return ((Context)node).lineEnd();
		else if( node instanceof TerminalNode )
			return ((TerminalNode)node).getSymbol().getLine();
		else
			return 1;
	}
	
	public int columnStart() {
		if( node instanceof Context )
			return ((Context)node).columnStart();
		else if( node instanceof TerminalNode )
			return ((TerminalNode)node).getSymbol().getCharPositionInLine();
		else
			return 0;
	}
	
	public int columnEnd() {
		if( node instanceof Context )
			return ((Context)node).columnEnd();
		else if( node instanceof TerminalNode )
			return ((TerminalNode)node).getSymbol().getCharPositionInLine() + ((TerminalNode)node).getSymbol().getText().length() - 1;
		else
			return 0;
	}

	public ShadowLabel getLabel() {
		return label;			
	}

	public void setLabel(ShadowLabel label) {
		this.label = label;
	}

	public int getLength() {
		return name.length();
	}

	@Override
	public int compareTo(Tree other) {
		if( label.level != other.label.level )
			return label.level - other.label.level;
		
		return toString().compareTo(other.toString());
	}
}	

