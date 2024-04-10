public class Token {
    public int type;
    public String text;

    public Token(int type, String text) {
        this.type = type;
        this.text = text;
    }

    public String toString() {
        String name = Lexer.tokenNames[type];
        return "<'" + text + "'," + name + ">";
    }
}
