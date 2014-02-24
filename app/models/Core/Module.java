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
import javax.persistence.Column;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
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
@Table(name="core_module")
public class Module extends Model{
   
    public String name;
    public String controllerName;
    public String moduleName;
    @Column(nullable=false)
    private String userAccessType="User";
    public String getUserAccessType(){return this.userAccessType;}
    
    @ManyToMany(mappedBy="modules")
    public List<ModuleController> controllers;
    
    //@ManyToMany(mappedBy="modules")
    //public List<UserBase> users;
    
    public Module(String _name, String _controllerName, String _moduleName)
    {
        this(_name, _controllerName, _moduleName, "User");
    }
	
    
    public Module(String _name, String _controllerName, String _moduleName, String _userAccessType)
    {
        this.name = _name;
        this.controllerName = _controllerName;
        this.moduleName = _moduleName;
	this.userAccessType = _userAccessType;
    }

    /**
     * Installation method. Call this method from a Job
     * @param _name (Module trival name)
     * @param _controllerName (default controller)
     * @param _moduleName (module real name)
     * @param controllers (List of all of the modules controllers)
     */
    public static Module install(String _name, String _controllerName, String _moduleName, List<ModuleController> controllers)
    {
	return install( _name, _controllerName, _moduleName, controllers , "User");
    }
	
    public static Module install(String _name, String _controllerName, String _moduleName, List<ModuleController> controllers , String _userAccessType)
    {
	try{
            //kontrollera om modulen redan finns
            Module module = Module.find(""
                + "select m from Module m"
                + " where m.moduleName = :moduleName").bind("moduleName", _moduleName).first();

            if(module == null)
            {
                Logger.info("Modulen %s installeras...", _moduleName);

		module = new models.Core.Module(_name, _controllerName, _moduleName, _userAccessType);
                
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
            return module;
            
        } catch (Exception ex){
            Logger.info("Ett fel inträffade när modulen %s skulle installeras!", _moduleName);
        } 
                 
        return null;
    }
    
    /**
     * Returns the url och the requested action of the default controller.
     * 
     * Can be used to redirect to the action
     * 
     * @param action (Action of controller)
     * @return the url... or null if no route is found
     */
    public String getModuleControllerActionRoute(String action)
    {
       try{
        String route = Router.reverse(this.moduleName+"."+this.controllerName+"."+action).url;
        return route;
       }catch(Exception ex){
           return null;
       }
    }
	
	@Override
	public String toString(){
		return this.name.toString();
	}
    
   
       
}
