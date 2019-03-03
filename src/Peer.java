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
//import org.json.simple.JSONArray; 
//import org.json.simple.JSONObject;
import java.rmi.server.UnicastRemoteObject;

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
//		int startStock = 5;
		
		Peer peer = new Peer(prop);
		int hopcount = Integer.parseInt(prop.getProperty("hop", "2"));	
//		Hashtable<Integer,String> productNames = new Hashtable<Integer,String>();
//		productNames.put(1, "Fish");
//		productNames.put(2, "Salt");
//		productNames.put(3, "Boar");
//		peer.setProductNames(productNames);
		
//		Hashtable<Integer,String> h = new Hashtable<Integer,String>();
		// Hashtable for neighbor ips and ports
//		h.put(1,"localhost:8911");
//		h.put(2,"localhost:8912");
//		h.put(3,"localhost:8913");
//		peer.setNeighborInfo(h);
		int nodeId = Integer.parseInt(args[0]);
		int numNodes = Integer.parseInt(prop.getProperty("N"));
		peer.setNodeId(nodeId);
		int portNumber;
		String ip;
		String portNumber1, portNumber2;
		if(nodeId == 1)
		{
			portNumber1 = prop.getProperty(Integer.toString(nodeId + 1));
			portNumber2 = prop.getProperty(Integer.toString(numNodes));
		}
		else if(nodeId == numNodes)
		{
			portNumber1 = prop.getProperty(Integer.toString(nodeId-1));
			portNumber2 = prop.getProperty(Integer.toString(1));
		}
		else
		{
			portNumber1 = prop.getProperty(Integer.toString(nodeId-1));
			portNumber2 = prop.getProperty(Integer.toString(nodeId+1));
		}
//		int numNodes = Integer.parseInt(args[1]);
		int myPortNumber = Integer.parseInt(prop.getProperty(Integer.toString(nodeId)).split(":")[1]);
		int numNeighbors = 0;
		
		try {
            PeerInterface stub = (PeerInterface) UnicastRemoteObject.exportObject(peer, 0);
            Registry registry = LocateRegistry.createRegistry(myPortNumber);
            registry.bind("PeerInterface", stub);
            System.err.println("Server ready");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
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
         		peer.addNeighbor((PeerInterface) registry.lookup("PeerInterface"));
				numNeighbors++;
			} catch (NotBoundException e) {
//				e.printStackTrace();
			} catch(RemoteException re){
//				re.printStackTrace();
			}
        }
		
		
//		if(peer.getNodeId() == 1) {
//			peer.setBuyer(0);
//		}
//		else if(peer.getNodeId() == 2) {
//			peer.setBuyer(0);
//		}
//		else{
//			peer.setBuyer(1);
//		}
        peer.decision();
		
		
        if(peer.getBuyer() == 0) {
        	peer.setProduct(peer.chooseProduct());
//        	peer.setStartStock(peer.getStartStock());
        	peer.setStock(peer.getStartStock());
        }
        else{
        	List<Integer> replies;
        	while(true) {
        		peer.setProduct(peer.chooseProduct());
        		boolean bought = false;
        		while(bought == false) {
        			try {
						replies = peer.lookup(peer.getNodeId(),peer.getProduct(), hopcount);
						replies = peer.getUniqueElements(replies);
						for(int i = 0;i < replies.size();i++) {
	        				System.out.println("Got reply from nodeID : " + replies.get(i));
	        			}
		        		if(!replies.isEmpty()) {
		        			int idx = peer.getRandomNumber(replies.size());
		        			int chosenSellerId = replies.get(idx);
		        			String[] tokens = prop.getProperty(Integer.toString(chosenSellerId)).split(":");
		        			Registry registry = LocateRegistry.getRegistry(tokens[0],Integer.parseInt(tokens[1]));
		        			try {
								PeerInterface tempStub = (PeerInterface) registry.lookup("PeerInterface");
								bought = tempStub.buy(peer.getNodeId(),peer.getProduct());
								System.out.println("Buying from " + chosenSellerId + "\n");
								System.out.println(bought);
								replies.remove(idx);
							} catch (NotBoundException e) {
								e.printStackTrace();
							} catch(Exception e) {
								e.printStackTrace();
							}
		        		}
//		        		else {
//		        			System.out.println("No replies");
//		        		}
					} catch (NotReadyException e1) {
						e1.printStackTrace();
					}
        			
        		}
        		try {
					TimeUnit.SECONDS.sleep(5);
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
			System.out.println("I am a seller\n");
		}
		else {
			System.out.println("I am a buyer\n");
		}
	}
	private int chooseProduct() {
		// TODO Auto-generated method stub
		Random rand = new Random();
		int select = rand.nextInt(1);
		System.out.println("Product chosen : " + productNames.get(select+1) + "\n");
		return select;
	}
	
	private List<Integer> getUniqueElements(List<Integer> inputList){
		Set<Integer> s = new HashSet<Integer>(inputList);
		return new ArrayList<Integer>(s);
	}

}
