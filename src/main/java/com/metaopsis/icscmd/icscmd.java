package com.metaopsis.icscmd;

import com.metaopsis.cli.Cli;

import com.metaopsis.crypto.Encryptor;
import com.metaopsis.helper.ConnectionPassword;
import com.metaopsis.helper.Record;
import com.metaopsis.icsapi.helper.*;
import com.metaopsis.icsapi.helper.Error;
import com.metaopsis.icsapi.impl.InformaticaCloudException;
import com.metaopsis.icsapi.impl.InformaticaCloudImpl;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.nio.file.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * Created by tbennett on 11/5/16.
 * Update: 8/19/2017 - Change for Carbonite to add flag so ICS Jobs in a state of Warning does not return exit code of 1
 * Update: 8/19/2017 - Change for Teradata so they can update passwords via CLI in either bulk or single connection
 */
public class icscmd {
    private static final Logger logger = Logger.getLogger(icscmd.class);
    private static InformaticaCloudImpl impl = null;
    private static int _EXITCODE = 0;
    
    public static void main(String[] args)
    {
        logger.debug("Passing CLI arguement in to be parsed:: " + Arrays.toString(args));
        Cli.parse(args);
        logger.debug("CLI Arguments parsed successfully");
        Login login = null;
        User user = null;
        String encpwd = null;

        try {

            if (Cli.hasCliValue("bteq"))
            {
                logger.info("Executing BTEQ Script " + Cli.getCliValue("bteq"));
                int exitval = 0;
                Process bteq = null;
                try {
                    bteq = Runtime.getRuntime().exec(Cli.getCliValue("bteq"));
                    exitval = bteq.waitFor();
                    logger.info("WaitFor Code: " + exitval + " Exit Value: " + bteq.exitValue());
                } catch(IOException e)
                {
                    logger.error(e.getMessage());
                    if (bteq != null)
                    {
                        bteq.destroy();
                        exitval = bteq.exitValue();
                    } else {
                        bteq.exitValue();
                    }
                } catch(InterruptedException e)
                {
                    logger.error(e.getMessage());
                    if (bteq != null)
                    {
                        bteq.destroy();
                        exitval = bteq.exitValue();
                    } else {
                        bteq.exitValue();
                    }
                }
                logger.info("Exit Code: " + exitval);
                System.exit(exitval);
            }
            impl = new InformaticaCloudImpl();
            if (Cli.hasCliValue("pw")) {
                encpwd = Cli.getCliValue("pw");
            }
            else {
                encpwd = System.getenv(Cli.getCliValue("pwe"));
            }
            login = new Login(Cli.getCliValue("un"), Encryptor.decrypt(encpwd));
            
            logger.debug(login.toString());
            _EXITCODE = 2;
            user = impl.login(login);
            _EXITCODE = 0;
            /*if (impl.hasError()) {
                logger.error("Error from call to ICS Login End Point");
                handleError(impl.getError());
                _EXITCODE = 2;
            }*/
            logger.debug(user.toString());

            // Connection Password change
            if (Cli.hasCliValue("c"))
            {
                if (Cli.hasCliValue("cf"))
                {
                    // get info from file for bulk updates.
                    String cf = Cli.getCliValue("cf");
                    if (cf == null) {
                    		_EXITCODE = 4;
                        throw (new InformaticaCloudException("No value for CLI parameter -cf"));
                        
                    }

                    Path path = Paths.get(cf);

                    if (!Files.exists(path))
                    {
                    		_EXITCODE = 4;
                        throw(new InformaticaCloudException("File and/or Path specified in -cf parameter does not exist: " + cf));
                    }

                    ConnectionPassword cp = new ConnectionPassword(path);
                    Record[] records = cp.getRecords();

                    if (records == null)
                    {
                    		_EXITCODE = 4;
                        throw (new InformaticaCloudException("No records in file for Connections :" + cf));
                    }


                    for (Record record : records)
                    {
                        logger.trace(record.toString());

                        logger.info("Processing for connection " + record.getConnectionName());
                        System.out.println("Processing update for connection " + record.getConnectionName());

                        Connection connection = lookupConnection(user, record.getConnectionName());
                        updateConnection(connection, record, user);
                    }

                } else if (Cli.hasCliValue("csf")) {
                	// get info from file for bulk updates.
                    String csf = Cli.getCliValue("csf");
                    if (csf == null) {
                    		_EXITCODE = 4;
                        throw (new InformaticaCloudException("No value for CLI parameter -csf"));
                    }

                    Path path = Paths.get(csf);

                    if (!Files.exists(path))
                    {
                    		_EXITCODE = 4;
                        throw(new InformaticaCloudException("File and/or Path specified in -csf parameter does not exist: " + csf));
                    }

                    ConnectionPassword cp = new ConnectionPassword(path);
                    Record[] records = cp.getRecords();

                    if (records == null)
                    {
                    		_EXITCODE = 4;
                        throw (new InformaticaCloudException("No records in file for Connections :" + csf));
                    }
                    
                    Connection[] connections = impl.getAllConnections(user);
                    for (Record record : records)
                    {
                    		boolean hasMatch = false;
                    		for (Connection connection : connections)
                    		{
                    			if (connection.getName().contains(record.getConnectionName()))
                    			{
                    				updateConnection(connection,record,user);
                    				hasMatch = true;
                    			}
                    		}
                    		if (!hasMatch) {
                    			System.out.println("No match found for search of " + record.getConnectionName());
                    			_EXITCODE = 1;
                    		}
                    }
                    
                    
                    
                } else {
               
                		String cn = Cli.getCliValue("cn");
                		if (cn == null)
                		{
                			_EXITCODE = 4;
                			throw (new InformaticaCloudException("Connection Name is null. Please provide a connection name."));
                		}
                		String cp = Cli.getCliValue("cp");
                		if (cp == null)
                		{
                			_EXITCODE = 4;
                			throw (new InformaticaCloudException("Connection Password is null. Please provide a connection password."));
                		}
                		
                		logger.info("Processing for connection " + cn);
                     System.out.println("Processing update for connection " + cn);
                     
                     Connection connection = lookupConnection(user, cn);
                     updateConnection(connection, new Record(cn,cp), user);
                                          
                }
            }
            else {
                Job job = new Job();
                job.setTaskType(stripNonValidCharacters(Cli.getCliValue("t")));
                job.setTaskName(stripNonValidCharacters(Cli.getCliValue("j")));

                logger.debug(job.toString());
                Job response = null;

                if (Cli.hasCliValue("s")) {
                		_EXITCODE = 3;
                    response = impl.job(user, job, false);
                    _EXITCODE = 0;
                    
                } else {
                		_EXITCODE = 3;
                    response = impl.job(user, job, true);
                    _EXITCODE = 0;
                }

                logger.trace("Response " + response.toString());

                if (Cli.hasCliValue("w")) {
                    Thread.sleep(500);
                    ActivityLog[] logs = null;
                    int counter = 0;
                    boolean hasRunId;
                    do {
                        hasRunId = false;
	                    	try
	                    	{
	                    		if (counter == 25)
	                    		{
	                    			logger.debug("25 minutes on token. Refreshing token");
	                    			impl.logout(user);
	                    			user = impl.login(login);
	                    			logger.debug("Token refreshed successful!");
	                    			counter = 0;
	                    		}
	                        //logs = impl.activityLog(user, response, -1, 1);
                                ActivityMonitor[] monitors = impl.activityMonitor(user, false);
                                if (monitors != null) {
                                    for (ActivityMonitor monitor : monitors) {
                                        if (monitor.getRunId() == response.getRunId())
                                            hasRunId = true;
                                    }
                                }

	                        //if (impl.hasError()) {
	                        //		_EXITCODE = 3;
	                        //    logger.error("Error from call to ICS ActivityLog End Point");
	                        //    handleError(impl.getError());
	                        //} else {
	                            if (hasRunId) {
	                                logger.trace("Sleeping before next call back to Activity Monitor");
	                                //impl.logout(user);
	                                Thread.sleep(500);
	                                counter++;
	                                logger.debug("Counter :: " + counter);
	                                logger.trace("Waking Up");
	                                ValidSessionIdResponse validateSessionResp = impl.validateSessionId(new ValidSessionIdRequest(user.getName(), user.getIcSessionId()), user);
	                                if (!validateSessionResp.isValidToken())
	                                {
	                                		logger.debug("Token :: " + user.getIcSessionId() + " is invalid. Attempting to get new Token");
	                                		impl.logout(user);
	                                		user = impl.login(new Login(Cli.getCliValue("un"), Encryptor.decrypt(encpwd)));
	                                		logger.debug("New Token :: " + user.getIcSessionId() + " received");
	                                }
	                                
	                            }
	                       // }
	
	                    	} catch(InformaticaCloudException e)
	                    	{
	                    		//reset user
	                    		user = null;
                                //if (user == null) {
	                    		_EXITCODE = 3;
	                    			logger.error(e.getMessage());
	                    			logger.trace("Sleeping for 1 minute. Then reattempt login");
                                	Thread.sleep(60000);
                                	
                                    for (int i = 0; i <= 3; i++) {
                                    	try {
                                        logger.warn("Attempt " + i + " to get user");
                                        user = impl.login(new Login(Cli.getCliValue("un"), Encryptor.decrypt(encpwd)));
                                        /*if (impl.hasError())
                                        {
                                        		_EXITCODE = 3;
                                            handleError(impl.getError());
                                        }*/
                                        if (user != null) break;
                                    	} catch(InformaticaCloudException ii) {
                                    		logger.warn("Attempt failed. Sleep 1 minute and try again.");
                                    		Thread.sleep(60000);
                                    	}
                                    }
                                	
                                    if (user == null) {
                                        logger.fatal("3 Attempts failed to get user.");
                                        handleError(e.getMessage());
                                    }
                                //} 
	                    	}
                    } while (hasRunId);
                    logs = impl.activityLog(user, response, -1, 1);
                /*
                * job.state = State of Job at completion
                * 3 = Failed
                * 2 = Warning
                * 1 = Success
                 */
                    if (logs.length > 1) {
                    		_EXITCODE = 3;
                        logger.fatal("Cannot have more than one response back. Based on RunId Query");
                        logger.fatal(response.toString());
                        logger.fatal(Arrays.toString(logs));
                        System.out.print("icscmd - Error. Too many logs returned for task. Check Logs");
                        System.exit(_EXITCODE);
                    } else {
                        switch (logs[0].getState()) {
                            case 1:
                            		_EXITCODE = 0;
                                System.out.println("icscmd - Success Job::" + logs[0].getObjectName());
                                logger.info("icscmd - Success Job::" + logs[0].getObjectName());
                                break;
                            case 2:
                            		if (!Cli.hasCliValue("iw"))
                            		{
                            			System.out.println("icscmd - Warning Job::" + logs[0].getObjectName() + " with taskId::" + logs[0].getObjectId() + " Error Message::" + logs[0].getEntries()[0].getErrorMsg());
                            		}
                                logger.warn("icscmd - Warning Job::" + logs[0].getObjectName() + " with taskId::" + logs[0].getObjectId() + " Error Message::" + logs[0].getEntries()[0].getErrorMsg());
                                break;
                            case 3:
                                System.out.println("icscmd - Failed Job::" + logs[0].getObjectName() + " with taskId::" + logs[0].getObjectId() + " Error Message::" + logs[0].getErrorMsg());
                                logger.error("icscmd - Failed Job::" + logs[0].getObjectName() + " with taskId::" + logs[0].getObjectId() + " Error Message::" + logs[0].getErrorMsg());
                                break;
                            default:
                                logger.warn(logs[0].toString());
                                System.out.println("icscmd - Unknown State");
                                break;
                        }


                        if (logs[0].getState() == 1) {
                            System.exit(_EXITCODE);
                        } else {
                        		if (logs[0].getState() == 2 && Cli.hasCliValue("iw"))
                        		{
                        			System.exit(_EXITCODE);
                        		} else {
                        			_EXITCODE = 5;
                        			logger.fatal("Job Execution not Successful");
                        			System.exit(_EXITCODE);
                        		}
                        }
                    }

                } else {
                    if (Cli.hasCliValue("s"))
                        System.out.println("Job:: " + job.getTaskName() + " in Informatica Cloud Org:: " + user.getOrgId() + " stopping");
                    else
                        System.out.println("icscmd - Scuccessfully started Job: " + job.getTaskName());

                }
            }
            if (user != null)
            {	
            		_EXITCODE = 3;
            		impl.logout(user);
            		_EXITCODE = 0;
	            logger.debug("Successfully Logged out ICS Rest API User");
	            logger.debug("EXITCODE: " + _EXITCODE);
	            System.exit(_EXITCODE);
            }
        } catch(InformaticaCloudException e) {
            logger.fatal("InformaticaCloudException::" + e.getMessage());
            for (StackTraceElement stack : e.getStackTrace())
                logger.fatal(stack.toString());
            	_EXITCODE = 3;
            	logger.debug("EXITCODE: " + _EXITCODE);
            System.exit(_EXITCODE);
        } catch (InvalidAlgorithmParameterException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | UnsupportedEncodingException e) 
        {
        		logger.fatal(e.getClass().getName() +"::"+e.getMessage());
        		System.err.println("Invalid Password");
        	    _EXITCODE = 2;
        	    logger.debug("EXITCODE: " + _EXITCODE);
        	    System.exit(_EXITCODE);
        }catch(Exception e)
        {
            for (StackTraceElement stack : e.getStackTrace())
                logger.fatal(stack.toString());
            logger.fatal(e.getMessage());
            _EXITCODE = 4;
            logger.debug("EXITCODE: " + _EXITCODE);
            System.exit(_EXITCODE);
        }
        logger.debug("EXITCODE: " + _EXITCODE);
        System.exit(_EXITCODE);

    }
    
    private static void updateConnection(Connection connection, Record record, User user) throws InvalidAlgorithmParameterException , NoSuchAlgorithmException , NoSuchPaddingException , InvalidKeyException , IllegalBlockSizeException , BadPaddingException , UnsupportedEncodingException
    {
    		String type;
    		String password;
        if (connection.getType().equals("TOOLKIT"))
        {
            type = connection.getInstanceName();
        } else {
            type = connection.getType();
        }
        if (Cli.hasCliValue("ep"))
        {
        		password = Encryptor.decrypt(record.getPassword());
        } else {
        		password = record.getPassword();
        }
        try {

            if (type.equals("Teradata")) {
                logger.info("Teradata Connection " + connection.getName() + " found. Preparing to update");
                Teradata td = (Teradata) impl.getConnection(user, connection.getId());
                td.getConnParams().setPassword(password);
                impl.updateTeradataConnection(user, td);
                logger.info("Teradata Connection " + connection.getName() + " updated.");
            }

            if (type.equals("Oracle")) {
                logger.info("Oracle Connection " + connection.getName() + " found. Preparing to update");
                OracleConnection ora = (OracleConnection) impl.getConnection(user, connection.getId());
                ora.setPassword(password);
                impl.updateOracleConnection(user, ora);
                logger.info("Oracle Connection " + connection.getName() + " updated.");
            }

            if (type.contains("SqlServer")) {
                logger.info("SqlServer Connection " + connection.getName() + " found. Preparing to update");
                SqlServerConnection sql = (SqlServerConnection) impl.getConnection(user, connection.getId());
                sql.setPassword(password);
                impl.updateSqlServerConnection(user, sql);
                logger.info("SqlServer Connection " + connection.getName() + " updated.");
            }
            
            if (type.equals("ODBC"))
            {
            		logger.info("ODBC Connection " + connection.getName() + " found. Preparing to update");
                ODBCConnection odbc = (ODBCConnection) impl.getConnection(user, connection.getId());
                odbc.setPassword(password);
                impl.updateODBCConnection(user, odbc);
                logger.info("ODBC Connection " + connection.getName() + " updated.");
            }
            System.out.println("Update for connection " + connection.getName() + " successful");
        } catch(InformaticaCloudException e)
        {
        		_EXITCODE = 3;
            logger.error(e.getMessage());
            System.err.println("Error processing Connection Update. Check Logs");
        }
    }
    
    private static void handleError(String error)
    {
        logger.fatal(error);
        System.out.println(error);
        logger.debug("EXITCODE: " + _EXITCODE);
        //System.out.println("icscmd - Error\nStatus Code: "+ error.getStatusCode() + "\nError Code: " + error.getCode() + "\nError Message: " + error.getDescription());
        System.exit(_EXITCODE);
    }

    private static Connection lookupConnection(User user, String name) throws InformaticaCloudException
    {
        logger.trace("Looking up Informatica Cloud Connection by Name : " + name);
        Connection connection = impl.getConnectionByName(user, name);

        if (connection == null)
        {
        		_EXITCODE = 1;
            logger.error("lookupConnection unable to find Connection for name : " + name);
            throw (new InformaticaCloudException("lookupConnection unable to find Connection for name : " + name));
        }

        return connection;
    }
    
    private static String stripNonValidCharacters(String in) {
        StringBuffer out = new StringBuffer(); // Used to hold the output.
        char current; // Used to reference the current character.

        if (in == null || ("".equals(in))) return ""; // vacancy test.
        for (int i = 0; i < in.length(); i++) {
            current = in.charAt(i); // NOTE: No IndexOutOfBoundsException caught here; it should not happen.
            if ((current == 0x9) ||
                    (current == 0xA) ||
                    (current == 0xD) ||
                    ((current >= 0x20) && (current <= 0xD7FF)) ||
                    ((current >= 0xE000) && (current <= 0xFFFD)) ||
                    ((current >= 0x10000) && (current <= 0x10FFFF)))
                out.append(current);
        }
        return out.toString();
    }
}
