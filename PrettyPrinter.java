public final class PrettyPrinter {

    private PrettyPrinter() { }

    public static String printAST(Main.Stmt ast) {
        int tabs = 0;
        final StringBuilder sb = new StringBuilder();
        if (ast != null)
            printStmt(ast, tabs, sb);
        return sb.toString().trim();
    }

    private static void printStmt(Main.Stmt stmt, final int tabs, final StringBuilder sb) {
        sb.append(getTabsString(tabs));
        sb.append("+--");
        sb.append(stmt.getLabel());
        sb.append("\n");
        stmt.children.forEach(c -> {
            if (c instanceof Main.Expr)
                printExpr((Main.Expr) c, tabs + 1, sb);
            else
                printStmt((Main.Stmt) c, tabs + 1, sb);
        });
    }

    private static void printExpr(Main.Expr expr, final int tabs, final StringBuilder sb) {
        sb.append(getTabsString(tabs));
        sb.append("*--");
        sb.append(expr.getLabel());
        sb.append("\n");
        if (!expr.getChildren().isEmpty()) {
            expr.getChildren().forEach(child -> {
                if (child instanceof Main.Expr)
                    printExpr((Main.Expr) child, tabs + 1, sb);
                else if (child instanceof Main.Stmt)
                    printStmt((Main.Stmt) child, tabs, sb);
            });
        }
    }

    private static String getTabsString(final int tabs) {
        return "|   ".repeat(Math.max(0, tabs));
    }
}
