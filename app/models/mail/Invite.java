package models.mail;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import play.db.jpa.Model;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import models.Company;
import models.UserBase;
import java.util.List;
import javax.persistence.Table;

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
	
	public UserBase getFrom(){ return this.from; }
	public UserBase getTo(){ return this.to; }
	public Company getCompany(){ return this.company;}
	public String getMsg(){ return this.msg; }
	
	public Invite(Company _company, UserBase _to, UserBase _from, String _msg)
	{
		super();
		this.msg = _msg;
		this.company = _company;
		this.to = _to;
		this.from = _from;
		
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