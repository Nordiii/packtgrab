package de.packethub;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.javascript.host.html.HTMLElement;
import net.sourceforge.htmlunit.cyberneko.HTMLElements;
import org.apache.commons.logging.Log;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

public class Packtpub {
    private final WebClient webClient;

    private final String defaultPage = "https://www.packtpub.com";
    private final String loginFormID = "packt-user-login-form";
    private final String freeBooksPage = "https://www.packtpub.com/packt/offers/free-learning";
    private final String afterClaim = "https://www.packtpub.com/account/my-ebooks";

    private final String email;
    private final String password;

    private final Logger logger;

    public Packtpub(String email, String password, Logger logger) {
        this.logger = logger;
        this.email = email;
        this.password = password;
        logger.info("Opening Webclient");
        webClient = new WebClient(BrowserVersion.CHROME);
        webClient.getOptions().setJavaScriptEnabled(false);
        webClient.getOptions().setUseInsecureSSL(true);


    }

    public boolean login() throws IOException {

        logger.info("Accessing packtpub.com");
        HtmlPage login = webClient.getPage(defaultPage);

        logger.info("Logging in");
        HtmlForm form = login.getHtmlElementById(loginFormID);

        form.getInputByName("email").setValueAttribute(email);
        form.getInputByName("password").setValueAttribute(password);
        return form.getInputByName("op").click().getUrl().toString().equals(defaultPage + "/index");
    }

    public boolean getFreeBook() throws IOException {
        HtmlPage freePage = webClient.getPage(freeBooksPage);

        logger.info("Free book: " + freePage.querySelector(".dotd-title").getTextContent().replaceAll("\t", "").replaceAll("\n", ""));

        return webClient.getPage(defaultPage + freePage.querySelector(".twelve-days-claim").getAttributes().getNamedItem("href").getNodeValue())
                .getUrl().toString().equals(afterClaim);
    }

    public boolean downloadBooks() throws  IOException
    {
        if(!new File("downloads").exists())
            new File("downloads").mkdir();
        HtmlPage ebooks = webClient.getPage("https://www.packtpub.com/account/my-ebooks");
        Iterator test = ebooks.getElementById("product-account-list").getChildElements().iterator();
        while (test.hasNext())
        {

            HtmlDivision book = (HtmlDivision) test.next();
            writeFile(book);
        }
        return false;
    }
    private void writeFile(HtmlDivision book) throws IOException
    {
        if(!book.getAttribute("class").equalsIgnoreCase("product-line unseen"))
            return;

        String title = book.getAttribute("title").replaceAll(":","_");

        File f = new File("downloads/"+title+".epub");
        if(f.exists())
            return;


        long length  = webClient.getPage("https://www.packtpub.com/ebook_download/"+book.getAttribute("nid")+"/epub").getWebResponse().getContentLength();

        if(length == 0)
            return;
        logger.info("Downloading:"+book.getAttribute("title"));

        InputStream is = webClient.getPage("https://www.packtpub.com/ebook_download/"+book.getAttribute("nid")+"/epub").getWebResponse().getContentAsStream();
        OutputStream os = new FileOutputStream(f);
        byte[] bytes = new byte[4096];
        int read;
        while ( (read = is.read(bytes)) !=-1)
        {
            os.write(bytes,0,read);
        }
        os.close();
        is.close();
        logger.info("Finished: "+book.getAttribute("title"));
    }

    public void closeWebClient() {
        webClient.close();
    }


}
