package de.packethub;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.Properties;
import java.util.Scanner;

public class Start {
    private static final Logger logger = LogManager.getLogger("PacketHub");

    public static void main(String[] args) {
        /*
         * Check if user.properties exists
         * False = create new one
         */
        logger.info("Checking properties file");
        if (!checkProperties()) {
            logger.info("Creating new user.properties");
            if (!createProperties())
                logger.warn("Failed to create File");
        }

        Packtpub site = getSite(getProperties());

        if(args.length == 0)
            claimBookJob(site);
        else
            downloadBook(site);
        logger.warn("-");

    }
    private static void  downloadBook(Packtpub site)
    {
        try
        {
            if (site.downloadBooks())
                logger.info("Successfully claimed");
        }catch (IOException e)
        {
            logger.error("wasn't able to download books ");
            logger.catching(e);
        }
    }

    private static Packtpub getSite(Properties prop)
    {
        Packtpub site = null;
         /*
         * Try block for de.packethub.Packtpub as it throws IOExceptions
         */
        try {

            site = new Packtpub(prop.getProperty("email"), prop.getProperty("password"), logger);

            //Try to login when failed exit with an IOException
            if (!site.login()) {
                logger.error("Wasn't able to login");
                throw new IOException();
            }

        } catch (IOException ignored) {
            if (site != null)
            {
                site.closeWebClient();
                logger.info("Closed WebClient");
            }
        }
        return site;
    }

    private static void claimBookJob(Packtpub site) {

        try
        {
            if (site.getFreeBook())
                logger.info("Successfully claimed");
        }catch (IOException e)
        {
            logger.error("wasn't able to claim the free book");
            logger.catching(e);
        }

    }

    private static boolean checkProperties() {
        return new File("user.properties").exists();
    }

    private static boolean createProperties() {
        Properties prop = new Properties();
        Scanner sc = new Scanner(System.in);
        try (OutputStream propFile = new FileOutputStream("user.properties")) {
            logger.info("Enter email:");
            prop.setProperty("email", sc.nextLine());

            logger.info("Enter password:");
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