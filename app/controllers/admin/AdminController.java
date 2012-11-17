/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers.admin;

import controllers.Application;
import controllers.PlanController;
import controllers.admin.CompanyController;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import models.Admin;
import models.Core.Module;
import models.Grupp;
import models.User;
import models.UserBase;
import play.Logger;
import play.i18n.Messages;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Router;

/**
 *
 * @author weetech
 */
public class AdminController extends PlanController {
    
	/**
	* Kontrollerar så användaren är admin.
	* Samt om användaren ej har ett företag, gå in och fixa ett företag
	*/
    @Before
    public static void authority() throws NoAdminException
    {
       try{
            //if(PlanController.user.getClass() == Admin.class)
           if(PlanController.user instanceof Admin)
            {
				Logger.info("AdminController.authority: ADMIN");
                //Om användaren ännu inte skapat ett företag. Gå och hantera detta!
				if(user.company == null && !(
                (Controller.request.action.equals("admin.CompanyController.index")) 
                ||(Controller.request.action.equals("admin.CompanyController.create"))
                ))
                {
                    CompanyController.index();
                }

            }
            else{
                flash.put("message", "You.need.to.login.as.administrator");
                Application.loginform();
            }
            
       } catch(Exception ex)       {
           Application.loginform();
           
       }
    }
    
    
    /**
     * Functions witch handles the adminfunctions of modules
     */
    
//<editor-fold defaultstate="collapsed" desc=" Modules" >
    public static void modules()
    {
        render("admin/modules/modules.html");
    }
    
    public static void addUserToModule(UserBase user, Module module)
    {
        
        if(user == null || module == null) Controller.forbidden("Not an allowed request!");
       
       
            if(module.addUser(user))
            {
                flash.put("message", Messages.get("module %s added to user %s", module.name, user.name));
            }
            else{
                flash.put("message", Messages.get("user %s already have access to %s module", user.name,module.name));
            }
        AdminController.modules();
    }
     
    
     
     //</editor-fold> 

     
    public static class NoAdminException extends Exception
     {
         public NoAdminException()
         {
             super("User with no admin-rights try to access AdminController");
         }
     }
}
