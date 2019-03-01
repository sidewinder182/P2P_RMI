import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
//import org.json.simple.JSONArray; 
//import org.json.simple.JSONObject;
import java.rmi.server.UnicastRemoteObject;


public class Peer implements Peer_interface{
	private int buyer;
	private int nodeId;
	private int product;
	private int stock;

	public int getBuyer() {
		return buyer;
	}

	public void setBuyer(int buyer) {
		this.buyer = buyer;
	}

	public int getNodeId() {
		return nodeId;
	}

	public void setNodeId(int nodeId) {
		this.nodeId = nodeId;
	}

	public int getProduct() {
		return product;
	}

	public void setProduct(int product) {
		this.product = product;
	}

	public int getStock() {
		return stock;
	}

	public void setStock(int stock) {
		this.stock = stock;
	}
	
	public void printMsg() {
		System.out.println("Hello!");
	}

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		Peer peer = new Peer();
		peer.setNodeId(Integer.parseInt(args[0]));
		int myPortNumber = Integer.parseInt(args[2]);
        try {
            Peer_interface stub = (Peer_interface) UnicastRemoteObject.exportObject(peer, 0);
            Registry registry = LocateRegistry.createRegistry(myPortNumber);
            registry.bind("Peer_interface", stub);
            System.err.println("Server ready");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
        int portNumber;
        
		int portNumber1 = Integer.parseInt(args[3]);
		int portNumber2 = Integer.parseInt(args[4]);
		int numNodes = Integer.parseInt(args[1]);
		int numNeighbors = 0;
		List<Peer_interface> stubs = new ArrayList<Peer_interface>();
		while(numNeighbors < 2){
         	try {
         		if (numNeighbors == 0) {
         			portNumber = portNumber1;
         		}
         		else {
         			portNumber = portNumber2;
         		}
         		Registry registry = LocateRegistry.getRegistry(portNumber);
//				Peer_interface 
				stubs.add((Peer_interface) registry.lookup("Peer_interface"));
				numNeighbors++;
			} catch (NotBoundException e) {
//				e.printStackTrace();
			} catch(RemoteException re){
//				re.printStackTrace();
			}
        }
        peer.decision();
        if(peer.getBuyer() == 1){
        	System.out.println("I am buyer");
        }
        else {
        	System.out.println("I am seller");
        }
        stubs.get(0).printMsg();
        stubs.get(1).printMsg();
// 		if(peer.getNodeId() == 1)
// 		{
// 			// ServerThread serverThread = new ServerThread(portNumber);
// 			// serverThread.start();
// 		}
// 		else if(peer.getNodeId() < numNodes)
// 		{
// 			Socket peerSocket = new Socket("localhost", 8000);
// //			PrintStream PS = new PrintStream(peerSocket.getOutputStream());
// //			PS.println("connection made from node" + peer.getNodeId());
// 			ServerThread serverThread2 = new ServerThread(portNumber);
// 			serverThread2.start();
			 
// 		}
// 		else if(peer.getNodeId() == numNodes)
// 		{
// 			Socket peerSocket1 = new Socket("localhost", 8000);
// 			Socket peerSocket2 = new Socket("localhost", 8001);
// 		}
		
//		System.out.println(peer.getNodeId());
//		JSONArray a = (JSONArray) parser.parse(new FileReader("config.json"));

	}
	private void decision() {
		// TODO Auto-generated method stub
		Random rand = new Random();
		int select = rand.nextInt(2);
		this.buyer = select;
	}

}
