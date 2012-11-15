/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import play.Logger;

/**
 *
 * @author weetech
 */
public class PlanClass {
    public static void printError(Exception ex)
    {
        StackTraceElement[] el = ex.getStackTrace();
        
        for(StackTraceElement ste: el)
        {
            Logger.info("%s", ste.toString());
        }
        Logger.info("--- %s ", ex.getMessage());
        Logger.info("--- %s ", ex.getClass().getName());
    }
}
