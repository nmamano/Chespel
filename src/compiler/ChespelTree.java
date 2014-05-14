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

import org.antlr.runtime.tree.*;
import org.antlr.runtime.Token;
import java.util.ArrayList;

/**
 * Class to extend the nodes of the AST. It includes two fields
 * to store the value of literals and strings.
 * This class is not strictly necessary, since the literals could
 * be extracted from the "text" fields of the tokens.
 * However, it helps to understand how to extend AST nodes in ANTLR.
 */
 
public class ChespelTree extends CommonTree {  
    /** Field to store the type of the node. */
    private TypeInfo type_info;

    /** Constructor of the class */
    public ChespelTree(Token t) {
        super(t);
        type_info = null;
    }

    /** Function to get the child of the node. */
    public ChespelTree getChild(int i) {
        return (ChespelTree) super.getChild(i);
    }

    /** Get info of the class */
    public TypeInfo getInfo() {
        return type_info;
    }
    
// --------------------
// Literals evaluation
// --------------------

    /** Define the numeric value of the node. Numeric values 
     *  are defined as an integer with its value multiplied 
     *  by 1.000 so users may specify any number of decimals, 
     *  but they are rounded to the nearest thousandth.
     */
    public void setNumValue() {
        int numValue = (int) Math.round (Float.parseFloat(getText()) * 1000);
        if (numValue%1000 != 0)
          type_info = new TypeInfo("NUMERIC_DEC");
        else
          type_info = new TypeInfo("NUMERIC");
        // change the text to the new numValue
    }

    /** Define the Boolean value of the node. */
    public void setBooleanType() {
        type_info = new TypeInfo("BOOLEAN");
    }

    /**
     * Define the string value of the node. It removes the
     * enclosing quotes. In this way, it can be printed as it is.
     */
    public void setStringType() {
        type_info = new TypeInfo("STRING");
    }
    
    public void setRangeType() {
        int levelOfArray = 1;
//         types.add("CELL / FILE / ROW");  -- if file or row then increment levelOfArray

        // TODO, it must generate more nodes in the AST with values of the range


//       s = s.toLowerCase();
//       constant = true;
//       content = new ArrayList<Data> ();
//       char s0 = s.charAt(1);
//       char s1 = s.charAt(2);
//       // 3 cases, range of cells, range of rows or range of files
//       if (s0 >= '1' && s0 <= '8') { // range of rows, f.e. $3-5
//         s1 = s.charAt(3);
//         // if (s1 - s0 < 1) error "Second operand of range definition " + s1 + " is greater or equal than first operand " + s0;
//         for (int i = s0 - '0'; i <= s1 - '0' ; ++i) {
//           Data d = new Data();
//           d.type = Type.ROW;
//           d.svalue = String.valueOf(i);
//           content.add(d);
//         }
//       }
//       else if (s1 == '-') { // range of files, f.e. $a-c
//         s1 = s.charAt(3);
//         // if (s1 - s0 < 1) error "Second operand of range definition " + s1 + " is greater or equal than first operand " + s0;
//         for (int i = s0; i <= s1; ++i) {
//           Data d = new Data();
//           d.type = Type.FILE;
//           d.svalue = "" +  ((char) i);
//           content.add(d);
//         }
//       }
//       else { // range of cells, f.e. $a2-b2
//         char s2 = s.charAt(4);
//         char s3 = s.charAt(5);
//         
//         // if (s0 > s2 || s1 > s3) error "Range of cells " + s0 + s1 + "-"+ s2 + s3 +" is not well-defined"
//         
//         // three more cases, cells from the same row, from the same file or from the same diagonal
//         if (s0 == s2) { //same file
//           for (int i = s1 - '0'; i <= s3 - '0'; ++i) {
//             Data d = new Data();
//             d.type = Type.CELL;
//             d.svalue = "" + s0 + String.valueOf(i);
//             content.add(d);
//           }
//         }
//         else if (s1 == s3) { //same row
//           for (int i = s0; i <= s2; ++i) {
//             Data d = new Data();
//             d.type = Type.CELL;
//             d.svalue = "" + ((char) i) + s1;
//             content.add(d);
//           }
//         }
//         else if (s2 - s0 == s3 - s1) { // same diagonal
//           for (int i = 0; i <= s2 - s0; ++i) {
//             Data d = new Data();
//             d.type = Type.CELL;
//             d.svalue = "" + (s0+i) + (s1+i);
//             content.add(d);
//           }
//         }
//         // else {
//         //   error "Range of cells " + s0 + s1 + "-"+ s2 + s3 +" is not well-defined"
//         // }
//       }

        type_info = new TypeInfo("CELL", levelOfArray);
    }
    
//     public void setCellType() {
//         info = new Data();
//         info.setCell(getText());
//     }
//     
//     public void setRowType() {
//         info = new Data();
//         info.setRow(getText());
//     }
//     
//     public void setFileType() {
//         info = new Data();
//         info.setFile(getText());
//     }
//     
//     public void setRankType() {
//         info = new Data();
//         info.setRank(getText());
//     }

}
