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

public class Data {
    /** Types of data */
    public enum Type {
      VOID,
      BOOLEAN, NUMERIC, STRING,
      PIECE, PAWN, BISHOP, ROOK, KNIGHT, KING, QUEEN,
      CELL, ROW, RANK, FILE,
      PERSON,
      ARRAY
    ;}

    /** Type of data*/
    private Type type;

    /** Value of the data */
    private int value; 
    private String svalue;
    
    private ArrayList<Data> content;
    
    
    /** Indicates whether the data has contents or just the type */
    private boolean constant;

    /** Constructor for numeric */
    Data(int v) { type = Type.NUMERIC; value = v; constant = true; }

    /** Constructor for Booleans */
    Data(boolean b) { type = Type.BOOLEAN; value = b ? 1 : 0; constant = true; }
    
    /** Constructor for Strings */
    Data(String s) { type = Type.STRING; svalue = s; constant = true; }
    
    /** Constructor for arrays */
    Data(ArrayList<Data> d) {
      assert d.size() > 0;
      type = Type.ARRAY;
      content = new ArrayList<Data>();
      for (int i = 0; i < d.size(); ++i) {
        content.add( d.get(i).copyData() );
      }
      constant = d.get(0).isConstant();
    }

    /** Constructor for void data */
    Data() {type = Type.VOID; constant = false; }
    
    /** Constructor for valueless types */
    Data(Type t) { type = t; constant = false; }
    
    /** Copy for data */
    public Data copyData() {
      Data newData = new Data();
      newData.type = this.getType();
      newData.constant = this.isConstant();
      
      if (newData.constant || newData.type == Type.ARRAY) // copy contents only if relevant
        switch (this.getType()) {
          case BOOLEAN:
          case NUMERIC:
          case PERSON:
            newData.value = this.value;
            break;
          case PIECE: // piece types
          case STRING:
          case PAWN:
          case BISHOP:
          case ROOK:
          case KNIGHT:
          case QUEEN:
          case ROW: // board types
          case RANK:
          case FILE:
          case CELL:
            newData.svalue = this.svalue;
            break;
          case ARRAY:
            newData = new Data(this.content);
            break;
          default:
            break;
        }
      return newData;
    }
    
    // Setters
    
    /** 
     *  Transforms the range to the appropiate array type
     *  and expands its value after checking it's a valid
     *  range.
     */
    public void setRange(String s) {
      s = s.toLowerCase();
      constant = true;
      content = new ArrayList<Data> ();
      char s0 = s.charAt(1);
      char s1 = s.charAt(2);
      // 3 cases, range of cells, range of rows or range of files
      if (s0 >= '1' && s0 <= '8') { // range of rows, f.e. $3-5
        s1 = s.charAt(3);
        // if (s1 - s0 < 1) error "Second operand of range definition " + s1 + " is greater or equal than first operand " + s0;
        for (int i = s0 - '0'; i <= s1 - '0' ; ++i) {
          Data d = new Data();
          d.type = Type.ROW;
          d.svalue = String.valueOf(i);
          content.add(d);
        }
      }
      else if (s1 == '-') { // range of files, f.e. $a-c
        s1 = s.charAt(3);
        // if (s1 - s0 < 1) error "Second operand of range definition " + s1 + " is greater or equal than first operand " + s0;
        for (int i = s0; i <= s1; ++i) {
          Data d = new Data();
          d.type = Type.FILE;
          d.svalue = "" +  ((char) i);
          content.add(d);
        }
      }
      else { // range of cells, f.e. $a2-b2
        char s2 = s.charAt(4);
        char s3 = s.charAt(5);
        
        // if (s0 > s2 || s1 > s3) error "Range of cells " + s0 + s1 + "-"+ s2 + s3 +" is not well-defined"
        
        // three more cases, cells from the same row, from the same file or from the same diagonal
        if (s0 == s2) { //same file
          for (int i = s1 - '0'; i <= s3 - '0'; ++i) {
            Data d = new Data();
            d.type = Type.CELL;
            d.svalue = "" + s0 + String.valueOf(i);
            content.add(d);
          }
        }
        else if (s1 == s3) { //same row
          for (int i = s0; i <= s2; ++i) {
            Data d = new Data();
            d.type = Type.CELL;
            d.svalue = "" + ((char) i) + s1;
            content.add(d);
          }
        }
        else if (s2 - s0 == s3 - s1) { // same diagonal
          for (int i = 0; i <= s2 - s0; ++i) {
            Data d = new Data();
            d.type = Type.CELL;
            d.svalue = "" + (s0+i) + (s1+i);
            content.add(d);
          }
        }
        // else {
        //   error "Range of cells " + s0 + s1 + "-"+ s2 + s3 +" is not well-defined"
        // }
      }
    }
    
    /** Defines a Boolean value for the data */
    public void setValue(boolean b) { type = Type.BOOLEAN; value = b ? 1 : 0; }

    /** Defines an integer value for the data */
    public void setValue(int v) { type = Type.NUMERIC; value = v; }

//     /** Copies the value from another data */
    public void setData(Data d) { // used in variable reassignation
      assert type == d.type;
      Data tmp = d.copyData();
      constant = tmp.constant;
      value = tmp.value;
      svalue = tmp.svalue;
      content = tmp.content;
    }
    
    
    // Getters
    
    public boolean isConstant() {
      return constant;
    }
    
    /** Indicates whether the data is Boolean */
    public boolean isBoolean() { return type == Type.BOOLEAN; }

    /** Indicates whether the data is integer */
    public boolean isInteger() { return type == Type.NUMERIC; }

    /** Indicates whether the data is void */
    public boolean isVoid() { return type == Type.VOID; }

    /** Returns the type of data */
    public Type getType() { return type; }
    
//     /**
//      * Gets the value of an integer data. The method asserts that
//      * the data is an integer.
//      */
//     public int getIntegerValue() {
//         assert type == Type.NUMERIC;
//         return value;
//     }
// 
//     /**
//      * Gets the value of a Boolean data. The method asserts that
//      * the data is a Boolean.
//      */
//     public boolean getBooleanValue() {
//         assert type == Type.BOOLEAN;
//         return value == 1;
//     }
    
    /** Returns a string representing the data in textual form. */
    public String toString() {
//         if (type == Type.BOOLEAN) return value == 1 ? "true" : "false";
//         return Integer.toString(value);
      return "not implemented yet";
    }
    
    
    // Evaluation of operations
    
    
//     public enum Type {
//       VOID,
//       BOOLEAN, NUMERIC, STRING,
//       PIECE, PAWN, BISHOP, ROOK, KNIGHT, KING, QUEEN,
//       CELL, ROW, RANK, FILE,
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
    //  plus/minus - requires 1 numeric
    //  concat - some extra structure must be made so it's not checked here (treated like a function call)
    //  array access - requires 1 array
    //  funccall - not checked here
    //  
    // Instructions -- not checked here
    //  ifte - requires 1 boolean -- not checked here
    //  forall - requires 1 array and the type of its content -- make a getter
    //  while - requieres 1 boolean -- not checked here
    //  score - requieres 1 numeric -- not checked here
    //  return - must match with function's signature -- not checked here
    
    /**
     * Checks for zero (for division). It raises an exception in case
     * the value is zero.
     */
    private void checkDivZero(Data d) {
//         if (d.value == 0) throw new RuntimeException ("Division by zero");
    }
    
//     private void checkInteger(Data d) {
// //         if (d.value < 1000) throw new RuntimeException ("Cannot do modulus with rationals");
//     }

    /**
     * Evaluation of arithmetic expressions. The evaluation is done
     * in a new Data, which will have a value if the operands are constants.
     * @param op Type of operator (token).
     * @param d Second operand.
     */
     
    public Data evaluateArithmetic (int op, Data d) {
        // if (not (type == Type.NUMERIC && d.type == Type.NUMERIC)) throw TypeException;
        Data result;
        if (isConstant() && d.isConstant()) {
          switch (op) {
              case ChespelLexer.PLUS: result = new Data(value + d.value); break;
              case ChespelLexer.MINUS: result = new Data(value - d.value); break;
              case ChespelLexer.MUL: result = new Data(value * d.value); break;
              case ChespelLexer.DIV: checkDivZero(d); result = new Data(value / d.value); break;
//               case ChespelLexer.MOD: checkDivZero(d); checkInteger(this); checkInteger(d); result = new Data(value % d.value); break;
              default: result = new Data();
          }
        }
        else {
          result = new Data(Type.NUMERIC);
        }
        assert result.getType() != Type.VOID;
        return result;
    }

    /**
     * Evaluation of expressions with relational operators.
     * @param op Type of operator (token).
     * @param d Second operand.
     * @return A Boolean data with the value of the expression.
     */
    public Data evaluateRelational (int op, Data d) {
//         // Type check
//         if (op == ChespelLexer.EQUAL || op == ChespelLexer.NOT_EQUAL) { assert type != Type.VOID && type == d.type; }
//         else { assert type == Type.NUMERIC && d.type == Type.NUMERIC };
//         switch (op) {
//             case ChespelLexer.EQUAL: return new Data(this.equals(d));
//             case ChespelLexer.NOT_EQUAL: return new Data(not this.equals(d));
//             // only defined for numeric data:
//             case ChespelLexer.LT: return new Data(value < d.value);
//             case ChespelLexer.LE: return new Data(value <= d.value);
//             case ChespelLexer.GT: return new Data(value > d.value);
//             case ChespelLexer.GE: return new Data(value >= d.value);
//             default: assert false; 
//         }
         return null;
    }
}
