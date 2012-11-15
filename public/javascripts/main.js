$(document).ready(
    function(){
       fadeMessageBox();
    }
);
    
function fadeMessageBox()
{
    var messagebox = $("div.message");
    //kontrollerar så meddelande boxen existerar
    if(messagebox != null)
    {
        setTimeout("$('div.message').hide(1000);",1000);

    } 
    
}