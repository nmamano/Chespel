package compiler;

import parser.*;

public class ChpOption {
    public String name;
    private int token_type;
    public String c_type;
    public Object value;
    public ChpOption(String n, String t, int t2, Object v) {
        name = n; c_type = t; value = v; token_type = t2;
    }
    public void setValue(ChespelTree t) throws CompileException {
        if (t.getType() != token_type) throw new CompileException("Value of option '"+name+"' is not of the expected type");
        String value_text = t.getText();
        switch (t.getType()) {
            case ChespelLexer.NUM:
                value = new Integer (Integer.parseInt(value_text));
                break;
            case ChespelLexer.BOOL:
                value = new String (value_text.equals("true") ? "TRUE" : "FALSE");
                break;
            default:
                assert false : "Cannot parse token of the option.";
        }
    }
}
