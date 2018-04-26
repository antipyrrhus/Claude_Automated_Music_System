package customapi;

/**
 * An abstract Superclass for CustomFunctions class. This is only here
 * in order to enable dynamic re-loading of CustomFunctions class (so that the end user can edit the 
 * CustomFunctions class and can dynamically re-load the updated GUI without having to re-start the entire program)
 * and to be able to cast it as its SuperClass.
 * 
 * This Superclass is necessary because apparently with dynamic reloading and instantiation thereof,
 * you must cast it either to a superclass or to an interface. In other words, the CustomFunctions class
 * cannot be cast to itself.
 */
public abstract class SuperCustomFunctions {
	
	//These abstract methods are invoked from other classes (e.g. CustomFunctionsPane), and will automatically trigger
	//the subclass (CustomFunctions)'s methods.
	public abstract String getCommandsStr(int i);
	public abstract void runCommand(String s);
}
