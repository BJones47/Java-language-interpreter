import java.util.ArrayList;
import java.util.List;

public class Lexer {
    public static int NUMBER = 2;
    public static int BOOL = 3;
    public static int ADD = 4;
    public static int SUB = 5;
    public static int MUL = 6;
    public static int DIV = 7;
    public static int AND = 8;
    public static int OR = 9;
    public static int NOT = 10;
    public static int ASN = 11;
    public static int FUNARG = 12;
    public static int LP = 13;
    public static int RP = 14;
    public static int LC = 15;
    public static int RC = 16;
    public static int SEMI = 17;
    public static int LT = 18;
    public static int LE = 19;
    public static int GT = 20;
    public static int GE = 21;
    public static int NE = 22;
    public static int EQ = 23;
    public static int IF = 24;
    public static int IF_ELSE = 25;
    public static int WHILE = 26;
    public static int READ = 27;
    public static int WRITE = 28;
    public static int LAMBDA = 29;
    public static int IDENT = 30;
    public static int AUTO = 31;
    public static int RETURN = 32;

    public static String[] tokenNames =
            {"n/a", "<EOF>", "NUMBER", "BOOL", "PLUS", "MINUS", "MUL", "DIV", "AND", "OR", "NOT",
                    "ASN", "FUNARG", "LP", "RP", "LC", "RC", "SEMI", "LT", "LE", "GT", "GE", "NE",
                    "EQ", "IF", "IF_ELSE", "WHILE", "READ", "WRITE", "LAMBDA", "IDENT", "AUTO", "RETURN"};

    // represent end of file char
    private static final char EOF = (char) -1;
    // represent EOF token type
    private static final int EOF_TYPE = 1;
    // input string
    private final String input;
    // index into input of current character
    private int p = 0;
    // current character
    private char c;

    public Lexer(String input) {
        this.input = input;
        c = input.charAt(p); // prime lookahead
    }

    // returns true if the current character is EOF
    public boolean isEOF() {
        return p >= input.length();
    }

    // move one character; detect "end of file"
    public void consume() {
        p++;
        if (p >= input.length()) c = EOF;
        else c = input.charAt(p);
    }

    // ensure x is next character on the input stream.
    public void match(char x) {
        if (c == x) consume();
        else throw new Error("expecting " + x + ", but found " + c);
    }

    public static String getTokenName(int x) { return tokenNames[x]; }

    boolean isLETTER() { return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z'; }

    boolean isDIGIT() { return c >= '0' && c <= '9'; }

    public List<Token> getTokens() {
        List<Token> tokenList = new ArrayList<>();
        while (!isEOF())
            tokenList.add(nextToken());
        return tokenList;
    }

    public Token nextToken() {
        while (c != EOF) {
            switch (c) {
                case ' ':
                case '\t':
                case '\n':
                case '\r':
                    WS();
                    continue;
                case ';':
                    consume();
                    return new Token(SEMI, ";");
                case '{':
                    consume();
                    return new Token(LC, "{");
                case '}':
                    consume();
                    return new Token(RC, "}");
                case '(':
                    consume();
                    return new Token(LP, "(");
                case ')':
                    consume();
                    return new Token(RP, ")");
                case '=':
                    consume();
                    return new Token(EQ, "=");
                case '!':
                    consume();
                    if (c == '=') {
                        consume();
                        return new Token(NE, "!=");
                    }
                case '<':
                    consume();
                    if (c == '=') {
                        consume();
                        return new Token(LE, "<=");
                    }
                    return new Token(LT, "<");
                case '>':
                    consume();
                    if (c == '=') {
                        consume();
                        return new Token(GE, ">=");
                    }
                    return new Token(GT, ">");
                case '@':
                    consume();
                    return new Token(FUNARG, "@");
                case ':':
                    consume();
                    if (c != '=') throw new Error("expecting =, found " + c);
                    consume();
                    return new Token(ASN, ":=");
                case '+':
                    consume();
                    return new Token(ADD, "+");
                case '-':
                    consume();
                    return new Token(SUB, "-");
                case '*':
                    consume();
                    return new Token(MUL, "*");
                case '/':
                    consume();
                    return new Token(DIV, "/");
                default:
                    if (isLETTER()) return NAME();
                    else if (isDIGIT()) return NUMBER();
                    throw new Error("invalid character: " + c);
            }
        }
        return new Token(EOF_TYPE, "<EOF>");
    }

    // NAME is sequence of >=1 letter
    Token NAME() {
        StringBuilder buf = new StringBuilder();
        do {
            buf.append(c);
            consume();
        } while (isLETTER());
        final String str = buf.toString();
        return switch (str) {
            case "true", "false" -> new Token(BOOL, str);
            case "read" -> new Token(READ, str);
            case "write" -> new Token(WRITE, str);
            case "while" -> new Token(WHILE, str);
            case "if" -> new Token(IF, str);
            case "ifelse" -> new Token(IF_ELSE, str);
            case "lambda" -> new Token(LAMBDA, str);
            case "and" -> new Token(AND, str);
            case "or" -> new Token(OR, str);
            case "not" -> new Token(NOT, str);
            case "auto" -> new Token(AUTO, str);
            case "ret" -> new Token(RETURN, str);
            default -> new Token(IDENT, str);
        };
    }

    // NUMBER is a sequence of >=1 digit
    Token NUMBER() {
        StringBuilder buf = new StringBuilder();
        do {
            buf.append(c);
            consume();
        } while (isDIGIT());
        return new Token(NUMBER, buf.toString());
    }

    // ignore any whitespace
    void WS() {
        while (c == ' ' || c == '\t' || c == '\n' || c == '\r') consume();
    }
}
