package com.metaopsis.cli;

import org.apache.commons.cli.*;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Hashtable;

public class Cli {

    private static final Logger logger = Logger.getLogger(Cli.class);
    private static final Options options = new Options();
    private static CommandLine cmd = null;
    private static ArrayList<String> types = new ArrayList<String>();

    static {
        options.addOption("h", "help", false, "show help.");
        options.addOption("w", "wait", false, "Will block any other commands from executing until job is completed");
        options.addOption("un", "username", true, "Informatica Cloud User Name");
        options.addOption("pw", "password", true, "Informatica Cloud Password");
        options.addOption("j", "session", true, "The Informatica Cloud Job to Execute");
        options.addOption("s", "stop", false, "Stop executed previously executed Job");
        options.addOption("t", "type", true, "Supported Arguments [AVS | DMASK | DQA | DRS | DSS | MTT | PCS | Workflow | DNB_WORKFLOW]");

        types.add("AVS");
        types.add("DMASK");
        types.add("DQA");
        types.add("DRS");
        types.add("DSS");
        types.add("MTT");
        types.add("PCS");
        types.add("Workflow");
        types.add("DNB_WORKFLOW");
    }

    public static String getCliValue(String option)
    {
        try {
            if (cmd.hasOption(option)) {
                return cmd.getOptionValue(option);
            }
            else
                return null;
        } catch(NullPointerException e)
        {
            return null;
        }
    }

    public static boolean hasCliValue(String option)
    {
        return cmd.hasOption(option);
    }

    public static void parse(String[] argv) {


        try {
            CommandLineParser parser = new DefaultParser();
            cmd = parser.parse(options, argv);

            if (cmd.hasOption("h")) {
                help();
            }

            if (cmd.hasOption("un")) {
                logger.info("Using cli argument -un");
            } else {
                logger.fatal("Missing -un option");
                help();
            }

            if (cmd.hasOption("pw"))
            {
                logger.info("Using cli argument -pw");
            } else {
                logger.fatal("Missing -pw option");
                help();
            }

            if (cmd.hasOption("t"))
            {
                logger.info("Using cli argument -t");
                if (!types.contains(cmd.getOptionValue("t")))
                {
                    logger.error("Value entered for -t option " + cmd.getOptionValue("t") + " is unknown and not supported.");
                    System.err.print("Value entered for -t option " + cmd.getOptionValue("t") + " is unknown and not supported.");
                    help();
                }
            } else {
                logger.fatal("Missing -t option");
            }

            if (cmd.hasOption("j")) {

                logger.info("Using cli argument -j");
            } else {
                logger.fatal("Missing -j option");
                help();
            }

            if (cmd.hasOption("s"))
            {
                logger.info("Using cli argument -s");
                if (cmd.hasOption("w"))
                {
                    logger.error("Use of both cli arguments -s and -w is not permitted");
                    System.err.print("Use of both cli arguments -s and -w is not permitted");
                    help();
                }
            }

            if (cmd.hasOption("w")) {
                logger.info("Using cli argument -w");
                if (cmd.hasOption("s"))
                {
                    logger.error("Use of both cli arguments -s and -w is not permitted");
                    System.err.print("Use of both cli arguments -s and -w is not permitted");
                    help();
                }
            }

        } catch (ParseException e) {
            logger.fatal("Failed to parse command line properties", e);
            help();
        }

    }

    private static void help() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("icscmd", options);
        System.exit(0);
    }

    private Cli() {
    } // Disabled constructor
}
