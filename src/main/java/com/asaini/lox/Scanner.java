package com.asaini.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.asaini.lox.TokenType.*;

class Scanner {
    private final String source; // source code
    private final List<Token> tokens = new ArrayList<>(); // parsed tokens

    // offsets that index into the source code strings
    private int start = 0; // points to first character of token being scanned
    private int current = 0; // points to current character of token being scanned

    // current source line
    private int line = 1;

    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("and", AND);
        keywords.put("class", CLASS);
        keywords.put("else", ELSE);
        keywords.put("false", FALSE);
        keywords.put("for", FOR);
        keywords.put("fun", FUN);
        keywords.put("if", IF);
        keywords.put("nil", NIL);
        keywords.put("or", OR);
        keywords.put("print", PRINT);
        keywords.put("return", RETURN);
        keywords.put("super", SUPER);
        keywords.put("this", THIS);
        keywords.put("true", TRUE);
        keywords.put("var", VAR);
        keywords.put("while", WHILE);
    }

    Scanner(String source) {
        this.source = source;
    }

    /**
     * Scan and return a list of all tokens in a source file.
     * 
     * @return a list of {@code Token} that were scanned
     */
    List<Token> scanTokens() {
        while (!isAtEnd()) {
            // We are the beginning of a new lexeme
            start = current;
            scanToken();
        }

        // to represent end of file in parse tree
        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    /**
     * Scans the current token and stores it internally
     */
    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(': addToken(LEFT_PAREN); break;
            case ')': addToken(RIGHT_PAREN); break;
            case '{': addToken(LEFT_BRACE); break;
            case '}': addToken(RIGHT_BRACE); break;
            case ',': addToken(COMMA); break;
            case '.': addToken(DOT); break;
            case '-': addToken(MINUS); break;
            case '+': addToken(PLUS); break;
            case ';': addToken(SEMICOLON); break;
            case '*': addToken(STAR); break;
            // checking for two character operands
            case '!': addToken(match('=') ? BANG_EQUAL : BANG); break;
            case '=': addToken(match('=') ? EQUAL_EQUAL : EQUAL); break;
            case '<': addToken(match('=') ? LESS_EQUAL : LESS); break;
            case '>': addToken(match('=') ? GREATER_EQUAL : GREATER); break;
            case '/': 
                if (match('/')) {
                    // A comment goes until the end of a line.
                    while (peek() != '\n' && !isAtEnd()) {
                        advance();
                    }
                } else {
                    addToken(SLASH);
                }
                break;
            // Handling ignored whitespace
            case ' ':
            case '\r':
            case '\t':
                break;
            case '\n':
                line++;
                break;
            // Handling String literals
            case '"': tokenizeString(); break;
            default:
                if (isDigit(c)) { // Handling number literals
                    tokenizeNumber();
                } else if (isAlpha(c)) {
                    tokenizeIdentifier();
                } else { 
                    Lox.error(line, "Unexpected character.");
                }
                break;
        }
    }

    /**
     * Tokenizes a variable name or keyword.
     */
    private void tokenizeIdentifier() {
        while (isAlphaNumeric(peek())) {
            advance();
        }
        String text = source.substring(start, current);
        TokenType type = keywords.getOrDefault(text, IDENTIFIER);

        addToken(type);
    }

    /**
     * Tokenizes a number and stores it internally.
     * Edge case with var x = 123.
     * Edge case with var x = .123
     */
    private void tokenizeNumber() {
        while (isDigit(peek())) {
            advance();
        }
        
        // currently allows '.123' or '123.', make sure to throw error
        if (peek() == '.' && isDigit(peekNext())) {
            // Consume the '.'
            advance();

            // Keep consuming digits after decimal point
            while (isDigit(peek())) {
                advance();
            }
        }

        addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
    }
    

    /**
     * Tokenizes a string and reports an error if the string is unterminated.
     */
    private void tokenizeString() {
        // Supporting multi-line comments
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') {
                line++;
            }
            advance();
        }

        if (isAtEnd()) {
            Lox.error(line, "Unterminated string");
            return;
        }

        // The closing "
        advance();

        // Trim the surrounding quotes
        String value = source.substring(start + 1, current - 1);
        addToken(STRING, value);
    }

    /** 
     * Checks to see if a two character operand is encountered, advances {@link #current} if found
     * 
     * @param expected the second character of the expected two-character operand
     * @return true if a two character operand is found, false if {@link #current} is at
     * the end of the source file, or if the specified two character operand is not found 
     */
    private boolean match(char expected) {
        if (isAtEnd()) {
            return false;
        }
        if (source.charAt(current) != expected) {
            return false;
        }

        // advancing to skip 
        current++;
        return true;
    }

    /**
     * Looks at and returns the next character.
     * 
     * @return the succeeding character if there is one before EOF, '\0' otherwise
     */
    private char peek() {
        if (isAtEnd()) {
            return '\0';
        }

        return source.charAt(current);
    }

    /**
     * Returns the character 2 indexes away from the current character.
     * 
     * @return the character two indexes away if there is one before EOF, '\0' otherwise.
     */
    private char peekNext() {
        if (current + 1 >= source.length()) { 
            return '\0';
        }

        return source.charAt(current + 1);
    }

    /**
     * Evaluates whether a character is a letter or an underscore.
     * 
     * @param c the character to be checked
     * @return true if a character is a valid letter of the alphabet or an underscore, false otherwise
     */
    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c == '_');
    }

    /**
     * Evaluates whether a character is a letter, underscore, or digit.
     * 
     * @param c the character to be checked
     * @return true if the character is a valid letter, underscore, or a valid digit, false otherwise
     */
    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }
    
    /**
     * Checks if a character is a digit or not.
     * 
     * @param c the character that is being checked
     * @return true if c is a digit, false otherwise
     */
    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    /**
     * Checks to see if interpreter has scanned all the source code
     * 
     * @return true if current is at or beyond EOF
     */
    private boolean isAtEnd() {
        return current >= source.length();
    }

    /**
     * Extracts the current character and advances {@link #current} to the next character
     * 
     * @return the character at index {@link #current} in the source file
     */
    private char advance() {
        return source.charAt(current++);
    }

    /**
     * Calls {@link #addToken(TokenType, Object)} to store token
     * 
     * @param type the type of token to add
     */
    private void addToken(TokenType type) {
        addToken(type, null);
    }

    /**
     * Stores token internally in {@link #tokens}
     * 
     * @param type the type of token to store
     * @param literal the value type of the token in the source file
     */
    private void addToken(TokenType type, Object literal) {
        String token = source.substring(start, current);
        tokens.add(new Token(type, token, literal, line));
    }
}
