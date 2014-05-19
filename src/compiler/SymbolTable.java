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

/**
 * Class to represent the span of visibility of variables.
 * The memory is organized as a stack of activation
 * records and each entry in the activation record contains is a pair
 * <name of variable,value>.
 */
 
public class SymbolTable {

    /** Stack of activation records */
    private LinkedList<HashMap<String,TypeInfo>> VariableTables;

    /** Reference to the current activation record */
    private HashMap<String,TypeInfo> CurrentVT = null;

    private HashMap<String,FunctionDefinition> FunctionTable;

    private HashMap<String,TypeInfo> GlobalTable;

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
        private TypeInfo return_type;
        private HashSet<ArrayList<TypeInfo>> headers_types;

        /*
        Constructor for a new function definition from a header. The header is assumed as only header
        for that function.
        */
        public FunctionDefinition (TypeInfo returnType, ArrayList<TypeInfo> headerParameters) throws CompileException {
            return_type = new TypeInfo(returnType); 
            headers_types = new HashSet<ArrayList<TypeInfo>>();
            addHeader(headerParameters);
        }
        /*
        Adds a new header to this function definition. It is necessary to check that the return
        value matches.
        */
        public void addFunctionDef(TypeInfo returnType, ArrayList<TypeInfo> headerParameters) throws CompileException {
            if(! return_type.equals(returnType)) throw new CompileException("Function doesn't return already defined type for its name");
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
                throw new CompileException("Function is not defined for header " +
                    headerToString(header) + " but yes for headers " + headersTypesToString());
            }
            return new TypeInfo(return_type);
        }
        private void addHeader(List<TypeInfo> header) throws CompileException {
            ArrayList<TypeInfo> h = new ArrayList<TypeInfo> (header);
            //assert !headers_types.contains(h); // function already defined
            //System.out.println("Headers: " + headers_types.toString());
            //System.out.println("New header to add: " + header.toString());
            if (isHeaderDefined(header)) throw new CompileException("Function has already been defined for the header " + header.toString());

            ArrayList<TypeInfo> new_header = new ArrayList<TypeInfo> ();
            for (int i = 0 ; i < header.size() ; ++i) new_header.add(new TypeInfo(header.get(i)));
            headers_types.add(new_header);
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

        }
    }

    /** Constructor of the memory */
    public SymbolTable() {
        VariableTables = new LinkedList<HashMap<String,TypeInfo>>();
        FunctionTable = new HashMap<String,FunctionDefinition>();
        GlobalTable = new HashMap<String,TypeInfo>();
        RuleTable = new HashMap<String,RuleDefinition>();
        CurrentVT = null;
    }

    /** Creates a new activation record on the top of the stack */
    public void pushVariableTable() {
        CurrentVT = new HashMap<String,TypeInfo>();
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
    public void defineVariable(String name, TypeInfo var_type) throws CompileException {
        TypeInfo d = CurrentVT.get(name);
        if (d == null) CurrentVT.put(name, var_type); // New definition
        else throw new CompileException("Variable '" + name + "' already defined"); // Error, name already defined
    }

    public void defineFunction(String name, TypeInfo returnValue, ArrayList<TypeInfo> parameters) throws CompileException {
        FunctionDefinition s = FunctionTable.get(name);
        if (s == null) {
            FunctionTable.put(name, new FunctionDefinition(returnValue, parameters));
        }
        else {
            s.addFunctionDef(returnValue, parameters);
        }
    }

    public void defineRule(String name, Set<String> opts) throws CompileException {
        if (RuleTable.get(name) != null) throw new CompileException("Rule '" + name + "' already defined");
        RuleTable.put(name, new RuleDefinition(opts)); 
    }

    public void defineGlobal(String name, TypeInfo type) throws CompileException {
        if (GlobalTable.get(name) != null ) throw new CompileException("Global '" + name + "' already defined");
        GlobalTable.put(name, new TypeInfo (type));
    }

    /** Gets the typeInfo of the variable.
     * @param name The name of the variable
     */
    public TypeInfo getVariableType(String name) throws CompileException {
        TypeInfo v = null;
        for (Iterator<HashMap<String,TypeInfo>> it = VariableTables.descendingIterator(); it.hasNext();) {
            HashMap<String,TypeInfo> table = it.next();
            v = table.get(name);
            if (v != null) return v;
        }
        if (v == null) { // might be a global
            v = GlobalTable.get(name);
            if (v == null) {
                throw new CompileException ("Variable '" + name + "' not defined");
            }
        }
        return v;
    }

    public TypeInfo getFunctionType(String name, List<TypeInfo> header) throws CompileException {
        FunctionDefinition fd = FunctionTable.get(name);
        if (fd == null) {
            throw new CompileException ("Function '" + name + "' not defined");
        }
        return fd.getFunctionType(header);
    }
}
    
