package mancala;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

/**
 * 
 * This class extends {@link Service}. It is used to create tasks
 * In this case, a task is created each time the client waits a request from the server.
 * And in order to make the interface not frozen, a thread is created.
 * @author Julien MONTEIL
 * @author Florian RICHARD
 *
 */
public class HandleSocketService extends Service<ServerOutputController> {
	
	private SocketManager manager;
	
	/**
	 * HandleSocketService constructor containing the {@link SocketManager} instance.
	 * @param manager
	 */
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
