package mancala;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
	private VBox hole0, hole1, hole2, hole3, hole4, hole5, hole6, hole7, hole8, hole9, hole10, hole11, rightGranary, leftGranary;
	
	@FXML
	private Label info;
	
	@FXML
	private Label error;
	
	@FXML
	private Label score;
	
	@FXML 
	private Button button1, button2;
	
	private List<Label> holesCount = new ArrayList<Label>();
	
	private List<StackPane> holesPane = new ArrayList<StackPane>();
	
	private SocketManager manager;
	
	private int playerNumber;
	
	private boolean isBeginning;
	
	private boolean isYourTurn;

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
		
		//Display number of seeds if a hole contains more than 10 seeds
		for (Label label: holesCount) {
			label.textProperty().addListener(new ChangeListener<String>() {
	            @Override
	            public void changed(ObservableValue<? extends String> ov, String t, String t1) {
	            	if(Integer.parseInt(t1)>10) {
	            		label.setVisible(true);
	            	}         		
	            }
	        });
		}
		
		((Label)rightGranary.getChildren().get(1)).textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov, String t, String t1) {
            	if(Integer.parseInt(t1)>23) {
            		((Label)rightGranary.getChildren().get(1)).setVisible(true);
            	}         		
            }
        });
		
		((Label)leftGranary.getChildren().get(0)).textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov, String t, String t1) {
            	if(Integer.parseInt(t1)>23) {
            		((Label)leftGranary.getChildren().get(0)).setVisible(true);
            	}         		
            }
        });
		
		for (StackPane pane : holesPane) {
			pane.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			     @Override
			     public void handle(MouseEvent event) {
			         System.out.println("Hole clicked");
			         if(isYourTurn) {
		        		 manager.sendMove(pane.getId());
			         }
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
		}
		if(response.isBoard()) {
			if(this.playerNumber==response.getPlayerNumberTurn())
				isYourTurn=true;
			else
				isYourTurn=false;
			if(isYourTurn)
				info.textProperty().bind(I18N.createStringBinding("info.yourTurn"));
			else
				info.textProperty().bind(I18N.createStringBinding("info.notYourTurn"));
			
			int seeds[] = response.getSeeds();
			if(this.playerNumber==1) {
				for (int i=0;i<seeds.length;i++) {
					holesCount.get(i).setText(String.valueOf(seeds[i]));
				}
			}
			//We reverse the side of the board for player 2
			else {
				for (int i=0;i<6;i++) {
					holesCount.get(i).setText(String.valueOf(seeds[6+i]));
				}
				for (int i=6;i<seeds.length;i++) {
					holesCount.get(i).setText(String.valueOf(seeds[i-6]));
				}
			}
			if(this.playerNumber==1) {
				((Label) rightGranary.getChildren().get(1)).setText(String.valueOf(response.getPlayerOneGranaryCount()));
				((Label) leftGranary.getChildren().get(0)).setText(String.valueOf(response.getPlayerTwoGranaryCount()));
			}
			else {
				((Label) rightGranary.getChildren().get(1)).setText(String.valueOf(response.getPlayerTwoGranaryCount()));
				((Label) leftGranary.getChildren().get(0)).setText(String.valueOf(response.getPlayerOneGranaryCount()));
			}
			if(this.playerNumber==1) 
				score.textProperty().bind(I18N.createStringBinding("score",response.getPlayerOneScore(),response.getPlayerTwoScore()));
			else
				score.textProperty().bind(I18N.createStringBinding("score",response.getPlayerTwoScore(),response.getPlayerOneScore()));
			
			updateSeeds();
			
			if(response.waitsForConfirmation()) {
		         error.textProperty().bind(I18N.createStringBinding("info.move.confirm"));
		         showConfirmButtons();
			}
		}
		if(response.isInit()) {
			this.isBeginning=response.isBeginning();
			this.playerNumber=response.getPlayerNumber();
			if(isBeginning()) {
				info.textProperty().bind(I18N.createStringBinding("info.yourTurn"));
				isYourTurn=true;
			}
			else {
				info.textProperty().bind(I18N.createStringBinding("info.notYourTurn"));
				isYourTurn=false;
			}
			//Change id for player 2 so that the board is reversed
			if(playerNumber==2) {
				for(int i=0; i<holesPane.size(); i++) {
					if(i<6) {
						holesPane.get(i).setId(String.valueOf(6+i));
					}
					else {
						holesPane.get(i).setId(String.valueOf(i-6));
					}
				}
			}
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
			
			int cnt=0;
			for (Node seed : seeds) {
				seed.getStyleClass().add("seed");
				if(cnt<5)
					seed.setVisible(true);
				else
					seed.setVisible(false);
				cnt++;
			}
			
			holesPane.add(stackPane);
		}
		
		StackPane child = (StackPane) rightGranary.getChildren().get(0);
		ObservableList<Node> children = child.getChildren();
		for(int j=0; j<24; j++) {
			if(children.get(j) instanceof ImageView) {
				children.get(j).getStyleClass().add("seed");
				children.get(j).setVisible(false);
			}
		}
		child = (StackPane) leftGranary.getChildren().get(1);
		children = child.getChildren();
		for(int j=0; j<24; j++) {
			if(children.get(j) instanceof ImageView) {
				children.get(j).getStyleClass().add("seed");
				children.get(j).setVisible(false);
			}
		}
	}
	
	public void showNumbers(ActionEvent event) {
		for (Label label : holesCount) {
			label.setVisible(!label.isVisible());
		}
		((Label)rightGranary.getChildren().get(1)).setVisible(true);
		((Label)leftGranary.getChildren().get(0)).setVisible(true);
	}
	
	public void updateSeeds() {
		for (int i=0;i<holesPane.size();i++) {
			ObservableList<Node> children = holesPane.get(i).getChildren();
			int nbSeeds = Integer.parseInt(holesCount.get(i).getText());
			for(int j=0; j<=nbSeeds && j<=10; j++) {
				if(children.get(j) instanceof ImageView) {
					children.get(j).setVisible(true);
				}
			}
			for(int j=10;j>nbSeeds;j--) {
				if(children.get(j) instanceof ImageView) {
					children.get(j).setVisible(false);
				}
			}
		}
		StackPane child = (StackPane) rightGranary.getChildren().get(0);
		ObservableList<Node> children = child.getChildren();
		for(int j=0; j<=Integer.parseInt(((Label) rightGranary.getChildren().get(1)).getText()); j++) {
			if(children.get(j) instanceof ImageView && j<=23) {
				children.get(j).setVisible(true);
			}
		}
		for(int j=23;j>Integer.parseInt(((Label) rightGranary.getChildren().get(1)).getText());j--) {
			if(children.get(j) instanceof ImageView) {
				children.get(j).setVisible(false);
			}
		}
		child = (StackPane) leftGranary.getChildren().get(1);
		children = child.getChildren();
		for(int j=0; j<=Integer.parseInt(((Label) leftGranary.getChildren().get(0)).getText()); j++) {
			if(children.get(j) instanceof ImageView && j<=23) {
				children.get(j).setVisible(true);
			}
		}
		for(int j=23;j>Integer.parseInt(((Label) leftGranary.getChildren().get(1)).getText());j--) {
			if(children.get(j) instanceof ImageView) {
				children.get(j).setVisible(false);
			}
		}
	}
	
	public void showConfirmButtons() {
		button1.textProperty().bind(I18N.createStringBinding("button.confirm.yes"));
		button2.textProperty().bind(I18N.createStringBinding("button.confirm.cancel"));
		
		button1.setOnAction(new EventHandler<ActionEvent>() {
		    @Override public void handle(ActionEvent e) {
		        manager.sendConfirm("confirm");
		        error.textProperty().bind(I18N.createStringBinding("empty"));
		        toggleButtonsVisibility();
		    }
		});
		button2.setOnAction(new EventHandler<ActionEvent>() {
		    @Override public void handle(ActionEvent e) {
		        manager.sendConfirm("abort");
		        error.textProperty().bind(I18N.createStringBinding("empty"));
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
