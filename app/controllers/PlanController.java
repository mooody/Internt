/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import controllers.admin.CompanyController;
import models.UserBase;
import models.Admin;
import models.User;
import play.Logger;
import play.cache.Cache;
import play.db.jpa.GenericModel;
import play.i18n.Messages;
import play.mvc.After;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Finally;

/**
 * Controller som kontrollerar så att användaren är inloggad.
 * !OBS ! vid omskrivning... sök redan på all användning av sessionuser i Catch!
 * @author weetech
 */
public class PlanController extends Controller{
    
    protected static UserBase user;
   
    @Before
    private static void getArgs()
    {


        //user = Cache.get("sessionuser", UserBase.class);
            
		try{
        user = UserBase.findById(new Long(session.get("userid")).longValue());
		} catch (NumberFormatException ne)
		{
			user = null;
		}
        
        Logger.info("userid: %s", session.get("userid"));
        if(user==null)
        {
            renderArgs.put("loginform",1);
            Application.loginform();
        }
        
		//DENNA LIGGER I ADMINCONTROLLER
        //kontroll om användaren tillhör ett företag. Annars skicka till vyn där vi skapar ett företag
        /*if(user.company == null && !(
                (Controller.request.action.equals("CompanyController.index")) 
                ||(Controller.request.action.equals("CompanyController.create"))
                ))
        {
            flash.put("message", Messages.get("you.need.to.create.a.company.before.you.can.continue"));
            CompanyController.index();
        }*/
        
        //om användaren inte är null så 
        if(user!=null)
        {
           UserBase updateduser = UserBase.findById(user.id);
           
           renderArgs.put("sessionuser",updateduser);
           Cache.set("sessionuser", updateduser);
           //user = UserBase.findById(new Long(session.get("userid")).longValue());
           Logger.info("PlanController.before Company:%s", user.company);
        }
        
        
    }
    
    @After
    private static void saveArgs()
    {
        /*user = Cache.get("sessionuser", UserBase.class);
       
        if(user!=null)
        {
           UserBase updateduser = UserBase.findById(user.id);
           renderArgs.put("sessionuser",updateduser);
           //Cache.set("sessionuser", updateduser);
        }
        */
        if(flash.contains("message"))
        {
            String msg = flash.get("message");
            
            renderArgs.put("message", msg);
        }
        
    }
}
