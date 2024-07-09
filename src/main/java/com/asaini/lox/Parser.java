package com.asaini.lox;

import java.util.List;

import static com.asaini.lox.TokenType.*;

class Parser {
    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    private Expr expression() {
        return equality();
    }

    /**
     * Parses an equality expression series of tokens into an {@code Expr}
     * 
     * @return an expression object as the result
     */
    private Expr equality() {
        Expr expr = comparison();

        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    /**
     * Parses a comparison expression into an {@code Expr}
     * 
     * @return an {@code Expr} that represents the comparison expression
     */
    private Expr comparison() {
        Expr expr = term();

        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    /**
     * 
     * @return an {@code Expr} that represents the term expression
     */
    private Expr term() {
        Expr expr = factor();

        while (match(MINUS, PLUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr factor() {
        Expr expr = unary();

        while (match(SLASH, STAR)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr unary() {
        if (match(BANG, MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }

        return primary();
    }

    private Expr primary() {
        if (match(FALSE)) {
            return new Expr.Literal(false);
        }
        if (match(TRUE)) {
            return new Expr.Literal(true);
        }
        if (match(NIL)) {
            return new Expr.Literal(null);
        }

        if (match(NUMBER, STRING)) {
            return new Expr.Literal(previous().literal);
        }

        if (match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expected ') after expression");

            return new Expr.Grouping(expr);
        }
    }

    /**
     * Conditional advance to consume tokens only if they match the specified token types passed as arguments
     * 
     * @param types an array of {@code TokenType} to be matched
     * @return true if the current token type matches one of the argument types
     */
    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    /**
     * Checks to see if the current token is the same type as the argument type
     * 
     * @param type the token type to check
     * @return true if the current token matches the token type passed as the argument, false if at EOF
     * or no match
     */
    private boolean check(TokenType type) {
        if (isAtEnd()) {
            return false;
        }

        return peek().type == type;
    }

    /**
     * Advances the parser to consume the next token
     * 
     * @return a token object of the token we just consumed. If we are at EOF, return the last token.
     */
    private Token advance() {
        if (!isAtEnd()) {
            current++;
        }

        return previous();
    }

    /**
     * Gets the current token
     * 
     * @return the current token
     */
    private Token peek() {
        return tokens.get(current);
    }

    /**
     * Checks to see if Parser has reached EOF
     * 
     * @return true if the parser has consumed all the tokens
     */
    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    /**
     * Gets the previous token
     * 
     * @return the previous token
     */
    private Token previous() {
        return tokens.get(current - 1);
    }
}
