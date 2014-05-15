package models.Core;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import models.Company;
import models.UserBase;
import play.db.jpa.Model;

/**
 *
 * @author weetech
 */
@Entity
@Table(name = "core_invite")
public class Invite extends Model{

	private String msg;
	@ManyToOne
	private Company company;
	@ManyToOne
	private UserBase to;
	@ManyToOne
	private UserBase from;
        @OneToMany
        List<Module> modules;
        
        @Temporal( TemporalType.DATE )
        private Date created;
        
        public Date getDate()
        {
            return this.created;
        }

        public List<Module> getModules() {
            return modules;
        }

        public void addModule(Module module)
        {
            if(this.modules == null) this.modules = new ArrayList<Module>();
            this.modules.add(module);
        }
        public void setModules(List<Module> modules) {
            this.modules = modules;
        }
        private String usertype = "User";

        public String getUsertype()
        {
            return usertype;
        }
	
	public UserBase getFrom(){ return this.from; }
	public UserBase getTo(){ return this.to; }
	public Company getCompany(){ return this.company;}
	public String getMsg(){ return this.msg; }
	
	public Invite(Company _company, UserBase _to, UserBase _from, String _msg, String _usertype)
	{
		super();
		this.msg = _msg;
		this.company = _company;
		this.to = _to;
		this.from = _from;
                this.usertype = _usertype;
                this.created = new Date();
		
	}
	
	public static List<Invite> getInvites(UserBase user)
	{
		List<Invite> invites = null;
		try
		{
			invites = Invite.find("select i from Invite i where i.to.id = :uid")
			.bind("uid", user.id)
			.fetch();
		}
		catch(Exception ex)
		{
			play.Logger.error(ex.getMessage()+"\n"+ex.getCause());
		}
		return invites;
	}
		
	
}