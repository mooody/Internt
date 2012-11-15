/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utils.language;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import play.Logger;

/**
 *
 * @author weetech
 */
public class Language {   
    
    public static String[] languageAvalible()
    {
        return new String[]{"Sv", "En"};
    }
    public String getString(String field) throws IllegalArgumentException, IllegalAccessException
    {
        try
        {
            Class clazz = this.getClass();
            Field f = clazz.getDeclaredField(field.replace(".","_").toLowerCase());
            return (String) f.get(this);
        }
        catch(NoSuchFieldException nex)
        {
            Logger.error("NoSuchFieldExc in Language.getString '%s' does not exists \n\n %s",field,nex);
            return field.replace(".", " ").replace("_"," ");
        }
    }
}
