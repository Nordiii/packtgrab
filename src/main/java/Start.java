
import java.io.*;
import java.util.Properties;
import java.util.Scanner;

public class Start {
    public static void main(String[] args) {
        /*
         * Check if user.properties exists
         * False = create new one
         */
        System.out.println("Checking properties file");
        if (!checkProperties()) {
            System.out.println("Creating new user.properties");
            if (!createProperties())
                System.out.println("Failed to create File");
        }

        Properties prop = getProperties();

        Packtpub site = null;
        /*
         * Try block for Packtpub as it throws IOExceptions
         */
        try {

            site = new Packtpub(prop.getProperty("email"), prop.getProperty("password"));

            //Try to login when failed exit with an IOException
            if (!site.login()) {
                System.out.println("Wasn't able to login");
                throw new IOException();
            }
            //Try to claim the book
            if (site.getFreeBook())
                System.out.println("Successfully claimed");

        } catch (IOException ignored) {

        } finally {
            if (site != null)
                site.closeWebClient();
        }

    }


    private static boolean checkProperties() {
        return new File("user.properties").exists();
    }

    private static boolean createProperties() {
        Properties prop = new Properties();
        Scanner sc = new Scanner(System.in);
        try (OutputStream propFile = new FileOutputStream("user.properties")) {
            System.out.println("Enter email:");
            prop.setProperty("email", sc.nextLine());

            System.out.println("Enter password:");
            prop.setProperty("password", sc.nextLine());

            prop.store(propFile, null);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sc.close();
        }
        return checkProperties();
    }

    private static Properties getProperties() {
        Properties prop = new Properties();
        try (InputStream propFile = new FileInputStream("user.properties")) {
            prop.load(propFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return prop;
    }
}