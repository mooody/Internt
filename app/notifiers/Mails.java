package notifiers;
 
import play.*;
import play.mvc.*;
import java.util.*;
import org.apache.commons.mail.*; 
import models.UserBase;
import models.mail.Invite;
import play.i18n.Messages;
 
public class Mails extends Mailer {
 
   public static void welcome(UserBase user) {
      setSubject("Welcome %s", user.name);
      addRecipient(user.email);
      setFrom("Me <me@me.com>");
      //EmailAttachment attachment = new EmailAttachment();
      //attachment.setDescription("A pdf document");
      //attachment.setPath(Play.getFile("rules.pdf").getPath());
      //addAttachment(attachment);
      send(user);
   }
 
   public static void lostPassword(UserBase user) {
      String newpassword = "test";
      setFrom("Robot <robot@thecompany.com>");
      setSubject("Your password has been reset");
      addRecipient(user.email);
      send(user, newpassword);
   }
   
   public static void sendInvite(Invite invite)
   {
      setFrom(invite.getFrom().name+" <"+invite.getFrom().email+">");
      addRecipient(invite.getTo().email);
	  setSubject(invite.getFrom().name+" "+Messages.get("invites.you.to")+" "+invite.getCompany().name);
      send(invite);
   }
 
}