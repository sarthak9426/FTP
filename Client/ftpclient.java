import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public class ftpclient {
	Socket requestSocket; // socket connect to the server
	ObjectOutputStream out; // stream write to the socket
	ObjectInputStream in; // stream read from the socket
	int portNo;
	Boolean cflag = Boolean.FALSE; //flag to check if the client is already connected or not 
 	Boolean lflag = Boolean.FALSE; // flag to break out of the while loop if login is successfull

	void run() {
		try {
			while(true) {
				BufferedReader Reader = new BufferedReader( new InputStreamReader( System.in ) );
				System.out.println("Please enter a command: ");
				String raw_cmd = Reader.readLine(); // read command from input
				String cmd = raw_cmd.toLowerCase();
				if ( cmd.startsWith( "ftpclient" ) ) {
					if ( cflag==true ) {
						System.out.println( "You are already connected to the server. Please try a different command" );
					} else {
						if ( cmd.equalsIgnoreCase( "ftpclient localhost 8000" ) ) {
							requestSocket = new Socket( "localhost", 8000 );
							System.out.println( "Connected to server at localhost with port 8000" );
							cflag = Boolean.TRUE;
							out = new ObjectOutputStream( requestSocket.getOutputStream() );
							out.flush();
							in = new ObjectInputStream( requestSocket.getInputStream() );
							while ( lflag == false ) {
								System.out.println( "Please enter your username:" );
								String us = Reader.readLine();
								sendMessage( us );
								System.out.println( "Please enter your password:" );
								String pa = Reader.readLine();
								sendMessage( pa );
								String temp = ( String ) in.readObject();
								System.out.println( temp );
								if ( temp.equalsIgnoreCase( "Logged in successfully" ) ) {
									lflag = Boolean.TRUE;
								}
							}
						} else 
							System.out.println("IP or Port No Invalid. Please try again with valid IP and Port No. ");
					}

				} else if ( cmd.startsWith("get") ) {
					if ( cflag ) {
						Boolean flag = Boolean.FALSE;
						out.writeObject(cmd);
						flag = in.readBoolean();
						if ( flag==true ) {	
							byte[] bytearr = new byte[8192];
							String filename = cmd.substring( cmd.indexOf(' ') + 1 );
							FileOutputStream fostream = new FileOutputStream( filename );
							in.read( bytearr, 0, bytearr.length );
							String s = new String( bytearr );
							bytearr = s.trim().getBytes();
							fostream.write( bytearr, 0, bytearr.length );
							fostream.close();
							System.out.println( "File " + filename + " received successfully" );
						
						} else 
							System.out.println( "No such file exists. Please try again with a valid file name." );

					} else 
						System.out.println( "No connection found. Please connect to the server first" );

				} else if (cmd.startsWith( "upload" )) {
					if (cflag==true) {
						out.writeObject(cmd);
						String filename = cmd.substring( cmd.indexOf(' ') + 1 );
						byte[] bytearr = new byte[8192];
						File file = new File( filename );
						if ( file.exists() ) {
							out.writeBoolean( Boolean.TRUE );
							FileInputStream finputs = new FileInputStream( filename );
							bytearr = finputs.readAllBytes();
							out.write( bytearr, 0, bytearr.length );
							System.out.println( "Sending file... " + filename );
							finputs.close();
							out.flush();
							System.out.println( "Success." );
						} else 
						{
							out.writeBoolean(Boolean.FALSE);
							System.out.println("Invalid file name. Please try again");
						}
					} else
						System.out.println( "No connection found. Please connect to the server first" );

				} else if ( cmd.equalsIgnoreCase( "dir" ) ) {
					if ( cflag ) {
						out.writeObject( cmd );
						Object obj = in.readObject();
						if ( obj instanceof ArrayList<?> ) {
							ArrayList<?> filenames = ( ArrayList<?> ) obj;
							if ( filenames.size() > 0 ) {
								for (int i = 0; i < filenames.size(); i++) {
									Object o = filenames.get(i);
									if (o instanceof String) {
										System.out.println( "\t" + filenames.get(i) );
									}
								}
							}

						}
					} else 
						System.out.println( "No connection found. Please connect to the server first" );
				} else 
					System.out.println( "Command Invalid. Please enter a valid command." );
			}

		} catch ( ConnectException e ) {
			System.err.println( "Connection refused. You need to initiate a server first." );
		} catch ( ClassNotFoundException e ) {
			System.err.println( "Class not found" );
		} catch ( UnknownHostException unknownHost ) {
			System.err.println( "You are trying to connect to an unknown host!" );
		} catch ( IOException ioException ) {
			ioException.printStackTrace();
		} finally {
			try {
				in.close();
				out.close();
				requestSocket.close();
			} catch ( IOException ioException ) {
				ioException.printStackTrace();
			}
		}
	}

	// send a message to the output stream
	void sendMessage( String msg ) {
		try {
			// stream write the message
			out.writeObject( msg );
			out.flush();
		} catch ( IOException ioException ) {
			ioException.printStackTrace();
		}
	}

	// main method
	public static void main(String args[]) {
		ftpclient client = new ftpclient();
		client.run();
	}

}