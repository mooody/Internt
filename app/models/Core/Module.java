/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.Core;

import controllers.ModuleController;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import models.Admin;
import models.UserBase;
import play.Logger;
import play.data.validation.Required;
import play.data.validation.Unique;
import play.db.jpa.GenericModel;
import play.db.jpa.Model;
import play.mvc.Router;

/**
 *
 * @author weetech
 */
@Entity
public class Module extends Model{
    
    public String name;
    public String controllerName;
    public String moduleName;
    
    @ManyToMany(mappedBy="modules")
    public List<ModuleController> controllers;
    
    //@ManyToMany(mappedBy="modules")
    //public List<UserBase> users;
    
    public Module(String _name, String _controllerName, String _moduleName)
    {
        this.name = _name;
        this.controllerName = _controllerName;
        this.moduleName = _moduleName;
    }
    
    /**
     * Installation method. Call this method from a Job
     * @param _name (Module trival name)
     * @param _controllerName (default controller)
     * @param _moduleName (module real name)
     * @param controllers (List of all of the modules controllers)
     */
    public static void install(String _name, String _controllerName, String _moduleName, List<ModuleController> controllers)
    {

        try{
            //kontrollera om modulen redan finns
            Module module = Module.find(""
                + "select m from Module m"
                + " where m.moduleName = :moduleName").bind("moduleName", _moduleName).first();

            if(module == null)
            {
                Logger.info("Modulen %s installeras...", _moduleName);
                module = new models.Core.Module(_name, _controllerName, _moduleName);
                
                if(module.controllers == null) module.controllers = new ArrayList();
                
                for(ModuleController mc: controllers)
                {
                    module.controllers.add(mc);
                }
                
                //Module.setDefaultRights(module);
                module.save();
                Logger.info("Modulen %s installerades utan fel", _moduleName);
            }
           
            Logger.info("Modulen %s finns tillgänglig", _moduleName);
            
        } catch (Exception ex){
            Logger.info("Ett fel inträffade när modulen %s skulle installeras!", _moduleName);
        }           
 
    }
    
    /**
     * Hämtar ut headAdmin och ger rättigheter till denna
     * @param module 
     */
	 /*
    private static boolean setDefaultRights(Module module) throws Exception
    {
       
        String email = play.Play.configuration.getProperty("defaultuser.email");

        Admin admin = UserBase.find("byEmail", email).first();
        if(admin == null)
        {
            Logger.info("module.setDefaultRights, Cant find admin. Have you made first installation?");
            throw new Exception("Kör installationen av modulen igen när du har installerat huvudadministratör");
        }

        if(module.users==null) module.users = new ArrayList();

        module.users.add(admin);
        
        return true;
        
        
    }*/
    
    /**
     * Adds a user to the module. If a User is added he/she will see the 
     * module at mypage.
     * 
     * @param user
     * @return true if success otherwise false
     */
	 /*
    public boolean addUser(UserBase user)
    {
        if(this.users == null) this.users = new ArrayList();
        
        if(!this.users.contains(user))
        {
        
            this.users.add(user);
            this.save();
            return true;
        }
        else
        {
            return false;
        }
    }*/
    
    /**
     * Returns the url och the requested action of the default controller.
     * 
     * Can be used to redirect to the action
     * 
     * @param action (Action of controller)
     * @return the url...
     */
    public String getModuleControllerActionRoute(String action)
    {
       String route = Router.reverse(this.moduleName+"."+this.controllerName+"."+action).url;
       return route;
    }
	
	@Override
	public String toString(){
		return this.name.toString();
	}
    
   
       
}
