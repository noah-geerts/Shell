package uk.ac.ucl.shell;

public class AppFactory {

	public static Application generateApp(String name) {
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
			default -> throw new RuntimeException(name + ": unknown application");
		};
	}
	
}
