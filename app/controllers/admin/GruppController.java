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

    public static void save(Grupp grupp) throws Exception
    {
        Grupp parent = Grupp.findById(grupp.parent.id);
        parent.childs.add(grupp);
        
        grupp.setCompanyId(PlanController.user().company.id);
        grupp.save();
        
        parent.save();
        index();
    }
    
    public static void delete() throws Exception
    {
        Long groupid = params.get("groupid",Long.class);
        Grupp group = Grupp.findById(groupid);
        Controller.notFoundIfNull(group, "Gruppen existerar inte!");
        
        if(group.childs == null || group.childs.size() == 0 )
        {
            Logger.info("GruppController.delete has childs");
        }
        
        Grupp parent = group.parent;
        
        
        parent.childs.remove(group);
        parent.save();
        
        group.delete();
        Controller.flash.put("message", Messages.get("group.erased"));
        
        index();
    }
}
