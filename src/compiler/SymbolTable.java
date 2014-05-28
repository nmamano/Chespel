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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.Iterator;
import java.util.Collections;

/**
 * Class to represent the span of visibility of variables.
 * The memory is organized as a stack of activation
 * records and each entry in the activation record contains is a pair
 * <name of variable,value>.
 */
 
public class SymbolTable {

    /** Stack of activation records */
    private LinkedList<HashMap<String,VariableDefinition>> VariableTables;

    /** Reference to the current activation record */
    private HashMap<String,VariableDefinition> CurrentVT = null;

    private HashMap<String,FunctionDefinition> FunctionTable;

    private HashMap<String,VariableDefinition> GlobalTable;

    private HashMap<String,RuleDefinition> RuleTable;

    /*
    Class to represent a function definition. A function name might have
    different headers, but always returns the same type.
    A header is characterized by the type and order of its parameters.
    The different headers are stored in a set of lists of TypeInfo.
    Each list corresponds to a different header, and it
    contains the types of the parameters in order of occurrence. 
    */

    private static String headerToString(List<TypeInfo> header) {
        String res = "(";
        boolean first = true;
        for (TypeInfo elem : header) {
            if (first) first = !first;
            else res += ", ";
            res += elem.toString();
        }
        return res + ")";
    }

    class FunctionDefinition {
        private String func_name;
        private TypeInfo return_type;
        private HashSet<ArrayList<TypeInfo>> headers_types;

        /*
        Constructor for a new function definition from a header. The header is assumed as only header
        for that function.
        */
        public FunctionDefinition (String func_name, TypeInfo returnType, ArrayList<TypeInfo> headerParameters) throws CompileException {
            this.func_name = func_name;
            return_type = new TypeInfo(returnType); 
            headers_types = new HashSet<ArrayList<TypeInfo>>();
            addHeader(headerParameters);
        }
        /*
        Adds a new header to this function definition. It is necessary to check that the return
        value matches.
        */
        public void addFunctionDef(TypeInfo returnType, ArrayList<TypeInfo> headerParameters) throws CompileException {
            if(! return_type.equals(returnType)) throw new CompileException("Function '"+ func_name  +"' doesn't return "+returnType.toString()+" which is the type defined for a previous declaration.");
            addHeader(headerParameters);
        }

        private String headersTypesToString() {
            String res = "{";
            boolean first = true;
            for (ArrayList<TypeInfo> elem : headers_types) {
                if (first) first = !first;
                else res += ", ";
                res += headerToString(elem);
            }
            return res + "}";
        }

        public TypeInfo getFunctionType(List<TypeInfo> header) throws CompileException {
            if (!isHeaderDefined(header)) {
                throw new CompileException("Function '"+func_name+"' is not defined for header " +
                    headerToString(header) + " but yes for headers " + headersTypesToString());
            }
            return new TypeInfo(return_type);
        }

        public ArrayList<TypeInfo> getFunctionRealHeader(List<TypeInfo> header) {
            ArrayList<TypeInfo> defined_header = getHeaderDefined(header);
            if (defined_header == null) throw new RuntimeException("Function '"+func_name+"' passed semantic check but now is uncallable.");
            return defined_header;
        }

        private void addHeader(List<TypeInfo> header) throws CompileException {
            ArrayList<TypeInfo> h = new ArrayList<TypeInfo> (header);
            //assert !headers_types.contains(h); // function already defined
            //System.out.println("Headers: " + headers_types.toString());
            //System.out.println("New header to add: " + header.toString());
            if (isHeaderDefined(header)) throw new CompileException("Function '"+func_name+"' has already been defined for the header " + header.toString());

            ArrayList<TypeInfo> new_header = new ArrayList<TypeInfo> ();
            for (int i = 0 ; i < header.size() ; ++i) new_header.add(new TypeInfo(header.get(i)));
            headers_types.add(new_header);
        }

        private ArrayList<TypeInfo> getHeaderDefined(List<TypeInfo> header) {
            for (Iterator<ArrayList<TypeInfo>> it = headers_types.iterator() ; it.hasNext() ; ) {
                ArrayList<TypeInfo> declared_header = it.next();
                boolean are_equal = declared_header.size() == header.size();
                int i = 0;
                while (are_equal && i < declared_header.size()) {
                    are_equal = header.get(i).equals(declared_header.get(i));
                    ++i;
                }
                if (are_equal) return declared_header;
            }
            return null;
        }
        private boolean isHeaderDefined(List<TypeInfo> header) {
            for (Iterator<ArrayList<TypeInfo>> it = headers_types.iterator() ; it.hasNext() ; ) {
                ArrayList<TypeInfo> declared_header = it.next();
                boolean are_equal = declared_header.size() == header.size();
                int i = 0;
                while (are_equal && i < declared_header.size()) {
                    are_equal = header.get(i).equals(declared_header.get(i));
                    ++i;
                }
                if (are_equal) return true;
            }
            return false;
        }

    }

    /*
    Class to represent a rule definition. Rules can't have repeated names,
    even if the rule options are different.
    Options are stored in a set of strings.
    */
    class RuleDefinition {
        private HashSet<String> options;

        /*
        Constructor for a new rule definition.
        */
        public RuleDefinition (Set<String> opts) {
            options = new HashSet<String>(opts);
            if (!opts.contains("opening") && !opts.contains("midgame") && !opts.contains("endgame")) {
                options.add("opening"); options.add("midgame"); options.add("endgame");
            }
        }

        public HashSet<String> getOptions() {
            return options;
        }
    }

    class VariableDefinition implements Comparable<VariableDefinition> {
        public boolean used;
        public String name;
        public TypeInfo type;
        public int line;
        public VariableDefinition(String name, TypeInfo type, int line) { this.name = name; this.type = type; used = false; this.line = line; }
        @Override
        public int compareTo(VariableDefinition v) {
            if (this.used && v.used || !this.used && !v.used) return name.compareTo(v.name);
            else if (!this.used) return -1;
            else return 1;
        }
    }

    /** Constructor of the memory */
    public SymbolTable() {
        VariableTables = new LinkedList<HashMap<String,VariableDefinition>>();
        FunctionTable = new HashMap<String,FunctionDefinition>();
        GlobalTable = new HashMap<String,VariableDefinition>();
        RuleTable = new HashMap<String,RuleDefinition>();
        CurrentVT = null;
    }

    /** Creates a new activation record on the top of the stack */
    public void pushVariableTable() {
        CurrentVT = new HashMap<String,VariableDefinition>();
        VariableTables.addLast (CurrentVT);
    }

    /** Destroys the current activation record */
    public void popVariableTable() {
        VariableTables.removeLast();
        if (VariableTables.isEmpty()) CurrentVT = null;
        else CurrentVT = VariableTables.getLast();
        //StackTrace.removeLast();
    }

    /** Defines the value of a variable. If the variable does not
     * exist, it is created. If it already exists, there is an error.
     * @param name The name of the variable
     * @param value The value of the variable
     */
    public void defineVariable(String name, TypeInfo var_type, int line) throws CompileException {
        if (CurrentVT.get(name) == null) CurrentVT.put(name, new VariableDefinition (name, new TypeInfo (var_type),line)); // New definition
        else throw new CompileException("Variable '" + name + "' already defined"); // Error, name already defined
    }

    public void defineFunction(String name, TypeInfo returnValue, ArrayList<TypeInfo> parameters) throws CompileException {
        FunctionDefinition s = FunctionTable.get(name);
        if (s == null) {
            FunctionTable.put(name, new FunctionDefinition(name, returnValue, parameters));
        }
        else {
            s.addFunctionDef(returnValue, parameters);
        }
    }

    public void defineRule(String name, Set<String> opts) throws CompileException {
        if (RuleTable.get(name) != null) throw new CompileException("Rule '" + name + "' already defined");
        RuleTable.put(name, new RuleDefinition(opts)); 
    }

    public void defineGlobal(String name, TypeInfo type, int line) throws CompileException {
        if (GlobalTable.get(name) != null ) throw new CompileException("Global '" + name + "' already defined");
        GlobalTable.put(name, new VariableDefinition (name, new TypeInfo (type),line));
    }

    /** Gets the typeInfo of the variable.
     * @param name The name of the variable
     */
    public TypeInfo getVariableType(String name) throws CompileException {
        VariableDefinition v = null;
        for (Iterator<HashMap<String,VariableDefinition>> it = VariableTables.descendingIterator(); it.hasNext();) {
            HashMap<String,VariableDefinition> table = it.next();
            v = table.get(name);
            if (v != null) {
                v.used = true;
                return v.type;
            }
        }
        if (v == null) { // might be a global
            v = GlobalTable.get(name);
            if (v == null) {
                throw new CompileException ("Variable '" + name + "' not defined");
            }
        }
        v.used = true;
        return v.type;
    }

    public TypeInfo getFunctionType(String name, List<TypeInfo> header) throws CompileException {
        FunctionDefinition fd = FunctionTable.get(name);
        if (fd == null) {
            throw new CompileException ("Function '" + name + "' not defined");
        }
        return fd.getFunctionType(header);
    }

    public HashSet<String> getRuleOptions(String name) {
        return RuleTable.get(name).getOptions();
    }

    public ArrayList<String> getUnusedVariables() {
        ArrayList<VariableDefinition> v_def = new ArrayList<VariableDefinition>(CurrentVT.values());
        Collections.sort(v_def);
        int i = 0;
        while (i < v_def.size() && !v_def.get(i).used) ++i;
        ArrayList<String> result = new ArrayList<String>();
        for (VariableDefinition v : v_def.subList(0,i)) result.add(v.line + "-" + v.name);
        return result;
    }

    public ArrayList<String> getUnusedGlobals() {
        ArrayList<VariableDefinition> g_def = new ArrayList<VariableDefinition>(GlobalTable.values());
        Collections.sort(g_def);
        int i = 0;
        while (i < g_def.size() && !g_def.get(i).used) ++i;
        ArrayList<String> result = new ArrayList<String>();
        for (VariableDefinition g : g_def.subList(0,i)) result.add(g.line + "-" + g.name);
        return result;
    }

    public ArrayList<TypeInfo> getFunctionRealHeader(String name, List<TypeInfo> header) {
        FunctionDefinition fd = FunctionTable.get(name);
        if (fd == null) {
            throw new RuntimeException ("Function '" + name + "' has passed the semantic check but now cannot be found.");
        }
        return fd.getFunctionRealHeader(header);
    }
}
    
