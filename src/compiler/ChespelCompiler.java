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
import java.util.Iterator;
import java.io.*;

/** Class that implements the compiler of the language. */

public class ChespelCompiler {

    /** Table of symbols. */
    private SymbolTable symbolTable;

    private LinkedList<ChespelTree> GlobalDefinitions;

    private LinkedList<ChespelTree> FunctionDefinitions;
    
    private LinkedList<ChespelTree> RuleDefinitions;

    private ChespelTree configOptionsTree;
    private ConfigOptions configOptions;

    private static String outfile = "";
    private static BufferedWriter writer = null;

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

    public void treatUnusedVariables() {
        ArrayList<String> unused_variables = symbolTable.getUnusedVariables();
        for (String s : unused_variables) {
            String[] ss = s.split("-");
            errors.addWarning(Integer.parseInt(ss[0]), "Variable '" + ss[1] + "' defined but never used", where());
        }
    }

    public void treatUnusedGlobals() {
        ArrayList<String> unused_globals = symbolTable.getUnusedGlobals();
        for (String s : unused_globals) {
            String[] ss = s.split("-");
            errors.addWarning(Integer.parseInt(ss[0]), "Global '" + ss[1] + "' defined but never used");
        }
    }

    /**
     * Constructor of the compiler. It prepares the main
     * data structures for the translation to C.
     */
    public ChespelCompiler(ChespelTree T, ErrorStack E, String outfile) {
        assert T != null;
        symbolTable = new SymbolTable(); // Creates the memory of the virtual machine
        parseDefinitions(T.getChild(1));
        errors = E;
        configOptionsTree = T.getChild(0);
        configOptions = new ConfigOptions();
        this.outfile = outfile;
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
                    assert false : "Not a definition";
            }
        }
    }

    /** Compiles the program by translating the sentences 
      * from Chespel to the C++ class of the chess state evaluation. 
      */
    public void compile() throws CompileException, IOException {
        parseConfigOptions();
        addPredefinedFunctionsToSymbolTable();

        semanticAnalysis();
        // Has no sense trying to infer empty list types when
        // in error. Furthermore, the only error which infering types
        // could yield is not-infering array type in forall statement.
        if (errors.hasErrors()) throw new CompileException("Compile errors.");
        inferEmptyArrayType();
        if (errors.hasErrors()) throw new CompileException("Compile errors.");
        if (errors.hasWarnings()) System.err.print(errors.getWarnings());
        codeTranslation();
    }

    private void codeTranslation() throws IOException { 
        // open file and write the code
        try {
            File output_file = new File(outfile);
            writer = new BufferedWriter(new FileWriter(output_file)); 
            writeCode();
        }
        finally {
            try { writer.close(); } catch (Exception e) {}
        }
    }

    private static String basic_indent = "    ";

    private String indentation = "";

    private void incr_indentation() {
        indentation += basic_indent;
    }

    private void decr_indentation() {
        indentation = indentation.substring(basic_indent.length());
    }

    private int UID = 0;

    private String getUID() {
        ++UID;
        return "" + UID;
    }
    private void writeCode() throws IOException {
        array_literal_definitions = new LinkedList<LinkedList<String>> ();
        writeIncludes();
        writeOptions();
        writeGlobals();
        writeHeaders();
        writePreamble();
        writeFunctions();
        writeRules();
        writeOpnEval();
        writeMidEval();
        writeEndEval();
    }

    private void write(String s) throws IOException {
        writer.write(s);
    }

    private void writeLn(String s) throws IOException {
        writer.write(s);
        writer.newLine();
    }

    private void writeIncludes() throws IOException {
        writeLn("#include \"generated_eval.h\"");
        writeLn("#include \"predefined_functions.h\"");
        writeLn("#include \"predefined_functions.tcc\"");
        writeLn("using namespace std;");
        writeLn("");

    }

    private void writeOptions() throws IOException {
        writeLn("// Configs");
        String prefix = "const ";
        String sufix = ";";
        for (ChpOption o : configOptions.getOptions()) {
            writeLn(prefix + o.c_type + " " + o.name + " = " + o.value.toString() + sufix);
        }
        writeLn("");
    }

    LinkedList<ArrayList<String>> preamble_init;

    private void writeGlobals() throws IOException {
        preamble_init = new LinkedList<ArrayList<String>> ();
        writeLn("// Globals");
        for (ChespelTree T : GlobalDefinitions) {
            String t = typeCode(getTypeFromDeclaration(T.getChild(0)));
            String id = T.getChild(1).getText();
            int tmp = array_literal_definitions.size();
            String s = exprCode(T.getChild(2));
            if (array_literal_definitions.size() - tmp == 0) { // initialization doesn't require array literals
                writeLn(t + " " + id + " = " + s + ";");
            }
            else {
                writeLn(t + " " + id +";"); // only declare
                ArrayList<String> global_init = new ArrayList<String>();
                global_init.add(id);
                global_init.add(s);
                preamble_init.add(global_init);
            }
        }
        writeLn("");
    }

    private void writeHeaders() throws IOException {
        writeLn("// Functions Headers");
        for (ChespelTree T : FunctionDefinitions) {
            writeLn(getFunctionHeader(T) + ";");
        }
        writeLn("");
        writeLn("// Rules Headers");
        for (ChespelTree T : RuleDefinitions) {
            writeLn(getRuleHeader(T) + ";");
        }
        writeLn("");
    }

    private void writePreamble() throws IOException {
        writeLn("// Preamble for array initialization");
        writeLn("void preamble() {");
        incr_indentation();
        write(addArrayLiteral());
        for (ArrayList<String> assign : preamble_init) {
            write(indentation + assign.get(0) + " = " + assign.get(1) + ";\n");
        }
        decr_indentation();
        writeLn("}\n");
        preamble_init = null;
    }

    private String getFunctionHeader(ChespelTree T) {
        String t = typeCode(getTypeFromDeclaration(T.getChild(0)));
        String name = T.getChild(1).getText();
        String params = "";
        for (int i = 0; i < T.getChild(2).getChildCount(); ++i) {
            params += getParamCode(T.getChild(2).getChild(i)) + ", ";
        }
        if (params.equals("")) params = "  ";
        return (t + " func_" + name + "(" + params.substring(0,params.length()-2)+ ")");
    }

    private String getParamCode(ChespelTree t) {
        return typeCode(getTypeFromDeclaration(t.getChild(0))) + " "  + t.getChild(1).getText();
    }

    private String getRuleHeader(ChespelTree T) {
        String t = "long int";
        String name = T.getChild(0).getText();
        return (t + " rule_" + name + "()");
    }

    private void writeFunctions() throws IOException {
        writeLn("// Functions code");
        for (ChespelTree T : FunctionDefinitions) {
            writeLn(getFunctionHeader(T) + " {");
            incr_indentation();
            write(getListInstructionCode(T.getChild(3)));
            decr_indentation();
            writeLn("}");
            writeLn("");
        }
    }

    private void writeRules() throws IOException {
        writeLn("// Rules code");
        for (ChespelTree T : RuleDefinitions) {
            writeLn(getRuleHeader(T) + " {");
            incr_indentation();
            write(getListInstructionCode(T.getChild(2)));
            decr_indentation();
            writeLn("}");
            writeLn("");
        }
    }

    private void writeOpnEval() throws IOException {
        writeLn("// Opening eval");
        writeLn("long int opn_eval() {");
        incr_indentation();
        writeEval(EvalType.OPENING);
        decr_indentation();
        writeLn("}\n");
    }

    private void writeMidEval() throws IOException {
        writeLn("// Midgame eval");
        writeLn("long int mid_eval() {");
        incr_indentation();
        writeEval(EvalType.MIDGAME);
        decr_indentation();
        writeLn("}\n");
    }

    private void writeEndEval() throws IOException {
        writeLn("// Endgame eval");
        writeLn("long int end_eval() {");
        incr_indentation();
        writeEval(EvalType.ENDGAME);
        decr_indentation();
        writeLn("}\n");
    }

    private enum EvalType {
        OPENING,
        MIDGAME,
        ENDGAME
    } ;

    private void writeEval(EvalType t) throws IOException {
        ArrayList<ChespelTree> symetric_rules = new ArrayList<ChespelTree>();
        writeLn(indentation + "long int score = 0;");
        String opt = ""; //dummy inicialization
        switch (t) {
            case OPENING:
                opt = "opening";
                break;
            case MIDGAME:
                opt = "midgame";
                break;
            case ENDGAME:
                opt = "endgame";
                break;
        }
        for (ChespelTree T : RuleDefinitions) {
            HashSet<String> rule_opt = symbolTable.getRuleOptions(T.getChild(0).getText());
            if (rule_opt.contains(opt) || (!rule_opt.contains("opening") &&
                !rule_opt.contains("midgame") && !rule_opt.contains("endgame"))) {
                writeLn(indentation + "score += rule_" + T.getChild(0).getText() + "();"); // call to function
                if (rule_opt.contains("sym")) symetric_rules.add(T);
            }
        }
        writeLn(indentation + "reset();");
        if (symetric_rules.size() > 0) {
            writeLn(indentation + "invert_players();");
            for (ChespelTree T : symetric_rules) {
                writeLn(indentation + "score -= rule_" + T.getChild(0).getText() + "();");
            }
        }
        writeLn(indentation + "return score;");
    }

    private LinkedList<LinkedList<String>> array_literal_definitions;

    private String addArrayLiteral() {
        String result = "";
        for (LinkedList arr_def : array_literal_definitions) {
            Iterator<String> it2 = arr_def.iterator();
            String name = it2.next();
            String type = it2.next();
            result += indentation;
            result += type; // add type definition
            result += " " + name; // add name
            result += " = " + type + "();\n"; // add initial vector
            while (it2.hasNext()) {
                result += indentation + name + ".push_back(" + it2.next() + ");\n";
            }
        }
        array_literal_definitions = new LinkedList<LinkedList<String>>();
        return result;
    }

    private String getListInstructionCode(ChespelTree T) {
        String s = "";
        for (int i = 0; i < T.getChildCount(); ++i) {
            s += sentenceCode(T.getChild(i)) + "\n";
        }
        return s;
    }


    private String sentenceCode(ChespelTree T) {
        String body, body2, instr = "";
        switch (T.getType()) {
            case ChespelLexer.VAR_DECL:
                String type = typeCode(getTypeFromDeclaration(T.getChild(0)));
                String names = "";
                ChespelTree var_defs = T.getChild(1);
                String initialization = "";
                for (int i = 0; i < var_defs.getChildCount(); ++i) {
                    ChespelTree var = var_defs.getChild(i);
                    if (var.getType() == ChespelLexer.ID)
                        names += var.getText() + ", ";
                    else {
                        names += var.getChild(0).getText() + ", ";
                        String tmp = sentenceCode(var);
                        initialization += addArrayLiteral() + tmp + "\n";
                        //initialization += tmp + "\n";
                    }
                }
                String result = indentation + type + " " + names.substring(0, names.length()-2) + ";\n" + initialization;
                return result.substring(0,result.length()-1);
            case ChespelLexer.ASSIGN:
                instr = exprCode(T.getChild(1));
                instr = T.getChild(0).getText() + " = " + instr + ";";
                break;
            case ChespelLexer.FORALL:
                incr_indentation();
                body = getListInstructionCode(T.getChild(1));
                decr_indentation();
                ChespelTree in_expr = T.getChild(0);
                String vector_name = exprCode(in_expr.getChild(1)); // get vector name
                String temp_it = "_it_" + getUID();
                String iterator_name = in_expr.getChild(0).getText();
                TypeInfo type_vec = getTypeExpression(in_expr.getChild(1));
                String vector_type = typeCode(type_vec);
                String content_type;
                try {
                    content_type = typeCode(type_vec.getArrayContent());
                } catch (Exception e) { throw new RuntimeException(e.getMessage()); }
                instr = "for (iterator<" + vector_type + " > "+temp_it+" = " + vector_name  + ".begin(); "+temp_it+" != " + vector_name + ".end(); ++"+temp_it+") {\n" + indentation + basic_indent +
                    content_type + " " + iterator_name + " = *"+temp_it+";\n" +
                    body + indentation +
                    "}";
                break;
            case ChespelLexer.IF:
                incr_indentation();
                body = getListInstructionCode(T.getChild(1));
                decr_indentation();
                body += indentation + "}";
                body2 = "";
                if (T.getChildCount() > 2) {
                    body2 = "\n" + indentation + "else {\n";
                    incr_indentation();
                    body2 += getListInstructionCode(T.getChild(2));
                    decr_indentation();
                    body2 += indentation + "}";
                }
                instr = "if (" + exprCode(T.getChild(0)) + ") {\n" + body + body2;
                break;
            case ChespelLexer.WHILE:
                incr_indentation();
                body = getListInstructionCode(T.getChild(1));
                decr_indentation();
                body += indentation + "}";
                instr = "while (" + exprCode(T.getChild(0)) + ") {\n" + body;
                break;
            case ChespelLexer.RETURN:
            case ChespelLexer.SCORE:
                if (T.getChild(0).getType() == ChespelLexer.VOID_TYPE) instr = "return;";
                else instr = "return " + exprCode(T.getChild(0)) + ";";
                break;
            case ChespelLexer.FUNCALL:
                String params = "";
                for (int i = 0; i < T.getChild(1).getChildCount(); ++i) {
                    params += exprCode(T.getChild(1).getChild(i)) + ", ";
                }
                if (params.equals("")) params = "  ";
                instr = "func_" + T.getChild(0).getText() +"(" + params.substring(0,params.length()-2) + ");";
                break;
        }
        String res = addArrayLiteral();
        //String res = "";
        return res + indentation + instr;
    }

    private String typeCode(TypeInfo t) {
        try {
            if (t.isGeneric()) return "int";
            if (t.isArray()) return "vector<" + typeCode(t.getArrayContent()) + (t.getArrayContent().isArray() && !t.getArrayContent().isGeneric() ? " >" : ">");
            else if (t.isBool()) return "bool";
            else if (t.isString()) return "string";
            else if (t.isVoid()) return "void";
            return "int";
        } catch (Exception e) { throw new RuntimeException(e.getMessage()); }
    }

    private String exprCode(ChespelTree t) {
        switch (t.getType()) {
            case ChespelLexer.EMPTY_LIST:
            case ChespelLexer.LIST_ATOM:
                //array_literals_definitions
                LinkedList<String> array_def = new LinkedList<String>();
                String array_type = typeCode(t.getInfo());
                String array_name = "_array_" + getUID();
                array_def.add(array_name);
                array_def.add(array_type);
                for (int i = 0; i < t.getChildCount(); ++i) {
                    array_def.add(exprCode(t.getChild(i)));
                }
                array_literal_definitions.add(array_def);
                return array_name;
                                         
            case ChespelLexer.BOOL:
                return t.getText();
            case ChespelLexer.FILE_LIT:
                return "get_file(" + t.getText().substring(1) + ")";
            case ChespelLexer.ROW_LIT:
                return "get_row(" + t.getText().substring(1) + ")";
            case ChespelLexer.RANK_LIT:
                return "get_rank(" + t.getText().substring(2) + ")";
            case ChespelLexer.CELL_LIT:
                return "get_cell(\"" + t.getText().substring(1) + "\")";
            case ChespelLexer.RANG_CELL_LIT:
                return "get_rang_cell(\"" + t.getText().substring(1,2) + "\",\"" + t.getText().substring(5) + "\" )";
            case ChespelLexer.RANG_ROW_LIT:
                return "get_rang_row(" + t.getText().substring(1,1) + "," + t.getText().substring(4) + ")";
            case ChespelLexer.RANG_FILE_LIT:
                return "get_rang_file(" + t.getText().substring(1,1) + "," + t.getText().substring(4) + ")";
            case ChespelLexer.RANG_RANK_LIT:
                return "get_rang_rank(" + t.getText().substring(2,2) + "," + t.getText().substring(5) + ")";
            case ChespelLexer.PIECE_LIT:
                String text = t.getText();
                int player = (text.charAt(0) == 's' ? 0 : 1);
                int piece;
                text = text.substring(1); // get piece name
                if (text.equals("pieces"))          piece = 0;
                else if (text.equals("pawns"))      piece = 1;
                else if (text.equals("bishops"))    piece = 2;
                else if (text.equals("rooks"))      piece = 3;
                else if (text.equals("knights"))    piece = 4;
                else if (text.equals("kings"))      piece = 5;
                else                                piece = 6; // queens
                return "get_pieces("+player+","+piece+")";
                        
            case ChespelLexer.BOARD_LIT:
            case ChespelLexer.SELF:
            case ChespelLexer.RIVAL:
                return t.getText() + "()";
            case ChespelLexer.NUM:
            case ChespelLexer.STRING:
            case ChespelLexer.ID:
                return t.getText();
            case ChespelLexer.FUNCALL:
                String params = "";
                for (int i = 0; i < t.getChild(1).getChildCount(); ++i) {
                    params += exprCode(t.getChild(1).getChild(i)) + ", ";
                }
                if (params.equals("")) params = "  ";
                return "func_" + t.getChild(0).getText() +"(" + params.substring(0,params.length()-2) + ")";
        }

        String s0 = exprCode(t.getChild(0));

        switch (t.getType()) {
            case ChespelLexer.NOT:
                return "!" + s0;
            case ChespelLexer.PLUS:
                if (t.getChildCount() == 1)
                    return "(" + s0 + ")";
            case ChespelLexer.MINUS:
                if (t.getChildCount() == 1)
                    return "-(" + s0 + ")";
        }

        String s1 = exprCode(t.getChild(1));
        String rel = "";
        switch (t.getType()) {
            case ChespelLexer.OR:
                rel = "or";
                break;
            case ChespelLexer.AND:
                rel = "and";
                break;
            case ChespelLexer.IN:
                return "in_expr("+s0 + "," + s1 + ")";
            case ChespelLexer.DOUBLE_EQUAL:
                if (getTypeExpression(t.getChild(0)).isArray())
                    return "array_equality(" + s0 + "," + s1 + ")";
                rel = "==";
                break;
            case ChespelLexer.NOT_EQUAL:
                if (getTypeExpression(t.getChild(0)).isArray())
                    return "!(array_equality(" + s0 + "," + s1 + "))";
                rel = "!=";
                break;
            case ChespelLexer.LT:
                rel = "<";
                break;
            case ChespelLexer.LE:
                rel = "<=";
                break;
            case ChespelLexer.GT:
                rel = ">";
                break;
            case ChespelLexer.GE:
                rel = ">=";
                break;
            case ChespelLexer.PLUS:
                rel = "+";
                break;
            case ChespelLexer.MINUS:
                rel = "-";
                break;
            case ChespelLexer.MUL:
                rel = "*";
                break;
            case ChespelLexer.DOT:
                return "func_" + s1 + "(" + s0 + ")";
            case ChespelLexer.L_BRACKET:
                return "access_array(" + s0 + "," + s1 + ")";
            case ChespelLexer.CONCAT:
                return "concat(" + s0 + "," + s1 + ")";
            default:
                assert false : "Relational expression not possible for exprCode";
        }
        return s0 + " " + rel + " " + s1;
    }

    /*
    carries out typechecking and detection of other errors and warnings
    */
    private void semanticAnalysis() {
        analyzeGlobals(); 
        analyzeFunctions();
        analyzeRules();
        treatUnusedGlobals();
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
                symbolTable.defineGlobal(T.getChild(1).getText(), return_type, linenumber);
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
            ArrayList<Boolean> references = new ArrayList<Boolean>();
            for (int i = 0; i < args.getChildCount(); ++i) {
                TypeInfo arg_type = getTypeFromDeclaration(args.getChild(i).getChild(0));
                Boolean ref = args.getChild(i).getChild(1).getText().substring(0,1).equals("&");
                header.add(arg_type);
                references.add(ref);
            }
            if (header.size() == 0) header.add(new TypeInfo());
            // define function
            setLineNumber(T);
            try {
                symbolTable.defineFunction(name, return_type, header, references);
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
                    symbolTable.defineVariable(arg_name, arg_type, linenumber);
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

            treatUnusedVariables();
            symbolTable.popVariableTable();
        }
    }

    private void analyzeRules() {
        def_type = "Rule";
        for (ChespelTree T : RuleDefinitions) {
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

            treatUnusedVariables();
            symbolTable.popVariableTable();
        }
    }

    private void inferEmptyArrayType() {
        // empty arrays on globals
        for (ChespelTree T : GlobalDefinitions) {
            def_name = "Global";
            def_line = T.getLine();
            def_name = T.getChild(1).getText();
            setLineNumber(T);
            inferEmptyArrayTypeExpr(getTypeFromDeclaration(T.getChild(0)), T.getChild(2));
        }

        // empty arrays on functions
        for (ChespelTree T : FunctionDefinitions) {
            def_name = "Function";
            def_line = T.getLine();
            def_name = T.getChild(1).getText();
            inferEmptyArrayTypeInstr(getTypeFromDeclaration(T.getChild(0)),T.getChild(3));
        }

        //// empty arrays on rules
        for (ChespelTree T : RuleDefinitions) {
            def_name = "Rule";
            def_line = T.getLine();
            def_name = T.getChild(0).getText();
            inferEmptyArrayTypeInstr(new TypeInfo(), T.getChild(2));
        }

    }

    private void inferEmptyArrayTypeInstr(TypeInfo return_type, ChespelTree instr) {
        switch (instr.getType()) {
            case ChespelLexer.LIST_INSTR:
                for (int i = 0; i < instr.getChildCount(); ++i) inferEmptyArrayTypeInstr(return_type,instr.getChild(i));
                break;
            case ChespelLexer.ASSIGN:
                inferEmptyArrayTypeExpr(getTypeExpression(instr.getChild(0)), instr.getChild(1));
                break;

            case ChespelLexer.VAR_DECL:
                TypeInfo var_type = getTypeFromDeclaration(instr.getChild(0));
                for (int i = 0; i < instr.getChild(1).getChildCount(); ++i) {
                    ChespelTree var_decl = instr.getChild(1).getChild(i);
                    if (var_decl.getType() == ChespelLexer.ASSIGN) {
                        inferEmptyArrayTypeExpr(var_type, var_decl.getChild(1));
                    }
                }
                break;
            case ChespelLexer.IF:
                inferEmptyArrayTypeExpr(new TypeInfo("BOOL"), instr.getChild(0));
                inferEmptyArrayTypeInstr(return_type, instr.getChild(1));
                if (instr.getChildCount() > 2) inferEmptyArrayTypeInstr(return_type, instr.getChild(2));
                break;
            case ChespelLexer.FORALL:
                TypeInfo forall_expr = getTypeExpression(instr.getChild(0).getChild(1));
                if (forall_expr.hasGenericArray()) {
                    setLineNumber(instr);
                    addErrorContext("Cannot infere array expression's type in forall statement");
                }
                else {
                    inferEmptyArrayTypeExpr(forall_expr, instr.getChild(0).getChild(1));
                }
                inferEmptyArrayTypeInstr(return_type, instr.getChild(1));
                break;

            case ChespelLexer.WHILE:
                inferEmptyArrayTypeExpr(new TypeInfo("BOOL"), instr.getChild(0));
                inferEmptyArrayTypeInstr(return_type, instr.getChild(1));
                break;

            case ChespelLexer.RETURN:
                inferEmptyArrayTypeExpr(return_type,instr.getChild(0));
                break;
            case ChespelLexer.FUNCALL:
                // get matching header, infer for every param the empty array type
                ArrayList<TypeInfo> header = new ArrayList<TypeInfo>();
                ChespelTree params = instr.getChild(1);
                for (int i = 0; i < params.getChildCount(); ++i) {
                    header.add(getTypeExpression(params.getChild(i)));
                }
                if (header.size() != 0) {
                    ArrayList<TypeInfo> real_header;
                    real_header = symbolTable.getFunctionRealHeader(instr.getChild(0).getText(), header);
                    for (int i = 0; i < params.getChildCount(); ++i)
                        inferEmptyArrayTypeExpr(real_header.get(i), params.getChild(i));
                }
                break;
        }
    }

    private void inferEmptyArrayTypeExpr(TypeInfo type, ChespelTree T) {
        inferEmptyArrayTypeTree(type, T); // update tree node
        switch (T.getType()) {
            case ChespelLexer.IN:
                try {
                    TypeInfo in_type = getTypeExpression(T.getChild(0));
                    TypeInfo arr_type = getTypeExpression(T.getChild(1)).getArrayContent();
                    TypeInfo spec_type = in_type.mergeTypes(arr_type);
                    inferEmptyArrayTypeExpr(spec_type, T.getChild(0));
                    inferEmptyArrayTypeExpr(new TypeInfo(spec_type,1), T.getChild(1));
                } catch (Exception e) { throw new RuntimeException(e.getMessage()); }

                break;
            case ChespelLexer.PLUS:
            case ChespelLexer.MINUS:
            case ChespelLexer.NOT:
                if (T.getChildCount() == 1) {
                    inferEmptyArrayTypeExpr(getTypeExpression(T.getChild(0)), T.getChild(0));
                    break;
                }
            case ChespelLexer.AND:
            case ChespelLexer.OR:
            case ChespelLexer.DOUBLE_EQUAL:
            case ChespelLexer.NOT_EQUAL:
            case ChespelLexer.LT:
            case ChespelLexer.LE:
            case ChespelLexer.GT:
            case ChespelLexer.GE:
                TypeInfo t = getTypeExpression(T.getChild(0)).mergeTypes(getTypeExpression(T.getChild(1)));
                inferEmptyArrayTypeExpr(t, T.getChild(0));
                inferEmptyArrayTypeExpr(t, T.getChild(1));
                break;
            case ChespelLexer.CONCAT:
                inferEmptyArrayTypeExpr(type, T.getChild(0));
                inferEmptyArrayTypeExpr(type, T.getChild(1));
                break;
            case ChespelLexer.L_BRACKET:
                inferEmptyArrayTypeExpr(new TypeInfo(type,1), T.getChild(0));
                inferEmptyArrayTypeExpr(new TypeInfo("NUM"), T.getChild(1));
                break;
            case ChespelLexer.LIST_ATOM:
                try {
                    TypeInfo list_type = getTypeExpression(T);
                    for (int i = 0; i < T.getChildCount() ; ++i) {
                        inferEmptyArrayTypeExpr(list_type.getArrayContent(), T.getChild(i));
                    }
                } catch (Exception e) { assert false : e.getMessage(); }
                break;
        }

        ArrayList<TypeInfo> header;
        ArrayList<TypeInfo> real_header;
        switch (T.getType()) {
            case ChespelLexer.DOT:
                header = new ArrayList<TypeInfo>();
                header.add(getTypeExpression(T.getChild(0)));
                real_header = symbolTable.getFunctionRealHeader(T.getChild(1).getText(), header);
                inferEmptyArrayTypeExpr(real_header.get(0), T.getChild(0));
                break;
            case ChespelLexer.FUNCALL:
                header = new ArrayList<TypeInfo>();
                ChespelTree params = T.getChild(1);
                for (int i = 0; i < params.getChildCount(); ++i) {
                    header.add(getTypeExpression(params.getChild(i)));
                }
                if (header.size() != 0) {
                    real_header = symbolTable.getFunctionRealHeader(T.getChild(0).getText(), header);
                    for (int i = 0; i < params.getChildCount(); ++i) {
                        TypeInfo paramType = real_header.get(i);
                        inferEmptyArrayTypeExpr(paramType, params.getChild(i));
                    }
                }
                break;
        }

    }

    private void inferEmptyArrayTypeTree(TypeInfo type, ChespelTree T) {
        TypeInfo tree_type = T.getInfo();
        //System.out.println("Type : " + tree_type.toString());
        if (tree_type.isGeneric()) { T.setTypeInfo(type); return; }
        if (tree_type.isGenericArray()) {
            assert type.isArray() : "Type of tree is GenericArray but it's forced to be " + type.toString();
            //System.out.println("Changing type to " + type.toString());
            T.setTypeInfo(type);
            return;
        }
        int n = 0;
        while (tree_type.isArray()) {
            ++n;
            try {
                tree_type = tree_type.getArrayContent();
                type = type.getArrayContent();
            } catch (Exception e) { throw new RuntimeException(e.getMessage()); }
            if (tree_type.isGenericArray()) {
                assert type.isArray() : "Type of tree is GenericArray but it's forced to be " + type.toString();
                //System.out.println("Changing type to " + ((new TypeInfo(type,n)).toString()));
                T.setTypeInfo(new TypeInfo(type, n));
                break;
            }
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

    private void parseConfigOptions() {
        //def_type = "Config options";
        //def_line = t.getLine();
        //def_name = name;
        for (int i = 0; i < configOptionsTree.getChildCount(); ++i) {
            ChespelTree t = configOptionsTree.getChild(i);
            String name = t.getChild(0).getText();
            setLineNumber(t);
            ChespelTree value = t.getChild(1);
            try {
                configOptions.setConfigOption(name, value);
            } catch (CompileException e) {
                addError(e.getMessage());
            }
        }   
    }


    private void addPredefinedFunctionsToSymbolTable() throws CompileException {
        try {
            BufferedReader br = new BufferedReader(new FileReader("./src/compiler/predefinedFunctionsHeaders.txt"));
            try  {
                String line = br.readLine();

                while (line != null) {
                    String[] words = line.split("[ ()]+");
                    TypeInfo return_type = TypeInfo.parseString(words[0]);
                    String name = words[1];
                    TypeInfo param = TypeInfo.parseString(words[2]);
                    ArrayList<TypeInfo> parameters = new ArrayList<TypeInfo>();
                    parameters.add(param);
                    ArrayList<Boolean> references = new ArrayList<Boolean>();
                    references.add(new Boolean(false));
                    symbolTable.defineFunction(name, return_type, parameters, references);

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
                    checkValidFunCall(t);
                    type_info = symbolTable.getFunctionReturnType(t.getChild(0).getText());
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
                        list_type = list_type.mergeTypes(getTypeExpression(t.getChild(i)));
                    }
                    type_info = new TypeInfo(list_type, 1);
                    break;
                case ChespelLexer.SELF:
                case ChespelLexer.RIVAL:
                    type_info = new TypeInfo("PLAYER");
                    break;
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
                    symbolTable.checkFunctionHeader(t.getChild(1).getText(), args);
                    type_info = symbolTable.getFunctionReturnType(t.getChild(1).getText());
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
                    t.getChild(0).setTypeInfo(varType);
                    break;
                case ChespelLexer.VAR_DECL:
                    //the VAR_DECL node has 2 sons
                    //the first is the type of the variable
                    //the second consists of a list with at least
                    //one child and all childs are ASSIGN or ID
                    TypeInfo declType = getTypeFromDeclaration(t.getChild(0));
                    ChespelTree list_of_decl = t.getChild(1);
                    for (int j = 0; j < list_of_decl.getChildCount(); ++j) {
                        ChespelTree decl_node = list_of_decl.getChild(j);
                        if (decl_node.getType() == ChespelLexer.ASSIGN) {
                            //check that the assigned value is coherent with the type of the variable
                            varName = decl_node.getChild(0).getText();
                            expressionType = getTypeExpression(decl_node.getChild(1));
                            if (!declType.equals(expressionType)) addErrorContext("Assignment type " + expressionType.toString() + " is not of expected type " + declType.toString());
                        }
                        else {
                            varName = decl_node.getText();
                        }
                        //add it to the current visibility scope
                        //this also checks that the variable is not already defined
                        setLineNumber(t);
                        try {
                            symbolTable.defineVariable(varName, declType, linenumber);
                        } catch (CompileException e) {
                            addErrorContext(e.getMessage());
                        }
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
                    setLineNumber(t);
                    try {
                        symbolTable.defineVariable(loopVarName, varType, linenumber);
                    } catch (CompileException e) {
                        addErrorContext(e.getMessage());
                    }
                    checkTypeListInstructions(t.getChild(1));
                    treatUnusedVariables();
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
                    treatUnusedVariables();
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
                    if (!scoring_type.isNum()) addErrorContext("Expected num in score but found " + scoring_type.toString() + " instead");
                    break;
                case ChespelLexer.FUNCALL:
                    checkValidFunCall(t);
                    boolean hasParamByRef = false;
                    String fName = t.getChild(0).getText();
                    ChespelTree declaration = getFunctionNode(fName);
                    if (declaration == null) { //case for predefined functions (which do not appear in the AST)
                        setLineNumber(t);
                        addErrorContext("Invalid instruction '" + fName + "'");
                        break;
                    }
                    for (int j = 0; j < declaration.getChild(1).getChildCount(); j++) {
                        ChespelTree paramDecl = declaration.getChild(1).getChild(j);
                        if (paramDecl.getChild(1).getType() == ChespelLexer.PREF) {
                            hasParamByRef = true;
                            break;
                        }
                    }
                    if (!hasParamByRef) {
                        try {
                            if (!symbolTable.getFunctionReturnType(fName).equals(new TypeInfo("VOID"))) { //there already is a warning for the case of void functions 
                                setLineNumber(t);
                                addWarningContext("Ignoring return value of function");
                            }
                        } catch (CompileException e) {
                            //this can't happen because we have checked that
                            //the call is valid beforehand
                        } 
                    }
                    break; //allow any kind of function calls

                default:
                    assert false : "Invalid instruction type";
            }
        }
    }

    private void checkValidFunCall(ChespelTree t) {
        //1) check that the header exists
        ArrayList<TypeInfo> header = new ArrayList<TypeInfo>();
        ChespelTree params = t.getChild(1);
        for (int i = 0; i < params.getChildCount(); ++i) {
            ChespelTree param = params.getChild(i);
            TypeInfo paramType = getTypeExpression(param);
            header.add(paramType);
        }
        if (header.size() == 0) header.add(new TypeInfo());
        //TypeInfo type_info;
        String fName = t.getChild(0).getText();
        try {
            symbolTable.checkFunctionHeader(fName, header);
        } catch (CompileException e) {
            addErrorContext(e.getMessage());
        }

        //2) check that parameters by reference are identifiers
        ChespelTree declaration = getFunctionNode(fName);
        if (declaration == null) {
            //predefined function case
            return;
        }
        for (int i = 0; i < declaration.getChild(1).getChildCount(); i++) {
            ChespelTree paramDecl = declaration.getChild(1).getChild(i);
            if (paramDecl.getChild(1).getType() == ChespelLexer.PREF) {
                ChespelTree param = params.getChild(i);
                if (param.getType() != ChespelLexer.ID) {
                    setLineNumber(param);
                    addErrorContext("Expression as parameter by reference.");
                }
            }
        }
    }

    /*
    Returns the AST node corresponding to the declaration of the function
    with name 'name'
    */
    private ChespelTree getFunctionNode(String name) {
        for(ChespelTree T : FunctionDefinitions) {
            if (name.equals(T.getChild(1).getText())) return T;
        }
        return null; //this is the case for predefined functions
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
