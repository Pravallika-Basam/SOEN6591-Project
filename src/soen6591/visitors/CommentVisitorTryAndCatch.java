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

/**
 * This class visits Java source code and extract comments that contain the keywords "TODO" or "FIXME" from try-catch blocks.
 * @author Aniket Tailor
 *
 */
public class CommentVisitorTryAndCatch extends ASTVisitor{
	private CompilationUnit cu;
	private Map<Integer, Integer> tryScope = new HashMap<Integer, Integer>();

	List<String> catchBlock = new ArrayList<String>();
	private Map<Integer, Integer> catchScope = new HashMap<Integer, Integer>();
	List<String> toDoComments = new ArrayList<String>();
	List<String> otherComments = new ArrayList<String>();
	int NoOfCommentTry = 0, NoOfCommentCatch = 0, TempCommentCount=0;
	String LineComment = "", BlockComment = "", DocsString = "";
	CompilationUnit compilationUnit;
    private String[] source;
	List<String> lc = new ArrayList<String>();
	List<String> bc = new ArrayList<String>();
	List<String> docs = new ArrayList<String>();
	List<String> tryInstance = new ArrayList<String>();
	
	/**
     * Constructor for the CommentVisitorTryAndCatch class
     * @param compilationUnit: the compilation unit to visit
     * @param source: an array containing the source code lines
     */
    public CommentVisitorTryAndCatch(CompilationUnit compilationUnit, String[] source) {
    	super();
        this.compilationUnit = compilationUnit;
        this.source = source;
    }
    
    /**
     * This method visits a try statement and records its start and end positions
     * @param node TryStatement node being visited.
     * @return true if the visitation was successful.
     */
    @Override
	public boolean visit(TryStatement node){
		int tryStartPosition = cu.getLineNumber(node.getBody().getStartPosition());
		int tryEndPosition = cu.getLineNumber(node.getBody().getStartPosition()+node.getBody().getLength());
		tryScope.put(tryStartPosition, tryEndPosition);
		tryInstance.add(node.getBody().toString());
		return super.visit(node);
	}
    
    /**
     * Visits a line comment and records its contents and position
     */
    @Override
	public boolean visit(LineComment node) {
		int startLineNumber = compilationUnit.getLineNumber(node.getStartPosition()) - 1;
		LineComment = source[startLineNumber].trim();
		int startPosition = cu.getLineNumber(node.getStartPosition());
		lc.add(LineComment);
		isCommentInTry(startPosition, startPosition);
		boolean isInCatch = isCommentInCatch(startPosition, startPosition);
		LineComment = deleteSpace(LineComment);
		if (isInCatch == true && (LineComment.toUpperCase().contains("TODO") || LineComment.toUpperCase().contains("FIXME"))){
			toDoComments.add(LineComment);
			TempCommentCount++;
		}
		return super.visit(node);
	}
    
    /**
     * Visits a catch clause and records its start and end positions
     */
	@Override
	public boolean visit(CatchClause node){
		int catchStartPosition = cu.getLineNumber(node.getBody().getStartPosition());
		int catchEndPosition = cu.getLineNumber(node.getBody().getStartPosition()+node.getBody().getLength());
		catchScope.put(catchStartPosition, catchEndPosition);
		catchBlock.add(node.getBody().toString());
		return super.visit(node);
	}
	
	/**
     * Visits a block comment and records its contents and position
     */
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
		BlockComment = blockComment.toString();
		int startPosition = cu.getLineNumber(node.getStartPosition());
		int endPosition = cu.getLineNumber(node.getStartPosition()+node.getLength());
		bc.add(BlockComment);
		isCommentInTry(startPosition, endPosition);
		boolean isInCatch = isCommentInCatch(startPosition, endPosition);
		BlockComment = deleteSpace(BlockComment);
		if (isInCatch == true && (BlockComment.toUpperCase().contains("TODO") || BlockComment.toUpperCase().contains("FIXME"))){
			toDoComments.add(BlockComment);
			TempCommentCount++;
		}
		return super.visit(node);
	}
	
	/**
	 * This method is called when visiting a JavaDoc node in the AST. It retrieves the string representation 
	 * of the JavaDoc, adds it to a list of all the JavaDocs in the source code, and returns a boolean to 
	 * continue visiting the AST.
	 *
	 */
	@Override
	public boolean visit(Javadoc node) {
		DocsString = node.toString();
		AntiPatternDetectorHandler.printMessage("Visiting JavaDoc at line " + cu.getLineNumber(node.getStartPosition()) +
									" end line " + cu.getLineNumber(node.getStartPosition()+node.getLength()));
		docs.add(DocsString);
		return super.visit(node);
	}
	
	/**
	 * This method checks if a given comment position is inside a "try" block. It returns true if it is, 
	 * otherwise false. It also increments a counter of comments in the "try" block.
	 * @param startPosition
	 * @param endPosition
	 * @return
	 */
	private boolean isCommentInTry(int startPosition, int endPosition) {
		boolean result = false;
		for(Entry<Integer, Integer> item : tryScope.entrySet()) {
			if(
				startPosition > item.getKey() && startPosition < item.getValue() &&
				endPosition >= item.getKey() && endPosition <= item.getValue()) {
				if(startPosition == endPosition) {
					NoOfCommentTry++;
				}else {
					NoOfCommentTry = NoOfCommentTry + (endPosition - startPosition + 1);
				}
				result = true;
			}
		}
		return result;
	}

	/**
	 * This method checks if a given comment position is inside a "catch" block. It returns true if it is, 
	 * otherwise false. It also increments a counter of comments in the "catch" block.
	 * @param startPosition
	 * @param endPosition
	 * @return
	 */
	private boolean isCommentInCatch(int startPosition, int endPosition) {
		boolean result = false;
		for(Entry<Integer, Integer> item : catchScope.entrySet()) {
			if(
				startPosition > item.getKey() && startPosition < item.getValue() &&
				endPosition >= item.getKey() && endPosition <= item.getValue()) {
				if(startPosition == endPosition) {
					NoOfCommentCatch++;
				}
				else {
					NoOfCommentCatch = NoOfCommentCatch + (endPosition - startPosition + 1);
				}
				result = true;
			}
		}
		return result;
	}
	
	/**
	 * This method takes a string input and removes all new line, carriage return, and tab characters, as well
	 * as multiple spaces, and returns the updated string.
	 * @param input
	 * @return
	 */
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
    
    //This method sets the CompilationUnit instance variable cu to the given CompilationUnit.
    public void setTree(CompilationUnit cu) {
    public void setTree(CompilationUnit cu) {
		cu = cu;
	}
	
    //This method returns the number of comments found inside the "try" block.
	public int getCommentInTryCount() {
		return NoOfCommentTry;
	}
	
	//This method returns the number of comments found inside the "catch" block.
	public int getCommentInCatchCount() {
		return NoOfCommentCatch;
	}
	
	//this method returns the number of TODO or FIXME comments found in the source code.
	public int getToDoOrFixMeCommentsCount() {
		return TempCommentCount;
	}
}