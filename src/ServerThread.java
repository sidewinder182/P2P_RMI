import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerThread extends Thread{
	private int portNumber;
	public ServerThread(int portNumber)
	{
		this.portNumber = portNumber;
	}
	
	
	public void run()
	{
		try {
			ServerSocket ss = new ServerSocket(this.portNumber);
			while(true)
			{
				Socket socket = ss.accept();
				
				System.out.println("New connection made at " + portNumber);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
