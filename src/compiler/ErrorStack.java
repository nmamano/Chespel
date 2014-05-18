package compiler;

import java.util.Comparator;
import java.util.ArrayList;
import java.util.Collections;

public class ErrorStack {
    
    private class Error implements Comparable<Error> {
        public int line;
        public String message;
        public Error(int line, String error) { this.line = line; this.message = error; }
        @Override
        public int compareTo(Error e) { return this.line-e.line; }
    }

    private ArrayList<Error> errors;

    private String infile;

    public ErrorStack(String infile) {
        errors = new ArrayList<Error>();
        this.infile = infile;
    }

    public boolean hasErrors() {
        return errors.size() > 0;
    }

    public void addError (int line, String error) {
       String new_error = "Error (" + infile + ", line " + line + "): " + error + ".";
       errors.add(new Error(line, new_error));
    }

    public void addError (int line, String error, String place) {
       String new_error = "Error (" + infile + ", line " + line + "): " + error + ".";
       new_error += "\n";
       new_error += " ** in " + place;
       errors.add(new Error(line, new_error));
    }

    public String getErrors () {
        String s = "";
        Collections.sort(errors);
        for (Error error : errors) {
            s += error.message + "\n";
        }
        s += "In total, " + errors.size() + " errors found\n";
        return s;
    }
}
