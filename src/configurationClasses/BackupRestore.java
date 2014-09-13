package configurationClasses;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Properties;
import java.util.regex.Pattern;

import databaseConnect.DatabaseConnect;
import expectForJava.Expect;
import telnetConnect.TelnetConnect;


import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;


import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;


/**
 * Contains properties common to Backup and Restore classes.<br>
 * There is a file <b>Automation/settings/defaultSettings.txt</b> 
 * with default properties. This can be used to 
 * manipulate default host and<br> 
 * other default settings<br>
 * */
public abstract class BackupRestore {
	enum RouterType{
		CISCO,JUNIPER
	};
	enum ConnectionType{
		TELNET,SSH
	};
	enum BackupServerType{
		scp,tftp
	};
	private final static String DEFAULT_SETTINGS_FILE_LOCATION="settings/defaultSettings.properties";
	private final static String COMMANDS_FILE_LOCATION_FILE="settings/commandFileLocationFile.properties";
	private final static File defaultSettingsFile=new File(DEFAULT_SETTINGS_FILE_LOCATION);
	private final static File commandFileLocs=new File(COMMANDS_FILE_LOCATION_FILE);
	private static boolean FETCHING_DATA_VIA_DATABASE=true;
	private static Date date=new Date(System.currentTimeMillis());
	private static SimpleDateFormat dateFormat=new SimpleDateFormat("dd.MM.yyyy-hh:mm:ss-zzz");
	private final static Properties properties=new Properties();
	private DatabaseConnect dbConnect=new DatabaseConnect();
	private String backupFilename=dateFormat.format(date);
	protected int TIME_OUT=1;
	static Logger log = Logger.getLogger(BackupRestore.class.getName());
	/**
	 * Used to decide the type of connection telnet or SSH*/
	private static ConnectionType connectionType=null;
	/**
	 * Used for connection via telnet*/
	private TelnetConnect telnetClient=null;
	/**
	 * Used by Expect tool to get the output or input*/
	protected Channel channel=null;
	/**
	 * Used for expect tool*/
	protected Expect expect=null;
	/**
	 * Used to establish connection to the device. Should not be altered directly and so doesn't have getter and setter<br>*/
	private Session session=null;
	/**
	 * Used to establish connection to the device. Should not be altered directly and so doesn't have getter and setter<br>*/
	JSch jsch = new JSch();
	/**
	 * It contains name of the backup server.<br>
	 * */
   private String backupServerName;
   /**
    * It contains backup servr's IP address<br>
    * */
   private String backupServerIP;
   /**
    * It stores the password for the backup server<br>*/
   protected String backupServerPassword;
   /**
    * It stores the folder location on the backup server where the files will be copied<br>*/
   private String backupServerFolderLocation;
   /**
    * It stores the type of backup server scp or tftp*/
   private BackupServerType backupServerType;
   /**
    * It has the IP of the device for which backup is to made*/
   private String deviceIP;
   /**
    * Used to store the device user name*/
   private String deviceName;
   /**
    * Used to store the device type for which backup is to be made. Its default value is stored in <b>defaultSettings.properties</b> file<br>*/
   private RouterType deviceType;
   /**
    * Stores the device password for which backup is to be made<br>*/
   protected String devicePassword;
   /**
    * Stores the enable password of the device<br>*/
   protected String deviceEnPassword;
   /**
    * Private constructor to carry out repetitive work*/
   private void repetitiveWork(){
	   PropertyConfigurator.configure("log4j.properties");
	   log = Logger.getLogger(BackupRestore.class.getName());
   }
   /**
    * Initializes with the default settings stored in the file <b>defaultSettings.properties</b><br>
    * Only device IP is passed to it, rest all the settings are default.<br><b>Automation/settings/defaultSettings.properties</b><br>
    * Default settings can be modified from the above mentioned file.
 * @throws FileNotFoundException */
   public BackupRestore(String ip){
	   backupFilename=dateFormat.format(date);
	   this.repetitiveWork();
	   log.info("Constructor of "+log.getName()+ " with only 1 parameter was called\n");
	   deviceIP=ip;
	   log.info("deviceIp set to "+deviceIP+"\n");
	   log.info("Trying to open "+DEFAULT_SETTINGS_FILE_LOCATION+" file ...\n");
	   try{
		   properties.load(new FileInputStream(defaultSettingsFile));
		   log.info("File "+DEFAULT_SETTINGS_FILE_LOCATION+" opened successfully :)\n");
		   /*Initializing parameter from the file defaultSettings.properties*/
		   log.info("Initializing parameter from the file defaultSettings.properties\n");
		   FETCHING_DATA_VIA_DATABASE=Boolean.parseBoolean(properties.getProperty("viaDatabase").toLowerCase());
		   if(FETCHING_DATA_VIA_DATABASE){
		     log.info("Method of fetching data is via database");
		     this.connectToDb();
		     this.dbConnector();
		   }
		   else{
			  log.info("Method of fetching data is via files");
			  if(properties.getProperty("connectionType").toUpperCase().equals("TELNET"))
		        connectionType=ConnectionType.TELNET;
			  else
				connectionType=ConnectionType.SSH;
		      log.info("Setting the default connection type as "+connectionType);
		      backupServerName=properties.getProperty("backupServerName");
		      log.info("backupServerName set to its default value "+backupServerName+"\n");
		      backupServerIP=properties.getProperty("backupServerIP");
		      log.info("backupServerIP set to its default value "+backupServerIP+"\n");
		      backupServerPassword=properties.getProperty("backupServerPassword");
		      log.info("backupServerPassword set to its default value "+"******"+"\n");
		      backupServerFolderLocation=properties.getProperty("backupServerFolderLocation");
		      log.info("backupServerFolderLoaction set to its default value "+backupServerFolderLocation+"\n");
		      if(properties.getProperty("backupServerType").toLowerCase().equals("scp"))
		         backupServerType=BackupServerType.scp;
		      else
		    	 backupServerType=BackupServerType.tftp;
		      log.info("backupServerType set to its default value "+backupServerType+"\n");
		      if(properties.getProperty("deviceType").toUpperCase().equals("CISCO"))
		         deviceType=RouterType.CISCO;
		      else
		    	 deviceType=RouterType.JUNIPER;
		      log.info("deviceType set to its default value "+deviceType+"\n");
		      deviceName=properties.getProperty("deviceUsername");
		      log.info("INFO:"+dateFormat.format(date)+"] "+"device user name set to its default value "+deviceName+"\n");
		      devicePassword=properties.getProperty("devicePassword"); 
		      log.info("devicePassword set to its default value "+"******"+"\n");
		      deviceEnPassword=properties.getProperty("devieEnablePassword");
		      log.info("deviceEnablePassword set to its default value "+"******"+"\n");
		   }
	   }catch(Exception e){
		  log.error("File "+DEFAULT_SETTINGS_FILE_LOCATION+" was not found!!\n");
	   }
   }
   /**
    * Initializes with the default settings stored in the file <b>defaultSettings.properties</b><br>
    * Device IP,device user name,device password,device enable password and device type are passed to it, rest all the settings are default.<br><b>Automation/settings/defaultSettings.properties</b><br>
    * Default settings can be modified from the above mentioned file.
 * @throws FileNotFoundException */
   public BackupRestore(String ip,String argUsername,String argPassword,String argEnPassword,String argDeviceType){
	   this.repetitiveWork();
	   log.info("Constructor of "+log.getName()+ " with 4 parameter was called\n");
	   deviceIP=ip;
	   log.info("deviceIp set to "+deviceIP+"\n");
	   if(argDeviceType.toUpperCase().equals("CISCO"))
	         deviceType=RouterType.CISCO;
	      else
	    	 deviceType=RouterType.JUNIPER;
	   log.info("deviceType set to "+deviceType+"\n");
	   deviceName=argUsername;
	   log.info("deviceName set to "+deviceName+"\n");
	   devicePassword=argPassword;
	   log.info("devicePassword set to "+"******\n");
	   deviceEnPassword=argEnPassword;
	   log.info("deviceEnablePassword set to"+"******\n");
	   try{
		   properties.load(new FileInputStream(defaultSettingsFile));
		   log.info("File "+DEFAULT_SETTINGS_FILE_LOCATION+" opened successfully :)\n");
		   /*Initializing parameter from the file defaultSettings.properties*/
		   log.info("Initializing parameter from the file defaultSettings.properties\n");
		   FETCHING_DATA_VIA_DATABASE=Boolean.parseBoolean(properties.getProperty("viaDatabase").toLowerCase());
		   if(FETCHING_DATA_VIA_DATABASE){
		     log.info("Method of fetching data is via database");
		     this.connectToDb();
		     log.info("Insering the new device data");
		     this.dbConnect.insertInto(properties.getProperty("deviceDetailsTable"),deviceIP,deviceName,devicePassword,deviceEnPassword,deviceType);
		     log.info("Successfully inserted the data");
		     this.dbConnector();
		   }
		   else{
		     log.info("Method of fetching data is via files");
		     if(properties.getProperty("connectionType").toUpperCase().equals("TELNET"))
			        connectionType=ConnectionType.TELNET;
			 else
					connectionType=ConnectionType.SSH;
		     log.info("Setting the default connection type as "+connectionType);
		     backupServerName=properties.getProperty("backupServerName");
		     log.info("backupServerName set to its default value "+backupServerName+"\n");
		     backupServerIP=properties.getProperty("backupServerIP");
		     log.info("backupServerIP set to its default value "+backupServerIP+"\n");
		     backupServerPassword=properties.getProperty("backupServerPassword");
		     log.info("backupServerPassword set to its default value "+"******"+"\n");
		     backupServerFolderLocation=properties.getProperty("backupServerFolderLocation");
		     log.info("backupServerFolderLoaction set to its default value "+backupServerFolderLocation+"\n");
		     if(properties.getProperty("backupServerType").toLowerCase().equals("scp"))
		         backupServerType=BackupServerType.scp;
		      else
		    	 backupServerType=BackupServerType.tftp;
		     log.info("backupServerType set to its default value "+backupServerType+"\n");
		   }
	   }catch(Exception e){
		  log.error("File "+DEFAULT_SETTINGS_FILE_LOCATION+" was not found!!\n");
	   }
   }
   /**
    * Returns the connection type telnet or SSH*/
   public ConnectionType getConnectionType(){
	   return connectionType; 
   }
   /**
    * Returns the name of the backup server(the parameter <b>backupServerName</b>)*/
   public String getBackupServerName(){
	   return backupServerName;
   }
   /**
    * Returns the IP of the backup server.*/
   public String getBackupServerIp(){
	   return backupServerIP;
   }
   /**
    * Used to get the folder location where the copies will be made on the backup server*/
   public String getBackupServerFolderLocation(){
	   return backupServerFolderLocation;
   }
   /**
    * Used to get backup server type*/
   public BackupServerType getBackupServerType(){
	   return backupServerType;
   }
   /**
    * Used to get the IP of the device for which backup is being made*/
   public String getDeviceIp(){
	   return deviceIP;
   }
   /**
    * Used to get backup file name*/
   public String getBackupFilename(){
	   return backupFilename;
   }
   /**
    * Used to get the device user name whose backup is being created<br>*/
   public String getDeviceName(){
	   return deviceName;
   }
   /**
    * Used to get the device type<br>*/
   public RouterType getDeviceType(){
	   return deviceType;
   }
   /**
    * Used to set the connection type telnet or SSH<br>*/
   public void setConnectiontype(String argConnectionType){
	   if(argConnectionType.toUpperCase().equals("TELNET"))
	        connectionType=ConnectionType.TELNET;
		  else
			connectionType=ConnectionType.SSH;
   }
   /**
    * Used to set device type<br>*/
   public void setDeviceType(String argDeviceType){
	   argDeviceType=argDeviceType.toUpperCase();
	   if(argDeviceType.equals("CISCO"))
	     deviceType=RouterType.CISCO;
	   else
		 deviceType=RouterType.JUNIPER;
   }
   /**
    * Sets the parameter <b>backupServerName</b> (i.e. the name of the backup server)*/
   public void setBackupServerName(String argName){
	   backupServerName=argName;
   }
   /**
    * Used to set backup server folder location where files will be saved*/
   public void setBackupServerFolderLocation(String argBackupServerFolderLoaction){
	   backupServerFolderLocation=argBackupServerFolderLoaction;
   }
   /**
    * Sets the parameter <b>backupServerIP</b> (i.e the IP of the backup server)*/
   public void setBackupServerIp(String argIp){
	   backupServerIP=argIp;
   }
   /**
    * Used to set backup server type*/
   public void setBackupServerType(String argBackupServerType){
	   if(argBackupServerType.toLowerCase().equals("scp"))
	         backupServerType=BackupServerType.scp;
	      else
	    	 backupServerType=BackupServerType.tftp;
   }
   /**
    * Sets the parameter <b>deviceIP</b> (i.e. the IP of the device whose backup is to be made)*/
   public void setDeviceIP(String argIp){
	   deviceIP=argIp;
   }
   /**
    * Sets the login password for the device.<br><b><blockquote>#NOTE:</blockquote></b><b>There is no getter for devicePassword</b> <br>*/
   public void setDevicePassword(String argPassword) {
	devicePassword = argPassword;
   }
   /**
    * Sets the enable password of the device whose backup is to be made.<br><br><b><blockquote>#NOTE:</blockquote></b><b>There is no getter for deviceEnPassword</b> <br>*/
   public void setDeviceEnPassword(String argEnPassword) {
	 deviceEnPassword = argEnPassword;
   }
   /**
    * Sets the device user name*/
   public void setDeviceName(String argDeviceName) {
	  deviceName = argDeviceName;
    }
   /**
    * Sets backup server password<br>
    * <b><blockquote>#NOTE:</blockquote>There is no getter method for backupServerPassword</b>*/
   public void setBackupServerPassword(String argPassword){
	   backupServerPassword=argPassword;
   }
   /**
    * Used to set backup file name*/
   public void setBackupFilename(String argBackupFilename){
	   this.backupFilename=argBackupFilename;
   }
   /**
    * Allows connection through telnet
 * @throws Exception */
   public void connectTelnet() throws Exception{
	   log.info("trying to connect to device "+deviceIP+" via telnet ... \n");
	   try{
		   telnetClient=new TelnetConnect(this.deviceIP,this.deviceName,this.devicePassword);
		   if(telnetClient.isConnected())
		   log.info("Successfully connected via telnet");
	   }catch(Exception e){
		   log.info("Unable to connect via telnet");
		   e.printStackTrace();
		   throw new Exception("Unable to connect via telnet");
	   }
   }
   /**
    * Disconnects the device through telnet
 * @throws Exception */
   public void disconnectTelnet() throws Exception{
	   log.info("trying to disconnect to device "+deviceIP+" ... \n");
	   try {
		telnetClient.disconnect();
		log.info("Successfully disconnected from "+deviceIP+" via telnet");
		if(telnetClient.isConnected())
		log.info("Still connected\n");
		if(FETCHING_DATA_VIA_DATABASE){
			dbConnect.closeConnection();
			log.info("Successfully closed the database. ");
		}
	} catch (IOException e) {
		log.error("Unable to disconnect to "+deviceIP+" via telnet");
		throw new Exception("Unable to disconnect!");
	}
   }
   /**
    * Allows connection through SSH to the device whose IP is present in the field deviceIP.
    * @throws Exception*/
   public void connectSSH() throws Exception{
	   log.info("Trying to connect to the device "+deviceIP+" via SSH ....\n");
	   try{
		    session = jsch.getSession(deviceName, deviceIP);
		    session.setPassword(devicePassword);
		    session.setConfig("StrictHostKeyChecking", "no");
		    session.connect(60 * 1000);    
		    if(session.isConnected())
		    	log.info("Successfully connected to device "+deviceIP+"\n");
		    else{
		    	log.error("Unable to connect to device "+deviceIP+" possibly because of wrong passowrd or username\n");
		    	throw new Exception("UnableToConnectToTheDeviceBecauseOfInvalidUsernameOrPassword");
		    }
		    
	   }catch(Exception e){
		   log.error("Unable to connect to the device "+deviceIP+"\n");
		   throw new Exception("UnableToConnectToTheDevice");
	   }
   }
   /**
    * Disconnects the device which was already connected.
    * <b><blockquote>#NOTE:</blockquote> Should be called only after connectSSH() is called and the field <b>session</b> gets initialized</b>*/
   public void disconnectSSH(){
	   log.info("Trying to disconnect to device "+deviceIP+" ...\n");
	   session.disconnect();
	   if(!session.isConnected())
		   log.info("Successfully disconnected from device "+deviceIP+"\n");
	   else
		   log.error("Unable to disconnect to the device "+deviceIP+"\n");
	   if(FETCHING_DATA_VIA_DATABASE){
		   dbConnect.closeConnection();
		   log.info("Successfully closed the database. ");
	   }
   }
   /**
    * Enters into enable mode.<br>
    * Requires connection first. So first function connectSSH() should be called<br>
 * @throws Exception */
   public void enterPrivilegedMode() throws Exception{
	   if(this.getConnectionType().equals(ConnectionType.SSH)){
	     if(!(session.isConnected())){
		    log.fatal("X---------FatalErrorOccured-----------X\n\n");
		    log.error("Can't enter the privileged mode without connecting to the device!!!");
		    log.info("Call connectSSH() first and then try again.");
		    Exception e =new Exception("NoConnectionHasBeenMade");
		    throw e;
	     }
	     else{
		     log.info("Trying to enter the privileged mode ... ");
		     this.startExpectSSH();
		     String command=setCommand("PRIVILEGED_MODE", this.deviceType.toString());
		     log.info("Command \""+command+"\" is going to be executed");
		     expect.expect(TIME_OUT,Pattern.compile(".*>$"));
		     log.info("The following was happening on the terminal screen\n\t\t");
		     log.info(expect.before);
		     expect.send(command+"\n");
		     expect.expect(TIME_OUT,"Password");
		     expect.send(this.deviceEnPassword+"\n");
		     log.info("Command \""+command+"\" was sent. Waiting for response ... ");
		     log.info("---The command executed was \n"+expect.before);
		     expect.expect(TIME_OUT,Pattern.compile(".*#$"));
		     log.info("----After execution of command \n"+expect.before);
	      }
	   }
	   else{
		   if(!(this.telnetClient.isConnected())){
			   log.fatal("X---------FatalErrorOccured-----------X\n\n");
			    log.error("Can't enter the privileged mode without connecting to the device!!!");
			    log.info("Call connectTelnet() first and then try again.");
			    Exception e =new Exception("NoConnectionHasBeenMade");
			    throw e;
		   }
		   else{
			     log.info("Trying to enter the privileged mode ... ");
			     this.startExpectTelnet();
			     String command=setCommand("PRIVILEGED_MODE", this.getDeviceType().toString());
			     log.info("Command \""+command+"\" is going to be executed");
			     expect.expect(TIME_OUT,Pattern.compile(".*>$"));
			     log.info("The following was happening on the terminal screen\n\t\t");
			     log.info(expect.before);
			     expect.send(command+"\n");
			     expect.expect(TIME_OUT,"Password");
			     expect.send(this.deviceEnPassword+"\n");
			     log.info("Command \""+command+"\" was sent. Waiting for response ... ");
			     log.info("---The command executed was \n"+expect.before);
			     expect.expect(TIME_OUT,Pattern.compile(".*#$"));
			     log.info("----After execution of command \n"+expect.before);
		   }
	   }
   }
   public String setCommand(String need,String argDeviceType) throws FileNotFoundException, IOException{
		String command=null;
	    if(!FETCHING_DATA_VIA_DATABASE){
		    Properties deviceCommandsLocFile=new Properties();
		    log.info("Trying to open file "+COMMANDS_FILE_LOCATION_FILE+" ...\n");
		    deviceCommandsLocFile.load(new FileInputStream(commandFileLocs));
		    log.info("Successfully opened the file "+COMMANDS_FILE_LOCATION_FILE+"\n");
		    String locationOfDeviceCommandsFile=deviceCommandsLocFile.getProperty(argDeviceType.toUpperCase());
		    log.info("The device commands list file location determined to be "+locationOfDeviceCommandsFile+"\n");
		    Properties deviceCommands=new Properties();
		    log.info("Trying to open "+locationOfDeviceCommandsFile+" ... \n");
		    File deviceCommandsFile=new File(locationOfDeviceCommandsFile);
		    deviceCommands.load(new FileInputStream(deviceCommandsFile));
		    log.info("Successfully opened file "+locationOfDeviceCommandsFile+"\n");
		    command=deviceCommands.getProperty(need.toUpperCase());
		    log.info("Command to be executed is "+"\""+command+"\""+" for performing "+need+" on device "+argDeviceType);
		}
	    else{
	    	log.info("trying to connect to the database "+properties.getProperty("routerCommandsDetailsTable")+" ...");
	    	dbConnect.getTableInfo(properties.getProperty("routerCommandsDetailsTable"));
	    	log.info("connection successful to database "+ properties.getProperty("routerCommandsDetailsTable"));
	    	if(deviceType.equals(RouterType.CISCO))
	    	  command=dbConnect.findValue("commandMnemonics",need.toUpperCase(),"actualCommandForCiscoRouter" );
	    	else
	    	  command=dbConnect.findValue("commandMnemonics",need.toUpperCase(),"actualCommandForJuniperRouter");
	    	log.info(" Command "+command+" is going to be executed");
	    }
		return command;
   }
   /**
    * Used for starting the Expect tool. Expect tool is mainly used to pass arguments to the router terminal
    * */
   public void startExpectSSH() throws JSchException, IOException {
	   channel = session.openChannel("shell");
	   expect = new Expect(channel.getInputStream(),channel.getOutputStream());
	   channel.connect();	
	}
   public void startExpectTelnet() throws Exception{
	   expect = telnetClient.getExpect();
   }
   /**
    * Connect to database without fetching anything*/
   public void connectToDb(){
	   log.info("Fetching the database name and other details ... ");
	   dbConnect.setDbName(properties.getProperty("dbName"));
	   dbConnect.setUsrName(properties.getProperty("dbUsrname"));
	   dbConnect.setPassword(properties.getProperty("dbPassword"));
	   dbConnect.setSQLType(properties.getProperty("sqlServer").toLowerCase());
	   log.info("Trying to connect to the database "+dbConnect.getDbName()+" ...\n with user name "+dbConnect.getUsrname()+" and password ****** ");
	   dbConnect.connectToDataBase(dbConnect.getSQLType(), dbConnect.getDbName(), dbConnect.getUsrname(), dbConnect.getPswd());
	   log.info("Successfully connected to the database "+dbConnect.getDbName());
   }
   /**
    * For setting up connection to the database*/
   public void dbConnector(){
	   log.info("Extracting device information ... ");
	   dbConnect.getTableInfo(properties.getProperty("deviceDetailsTable"));
	   deviceName=dbConnect.findValue("deviceIP", deviceIP, "deviceName");
	   log.info("deviceName set to "+deviceName+" deviceIP= "+deviceIP);
	   if(dbConnect.findValue("deviceIP",deviceIP, "deviceType").equals("CISCO"))
		   deviceType=RouterType.CISCO;
	   else
		   deviceType=RouterType.JUNIPER;
	   log.info("deviceType set to "+deviceType);
	   devicePassword=dbConnect.findValue("deviceIP", deviceIP, "devicePassword");
	   deviceEnPassword=dbConnect.findValue("deviceIP",deviceIP,"deviceEnPassword");
	   log.info("Successfully extracted device data\nExtracting backupServer details");
	   log.info("Seraching for backupServerDetailsTable ... ");
	   dbConnect.getTableInfo(properties.getProperty("backupServerDetailsTable"));
	   log.info("Found the table containing backup server data");
	   backupServerIP=properties.getProperty("backupServerIP");
	   backupServerName=dbConnect.findValue("backupServerIP", properties.getProperty("backupServerIP"), "backupServerName");
	   backupServerPassword=dbConnect.findValue("backupServerIP", properties.getProperty("backupServerIP"), "backupServerPassword");
	   if(dbConnect.findValue("backupServerIP", properties.getProperty("backupServerIP"), "backupServerType").equals("scp"))
		     backupServerType=BackupServerType.scp;
	   else
		     backupServerType=BackupServerType.tftp;
	   backupServerFolderLocation=dbConnect.findValue("backupServerIP", backupServerIP, "backupServerFolderLocation");
	   if(dbConnect.findValue("backupServerIP", backupServerIP, "connectionType").equals("TELNET"))
		   connectionType=ConnectionType.TELNET;
	   else
		   connectionType=ConnectionType.SSH;
	   log.info("Succesfully collected data about the backup sever IP="+backupServerIP+" folderLoaction="+backupServerFolderLocation+" connection type= "+connectionType+" backupServerType= "+backupServerType+" username= "+backupServerName+" password= ******");
   }
}
