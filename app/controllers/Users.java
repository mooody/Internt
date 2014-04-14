/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.util.List;
import java.util.logging.Level;
import models.Admin;
import models.Company;
import models.Grupp;
import models.User;
import models.UserBase;
import play.Logger;
import play.cache.Cache;
import play.db.jpa.GenericModel;
import play.i18n.Messages;
import play.mvc.Controller;
import play.*;
import java.util.ArrayList;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import models.Core.Module;

/**
 *
 * @author weetech
 */
public class Users extends PlanController{
    
/**
	* H�mtar in alla moduler som anv�ndaren har. Om f�retaget har blivit av med r�ttigheterna f�r modulen 
	* s� kommer den tas bort fr�n listan.
	*/
    public static void mypage()
    {
		UserBase user = user();
		Company company = user.company;
		List<Module> useraccess = user.getUserAndGroupModules();
                if(useraccess==null)
                {
                   render();
                }
                else
                {
                  List<Module> modules = new ArrayList<Module>();
                  for(Module module: useraccess)
                  {
                          if(!modules.contains(module)&&company.modules.contains(module))
                          {
                                      modules.add(module);
                          }
                  }
                  render(modules);
                }
    }
    
    public static void myaccount()
    {
        UserBase user = UserBase.findById(getUserId());
        render(user);
    }
    
    public static void save(User user)
    {
        if(user.id != getUserId())
        {
            Controller.forbidden("Not allowed!");
        }
        
        String old = params.get("oldpassword");
        
        try {
            if(!user.getPassword().equals(old))
            {
                validation.addError("error",Messages.get("validation.oldpassword.dont.match"));
                validation.keep();
                myaccount();
            }
        } catch (InvalidKeyException ex) {
            java.util.logging.Logger.getLogger(Users.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalBlockSizeException ex) {
            java.util.logging.Logger.getLogger(Users.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BadPaddingException ex) {
            java.util.logging.Logger.getLogger(Users.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(Users.class.getName()).log(Level.SEVERE, null, ex);
        }
        String pw2 = params.get("password2");
        String pw1 = params.get("password1");
        
        if(!pw2.isEmpty()&&!pw1.isEmpty())
        {
            if(!pw2.equals(pw1))
            {
                validation.addError("password.error",Messages.get("user.password.dont.match"));
            }
             
            try {
                    user.setUserPassword(pw1);
            }
            catch(UserBase.PassWordCreteriaException pwe)
            {
                validation.addError("error",pwe.getMessage());
            }
            catch (Exception ex) {
                    validation.addError("error",Messages.get("could.not.set.new.password.contact.site.helpdesk"));
                    validation.keep();
                    myaccount();
            }	
        }
        else if(pw1.isEmpty()^pw2.isEmpty())
        {
			validation.addError("password.error",Messages.get("user.password.dont.match"));
        }
		
		if(user.name.isEmpty())
		{
			validation.addError("user.name",Messages.get("user.name.is.empty"));
		}
		
		if(user.email.isEmpty())
		{
			validation.addError("user.email",Messages.get("user.email.is.empty"));
		}
		
		if(validation.hasErrors())
		{
			validation.keep();
                        myaccount();
		}
        
                UserBase userToSave = User.findById(user.id);
        user.save();
        flash.put("message", Messages.get("user.updated"));
        myaccount();
        
    }
	
    /**
     * Called by AJAX
     * @param userId 
     */
    public static void viewSingelUser(long userId)
    {
       UserBase user = UserBase.findById(userId);
       if(user.companies.contains(user().company))
         render(user);
       else{
          forbidden(Messages.get("core.user.not.in.compay"));
       }
    }
    
    
}
