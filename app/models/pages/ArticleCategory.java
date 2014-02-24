/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.pages;

import javax.persistence.Entity;
import javax.persistence.Table;
import play.db.jpa.Model;

/**
 *
 * @author mikael
 */
@Entity(name="pages.ArticleCategory")
@Table(name="pages_ArticleCategory")
public class ArticleCategory extends Model{
	public String Name;
	
}
