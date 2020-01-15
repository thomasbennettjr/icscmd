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
    private static String _VERSION = "1.5_12";

    static {
        options.addOption("h", "help", false, "show help.");
        options.addOption("w", "wait", false, "Will block any other commands from executing until job is completed");
        options.addOption("un", "username", true, "Informatica Cloud User Name");
        options.addOption("pw", "password", true, "Informatica Cloud Password");
        options.addOption("pwe", "password", true, "Informatica Cloud Password in Environment Variable");
        options.addOption("j", "session", true, "The Informatica Cloud Job to Execute");
        options.addOption("s", "stop", false, "Stop executed previously executed Job");
        options.addOption("t", "type", true, "Supported Arguments [AVS | DMASK | DQA | DRS | DSS | MTT | PCS | Workflow | DNB_WORKFLOW]");
        options.addOption("iw", "ignorewarning", false, "Informatica Cloud Jobs in warning status will not be reported as failed");
        options.addOption("c","connection", false, "Change a Connection Password");
        options.addOption("cn", "name", true, "Connection Name");
        options.addOption("cp", "connpwd", true, "Connection Password");
        options.addOption("cf", "connfile", true, "Path to Connection Password file");
        options.addOption("csf", "connectionsearchfile", true, "Path to file with search value");
        options.addOption("ep", "encryptedpwd",false,"Connection passwords encrypted");
        options.addOption("bteq", "bteq", true, "Execute Teradata BTEQ script");
        options.addOption("v","version", false,"product version");
        options.addOption("dm", "dynamicmapping", false, "Create Mapping Configuration Task(s)");
        options.addOption("dmf", "", true, "Path to file to create Mapping Configurations");
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
            boolean hasPwd = true;
            CommandLineParser parser = new DefaultParser();
            cmd = parser.parse(options, argv);
            if (cmd.hasOption("v"))
            {
                System.out.println("Version: " + _VERSION);
                System.exit(0);
            }
            if (cmd.hasOption("h")) {
                help();
            }

            if (cmd.hasOption("un")) {
                logger.info("Using cli argument -un");
            } else {
                if (!cmd.hasOption("bteq")) {
                    logger.warn("Missing -un option");
                    help();
                }
            }

            if (cmd.hasOption("pw"))
            {
                logger.info("Using cli argument -pw");
            } else {
                hasPwd = false;
                //logger.warn("Missing -pw option");
            }

            if (cmd.hasOption("pwe"))
            {
                logger.info("Using cli argument -pwe");
            } else {
                if (!hasPwd)
                {
                    if (!cmd.hasOption("bteq")) {
                        logger.fatal("Missing -pwe | -pw option");
                        help();
                    }
                } else {
                    if (!cmd.hasOption("bteq")) {
                        logger.warn("Missing -pwe option");
                    }
                }
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
                logger.warn("Missing -t option");
            }

            if (cmd.hasOption("j")) {

                logger.info("Using cli argument -j");
            } else {
                logger.warn("Missing -j option");
                //help();
            }

            if (cmd.hasOption("dm"))
            {
                logger.info("Using cli argument -dm");
                if (!cmd.hasOption("dmf"))
                {
                    logger.error("Use of dm option requires use of dmf option");
                    System.err.print("Use of dm option requires use of dmf option");
                    help();
                }
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

            if (cmd.hasOption("iw"))
            {
                logger.info("Using cli argument -iw");
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

            if (cmd.hasOption("bteq"))
            {
                logger.info("Using cli argument -bteq");
            }

            if (cmd.hasOption("c"))
            {
                logger.info("Using cli argument -c");
                if (cmd.hasOption("cf")) {
                		if (cmd.hasOption("csf"))
                		{
                			logger.error("Cannot have both -cf and -csf");
                			System.err.println("Cannot have both -cf and -csf");
                			help();
                		}
                    logger.info("Using -cf argument to read bulk connection file");
                }
                else if (cmd.hasOption("csf"))
                {
	                	if (cmd.hasOption("cf"))
	            		{
	            			logger.error("Cannot have both -cf and -csf");
	            			System.err.println("Cannot have both -cf and -csf");
	            			help();
	            		}
	                logger.info("Using -csf argument to read bulk search connection file");
                }
                else
                {
                    if (!cmd.hasOption("cn")) {
                        logger.info("Use of -c argument requires -cn for connection name");
                        System.err.print("Use of -c argument requires -cn for connection name");
                        help();
                    }
                    if (!cmd.hasOption("cp")) {
                        logger.info("Use of -c argument requires -cp for connection password");
                        System.err.print("Use of -c argument requires -cp for connection password");
                        help();
                    }
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
