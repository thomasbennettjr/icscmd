package com.metaopsis.cli;

import org.apache.commons.cli.*;
import org.apache.log4j.Logger;

import java.util.Hashtable;

public class Cli {

    private static final Logger logger = Logger.getLogger(Cli.class);
    private static final Options options = new Options();

    static {
        options.addOption("h", "help", false, "show help.");
        options.addOption("w", "wait", false, "Will block any other commands from executing until job is completed");
        options.addOption("un", "username", true, "Informatica Cloud User Name");
        options.addOption("pw", "password", true, "Informatica Cloud Password");
        options.addOption("j", "session", true, "The Informatica Cloud Job to Execute");

    }

    public static Hashtable<String, String> parse(String[] argv) throws ParseException {
        Hashtable<String, String> cmds = new Hashtable<String, String>();

        try {
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, argv);

            if (cmd.hasOption("h")) {
                help();
            }

            if (cmd.hasOption("un")) {
                logger.info("Using cli argument -un=" + cmd.getOptionValue("un"));
                cmds.put("un", cmd.getOptionValue("in"));
            } else {
                logger.fatal("Missing in option");
                help();
            }

            if (cmd.hasOption("pw"))
            {
                logger.info("Using cli argument -pw=***********");
                cmds.put("pw", cmd.getOptionValue("pw"));
            } else {
                logger.fatal("Missing in option");
                help();
            }

            if (cmd.hasOption("j")) {

                logger.info("Using cli argument -j=" + cmd.getOptionValue("j"));
                cmds.put("j", cmd.getOptionValue("j"));
            } else {
                logger.fatal("Missing in option");
                help();
            }


            if (cmd.hasOption("w")) {
                logger.info("Using cli argument -w");
                cmds.put("w", "");
            }

        } catch (ParseException e) {
            logger.fatal("Failed to parse command line properties", e);
            help();
            throw e;
        }

        return cmds;
    }

    private static void help() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("Main", options);
    }

    private Cli() {
    } // Disabled constructor
}
