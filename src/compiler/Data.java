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
 *   BoardType: cell, row/rank (only one of the two, the other is an alias), file.
 *   Array: array of any type (including array).
 * Each operation checks that the operands have the appropiate types.
 * All the operations on constants are calculated in-place,
 * i.e., the result is stored in the same data.
 * The type VOID is used to represent void values on function returns.
 */

import parser.*;

public class Data {
    /** Types of data */
    public enum Type {
      VOID,
      BOOLEAN, NUMERIC, STRING,
      PIECE, PAWN, BISHOP, ROOK, KNIGHT, KING, QUEEN,
      CELL, ROW, FILE,
      ARRAY
    ;}

    /** Type of data*/
    private Type type;

    /** Value of the data */
    private int value; 
    private String svalue;
    private Data content;
    
    private boolean constant;

    /** Constructor for numeric */
    Data(int v) { type = Type.NUMERIC; value = v; constant = true; }

    /** Constructor for Booleans */
    Data(boolean b) { type = Type.BOOLEAN; value = b ? 1 : 0; constant = true; }
    
    Data(String s) { type = Type.STRING; svalue = s; constant = true; }
    
    /** Constructor for arrays */
    Data(Data d) { type = Type.ARRAY; content = d; constant = d.isConstant(); }

    /** Constructor for void data */
    Data() {type = Type.VOID; }

    public Data copyData() {
      Data newData = new Data();
      newData.type = this.getType();
      newData.constant = this.isConstant();
      if (newData.constant || newData.type == Type.ARRAY) // copy contents only if relevant
        switch (this.getType()) {
          case BOOLEAN:
          case NUMERIC:
            newData.value = this.value;
            break;
          case STRING:
            newData.svalue = this.svalue;
            break;
          case ARRAY:
            newData.content = this.content.copyData();
            break;
          default:
            break;
        }
      return newData;
    }
    
    public boolean isConstant() {
      return true;
    }

    /** Returns the type of data */
    public Type getType() { return type; }

    /** Indicates whether the data is Boolean */
    public boolean isBoolean() { return type == Type.BOOLEAN; }

    /** Indicates whether the data is integer */
    public boolean isInteger() { return type == Type.NUMERIC; }

    /** Indicates whether the data is void */
    public boolean isVoid() { return type == Type.VOID; }

    /**
     * Gets the value of an integer data. The method asserts that
     * the data is an integer.
     */
    public int getIntegerValue() {
        assert type == Type.NUMERIC;
        return value;
    }

    /**
     * Gets the value of a Boolean data. The method asserts that
     * the data is a Boolean.
     */
    public boolean getBooleanValue() {
        assert type == Type.BOOLEAN;
        return value == 1;
    }

    /** Defines a Boolean value for the data */
    public void setValue(boolean b) { type = Type.BOOLEAN; value = b ? 1 : 0; }

    /** Defines an integer value for the data */
    public void setValue(int v) { type = Type.NUMERIC; value = v; }

    /** Copies the value from another data */
    public void setData(Data d) { type = d.type; value = d.value; }
    
    /** Returns a string representing the data in textual form. */
    public String toString() {
        if (type == Type.BOOLEAN) return value == 1 ? "true" : "false";
        return Integer.toString(value);
    }
    
    /**
     * Checks for zero (for division). It raises an exception in case
     * the value is zero.
     */
    private void checkDivZero(Data d) {
        if (d.value == 0) throw new RuntimeException ("Division by zero");
    }

    /**
     * Evaluation of arithmetic expressions. The evaluation is done
     * "in place", returning the result on the same data.
     * @param op Type of operator (token).
     * @param d Second operand.
     */
     
    public void evaluateArithmetic (int op, Data d) {
        // assert type == Type.NUMERIC && d.type == Type.NUMERIC;
        // switch (op) {
        //     case ChespelLexer.PLUS: value += d.value; break;
        //     case ChespelLexer.MINUS: value -= d.value; break;
        //     case ChespelLexer.MUL: value *= d.value; break;
        //     case ChespelLexer.DIV: checkDivZero(d); value /= d.value; break;
        //     case ChespelLexer.MOD: checkDivZero(d); value %= d.value; break;
        //     default: assert false;
        // }
    }

    /**
     * Evaluation of expressions with relational operators.
     * @param op Type of operator (token).
     * @param d Second operand.
     * @return A Boolean data with the value of the expression.
     */
    public Data evaluateRelational (int op, Data d) {
        assert type != Type.VOID && type == d.type;
        switch (op) {
            case ChespelLexer.EQUAL: return new Data(value == d.value);
            case ChespelLexer.NOT_EQUAL: return new Data(value != d.value);
            case ChespelLexer.LT: return new Data(value < d.value);
            case ChespelLexer.LE: return new Data(value <= d.value);
            case ChespelLexer.GT: return new Data(value > d.value);
            case ChespelLexer.GE: return new Data(value >= d.value);
            default: assert false; 
        }
        return null;
    }
}