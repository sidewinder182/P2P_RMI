import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.TimeUnit;

public class Peer implements PeerInterface{
	private int buyer = -1;
	private int nodeId;
	private int product;
	private int stock;
	private int neighbors = 2;
	private int startStock;
	private Hashtable<Integer,String> productNames;
	private Hashtable<Integer,String> neighborInfo;
	private List<PeerInterface> neighborStubs = new ArrayList<PeerInterface>();
	
	public Hashtable<Integer, String> getProductNames() {
		return productNames;
	}
	
	public void setProductNames(Hashtable<Integer, String> productNames) {
		this.productNames = productNames;
	}
	
	public int getStartStock() {
		return startStock;
	}
	public void setStartStock(int startStock) {
		this.startStock = startStock;
	}
	
	public Hashtable<Integer, String> getNeighborInfo() {
		return neighborInfo;
	}
	public void setNeighborInfo(Hashtable<Integer, String> neighborInfo) {
		this.neighborInfo = neighborInfo;
	}
	
	private void addNeighbor(PeerInterface stub) {
		// Adds neighbor reference to list of neighbors
		neighborStubs.add(stub);
		try {
			System.out.println("Added neighbor " + stub.getNodeId());
		} catch (RemoteException e) {
			e.printStackTrace();
		}
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
	
	private void restock() {
		this.chooseProduct();
		this.stock = this.startStock;
	}
	
	public List<Integer> lookup(int callingNodeId,int productId,int hopcount) throws NotReadyException {
//		System.out.println("Lookup called by : " + callingNodeId + "\n");
		List<Integer> result = new ArrayList<Integer>();
		if(buyer == -1) {
			throw new NotReadyException("Node " + nodeId + " is not yet ready");
		}
		if(hopcount == 0) {
			if(buyer == 1) {
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
					return result;
				}
			}
		}
		else {
			for(int i = 0;i < neighbors;i++) {
				try {
					if(neighborStubs.get(i).getNodeId() != callingNodeId) {
						result.addAll(neighborStubs.get(i).lookup(nodeId,productId,hopcount-1));
					}
				} catch (RemoteException e) {
//					e.printStackTrace();
				} catch(IndexOutOfBoundsException e1){
//					e1.printStackTrace();
				} catch(NotReadyException e2) {
//					e2.printStackTrace();
				} catch(Exception e3) {
					e3.printStackTrace();
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
	
	public synchronized boolean buy(int nodeId,int productId) {
		System.out.println("Received a buy request for " + productNames.get(productId + 1) + " from " + nodeId);
		if(this.buyer != 0) {
			System.out.println("Sorry. I am not a seller\n");
			return false;
		}
		if(productId != product) {
			System.out.println("Sorry. Wrong product\n");
			return false;
		}
		if(this.stock > 0) {
			this.stock -= 1;
			System.out.println("Sold item " + this.productNames.get(this.product+1) + " to node " + Integer.toString(nodeId) + "\nRemaining stock : " + this.stock + "\n");
			if(this.stock == 0) {
				this.restock();
			}
			return true;
		}
		System.out.println("Sorry. Not enough stock\n");
		return false;
	}
	
	private int reply() {
		return nodeId;
	}
	
	public Peer(Properties prop)
	{
		this.startStock = Integer.parseInt(prop.getProperty("Stock"));
		Hashtable<Integer,String> productNames = new Hashtable<Integer,String>();
		productNames.put(1, "Fish");
		productNames.put(2, "Salt");
		productNames.put(3, "Boar");
		setProductNames(productNames);
		
	}

	public static void main(String[] args) throws IOException {
		Properties prop = new Properties();
		InputStream input = null;
		input = new FileInputStream("config.properties");
		prop.load(input);
		
		// Checking system configuration
		Peer peer = new Peer(prop);
		int hopcount = Integer.parseInt(prop.getProperty("hop", "2"));	
		int nodeId = Integer.parseInt(args[0]);
		int numNodes = Integer.parseInt(prop.getProperty("N"));
		
		if(peer.getStartStock() <= 0) {
			System.out.println("Starting stock for a peer should be > 0. Please check config file\n");
			System.exit(0);
		}
		if(numNodes <= 0) {
			System.out.println("Number of nodes in the network cannot be less than 1. Please check config file\n");
			System.exit(0);
		}
		if(hopcount <= 0) {
			System.out.println("Hopcount should be > 0. Please check config file\n");
			System.exit(0);
		}
		if(nodeId <= 0 || nodeId > numNodes) {
			System.out.println("Invalid peer ID. Peer ID should be an integer in the interval [1,N]\n");
			System.exit(0);
		}
		peer.setNodeId(nodeId);
		
		
		// Binding itself to registry on specified port and ip in the config file
		int myPortNumber = Integer.parseInt(prop.getProperty(Integer.toString(nodeId)).split(":")[1]);
		String myIP = prop.getProperty(Integer.toString(nodeId)).split(":")[0];
		System.setProperty("java.rmi.server.hostname", myIP);

		
		try {
            PeerInterface stub = (PeerInterface) UnicastRemoteObject.exportObject(peer, 0);
            Registry registry = LocateRegistry.createRegistry(myPortNumber);
            registry.bind("PeerInterface", stub);
            System.err.println("Server ready");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
        
		
		// Establishing connections with neighbors according to a ring topology.
		int numNeighbors = 0;
		int portNumber;
		String ip="";
		String portNumber1="", portNumber2="";
		if(nodeId == 1)
		{
			// If nodeId 1, connect to node 2 and node N
			portNumber1 = prop.getProperty(Integer.toString(nodeId + 1));
			portNumber2 = prop.getProperty(Integer.toString(numNodes));
		}
		else if(nodeId == numNodes)
		{
			// If nodeId N, connect to node N-1 and node 1
			portNumber1 = prop.getProperty(Integer.toString(nodeId-1));
			portNumber2 = prop.getProperty(Integer.toString(1));
		}
		else
		{
			// If nodeId between 1 and N, connect to node (nodeId-1) and (nodeId+1)
			portNumber1 = prop.getProperty(Integer.toString(nodeId-1));
			portNumber2 = prop.getProperty(Integer.toString(nodeId+1));
		}
		
		while(numNeighbors < 2){
         	try {
         		if (numNeighbors == 0) {
         			portNumber = Integer.parseInt(portNumber1.split(":")[1]);
         			ip = portNumber1.split(":")[0];
         		}
         		else {
         			portNumber = Integer.parseInt(portNumber2.split(":")[1]);
         			ip = portNumber2.split(":")[0];
         		}
         		Registry registry = LocateRegistry.getRegistry(ip, portNumber);
         		PeerInterface nStub = (PeerInterface) registry.lookup("PeerInterface");
         		peer.addNeighbor(nStub);
				numNeighbors++;
			} catch (NotBoundException e) {
//				e.printStackTrace();
			} catch(RemoteException re){
				System.out.println("Neighbor peer not up yet");
//				re.printStackTrace();
			}
        }
		
		
		/* Randomly choosing buyer or seller role*/
		
//		if(peer.getNodeId() == 1) {
//			peer.setBuyer(1);
//		}
//		else if(peer.getNodeId() == 2) {
//			peer.setBuyer(0);
//		}
//		else{
//			peer.setBuyer(0);
//		}
        peer.decision();
		
		
        if(peer.getBuyer() == 0) { 
        	// Code for Seller
        	peer.setProduct(peer.chooseProduct());
        	peer.setStock(peer.getStartStock());
        }
        else{ 
        	// Code for Buyer
        	List<Integer> replies;
        	int numRequests = 0;
        	double totalTimeElapsed = 0;
        	while(true) {
        		boolean bought = false;
        		while(bought == false) {
        			peer.setProduct(peer.chooseProduct());
        			try {
//        				long startTime = System.nanoTime();
						replies = peer.lookup(peer.getNodeId(),peer.getProduct(), hopcount);
//						long endTime = System.nanoTime();
//						double timeElapsed = ((double)endTime - (double)startTime)/1000000;
//						totalTimeElapsed += timeElapsed;
//						numRequests++;
//						if(numRequests == 1000) {
//							System.out.println("Average time for 1000 search requests : " + totalTimeElapsed/1000 + " #############################################");
//							totalTimeElapsed = 0;
//							numRequests = 0;
//						}
						replies = peer.getUniqueElements(replies);
						for(int i = 0;i < replies.size();i++) {
	        				System.out.println("Got reply from nodeID : " + replies.get(i));
	        			}
		        		while(!replies.isEmpty()) {
		        			int idx = peer.getRandomNumber(replies.size());
		        			int chosenSellerId = replies.get(idx);
		        			String[] tokens = prop.getProperty(Integer.toString(chosenSellerId)).split(":");
		        			Registry registry = LocateRegistry.getRegistry(tokens[0],Integer.parseInt(tokens[1]));
		        			try {
		        				System.out.println("Buying from " + chosenSellerId);
		        				PeerInterface tempStub = (PeerInterface) registry.lookup("PeerInterface");
								bought = tempStub.buy(peer.getNodeId(),peer.getProduct());
								if(bought) {
									System.out.println("Succeeded\n");
									break;
								}
								else {
									System.out.println("Failed. Trying again\n");
								}
							} catch (NotBoundException e) {
								e.printStackTrace();
							} catch(Exception e) {
								e.printStackTrace();
							}
		        			replies.remove(idx);
		        		}
					} catch (NotReadyException e1) {
						e1.printStackTrace();
					}
        			if(!bought) {
	        			System.out.println("No sellers available. Choosing product again\n");
	        		}
        		}
        		
        		// Waiting for a random number of seconds between 1 and 10
        		try {
        			int waitTime = peer.getRandomNumber(10);
        			System.out.println("Waiting for " + (waitTime+1) + " seconds\n");
					TimeUnit.SECONDS.sleep(waitTime);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
        	}
        }

	}
	
	public int getRandomNumber(int max) {
		// Return a random number from [0,max - 1]
		Random rand = new Random();
		return rand.nextInt(max);
	}
	
	private void decision() {
		// Randomly assigns a buyer or seller role to peer.
		Random rand = new Random();
		buyer = rand.nextInt(2);
		if(buyer == 0) {
			System.out.println("I am a seller\n");
		}
		else {
			System.out.println("I am a buyer\n");
		}
	}
	private int chooseProduct() {
		// Randomly chooses a product out of fish, salt or boar.
		Random rand = new Random();
		int select = rand.nextInt(3);
		System.out.println("Product chosen : " + productNames.get(select+1) + "\n");
		return select;
	}
	
	private List<Integer> getUniqueElements(List<Integer> inputList){
		// Gets unique integers from a list
		Set<Integer> s = new HashSet<Integer>(inputList);
		return new ArrayList<Integer>(s);
	}

}
