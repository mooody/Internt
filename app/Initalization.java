
import java.util.ArrayList;
import models.*;
import play.Logger;
import play.jobs.Job;
import play.jobs.OnApplicationStart;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author weetech
 */
@OnApplicationStart
public class Initalization extends Job {

    public void doJob() throws Exception {

        Logger.info("Initalization.doJob()");
        boolean create = false;
        //Skapa defaultuser
        try{
            if(UserBase.count()==0){
                create = true;
            }
        } catch (Exception ex){
            create = true;
        }
            
        if(create){
            
            SuperAdmin user = new SuperAdmin();
            user.email = play.Play.configuration.getProperty("defaultuser.email");
            user.name = play.Play.configuration.getProperty("defaultuser.name");
            user.setPassword(play.Play.configuration.getProperty("defaultuser.pass"));
            
            Logger.info("Creating default user %s", user.email);
            
            user.save();
            
            Company company = new Company();
            String companyName = play.Play.configuration.getProperty("defaultuser.company");
            company.name = companyName;
           
            company.addUser(user);
			if(user.companies == null) user.companies = new ArrayList<Company>();
			user.companies.add(company);
            company.save();
            
            String defaultgrupp = play.Play.configuration.getProperty("defaultuser.grupp");
            
            Grupp grupp = new Grupp(company.id);
            grupp.name = defaultgrupp;
            if(grupp.users == null) grupp.users = new ArrayList();
            grupp.users.add(user);
            
            Logger.info("Creating default group %s", grupp.name);
            grupp.save();
            
            
            
       
        }
    
    }
    
    
    
    
}
