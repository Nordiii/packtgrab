package de.packethub;

import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ExecutorService;
import java.io.*;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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

    private ExecutorService exe = Executors.newFixedThreadPool(10);

    public boolean downloadBooks(String format) throws IOException {
        if (!new File("downloads").exists())
            new File("downloads").mkdir();
        if (!new File("downloads/" + format).exists())
            new File("downloads/" + format).mkdir();

        HtmlPage ebooks = webClient.getPage("https://www.packtpub.com/account/my-ebooks");
        Iterator test = ebooks.getElementById("product-account-list").getChildElements().iterator();
        if (!test.hasNext())
            return false;

        logger.info("Start to download books with the format " + format);
        while (test.hasNext()) {
            HtmlDivision book = (HtmlDivision) test.next();
            exe.execute(new downloadThread(book, format));
        }
        exe.shutdown();

        try {
            exe.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            logger.fatal("Unexpected interrupt");
        }

        return true;
    }

    private class downloadThread extends Thread {
        HtmlDivision book;
        String format;
        public downloadThread(HtmlDivision book, String format) {
            this.book = book;
            this.format = format;
        }


        public void run() {
            if (!book.getAttribute("class").equalsIgnoreCase("product-line unseen"))
                return;
            String title = book.getAttribute("title").replaceAll("[:.]", "_");

            File f = new File("downloads/" + format + "/" + title + "." + format);
            if (f.exists())
                return;
            InputStream is;
            OutputStream os;
            try {
                Page test = webClient.getPage("https://www.packtpub.com/ebook_download/" + book.getAttribute("nid") + "/" + format);
                WebResponse response =  test.getWebResponse();
                long length = response.getContentLength();

                if (length == 0)
                    return;

                logger.info("   Download:  " + book.getAttribute("title"));

                is = response.getContentAsStream();
                os = new FileOutputStream(f);
                byte[] bytes = new byte[4096];
                int read;
                while ((read = is.read(bytes)) != -1) {
                    os.write(bytes, 0, read);
                }
                os.close();
                is.close();
            } catch (IOException e) {
                logger.error("Failed to download book");
            } finally {

            }


        }
    }


    public void closeWebClient() {
        webClient.close();
    }


}
