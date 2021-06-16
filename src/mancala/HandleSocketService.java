package mancala;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class HandleSocketService extends Service<ServerOutputController> {
	
	private SocketManager manager;
	
	public HandleSocketService(SocketManager manager) {
		super();
		this.manager = manager;
	}

	@Override
	protected Task<ServerOutputController> createTask() {
		return new Task<ServerOutputController>() {

			@Override
			protected ServerOutputController call() throws Exception {
				ServerOutputController response = manager.listen();
				return response;
			}
		};
	}
}
