/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

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

/**
 *
 * @author weetech
 */
public class Users extends PlanController{
    
    public static void mypage()
    {
        render();
    }
    
    public static void myaccount()
    {
        User user = UserBase.findById(getUserId());
        render(user);
    }
    
    public static void save(User user)
    {
        if(user.id != getUserId())
        {
            Controller.forbidden("Not allowed!");
        }
        
        String pw2 = params.get("password2");
        String pw1 = params.get("password1");
        
        if(!pw2.isEmpty()&&!pw1.isEmpty())
        {
            if(!pw2.equals(pw1))
            {
                flash.put("message", Messages.get("user.password.dont.match"));
                myaccount();
            }
             
			try {
				user.setPassword(pw1);
			} catch (Exception ex) {
				flash.put("message", Messages.get("could.not.set.new.password.contact.site.helpdesk"));
            myaccount();
			}	
        }
        else if(pw1.isEmpty()^pw2.isEmpty())
        {
            flash.put("message", Messages.get("user.password.dont.match"));
            myaccount();
        }
        
        
        
        user.save();
        flash.put("message", Messages.get("user.updated"));
        myaccount();
        
    }
    
    
}
