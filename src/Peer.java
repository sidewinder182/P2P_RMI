import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

//import org.json.simple.JSONArray; 
//import org.json.simple.JSONObject;


public class Peer {
	private int buyer;
	private int nodeId;
	private int product;
	private int stock;

	public int isBuyer() {
		return buyer;
	}

	public void setBuyer(boolean buyer) {
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

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		Peer peer = new Peer();
		peer.decision();
		peer.setNodeId(Integer.parseInt(args[0]));
		int portNumber = Integer.parseInt(args[2]);
		int numNodes = Integer.parseInt(args[1]);
		if(peer.getNodeId() == 1)
		{
			ServerThread serverThread = new ServerThread(portNumber);
			serverThread.start();
		}
		else if(peer.getNodeId() < numNodes)
		{
			Socket peerSocket = new Socket("localhost", 8000);
//			PrintStream PS = new PrintStream(peerSocket.getOutputStream());
//			PS.println("connection made from node" + peer.getNodeId());
			ServerThread serverThread2 = new ServerThread(portNumber);
			serverThread2.start();
			 
		}
		else if(peer.getNodeId() == numNodes)
		{
			Socket peerSocket1 = new Socket("localhost", 8000);
			Socket peerSocket2 = new Socket("localhost", 8001);
		}
		
		
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
