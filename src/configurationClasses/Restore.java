package configurationClasses;


import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;



public class Restore extends BackupRestore{
	/**
	 * Shows if files are restored successfully or not*/
	private boolean filesRestoredSuccessfully=false;
	/**
	 * Does the repetitive work of opening .properties file*/
	private void repetitiveWork(){
		log = Logger.getLogger(Backup.class.getName());
		PropertyConfigurator.configure("log4j.properties");
	}
	/**
	 * To get the status if files are restored or not. */
	public boolean isRestored(){
		return filesRestoredSuccessfully;
	}
	/**
	 * Sets the parameters for the device whose restore will be taken<br>
	 * <br>Requires only IP of the device as parameter.Rest all the settings are default.<br>
	 * */
	public Restore(String ip) {
		super(ip);
		this.repetitiveWork();
		log.info("A restore instance for the device named "+this.getDeviceName()+" and IP "+this.getDeviceIp()+" is being created ...");
	}
	/**
	 * Sets the parameters for the device whose backup will be taken<br>
	 * <br>Requires IP,user name,password,enable password and device type of the device<br>
	 * Rest all the settings are default settings<br>*/
	public Restore(String ip,String username,String password,String enablePassword,String argDeviceType){
		super(ip,username,password,enablePassword,argDeviceType);
		this.repetitiveWork();
		log.info("A restore instance for the device named "+this.getDeviceName()+" and IP "+this.getDeviceIp()+" is being created ...");
	}
	/**
	 * Used to restore configuration settings for the device<br>It has the version feature.
	 * @throws Exception */
	public void restore(String argFileName) throws Exception{
		log.info("X---------RestoreMakingStarted-----------X\n\n");
		log.info("retore(String argFilename) is being executed");
		this.filesRestoredSuccessfully=false;
		String command=this.setCommand("COPY", this.getDeviceType().toString());
		if(this.getDeviceType().equals(RouterType.CISCO));
		    this.enterPrivilegedMode();
		command=command.concat(" "+this.getBackupServerType()+"://"+this.getBackupServerName()+"@"+this.getBackupServerIp()+"/"+this.getBackupServerFolderLocation());
		command=command+" "+this.setCommand("RUNNING_CONFIG", this.getDeviceType().toString());
		expect.send(command+"\n");
		expect.send(this.getBackupServerFolderLocation()+argFileName);
		expect.send("\n");
		expect.send("\n");
		log.info("Command \""+command+"\" was sent");
		if(this.getBackupServerType().equals(BackupServerType.scp)){
		  log.info("Expecting Password Prompt");
		  expect.expect(TIME_OUT,"Password");
		  log.info("The following happened before sending password\n"+expect.before);
		  if(expect.isSuccess){
			 expect.send(this.backupServerPassword+"\n");
			 log.info("Got the password prompt and password of backup server was sent successfully \n");
		  }
		  else{
			 log.error("Password prompt wasn't found or some error occurred while sending the password of backup server\n");
		  }
		}
		expect.expect(TIME_OUT,Pattern.compile(".*#$"));
		log.info("After command execution \n"+expect.before);
        if(expect.before==null){
        	filesRestoredSuccessfully=false;
        	log.error("Unable to copy files!! on the backup server with IP "+this.getBackupServerIp()+"\n"+"\t\t\t\t\t\t\t\t\t\t\t\t\t\tPossible reason: The device is not registered in the database. \n\t\t\t\t\t\t\t\t\t\t\t\t\t\tTry passing all the arguments the user name,password, enable password and device type to the constructor .");
		}
        else if(expect.before.contains("bytes copied")){
			filesRestoredSuccessfully = true;
		    log.info("Files successfully copied\n");	
		}
		else{
			filesRestoredSuccessfully=false;
			log.error("Unable to copy files!! on the backup server with IP "+this.getBackupServerIp()+"\n");
		}
		log.info("Logging out of device "+this.getDeviceIp()+" ...");
		expect.send("exit\n");
		expect.close();
	}
}
