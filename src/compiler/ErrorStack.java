package compiler;

import java.util.Comparator;
import java.util.ArrayList;
import java.util.Collections;

/*
Has a stack of errors and a stack of warnings
*/
public class ErrorStack {
    
    private class Error implements Comparable<Error> {
        public int line;
        public String message;
        public Error(int line, String error) { this.line = line; this.message = error; }
        @Override
        public int compareTo(Error e) { return this.line-e.line; }
    }

    private ArrayList<Error> errors;
    private ArrayList<Error> warnings;

    private String infile;

    public ErrorStack(String infile) {
        errors = new ArrayList<Error>();
        warnings = new ArrayList<Error>();
        this.infile = infile;
    }

    public boolean hasErrors() {
        return errors.size() > 0;
    }

    public boolean hasWarnings() {
        return warnings.size() > 0;
    }

    public void addError (int line, String error) {
       String new_error = "Error (" + infile + ", line " + line + "): " + error + ".";
       errors.add(new Error(line, new_error));
    }

    public void addWarning (int line, String warning) {
        String new_warning = "Warning (" + infile + ", line " + line + "): " + warning + ".";
       warnings.add(new Error(line, new_warning));
    }

    public void addError (int line, String error, String place) {
       String new_error = "Error (" + infile + ", line " + line + "): " + error + ".";
       new_error += "\n";
       new_error += " ** in " + place;
       errors.add(new Error(line, new_error));
    }

    public void addWarning (int line, String warning, String place) {
       String new_warning = "Warning (" + infile + ", line " + line + "): " + warning + ".";
       new_warning += "\n";
       new_warning += " ** in " + place;
       warnings.add(new Error(line, new_warning));
    }

    public String getErrors () {
        String s = "";
        Collections.sort(errors);
        for (Error error : errors) {
            s += error.message + "\n";
        }
        s += errors.size() + " errors detected. The program has not been compiled.\n";
        return s;
    }

    public String getWarnings () {
        String s = "";
        Collections.sort(warnings);
        for (Error warning : warnings) {
            s += warning.message + "\n";
        }
        s += warnings.size() + " warnings detected.\n";
        return s;
    }
}
