grammar ShellGrammar;

/*
 * Parser Rules
 */

root : seq | command;

seq	: 	left = command ';' right = command	#seq1
		| left = seq ';' right = command	#seq2;

command  : left=command '|' right=command   #pipeCommand
         | (NONSPECIAL)+					#atomicCommand;


/*
 * Lexer Rules
 */

NONSPECIAL : ~[;|]+;