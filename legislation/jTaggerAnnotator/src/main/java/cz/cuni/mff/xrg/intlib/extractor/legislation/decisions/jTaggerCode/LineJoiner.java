package cz.cuni.mff.xrg.intlib.extractor.legislation.decisions.jTaggerCode;

public class LineJoiner {

    public static String joinLines(String input_string) {
        //to ensure that empty lines are really empty (no hidden spaces, tabs, ..)
        String output = input_string.replaceAll("\n\\s+\n", "\n\n");
        
        //to remove \n from lines which do not end with "." (line in the paragraph) and new line char (empty line between paragraphs)
        //If they end with dots, \n is not removed if the new line character is followed by space, tab or next new line character, 
        //because this denotes new paragraph and in this case new line char is ok.  
        output = output.replaceAll("([^.\n])\n([^\\s])", "$1$2");
        
        return output;
        
    }
}
