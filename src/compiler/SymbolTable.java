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


    /*
    Class to represent a function definition. A function name might have
    different headers, but always returns the same type.
    A header is characterized by the type and order of its parameters.
    The different headers are stored in a set of lists of TypeInfo.
    Each list corresponds to a different header, and it
    contains the types of the parameters in order of occurrence. 
    */
    class FunctionDefinition {
        private TypeInfo return_type;
        private HashSet<ArrayList<TypeInfo>> headers_types;

        /*
        Constructor for a new function definition from a header. The header is assumed as only header
        for that function.
        */
        public FunctionDefinition (TypeInfo returnType, ArrayList<TypeInfo> headerParameters) {
            return_type = new TypeInfo(returnType); 
            headers_types = new HashSet<ArrayList<TypeInfo>>();
            addHeader(headerParameters);
        }
        /*
        Adds a new header to this function definition. It is necessary to check that the return
        value matches.
        */
        public void addFunctionDef(TypeInfo returnType, ArrayList<TypeInfo> headerParameters) {
            assert return_type.equals(returnType); // function doesn't return expected type
            addHeader(headerParameters);
        }
        public TypeInfo getFunctionType(List<TypeInfo> header) {
            assert headers_types.contains(header); //function doesn't have the specified header
            return new TypeInfo(return_type);
        }
        private void addHeader(List<TypeInfo> header) {
            ArrayList<TypeInfo> h = new ArrayList<TypeInfo> (header);
            assert !headers_types.contains(h); // function already defined
            //System.out.println("Headers: " + headers_types.toString());
            //System.out.println("New header to add: " + header.toString());
            for (Iterator<ArrayList<TypeInfo>> it = headers_types.iterator() ; it.hasNext() ; ) {
                ArrayList<TypeInfo> declared_header = it.next();
                boolean b = declared_header.size() == header.size();
                int i = 0;
                while (b && i < declared_header.size()) {
                    b = header.get(i).equals(declared_header.get(i));
                    ++i;
                }
                assert !b;
            }
            ArrayList<TypeInfo> new_header = new ArrayList<TypeInfo> ();
            for (int i = 0 ; i < header.size() ; ++i) new_header.add(new TypeInfo(header.get(i)));
            headers_types.add(new_header);
        }
    }
    
    /** Constructor of the memory */
    public SymbolTable() {
        VariableTables = new LinkedList<HashMap<String,TypeInfo>>();
        FunctionTable = new HashMap<String,FunctionDefinition>();
        GlobalTable = new HashMap<String,TypeInfo>();
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
    public void defineVariable(String name, TypeInfo var_type) {
        TypeInfo d = CurrentVT.get(name);
        if (d == null) CurrentVT.put(name, var_type); // New definition
        else assert false; // Error, name already defined
    }

    public void defineFunction(String name, TypeInfo returnValue, ArrayList<TypeInfo> parameters) {
        FunctionDefinition s = FunctionTable.get(name);
        if (s == null) {
            FunctionTable.put(name, new FunctionDefinition(returnValue, parameters));
        }
        else {
            s.addFunctionDef(returnValue, parameters);
        }
    }

    public void defineGlobal(String name, TypeInfo type) {
        TypeInfo t = GlobalTable.get(name);
        if (t == null) GlobalTable.put(name, new TypeInfo (type));
        else assert false; // already defined
    }

    /** Gets the value of the variable. The value is represented as
     * a Data object. In this way, any modification of the object
     * implicitly modifies the value of the variable.
     * @param name The name of the variable
     * @return The value of the variable
     */
    public TypeInfo getVariableType(String name) {
        //Iterator<LinkedList<HashMap<String,TypeInfo>>> it = VariableDefinitions.descendingIterator();
        TypeInfo v = null;
        for (Iterator<HashMap<String,TypeInfo>> it = VariableTables.descendingIterator(); it.hasNext();) {
            HashMap<String,TypeInfo> table = it.next();
            v = table.get(name);
            if (v != null) return v;
        }
        if (v == null) { // might be a global
            v = GlobalTable.get(name);
            if (v == null) {
                throw new RuntimeException ("Variable " + name + " not defined");
            }
        }
        return v;
    }

    public TypeInfo getFunctionType(String name, List<TypeInfo> header) {
        FunctionDefinition fd = FunctionTable.get(name);
        if (fd == null) {
            throw new RuntimeException ("Function " + name + " not defined");
        }
        return fd.getFunctionType(header);
    }
}
    
