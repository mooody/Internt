/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers.admin;

import controllers.PlanController;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import models.Admin;
import models.Company;
import models.Core.CompanyUserSettings;
import models.Core.Invite;
import models.Core.Module;
import models.Grupp;
import models.PrivilegeUser;
import models.User;
import models.UserBase;
import play.Logger;
import play.data.validation.Validation;
import play.i18n.Messages;
import play.mvc.Controller;

/**
 *
 * @author weetech
 */
public class UsersController extends AdminController
{

    public static void index()
    {
        //UserBase tUser = UserBase.findById(getUserId());
        //List<Grupp> groups = tUser.getAllGroups();
        /*List<UserBase> users = UserBase.find("select u from UserBase u "
         + "where u.company.id = :crit").bind("crit", tUser.company.id).fetch();
         */

        List<Invite> invites = Invite.find("byCompany", user().company).fetch();
        List<UserBase> users = UserBase.find("select u from UserBase u left join u.companies uc where uc.id = :company order by u.name asc")
                .bind("company", getCompanyId()).fetch();

        Map<Long, String> usertypes = new TreeMap<Long, String>();

        CompanyUserSettings cus = null;
        boolean userNotActivated = false;
        for (UserBase user : users)
        {
            Logger.info("username : %s " , user);
            if(user.activated)
            {
            cus = CompanyUserSettings.find("byUserAndCompany", user, user().company).first();
            usertypes.put(user.id, cus.getUserType());
            }
            else
            {
                userNotActivated = true;
            }
        }

        //render("admin/users/index.html", users, usertypes, invites,userNotActivated);
        render(users, usertypes, invites,userNotActivated);
    }

    public static void create()
    {
//      //List<Grupp> groups = Grupp.findAll();
//      List<Grupp> groups = Grupp.find("select g from Grupp g where g.companyId = :cid")
//              .bind("cid", user().company.id)
//              .fetch();
//      //THIS IS NOT GOOD! find ALL
//      //notFound("SEE UsersController.create()");
//      render("admin/users/create.html", groups);

        List<Module> modules = user().company.modules;
        //render("admin/users/create.html", modules);
        render(modules);
    }

    /**
     * Sparar en ny användar till ett företag Om användaren redan finns i
     * systemet skickas en inbjudan ut samt att en invite sparas undan Annars så
     * skapas en ny användare
     */
    public static void save(List<Long> moduleIds)
    {
        //hämta ut administratörens företag
        UserBase user = params.get("user", User.class);

        boolean update = (user.id != null && user.id > 0) ? true : false;

        Controller.notFoundIfNull(user, Messages.get("user.not.found"));

        if (update)
        {
            user.save();
            flash.put("message", Messages.get("user.updated"));
            edit(user.id);
        } else
        {
            Company company = PlanController.user().company;

            if (company.getUsersCount() <= company.usersWithMultipleAccounts.size())
            {
                Logger.info("Users: %s of %s", company.usersWithMultipleAccounts.size(), company.getUsersCount());
                Validation.addError("error", Messages.get("you.cant.add.user.your.company.has.accounts", company.getUsersCount()));
                Validation.keep();
                index();
            }

            //Finns användaren i systemet skicka en inbjudan istället
            UserBase invited = UserBase.find("byEmail", user.email).first();
           
            if (invited != null)
            {
                UsersController.createInvitation(invited, moduleIds);
                
            } 
            else
            {
                UsersController.createNewUser(user, moduleIds);
            }

            

            flash.put("message", Messages.get("user.created"));
            index();
        }
    }
    
    /**
     * Skapar en inbjudan till en användare
     * Sätter usertype från paramenter 'usertype'
     * @param invited
     * @param moduleIds 
     */
    private static void createInvitation(UserBase invited, List<Long> moduleIds)
    {

        String usertype = params.get("usertype") != null ? params.get("usertype") : "User";
        Invite invite = new Invite(user().company, invited, user(), Messages.get("core.invited.to.company", user(), user().company), usertype);
        
        notifiers.Mails.sendInvite(invite);
        
        //Lägg till modulerna till användaren
        for (Long moduleId : moduleIds)
        {
            Module m = Module.findById(moduleId);
            invite.addModule(m);
        }

        invite.save();
  
    }
    
    /**
     * Skapar en ny användare. Skapar en CUS, genererar ett lösen och skickar ut ett mail
     * @param user
     * @param moduleIds 
     */
    private static void createNewUser(UserBase user, List<Long> moduleIds)
    {
        //Lägg till modulerna till användaren
        CompanyUserSettings cus = null;
        user.generatePassword();
//        if (user.companies == null)
//        {
//            user.companies = new ArrayList<Company>();
//        }
//        user.companies.add(user().company);
        user.save();
        cus = user().company.attachUserToCompanyToGetCUS(user);
        //Skickar ut välkomstmailet
        notifiers.Mails.welcome(user);

        for (Long moduleId : moduleIds)
        {
            Module m = Module.findById(moduleId);
            cus.addModule(m);
        }

        String usertype = params.get("usertype") != null ? params.get("usertype") : "User";
        cus.setUserType(usertype);
    }

    public static void edit(Long id)
    {
        UserBase user = UserBase.findById(id);
        //render("admin/users/edit.html", user);
        render(user);
    }

    public static void addGroupToUser(Long userid, Long groupId)
    {
        if (PlanController.user() == null || !(PlanController.user() instanceof Admin))
        {
            Controller.forbidden();
        }

        Grupp group = Grupp.findById(groupId);
        if (group == null)
        {
            validation.addError("group", "no.group.choosen");
        } else
        {
            if (group.getCompanyId() != user().company.id)
            {
                Controller.forbidden(Messages.get("forbidden.task"));
            }
            //Grupp g = Grupp.findById(groupId);
            UserBase u = UserBase.findById(userid);

            String message = Messages.get("Group.added.to.user %s %s", groupId, u.name);
            try
            {
                long gId = u.addGroupToUser(groupId);

                if (gId > 0)
                {
                    Grupp parent = Grupp.find("select g from Grupp g where g.id = :crit").bind("crit", gId).first();
                    message = Messages.get("User.inready.in.parent.group", parent.name);
                }
            } catch (Exception ex)
            {
                message = Messages.get("Some.error.occured");
            }

            flash.put("message", message);
            edit(u.id);
        }

    }

    public static void removeUserFromGroup(Long userId, Long groupId)
    {
        Grupp group = Grupp.findById(groupId);

        UserBase user = UserBase.findById(userId);

        String message = Messages.get("user.removed.from.group %s %s", group.name, user.name);
        try
        {
            if (!group.users.remove(user))
            {
                message = Messages.get("user.not.removed.from.group %s %s", group.name, user.name);
            }

            group.save();
        } catch (Exception ex)
        {
            message = Messages.get("error.occured.when.try.to.remove.user %s %s", group.name, user.name);
        }

        Logger.info("Users.removeUserFromGroup (%s)", message);

        flash.put("message", message);
        edit(user.id);

    }

    public static void setUserType(long id, short usertype) throws Exception
    {
        //long count = UserBase.count("select count(id) from UserBase u where u.id = ?", id);
        UserBase user = UserBase.findById(id);
        Company company = user().company;
        if (user == null)
        {
            notFound();
        }

        if (id == getUserId())
        {
            flash("message", Messages.get("you.cant.change.your.own.usertype"));
            edit(id);
        }

        String discriminator = null;

        switch (usertype)
        {
            case 1:
            {
                //kontollerar så företaget kan lägga till flera administratörer
                long count = Admin.count("select count(a.id) from Admin a left join a.companies c where c.id = ?", company.id);
                if (count >= company.getAdminsCount())
                {
                    Validation.addError("error", Messages.get("you.have.to.upgrade.admin.counts", company.getAdminsCount()));
                    Validation.keep();
                    edit(id);
                }

                discriminator = "Admin";
                break;
            }
            case 2:
            {
                discriminator = "Super";
                break;
            }
            case 3:
            {
                //kontrollerar så företaget kan lägga till flera användare med privilegium
                long count = PrivilegeUser.count("select count(p.id) from PrivilegeUser p left join p.companies c where c.id = ?", company.id);
                if (count - company.getAdminsCount() >= company.getPrivilegeUsersCount())
                {
                    Validation.addError("error", Messages.get("you.have.to.upgrade.privilegue.counts", company.getPrivilegeUsersCount()));
                    Validation.keep();
                    edit(id);
                }

                discriminator = "PrivilegeUser";
                break;
            }
            default:
                discriminator = "User";
                break;
        }

        play.db.jpa.JPA.em().createNamedQuery("UserBase.changeUserType")
                .setParameter("type", discriminator).setParameter("id", id)
                .executeUpdate();

        CompanyUserSettings cus = CompanyUserSettings.find("select c from CompanyUserSettings c where c.user.id = :uid and c.company.id = :company").bind("uid", id).bind("company", company.id).first();

        cus.setUserType(discriminator);

        edit(id);

    }

    public static void removeUser(Long id)
    {

        if (id == getUserId())
        {
            flash("message", Messages.get("you.cant.erase.yourself"));
            index();
        }

        UserBase user = UserBase.findById(id);
        
        Controller.notFoundIfNull(user, "Not.found.user");
        user.removeCompany(user().company);
        List<Grupp> groups = Grupp.find("select g from Grupp g left join g.users u where u.id = :userid and g.companyId = :companyId").bind("userid", id).bind("companyId", PlanController.getCompanyId()).fetch();

        for (Grupp g : groups)
        {
            g.users.remove(user);
            g.save();
        }
        
        /*
        List<Grupp> groups = Grupp.find("select g from Grupp g left join g.users u where u.id = :userid and g.companyId = :companyId").bind("userid", id).bind("companyId", PlanController.getCompanyId()).fetch();

        Admin admin = (Admin) PlanController.user();
        for (Grupp g : groups)
        {
            g.users.remove(user);
            g.save();
        }

        user.companies.remove(admin.company);
        admin.company.users.remove(user);
        admin.company.save();

        CompanyUserSettings cus = CompanyUserSettings.find("byUserAndCompany", user, admin.company).first();
        if (cus != null)
        {
            cus.delete();
        }

        //Om användaren är inloggad i flera företag
        if (user.companies.size() == 0)
        {
            flash.put("message", Messages.get("user.deleted"));
            user.delete();
        } else
        {
            flash.put("message", Messages.get("user.removed.from.company"));
            //se till användaren har ett aktivt företag som inställning...
            user.company = user.companies.get(0);
            user.save();
        }
                */

        index();
    }
    @Deprecated
    public static void deleteUser(Long id)
    {

        if (id == getUserId())
        {
            flash("message", Messages.get("you.cant.erase.yourself"));
            index();
        }

        UserBase user = UserBase.findById(id);
        
        Controller.notFoundIfNull(user, "Not.found.user");
        user.removeCompany(user().company);
        /*
        List<Grupp> groups = Grupp.find("select g from Grupp g left join g.users u where u.id = :userid and g.companyId = :companyId").bind("userid", id).bind("companyId", PlanController.getCompanyId()).fetch();

        Admin admin = (Admin) PlanController.user();
        for (Grupp g : groups)
        {
            g.users.remove(user);
            g.save();
        }

        user.companies.remove(admin.company);
        admin.company.users.remove(user);
        admin.company.save();

        CompanyUserSettings cus = CompanyUserSettings.find("byUserAndCompany", user, admin.company).first();
        if (cus != null)
        {
            cus.delete();
        }

        //Om användaren är inloggad i flera företag
        if (user.companies.size() == 0)
        {
            flash.put("message", Messages.get("user.deleted"));
            user.delete();
        } else
        {
            flash.put("message", Messages.get("user.removed.from.company"));
            //se till användaren har ett aktivt företag som inställning...
            user.company = user.companies.get(0);
            user.save();
        }
                */

        index();
    }
    
    public static void removeInvitation(long id)
    {
        //hämta invitation
        Invite invite = Invite.findById(id);
        UserBase user = invite.getTo(); 
        invite.delete();
            
        flash("message",Messages.get("core.invite.removed", user));
        index();
    }

//    @Deprecated
//    public static void sendInvitationMail(long id)
//    {
//        UserBase user = UserBase.findById(id);
//        Invite invite = new Invite(user().company, user, user(), "");
//        invite.save();
//        notifiers.Mails.sendInvite(invite);
//        index();
//
//    }

}
