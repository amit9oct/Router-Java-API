package telnetConnect;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;




import expectForJava.Expect;

public class TelnetConnect {
	final private int TIME_OUT=1;
	/**
	 * For writing on telnet terminal*/
    private Expect expect=null;
	/**
     * Default connection port is set to 23.<br>As by default we have 23 as telnet port*/
	private int DEFAULT_CONNECTION_PORT=23;
	/**
	 * The telnetConnect supports only client socket*/
	private Socket client=null;
	/**
	 * Sets the input stream for the socket*/
	private InputStream input=null;
	/**
	 * Sets the output stream for the socket*/
	private OutputStream output=null;
    /**
     * */
	private void startExpect(){
	   expect=new Expect(this.input,this.output);
	}
	/**
	 * Opens the default port number 23.<br>Default port can be changed by changing DEFAULT_CONNECTION_PORT.<br>
	 * @throws Exception 
	 * */
	public TelnetConnect(String ip,String usrName,String pswd) throws Exception{
		try{
			client=new Socket(ip,DEFAULT_CONNECTION_PORT);
			input=client.getInputStream();
			output=client.getOutputStream();
			this.startExpect();
			expect.expect(TIME_OUT,"Username");
			expect.send(usrName+"\n");
			expect.expect(TIME_OUT,"Password");
			expect.send(pswd+"\n");
		}catch(Exception e){
			System.out.println("Unable to connect to the server");
			e=new Exception("Unable to connect to the server");
			throw e;
		}
	}
	/**
	 * Initializes the socket and opens the port specified by the user*/
    public TelnetConnect(String ip,int port,String usrName,String pswd) throws Exception{
    	try{
    		client=new Socket(ip,port);
    		input=client.getInputStream();
    		output=client.getOutputStream();
    		this.startExpect();
			expect.expect(TIME_OUT,"Username");
			expect.send(usrName+"\n");
			expect.expect(TIME_OUT,"Password");
			expect.send(pswd+"\n");
    	}catch(Exception e){
    		System.out.println("Socket not opened\n");
    		e= new Exception("Unable to open client socket");
    		throw e;
    	}
    }
    /**
     * To see what's on terminal screen
     * @throws Exception */
    public String onScreen() throws Exception{
    	expect.expect(TIME_OUT,">");
    	if(expect.before==null&&expect.match==null){
    		expect.expect(1,"#");
    	}
    	if(expect.before==null&&expect.match==null){
    		return "\n";
    	}
    	return expect.before+expect.match;
    }
    /**
     * To write on telnet terminal 
     * @throws Exception 
     * @throws IOException */
    public void writeOnScreen(String argCommand) throws IOException, Exception{
    	expect.send(argCommand+"\n");
    }
    /**
     * To check if the client is connected*/
    public boolean isConnected(){
    	return !(client.isClosed());
    }
    /**
     * For returning the input stream*/
    public InputStream getInputStreamForTelnet() throws Exception{
    	if(input!=null)
    	  return input;
    	else{
    		throw new Exception("Input Stream for TelnetConnect is not initialized");
    	}
    }
    /**
     * For returning the output stream*/
    public OutputStream getOutputStreamForTelnet() throws Exception{
    	if(output!=null)
    		return output;
    	else
    		throw new Exception("Output stream for TelnetConnect is not initialized");
    }
    /**
     * For disconnecting the telnet client*/
    public void disconnect() throws IOException{
    	input.close();
    	output.close();
    	client.close();
    	expect.send("exit"+"\n");
    	if(!client.isClosed())
    		System.err.println("Socket not closed!!");
    }
    /**
     * Returns the expect for the use of other tools*/
    public Expect getExpect(){
       return expect;	
    }
}
