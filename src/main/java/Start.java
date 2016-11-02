        import com.gargoylesoftware.htmlunit.BrowserVersion;
        import com.gargoylesoftware.htmlunit.WebClient;
        import com.gargoylesoftware.htmlunit.html.HtmlForm;
        import com.gargoylesoftware.htmlunit.html.HtmlPage;

        import java.io.*;
        import java.util.Properties;
        import java.util.Scanner;

/**
 * Created by Nordiii on 01.11.2016.
 */
public class Start {
    public static void main(String[] args)
    {

        if(!checkProperties())
        {
            System.out.println("Creating new user.properties");
            if(!createProperties())
                System.out.println("Failed to create File");

        }

        Properties prop = getProperties();

        try (final WebClient webClient = new WebClient(BrowserVersion.CHROME)) {
            webClient.getOptions().setJavaScriptEnabled(false);
            webClient.getOptions().setUseInsecureSSL(true);

            HtmlPage login = webClient.getPage("https://www.packtpub.com/#");

            HtmlForm form = login.getHtmlElementById("packt-user-login-form");

            form.getInputByName("email").setValueAttribute(prop.getProperty("email"));
            form.getInputByName("password").setValueAttribute(prop.getProperty("password"));
            form.getInputByName("op").click();

            HtmlPage freePage = webClient.getPage("https://www.packtpub.com/packt/offers/free-learning");

            System.out.println("Free book: "+freePage.querySelector(".dotd-title").getTextContent().replaceAll("\t","").replaceAll("\n",""));

            webClient.getPage("https://www.packtpub.com/"+freePage.querySelector(".twelve-days-claim").getAttributes().getNamedItem("href").getNodeValue());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static boolean checkProperties()
    {
        return new File("user.properties").exists();
    }

    private static boolean createProperties()
    {
        Properties prop = new Properties();
        Scanner sc = new Scanner(System.in);
        try(OutputStream propFile = new FileOutputStream("user.properties"))
        {
            System.out.println("Enter email:");
            prop.setProperty("email",sc.nextLine());

            System.out.println("Enter email:");
            prop.setProperty("password",sc.nextLine());

            prop.store(propFile,null);

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            sc.close();
            return checkProperties();
        }

    }

    private static Properties getProperties()
    {
        Properties prop = new Properties();
        try(InputStream propFile = new FileInputStream("user.properties"))
        {
            prop.load(propFile);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return prop;
    }
}