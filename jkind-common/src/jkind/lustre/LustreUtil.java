package jkind.lustre;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import jkind.lustre.builders.NodeBuilder;

public class LustreUtil {

	/* Binary Expressions */

	public static Expr and(Expr left, Expr right) {
		return new BinaryExpr(left, BinaryOp.AND, right);
	}

	public static Expr or(Expr left, Expr right) {
		return new BinaryExpr(left, BinaryOp.OR, right);
	}

	public static Expr implies(Expr left, Expr right) {
		return new BinaryExpr(left, BinaryOp.IMPLIES, right);
	}

	public static Expr xor(Expr left, Expr right) {
		return new BinaryExpr(left, BinaryOp.XOR, right);
	}

	public static Expr arrow(Expr left, Expr right) {
		return new BinaryExpr(left, BinaryOp.ARROW, right);
	}

	public static Expr less(Expr left, Expr right) {
		return new BinaryExpr(left, BinaryOp.LESS, right);
	}

	public static Expr lessEqual(Expr left, Expr right) {
		return new BinaryExpr(left, BinaryOp.LESSEQUAL, right);
	}

	public static Expr greater(Expr left, Expr right) {
		return new BinaryExpr(left, BinaryOp.GREATER, right);
	}

	public static Expr greaterEqual(Expr left, Expr right) {
		return new BinaryExpr(left, BinaryOp.GREATEREQUAL, right);
	}

	public static Expr equal(Expr left, Expr right) {
		return new BinaryExpr(left, BinaryOp.EQUAL, right);
	}

	public static Expr notEqual(Expr left, Expr right) {
		return new BinaryExpr(left, BinaryOp.NOTEQUAL, right);
	}

	public static Expr plus(Expr left, Expr right) {
		return new BinaryExpr(left, BinaryOp.PLUS, right);
	}

	public static Expr minus(Expr left, Expr right) {
		return new BinaryExpr(left, BinaryOp.MINUS, right);
	}

	public static Expr multiply(Expr left, Expr right) {
		return new BinaryExpr(left, BinaryOp.MULTIPLY, right);
	}

	public static Expr mod(Expr left, Expr right) {
		return new BinaryExpr(left, BinaryOp.MODULUS, right);
	}

	public static Expr intDivide(Expr left, Expr right) {
		return new BinaryExpr(left, BinaryOp.INT_DIVIDE, right);
	}

	public static Expr divide(Expr left, Expr right) {
		return new BinaryExpr(left, BinaryOp.DIVIDE, right);
	}

	/* Unary Expressions */

	public static Expr negative(Expr expr) {
		return new UnaryExpr(UnaryOp.NEGATIVE, expr);
	}

	public static Expr not(Expr expr) {
		return new UnaryExpr(UnaryOp.NOT, expr);
	}

	public static Expr pre(Expr expr) {
		return new UnaryExpr(UnaryOp.PRE, expr);
	}

	public static Expr optimizeNot(Expr expr) {
		if (expr instanceof UnaryExpr) {
			UnaryExpr ue = (UnaryExpr) expr;
			if (ue.op == UnaryOp.NOT) {
				return ue.expr;
			}
		}
		return new UnaryExpr(UnaryOp.NOT, expr);
	}

	/* IdExpr Expressions */

	public static IdExpr id(String id) {
		return new IdExpr(id);
	}

	public static IdExpr id(VarDecl vd) {
		return new IdExpr(vd.id);
	}

	/* Literal Expressions */

	public static RealExpr real(BigInteger bi) {
		return new RealExpr(new BigDecimal(bi));
	}

	public static IntExpr integer(int iv) {
		return new IntExpr(iv);
	}

	public static Expr TRUE = new BoolExpr(true);
	public static Expr FALSE = new BoolExpr(false);

	/* Cast Expressions */

	public static Expr castInt(Expr expr) {
		return new CastExpr(NamedType.INT, expr);
	}

	public static Expr castReal(Expr expr) {
		return new CastExpr(NamedType.REAL, expr);
	}

	/* Miscellaneous Expressions */

	public static Expr and(List<Expr> conjuncts) {
		return conjuncts.stream().reduce((acc, e) -> and(acc, e)).orElse(TRUE);
	}

	public static Expr or(List<Expr> disjuncts) {
		return disjuncts.stream().reduce((acc, e) -> and(acc, e)).orElse(FALSE);
	}

	public static Expr ite(Expr cond, Expr thenExpr, Expr elseExpr) {
		return new IfThenElseExpr(cond, thenExpr, elseExpr);
	}

	public static Expr typeConstraint(String id, Type type) {
		if (type instanceof SubrangeIntType) {
			return subrangeConstraint(id, (SubrangeIntType) type);
		} else if (type instanceof EnumType) {
			return enumConstraint(id, (EnumType) type);
		} else {
			return null;
		}
	}

	public static Expr subrangeConstraint(String id, SubrangeIntType subrange) {
		return boundConstraint(id, new IntExpr(subrange.low), new IntExpr(subrange.high));
	}

	public static Expr enumConstraint(String id, EnumType et) {
		return boundConstraint(id, new IntExpr(0), new IntExpr(et.values.size() - 1));
	}

	private static Expr boundConstraint(String id, Expr low, Expr high) {
		return and(lessEqual(low, id(id)), lessEqual(id(id), high));
	}

	/* Decls */

	public static VarDecl varDecl(String name, Type type) {
		return new VarDecl(name, type);
	}

	public static Equation eq(IdExpr id, Expr expr) {
		return new Equation(id, expr);
	}

	/* Nodes */

	public static Node historically() {
		return historically("historically");
	}

	public static Node historically(String name) {
		NodeBuilder historically = new NodeBuilder(name);

		IdExpr signal = id("signal");
		historically.addInput(varDecl(signal.id, NamedType.BOOL));

		IdExpr holds = id("holds");
		historically.addOutput(varDecl(holds.id, NamedType.BOOL));

		// historically: holds = signal and (true -> pre holds);
		Equation equation = eq(holds, and(signal, arrow(TRUE, pre(holds))));
		historically.addEquation(equation);

		return historically.build();
	}

	public static Node once() {
		return once("once");
	}

	public static Node once(String name) {
		NodeBuilder once = new NodeBuilder(name);

		IdExpr signal = id("signal");
		once.addInput(varDecl(signal.id, NamedType.BOOL));

		IdExpr holds = id("holds");
		once.addOutput(varDecl(holds.id, NamedType.BOOL));

		// once: holds = signal or (false -> pre holds);
		Equation equation = eq(holds, or(signal, arrow(FALSE, pre(holds))));
		once.addEquation(equation);

		return once.build();
	}

	public static Node since() {
		return since("since");
	}

	public static Node since(String name) {
		NodeBuilder since = new NodeBuilder(name);

		IdExpr a = id("a");
		since.addInput(varDecl(a.id, NamedType.BOOL));
		
		IdExpr b = id("b");
		since.addInput(varDecl(b.id, NamedType.BOOL));

		IdExpr holds = id("holds");
		since.addOutput(varDecl(holds.id, NamedType.BOOL));

		// since: holds = b or (a and (false -> pre holds))
		Equation equation = eq(holds, or(b,and(a,arrow(FALSE,pre(holds)))));
		since.addEquation(equation);

		return since.build();
	}
	
	public static Node triggers() {
		return triggers("triggers");
	}

	public static Node triggers(String name) {
		NodeBuilder triggers = new NodeBuilder(name);

		IdExpr a = id("a");
		triggers.addInput(varDecl(a.id, NamedType.BOOL));
		
		IdExpr b = id("b");
		triggers.addInput(varDecl(b.id, NamedType.BOOL));

		IdExpr holds = id("holds");
		triggers.addOutput(varDecl(holds.id, NamedType.BOOL));

		// triggers: holds = b and (a or (true -> pre holds))
		Equation equation = eq(holds, and(b,or(a,arrow(TRUE,pre(holds)))));
		triggers.addEquation(equation);

		return triggers.build();
	}
}
