/**
 * Copyright (c) 2011, Jordi Cortadella
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the name of the <organization> nor the
 *      names of its contributors may be used to endorse or promote products
 *      derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package Chespel;

// Imports for ANTLR
import org.antlr.runtime.*;
import org.antlr.runtime.tree.*;
import org.antlr.stringtemplate.*;

// Imports from Java
import org.apache.commons.cli.*; // Command Language Interface
import java.io.*;

// Parser and Compiler
import parser.*;
import compiler.*;

/**
 * The class <code>Chespel</code> implement the main function of the
 * compiler. It accepts a set of options to generate the AST in
 * dot format. To know about the accepted options,
 * run the command Chespel -help.
 */

public class Chespel{

    /** The file name of the program. */
    private static String infile = null;

    private static String outfile = "generated_eval.cpp";
    /** Name of the file representing the AST. */
    private static String astfile = null;
    /** Flag indicating that the AST must be written in dot format. */
    private static boolean dotformat = false;
//     /** Name of the file storing the trace of the program. */
//     private static String tracefile = null;
    /** Flag to indicate whether the program must be compiled after parsing. */
    private static boolean compile = true;
      
    /** Main program that invokes the parser and the compiler. */
    
    public static void main(String[] args) throws Exception {
        // Parser for command line options
        if (!readOptions (args)) System.exit(1);

        // Parsing of the input file
        CharStream input = null;
        try {
            input = new ANTLRFileStream(infile);
        } catch (IOException e) {
            System.err.println ("Error: file " + infile + " could not be opened.");
            System.exit(1);
        }

        // Creates the lexer
        ChespelLexer lex = new ChespelLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lex);

        // Creates and runs the parser. As a result, an AST is created
        ChespelParser parser = new ChespelParser(tokens);
        ChespelTreeAdaptor adaptor = new ChespelTreeAdaptor();
        parser.setTreeAdaptor(adaptor);
        ChespelParser.prog_return result = null;
        try {
            result = parser.prog();
        } catch (Exception e) {} // Just catch the exception (nothing to do)
        
        // Check for parsing errors
        int nerrors = parser.getNumberOfSyntaxErrors();
        if (nerrors > 0) {
            System.err.println (nerrors + " errors detected. " +
                                "The program has not been compiled.");
            System.exit(1);
        }

        // Get the AST
        ChespelTree t = (ChespelTree)result.getTree();

        // Generate a file for the AST (option -ast file)
        if (astfile != null) {
            File ast = new File(astfile);
            BufferedWriter output = new BufferedWriter(new FileWriter(ast));
            if (dotformat) {
                DOTTreeGenerator gen = new DOTTreeGenerator();
                output.write(gen.toDOT(t).toString());
            } else {
                output.write(t.toStringTree());
            }
            output.close();
        }

        // Start interpretation (only if execution required)
        if (compile) {    
            ChespelCompiler C = null;
            int linenumber = -1;
            ErrorStack E = new ErrorStack(infile);
            try {
                C = new ChespelCompiler(t, E, outfile);
                C.compile();                  // Compiles the code
            } catch (CompileException e) {
                System.err.print (E.getErrors());
                System.exit(1); // status code 1
            } 
            /*catch (RuntimeException e) {
                if (C != null) linenumber = C.lineNumber();
                System.err.print ("Runtime error");
                if (linenumber < 0) System.err.print (": ");
                else System.err.print (" (" + infile + ", line " + linenumber + "): ");
                System.err.println (e.getMessage() + ".");
            } catch (StackOverflowError e) {
                if (C != null) linenumber = C.lineNumber();
                System.err.print("Stack overflow error");
                if (linenumber < 0) System.err.print (".");
                else System.err.println (" (" + infile + ", line " + linenumber + ").");
            }*/
         }
    }

    /**
     * Function to parse the command line. It defines some of
     * the attributes of the class. It returns true if the parsing
     * hass been successful, and false otherwise.
     */

    private static boolean readOptions(String[] args) {
        // Define the options
        Option help = new Option("help", "print this message");
        Option noexec = new Option("nocomp", "do not compile the program");
        Option dot = new Option("dot", "dump the AST in dot format");
        Option output = OptionBuilder
                        .withArgName ("file")
                        .hasArg()
                        .withDescription ("output file")
                        .create ("o");

        Option ast = OptionBuilder
                        .withArgName ("file")
                        .hasArg()
                        .withDescription ("write the AST")
                        .create ("ast");
                                       
        Options options = new Options();
        options.addOption(help);
        options.addOption(dot);
        options.addOption(ast);
        options.addOption(noexec);
        options.addOption(output);
        CommandLineParser clp = new GnuParser();
        CommandLine line = null;

        String cmdline = "Chespel [options] file";
        
        
        // Parse the options
        try {
            line = clp.parse (options, args);
        }
        catch (ParseException exp) {
            System.err.println ("Incorrect command line: " + exp.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp (cmdline, options);
            return false;
        }

        // Option -help
        if (line.hasOption ("help")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp (cmdline, options);
            return false;
        }
        
        // Option -dot
        if (line.hasOption ("dot")) dotformat = true;

        // Option -ast dotfile
        if (line.hasOption ("ast")) astfile = line.getOptionValue ("ast");

        // Option -o output_file
        if (line.hasOption ("o")) {
            outfile = line.getOptionValue ("o");
            if (outfile.length() < 5 || !outfile.substring( outfile.length()-4 ).equals(".cpp")) outfile += ".cpp";
        }
        
        // Option -noexec
        if (line.hasOption ("nocomp")) compile = false;

        // Remaining arguments (the input file)
        String[] files = line.getArgs();
        if (files.length != 1) {
            System.err.println ("Incorrect command line.");
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp (cmdline, options);
            return false;
        }
        
        infile = files[0];
        return true;
    }
}

