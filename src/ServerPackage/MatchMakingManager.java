package ServerPackage;

public class MatchMakingManager implements Runnable{

	@Override
	public void run() {
		while (true) {
			Server.mmService.checkQueues(Server.mmService.playerEasyQueue2, 1);
			Server.mmService.checkQueues(Server.mmService.playerChallengingQueue2, 2);
			Server.mmService.checkQueues(Server.mmService.playerInsaneQueue2, 3);
			
			try {
				Thread.sleep(3250);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
