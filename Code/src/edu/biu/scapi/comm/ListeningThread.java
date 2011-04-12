/**
 * 
 */
package edu.biu.scapi.comm;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;

import edu.biu.scapi.generals.Logging;

/** 
 * @author LabTest
 */
class ListeningThread extends Thread{
	private Map<InetAddress , Vector<SecuringConnectionThread>> connectingThreadsMap;//map that includes vectors of SecuringConnectionThread of the down connections.
																				  //Since we may have multiple channels from the same ip address we use a vector
																				  //for each ip address. We can not differentiate using the port since when a client connects
																				  //its port is unknown.
	private int port;//the port to listen on
	private boolean bStopped = false;//a flag that indicates if to keep on listening or stop
	private ServerSocketChannel listener;
	private int numOfIncomingConnections;
	

	/**
	 * 
	 */
	public ListeningThread( Map<InetAddress ,Vector<SecuringConnectionThread>> securingThreadsMap, int port, int numOfIncomingConnections) {

		connectingThreadsMap = securingThreadsMap;
		this.numOfIncomingConnections = numOfIncomingConnections;
		
		//prepare the listener.
		try {
			listener = ServerSocketChannel.open();
			listener.socket().bind (new InetSocketAddress (port));
			listener.configureBlocking (false);
		} catch (IOException e) {
			
			Logging.getLogger().log(Level.WARNING, e.toString());
			
			e.printStackTrace();
		}
		
		
	}
	
	/**
	 * 
	 * stopConnecting - sets the flag bStopped to false. In the run function of this thread this flag is checked
	 * 					if the flag is true the run functions returns, otherwise continues.
	 */
	public void stopConnecting(){
		
		//set the flag to true.
		bStopped = true;
	}
	
	
	
	/**
	 * run : This function is the main function of the ListeningThread. Mainly, we listen and accept valid connections as long
	 *  	 as the flag bStopped is false.
	 *       We use the ServerSocketChannel rather than the regular ServerSocket since we want the accept to be non-blocking. If
	 *       the accept function is blocking the flag bStopped will not be checked until the thread is unblocked.  
	 */
	public void run() {

		//first set the channels in the map to connecting
		Collection<Vector<SecuringConnectionThread>> collection = connectingThreadsMap.values();
		Iterator<Vector<SecuringConnectionThread>> itr = collection.iterator();
		
		while(itr.hasNext()){ 
			
			//get the vector of threads.
			Vector<SecuringConnectionThread> threadsVector = itr.next();
			int vectorSize = threadsVector.size();
			
			//go over the vector to set the state to connecting.
			for(int i=0; i<vectorSize ; i++){
				
				//get the plain channel in order to change the state
				PlainChannel channel = threadsVector.get(i).getChannel(); 
			
				//set the channel state to connecting
				channel.setState(PlainChannel.State.CONNECTING);
			}
		       
		}
		
			
		int i=0;
		//loop for incoming connections and make sure that this thread should not stopped.
        while (i < numOfIncomingConnections && !bStopped) {
        	
            SocketChannel socketChannel = null;
			try {
				
				//use the server socket to listen on incoming connections.
				// accept connections from all the smaller processes
				Logging.getLogger().log(Level.INFO, "Trying to listen "+ listener.socket().getLocalPort());
				
				socketChannel = listener.accept();
			
				
			}	catch (ClosedChannelException e) {
				// TODO: handle exception
			} 	catch (IOException e) {
				
				Logging.getLogger().log(Level.WARNING, e.toString());
				
				e.printStackTrace();
			}
			
			//there was no connection request
			if(socketChannel==null){
				try {
					Thread.sleep (1000);
				} catch (InterruptedException e) {
					
					Logging.getLogger().log(Level.INFO, e.toString());
					
					e.printStackTrace();
				}
			}
			else{
				
				
				//get the ip of the client socket
				InetAddress inetAddr = socketChannel.socket().getInetAddress();
				
				
				//use the address from the socket and find it the map. We get a vector of all the SecuringConnectionThreads that have this ip address.
				Vector<SecuringConnectionThread> scThreadsVector = connectingThreadsMap.get(inetAddr);
				
				//check if the ip address is a valid address. I.e. exists in the map. If the returned vector is null it means that there is no
				//SecuringConnectionThreads for this address.
				if(scThreadsVector==null){//an unauthorized ip tried to connect
					
					//close the socket
					try {
						socketChannel.close();
					} catch (IOException e) {
						
						Logging.getLogger().log(Level.WARNING, e.toString());
						
						e.printStackTrace();
					}
				}
	        	else{ //we have a thread that corresponds to this ip address. Thus, this address is valid
	        		
	        		//increment the index
	        		i++;
	        		
	        		//remove the first index and get the securing thread. Get the first thread in the vector. It may be that this is not
	        		//the thread that has the appropriate port. However this is for the key exchange algorithm to check if it is important
	        		//to the application that use the com layer.
	        		SecuringConnectionThread scThread = scThreadsVector.remove(0);
	        		
	        		//If there is nothing left in the vector remove it from the map too.
	        		if(scThreadsVector.isEmpty()){
	        			
	        			connectingThreadsMap.remove(inetAddr);
	        		}
	        			
	        		
	        		//check that the channel is concrete channel and not some decoration
	        		if(scThread.getChannel() instanceof PlainTCPChannel){
	        			//get the channel from the thread and set the obtained socket.
	        			((PlainTCPChannel)scThread.getChannel()).setSocket(socketChannel.socket());
	        			
	        			//start the connecting thread
	        			scThread.start();
	        		}
	        		else
	        			;//throw an exception. The channel must be concrete
	        		
	        	}
			}
        		
        }	
        Logging.getLogger().log(Level.INFO, "End of listening thread run");
        System.out.println("End of listening thread run");
	}
}