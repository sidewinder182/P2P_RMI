import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.TimeUnit;
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
	private List<PeerInterface> neighborStubs = new ArrayList<PeerInterface>();

	private void addNeighbor(PeerInterface stub) {
		neighborStubs.add(stub);
	}
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
					if(!result.contains(nodeId)) {
						result.add(nodeId);
					}
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
				if(!result.contains(nodeId)) {
					result.add(nodeId);
				}
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
		int startStock = 5;
		int hopcount = 2;
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
         		peer.addNeighbor((PeerInterface) registry.lookup("PeerInterface"));
//				stubs.add((PeerInterface) registry.lookup("PeerInterface"));
				numNeighbors++;
			} catch (NotBoundException e) {
//				e.printStackTrace();
			} catch(RemoteException re){
//				re.printStackTrace();
			}
        }
        peer.decision();
        if(peer.getBuyer() == 0) {
        	peer.setProduct(peer.chooseProduct());
        	System.out.println("Trying to sell " + peer.getProduct());
        }
        else{
        	List<Integer> replies;
        	while(true) {
        		peer.setProduct(peer.chooseProduct());
        		System.out.println("Trying to buy " + peer.getProduct());
        		boolean bought = false;
        		while(bought == false) {
        			replies = peer.lookup(peer.getProduct(), hopcount);
	        		if(!replies.isEmpty()) {
	        			int idx = peer.getRandomNumber(replies.size());
	        			int chosenSellerId = replies.get(idx);
	        			System.out.println("Buying from " + chosenSellerId);
	        			bought = true;
	        			// Buy from chosen seller
	        		}
        		}
        		try {
					TimeUnit.SECONDS.sleep(2);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
        	}
        }

	}
	private int getRandomNumber(int max) {
		Random rand = new Random();
		return rand.nextInt(max);
	}
	private void decision() {
		// TODO Auto-generated method stub
		Random rand = new Random();
		buyer = rand.nextInt(2);
		if(buyer == 0) {
			System.out.println("I am a seller");
		}
		else {
			System.out.println("I am a buyer");
		}
	}
	private int chooseProduct() {
		// TODO Auto-generated method stub
		Random rand = new Random();
		int select = rand.nextInt(1);
		return select;
	}

}
