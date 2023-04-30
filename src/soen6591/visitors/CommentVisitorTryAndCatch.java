package soen6591.visitors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.BlockComment;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.LineComment;
import org.eclipse.jdt.core.dom.TryStatement;

import soen6591.handlers.AntiPatternDetectorHandler;

public class CommentVisitorTryAndCatch extends ASTVisitor{
	private CompilationUnit tree;
	List<String> lineComments = new ArrayList<String>();
	List<String> blockComments = new ArrayList<String>();
	List<String> javaDoc = new ArrayList<String>();
	List<String> tryBlock = new ArrayList<String>();
	private Map<Integer, Integer> tryScope = new HashMap<Integer, Integer>();
	int commentInTryCount = 0;
	int commentInCatchCount = 0;
	List<String> catchBlock = new ArrayList<String>();
	private Map<Integer, Integer> catchScope = new HashMap<Integer, Integer>();
	List<String> toDoComments = new ArrayList<String>();
	List<String> otherComments = new ArrayList<String>();
	int toDoCommentCount = 0;
	String lineCommentString = "";
	String blockCommentString = "";
	String javaDocString = "";
	CompilationUnit compilationUnit;
    private String[] source;
	
    public CommentVisitorTryAndCatch(CompilationUnit compilationUnit, String[] source) {
    	super();
        this.compilationUnit = compilationUnit;
        this.source = source;
    }
    
	@Override
	public boolean visit(CatchClause node){
		int catchStartPosition = tree.getLineNumber(node.getBody().getStartPosition());
		int catchEndPosition = tree.getLineNumber(node.getBody().getStartPosition()+node.getBody().getLength());
		catchScope.put(catchStartPosition, catchEndPosition);
		catchBlock.add(node.getBody().toString());
		return super.visit(node);
	}
	
	@Override
	public boolean visit(TryStatement node){
		int tryStartPosition = tree.getLineNumber(node.getBody().getStartPosition());
		int tryEndPosition = tree.getLineNumber(node.getBody().getStartPosition()+node.getBody().getLength());
		tryScope.put(tryStartPosition, tryEndPosition);
		tryBlock.add(node.getBody().toString());
		return super.visit(node);
	}
	
	@Override
	public boolean visit(LineComment node) {
		int startLineNumber = compilationUnit.getLineNumber(node.getStartPosition()) - 1;
		lineCommentString = source[startLineNumber].trim();
		int startPosition = tree.getLineNumber(node.getStartPosition());
		lineComments.add(lineCommentString);
		isCommentInTry(startPosition, startPosition);
		boolean isInCatch = isCommentInCatch(startPosition, startPosition);
		lineCommentString = deleteSpace(lineCommentString);
		if (isInCatch == true && (lineCommentString.toUpperCase().contains("TODO") || lineCommentString.toUpperCase().contains("FIXME"))){
			toDoComments.add(lineCommentString);
			toDoCommentCount++;
		}
		return super.visit(node);
	}
	
	@Override
	public boolean visit(BlockComment node) {
		int startLineNumber = compilationUnit.getLineNumber(node.getStartPosition()) - 1;
        int endLineNumber = compilationUnit.getLineNumber(node.getStartPosition() + node.getLength()) - 1;
        StringBuffer blockComment = new StringBuffer();
        for (int lineCount = startLineNumber ; lineCount<= endLineNumber; lineCount++) {
            String blockCommentLine = source[lineCount].trim();
            blockComment.append(blockCommentLine);
            if (lineCount != endLineNumber) {
                blockComment.append("\n");
            }
        }
		blockCommentString = blockComment.toString();
		int startPosition = tree.getLineNumber(node.getStartPosition());
		int endPosition = tree.getLineNumber(node.getStartPosition()+node.getLength());
		blockComments.add(blockCommentString);
		isCommentInTry(startPosition, endPosition);
		boolean isInCatch = isCommentInCatch(startPosition, endPosition);
		blockCommentString = deleteSpace(blockCommentString);
		if (isInCatch == true && (blockCommentString.toUpperCase().contains("TODO") || blockCommentString.toUpperCase().contains("FIXME"))){
			toDoComments.add(blockCommentString);
			toDoCommentCount++;
		}
		return super.visit(node);
	}
	
	@Override
	public boolean visit(Javadoc node) {
		javaDocString = node.toString();
		AntiPatternDetectorHandler.printMessage("Visiting JavaDoc at line " + tree.getLineNumber(node.getStartPosition()) +
									" end line " + tree.getLineNumber(node.getStartPosition()+node.getLength()));
		javaDoc.add(javaDocString);
		return super.visit(node);
	}

	public void setTree(CompilationUnit cu) {
		tree = cu;
	}
	
	public int getCommentInTryCount() {
		return commentInTryCount;
	}
	
	public int getCommentInCatchCount() {
		return commentInCatchCount;
	}
	
	public int getToDoOrFixMeCommentsCount() {
		return toDoCommentCount;
	}
	
	private boolean isCommentInTry(int startPosition, int endPosition) {
		boolean result = false;
		for(Entry<Integer, Integer> item : tryScope.entrySet()) {
			if(
				startPosition > item.getKey() && startPosition < item.getValue() &&
				endPosition >= item.getKey() && endPosition <= item.getValue()) {
				if(startPosition == endPosition) {
					commentInTryCount++;
				}else {
					commentInTryCount = commentInTryCount + (endPosition - startPosition + 1);
				}
				result = true;
			}
		}
		return result;
	}

	private boolean isCommentInCatch(int startPosition, int endPosition) {
		boolean result = false;
		for(Entry<Integer, Integer> item : catchScope.entrySet()) {
			if(
				startPosition > item.getKey() && startPosition < item.getValue() &&
				endPosition >= item.getKey() && endPosition <= item.getValue()) {
				if(startPosition == endPosition) {
					commentInCatchCount++;
				}
				else {
					commentInCatchCount = commentInCatchCount + (endPosition - startPosition + 1);
				}
				result = true;
			}
		}
		
		return result;
	}
	
    private String deleteSpace(String input)
    {
        if (input == null || input == "") {
        	return input;
        }

        String updatedStr = input.replace("\n", "").replace("\r", "").replace("\t", "")
        .replace("    ", " ").replace("    ", " ").replace("   ", " ")
        .replace("  ", " ");

        return updatedStr;
    }
}