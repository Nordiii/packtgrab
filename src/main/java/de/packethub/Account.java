package de.packethub;

import java.io.*;
import java.util.Properties;
import java.util.Scanner;

import org.apache.logging.log4j.Logger;

/**
 * Created by Nordiii on 05.12.2016.
 */
public class Account {
    Logger logger;

    public Account(Logger logger) {
        this.logger = logger;
    }

    public boolean checkProperties() {
        logger.info("Checking properties file");
        if (!new File("user.properties").exists())
            return createProperties();
        return true;
    }

    private boolean createProperties() {

        logger.info("Creating new user.properties");
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

    public Properties getProperties() {
        Properties prop = new Properties();
        try (InputStream propFile = new FileInputStream("user.properties")) {
            prop.load(propFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return prop;
    }
}
