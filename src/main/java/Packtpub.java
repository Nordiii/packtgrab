import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.io.IOException;

public class Packtpub {
    private final WebClient webClient;

    private final String defaultPage = "https://www.packtpub.com";
    private final String loginFormID = "packt-user-login-form";
    private final String freeBooksPage = "https://www.packtpub.com/packt/offers/free-learning";
    private final String afterClaim = "https://www.packtpub.com/account/my-ebooks";

    private final String email;
    private final String password;


    public Packtpub(String email, String password) {
        this.email = email;
        this.password = password;
        System.out.println("Opening Webclient");
        webClient = new WebClient(BrowserVersion.CHROME);
        webClient.getOptions().setJavaScriptEnabled(false);
        webClient.getOptions().setUseInsecureSSL(true);


    }

    public boolean login()throws IOException {

            System.out.println("Accessing packtpub.com");
            HtmlPage login = webClient.getPage(defaultPage);

            System.out.println("Logging in");
            HtmlForm form = login.getHtmlElementById(loginFormID);

            form.getInputByName("email").setValueAttribute(email);
            form.getInputByName("password").setValueAttribute(password);
           return form.getInputByName("op").click().getUrl().toString().equals(defaultPage+"/index");
    }

    public boolean getFreeBook() throws IOException
    {
        HtmlPage freePage = webClient.getPage(freeBooksPage);

        System.out.println("Free book: "+freePage.querySelector(".dotd-title").getTextContent().replaceAll("\t","").replaceAll("\n",""));

       return webClient.getPage(defaultPage+freePage.querySelector(".twelve-days-claim").getAttributes().getNamedItem("href").getNodeValue())
                .getUrl().toString().equals(afterClaim);
    }

    public void closeWebClient()
    {
        webClient.close();
    }


}
