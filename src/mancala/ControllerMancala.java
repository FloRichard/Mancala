package mancala;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import utils.I18N;

public class ControllerMancala {
	
	@FXML
	private VBox hole0, hole1, hole2, hole3, hole4, hole5, hole6, hole7, hole8, hole9, hole10, hole11, player1Granary, player2Granary;
	
	@FXML
	private Label info;
	
	@FXML
	private Label error;
	
	@FXML 
	private Button button1, button2;
	
	private List<Label> holesCount = new ArrayList<Label>();
	
	private List<StackPane> holesPane = new ArrayList<StackPane>();
	
	private SocketManager manager;
	
	private int playerNumber;
	
	private boolean isBeginning;

	@FXML
	public void initialize() {
		
		List<VBox> holes = new ArrayList<VBox>();
		Collections.addAll(holes, hole0, hole1, hole2, hole3, hole4, hole5, hole6, hole7, hole8, hole9, hole10, hole11);
		setupLists(holes);
		
		info.textProperty().bind(I18N.createStringBinding("info.waiting"));
		
		HandleSocketService socketHandler = new HandleSocketService(manager);
		
		socketHandler.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			
			@Override
			public void handle(WorkerStateEvent event) {
				handleResponse((ServerOutputController) event.getSource().getValue());
				socketHandler.restart();
			}
		});
		
		for (StackPane pane : holesPane) {
			pane.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			     @Override
			     public void handle(MouseEvent event) {
			         System.out.println("Hole clicked");
			         manager.sendMove(pane.getId());
			         showConfirmButtons();
			         event.consume();
			     }
			});
		}
		
		Platform.runLater(() -> {
			socketHandler.start();
		});
	}

	public void handleResponse(ServerOutputController response) {
		System.out.println(response.getRawJSONOutput());
		if(response.isInfo()) {
			info.textProperty().bind(I18N.createStringBinding(response.getInfoValue()));
		}
		if(response.isError()) {
			error.textProperty().bind(I18N.createStringBinding(response.getErrorValue()));
			error.getStyleClass().add("error");
			toggleButtonsVisibility();
		}
		if(response.isBoard()) {
			int seeds[] = response.getSeeds();
			for (int i=0;i<seeds.length;i++) {
				holesCount.get(i).setText(String.valueOf(seeds[i]));
			}
			((Label) player1Granary.getChildren().get(1)).setText(String.valueOf(response.getPlayerOneGranaryCount()));
			((Label) player2Granary.getChildren().get(0)).setText(String.valueOf(response.getPlayerTwoGranaryCount()));
			updateSeeds();
			error.textProperty().bind(I18N.createStringBinding("info.move.confirm"));
		}
		if(response.isInit()) {
			this.isBeginning=response.isBeginning();
			this.playerNumber=response.getPlayerNumber();
			if(isBeginning())
				info.textProperty().bind(I18N.createStringBinding("info.yourTurn"));
			else
				info.textProperty().bind(I18N.createStringBinding("info.notYourTurn"));
		}
	}

	public SocketManager getManager() {
		return manager;
	}

	public void setManager(SocketManager manager) {
		this.manager = manager;
	}
	
	public void setupLists(List<VBox> holes) {
		for (VBox hole : holes) {
			
			holesCount.add((Label) hole.getChildren().get(0));
			StackPane stackPane = (StackPane) hole.getChildren().get(1);
			
			ObservableList<Node> seeds = stackPane.getChildren();
			
			for(int i=0; i<5; i++) {
				if(seeds.get(i) instanceof ImageView) {
					seeds.get(i).getStyleClass().add("seed");
				}
			}
			
			holesPane.add(stackPane);
		}
	}
	
	public void showNumbers(ActionEvent event) {
		for (Label label : holesCount) {
			label.setVisible(!label.isVisible());
		}
	}
	
	public void updateSeeds() {
		for (int i=0;i<holesPane.size();i++) {
			ObservableList<Node> children = holesPane.get(i).getChildren();
			for(int j=0; j<=Integer.parseInt(holesCount.get(i).getText()); j++) {
				if(children.get(j) instanceof ImageView) {
					children.get(j).getStyleClass().add("seed");
				}
			}	
		}
		StackPane child = (StackPane) player1Granary.getChildren().get(0);
		ObservableList<Node> children = child.getChildren();
		for(int j=0; j<=Integer.parseInt(((Label) player1Granary.getChildren().get(1)).getText()); j++) {
			if(children.get(j) instanceof ImageView) {
				children.get(j).getStyleClass().add("seed");
			}
		}
		child = (StackPane) player2Granary.getChildren().get(1);
		children = child.getChildren();
		for(int j=0; j<=Integer.parseInt(((Label) player2Granary.getChildren().get(0)).getText()); j++) {
			if(children.get(j) instanceof ImageView) {
				children.get(j).getStyleClass().add("seed");
			}
		}
	}
	
	public void showConfirmButtons() {
		button1.textProperty().bind(I18N.createStringBinding("button.confirm.yes"));
		button2.textProperty().bind(I18N.createStringBinding("button.confirm.cancel"));
		
		button1.setOnAction(new EventHandler<ActionEvent>() {
		    @Override public void handle(ActionEvent e) {
		        manager.sendConfirm("confirm");
		        toggleButtonsVisibility();
		    }
		});
		button2.setOnAction(new EventHandler<ActionEvent>() {
		    @Override public void handle(ActionEvent e) {
		        manager.sendConfirm("abort");
		        toggleButtonsVisibility();
		    }
		});
		toggleButtonsVisibility();
	}
	
	public void toggleButtonsVisibility() {
		button1.setVisible(!button1.isVisible());
		button2.setVisible(!button2.isVisible());
	}

	public Label getInfo() {
		return info;
	}

	public void setInfo(Label info) {
		this.info = info;
	}

	public Label getError() {
		return error;
	}

	public void setError(Label error) {
		this.error = error;
	}

	public int getPlayerNumber() {
		return playerNumber;
	}

	public void setPlayerNumber(int playerNumber) {
		this.playerNumber = playerNumber;
	}

	public boolean isBeginning() {
		return isBeginning;
	}

	public void setBeginning(boolean isBeginning) {
		this.isBeginning = isBeginning;
	}
	
	
}
