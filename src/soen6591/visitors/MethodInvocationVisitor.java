package soen6591.visitors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import soen6591.handlers.AntiPatternDetectorHandler;

public class MethodInvocationVisitor extends ASTVisitor{
	
	private static String[] LogMethods = {"log", "info", "warn", "error", "trace", "debug", "fatal"};
    private static String[] PrintMethods = {"println", "print"}; 
	private static String[] DefaultMethods = {"printStackTrace"}; 
	private static String[] ThrowMethods = {"throw"}; 
	
	//private int logPrintDefaultStatements = 0;
	
	
	private String exceptionTypeName;
	private ArrayList<String> flowHandlingActionStatements = new ArrayList<String>();
	private String statementAccordingToVisitorType;
	private String exceptionName;
	private int thrownStatements = 0;
	private ITypeBinding exceptionType;
	
	private MethodInvocation currentNode;
	private MethodInvocation invokedMethodNode;
	private int flowHandlingAction = 0;
	
	public ArrayList<String> exceptionList = new ArrayList<String>();
	private int numberofCheckedException =0;
	public ITypeBinding[] methodExceptionBindings;
	private int numberofMethodInvoke =0;
	
	public MethodInvocationVisitor(String statement) 
	{
		this.statementAccordingToVisitorType = statement;
	}
	
//	public MethodInvocationVisitor() 
//	{
//	}
	
	/**
	 *  It goes through each node and processes each node based on the statements and updates certain metrics related to logging and exception handling
	 *   in the program.
     *   @author Ankur
	 *   @param MethodInvovation node representing a method call in the AST.
	 *   @return true if the node has been visited successfully, false otherwise.
	 *   	 
	 */
	@Override
	public boolean visit(MethodInvocation node) {
		this.currentNode = node;
		
		/**
		 * checks for log statement inside catch. If present then checks for if logging and thrown is present then increments 
		 * throw counter by 1. Also checks if logging statement or method call is present, then increments flow handling action by 1.
		 * 
		 */
		if(this.statementAccordingToVisitorType == "LogCatchSwitch"){ 
			
			String nodeName = node.getName().toString();
			//SampleHandler.printMessage("Invoked nodeName::::::" + nodeName);
//			if (IsLoggingStatement(nodeName) || IsDefaultStatement(nodeName) || IsPrintStatement(nodeName)) {
//				logPrintDefaultStatements += 1;
//			}
			
			if(IsLoggingStatement(nodeName) && IsThrownStatement(nodeName)) {
				thrownStatements += 1;
			}

			//Flow Handling Actions: Method call/Log inside Catch
			if(IsLoggingStatement(nodeName) || isMethodCall(node)) {
				flowHandlingAction ++;
			}
		}
		
		/**
		 *  If it is a "try" block, then it retrieves the invoked method node and resolves its method binding. 
		 *  @param "node" is an instance of the class "MethodInvocation" representing the method invocation node being visited.
		 *  @return updates instance variables of the class that are later used for analysis.
		 */ 
		
		if (this.statementAccordingToVisitorType == "TryBlock") {

			this.invokedMethodNode = node;
			IMethodBinding method = node.resolveMethodBinding();
			if (method != null) {
				ITypeBinding[] exceptionBinding = method.getExceptionTypes();
				for (ITypeBinding exception : exceptionBinding) {
					String exceptionName = exception.getQualifiedName();
					this.exceptionName = exceptionName;

					ITypeBinding exceptionType = exception.getTypeDeclaration();
					this.exceptionTypeName = exceptionType.getQualifiedName();
					this.exceptionType = exceptionType;
					
					this.exceptionList.add(exceptionName);
				}
			}

		}
		
		/**
		 * checks if the current visited node is a method invocation statement. If it is, it retrieves the method binding of the invoked method, 
		 * increments the counter for the number of method invocations
		 * @param None
		 * @return void method doesnt return
		 * 
		 */
		if(this.statementAccordingToVisitorType == "MethodInvoke") {
			this.invokedMethodNode = node;
			
			IMethodBinding methodNode = node.resolveMethodBinding();
			//SampleHandler.printMessage("Invoked Method::::::" + node.getParent().getParent().getParent());
			numberofMethodInvoke++;
		}
		if(this.statementAccordingToVisitorType == "TryScope") {
			//SampleHandler.printMessage("Invoked Method::::::" + node);			
		}
		
		/**
		 * When the field is "throwBlock", it retrieves the method binding for the throw statement and counts the number of checked exceptions
		 *  in the method's exception type list.
		 *  @param node representing the throw statement
		 *  @return none
		 */
		if(this.statementAccordingToVisitorType == "throwBlock") 
		{
			IMethodBinding method = node.resolveMethodBinding();
			if(method != null) {
				ITypeBinding[] exceptionBind = method.getExceptionTypes();
				methodExceptionBindings = exceptionBind.clone();
				for(ITypeBinding exception : exceptionBind) {
					numberofCheckedException++;
				}
			}
		}	

		return super.visit(node);
	}

	
	/**
	 * Checks if the given node is a method call and adds the method name and action to list.
	 * @param node The MethodInvocation node to be checked.
	 * @return true if the given node is a method call, false otherwise.
	 */
	public boolean isMethodCall(MethodInvocation node) {
		IMethodBinding method = currentNode.resolveMethodBinding();
		
		if(method != null) 
		{
			IMethodBinding bind = method.getMethodDeclaration();
			
			Expression e = node.getExpression(); 
			if(e instanceof Name) 
			{
				Name name = (Name) e;
				String type = name.resolveBinding() + "";
				//SampleHandler.printMessage("Object Name:" + n.resolveBinding().getName() + ", Method name:" + node.getName().getFullyQualifiedName());
				//SampleHandler.printMessage("type:" + name.resolveBinding());
				if(!type.contains("java.util.logging.Logger")) 
				{
					flowHandlingActionStatements.add(bind.getName() + ", Action:'Method Call'");
				}
			}
			else 
			{
				flowHandlingActionStatements.add(bind.getName() + ", Action:'Method Call'");
			}

			
				return true;
			//}
		}
		
		return false;
	}
	public ITypeBinding getExceptionType() {
		return exceptionType;
	}
	
	public ITypeBinding[] getMethodExceptionBindings() {
		return methodExceptionBindings;
	}
	public String getExceptionTypeName() {
		return exceptionTypeName;
	}
	public String getExceptionName() {
		return exceptionName;
	}
	
	public MethodInvocation getInvokedMethod() {
		return invokedMethodNode;
	}
	
	

	
	
	
	
	
	/**
	 * If the parent node is not a type declaration, the function recursively searches for the parent type declaration until it finds one.
	 * @param node
	 * @return A String representing the name of the parent type declaration of the given node.
	 */
	public static String findParentType (ASTNode node){
		  int parentNodeType = node.getParent().getNodeType();
		  if(parentNodeType == ASTNode.TYPE_DECLARATION)
		  {
			  TypeDeclaration type = (TypeDeclaration) node.getParent();
			  if(type.resolveBinding() != null)
				  
				  return type.resolveBinding().getQualifiedName();
			  else		  
				  
				  return type.getName().getFullyQualifiedName();
		  }
		  return findParentType(node.getParent());
	  }

	
	
    /**
     * This method checks if a given statement contains any of the throw methods listed in the ThrowMethods array.
     * @param statement
     * @return true if the statement contains any of the throw methods in the ThrowMethods array, otherwise false.
     */
    private static boolean IsThrownStatement(String statement)
    {
        if (statement == null) 
        	return false;
        for (String logs : ThrowMethods)
        {
            if (statement.indexOf(logs) > -1)
            {
                return true;
            }
        }
        return false;
    }
    
    /**
     * The method IsLoggingStatement is a private static boolean method that checks if the given statement is a logging statement or not.
     * @param statement
     * @return true if its logging statement false otherwise
     */
    private static boolean IsLoggingStatement(String statement)
    {
        if (statement == null) 
        	return false;
        for (String logs : LogMethods)
        {
            if (statement.indexOf(logs) > -1)
            {
                return true;
            }
        }
        
        return false;
    }
    
    /// To check whether an invocation is a default statement
//	private static boolean IsDefaultStatement(String statement)
//	{
//        if (statement == null) return false;
//        for (String defaultmethod : DefaultMethods)
//        {
//            if (statement.indexOf(defaultmethod) > -1)
//            {
//                return true;
//            }
//        }
//        return false;
//    }
	
    /// To check whether an invocation is a print statement
//	private static boolean IsPrintStatement(String statement)
//	{
//        if (statement == null) return false;
//        for (String defaultmethod : PrintMethods)
//        {
//            if (statement.indexOf(defaultmethod) > -1)
//            {
//                return true;
//            }
//        }
//        return false;
//    }

	public ArrayList<String> getFlowHandlingActions() {
		return flowHandlingActionStatements;
	}
	public int getNumberofCheckedException() {
		return numberofCheckedException;
	}
	
//	public int getLogPrintDefaultStatements() {
//		return logPrintDefaultStatements;
//	}
	
	public int getNumberofMethodInvoke() {
		return numberofMethodInvoke;
	}
	public int getThrownStatements() {
		return thrownStatements;
	}
	
}

