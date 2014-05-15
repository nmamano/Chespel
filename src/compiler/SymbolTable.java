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

/**
 * Class to represent the memory of the virtual machine of the
 * interpreter. The memory is organized as a stack of activation
 * records and each entry in the activation record contains is a pair
 * <name of variable,value>.
 */
 
public class SymbolTable {

    /** Stack of activation records */
    private LinkedList<HashMap<String,TypeInfo>> VariableTable;

    /** Reference to the current activation record */
    private HashMap<String,TypeInfo> CurrentVT = null;

    private HashMap<String,FunctionDefinition> FunctionTable;

    private HashMap<String,TypeInfo> GlobalTable;

    ///**
    // * Class to represent an item of the Stack trace.
    // * For each function call, the function name and
    // * the line number of the call are stored.
    // */
    //class StackTraceItem {
    //    public String fname; // Function name
    //    public int line; // Line number
    //    public StackTraceItem (String name, int l) {
    //        fname = name; line = l;
    //    }
    //}
    class FunctionDefinition {
        private TypeInfo return_type;
        private HashSet<ArrayList<TypeInfo>> headers_types;
        public FunctionDefinition (ArrayList<TypeInfo> func_def) {
            return_type = new TypeInfo(func_def.get(0)); 
            headers_types = new HashSet<ArrayList<TypeInfo>>();
            addHeader(func_def.subList(1,func_def.size()));
        }
        public void addFunctionDef(ArrayList<TypeInfo> func_def) {
            if (!return_type.equals(func_def.get(0))) assert false; // function doesn't return expected type
            addHeader(func_def.subList(1,func_def.size()));
        }
        public TypeInfo getFunctionType(List<TypeInfo> header) {
            if (!headers_types.contains(header)) assert false; //function doesn't have the specified header
            return new TypeInfo(return_type);
        }
        private void addHeader(List<TypeInfo> header) {
            if (headers_types.contains(header)) assert false; // function already defined
            ArrayList<TypeInfo> new_header = new ArrayList<TypeInfo> ();
            for (int i = 0 ; i < header.size() ; ++i) new_header.add(new TypeInfo(header.get(i)));
            headers_types.add(new_header);
        }
    }

    ///** Stack trace to keep track of function calls */
    //private LinkedList<StackTraceItem> StackTrace;
    
    /** Constructor of the memory */
    public SymbolTable() {
        VariableTable = new LinkedList<HashMap<String,TypeInfo>>();
        FunctionTable = new HashMap<String,FunctionDefinition>();
        GlobalTable = new HashMap<String,TypeInfo>();
        CurrentVT = null;
        //StackTrace = new LinkedList<StackTraceItem>();
    }

    /** Creates a new activation record on the top of the stack */
    public void pushVariableTable() {
        CurrentVT = new HashMap<String,TypeInfo>();
        VariableTable.addLast (CurrentVT);
        //StackTrace.addLast (new StackTraceItem(name, line));
    }

    /** Destroys the current activation record */
    public void popVariableTable() {
        VariableTable.removeLast();
        if (VariableTable.isEmpty()) CurrentVT = null;
        else CurrentVT = VariableTable.getLast();
        //StackTrace.removeLast();
    }

    /** Defines the value of a variable. If the variable does not
     * exist, it is created. If it exists, the value and type of
     * the variable are re-defined.
     * @param name The name of the variable
     * @param value The value of the variable
     */
    public void defineVariable(String name, TypeInfo var_type) {
        TypeInfo d = CurrentVT.get(name);
        if (d == null) CurrentVT.put(name, var_type); // New definition
        else assert false; // Error, name already defined
    }

    public void defineFunction(String name, ArrayList<TypeInfo> func_type) {
        FunctionDefinition s = FunctionTable.get(name);
        if (s == null) {
            FunctionTable.put(name, new FunctionDefinition(func_type));
        }
        else {
            s.addFunctionDef(func_type);
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
        TypeInfo v = CurrentVT.get(name);
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

//    /**
//     * Generates a string with the contents of the stack trace.
//     * Each line contains a function name and the line number where
//     * the next function is called. Finally, the line number in
//     * the current function is written.
//     * @param current_line program line executed when this function
//     *        is called.
//     * @return A string with the contents of the stack trace.
//     */ 
//    public String getStackTrace(int current_line) {
//        int size = StackTrace.size();
//        ListIterator<StackTraceItem> itr = StackTrace.listIterator(size);
//        StringBuffer trace = new StringBuffer("---------------%n| Stack trace |%n---------------%n");
//        trace.append("** Depth = ").append(size).append("%n");
//        while (itr.hasPrevious()) {
//            StackTraceItem it = itr.previous();
//            trace.append("|> ").append(it.fname).append(": line ").append(current_line).append("%n");
//            current_line = it.line;
//        }
//        return trace.toString();
//    }

//    /**
//     * Generates a string with a summarized contents of the stack trace.
//     * Only the first and last items of the stack trace are returned.
//     * @param current_line program line executed when this function
//     *        is called.
//     * @param nitems number of function calls returned in the string
//     *        at the beginning and at the end of the stack.
//     * @return A string with the contents of the stack trace.
//     */ 
//    public String getStackTrace(int current_line, int nitems) {
//        int size = StackTrace.size();
//        if (2*nitems >= size) return getStackTrace(current_line);
//        ListIterator<StackTraceItem> itr = StackTrace.listIterator(size);
//        StringBuffer trace = new StringBuffer("---------------%n| Stack trace |%n---------------%n");
//        trace.append("** Depth = ").append(size).append("%n");
//        int i;
//        for (i = 0; i < nitems; ++i) {
//           StackTraceItem it = itr.previous();
//           trace.append("|> ").append(it.fname).append(": line ").append(current_line).append("%n");current_line = it.line;
//        }
//        trace.append("|> ...%n");
//        for (; i < size-nitems; ++i) current_line = itr.previous().line;
//        for (; i < size; ++i) {
//           StackTraceItem it = itr.previous();
//           trace.append("|> ").append(it.fname).append(": line ").append(current_line).append("%n");current_line = it.line;
//        }
//        return trace.toString();
//    } 
}
    
