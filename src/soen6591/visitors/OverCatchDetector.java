package ca.concordia.soen6591.antipattern_detector.visitors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ThrowStatement;

public class OverCatchDetector extends ASTVisitor {

    private int numAntiPatternsDetected = 0;
    private String compilationUnitName;
    private CompilationUnit compilationUnit;
    private List<OverCatch> detectionList = new ArrayList<>();

    public OverCatchDetector(String unitName, CompilationUnit compilationUnit) {
        this.compilationUnitName = unitName;
        this.compilationUnit = compilationUnit;
    }

    /**
     *  This method visits each catch clause in the AST and checks if it over-catches exceptions. If the catch clause is
     *  determined to be over-catching, an OverCatch object is created and added to the detection list.
     */

    @Override
    public boolean visit(CatchClause node) {
        boolean isOverCaught = true;
        List<ThrowStatement> throwStatements = new ArrayList<>();
        node.getBody().accept(new ThrowStatementVisitor(throwStatements));
        for (ThrowStatement ts : throwStatements) {
            if (ts.getExpression() == null) {
                isOverCaught = false;
                break;
            }
        }
        if (isOverCaught) {
            int lineNumber = compilationUnit.getLineNumber(node.getStartPosition());
            detectionList.add(new OverCatch(compilationUnitName, lineNumber));
            numAntiPatternsDetected++;
        }
        return super.visit(node);
    }

    private class ThrowStatementVisitor extends ASTVisitor {
        private List<ThrowStatement> throwStatements;

        public ThrowStatementVisitor(List<ThrowStatement> throwStatements) {
            this.throwStatements = throwStatements;
        }

        @Override
        public boolean visit(ThrowStatement node) {
            throwStatements.add(node);
            return super.visit(node);
        }
    }

    public int getNumAntiPatternsDetected() {
        return numAntiPatternsDetected;
    }

    public class OverCatch{
        private String fileName;
        private int lineNumber;
 
        public OverCatch(String fileName, int lineNumber) {
            this.fileName = fileName;
            this.lineNumber = lineNumber;
        }

        public String getFileName() {
            return fileName;
        }

        public int getLineNumber() {
            return lineNumber;
        }
    }
    
    public List<OverCatch> getDetectionList() {
        return detectionList;
    }
}
