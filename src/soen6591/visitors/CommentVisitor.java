package soen6591.visitors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.BlockComment;
import org.eclipse.jdt.core.dom.LineComment;
import org.eclipse.jdt.core.dom.Statement;

import soen6591.handlers.AntiPatternDetectorHandler;

import org.eclipse.jdt.core.dom.CompilationUnit;

public class CommentVisitor extends ASTVisitor  {
    List<String> lc = new ArrayList<String>();
    CompilationUnit cu;
    private String[] temp;
    List<String> bc = new ArrayList<String>();

    public CommentVisitor(CompilationUnit compilationUnit, String[] source) {
        super();
        this.cu = compilationUnit;
        this.temp = source;
    }

    /**
     * Visits a LineComment node in the Abstract Syntax Tree (AST) of a Java compilation unit and extracts its content.
     * Adds the content to a list of line comments.
     *
     * @param node the LineComment object to visit
     * @return true to continue visiting the node's children, false to stop
     */
    @Override
    public boolean visit(LineComment node) {
        int startLineNumber = cu.getLineNumber(node.getStartPosition()) - 1;
        String lineComment = temp[startLineNumber].trim();

        lc.add(lineComment);

        return super.visit(node);
    }

    /**
     * Visits a BlockComment node in the Abstract Syntax Tree (AST) of a Java compilation unit and extracts its content.
     * Adds the content to a list of block comments.
     *
     * @param node the BlockComment object to visit
     * @return true to continue visiting the node's children, false to stop
     */
    @Override
    public boolean visit(BlockComment node) {
        int startLineNumber = cu.getLineNumber(node.getStartPosition()) - 1;
        int endLineNumber = cu.getLineNumber(node.getStartPosition() + node.getLength()) - 1;
        StringBuffer blockComment = new StringBuffer();

        for (int lineCount = startLineNumber ; lineCount<= endLineNumber; lineCount++) {

            String blockCommentLine = temp[lineCount].trim();
            blockComment.append(blockCommentLine);
            if (lineCount != endLineNumber) {
                blockComment.append("\n");
            }
        }
        bc.add(blockComment.toString());

        return super.visit(node);
    }

    // return the line comments
    public List<String> getLineComments() {
        return lc;
    }

    // return the block comments.
    public List<String> getBlockComments() {
        return bc;}
}
