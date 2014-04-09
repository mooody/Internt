/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

/**
 *
 * @author weetech
 */
public class UsefullStuff {
    public static String extractDigits(String src) {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < src.length(); i++) {
        char c = src.charAt(i);
        if (Character.isDigit(c)) {
            builder.append(c);
        }
    }
    return builder.toString();
}
}
