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

package compiler;

import parser.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.LinkedList;
import java.io.*;

/** Class that implements the compiler of the language. */

public class ChespelCompiler {

    /** Table of symbols. */
    private SymbolTable SymbolTable;

    private LinkedList<ChespelTree> GlobalDefinitions;

    private LinkedList<ChespelTree> FunctionDefinitions;
    
    private LinkedList<ChespelTree> RuleDefinitions;

    /**
     * Stores the line number of the current statement.
     * The line number is used to report runtime errors.
     */
    private int linenumber = -1;
    
    /**
     * Constructor of the compiler. It prepares the main
     * data structures for the translation to C.
     */
    public ChespelCompiler(ChespelTree T) {
        assert T != null;
        PreProcessAST(T); // Some internal pre-processing of the AST
        SymbolTable = new SymbolTable(); // Creates the memory of the virtual machine
        parseDefinitions(T);

    }

    private void parseDefinitions(ChespelTree T) {
        assert T != null && T.getType() == ChespelLexer.LIST_DEF;
        GlobalDefinitions = new LinkedList<ChespelTree>();
        FunctionDefinitions = new LinkedList<ChespelTree>();
        RuleDefinitions = new LinkedList<ChespelTree>();
        int n = T.getChildCount();
        for (int i = 0; i < n; ++i) {
            ChespelTree f = T.getChild(i);
            switch (f.getType()) {
                case ChespelLexer.FUNCTION_DEF:
                    FunctionDefinitions.addLast(f);
                    break;
                case ChespelLexer.GLOBAL_DEF:
                    GlobalDefinitions.addLast(f);
                    break;
                case ChespelLexer.RULE_DEF:
                    RuleDefinitions.addLast(f);
                    break;
                default:
                    assert false;
            }
        }
    }

    /** Compiles the program by translating the sentences 
      * from Chespel to the C++ class of the chess state evalation. 
      */
    public void compile() {
        //not implemented yet
        //output header of the .cpp file
        checkTypes();
        //compile
    }

    private void checkTypes() {
        checkGlobalTypes(); 
    }

    private void checkGlobalTypes() {
        for (ChespelTree T : GlobalDefinitions) {
            TypeInfo return_type = getTypeFromAST(T.getChild(0));
            System.out.println("Printing global types: "+ T.getChild(1).getText() + ": " + return_type.toString());

        }
    }

    private TypeInfo getTypeFromAST(ChespelTree t) {
        switch (t.getType()) {
            case ChespelLexer.STRING_TYPE:
                return new TypeInfo("STRING");
            case ChespelLexer.BOARD_TYPE:
                if (t.getText().equals("cell")) return new TypeInfo("CELL");
                else if (t.getText().equals("row")) return new TypeInfo("ROW");
                else if (t.getText().equals("file")) return new TypeInfo("FILE");
                else if (t.getText().equals("rank")) return new TypeInfo("RANK");
            case ChespelLexer.PIECE_TYPE:
                return new TypeInfo("PIECE");
            case ChespelLexer.NUM_TYPE:
                return new TypeInfo("NUMERIC");
            case ChespelLexer.BOOL_TYPE:
                return new TypeInfo("BOOL");
            case ChespelLexer.VOID_TYPE:
                return new TypeInfo("VOID");
            case ChespelLexer.L_BRACKET:
                int num_array = 0;
                while (t.getType() == ChespelLexer.L_BRACKET) {
                    ++num_array;
                    t = t.getChild(0);
                }
                TypeInfo c = getTypeFromAST(t);
                return new TypeInfo(c.toString(), num_array);
            default:
                assert false;
        }
        // dummy return
        return new TypeInfo();
    }

    
    /**
     * Gathers information from the AST and creates the map from
     * definition names to the corresponding AST nodes.
     */
    private void MapDefinitions(ChespelTree T) {
        // assert T != null && T.getType() == ChespelLexer.LIST_DEF;
        // DefName2Tree = new HashMap<String,ChespelTree> ();
        // int n = T.getChildCount();
        // for (int i = 0; i < n; ++i) {
        //     ChespelTree f = T.getChild(i);
        //     assert f.getType() == ChespelLexer.FUNC;
        //     String fname = f.getChild(0).getText();
        //     if (DefName2Tree.containsKey(fname)) {
        //         throw new RuntimeException("Multiple definitions of function " + fname);
        //     }
        //     DefName2Tree.put(fname, f);
        // } 
    }

    /**
     * Performs some pre-processing on the AST. Basically, it
     * calculates the value of the literals and stores a simpler
     * representation. See ChespelTree.java for details.
     */
    private void PreProcessAST(ChespelTree T) {
        // if (T == null) return;
        // switch(T.getType()) {
        //     case ChespelLexer.INT: T.setIntValue(); break;
        //     case ChespelLexer.STRING: T.setStringValue(); break;
        //     case ChespelLexer.BOOLEAN: T.setBooleanValue(); break;
        //     default: break;
        // }
        // int n = T.getChildCount();
        // for (int i = 0; i < n; ++i) PreProcessAST(T.getChild(i));
    }

    /**
     * Gets the current line number. In case of a runtime error,
     * it returns the line number of the statement causing the
     * error.
     */
    public int lineNumber() { return linenumber; }

    /** Defines the current line number associated to an AST node. */
    private void setLineNumber(ChespelTree t) { linenumber = t.getLine();}

    /** Defines the current line number with a specific value */
    private void setLineNumber(int l) { linenumber = l;}
    
    /**
     * Executes a function.
     * @param funcname The name of the function.
     * @param args The AST node representing the list of arguments of the caller.
     * @return The data returned by the function.
     */
    // private Data executeFunction (String funcname, ChespelTree args) {
        // // Get the AST of the function
        // ChespelTree f = FuncName2Tree.get(funcname);
        // if (f == null) throw new RuntimeException(" function " + funcname + " not declared");

        // // Gather the list of arguments of the caller. This function
        // // performs all the checks required for the compatibility of
        // // parameters.
        // ArrayList<Data> Arg_values = listArguments(f, args);

        // // Dumps trace information (function call and arguments)
        // if (trace != null) traceFunctionCall(f, Arg_values);
        
        // // List of parameters of the callee
        // ChespelTree p = f.getChild(1);
        // int nparam = p.getChildCount(); // Number of parameters

        // // Create the activation record in memory
        // Stack.pushActivationRecord(funcname, lineNumber());

        // // Track line number
        // setLineNumber(f);
         
        // // Copy the parameters to the current activation record
        // for (int i = 0; i < nparam; ++i) {
        //     String param_name = p.getChild(i).getText();
        //     Stack.defineVariable(param_name, Arg_values.get(i));
        // }

        // // Execute the instructions
        // Data result = executeListInstructions (f.getChild(2));

        // // If the result is null, then the function returns void
        // if (result == null) result = new Data();
        
        // // Dumps trace information
        // if (trace != null) traceReturn(f, result, Arg_values);
        
        // // Destroy the activation record
        // Stack.popActivationRecord();

        // return result;
    // }

    /**
     * Executes a block of instructions. The block is terminated
     * as soon as an instruction returns a non-null result.
     * Non-null results are only returned by "return" statements.
     * @param t The AST of the block of instructions.
     * @return The data returned by the instructions (null if no return
     * statement has been executed).
     */
    // private Data executeListInstructions (ChespelTree t) {
        // assert t != null;
        // Data result = null;
        // int ninstr = t.getChildCount();
        // for (int i = 0; i < ninstr; ++i) {
        //     result = executeInstruction (t.getChild(i));
        //     if (result != null) return result;
        // }
        // return null;
    // }
    
    /**
     * Executes an instruction. 
     * Non-null results are only returned by "return" statements.
     * @param t The AST of the instruction.
     * @return The data returned by the instruction. The data will be
     * non-null only if a return statement is executed or a block
     * of instructions executing a return.
     */
    // private Data executeInstruction (ChespelTree t) {
        // assert t != null;
        
        // setLineNumber(t);
        // Data value; // The returned value

        // // A big switch for all type of instructions
        // switch (t.getType()) {

        //     // Assignment
        //     case ChespelLexer.ASSIGN:
        //         value = evaluateExpression(t.getChild(1));
        //         Stack.defineVariable (t.getChild(0).getText(), value);
        //         return null;

        //     // If-then-else
        //     case ChespelLexer.IF:
        //         value = evaluateExpression(t.getChild(0));
        //         checkBoolean(value);
        //         if (value.getBooleanValue()) return executeListInstructions(t.getChild(1));
        //         // Is there else statement ?
        //         if (t.getChildCount() == 3) return executeListInstructions(t.getChild(2));
        //         return null;

        //     // While
        //     case ChespelLexer.WHILE:
        //         while (true) {
        //             value = evaluateExpression(t.getChild(0));
        //             checkBoolean(value);
        //             if (!value.getBooleanValue()) return null;
        //             Data r = executeListInstructions(t.getChild(1));
        //             if (r != null) return r;
        //         }

        //     // Return
        //     case ChespelLexer.RETURN:
        //         if (t.getChildCount() != 0) {
        //             return evaluateExpression(t.getChild(0));
        //         }
        //         return new Data(); // No expression: returns void data

        //     // Read statement: reads a variable and raises an exception
        //     // in case of a format error.
        //     case ChespelLexer.READ:
        //         String token = null;
        //         Data val = new Data(0);;
        //         try {
        //             token = stdin.next();
        //             val.setValue(Integer.parseInt(token)); 
        //         } catch (NumberFormatException ex) {
        //             throw new RuntimeException ("Format error when reading a number: " + token);
        //         }
        //         Stack.defineVariable (t.getChild(0).getText(), val);
        //         return null;

        //     // Write statement: it can write an expression or a string.
        //     case ChespelLexer.WRITE:
        //         ChespelTree v = t.getChild(0);
        //         // Special case for strings
        //         if (v.getType() == ChespelLexer.STRING) {
        //             System.out.format(v.getStringValue());
        //             return null;
        //         }

        //         // Write an expression
        //         System.out.print(evaluateExpression(v).toString());
        //         return null;

        //     // Function call
        //     case ChespelLexer.FUNCALL:
        //         executeFunction(t.getChild(0).getText(), t.getChild(1));
        //         return null;

        //     default: assert false; // Should never happen
        // }

        // // All possible instructions should have been treated.
        // assert false;
        // return null;
    // }

    /**
     * Evaluates the expression represented in the AST t.
     * @param t The AST of the expression
     * @return The value of the expression.
     */
    // private Data evaluateExpression(ChespelTree t) {
        // assert t != null;

        // int previous_line = lineNumber();
        // setLineNumber(t);
        // int type = t.getType();

        // Data value = null;
        // // Atoms
        // switch (type) {
        //     // A variable
        //     case ChespelLexer.ID:
        //         value = new Data(Stack.getVariable(t.getText()));
        //         break;
        //     // An integer literal
        //     case ChespelLexer.INT:
        //         value = new Data(t.getIntValue());
        //         break;
        //     // A Boolean literal
        //     case ChespelLexer.BOOLEAN:
        //         value = new Data(t.getBooleanValue());
        //         break;
        //     // A function call. Checks that the function returns a result.
        //     case ChespelLexer.FUNCALL:
        //         value = executeFunction(t.getChild(0).getText(), t.getChild(1));
        //         assert value != null;
        //         if (value.isVoid()) {
        //             throw new RuntimeException ("function expected to return a value");
        //         }
        //         break;
        //     default: break;
        // }

        // // Retrieve the original line and return
        // if (value != null) {
        //     setLineNumber(previous_line);
        //     return value;
        // }
        
        // // Unary operators
        // value = evaluateExpression(t.getChild(0));
        // if (t.getChildCount() == 1) {
        //     switch (type) {
        //         case ChespelLexer.PLUS:
        //             checkInteger(value);
        //             break;
        //         case ChespelLexer.MINUS:
        //             checkInteger(value);
        //             value.setValue(-value.getIntegerValue());
        //             break;
        //         case ChespelLexer.NOT:
        //             checkBoolean(value);
        //             value.setValue(!value.getBooleanValue());
        //             break;
        //         default: assert false; // Should never happen
        //     }
        //     setLineNumber(previous_line);
        //     return value;
        // }

        // // Two operands
        // Data value2;
        // switch (type) {
        //     // Relational operators
        //     case ChespelLexer.EQUAL:
        //     case ChespelLexer.NOT_EQUAL:
        //     case ChespelLexer.LT:
        //     case ChespelLexer.LE:
        //     case ChespelLexer.GT:
        //     case ChespelLexer.GE:
        //         value2 = evaluateExpression(t.getChild(1));
        //         if (value.getType() != value2.getType()) {
        //           throw new RuntimeException ("Incompatible types in relational expression");
        //         }
        //         value = value.evaluateRelational(type, value2);
        //         break;

        //     // Arithmetic operators
        //     case ChespelLexer.PLUS:
        //     case ChespelLexer.MINUS:
        //     case ChespelLexer.MUL:
        //     case ChespelLexer.DIV:
        //     case ChespelLexer.MOD:
        //         value2 = evaluateExpression(t.getChild(1));
        //         checkInteger(value); checkInteger(value2);
        //         value.evaluateArithmetic(type, value2);
        //         break;

        //     // Boolean operators
        //     case ChespelLexer.AND:
        //     case ChespelLexer.OR:
        //         // The first operand is evaluated, but the second
        //         // is deferred (lazy, short-circuit evaluation).
        //         checkBoolean(value);
        //         value = evaluateBoolean(type, value, t.getChild(1));
        //         break;

        //     default: assert false; // Should never happen
        // }
        
        // setLineNumber(previous_line);
        // return value;
    // }
    
    /**
     * Evaluation of Boolean expressions. This function implements
     * a short-circuit evaluation. The second operand is still a tree
     * and is only evaluated if the value of the expression cannot be
     * determined by the first operand.
     * @param type Type of operator (token).
     * @param v First operand.
     * @param t AST node of the second operand.
     * @return An Boolean data with the value of the expression.
     */
    // private Data evaluateBoolean (int type, Data v, ChespelTree t) {
        // // Boolean evaluation with short-circuit

        // switch (type) {
        //     case ChespelLexer.AND:
        //         // Short circuit if v is false
        //         if (!v.getBooleanValue()) return v;
        //         break;
        
        //     case ChespelLexer.OR:
        //         // Short circuit if v is true
        //         if (v.getBooleanValue()) return v;
        //         break;
                
        //     default: assert false;
        // }

        // // Return the value of the second expression
        // v = evaluateExpression(t);
        // checkBoolean(v);
        // return v;
    // }

    /** Checks that the data is Boolean and raises an exception if it is not. */
//     private void checkBoolean (Data b) {
        // if (!b.isBoolean()) {
        //     throw new RuntimeException ("Expecting Boolean expression");
        // }
//     }
    
    /** Checks that the data is integer and raises an exception if it is not. */
//     private void checkInteger (Data b) {
        // if (!b.isInteger()) {
        //     throw new RuntimeException ("Expecting numerical expression");
        // }
//     }

    /**
     * Gathers the list of arguments of a function call. It also checks
     * that the arguments are compatible with the parameters. In particular,
     * it checks that the number of parameters is the same and that no
     * expressions are passed as parametres by reference.
     * @param AstF The AST of the callee.
     * @param args The AST of the list of arguments passed by the caller.
     * @return The list of evaluated arguments.
     */
     
    // private ArrayList<Data> listArguments (ChespelTree AstF, ChespelTree args) {
        // if (args != null) setLineNumber(args);
        // ChespelTree pars = AstF.getChild(1);   // Parameters of the function
        
        // // Create the list of parameters
        // ArrayList<Data> Params = new ArrayList<Data> ();
        // int n = pars.getChildCount();

        // // Check that the number of parameters is the same
        // int nargs = (args == null) ? 0 : args.getChildCount();
        // if (n != nargs) {
        //     throw new RuntimeException ("Incorrect number of parameters calling function " +
        //                                 AstF.getChild(0).getText());
        // }

        // // Checks the compatibility of the parameters passed by
        // // reference and calculates the values and references of
        // // the parameters.
        // for (int i = 0; i < n; ++i) {
        //     ChespelTree p = pars.getChild(i); // Parameters of the callee
        //     ChespelTree a = args.getChild(i); // Arguments passed by the caller
        //     setLineNumber(a);
        //     if (p.getType() == ChespelLexer.PVALUE) {
        //         // Pass by value: evaluate the expression
        //         Params.add(i,evaluateExpression(a));
        //     } else {
        //         // Pass by reference: check that it is a variable
        //         if (a.getType() != ChespelLexer.ID) {
        //             throw new RuntimeException("Wrong argument for pass by reference");
        //         }
        //         // Find the variable and pass the reference
        //         Data v = Stack.getVariable(a.getText());
        //         Params.add(i,v);
        //     }
        // }
        // return Params;
    // }

}
