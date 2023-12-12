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
		return switch (name) {
			case "cd" -> new Cd();
			case "pwd" -> new Pwd();
			case "ls" -> new Ls();
			case "cat" -> new Cat();
			case "echo" -> new Echo();
			case "head" -> new Head();
			case "tail" -> new Tail();
			case "grep" -> new Grep();
			case "cut" -> new Cut();
			case "find" -> new Find();
			case "uniq" -> new Uniq();
			case "sort" -> new Sort();
			case "mkdir" -> new Mkdir();
			case "touch" -> new Touch();
			default -> throw new RuntimeException(name + ": unknown application");
		};
	}
	
}
