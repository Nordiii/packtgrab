package de.packethub;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.*;
import java.util.Properties;

public class Start {
    private static final Logger logger = LogManager.getLogger("PacketHub");
    @Option(name = "-c", usage = "-c = enable claiming free book")
    private boolean claim = false;

    @Option(name = "-dl", usage = "-dl <extension> download books with the extension")
    private String format = null;

    Account account = new Account(logger);

    public static void main(String[] args) {
        new Start().doStuff(args);
    }

    private void doStuff(String[] args) {
        /*
         * Check if user.properties exists
         * False = create new one
         */
        account.checkProperties();

        CmdLineParser parser = new CmdLineParser(this);
        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            logger.error("Argument couldn't be parsed");
        }
        Packtpub site = getSite(account.getProperties());

        if (claim)
            claimBookJob(site);
        if (format != null)
            downloadBook(site, format);
        site.closeWebClient();
        logger.warn("-");
    }

    private void downloadBook(Packtpub site, String format) {
        try {
            if (site.downloadBooks(format))
                logger.info("Finished downloading every available book with this format");
        } catch (IOException e) {
            logger.error("wasn't able to download books ");
            logger.catching(e);
        }
    }

    private Packtpub getSite(Properties prop) {
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
            if (site != null) {
                site.closeWebClient();
                logger.info("Closed WebClient");
            }
        }
        return site;
    }

    private void claimBookJob(Packtpub site) {

        try {
            if (site.getFreeBook())
                logger.info("Successfully claimed");
        } catch (IOException e) {
            logger.error("wasn't able to claim the free book");
            logger.catching(e);
        }

    }

}