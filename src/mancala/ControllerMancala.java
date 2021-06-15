package mancala;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;

public class ControllerMancala {
	@FXML
	private VBox hole0, hole1, hole2, hole3, hole4, hole5, hole6, hole7, hole8, hole9, hole10, hole11;
	
	private SocketManager manager;
	
	@FXML
	public void initialize() {
		
		Platform.runLater(() -> {
		});
	}

	public SocketManager getManager() {
		return manager;
	}

	public void setManager(SocketManager manager) {
		this.manager = manager;
	}
	
}
