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
import java.util.HashSet;
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

    private String def_name = "";
    private int def_line = -1;
    private String def_type = "";
    
    public String where() {
        return def_type + ": " + def_name + ", line " + def_line;
    }

    private ErrorStack errors;

    public void addError(String error) {
        errors.addError(linenumber, error);
    }

    public void addErrorContext(String error) {
        errors.addError(linenumber, error, where());
    }

    /**
     * Constructor of the compiler. It prepares the main
     * data structures for the translation to C.
     */
    public ChespelCompiler(ChespelTree T, ErrorStack E) {
        assert T != null;
        //PreProcessAST(T); // Some internal pre-processing of the AST
        symbolTable = new SymbolTable(); // Creates the memory of the virtual machine
        parseDefinitions(T);
        errors = E;
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
                    assert false : "Not a definition";
            }
        }
    }

    /** Compiles the program by translating the sentences 
      * from Chespel to the C++ class of the chess state evalation. 
      */
    public void compile() throws CompileException {
        checkTypes();
        if (errors.hasErrors()) throw new CompileException("Compile errors.");
        //output header of the .cpp file
        //compile
    }

    private void checkTypes() {
        checkGlobalTypes(); 
        checkFunctionTypes();
        checkRuleTypes();
    }

    private void checkGlobalTypes() {
        //System.out.println("Global variable declarations");
        def_type = "Global";
        for (ChespelTree T : GlobalDefinitions) {
            def_line = T.getLine();
            def_name = T.getChild(1).getText();
            TypeInfo return_type = getTypeFromDeclaration(T.getChild(0));
            //System.out.println(T.getChild(1).getText() + ": " + return_type.toString());
            TypeInfo expression_type = getTypeExpression(T.getChild(2));
            setLineNumber(T);
            if (! return_type.equals(expression_type)) addError("Global " + T.getChild(1).getText() + " is declared as " + return_type.toString() + " but its expression is of type " + expression_type.toString());
            try {
                symbolTable.defineGlobal(T.getChild(1).getText(), return_type);
            } catch (CompileException e) {
                addError(e.getMessage());
            }
        }
    }

    private void checkFunctionTypes() {
        //System.out.println("Function declarations");
        def_type = "Function";
        for (ChespelTree T : FunctionDefinitions) {
            def_line = T.getLine();
            def_name = T.getChild(1).getText();
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
            setLineNumber(T);
            try {
                symbolTable.defineFunction(name, return_type, header);
            } catch (CompileException e) {
                addError(e.getMessage());
            }
            //System.out.println(return_type.toString() + " " + name + " " + header.toString());
        }

        for (ChespelTree T : FunctionDefinitions) {
            // define arguments as variables
            def_line = T.getLine();
            def_name = T.getChild(1).getText();
            ChespelTree args = T.getChild(2);
            symbolTable.pushVariableTable();
            for (int i = 0; i < args.getChildCount() ; ++i) {
                ChespelTree arg = args.getChild(i);
                TypeInfo arg_type = getTypeFromDeclaration(arg.getChild(0));
                String arg_name = arg.getChild(1).getText();
                if (arg.getChild(1).getType() == ChespelLexer.PREF) arg_name = arg_name.substring(1); // drop '&' of token's text
                setLineNumber(args.getChild(i));
                try {
                    symbolTable.defineVariable(arg_name, arg_type);
                } catch (CompileException e) {
                    addError(e.getMessage());
                }
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
        //System.out.println("Rule declarations");
        def_type = "Rule";
        for (ChespelTree T : ruleDefinitions) {
            String name = T.getChild(0).getText();
            def_line = T.getLine();
            def_name = name;
            ChespelTree optionsNode = T.getChild(1);
            HashSet<String> opts = new HashSet<String>();
            for (int i = 0; i < optionsNode.getChildCount() ; ++i) {
                String opt = optionsNode.getChild(i).getText();
                setLineNumber(optionsNode.getChild(i));
                if (opts.contains(opt)) addError("Option " + opt + " repeated in the header of the rule.");
                opts.add(opt);
            }
            setLineNumber(T);
            try {
                symbolTable.defineRule(name, opts);
            } catch (CompileException e) {
                addError(e.getMessage());
            }
            //System.out.println(name + " " + opts.toString());
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
                setLineNumber(t);
                TypeInfo returnExprType = getTypeExpression(t.getChild(0));
                if (!returnType.equals(returnExprType)) addErrorContext("Return of function is declared as " + returnType.toString() + " but expression in return statement is of type " + returnExprType.toString());
            }
        }
    }

    private void checkNoScoreStatements(ChespelTree listInstr) {
        assert listInstr.getType() == ChespelLexer.LIST_INSTR;
        for (int i = 0; i < listInstr.getChildCount(); ++i) {
            ChespelTree t = listInstr.getChild(i);
            setLineNumber(t);
            if (t.getType() == ChespelLexer.SCORE) addErrorContext("Score statement in a function");
        }
    }

    private void checkOnlyVoidReturnStatements(ChespelTree listInstr) {
        TypeInfo voidType = new TypeInfo("VOID");
        assert listInstr.getType() == ChespelLexer.LIST_INSTR;
        for (int i = 0; i < listInstr.getChildCount(); ++i) {
            ChespelTree t = listInstr.getChild(i);
            if (t.getType() == ChespelLexer.RETURN) {
                setLineNumber(t);
                TypeInfo returnType = getTypeExpression(t.getChild(0));
                if (!returnType.equals(voidType)) addErrorContext("Non-void return in a rule");
            }
        }
    }

    private TypeInfo getTypeFromDeclaration(ChespelTree t) {
        setLineNumber(t);
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
                addErrorContext("Not a type declaration " + t.toString());
                return new TypeInfo("GENERIC");
        }
    }

    private TypeInfo getTypeExpression(ChespelTree t) {
        assert t != null;
        setLineNumber(t);
        TypeInfo type_info = t.getInfo();
        if (type_info == null) {
            computeTypeExpression(t);
            type_info = t.getInfo();
        }
        return type_info;
    }

    private void computeTypeExpression(ChespelTree t) {
        TypeInfo type_info = null;
        setLineNumber(t);
        // atomic expression: it has a type by itself
        try {
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
                    type_info = new TypeInfo("FILE");
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
                    type_info = new TypeInfo("GENERIC_ARRAY");
                    break;
                case ChespelLexer.LIST_ATOM:
                    TypeInfo list_type = getTypeExpression(t.getChild(0));
                    for (int i = 1; i < t.getChildCount(); ++i) {
                        if (!list_type.equals(getTypeExpression(t.getChild(i)))) addErrorContext("Elements of the list aren't of the same type");
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

            //unary operations and DOT functions
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
                case ChespelLexer.DOT:
                    ArrayList<TypeInfo> args = new ArrayList<TypeInfo>();
                    args.add(t0);
                    type_info = symbolTable.getFunctionType(t.getChild(1).getText(), args);
                    break;
            }
            
            if (type_info != null) {
                t.setTypeInfo(type_info);
                return;
            }

            // relational operations
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
                case ChespelLexer.CONCAT:
                    type_info = t0.checkTypeConcat(t1);
                    break;
                case ChespelLexer.L_BRACKET:
                    if (!t1.isNumeric()) addErrorContext("The position of the array must be a numeric, but it's " + t1.toString() + " instead");
                    try {
                        type_info = t0.getArrayContent();
                    } catch (CompileException e) {
                        addErrorContext(e.getMessage());
                        type_info = new TypeInfo("GENERIC");
                    }
            }
        } catch (CompileException e) {
            addErrorContext(e.getMessage());
            type_info = new TypeInfo("GENERIC");
        }

        assert type_info != null : linenumber;
        t.setTypeInfo(type_info);
        return;
    }

    private void checkTypeListInstructions(ChespelTree listInstr)  {
        assert listInstr.getType() == ChespelLexer.LIST_INSTR : linenumber;
        for (int i = 0; i < listInstr.getChildCount(); ++i) {
            ChespelTree t = listInstr.getChild(i);
            setLineNumber(t);
            TypeInfo varType, expressionType;
            String varName;
            switch (t.getType()) {
                case ChespelLexer.ASSIGN:
                    //the ASSIGN (:= in the AST) node has 2 sons
                    //the name of the variable, and the expression that has to be
                    //evaluated and assigned to it
                    varName = t.getChild(0).getText();
                    try {
                        varType = symbolTable.getVariableType(varName); //checks that it is already defined
                    } catch (CompileException e) {
                        addErrorContext(e.getMessage());
                        varType = new TypeInfo("GENERIC");
                    }
                    expressionType = getTypeExpression(t.getChild(1));
                    //check that the assigned value is coherent with the type of the variable
                    if (!varType.equals(expressionType)) addErrorContext("Assignment type " + expressionType.toString() + " is not of expected type " + varType.toString());
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
                    if (!declType.equals(expressionType)) addErrorContext("Assignment type " + expressionType.toString() + " is not of expected type " + declType.toString());

                    //add it to the current visibility scope
                    //this also checks that the variable is not already defined
                    try {
                        symbolTable.defineVariable(varName, declType);
                    } catch (CompileException e) {
                        addErrorContext(e.getMessage());
                    }
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
                    try {
                        varType = arrayType.getArrayContent();
                    } catch (CompileException e) {
                        addErrorContext(e.getMessage());
                        varType = new TypeInfo ("GENERIC");
                    }

                    //new visibility scope for the list of instructions of the forall statement
                    symbolTable.pushVariableTable();
                    //with the loop variable defined in it
                    try {
                        symbolTable.defineVariable(loopVarName, varType);
                    } catch (CompileException e) {
                        addErrorContext(e.getMessage());
                    }
                    checkTypeListInstructions(t.getChild(1));
                    symbolTable.popVariableTable();
                    break;
                case ChespelLexer.IF:
                case ChespelLexer.WHILE:
                    //the IF and WHILE nodes have 2 sons
                    //the first is a boolean expression
                    //the second is a list of instructions with a new visibility scope
                    TypeInfo condition_type = getTypeExpression(t.getChild(0));
                    if (!condition_type.isBoolean() ) addErrorContext( "Expected boolean in instruction if/while but found " + condition_type.toString() + " instead");
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
                    TypeInfo scoring_type = getTypeExpression(t.getChild(0));
                    if (!scoring_type.isNumeric()) addErrorContext("Expected numeric in score but found " + scoring_type.toString() + " instead");
                    break;
                    
                default:
                    assert false;
            }
        }
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

}
