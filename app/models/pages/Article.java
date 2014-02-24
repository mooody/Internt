/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.pages;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import models.Company;
import play.db.jpa.Model;

/**
 *
 * @author mikael
 */
@Entity(name="pages.article")
@Table(name="pages_article")
public class Article extends Model{
	public String title;
        @Basic(fetch=FetchType.LAZY)
	public String content;
        
	public boolean menuitem=false;
	public int menyorder = 0;
        @ManyToOne
        public Company company;
        public boolean global = false;
	
        @Transient
	public void copy(Article source)
	{
		this.title = source.title;
		this.content = source.content;
		this.menuitem = source.menuitem;
                this.global = source.global;
	}
	
	@Override
	public String toString()
	{
		return "{Id:"+this.id+" title:"+this.title+" global:"+this.global+" company:"+this.company+"}";
	}
}
