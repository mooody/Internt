/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.pages;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import models.Company;
import play.db.jpa.Model;

/**
 *
 * @author mikael
 */
@Entity(name="core.homepage")
@Table(name="core_homepage")
public class HomePage extends Model{
	@OneToOne
	public Article frontpage;
        @ManyToOne
        public Company company;
}
