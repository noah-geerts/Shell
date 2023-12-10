package uk.ac.ucl.shell;
/**
 * Factory class used to generate Application objects
 */
public class AppFactory {

	/**
	 * generates an Application object corresponding to the name input
	 * @param name The name of the desired Application
	 * @return The generated Application object
	 * @throws IllegalArgumentException if the name is null or empty
	 * @throws RuntimeException if there is no Application with the given name
	 */
	public Application generateApp(String name) {
		if (name == null || name.isEmpty()) {
			throw new IllegalArgumentException("Application name cannot be null or empty");
		}
		switch(name) {
			case "cd":
				return new Cd();
			case "pwd":
				return new Pwd();
			case "ls":
				return new Ls();
			case "cat":
				return new Cat();
			case "echo":
				return new Echo();
			case "head":
				return new Head();
			case "tail":
				return new Tail();
			case "grep":
				return new Grep();
			case "cut":
				return new Cut();
			case "find":
				return new Find();
			case "uniq":
				return new Uniq();
			case "sort":
				return new Sort();
			default:
				throw new RuntimeException(name + ": unknown application");
		}
	}
	
}
