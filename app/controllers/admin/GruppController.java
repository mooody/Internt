/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers.admin;

import controllers.PlanController;
import java.util.ArrayList;
import java.util.List;
import models.Grupp;
import models.User;
import models.UserBase;
import play.Logger;
import play.db.jpa.GenericModel;
import play.i18n.Messages;
import play.mvc.Controller;
import models.Core.Module;

/**
 *
 * @author weetech
 */
public class GruppController extends AdminController{
  
    public static void index() throws Exception
    {
        List<Grupp> groups =  user().company.getCompanyGroups();
        Logger.info("AdminController.groups size= %s", groups.size());

        render("admin/groups/groups.html",groups);
    }
	
	public static void modules()
    {
		String flashid = flash.get("group");
		Long id = params.get("groupid", Long.class);

		Grupp group = Grupp.findById(id!=null?id.longValue():(flashid!=null?new Long(flashid).longValue():0));
		
		List<Module> groupmodules = group.getModules();
		List<Module> companymodules = user().company.getCompanyModules();
        render("admin/modules/modules.html", groupmodules, companymodules, group);
    }
	
	 public static void addModuleToGroup(long gId, List<Long> modid)
	{
		Grupp group = Grupp.findById(gId);
		//Lägg företaget i flash för att vi ska kunna få tag i det i metoden modules()
		
		flash.put("group", group.id);
		if(group == null )
		{
			flash.put("message", Messages.get("admin.NoGroupSelected"));
			GruppController.modules();
		}
		
		
		//Kontrollerar så någon modul är vald om den inte är vald så tömmer vi hela medulslistan för företaget
		if(modid == null || modid.size()==0){
			flash.put("message", "SuperAdmin.NoModuleSelected");
			GruppController.modules();
		}
		
		List<String> added = new ArrayList<String>();
		for(Long moduleId: modid)
		{
			if(moduleId != null)
			{
				Module module = Module.findById(moduleId);
				if(module != null)
				{
					if(addModuleToGroup(group, module))
					{
						added.add(module.name);
					}
				}
			}
		}    
		if(added.size()>0)
		{
			flash.put("message", Messages.get("Modules.added.to.group", added, group.name));
			group.save();
		}
		GruppController.modules();
	}
	
	public static void removeModuleFromGroup(long groupid, long moduleid)
	{
		Grupp group = Grupp.findById(groupid);

		notFoundIfNull(group, "Felaktig Grupp");
		flash.put("group", group.id);
		
		String modulename = null;
		for(Module module : group.getModules())
		{
			if(module.id == moduleid)
			{
				group.modules.remove(module);
				modulename = module.name;
				group.save();
				break;
			}
		}
		
		if(modulename!=null)
		{
			flash.put("message", Messages.get("admin.module.was.removed", modulename));
		}
		else
		{
			flash.put("message", Messages.get("No.modules.was.removed.from", group.name));
		}
		GruppController.modules();
	}
	
	private static boolean addModuleToGroup(Grupp group, Module module)
	{
		boolean companyHasAccessToModule = false;
		for(Module mod : user().company.modules)
		{
			if(module.id == mod.id)
			{
				companyHasAccessToModule = true;
				break;
			}
		}
		
		if(!companyHasAccessToModule) forbidden("Company has not access to Module "+module.name);
		
		try{
			group.modules.add(module);
			return true;
		} catch(Exception ex){
			Logger.error("Fel i AdminController.addModuleToCompanySettings" );
			Logger.error(ex.getMessage()+"\n"+ex.getCause().toString());
			return false;
		}
	}
	
	
    public static void save(Grupp grupp) throws Exception
    {
        Grupp parent = Grupp.findById(grupp.parent.id);
        parent.childs.add(grupp);
        
        grupp.setCompanyId(PlanController.user().company.id);
        grupp.save();
        
        parent.save();
		
		flash("message", Messages.get("group.is.created", grupp.name));
		
        index();
    }
    
	/*
	* Raderar en grupp utifrån id. 
	* hämtar ut gruppen från databasen, hämtar in föräldern och raderar gruppen från dess lista.
	* Sedan raderas gruppen.
	* Om gruppen ej har en förälder tillåts inte radering!
	**/
    public static void delete() throws Exception
    {
        Long groupid = params.get("groupid",Long.class);
        Grupp group = Grupp.findById(groupid);
        Controller.notFoundIfNull(group, "Gruppen existerar inte!");
        
		//om gruppen hör till ett annat företag
		if(group.getCompanyId() != user().company.id)
		{
			forbidden();
		}
		//Huvudgruppen kan inte raderas
		if(group.parent == null)
		{
			flash("message", Messages.get("is.your.headgroup.cant.erase", group.name));
			index();
		}
		
        if(group.childs == null || group.childs.size() == 0 )
        {
            Logger.info("GruppController.delete has childs");
        }
        
        Grupp parent = group.parent;
        
        
        parent.childs.remove(group);
        parent.save();
        
        group.delete();
        Controller.flash.put("message", Messages.get("group.erased", group.name));
        
        index();
    }

	/*
	public static void addModuleToGroup(long gId, List<Long> modid)
	{
		Grupp group = Grupp.findById(gId);
		//Lägg företaget i flash för att vi ska kunna få tag i det i metoden modules()
		
		flash.put("group", group.id);
		if(group == null )
		{
			flash.put("message", Messages.get("admin.NoGroupSelected"));
			AdminController.modules();
		}
		
		
		//Kontrollerar så någon modul är vald om den inte är vald så tömmer vi hela medulslistan för företaget
		if(modid == null || modid.size()==0){
			flash.put("message", "SuperAdmin.NoModuleSelected");
			AdminController.modules();
		}
		
		List<String> added = new ArrayList<String>();
		for(Long moduleId: modid)
		{
			if(moduleId != null)
			{
				Module module = Module.findById(moduleId);
				if(module != null)
				{
					if(addModuleToCompanyUserSettings(user, module))
					{
						added.add(module.name);
					}
				}
			}
		}    
		if(added.size()>0)
		{
			flash.put("message", Messages.get("Modules.added.to.user", added, user.name));
		}
		modules();
	}
	*/
}
