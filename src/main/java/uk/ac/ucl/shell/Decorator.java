package uk.ac.ucl.shell;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public abstract class Decorator implements Application {
    private Application decoratedApp;

    public Decorator(Application decoratedApp) {
        this.decoratedApp = decoratedApp;
    }

    @Override
    public void exec(ArrayList<String> appArgs, String input, OutputStreamWriter writer) throws IOException {
        decoratedApp.exec(appArgs, input, writer);
    }
}

class UnsafeDecorator extends Decorator
{
    public UnsafeDecorator(Application decoratedApp)
    {
        super(decoratedApp);
    }
    public void exec(ArrayList<String> appArgs, String input, OutputStreamWriter writer) throws IOException
    {
        try
        {
            super.exec(appArgs, input, writer);
        }
        catch (Exception e)
        {
            writer.write(e.getMessage());
            writer.write(System.getProperty("line.separator"));
            writer.flush();
        }
    }
}