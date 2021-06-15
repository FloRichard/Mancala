package mancala;

import javafx.concurrent.Task;

public class HandleSocketTask extends Task<ServerOutputController> {
	
	private SocketManager manager;
	
	public HandleSocketTask(SocketManager manager) {
		super();
		this.manager = manager;
	}

	@Override
	protected ServerOutputController call() throws Exception {
		ServerOutputController response = manager.listen();
		return response;
	}

}
