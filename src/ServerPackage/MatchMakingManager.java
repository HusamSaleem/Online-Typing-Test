package ServerPackage;

public class MatchMakingManager implements Runnable{

	@Override
	public void run() {
		while (true) {
			Server.mmService.checkQueue();
			
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
