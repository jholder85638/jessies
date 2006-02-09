package e.edit;

import java.io.*;
import java.util.*;
import java.util.regex.*;
import e.util.*;

public class FileIgnorer {
    /** Extensions of files that shouldn't be indexed. */
    private String[] ignoredExtensions;
    
    /** Names of directories that shouldn't be entered when indexing. */
    private Pattern uninterestingDirectoryNames;
    
    public FileIgnorer(String rootDirectoryPath) {
        File rootDirectory = FileUtilities.fileFromString(rootDirectoryPath);
        ignoredExtensions = FileUtilities.getArrayOfPathElements(Parameters.getParameter("files.uninterestingExtensions", ""));
        uninterestingDirectoryNames = Pattern.compile(getUninterestingDirectoryPattern(rootDirectory));
    }
    
    public boolean isIgnored(File file) {
        if (file.isHidden() || file.getName().startsWith(".") || file.getName().endsWith("~")) {
            return true;
        }
        if (file.isDirectory()) {
            return isIgnoredDirectory(file);
        }
        return FileUtilities.nameEndsWithOneOf(file, ignoredExtensions);
    }
    
    private static String getUninterestingDirectoryPattern(File rootDirectory) {
        ArrayList<String> patterns = new ArrayList<String>();
        
        // Start with the default ignored directory patterns.
        // autotools directories:
        patterns.add("\\.deps");
        patterns.add("autom4te.cache");
        // SCM directories:
        patterns.add("\\.svn");
        patterns.add("BitKeeper");
        patterns.add("CVS");
        patterns.add("SCCS");
        
        // Try to run the site-local script.
        // FIXME: check that we handle the case where the script doesn't exist.
        // FIXME: move ShellCommand.makeCommandLine into ProcessUtilities when we have a better name.
        final String scriptName = "echo-local-non-source-directory-pattern";
        String[] command = ShellCommand.makeCommandLine(scriptName);
        ArrayList<String> errors = new ArrayList<String>();
        ProcessUtilities.backQuote(rootDirectory, command, patterns, errors);
        
        // Make a regular expression.
        return StringUtilities.join(patterns, "|");
    }
    
    public boolean isIgnoredDirectory(File directory) {
        return uninterestingDirectoryNames.matcher(directory.getName()).matches();
    }
}
