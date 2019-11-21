import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public class ftpserver {

	private static final int sPort = 8000;   //The server will be listening on this port number

	public static void main(String[] args) throws Exception {
		System.out.println("The server is running."); 
    	ServerSocket listener = new ServerSocket(sPort);
		int clientNum = 1;
        	try {
            		while(true) {
                		new Handler(listener.accept(),clientNum).start();
				System.out.println("Client "  + clientNum + " is connected!");
				clientNum++;
            			}
        	} finally {
            		listener.close();
        	} 
 
    	}

		/**
     	* A handler thread class.  Handlers are spawned from the listening
     	* loop and are responsible for dealing with a single client's requests.
     	*/
		private static class Handler extends Thread {
    	private String cmd;    //message received from the client
		private Socket connection;
    	private ObjectInputStream in;	//stream read from the socket
    	private ObjectOutputStream out;    //stream write to the socket
		private int no;		//The index number of the client
		private Boolean lflag; // flag to keep checking if client is logged in or not
		
    	public Handler(Socket connection, int no) {
    		this.connection = connection;
    		this.no = no;
    	}

        public void run() {
 		try{
 			
 			lflag = Boolean.FALSE;
			//initialize input and output streams
			out = new ObjectOutputStream(connection.getOutputStream());
			out.flush();
			in = new ObjectInputStream(connection.getInputStream());
			try{
				while(true)
				{
					while( lflag==false )
					{
						//receive the message sent from the client
						String us = (String)in.readObject();
						System.out.println("Got username: " + us + " from client " + no);
						String pa = (String)in.readObject();
						if( us.equalsIgnoreCase("admin") && pa.equalsIgnoreCase("123") ) 
						{
							lflag = Boolean.TRUE;
							sendMessage("Logged in successfully");
						}
						else
						{
							sendMessage("Login unsucessfull...Retry");
						}
					}
					cmd = (String)in.readObject();
					System.out.println("Received command: " + cmd + " from client " + no);					
					if(cmd.startsWith("get"))
					{
						String fname= cmd.substring( cmd.indexOf(' ') + 1 );
						File file = new File(fname);
						if(file.exists())
						{
							out.writeBoolean(Boolean.TRUE);
							byte [] bytearr  = new byte [8192];
							FileInputStream finputs = new FileInputStream(fname);
					        bytearr = finputs.readAllBytes();
					        System.out.println("Sending " + fname+ ".....");
					        out.write(bytearr,0,bytearr.length);
					        System.out.println("Success");
					        finputs.close(); 
							out.flush();
						}
						else
						{
							out.writeBoolean(Boolean.FALSE);
							out.flush();
						}	
					}
					else if(cmd.startsWith("upload"))
					{
						Boolean flag = Boolean.FALSE;
						String fname= cmd.substring(cmd.indexOf(' ') + 1);
						flag=in.readBoolean();
					    if(flag) {
					    	byte [] bytearr  = new byte [8192];
							FileOutputStream fostream = new FileOutputStream(fname);
						    int count;
					    	while(( count = in.read(bytearr))>0 ){
					    		fostream.write(bytearr,0,count);
					    	}
					    	System.out.println("File " + fname + " received");
					    	fostream.close();
					    }
					    else
					    	System.out.println("Error in UPLOAD - Please try again with valid filename");
					}
					else if(cmd.equalsIgnoreCase("dir"))
					{
						String dir = System.getProperty("user.dir");
						File f = new File(dir);
						File[] list = f.listFiles();
						int n=list.length;
						ArrayList<String> fnames =new ArrayList<String>();
						for (int i = 0; i < n; i++) {
							if (list[i].isFile()) {
								fnames.add(list[i].getName());
							} 
						}
						out.writeObject(fnames);
					}
				}
			}
			catch(ClassNotFoundException classnot){
					System.err.println("Input in in unknown format");
				}
		}
		catch(IOException ioException){
			System.out.println("Disconnecting with client " + no);
		}
		finally{
			try{
				in.close();
				out.close();
				connection.close();
			}
			catch(IOException ioException){
				System.out.println("Disconnect with client " + no);
			}
		}
	}

	public void sendMessage(String msg)
	{
		try{
			out.writeObject(msg);
			out.flush();
			System.out.println( msg + " with Client " + no);
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}
	}
    }
}