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

/**
 * Class to represent data in the compiler.
 * Each data item has a type and, if constant, a value.
 * The type can be:
 *   Basic: numeric, boolean, string.
 *   Piece: piece (generic), pawn, bishop, rook, knight, king, queen.
 *   BoardType: cell, row/rank, file.
 *   Array: array of any type (including array).
 * Each operation checks that the operands have the appropiate types.
 * All the operations on constants are calculated in-place,
 * i.e., the result is stored in the same data.
 * The type VOID is used to represent void values on function returns.
 */

import parser.*;
import java.util.ArrayList;

public class TypeInfo {
    /** Types of data */
    private enum Type {
        VOID,
        BOOLEAN, 
        NUMERIC,
        STRING,
        PIECE, // PAWN, BISHOP, ROOK, KNIGHT, KING, QUEEN,
        CELL, ROW, RANK, FILE,
        PERSON,
        ARRAY
    ;}

    private Type type;
    private TypeInfo content;
    
    /** Constructor for arrays */
    TypeInfo(String s, int levelOfArray) {
        if (levelOfArray == 0) {
            type = getType(s);
            content = null;
        }
        else {
            type = Type.ARRAY;
            content = new TypeInfo(s, levelOfArray-1);
        }
    }

    /** Constructor for void */
    TypeInfo() {type = Type.VOID; content = null; }
    
    /** Constructor for types */
    TypeInfo(String s) { 
        content = null;
        type = getType(s);
        assert (type != Type.ARRAY && type != Type.VOID);
    }
    
    /** Copy for TypeInfo */
    TypeInfo(TypeInfo t) {
        type = t.type;
        if (type == Type.ARRAY) content = new TypeInfo(t.content);
        else content = null;
    }
    
    private Type getType(String s) {
        return Type.valueOf(s);
    }
    
    public boolean equals(TypeInfo t) {
        boolean result = type == t.type;
        if (type == Type.ARRAY) return result && content.equals(t.content);
        return result;
    }
    
    /** Indicates whether the data is Boolean */
    public boolean isBoolean() { return type == Type.BOOLEAN; }

    /** Indicates whether the data is integer */
    public boolean isNumeric() { return type == Type.NUMERIC; }

    /** Indicates whether the data is void */
    public boolean isVoid() { return type == Type.VOID; }
    
    public boolean isArray() { return type == Type.ARRAY; }
    
    /** Returns the type of data */
//     public Type getType() { return type; }

    public TypeInfo getArrayContent() {
        assert type == Type.ARRAY;
        return content;
    }
    
    /** Returns a string representing the data in textual form. */
    public String toString() {
//         if (type == Type.BOOLEAN) return value == 1 ? "true" : "false";
//         return Integer.toString(value);
        if (type == Type.ARRAY) return "ARRAY_" + content.toString();
        return type.name();
    }
    
    
    // Evaluation of operations
    
    
//     public enum Type {
//       VOID,
//       BOOLEAN, NUMERIC, NUMERIC_DEC, STRING,
//       PIECE,
//       CELL,
//       PERSON,
//       ARRAY
//     ;}
    
    // Operations:
    //  arithmetic - requieres 2 numeric
    //  boolean - requires 2 boolean
    //  relational
    //    equality - 2 anything but void (both same type)
    //    order - 2 numeric
    //  in - element and 1 array (element's type must match array's content's type)
    //  not - requires 1 boolean -- might not be checked here
    //  plus/minus - requires 1 numeric -- might not be checked here
    //  concat - some extra structure must be made so it's not checked here (treated like a function call)
    //  array access - requires 1 array -- not checked here (the same as in)
    //  funccall -- not checked here
    //  
    // Instructions -- not checked here
    //  ifte - requires 1 boolean -- not checked here
    //  forall - requires 1 array and the type of its content -- make a getter
    //  while - requieres 1 boolean -- not checked here
    //  score - requieres 1 numeric -- not checked here
    //  return - must match with function's signature -- not checked here
       
//     private void checkInteger(Data d) {
//          if (d.value < 1000) throw new RuntimeException ("Cannot do modulus with rationals");
//     }

    public TypeInfo checkTypeArithmetic (TypeInfo d) {
        assert type == Type.NUMERIC && d.type == Type.NUMERIC;
        return new TypeInfo("NUMERIC");
    }
    
    public TypeInfo checkTypeBooleanOp (TypeInfo d) {
        assert (type == Type.BOOLEAN && d.type == Type.BOOLEAN);
        return new TypeInfo("BOOLEAN");
    }
    
    public TypeInfo checkTypeIn (TypeInfo d) { // f.e. "3 in [1,2,3]"
        assert type.equals(d.getArrayContent());
        return new TypeInfo("BOOLEAN");
    }

    public TypeInfo checkTypeEquality (TypeInfo d) {
         // Type check
         assert type != Type.VOID && type != Type.ARRAY && (type == d.type);
         return new TypeInfo("BOOLEAN");
    }
    
    public TypeInfo checkTypeOrder (TypeInfo d) {
        assert type == Type.NUMERIC && d.type == Type.NUMERIC;
        return new TypeInfo("BOOLEAN");
    }
    
}
