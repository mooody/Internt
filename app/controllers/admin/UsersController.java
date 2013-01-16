/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers.admin;

import controllers.PlanController;
import java.util.List;
import models.Admin;
import models.Company;
import models.Grupp;
import models.UserBase;
import play.Logger;
import play.i18n.Messages;
import play.mvc.Controller;
import java.util.ArrayList;

/**
 *
 * @author weetech
 */
public class UsersController extends AdminController {
    
    public static void index()
    {
        UserBase tUser = UserBase.findById(getUserId());
        //List<Grupp> groups = tUser.getAllGroups();
        List<UserBase> users = UserBase.find("select u from UserBase u "
                + "where u.company.id = :crit").bind("crit", tUser.company.id).fetch();
        render("admin/users/show.html", users);
    }   
     
    public static void create()
    {
        List<Grupp> groups = Grupp.findAll();
        
        render("admin/users/create.html", groups);
    }
    
	/**
	* Sparar en ny användar till ett företag
	*/
    public static void save()
    {
        //hämta ut administratörens företag
        UserBase user = params.get("user", UserBase.class);
        
        boolean update = (user.id!=null&&user.id>0)?true:false;
        
        Controller.notFoundIfNull(user, Messages.get("user.not.found"));
        
        if(update)
        {
            user.save();
            flash.put("message", Messages.get("user.updated"));
            edit(user.id);
        }
        else
        {
            Company company = PlanController.user().company;

            user.company = company;
			if(user.companies == null) user.companies = new ArrayList<Company>();
			user.companies.add(company);

            if(user!=null)
            {
                user.save();
            }
            
            flash.put("message", Messages.get("user.created"));
            index();

        }        
    }
    
    public static void edit(Long id)
    {
        UserBase user = UserBase.findById(id);
        render("admin/users/edit.html", user);
    }
    
    
    
 

    public static void addGroupToUser(Long userid, Long groupId)
    {
        Logger.info("Users.addGroupToUser - adding");
        
        if(PlanController.user() == null || !(PlanController.user() instanceof Admin))
        {
                Controller.forbidden();
        }
        
        //Grupp g = Grupp.findById(groupId);
        UserBase u = UserBase.findById(userid);
                
        String message = Messages.get("Group.added.to.user %s %s",groupId, u.name);  
        try {
            long gId = u.addGroupToUser(groupId);
            
            if(gId>0)
            {
                Grupp parent= Grupp.find("select g from Grupp g where g.id = :crit").bind("crit", gId).first();
                message = Messages.get("User.inready.in.parent.group %s",parent.name);  
            }
        } catch (Exception ex) {
            message = Messages.get("Some.error.occured");
        }
        
    
        
        flash.put("message", message);
        edit(u.id);
        
    }
    
    public static void removeUserFromGroup(Long userId, Long groupId)
    {
        Grupp group = Grupp.findById(groupId);
        
        UserBase user = UserBase.findById(userId);
        
        String message = Messages.get("user.removed.from.group %s %s",group.name, user.name);
        try 
        {
            if(!group.users.remove(user))
            {
                message = Messages.get("user.not.removed.from.group %s %s",group.name, user.name);
            }
            
            group.save();   
        } catch(Exception ex) {
            message = Messages.get("error.occured.when.try.to.remove.user %s %s",group.name, user.name);
        }
        
        Logger.info("Users.removeUserFromGroup (%s)", message);
        
        flash.put("message", message);
        edit(user.id);
        
    }
    
    public static void setUserType(Long id,boolean admin) throws Exception
    {
        UserBase user = UserBase.find("select u from User u where u.id = :crit").bind("crit", id).first();
      
        UserBase.em().createNamedQuery("update u from User u set DTYPE = ADMIN where u.id = "+id);
        throw new Exception("NotImplentedYet");
                
        
    }
    
    public static void deleteUser(Long id)
    {
        UserBase user = UserBase.findById(id);
        Controller.notFoundIfNull(user, "Not.found.user");
        
        List<Grupp> groups = Grupp.find("select g from Grupp g left join g.users u where u.id = :userid").bind("userid", id).fetch();

        for(Grupp g: groups)
        {
            g.users.remove(user);
            g.save();
        }
        user.delete();
        
        String message = "user.deleted";
        flash.put("message", message);
        
        index();
    }
    
}
