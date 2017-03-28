package com.metaopsis.icscmd;

import com.metaopsis.cli.Cli;

import com.metaopsis.icsapi.helper.*;
import com.metaopsis.icsapi.helper.Error;
import com.metaopsis.icsapi.impl.InformaticaCloudException;
import com.metaopsis.icsapi.impl.InformaticaCloudImpl;
import org.apache.log4j.Logger;

import java.util.Arrays;

/**
 * Created by tbennett on 11/5/16.
 */
public class icscmd {
    private static final Logger logger = Logger.getLogger(icscmd.class);
    public static void main(String[] args)
    {
        logger.debug("Passing CLI arguement in to be parsed:: " + Arrays.toString(args));
        Cli.parse(args);
        logger.debug("CLI Arguments parsed successfully");
        Login login = null;
        User user = null;
        String encpwd = null;
        try {
            InformaticaCloudImpl impl = new InformaticaCloudImpl();
            if (Cli.hasCliValue("pw")) {
                login = new Login(Cli.getCliValue("un"), Cli.getCliValue("pw"));
                encpwd = Cli.getCliValue("pw");
            }
            else {
                login = new Login(Cli.getCliValue("un"), System.getenv(Cli.getCliValue("pwe")));
                encpwd = System.getenv(Cli.getCliValue("pwe"));
            }

            logger.debug(login.toString());
            user = impl.login(login);
            if (impl.hasError()) {
                logger.error("Error from call to ICS Login End Point");
                handleError(impl.getError());
            }
            logger.debug(user.toString());

            Job job = new Job();
            job.setTaskType(Cli.getCliValue("t"));
            job.setTaskName(Cli.getCliValue("j"));

            logger.debug(job.toString());
            Job response = null;

            if (Cli.hasCliValue("s")) {
                response = impl.job(user, job, false);
                if (impl.hasError())
                {
                    logger.error("Error from call to ICS Job Stop End Point");
                    handleError(impl.getError());
                }
            }
            else
                response = impl.job(user, job, true);

            if (impl.hasError()) {
                logger.error("Error from call to ICS Job End Point");
                handleError(impl.getError());
            }

            if (Cli.hasCliValue("w"))
            {
                ActivityLog[] logs = null;
                do {
                    logs = impl.activityLog(user, response, -1, 1);
                    if (impl.hasError())
                    {
                        logger.error("Error from call to ICS ActivityLog End Point");
                        handleError(impl.getError());
                    } else {
                        if (logs == null) {
                            logger.trace("Sleeping before next call back to Activity Monitor");
                            Thread.sleep(5000);
                            logger.trace("Waking Up");
                        }
                    }
                    impl.logout(user);
                    user = impl.login(new Login(Cli.getCliValue("un"), encpwd));

                } while(logs == null);
                /*
                * job.state = State of Job at completion
                * 3 = Failed
                * 2 = Warning
                * 1 = Success
                 */
                if (logs.length > 1)
                {
                    logger.fatal("Cannot have more than one response back. Based on RunId Query");
                    logger.fatal(response.toString());
                    logger.fatal(Arrays.toString(logs));
                    System.out.print("icscmd - Error. Too many logs returned for task. Check Logs");
                    System.exit(1);
                } else {
                    switch(logs[0].getState())
                    {
                        case 1:
                            System.out.println("icscmd - Success Job::" + logs[0].getObjectName());
                            break;
                        case 2:
                            System.out.println("icscmd - Warning Job::" + logs[0].getObjectName() +" with taskId::" + logs[0].getObjectId() + " Error Message::" + logs[0].getEntries()[0].getErrorMsg());
                            break;
                        case 3:
                            System.out.println("icscmd - Failed Job::" + logs[0].getObjectName() +" with taskId::" + logs[0].getObjectId() + " Error Message::" + logs[0].getErrorMsg());
                            break;
                        default:
                            logger.warn(logs[0].toString());
                            System.out.println("icscmd - Unknown State");
                            break;
                    }


                    if (logs[0].getState() == 1)
                        System.exit(0);
                    else
                        System.exit(1);
                }

            } else {
                if (Cli.hasCliValue("s"))
                    System.out.println("Job:: " + job.getTaskName() +" in Informatica Cloud Org:: " + user.getOrgId() + " stopping");
                else
                    System.out.println("icscmd - Scuccessfully started Job: " + job.getTaskName());

            }

            if (!impl.logout(user))
            {
                logger.error("Error while logging out User from ICS Rest API");
                if (impl.hasError())
                {
                    handleError(impl.getError());
                    System.exit(1);
                }
            } else {
                logger.debug("Successfully Loged out ICS Rest API User");
                logger.trace(user.toString());
                System.exit(0);

            }
        } catch(InformaticaCloudException ice) {
            logger.fatal("InformaticaCloudException::" + ice.getMessage());
            System.exit(1);
        } catch(Exception e)
        {
            logger.fatal(e.getMessage());
            System.exit(1);
        }

    }

    private static void handleError(Error error)
    {
        logger.fatal(error.toString());
        System.out.println("icscmd - Error\nStatus Code: "+ error.getStatusCode() + "\nError Code: " + error.getCode() + "\nError Message: " + error.getDescription());
        System.exit(1);
    }
}
