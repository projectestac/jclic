package org.jdom.output;

public class SpecialXMLOutputter extends XMLOutputter {
    
    public SpecialXMLOutputter(String indent, boolean newlines, String encoding){
        //super(indent, newlines, encoding);
        super();
        Format format=Format.getPrettyFormat();
        if(indent!=null)
          format.setIndent(indent);
        if(encoding!=null)
          format.setEncoding(encoding);
        setFormat(format);
    }

    
}
