package uk.ac.ucl.shell;

public class AppFactory {

	public static Application generateApp(String name) {
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
			default:
				throw new RuntimeException(name + ": unknown application");
		}
	}
	
}
