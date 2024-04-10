/*
* Name: Braxton Jones, 5007333716, As3
* Description: Using Java this program will interpret 
a language
* Input: different expressions for the language
* Output: any write statement given
*/

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import javax.security.auth.Subject;

public class Main {
    // this global variable indicates if an error has occurred.
    public static boolean error = false;
    // this global symbol table represents the scope of variables.
    public static SymbolTable table = new SymbolTable();
    // this global variable indicates if a variable/value is returned
    // by a lambda function.
    public static Stack<Value> returnVariables = new Stack<>();

    // ===-----------------------------------------------------------------===
    // the AST clas is the super-class for abstract syntax trees.
    // Every AST node hast its own subclass.
    public static abstract class AST {
        protected String label = "EMPTY";
        protected LinkedList<AST> children = new LinkedList<>();

        public AST() { }

        public String getLabel() {
            return label;
        }

        public List<AST> getChildren() {
            return Collections.unmodifiableList(children);
        }

        public abstract void addChild(AST child);
    }

    // ===-----------------------------------------------------------------===
    // every AST node that is a not a Stmt is an Expr. These
    // represent actual computations that return something, such
    // as a Value object.
    public static class Expr extends AST {
        protected Value eval() {
            if (!error) {
                System.err.println("eval() not yet implemented for " + this.getClass().getSimpleName());
                error = true;
            }
            return new Value();
        }

        @Override
        public void addChild(AST child) {
            children.add(child);
        }
    }

    // ===-----------------------------------------------------------------===
    // an identifier such as the name of a variable or function.
    public static class Identifier extends Expr {
        protected String value;

        public Identifier(final String value) {
            this.value = value;
            this.label = "Identifier `" + value + "`";
        }

        public void setValue(final String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        @Override
        protected Value eval() {
            
            if (!error) {
                // CODE HERE
                Value result = table.lookup(getValue());
                return result;
            }   else {
            return new Value();
            }
        }
    }

    // ===-----------------------------------------------------------------===
    // a literal number in the program.
    public static class Number extends Expr {
        protected int value;

        public Number(final int value) {
            this.value = value;
            this.label = "Number `" + value + "`";
        }

        @Override
        protected Value eval() {
            return new Value(value);
        }
    }

    // ===-----------------------------------------------------------------===
    // A literal boolean value such as "true" or "false".
    public static class BoolExpr extends Expr {
        private final boolean value;

        public BoolExpr(final boolean value) {
            this.value = value;
            this.label = "Boolean `" + value + "`";
        }

        @Override
        protected Value eval() {
            // CODE HERE
            if (value == true) 
            return new Value(true);
            else if (value == false)
            return new Value(false);
            
            return new Value();
        }
    }

    // ===-----------------------------------------------------------------===
    // this gives the type to the different kinds of operators.
    public enum Oper {
        ADD, SUB, MUL, DIV, LT, GT, LE, GE, EQ, NE, AND, OR, NOT
    }

    // a binary operation for arithmetic operations such as +, -, *, or /.
    public static class ArithmeticOp extends Expr {
        private final Oper op;
        private final Expr left;
        private final Expr right;

        public ArithmeticOp(Expr left, Expr right, Oper op) {
            this.op = op;
            this.left = left;
            this.right = right;
            this.label = "ArithmeticOp '<left> <op> <right>'";
            addChild(left);
            addChild(right);
        }

        @Override
        protected Value eval() {
            final int l = left.eval().getNumber();
            final int r = right.eval().getNumber();
            switch (op) {
                case ADD:
                    return new Value(l + r);
                case SUB:
                    return new Value(l - r);
                case MUL:
                    return new Value(l * r);
                case DIV: {
                    if (r != 0) return new Value(l / r);
                    else if (!error) {
                        System.err.println("ERROR: Division by zero!");
                        error = true;
                    }
                }
            }
            return new Value();
        }
    }

    // ===-----------------------------------------------------------------===
    // a binary operator for comparison such as < or !=.
    public static class ComparisonOp extends Expr {
        private final Oper op;
        private final Expr left;
        private final Expr right;

        public ComparisonOp(Expr left, Expr right, Oper op) {
            this.left = left;
            this.right = right;
            this.op = op;
            this.label = "ComparisonOp `<left> <op> <right>`";
            addChild(left);
            addChild(right);
        }

        @Override
        protected Value eval() {
            final int l = left.eval().getNumber();
            final int r = right.eval().getNumber();
            // CODE HERE
            switch(op) {
                case EQ:
                    return new Value(l == r);
                case GE:
                    return new Value(l >= r);
                case LE:
                    return new Value(l <= r);
                case GT:
                    return new Value(l > r);
                case LT:
                    return new Value(l < r);
            }
            return new Value();
        }
    }

    // ===-----------------------------------------------------------------===
    // a binary operation for boolean logic such as and, or.
    public static class BooleanOp extends Expr {
        private final Oper op;
        private final Expr left;
        private final Expr right;

        public BooleanOp(Expr left, Expr right, Oper op) {
            this.left = left;
            this.right = right;
            this.op = op;
            this.label = "BooleanOp `<left> <op> <right>`";
            addChild(left);
            addChild(right);
        }

        @Override
        protected Value eval() {
            // TODO: students need to complete this
            final boolean l = left.eval().getBoolean();
            final boolean r = right.eval().getBoolean();
            // CODE HERE
            switch(op) {
                case AND:
                    return new Value(l && r);
                case OR:
                    return new Value(l || r);
            }
            return new Value();
        }
    }

    // ===-----------------------------------------------------------------===
    // this class represents a unary negation operation.
    public static class NegationOp extends Expr {
        private final Expr right;

        public NegationOp(Expr right) {
            this.right = right;
            this.label = "NegationOp `<op> <right>`";
            addChild(right);
        }

        @Override
        protected Value eval() {

            final int r = right.eval().getNumber();
            return new Value(-1 * r);
        }
    }

    // ===-----------------------------------------------------------------===
    // this class represents a unary not operator.
    public static class NotOp extends Expr {
        private final Expr right;

        public NotOp(Expr right) {
            this.right = right;
            this.label = "NotOp `<op> <right>`";
            addChild(right);
        }

        @Override
        protected Value eval() {

            final boolean r = right.eval().getBoolean();
            return new Value(!r);
        }
    }

    // ===-----------------------------------------------------------------===
    // a read expression.
    public static class Read extends Expr {
        public Read() {
            this.label = "Read";
        }

        @Override
        protected Value eval() {
            System.out.print("read> ");
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            try {
                final String val = reader.readLine();
                return new Value(Integer.parseInt(val));
            } catch (Exception ignored) {
            }
            return new Value();
        }
    }

    // ===-----------------------------------------------------------------===
    // a return expression.
    public static class ReturnExpr extends Expr {
        private final Expr value;

        public ReturnExpr(Expr value) {
            this.value = value;
            this.label = "ReturnExpr `ret := <expr>`";
            addChild(value);
        }

        @Override
        protected Value eval() {
            // TODO: students need to complete this
            final int l = value.eval().getNumber();
            if (!error) {
                // CODE HERE
                Main.returnVariables.push(value.eval());
            }
            return new Value();
        }
    }

    // ===-----------------------------------------------------------------===
    // a Stmt is anything that can be evaluated at the top level such
    // as I/O, assignments, and control structures. Note that the last
    // child of any statement is the next statement in sequence.
    public static class Stmt extends AST {
        private Stmt next;

        public Stmt() {
            next = new NullStmt();
            children.add(next);
        }

        public Stmt(Stmt next) {
            if (next != null) children.add(next);
            this.next = next;
        }

        // this method is for building sequences of statements by the
        // parser. It takes two statements and appends one at the end
        // of the other. The returned value is a reference to the new
        // statement representing the sequence.
        public static Stmt append(Stmt a, Stmt b) {
            if (!a.hasNext()) return b;
            Stmt last = a;
            while (last.getNext().hasNext())
                last = last.getNext();
            last.setNext(b);
            return a;
        }

        public Stmt getNext() {
            return next;
        }

        public void setNext(Stmt next) {
            children.removeLast();
            children.add(next);
            this.next = next;
        }

        public boolean hasNext() {
            return next != null;
        }

        public void exec() {
            if (!error) {
                System.err.println("exec() not yet implemented for " + this.getClass().getSimpleName());
                error = true;
            }
        }

        @Override
        public void addChild(AST child) {
            // this inserts before the last item in the list
            children.add(children.size() - 1, child);
        }
    }

    // ===-----------------------------------------------------------------===
    // this class is necessary to terminate a sequence of statements.
    public static class NullStmt extends Stmt {
        public NullStmt() {
            super(null);
            this.label = "NullStmt `null`";
        }

        // nothing to execute!
        @Override
        public void exec() { }
    }

    // ===-----------------------------------------------------------------===
    // this class is a statement for a block of code; i.e., code enclosed
    // in curly braces { and }. This is where scopes will begin and end.
    public static class Block extends Stmt {
        private Stmt body;

        public Block(Stmt body) {
            this.body = body;
            this.label = "Block `{ <stmt> }`";
            addChild(body);
        }

        public void setBody(Stmt body) {
            this.body = body;
        }

        public Stmt getBody() {
            return body;
        }

        @Override
        public void exec() {
            // TODO: students need to complete this
            // CODE HERE
            table.openScope();
            body.exec();
            table.closeScope();
            getNext().exec();                 
        }
    }

    // ===-----------------------------------------------------------------===
    // this is a class for "if" and "if-else" statements.
    public static class IfStmt extends Stmt {
        private Expr condition;
        private Stmt ifBlock;
        private Stmt elseBlock;

        public IfStmt(Expr condition, Stmt ifBlock, Stmt elseBlock) {
            this.condition = condition;
            this.ifBlock = ifBlock;
            this.elseBlock = elseBlock;
            this.label = "IfStmt `if <expr> { <stmt> }`";
            addChild(condition);
            if (ifBlock != null)
                addChild(ifBlock);
            if (elseBlock != null)
                addChild(elseBlock);
            else
                addChild(new NullStmt());
        }

        public void setCondition(Expr condition) {
            this.condition = condition;
        }

        public void setIfBlock(Stmt ifBlock) {
            this.ifBlock = ifBlock;
        }

        public void setElseBlock(Stmt elseBlock) {
            this.elseBlock = elseBlock;
        }

        public Expr getCondition() {
            return condition;
        }

        public Stmt getIfBlock() {
            return ifBlock;
        }

        public Stmt getElseBlock() {
            return elseBlock;
        }

        @Override
        public void exec() {
            // TODO: students need to complete this
            if (!error) {
                // CODE HERE
                if (condition.eval().getBoolean()) {
 
                    ifBlock.exec();
                    
                }   else {
                    if (elseBlock != null) {
                        
                        elseBlock.exec();
                        
                    }

                }
            }
            getNext().exec();
        }
    }

    // ===-----------------------------------------------------------------===
    // this class is for "while" statements.
    public static class WhileStmt extends Stmt {
        private Expr condition;
        private Stmt body;

        public WhileStmt(Expr condition, Stmt body) {
            this.condition = condition;
            this.body = body;
            this.label = "WhileStmt `while <expr> { <stmt> }`";
            addChild(condition);
            if (body != null)
                addChild(body);
        }

        public void setCondition(Expr condition) {
            this.condition = condition;
        }

        public void setBody(Stmt body) {
            this.body = body;
        }

        public Expr getCondition() {
            return condition;
        }

        public Stmt getBody() {
            return body;
        }

        @Override
        public void exec() {
            // TODO: students need to complete this
            if (!error) {
                // CODE HERE
                
                while (condition.eval().getBoolean()) {
                    body.exec();
                }
                
            }
            getNext().exec();
        }
    }

    // ===-----------------------------------------------------------------===
    // this is a "new" statement creates a new binding of the variable
    // to the stated value
    public static class AutoStmt extends Stmt {
        private Identifier lhs;
        private Expr rhs;

        public AutoStmt(Identifier lhs, Expr rhs) {
            this.lhs = lhs;
            this.rhs = rhs;
            this.label = "AutoStmt `new <var> := <expr>`";
            addChild(lhs);
            addChild(rhs);
        }

        public void setLhs(Identifier lhs) {
            this.lhs = lhs;
        }

        public void setRhs(Expr rhs) {
            this.rhs = rhs;
        }

        public Identifier getLhs() {
            return lhs;
        }

        public Expr getRhs() {
            return rhs;
        }

        @Override
        public void exec() {
            if (!error) {
                table.bind(lhs.getValue(), rhs.eval());
            }
            getNext().exec();
        }
    }

    // ===-----------------------------------------------------------------===
    // this is an assignment statement. This represents RE-binding in
    // the symbol table.
    public static class AssignStmt extends Stmt {
        private Identifier lhs;
        private Expr rhs;

        public AssignStmt(Identifier lhs, Expr rhs) {
            this.lhs = lhs;
            this.rhs = rhs;
            this.label = "AssignStmt `<var> := <expr> `";
            addChild(lhs);
            addChild(rhs);
        }

        public void setLhs(Identifier lhs) {
            this.lhs = lhs;
        }

        public void setRhs(Expr rhs) {
            this.rhs = rhs;
        }

        public Identifier getLhs() {
            return lhs;
        }

        public Expr getRhs() {
            return rhs;
        }

        @Override
        public void exec() {
            // TODO: complete by implementation
            if (!error) {
                // CODE HERE
                
                    table.rebing(lhs.getValue(), rhs.eval());
                
                // System.out.println("INFO: replacing the value `" + val + "` in `" + lhs.getValue() + "`");

            }
            getNext().exec();
        }
    }

    // ===-----------------------------------------------------------------===
    // this is a write statement.
    public static class Write extends Stmt {
        private final Expr value;

        public Write(Expr value) {
            this.value = value;
            this.label = "Write `write <value>`";
            addChild(value);
        }

        @Override
        public void exec() {
            Value val = value.eval();
            if (!error)
                val.writeTo();
            getNext().exec();
        }
    }

    // ===-----------------------------------------------------------------===
    // an expression statement that consists of a single expression.
    public static class ExprStmt extends Stmt {
        private final Expr value;

        public ExprStmt(Expr value) {
            this.value = value;
            this.label = "ExprStmt `<expr>`";
            addChild(value);
        }

        @Override
        public void exec() {
            if (!error)
                value.eval();
            getNext().exec();
        }
    }

    // ===-----------------------------------------------------------------===
    // a lambda expression consists of a parameter name and a body.
    public static class Lambda extends Expr {
        private final Identifier variable;
        private final Stmt body;

        public Lambda(Identifier variable, Stmt body) {
            this.variable = variable;
            this.body = body;
            this.label = "Lambda `lambda <var> { <stmt> }`";
            addChild(variable);
            addChild(body);
        }

        public String getVariable() {
            return variable.getValue();
        }

        public Stmt getBody() {
            return body;
        }

        @Override
        protected Value eval() {
            // TODO: students must complete this
            if (!error) {
                // CODE HERE
                
                return new Value(new Lambda(variable, body));
            }
            return new Value();
        }
    }

    // ===-----------------------------------------------------------------===
    // a function call consists of the function name, and the actual
    // argument. Note that all functions are unary.
    public static class Call extends Expr {
        private final Expr funExpr;
        private final Expr arg;

        public Call(Expr funExpr, Expr arg) {
            this.funExpr = funExpr;
            this.arg = arg;
            this.label = "Call `<fun> @ <arg>`";
            addChild(funExpr);
            addChild(arg);
        }

        @Override
        protected Value eval() {
            // TODO: students must complete this
            if (!error) {
                // CODE HERE
                if (funExpr.eval().getType() == Type.FUN_T) {
                    Lambda lambda = funExpr.eval().getLambda();
                    table.openScope();
                    table.bind(lambda.getVariable(), arg.eval());
                    // execute function body
                    lambda.getBody().exec();

                    Value retVal = new Value();
                    // capture return value
                    if (!returnVariables.isEmpty()) {
                        retVal = returnVariables.pop();
                    }

                    table.closeScope();
                    return retVal;
                }   else {
                    System.err.println("ERROR: Attempting to call a non-function.");
                    error = true;
                }
                
            }
            return new Value();
        }
    }

    // ===-----------------------------------------------------------------===
    // this gives the type of what's stored in the Value object.
    public enum Type {
        NUM_T, BOOL_T, FUN_T, NONE_T;
    }

    public static class Value {
        private Object val;
        private final Type type;

        public Value() {
            type = Type.NONE_T;
        }

        public Value(int n) {
            val = n;
            type = Type.NUM_T;
        }

        public Value(boolean b) {
            val = b;
            type = Type.BOOL_T;
        }

        public Value(Lambda l) {
            val = l;
            type = Type.FUN_T;
        }

        public Type getType() {
            return type;
        }

        public int getNumber() {
            return (int) val;
        }

        public boolean getBoolean() {
            return (boolean) val;
        }

        public Lambda getLambda() {
            return (Lambda) val;
        }

        public void writeTo() {
            switch (type) {
                case NUM_T, BOOL_T -> System.out.println(val);
                case FUN_T -> System.out.println("lambda expression");
                case NONE_T -> System.out.println("Unset value!");
            }
        }

        @Override
        public String toString() {
            return switch (type) {
                case NUM_T, BOOL_T -> String.valueOf(val);
                case FUN_T -> "lambda expression";
                case NONE_T -> "Unset value!";
            };
        }
    }

    // ===-----------------------------------------------------------------===
    // this class represents a simple global symbol table.
    public static class SymbolTable {
        private final Map<String, Stack<Value>> binding = new HashMap<>();
        private final Stack<Set<String>> scopeVariables = new Stack<>();

        public SymbolTable() { }

        // returns the value bound to the given name.
        Value lookup(final String name) {
            // TODO: write the implementation
            // check if the current scope has variable
        for (String names : scopeVariables.peek()) {
            if (names.equals(name) ) {
                Stack<Value> stack = binding.get(name);
                return stack.peek();
            }
        }

        if(scopeVariables.peek().isEmpty() || !scopeVariables.peek().contains(name)) {
        // if empty check for any prior keys for scope handling
            if (binding.containsKey(name)) {
                Stack<Value> stack = binding.get(name);
                return stack.peek();
            }
        } else {
            error = true;
            System.out.println("ERROR: No binding for variable `" + name + "` exists!");
        }
            return new Value();
        }   

        // create a new name-value binding.
        public void bind(final String name, Value val) {
            // TODO: write the implementation

            // if no variables in scope or scope doesnt contain a name of variable 
            if (scopeVariables.peek().isEmpty() || !scopeVariables.peek().contains(name)) {
                // put name and a new stack into binding table
                
                // if there is already a key push onto (scope handling)
                if (binding.containsKey(name)) {
                    Stack<Value> stack = binding.get(name);
                    stack.push(val);
                }   else {
                binding.put(name, new Stack<>());
                // push a value onto the name in the table
                binding.get(name).push(val);
                }
                // add the name to the scope variables at the top of stack
                scopeVariables.peek().add(name);
            }   
            else {
                error = true;
                System.out.println("ERROR: Variable `" + name + "` already bound!");
            }
        }

        // re-defines the value bound to the given name.
        void rebing(final String name, Value val) {
            // TODO: write the implementation
            // check for variable in scope
            if (binding.containsKey(name)) {
                // change name at top of binding table to val
                // pop the current value out of stack then push new one
                binding.get(name).pop();
                binding.get(name).push(val);

            }   else {
                error = true;
                System.out.println("ERROR: Cannot rebind `" + name + "` because it is not bound!");
                }
        }

        public void openScope() {
            // TODO: write the implementation
            System.out.println("INFO: Opening scope!");
            // CODE HERE
            scopeVariables.push(new HashSet<>());
        }

        public void closeScope() {
            // TODO: write the implementation
            System.out.println("INFO: Closing scope!");
            // CODE HERE

            if (!scopeVariables.isEmpty()) {
                for (String name : scopeVariables.peek()) {
                    if (!scopeVariables.peek().isEmpty()) {
                    binding.get(name).pop();
                    }
                }
                scopeVariables.pop();
            }
            
        }
    }

    public static void main(String[] args) throws IOException {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        Lexer lexer;
        Parser parser;
        Stmt stmt = null;
        Stmt ast = null;
        String input = null;
        Main.table.openScope();
        while (true) {
            Main.error = false;
            System.out.print("brain> ");
            input = reader.readLine();
            if ("quit".equals(input)) break;
            if ("tree".equals(input)) {
                if (ast != null)
                    System.out.println(PrettyPrinter.printAST(ast));
                continue;
            }
            lexer = new Lexer(input);
            List<Token> tokens = lexer.getTokens();
//            System.out.println(tokens);
            parser = new Parser(tokens);
            stmt = parser.init();
            if (ast == null) ast = stmt;
            else ast.addChild(stmt);
            if (stmt == null && !Main.error) break;
            else if (stmt != null)
                stmt.exec();
        }
        Main.table.closeScope();
        System.out.println("Good bye!");
    }
}