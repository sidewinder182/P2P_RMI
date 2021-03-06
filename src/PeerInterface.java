import java.rmi.Remote;
import java.rmi.RemoteException;
//import java.io.*;
//import java.text.*;
import java.util.*;
//import java.net.*;

public interface PeerInterface extends Remote{
	public void printMsg() throws RemoteException;
	public int getNodeId() throws RemoteException;
	public List<Integer> lookup(int nodeId,int productId,int hopcount) throws RemoteException, NotReadyException;
	public boolean buy(int nodeId,int productId) throws RemoteException;
	// public String deposit(String acc_no,double amt) throws RemoteException;
	// public String withdraw(String acc_no,double amt) throws RemoteException;
	// public String balance(String acc_no) throws RemoteException;
	// public List<String> transaction_details(String acc_no, Date start_date, Date end_date) throws RemoteException;
	// public List<String> all_transaction_details(String acc_no) throws RemoteException;
}