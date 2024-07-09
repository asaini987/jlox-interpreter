package com.asaini.lox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

class Lox {
    // determines whether to keep executing code
    static boolean hadError = false;
    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: jlox[script]");
            System.exit(64);
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    /**
     * Executes a lox source file.
     * 
     * @param path file path to source file
     * @throws IOException if an error is encountered within the source code
     */
    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));

        if (hadError) {
            System.exit(65);
        }
    }  

    /**
     * Starts a REPL lox interpreter from the command line.
     * 
     * @throws IOException 
     */
    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        while (true) {
            System.out.print("> ");
            String line = reader.readLine();
            if (line == null) {
                break;
            }
            run(line);
            hadError = false;
        }
    }

    /**
     * Prints the scanned tokens from source code.
     * 
     * @param source the lox source code
     */
    private static void run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();
        
        for (Token token : tokens) {
            System.out.println(token);
        }
    }

    /**
     * Generate error at specified line and message.
     * 
     * @param line the line where the error ocurred in the source file
     * @param message the error message to be printed
     */
    static void error(int line, String message) {
        report(line, "", message);
    }

    /**
     * Reports an error found while executing lox source code.
     * 
     * @param line the line where the error was reported
     * @param where the exact location of where error is
     * @param message the error message to be printed
     */
    private static void report(int line, String where, String message) {
        System.err.println(
            "[line " + line + "] Error" + where + ": " + message
        );

        hadError = true;
    }
}
