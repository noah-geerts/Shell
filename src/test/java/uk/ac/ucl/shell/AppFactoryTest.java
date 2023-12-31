package uk.ac.ucl.shell;


import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class AppFactoryTest {

    AppFactory a = new AppFactory();

    @Test
    public void testGenerateAppEmptyNullString() {
        try {
            a.generateApp(null);
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().equals("Application name cannot be null or empty"));
        }

        try {
            a.generateApp("");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().equals("Application name cannot be null or empty"));
        }

    }

    @Test
    public void testGenerateAppNonexistentAppName() {
        try {
            a.generateApp("nonexistentAppName");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().equals("nonexistentAppName: unknown application"));
        }
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

        app = a.generateApp("mkdir");
        assertTrue(app instanceof Mkdir);
    }
}
