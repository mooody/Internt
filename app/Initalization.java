
import java.util.ArrayList;
import java.util.List;
import models.Company;
import models.Grupp;
import models.SuperAdmin;
import models.UserBase;
import models.booking.BookingYear;
import models.booking.Verification;
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
            
		//Skapar upp default admin
		//s�tt defaultuser.email, defaultuser.name, defaultuser.pass, defaultuser.company, defaultuser.group i application.conf
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
                //EDITERINGAR AV DATABASEN
		//ver();
		//setCompanyUserSettings();
		/*Company company = Company.findById(1L);
		if(company!=null)
                setYear(company);*/
    }
	
    //se till att fixa unika constrains i databasen, year, company, verid
    //Samt även radera gamla year-fältet i bookingsettings
	public void setYear(Company company)
	{
                if(company == null) return;
		List<BookingYear> years = BookingYear.find("byCompany", company).fetch();
		
		play.Logger.info("Found %s years in Company %s", years.size(), company.name);
		long count = 0;
		for(BookingYear year: years)
		{
			play.Logger.info("Y : %s --- %s", year.startdate, year.enddate);
			List<Verification> verifications = Verification.find("select v from Verification v where v.company = :company and v.kontering = (0) and v.date between :min and :max order by v.date asc")
				.bind("company", company)
				.bind("min",year.startdate)
				.bind("max",year.enddate)
				.fetch();

			play.Logger.info("Found %s verifications", verifications.size());
			for(Verification v : verifications)
			{
				if(v.year == null)
				{
					count++;
					v.verificationNr = count;
					v.year = year;
					v.save();
					
				}
			}

			play.Logger.info("Edited %s verifications", count);
			count = 0;
		}
	}
	
	/*
	public void ver()
	{
		
		List<Verification> verifications = Verification.find("select v from Verification v where v.company.id = 1 order by v.date asc").fetch();
		
		int index = 0;
		for(Verification verification: verifications)
		{
			index++;
			verification.verificationNr = index+verifications.size()+10;
			Logger.info("i:%s nr:%s", index, verification.verificationNr);
			
		}
		for(Verification verification: verifications)
		{
			verification.save();
		}
		
		
		index=0;
		for(Verification verification: verifications)
		{
			index++;
			verification.verificationNr = index;
			Logger.info("i:%s nr:%s", index, verification.verificationNr);
			
		}
		for(Verification verification: verifications)
		{
			verification.save();
		}
		Logger.info("%s", verifications.size());
		
	}
	*/
	/*
	public void setCompanyUserSettings()
	{
		List<UserBase> users = UserBase.find("select u from UserBase u").fetch();
		for(UserBase user: users)
		{
			for(Company company: user.companies)
			{
				CompanyUserSettings cus = CompanyUserSettings.find("byUserAndCompany", user, company).first();
				if(cus==null)
				{
					Logger.info("Creating cus for %s in %s", user.name, company.name);
					cus = new CompanyUserSettings(user, company);
					if(user.email.equals("mikael.forsberg@weetech.se"))
					{
						cus.setUserType("SuperAdmin");
					}
				}
			}
		}
	}*/
    
    
    
    
}
