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
    private SymbolTable symbolTable;

    private LinkedList<ChespelTree> GlobalDefinitions;

    private LinkedList<ChespelTree> FunctionDefinitions;
    
    private LinkedList<ChespelTree> ruleDefinitions;

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
        symbolTable = new SymbolTable(); // Creates the memory of the virtual machine
        parseDefinitions(T);

    }

    private void parseDefinitions(ChespelTree T) {
        assert T != null && T.getType() == ChespelLexer.LIST_DEF;
        GlobalDefinitions = new LinkedList<ChespelTree>();
        FunctionDefinitions = new LinkedList<ChespelTree>();
        ruleDefinitions = new LinkedList<ChespelTree>();
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
                    ruleDefinitions.addLast(f);
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
        checkTypes();
        //output header of the .cpp file
        //compile
    }

    private void checkTypes() {
        checkGlobalTypes(); 
        checkFunctionTypes();
        checkRuleTypes();
    }

    private void checkGlobalTypes() {
        System.out.println("Global variable declarations");
        for (ChespelTree T : GlobalDefinitions) {
            TypeInfo return_type = getTypeFromDeclaration(T.getChild(0));
            System.out.println(T.getChild(1).getText() + ": " + return_type.toString());
            assert return_type.equals(getTypeExpression(T.getChild(2)));
            symbolTable.defineGlobal(T.getChild(1).getText(), return_type);
        }
    }

    private void checkFunctionTypes() {
        System.out.println("Function declarations");
        for (ChespelTree T : FunctionDefinitions) {
            TypeInfo return_type = getTypeFromDeclaration(T.getChild(0));
            String name = T.getChild(1).getText();
            ChespelTree args = T.getChild(2);
            // Treat header
            ArrayList<TypeInfo> header = new ArrayList<TypeInfo>();
            for (int i = 0; i < args.getChildCount() ; ++i) {
                TypeInfo arg_type = getTypeFromDeclaration(args.getChild(i).getChild(0));
                header.add(arg_type);
            }
            if (header.size() == 0) header.add(new TypeInfo());
            // define function
            symbolTable.defineFunction(name, return_type, header);
            System.out.println(return_type.toString() + " " + name + " " + header.toString());
        }

        for (ChespelTree T : FunctionDefinitions) {
            // define arguments as variables
            ChespelTree args = T.getChild(2);
            symbolTable.pushVariableTable();
            for (int i = 0; i < args.getChildCount() ; ++i) {
                ChespelTree arg = args.getChild(i);
                TypeInfo arg_type = getTypeFromDeclaration(arg.getChild(0));
                String arg_name = arg.getChild(1).getText();
                if (arg == ChespelLexer.PREF) arg_name = arg_name.substring(1); // drop '&' of token's text
                symbolTable.defineVariable(arg_name, arg_type);
            }
            TypeInfo returnType = getTypeFromDeclaration(T.getChild(0));
            ChespelTree listInstr = T.getChild(3);
            checkTypeListInstructions(listInstr);
            checkCorrectReturnType(listInstr, returnType);
            checkNoScoreStatements(listInstr);
            //checkAlwaysReachReturn(listInstr); //not implemented
            symbolTable.popVariableTable(); // delete variables
        }
    }

    private void checkRuleTypes() {
        System.out.println("Rule declarations");
        for (ChespelTree T : ruleDefinitions) {
            String name = T.getChild(0).getText();
            //missing: check that no repeated RULE_OPTIONS
            symbolTable.pushVariableTable();
            ChespelTree listInstr = T.getChild(2);
            checkTypeListInstructions(listInstr);
            checkOnlyVoidReturnStatements(listInstr);
            symbolTable.popVariableTable();
        }
    }

    private void checkCorrectReturnType(ChespelTree listInstr, TypeInfo returnType) {
        assert listInstr.getType() == ChespelLexer.LIST_INSTR;
        for (int i = 0; i < listInstr.getChildCount(); ++i) {
            ChespelTree t = listInstr.getChild(i);
            if (t.getType() == ChespelLexer.RETURN) {
                TypeInfo returnExprType = getTypeExpression(t.getChild(0));
                assert returnType.equals(returnExprType);
            }
        }
    }

    private void checkNoScoreStatements(ChespelTree listInstr) {
        assert listInstr.getType() == ChespelLexer.LIST_INSTR;
        for (int i = 0; i < listInstr.getChildCount(); ++i) {
            ChespelTree t = listInstr.getChild(i);
            assert t.getType() != ChespelLexer.SCORE;
        }
    }

    private void checkOnlyVoidReturnStatements(ChespelTree listInstr) {
        TypeInfo voidType = new TypeInfo("VOID");
        assert listInstr.getType() == ChespelLexer.LIST_INSTR;
        for (int i = 0; i < listInstr.getChildCount(); ++i) {
            ChespelTree t = listInstr.getChild(i);
            if (t.getType() == ChespelLexer.RETURN) {
                TypeInfo returnType = getTypeExpression(t.getChild(0));
                assert returnType.equals(voidType);
            }
        }
    }

    private TypeInfo getTypeFromDeclaration(ChespelTree t) {
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
                return new TypeInfo("BOOLEAN");
            case ChespelLexer.VOID_TYPE:
                return new TypeInfo("VOID");
            case ChespelLexer.L_BRACKET:
                int num_array = 0;
                while (t.getType() == ChespelLexer.L_BRACKET) {
                    ++num_array;
                    t = t.getChild(0);
                }
                TypeInfo c = getTypeFromDeclaration(t);
                return new TypeInfo(c.toString(), num_array);
            default:
                System.out.println("Error: not a type declaration " + t.toString());
                assert false;
        }
        // dummy return
        return new TypeInfo();
    }

    private TypeInfo getTypeExpression(ChespelTree t) {
        assert t != null;
        TypeInfo type_info = t.getInfo();
        if (type_info == null) {
            computeTypeExpression(t);
            type_info = t.getInfo();
        }
        return type_info;
    }

    private void computeTypeExpression(ChespelTree t) {
        TypeInfo type_info = null;
        // atomic expression: it has a type by itself
        switch (t.getType()) {
            case ChespelLexer.ID:    
                type_info = symbolTable.getVariableType(t.getText());
                break;
            case ChespelLexer.VOID_TYPE:
                type_info = new TypeInfo("VOID");
                break;
            case ChespelLexer.BOOLEAN:
                type_info = new TypeInfo("BOOLEAN");
                break;
            case ChespelLexer.FUNCALL:
                ArrayList<TypeInfo> header = new ArrayList<TypeInfo>();
                ChespelTree params = t.getChild(1);
                for (int i = 0; i < params.getChildCount(); ++i) {
                    header.add(getTypeExpression(params.getChild(i)));
                }
                if (header.size() == 0) header.add(new TypeInfo());
                type_info = symbolTable.getFunctionType(t.getChild(0).getText(), header);
                break;
            case ChespelLexer.STRING:
                type_info = new TypeInfo("STRING");
                break;
            case ChespelLexer.ROW_LIT:
                type_info = new TypeInfo("ROW");
                break;
            case ChespelLexer.COLUMN_LIT:
                type_info = new TypeInfo("COLUMN");
                break;
            case ChespelLexer.RANK_LIT:
                type_info = new TypeInfo("RANK");
                break;
            case ChespelLexer.CELL_LIT:
                type_info = new TypeInfo("CELL");
                break;
            case ChespelLexer.RANG_CELL_LIT:
                type_info = new TypeInfo("CELL",1);
                break;
            case ChespelLexer.RANG_ROW_LIT:
                type_info = new TypeInfo("ROW",1);
                break;
            case ChespelLexer.RANG_RANK_LIT:
                type_info = new TypeInfo("RANK",1);
                break;
            case ChespelLexer.RANG_FILE_LIT:
                type_info = new TypeInfo("FILE",1);
                break;
            case ChespelLexer.BOARD_LIT:
                if (t.getText().equals("cells")) type_info = new TypeInfo("CELL",1);
                else if (t.getText().equals("rows")) type_info = new TypeInfo("ROW",1);
                else if (t.getText().equals("files")) type_info = new TypeInfo("FILE",1);
                else type_info = new TypeInfo("RANK",1);
                break;
            case ChespelLexer.PIECE_LIT:
                type_info = new TypeInfo("PIECE",1);
                break;
            case ChespelLexer.NUM:
                type_info = new TypeInfo("NUMERIC");
                break;
            case ChespelLexer.EMPTY_LIST:
                type_info = new TypeInfo("EMPTY_ARRAY");
                break;
            case ChespelLexer.LIST_ATOM:
                TypeInfo list_type = getTypeExpression(t.getChild(0));
                for (int i = 1; i < t.getChildCount(); ++i) {
                    assert list_type.equals(getTypeExpression(t.getChild(i)));
                }
                type_info = new TypeInfo(list_type, 1);
                break;
            case ChespelLexer.SELF:
            case ChespelLexer.RIVAL:
                type_info = new TypeInfo("PERSON");
        }

        if (type_info != null) {
            t.setTypeInfo(type_info);
            return;
        }

        //unary operations
        TypeInfo t0 = getTypeExpression(t.getChild(0));
        switch (t.getType()) {
            case ChespelLexer.NOT:
                type_info = t0.checkTypeUnaryBoolean();
                break;
            case ChespelLexer.MINUS:
            case ChespelLexer.PLUS:
                if (t.getChildCount() != 1) break; //it is not the unary case
                type_info = t0.checkTypeUnaryArithmetic();
                break;
        }
        
        if (type_info != null) {
            t.setTypeInfo(type_info);
            return;
        }

        // relational
        TypeInfo t1 = getTypeExpression(t.getChild(1));
        switch (t.getType()) {
            case ChespelLexer.OR:
            case ChespelLexer.AND:
                type_info = t0.checkTypeBooleanOp(t1);
                break;
            case ChespelLexer.DOUBLE_EQUAL:
            case ChespelLexer.NOT_EQUAL:
                type_info = t0.checkTypeEquality(t1);
                break;
            case ChespelLexer.LT:
            case ChespelLexer.LE:
            case ChespelLexer.GT:
            case ChespelLexer.GE:
                type_info = t0.checkTypeOrder(t1);
                break;
            case ChespelLexer.MUL:
            case ChespelLexer.DIV:
            case ChespelLexer.MINUS:
                type_info = t0.checkTypeArithmetic(t1);
                break;
            case ChespelLexer.PLUS:
                type_info = t0.checkTypeArithmetic(t1);
                //missing case: concatenation of arrays
                break;
            case ChespelLexer.IN:
                type_info = t0.checkTypeIn(t1);
                break;
        }

        assert type_info != null;
        t.setTypeInfo(type_info);
        return;
    }

    private void checkTypeListInstructions(ChespelTree listInstr) {
        assert listInstr.getType() == ChespelLexer.LIST_INSTR;
        for (int i = 0; i < listInstr.getChildCount(); ++i) {
            ChespelTree t = listInstr.getChild(i);
            TypeInfo varType, expressionType;
            String varName;
            switch (t.getType()) {
                case ChespelLexer.ASSIGN:
                    //the ASSIGN (:= in the AST) node has 2 sons
                    //the name of the variable, and the expression that has to be
                    //evaluated and assigned to it
                    varName = t.getChild(0).getText();
                    varType = symbolTable.getVariableType(varName); //checks that it is already defined               
                    expressionType = getTypeExpression(t.getChild(1));
                    //check that the assigned value is coherent with the type of the variable
                    assert varType.equals(expressionType);
                    break;
                case ChespelLexer.VAR_DECL:
                    //the VAR_DECL node has 2 sons
                    //the first is the type of the variable
                    //the second consists of an ASSIGN (:= in the AST) node, which has two sons:
                    //the name of the variable, and the expression that has to be
                    //evaluated and assigned to it
                    TypeInfo declType = getTypeFromDeclaration(t.getChild(0));
                    ChespelTree assignmentNode = t.getChild(1);
                    varName = assignmentNode.getChild(0).getText();
                    expressionType = getTypeExpression(assignmentNode.getChild(1));
                    //check that the assigned value is coherent with the type of the variable
                    assert declType.equals(expressionType);
                    //add it to the current visibility scope
                    //this also checks that the variable is not already defined
                    symbolTable.defineVariable(varName, declType);
                    break;
                case ChespelLexer.FORALL:
                    //the FORALL node has 2 sons
                    //the first defines the variable of the loop:
                    //it has two sons, the name of the variable,
                    //and the expression (which should be an array)
                    //from which the variable draws values.
                    //the second is a list of instructions where the loop variable
                    //is defined
                    ChespelTree varDefNode = t.getChild(0);
                    String loopVarName = varDefNode.getChild(0).getText();
                    ChespelTree arrayExpression = varDefNode.getChild(1);
                    TypeInfo arrayType = getTypeExpression(arrayExpression);
                    varType = arrayType.getArrayContent();

                    //new visibility scope for the list of instructions of the forall statement
                    symbolTable.pushVariableTable();
                    //with the loop variable defined in it
                    symbolTable.defineVariable(loopVarName, varType);
                    checkTypeListInstructions(t.getChild(1));
                    symbolTable.popVariableTable();
                    break;
                case ChespelLexer.IF:
                case ChespelLexer.WHILE:
                    //the IF and WHILE nodes have 2 sons
                    //the first is a boolean expression
                    //the second is a list of instructions with a new visibility scope
                    assert getTypeExpression(t.getChild(0)).isBoolean();
                    symbolTable.pushVariableTable();
                    checkTypeListInstructions(t.getChild(1));
                    symbolTable.popVariableTable();
                    break;
                case ChespelLexer.RETURN:
                    //we only check that the expression of the return is correct in itself
                    //(i.e., that it does not reference non defined variables, ... )
                    //but not whether it has the type that the containing function is
                    //declared to return that will be done at a later stage
                    getTypeExpression(t.getChild(0));
                    break;

                case ChespelLexer.SCORE:
                    //check that we are modifying the score with a numeric value
                    assert getTypeExpression(t.getChild(0)).isNumeric();
                    break;
                    
                default:
                    assert false;
            }
        }
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
