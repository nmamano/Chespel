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

    private ErrorStack errors; //and warnings

    public void addError(String error) {
        errors.addError(linenumber, error);
    }

    public void addErrorContext(String error) {
        errors.addError(linenumber, error, where());
    }

    public void addWarning(String warning) {
        errors.addWarning(linenumber, warning);
    }

    public void addWarningContext(String warning) {
        errors.addWarning(linenumber, warning, where());
    }

    /**
     * Constructor of the compiler. It prepares the main
     * data structures for the translation to C.
     */
    public ChespelCompiler(ChespelTree T, ErrorStack E) {
        assert T != null;
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
        addPredefinedFunctionsToSymbolTable();

        semanticAnalysis();
        if (errors.hasErrors()) throw new CompileException("Compile errors.");
        if (errors.hasWarnings()) System.err.print(errors.getWarnings());
        codeTranslation();
    }

    void codeTranslation() { //to do
        //output header of the .cpp file
        //compile
    }

    /*
    carries out typechecking and detection of other errors and warnings
    */
    private void semanticAnalysis() {
        analyzeGlobals(); 
        analyzeFunctions();
        analyzeRules();
    }


    private void analyzeGlobals() {
        def_type = "Global";
        for (ChespelTree T : GlobalDefinitions) {
            def_line = T.getLine();
            def_name = T.getChild(1).getText();
            TypeInfo return_type = getTypeFromDeclaration(T.getChild(0));
            TypeInfo expression_type = getTypeExpression(T.getChild(2));
            setLineNumber(T);
            if (! return_type.equals(expression_type)) {
                addError("Global " + T.getChild(1).getText() + " is declared as " +
                    return_type.toString() + " but its expression is of type " +
                    expression_type.toString());
            }
            try {
                symbolTable.defineGlobal(T.getChild(1).getText(), return_type);
            } catch (CompileException e) {
                addError(e.getMessage());
            }
        }
    }

    private void analyzeFunctions() {
        def_type = "Function";

        /* First pass: add all functions to the symbol table */
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
        }

        /* Second pass: typecheking of the function instructions */
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
            checkReturnType(listInstr, returnType);
            checkNoScoreStatements(listInstr);
            if (returnType.equals(new TypeInfo("VOID"))) {
                checkAtLeastOneParameterByReference(T);
            }
            else {
                checkAlwaysReachReturn(listInstr);
            }
            checkNoUnreacheableInstructions(listInstr);
            
            checkNoUnusedVariables(listInstr);
            symbolTable.popVariableTable();
        }
    }

    private void analyzeRules() {
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
            ChespelTree listInstr = T.getChild(2);
            if (T.getChildCount() > 3) { // rule has doif
                ChespelTree doif = T.getChild(3);
                TypeInfo t = getTypeExpression(doif.getChild(0));
                if (! t.isBool()) addError("'Doif' of rule '" + name + "' is "+ t.toString() + " instead of BOOLEAN");
            }
            symbolTable.pushVariableTable();
            checkTypeListInstructions(listInstr);
            checkReturnType(listInstr, new TypeInfo("VOID"));
            checkContainsScore(listInstr);
            checkNoUnreacheableInstructions(listInstr);

            checkNoUnusedVariables(listInstr);
            symbolTable.popVariableTable();
        }
    }

    private void checkAtLeastOneParameterByReference(ChespelTree function) {
        ChespelTree args = function.getChild(2);
        boolean foundRef = false;
        for (int i = 0; i < args.getChildCount() ; ++i) {
            ChespelTree arg = args.getChild(i);
            setLineNumber(arg);
            if (arg.getChild(1).getType() == ChespelLexer.PREF) foundRef = true;
        }
        if (!foundRef) {
            String name = function.getChild(1).getText();
            addWarning("void function '" + name + "' without parameters by reference");
        }
    }

    private void checkAlwaysReachReturn(ChespelTree listInstr) {
        setLineNumber(listInstr);
        if (! alwaysReachReturn(listInstr)) {
            //note: is the line of the warning message set correctly?
            addWarningContext("Return statement not reached through every possible branch");
        }
    }

    private boolean alwaysReachReturn(ChespelTree listInstr) {
        assert listInstr.getType() == ChespelLexer.LIST_INSTR;
        for (int i = 0; i < listInstr.getChildCount(); ++i) {
            ChespelTree t = listInstr.getChild(i);
            setLineNumber(t);
            switch(t.getType()) {
                case ChespelLexer.RETURN:
                    return true;
                case ChespelLexer.IF:
                    if (t.getChildCount() == 3) {
                        if (alwaysReachReturn(t.getChild(1)) && alwaysReachReturn(t.getChild(2))) {
                            return true;
                        }
                    }
            }
        }
        return false;
    }

    private void checkNoUnusedVariables(ChespelTree listInstr) {
        //not implemented yet
    }

    private void checkNoUnreacheableInstructions(ChespelTree listInstr) {
        //unreacheable instructions are those after a return
        //or after an if in which all branches always reached a return
        assert listInstr.getType() == ChespelLexer.LIST_INSTR;
        for (int i = 0; i < listInstr.getChildCount()-1; ++i) { //don't check last instruction
            ChespelTree t = listInstr.getChild(i);
            setLineNumber(t);
            switch(t.getType()) {
                case ChespelLexer.RETURN:
                    addWarningContext("Unreacheable instructions after return statement");
                    break;
                case ChespelLexer.FORALL:
                case ChespelLexer.WHILE:
                    checkNoUnreacheableInstructions(t.getChild(1));
                    break;
                case ChespelLexer.IF:
                    checkNoUnreacheableInstructions(t.getChild(1));
                    if (t.getChildCount() == 3) { //else branch
                        checkNoUnreacheableInstructions(t.getChild(2));
                        if (alwaysReachReturn(t.getChild(1)) && alwaysReachReturn(t.getChild(2))) {
                            addWarningContext("Unreacheable instructions after if/else statement");
                        }
                    }
            }
        }
    }

    private void addPredefinedFunctionsToSymbolTable() throws CompileException {
        //System.out.println("Predefined function declarations");
        try {
            BufferedReader br = new BufferedReader(new FileReader("./src/compiler/predefinedFunctions.txt"));
            try  {
                String line = br.readLine();

                while (line != null) {
                    String[] words = line.split("[ ()]+");
                    TypeInfo return_type = TypeInfo.parseString(words[0]);
                    String name = words[1];
                    TypeInfo param = TypeInfo.parseString(words[2]);
                    ArrayList<TypeInfo> parameters = new ArrayList<TypeInfo>();
                    parameters.add(param);
                    //System.out.println(return_type.toString() + " " + name + " " + parameters.toString());
                    symbolTable.defineFunction(name, return_type, parameters);

                    line = br.readLine();
                }
            } catch(IOException e) {
                assert false : e.getMessage();
            } finally {
                try {
                    br.close();
                } catch(IOException e) { assert false : "total failure"; }
            }
        } catch (FileNotFoundException e) {
            assert false : "Could not find predefinedFunctions.txt";
        }
    }

    private void checkReturnType(ChespelTree listInstr, TypeInfo returnType) {
        assert listInstr.getType() == ChespelLexer.LIST_INSTR;
        for (int i = 0; i < listInstr.getChildCount(); ++i) {
            ChespelTree t = listInstr.getChild(i);
            setLineNumber(t);
            switch(t.getType()) {
                case ChespelLexer.RETURN:
                    TypeInfo returnExprType = getTypeExpression(t.getChild(0));
                    if (!returnType.equals(returnExprType)) {
                        addErrorContext("Return of function is declared as " +
                            returnType.toString() + " but expression in return statement is of type " +
                            returnExprType.toString());
                    }
                    break;
                case ChespelLexer.FORALL:
                case ChespelLexer.WHILE:
                    checkReturnType(t.getChild(1), returnType);
                    break;
                case ChespelLexer.IF:
                    checkReturnType(t.getChild(1), returnType);
                    if (t.getChildCount() == 3) checkReturnType(t.getChild(2), returnType); //else branch
            }
        }
    }

    private void checkNoScoreStatements(ChespelTree listInstr) {
        assert listInstr.getType() == ChespelLexer.LIST_INSTR;
        for (int i = 0; i < listInstr.getChildCount(); ++i) {
            ChespelTree t = listInstr.getChild(i);
            setLineNumber(t);
            switch(t.getType()) {
                case ChespelLexer.SCORE:
                    addErrorContext("score statement in a function");
                    break;
                case ChespelLexer.FORALL:
                case ChespelLexer.WHILE:
                    checkNoScoreStatements(t.getChild(1));
                    break;
                case ChespelLexer.IF:
                    checkNoScoreStatements(t.getChild(1));
                    if (t.getChildCount() == 3) checkNoScoreStatements(t.getChild(2)); //else branch
            }
        }
    }

    private void checkContainsScore(ChespelTree listInstr) {
        setLineNumber(listInstr);
        if (!containsScore(listInstr)) addErrorContext("No score statement in rule");
    }

    private boolean containsScore(ChespelTree listInstr) {
        assert listInstr.getType() == ChespelLexer.LIST_INSTR;
        boolean hasScore = false;
        for (int i = 0; i < listInstr.getChildCount(); ++i) {
            ChespelTree t = listInstr.getChild(i);
            switch(t.getType()) {
                case ChespelLexer.SCORE:
                    hasScore = true;
                    break;
                case ChespelLexer.FORALL:
                case ChespelLexer.WHILE:
                    if (containsScore(t.getChild(1))) hasScore = true;
                    break;
                case ChespelLexer.IF:
                    if (containsScore(t.getChild(1))) hasScore = true;
                    if (t.getChildCount() == 3 && containsScore(t.getChild(2))) hasScore = true;
            }
        }
        return hasScore;
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
                return new TypeInfo("NUM");
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
                case ChespelLexer.BOOL:
                    type_info = new TypeInfo("BOOL");
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
                case ChespelLexer.FILE_LIT:
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
                    type_info = new TypeInfo("NUM");
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
                    type_info = new TypeInfo("PLAYER");
            }

            if (type_info != null) {
                t.setTypeInfo(type_info);
                return;
            }

            //unary operations and DOT functions
            TypeInfo t0 = getTypeExpression(t.getChild(0));
            switch (t.getType()) {
                case ChespelLexer.NOT:
                    type_info = t0.checkTypeUnaryBool();
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
                    type_info = t0.checkTypeBoolOp(t1);
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
                    if (!t1.isNum()) addErrorContext("The position of the array must be a Num, but it's " + t1.toString() + " instead");
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
                    if (!condition_type.isBool() ) addErrorContext( "Expected boolean expression in instruction if/while but found " + condition_type.toString() + " instead");
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
                    //check that we are modifying the score with a num value
                    TypeInfo scoring_type = getTypeExpression(t.getChild(0));
                    if (!scoring_type.isNum()) addErrorContext("Expected Num in score but found " + scoring_type.toString() + " instead");
                    break;
                    
                default:
                    assert false;
            }
        }
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
