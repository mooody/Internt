import org.junit.*;
import java.util.*;
import play.test.*;
import models.*;
import play.Logger;

public class BasicTest extends UnitTest {
    
    @Test
    public void testGroups(){
        Grupp grupp1 = new Grupp(1);
        grupp1.name = "Grupp 1";
        
        grupp1.childs = new ArrayList();
        
        Grupp grupp2 = new Grupp(1);
        grupp2.name = "Grupp 2";
        grupp2.save();
        
        grupp1.childs.add(grupp2);
        grupp1.save();
        
        UserBase user = new UserBase();
        
        user.name = "Johan";
        user.password = "pass";
        user.email = "mail@mail.se";
        user.save();
        
        grupp2.users = new ArrayList();
        
        grupp2.users.add(user);
        grupp2.save();
        
        Logger.info("Checking if user %s in group %s", user, grupp2);
        
        for(Grupp g: grupp1.childs)
        {
            Logger.info("------------");
            Logger.info("%s", g.name);
            
            for(UserBase u : g.users)
            {
                Logger.info("%s", user);
            }
        }
        assertEquals(true,user.isUserInGroup(grupp2));
        Logger.info("Checking if user %s NOT in group %s", user, grupp1);
        assertEquals(false, user.isUserInGroup(grupp1));
        
        grupp2.delete();
        grupp1.delete();
        user.delete();
    }

}
