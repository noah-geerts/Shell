package uk.ac.ucl.shell;

public class ApplicationException extends RuntimeException {
    public ApplicationException(String message) {
        super(message);
    }
}
class CatException extends ApplicationException {
    public CatException(String message) {
        super("cat: " + message);
    }
}
class CdException extends ApplicationException {
    public CdException(String message) {
        super("cd: " + message);
    }
}

class CutException extends ApplicationException {
    public CutException(String message) {
        super("cut: " + message);
    }
}
class EchoException extends ApplicationException {
    public EchoException(String message) {
        super("echo: " + message);
    }
}
class FindException extends ApplicationException {
    public FindException(String message) {
        super("find: " + message);
    }
}
class GrepException extends ApplicationException {
    public GrepException(String message) {
        super("grep: " + message);
    }
}
class HeadException extends ApplicationException {
    public HeadException(String message) {
        super("head: " + message);
    }
}
class LsException extends ApplicationException {
    public LsException(String message) {
        super("ls: " + message);
    }
}
class PwdException extends ApplicationException {
    public PwdException(String message) {
        super("pwd: " + message);
    }
}
class SortException extends ApplicationException {
    public SortException(String message) {
        super("sort: " + message);
    }
}
class TailException extends ApplicationException {
    public TailException(String message) {
        super("tail: " + message);
    }
}
class UniqException extends ApplicationException {
    public UniqException(String message) {
        super("uniq: " + message);
    }
}