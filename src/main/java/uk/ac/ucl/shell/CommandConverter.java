package uk.ac.ucl.shell;

public class CommandConverter extends ShellGrammarBaseVisitor<Command>{

	public Command visitSeq1(ShellGrammarParser.Seq1Context ctx) {
		Command left = visit(ctx.left);
		Command right = visit(ctx.right);
		return new Seq(left, right, "", System.out);
	}
	
	public Command visitSeq2(ShellGrammarParser.Seq2Context ctx) {
		Command left = visit(ctx.left);
		Command right = visit(ctx.right);
		return new Seq(left, right, "", System.out);
	}
	
	public Command visitPipeCommand(ShellGrammarParser.PipeCommandContext ctx) {
		Command left = visit(ctx.left);
		Command right = visit(ctx.right);
		return new Pipe(left, right, "", System.out);
	}
	
	public Command visitAtomicCommand(ShellGrammarParser.AtomicCommandContext ctx) {
		return new Call(ctx.getText(), "", System.out);
	}
	
}
