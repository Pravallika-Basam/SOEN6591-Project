package soen6591.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

public class AntiPatternDetectorHandler extends AbstractHandler {

	private static final String CONSOLE_NAME = "SOEN 6591 Antipattern Detection Analysis";
	private static MessageConsole myConsole;
	private static MessageConsoleStream out;
	
	/*
	 * The AntiPatternDetectorHandler class is responsible for handling user commands 
	 * for detecting anti-patterns in code. It extends the AbstractHandler class and 
	 * overrides its execute() method to execute the detection process. 
	 * 
	 * The execute() method retrieves the console where the detection process messages 
	 * will be displayed, creates an instance of the AntiPatternDetector class, 
	 * and calls its execute() method.
	 * 
	 * The findConsole() method is responsible for finding or creating the console 
	 * where the detection process messages will be displayed.
	 * 
	 * The printMessage() method is used to print messages into the Debug view, 
	 * not just in the console.
	 */

	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		AntiPatternDetectorHandler.myConsole = findConsole(CONSOLE_NAME);
		AntiPatternDetectorHandler.out = myConsole.newMessageStream();
		AntiPatternDetector detectException = new AntiPatternDetector();
		detectException.execute(event);
		
		return null;
	}
	
	private MessageConsole findConsole(String name) {
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager conMan = plugin.getConsoleManager();
		IConsole[] existing = conMan.getConsoles();
		
		for (int i = 0; i < existing.length; i++)
		   if (name.equals(existing[i].getName()))
		      return (MessageConsole) existing[i];
		
		//no console found, so create a new one
		MessageConsole myConsole = new MessageConsole(name, null);
		conMan.addConsoles(new IConsole[]{myConsole});
		return myConsole;
	}
	
	// To print messages into the Debug view, not just in the console here.
	static public void printMessage(String message) {
		out.println(message);
	}
}
