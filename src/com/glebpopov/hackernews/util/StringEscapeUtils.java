package com.glebpopov.hackernews.util;

import java.io.StringWriter;
import java.io.Writer;

/**
 * Borrowed from Apache2 StringEscapeUtils
 * http://www.java2s.com/Open-Source/Android/Widget/android-delayed/org/apache/commons/lang/StringEscapeUtils.java.htm
 * @author glebp
 *
 */
public class StringEscapeUtils
{
	public static String unescapeHtml(String str) {
        if (str == null) {
            return null;
        }
        try {
            StringWriter writer = new StringWriter(
                    (int) (str.length() * 1.5));
            unescapeHtml(writer, str);
            return writer.toString();
        } catch (Exception e) {
            //assert false;
            //should be impossible
            e.printStackTrace();
            return null;
        }
    }
	
	public static void unescapeHtml(Writer writer, String string)
	    throws Exception {
	if (writer == null) {
	    throw new IllegalArgumentException(
	            "The Writer must not be null.");
	}
	if (string == null) {
	    return;
	}
	Entities.HTML40.unescape(writer, string);
	}
}
