/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import controllers.admin.AdminController;
import java.util.List;
import models.Admin;
import models.Core.Module;
import models.Grupp;
import models.SuperAdmin;
import models.UserBase;
import play.Logger;
import play.i18n.Messages;
import play.mvc.Before;
import play.mvc.Controller;

/**
 * Hanterar alla superadminfunktioner. 
 * Först kontolleras att användaren är superadministratör
 * @author weetech
 */
public class SuperAdminController extends PlanController {
    
   /* @Before
    public static void authority() throws AdminController.NoAdminException
    {
       try{
            
           if(!(PlanController.user instanceof SuperAdmin))
            {
                flash.put("message", "You.need.to.login.as.superadministrator");
                Application.loginform();
            }
            
       } catch(Exception ex)       {
           Application.loginform();
           
       }
    }*/  
    
    /**
     * Visar första sidan för superadministratörs panelen
     */
    public static void index()
    {
        render("bigadmin/index.html");
    }
    
    /**
     * Visar superadministratörspanelen för moduler
     */
    public static void modules()
    {
        render("bigadmin/modules.html");
    }
    
    /** 
     * Visar superadministratörspanelen för grupper
     */
    public static void groups()
    {
        
        List<Grupp> groups = Grupp.find("select g from Grupp g where g.parent = null").fetch();
        render("bigadmin/groups.html", groups);
    }
    
     /**
     * Visar superadministratörs panelen för användare, (sorterad enligt company.name)
     */
    public static void users()
    {
        List<UserBase> users = UserBase.find("select u from UserBase u order by u.company.name, u.name").fetch();
        render("bigadmin/users.html", users);
    }
    
    /**
     * NO USE!
     * @param id 
     */
    public static void getGroupById(long id) throws Exception
    {
        throw new Exception("NO IMPLEMENTED! - superadmin.getGroupById");
        //return Grupp.findById(id);
    }
    
   
    
    public static void addAdminToModule(Admin user, Module module)
    {        
        if(user == null || module == null) Controller.forbidden("Not an allowed request!");
       
       
            if(module.addUser(user))
            {
                flash.put("message", Messages.get("module %s added to user %s", module.name, user.name));
            }
            else{
                flash.put("message", Messages.get("user %s already have access to %s module", user.name,module.name));
            }
        
       
        
        SuperAdminController.modules();
    }
}
