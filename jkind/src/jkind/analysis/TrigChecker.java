package jkind.analysis;

import jkind.StdErr;
import jkind.lustre.BinaryExpr;
import jkind.lustre.Equation;
import jkind.lustre.Expr;
import jkind.lustre.Node;
import jkind.lustre.Program;
import jkind.lustre.UnaryExpr;
import jkind.lustre.UnaryOp;
import jkind.lustre.visitors.ExprIterVisitor;

public class TrigChecker extends ExprIterVisitor {
	private final Level level;
	private boolean passed;
	private ConstantAnalyzer constantAnalyzer;

	public TrigChecker(Level level) {
		this.level = level;
		this.passed = true;
	}

	public static boolean check(Program program, Level level) {
		return new TrigChecker(level).visitProgram(program);
	}

	public static boolean isLinear(Program program) {
		return new TrigChecker(Level.IGNORE).visitProgram(program);
	}

	public static boolean isLinear(Node node) {
		return isLinear(new Program(node));
	}

	public boolean visitProgram(Program program) {
		constantAnalyzer = new ConstantAnalyzer(program);

		for (Node node : program.nodes) {
			visitNode(node);
		}

		return passed;
	}

	public void visitNode(Node node) {
		for (Equation eq : node.equations) {
			eq.expr.accept(this);
		}
		for (Expr e : node.assertions) {
			e.accept(this);
		}
	}


	@Override
	public Void visit(UnaryExpr e) {
		switch (e.op){
		case EXP: 
		case LOG:
		case SQRT:
		case POW:
		case SIN:
		case COS: 
		case TAN:
		case ARCSIN: 
		case ARCCOS:
		case ARCTAN:
		case SINH: 
		case COSH:
		case TANH:
		case ARCTAN2:
		case MATAN: 
		{ 
			if (!isConstant(e.expr)) {
				StdErr.output(level, e.location, "use of trigonometric function with non-constant argument");
				passed = false;
			}
		}
		default: break;
		}
		return null;
	}
	private boolean isConstant(Expr e) {
		return e.accept(constantAnalyzer);
	}
}