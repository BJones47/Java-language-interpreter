import java.util.List;

public class Parser {
    // from where do we get the tokens
    private final List<Token> input;
    // the current lookahead token
    private Token lookahead;
    private int p = 0;

    public Parser(List<Token> input) {
        this.input = input;
        consume();
    }

    // lookahead token type matches x, consume & return else error.
    public void match(int x) {
        if (lookahead.type == x) consume();
        else throw new Error("expecting " + Lexer.getTokenName(x) + ", but found " + lookahead);
    }

    public void consume() {
        if (p >= input.size() - 1) lookahead = input.get(input.size() - 1);
        else lookahead = input.get(p);
        ++p;
    }

    public Token peek(int LA) {
        if (p + LA - 1 >= input.size()) return input.get(p - 1);
        return input.get(p + LA - 1);
    }

    public Main.Stmt init() {
        return stmt();
    }

    public Main.Block block() {
        if (lookahead.type == Lexer.LC) {
            match(Lexer.LC);
            Main.Block block = new Main.Block(stmtList());
            match(Lexer.RC);
            return block;
        }
        return null;
    }

    public Main.Stmt stmtList() {
        Main.Stmt next = new Main.NullStmt();
        while (lookahead.type == Lexer.AUTO ||
                lookahead.type == Lexer.IDENT ||
                lookahead.type == Lexer.WRITE ||
                lookahead.type == Lexer.RETURN ||
                lookahead.type == Lexer.IF ||
                lookahead.type == Lexer.IF_ELSE ||
                lookahead.type == Lexer.WHILE ||
                lookahead.type == Lexer.LC) {
            next = Main.Stmt.append(next, stmt());
        }
        return next;
    }

    public Main.Stmt stmt() {
        if (lookahead.type == Lexer.AUTO) {
            match(Lexer.AUTO);
            Main.Identifier variable = new Main.Identifier(lookahead.text);
            match(Lexer.IDENT);
            match(Lexer.ASN);
            Main.Expr expr = expr();
            match(Lexer.SEMI);
            return new Main.AutoStmt(variable, expr);
        }
        if (lookahead.type == Lexer.IDENT && peek(1).type != Lexer.FUNARG) {
            Main.Identifier variable = new Main.Identifier(lookahead.text);
            match(Lexer.IDENT);
            match(Lexer.ASN);
            Main.Expr expr = expr();
            match(Lexer.SEMI);
            return new Main.AssignStmt(variable, expr);
        }
        if (lookahead.type == Lexer.WRITE) {
            match(Lexer.WRITE);
            Main.Expr expr = expr();
            match(Lexer.SEMI);
            return new Main.Write(expr);
        }
        if (lookahead.type == Lexer.IF) {
            match(Lexer.IF);
            Main.Expr expr = expr();
            Main.Block ifblock = block();
            return new Main.IfStmt(expr, ifblock, null);
        }
        if (lookahead.type == Lexer.IF_ELSE) {
            match(Lexer.IF_ELSE);
            Main.Expr expr = expr();
            Main.Block ifblock = block();
            Main.Block elseBlock = block();
            return new Main.IfStmt(expr, ifblock, elseBlock);
        }
        if (lookahead.type == Lexer.WHILE) {
            match(Lexer.WHILE);
            Main.Expr expr = expr();
            Main.Block block = block();
            return new Main.WhileStmt(expr, block);
        }
        if (lookahead.type == Lexer.LC)
            return block();
        return new Main.ExprStmt(expr());
    }

    public Main.Expr expr() {
        return or();
    }

    public Main.Expr or() {
        Main.Expr expr = and();
        while (lookahead.type == Lexer.OR) {
            match(Lexer.OR);
            Main.Expr right = and();
            expr = new Main.BooleanOp(expr, right, Main.Oper.OR);
        }
        return expr;
    }

    public Main.Expr and() {
        Main.Expr expr = equality();
        while (lookahead.type == Lexer.AND) {
            match(Lexer.AND);
            Main.Expr right = equality();
            expr = new Main.BooleanOp(expr, right, Main.Oper.AND);
        }
        return expr;
    }

    public Main.Expr equality() {
        Main.Expr expr = comparison();
        while (lookahead.type == Lexer.EQ || lookahead.type == Lexer.NE) {
            Main.Oper op = Main.Oper.EQ;
            if (lookahead.type == Lexer.EQ)
                match(Lexer.EQ);
            else {
                op = Main.Oper.NE;
                match(Lexer.NE);
            }
            Main.Expr right = comparison();
            expr = new Main.ComparisonOp(expr, right, op);
        }
        return expr;
    }

    public Main.Expr comparison() {
        Main.Expr expr = term();
        while (lookahead.type == Lexer.GT || lookahead.type == Lexer.GE ||
                lookahead.type == Lexer.LT || lookahead.type == Lexer.LE) {
            Main.Oper op = Main.Oper.GT;
            if (lookahead.type == Lexer.GT)
                match(Lexer.GT);
            else if (lookahead.type == Lexer.GE) {
                op = Main.Oper.GE;
                match(Lexer.GE);
            } else if (lookahead.type == Lexer.LT) {
                op = Main.Oper.LT;
                match(Lexer.LT);
            } else {
                op = Main.Oper.LE;
                match(Lexer.LE);
            }
            Main.Expr right = term();
            expr = new Main.ComparisonOp(expr, right, op);
        }
        return expr;
    }

    public Main.Expr term() {
        Main.Expr expr = factor();
        while (lookahead.type == Lexer.SUB || lookahead.type == Lexer.ADD) {
            Main.Oper op = Main.Oper.ADD;
            if (lookahead.type == Lexer.ADD)
                match(Lexer.ADD);
            else {
                op = Main.Oper.SUB;
                match(Lexer.SUB);
            }
            Main.Expr right = factor();
            expr = new Main.ArithmeticOp(expr, right, op);
        }
        return expr;
    }

    public Main.Expr factor() {
        Main.Expr expr = unary();
        while (lookahead.type == Lexer.MUL || lookahead.type == Lexer.DIV) {
            Main.Oper op = Main.Oper.MUL;
            if (lookahead.type == Lexer.MUL)
                match(Lexer.MUL);
            else {
                op = Main.Oper.DIV;
                match(Lexer.DIV);
            }
            Main.Expr right = factor();
            expr = new Main.ArithmeticOp(expr, right, op);
        }
        return expr;
    }

    public Main.Expr unary() {
        if (lookahead.type == Lexer.NOT || lookahead.type == Lexer.SUB) {
            final int op = lookahead.type;
            if (op == Lexer.NOT)
                match(Lexer.NOT);
            else
                match(Lexer.SUB);
            Main.Expr expr = unary();
            if (op == Lexer.NOT)
                return new Main.NotOp(expr);
            else
                return new Main.NegationOp(expr);
        }
        return call();
    }

    public Main.Expr call() {
        if (lookahead.type == Lexer.LAMBDA) {
            match(Lexer.LAMBDA);
            Main.Identifier variable = new Main.Identifier(lookahead.text);
            match(Lexer.IDENT);
            return new Main.Lambda(variable, block());
        }
        if (lookahead.type == Lexer.READ) {
            match(Lexer.READ);
            return new Main.Read();
        }
        if (lookahead.type == Lexer.RETURN) {
            match(Lexer.RETURN);
            match(Lexer.ASN);
            Main.Expr expr = expr();
            match(Lexer.SEMI);
            return new Main.ReturnExpr(expr);
        }
        Main.Expr expr = primary();
        if (lookahead.type == Lexer.FUNARG) {
            match(Lexer.FUNARG);
            Main.Expr arg = expr();
            return new Main.Call(expr, arg);
        }
        return expr;
    }

    public Main.Expr primary() {
        if (lookahead.type == Lexer.IDENT) {
            Main.Identifier variable = new Main.Identifier(lookahead.text);
            match(Lexer.IDENT);
            return variable;
        } else if (lookahead.type == Lexer.NUMBER) {
            Main.Number number = new Main.Number(Integer.parseInt(lookahead.text));
            match(Lexer.NUMBER);
            return number;
        } else if (lookahead.type == Lexer.BOOL) {
            Main.BoolExpr bool = new Main.BoolExpr(Boolean.parseBoolean(lookahead.text));
            match(Lexer.BOOL);
            return bool;
        }
        match(Lexer.LP);
        Main.Expr expr = expr();
        match(Lexer.RP);
        return expr;
    }
}
