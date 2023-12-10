package uk.ac.ucl.shell;

/**
 * This class extends ShellGrammarBaseVisitor to convert parsed shell grammar contexts into Command objects.
 */
public class CommandConverter extends ShellGrammarBaseVisitor<Command>{

    /**
     * Visits a Seq1 context, creates a Seq command with left and right commands, and default input/output streams.
     * A Seq1 context consists of two non-Seq commands separated by a `;` as defined in ShellGrammar
     * @param ctx The Seq1 context to be visited.
     * @return A Seq command representing the sequence of left and right commands.
     */
    public Command visitSeq1(ShellGrammarParser.Seq1Context ctx) {
        Command left = visit(ctx.left);
        Command right = visit(ctx.right);
        return new Seq(left, right, "", System.out);
    }
    
    /**
     * Visits a Seq2 context, creates a Seq command with left and right commands, and default input/output streams.
     * A Seq2 context consists of a Seq command followed by a non-Seq command separated by a `;`
     * @param ctx The Seq2 context to be visited.
     * @return A Seq command representing the sequence of left and right commands.
     */
    public Command visitSeq2(ShellGrammarParser.Seq2Context ctx) {
        Command left = visit(ctx.left);
        Command right = visit(ctx.right);
        return new Seq(left, right, "", System.out);
    }
    
    /**
     * Visits a PipeCommand context, creates a Pipe command with left and right commands, and default input/output streams.
     * @param ctx The PipeCommand context to be visited.
     * @return A Pipe command representing the piping of left and right commands.
     */
    public Command visitPipeCommand(ShellGrammarParser.PipeCommandContext ctx) {
        Command left = visit(ctx.left);
        Command right = visit(ctx.right);
        return new Pipe(left, right, "", System.out);
    }
    
    /**
     * Visits an AtomicCommand context, creates a Call command with the atomic command text and default input/output streams.
     * @param ctx The AtomicCommand context to be visited.
     * @return A Call command representing the execution of an atomic command.
     */
    public Command visitAtomicCommand(ShellGrammarParser.AtomicCommandContext ctx) {
        return new Call(ctx.getText(), "", System.out);
    }
}
