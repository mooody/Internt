/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import controllers.PlanController;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import models.Core.Module;
import models.UserBase;
import play.Logger;

/**
 * ModuleController. 
 * 
 * Extend all module controllers with this controller.
 * this controller is saved in the db.
 * @author weetech
 */
@Entity
public class ModuleController extends PlanController{
    
    @Id 
    @GeneratedValue
    public Long id;
    
    public Long getId() {
        return id;
    }

    public Object _key() {
        return getId();
    }
    
    @ManyToMany
    public List<Module> modules;
    
    /**
     * Fixar en länk till indexmetoden för modulen
     * @param moduleId 
     */
    public static void index(Long moduleId)
    {
        Module module = Module.findById(moduleId);
        redirect(module.getModuleControllerActionRoute("index"));
    }
    
    //Laddar in peek från modulen
    public static void peek(Long moduleId)
    {
        Logger.info("Loading peek from %s", moduleId);
        Module module = Module.findById(moduleId);
        redirect(module.getModuleControllerActionRoute("peek"));
        //render(module.name+"/peek.html");
    }
    

   
            
     
    
}
