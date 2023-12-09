package uk.ac.ucl.shell;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class AppFactoryTest {

	AppFactory a = new AppFactory();
	
	@Test
	public void testGenerateAppEmptyNullString() {
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> {
			a.generateApp(null);
        });
		assertTrue(e.getMessage().equals("Application name cannot be null or empty"));
		
		IllegalArgumentException e2 = assertThrows(IllegalArgumentException.class, () -> {
			a.generateApp("");
        });
		assertTrue(e2.getMessage().equals("Application name cannot be null or empty"));
		
	}
	
	@Test
	public void testGenerateAppNonexistentAppName() {
		RuntimeException e = assertThrows(RuntimeException.class, () -> {
			a.generateApp("nonexistentAppName");
        });
		assertTrue(e.getMessage().equals("nonexistentAppName: unknown application"));
	}
	
	@Test
	public void testNormalAppGeneration() {
		Application app = a.generateApp("cd");
		assertTrue(app instanceof Cd);
		
		app = a.generateApp("pwd");
		assertTrue(app instanceof Pwd);
		
		app = a.generateApp("ls");
		assertTrue(app instanceof Ls);
		
		app = a.generateApp("head");
		assertTrue(app instanceof Head);
		
		app = a.generateApp("tail");
		assertTrue(app instanceof Tail);
		
		app = a.generateApp("grep");
		assertTrue(app instanceof Grep);
		
		app = a.generateApp("cut");
		assertTrue(app instanceof Cut);
		
		app = a.generateApp("find");
		assertTrue(app instanceof Find);
		
		app = a.generateApp("uniq");
		assertTrue(app instanceof Uniq);
		
		app = a.generateApp("sort");
		assertTrue(app instanceof Sort);
	}
}
