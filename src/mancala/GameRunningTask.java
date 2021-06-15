package mancala;

import javafx.concurrent.Task;
import utils.I18N;

public class GameRunningTask extends Task<Void> {

	private ControllerMancala controller;
	private SocketManager manager;
	
	public GameRunningTask(ControllerMancala controller, SocketManager manager) {
		super();
		this.controller = controller;
		this.manager=manager;
	}

	@Override
	protected Void call() throws Exception {
		while(true) {
			if(controller.isBeginning())
				updateMessage(I18N.createStringBinding("info.yourTurn").getValue());
			else
				updateMessage(I18N.createStringBinding("info.notYourTurn").getValue());
			Thread send = new Thread(new HandleSocketTask(manager));
			send.setDaemon(true);
			send.run();
		}
	}

}
