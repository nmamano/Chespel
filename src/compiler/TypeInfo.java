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
        PLAYER,
        ARRAY,
        GENERIC_ARRAY,
        GENERIC // used for unclear types due to an error
    ;}

    private Type type;
    private TypeInfo content;
    
    /** Constructor for arrays */
    TypeInfo(String s, int levelOfArray) {
        assert !s.equals("VOID") && !s.equals("ARRAY") && !s.equals("GENERIC_ARRAY");
        if (levelOfArray == 0) {
            type = getType(s);
            content = null;
        }
        else {
            type = Type.ARRAY;
            content = new TypeInfo(s, levelOfArray-1);
        }
    }

    public static TypeInfo parseString(String s) {
        int i = 0;
        int levelOfArray = 0;
        while (s.charAt(i) == '[') {
            i++;
            levelOfArray++;
        }
        String ss = s.substring(i, s.length()-i);
        if (ss.equals("Num")) return new TypeInfo("NUMERIC", levelOfArray);
        else if (ss.equals("Bool")) return new TypeInfo("BOOLEAN", levelOfArray);
        else if (ss.equals("Piece")) return new TypeInfo("PIECE", levelOfArray);
        else if (ss.equals("String")) return new TypeInfo("STRING", levelOfArray);
        else if (ss.equals("Cell")) return new TypeInfo("CELL", levelOfArray);
        else if (ss.equals("Row")) return new TypeInfo("ROW", levelOfArray);
        else if (ss.equals("Rank")) return new TypeInfo("RANK", levelOfArray);
        else if (ss.equals("File")) return new TypeInfo("FILE", levelOfArray);
        else if (ss.equals("Player")) return new TypeInfo("PLAYER", levelOfArray);
        else if (ss.equals("GenericArray")) { assert levelOfArray == 0; return new TypeInfo("GENERIC_ARRAY"); }
        assert false: "Could not parse type " + ss;
        return new TypeInfo(); //dummy
    }

    TypeInfo (TypeInfo t, int levelOfArray) {
        assert t.type != Type.VOID && t.type != Type.GENERIC_ARRAY;
        if (levelOfArray == 0) {
            type = t.type;
            if (t.isArray()) content = new TypeInfo (t.content);
        }
        else {
           type = Type.ARRAY;
           content = new TypeInfo(t, levelOfArray-1);
        }
    }

    /** Constructor for void */
    TypeInfo() {type = Type.VOID; content = null; }
    
    /** Constructor for types */
    TypeInfo(String s) { 
        content = null;
        type = getType(s);
        assert type != Type.ARRAY;
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
        if (type == Type.GENERIC || t.type == Type.GENERIC) return true;
        if (type == Type.GENERIC_ARRAY || t.type == Type.GENERIC_ARRAY) return isArray() && t.isArray();
        if (type == Type.ARRAY) return result && content.equals(t.content);
        return result;
    }
    
    /** Indicates whether the data is Boolean */
    public boolean isBoolean() { return type == Type.GENERIC || type == Type.BOOLEAN; }

    /** Indicates whether the data is integer */
    public boolean isNumeric() { return type == Type.GENERIC || type == Type.NUMERIC; }
    
    //not implemented yet: this would allows things such as score p.row or abs(p.row - p.startingRow)
    //public boolean isNumeric() { return isConvertibleToNumeric(); }

    public boolean isConvertibleToNumeric() {
        return type == Type.GENERIC || type == Type.NUMERIC ||
            type == Type.CELL || type == Type.ROW || type == Type.RANK || type == Type.FILE;
    }

    /** Indicates whether the data is void */
    public boolean isVoid() { return type == Type.VOID || type == Type.GENERIC; }
    
    public boolean isArray() { return type == Type.ARRAY || type == Type.GENERIC_ARRAY || type == Type.GENERIC; }

    public boolean isString() { return type == Type.STRING || type == Type.GENERIC; }
    
    public TypeInfo getArrayContent() throws CompileException {
        if (type == Type.GENERIC) return new TypeInfo("GENERIC");
        if (type != Type.ARRAY) throw new CompileException("Cannot get content's type of " + this.toString());
        return content;
    }
    
    /** Returns a string representing the data in textual form. */
    public String toString() {
        if (type == Type.ARRAY) return "[" + content.toString() + "]";
        return type.name();
    }
    
    /*
    Rows, Files, Cells and Ranks can be implicitly converted to numbers, under the condition:
    If the two operands have the same type, the result has that same type.
    If the two operands have diffent type, the operation is not valid, unless one of
    the operands is Num, in which case the result is returned as Num
    */
    public TypeInfo checkTypeArithmetic (TypeInfo d) throws CompileException {
        if (this.isConvertibleToNumeric() && d.isConvertibleToNumeric()) {
            if (this.type == d.type) {
                return new TypeInfo(this);
            }
            else if (this.isNumeric() || d.isNumeric()) {
                return new TypeInfo("NUMERIC");
            }
        }
        throw new CompileException("Cannot perform arithmetic operation between " + this.toString() + " and " + d.toString());
    }

    public TypeInfo checkTypeConcat (TypeInfo d) throws CompileException {
        if (type == Type.GENERIC || d.type == Type.GENERIC) return new TypeInfo("GENERIC");
        if (type == Type.STRING || d.type == Type.STRING) return new TypeInfo("STRING");
        if (!this.equals(d)) throw new CompileException("Cannot concatenate " + this.toString() + " with " + d.toString()); // arrays
        return new TypeInfo(this);
    }
    
    public TypeInfo checkTypeBooleanOp (TypeInfo d) throws CompileException {
        if (!this.isBoolean() || !d.isBoolean()) throw new CompileException("Cannot perform boolean operation between " + this.toString() + " and " + d.toString());
        return new TypeInfo("BOOLEAN");
    }
    
    public TypeInfo checkTypeIn (TypeInfo d) throws CompileException { // f.e. "3 in [1,2,3]"
        if (!this.equals(d.getArrayContent())) throw new CompileException("Cannot check if element of type " + this.toString() + " is in " + d.toString());
        return new TypeInfo("BOOLEAN");
    }

    public TypeInfo checkTypeEquality (TypeInfo d) throws CompileException {
         // Type check
         //(to consider: allow comparison between row and rank)
         if (type == Type.VOID || !this.equals(d)) throw new CompileException("Cannot check equality between " + this.toString() + " and " + d.toString());
         return new TypeInfo("BOOLEAN");
    }

    /*
    Rows, Files, Cells and Ranks can be compared because they can be implicitly converted to numbers
    However, both operands must have the same type, or at least one of them must be a Num
    */    
    public TypeInfo checkTypeOrder (TypeInfo d) throws CompileException {
        if (this.isConvertibleToNumeric() && d.isConvertibleToNumeric()) {
            if (this.type == d.type || this.isNumeric() || d.isNumeric()) {
                return new TypeInfo("BOOLEAN");
            }
        }
        throw new CompileException("Cannot perform arithmetic operation between " + this.toString() + " and " + d.toString());
    }

    public TypeInfo checkTypeUnaryBoolean () throws CompileException {
        if (!this.isBoolean()) throw new CompileException("Cannot perform boolean unary operation over " + this.toString());
        return new TypeInfo("BOOLEAN");
    }

    public TypeInfo checkTypeUnaryArithmetic () throws CompileException {
        if (!this.isNumeric()) throw new CompileException("Cannot perform arithmetic unary operation over " + this.toString());
        return new TypeInfo("NUMERIC");
    }
    
}
