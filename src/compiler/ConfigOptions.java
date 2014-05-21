package compiler;

import parser.*;
import java.util.TreeMap;
import java.util.ArrayList;

public class ConfigOptions {
    // Options
    public TreeMap<String, ChpOption> options;
    public int centipawn_value;
    public boolean default_PStables;

    public ConfigOptions() {
        options = new TreeMap<String,ChpOption> ();
        options.put("centipawn_value", new ChpOption ("centipawn_value", "int", ChespelLexer.NUM, new Integer (20)));
        options.put("default_PStables", new ChpOption("default_PStables", "bool", ChespelLexer.BOOL, new String ("FALSE")));
    }

    public void setConfigOption(String name, ChespelTree value) throws CompileException {
        ChpOption o = options.get(name);
        if (o == null) throw new CompileException("Undefined config option: '" + name + "'");
        else {
            o.setValue(value);
        }
    }

    public ArrayList<ChpOption> getOptions() {
        return new ArrayList(options.values());
    }
}
