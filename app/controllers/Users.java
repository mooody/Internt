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
import models.UserBase;
import play.Logger;
import play.cache.Cache;
import play.db.jpa.GenericModel;
import play.i18n.Messages;
import play.mvc.Controller;

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
        UserBase user = UserBase.findById(PlanController.user.id);
        render(user);
    }
    
    public static void save(UserBase user)
    {
        if(user.id != PlanController.user.id)
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
             
            user.password = pw1;
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
