package com.asaini.lox;

class Token {
    final TokenType type; // type of literal
    final String lexeme; // string literal read
    final Object literal; // value type literal was converted to
    final int line; // line at which token was found]

    Token(TokenType type, String lexeme, Object literal, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
    }

    public String toString() {
        return type + " " + lexeme + " " + literal;
    }
}
