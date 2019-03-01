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


public class Peer implements PeerInterface{
	private int buyer;
	private int nodeId;
	private int product;
	private int stock;
	private int neighbors = 2;
	private List<PeerInterface> neighborStubs;

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
	
	public List<Integer> lookup(int productId,int hopcount) {
		List<Integer> result = new ArrayList<Integer>();
		if(hopcount == 0) {
			if(buyer == 1) {
//				result.add(0);
				return result;
			}
			else {
				if(productId == product) {
					result.add(nodeId);
					return result;
				}
				else {
//					result.add(0);
					return result;
				}
			}
		}
		else {
			for(int i = 0;i < neighbors;i++) {
				try {
					result.addAll(neighborStubs.get(i).lookup(productId,hopcount-1));
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
			if(buyer == 0 && productId == product) {
				result.add(nodeId);
			}
			return result;
		}
	}
	
	public boolean buy(int nodeId,int productId) {
		if(this.buyer == 1) {
			return false;
		}
		if(productId != product) {
			return false;
		}
		if(this.stock > 0) {
			this.stock -= 1;
			System.out.println("Sold item " + Integer.toString(this.product) + " to node " + Integer.toString(nodeId));
			return true;
		}
		return false;
	}
	private int reply() {
		return nodeId;
	}

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		int startStock = 5;
		Peer peer = new Peer();
		peer.setNodeId(Integer.parseInt(args[0]));
		int myPortNumber = Integer.parseInt(args[2]);
        try {
            PeerInterface stub = (PeerInterface) UnicastRemoteObject.exportObject(peer, 0);
            Registry registry = LocateRegistry.createRegistry(myPortNumber);
            registry.bind("PeerInterface", stub);
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
		List<PeerInterface> stubs = new ArrayList<PeerInterface>();
		while(numNeighbors < 2){
         	try {
         		if (numNeighbors == 0) {
         			portNumber = portNumber1;
         		}
         		else {
         			portNumber = portNumber2;
         		}
         		Registry registry = LocateRegistry.getRegistry(portNumber);
//				PeerInterface 
				stubs.add((PeerInterface) registry.lookup("PeerInterface"));
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
        	while(true) {
        		peer.setProduct(peer.chooseProduct());
        		// code for buying
        	}
        }
        else {
        	System.out.println("I am seller");
        	peer.setProduct(peer.chooseProduct());
        	peer.setStock(startStock);
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
	private int chooseProduct() {
		// TODO Auto-generated method stub
		Random rand = new Random();
		int select = rand.nextInt(3);
		return select;
	}

}
