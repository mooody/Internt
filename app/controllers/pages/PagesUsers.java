/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers.pages;

import controllers.PlanController;
import java.util.ArrayList;
import java.util.List;
import models.pages.Article;
import play.cache.Cache;

/**
 *
 * @author weetech
 */
public class PagesUsers extends PlanController{
    public static List<Article> getGlobalMenuItems()
    {
         List<Article> globalArticles = Article.find("byGlobalAndMenuitem", true,true).fetch();
         return globalArticles;
    }
    
    public static void showArticle(long id)
    {
        List<Article> breadcrumb = (List<Article>)Cache.get(session.getId()+"breadcrumb", List.class);

        Article article = Article.findById(id);
        article.company = null;

        if(breadcrumb==null)
        {
            breadcrumb = new ArrayList<Article>();
        }
        
        if(article.menuitem)
        {
            breadcrumb.clear();
            breadcrumb.add(article);
        }
        else
        {
            if(!breadcrumb.contains(article))
            {
                breadcrumb.add(article);
            }
        }
        Cache.set(session.getId()+"breadcrumb", breadcrumb);

        render("/pages/article.html",article,breadcrumb);
    }
}
