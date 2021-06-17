package mancala;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.web.WebView;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
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
	
	private MediaPlayer a;
	
	private boolean hoverIsEnabled=false, showNumbersIsEnabled=false, isMusicEnabled=false, isSoundEnabled=false;
	
	private MediaPlayer applause, cancel, confirm, lose, seed, win; 

	@FXML
	public void initialize() {
		
		List<VBox> holes = new ArrayList<VBox>();
		Collections.addAll(holes, hole0, hole1, hole2, hole3, hole4, hole5, hole6, hole7, hole8, hole9, hole10, hole11);
		setupLists(holes);
		
		info.textProperty().bind(I18N.createStringBinding("info.waiting"));
		
		HandleSocketService socketHandler = new HandleSocketService(manager);
		
		loadMusicAndSounds();
		
		Platform.runLater(() -> {
			initializeHandlersListeners(socketHandler);
			socketHandler.start();
		});
	}

	private void loadMusicAndSounds() {
		//Load music
		URL resource = getClass().getClassLoader().getResource("music.mp3");
		a = new MediaPlayer(new Media(resource.toString()));

		//Make the music looping
		a.setOnEndOfMedia(new Runnable() {
			public void run() {
				a.seek(Duration.ZERO);
			}
		});
		a.setVolume(0.05);
		
		//Load sound effects
		resource = getClass().getClassLoader().getResource("applause.wav");
		applause = new MediaPlayer(new Media(resource.toString()));
		resource = getClass().getClassLoader().getResource("cancel.wav");
		cancel = new MediaPlayer(new Media(resource.toString()));
		resource = getClass().getClassLoader().getResource("confirm.wav");
		confirm = new MediaPlayer(new Media(resource.toString()));
		resource = getClass().getClassLoader().getResource("lose.wav");
		lose = new MediaPlayer(new Media(resource.toString()));
		resource = getClass().getClassLoader().getResource("seed.wav");
		seed = new MediaPlayer(new Media(resource.toString()));
		resource = getClass().getClassLoader().getResource("win.wav");
		win = new MediaPlayer(new Media(resource.toString()));
	}

	public void initializeHandlersListeners(HandleSocketService socketHandler) {
		//Ask for confirmation when closing window
		info.getScene().getWindow().setOnCloseRequest(new EventHandler<WindowEvent>() {

			@Override
			public void handle(WindowEvent event) {
				Alert alert = new Alert(AlertType.CONFIRMATION);
				alert.setTitle(I18N.get("quit.title"));
				alert.setHeaderText(I18N.get("quit.header"));
		        alert.setContentText(I18N.get("quit.content"));
		        
		        Optional<ButtonType> option = alert.showAndWait();
		        if (option.get() == null) {
		        	 event.consume();
		         } else if (option.get() == ButtonType.OK) {
		        	 System.exit(0);
		         } else if (option.get() == ButtonType.CANCEL) {
		        	 event.consume();
		         }
		         else
		        	 event.consume();
			}
		});
		
		//Every time the client receives something, restarts the service to be ready to receive something else
		socketHandler.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				handleResponse((ServerOutputController) event.getSource().getValue());
				socketHandler.restart();
			}
		});
		
		//Display number of seeds if a hole contains more than 10 seeds, because graphically only 10 seeds can be displayed
		for (Label label: holesCount) {
			label.textProperty().addListener(new ChangeListener<String>() {
	            @Override
	            public void changed(ObservableValue<? extends String> ov, String t, String t1) {
	            	if(Integer.parseInt(t1)>10) 
	            		label.setVisible(true);
	            	if(Integer.parseInt(t1)<11 && !showNumbersIsEnabled)
	            		label.setVisible(false);
	            }
	        });
		}
		
		//Display number of seeds if a granary contains more than 23 seeds, because graphically only 23 seeds can be displayed
		((Label)rightGranary.getChildren().get(1)).textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov, String t, String t1) {
            	if(Integer.parseInt(t1)>23)
            		((Label)rightGranary.getChildren().get(1)).setVisible(true);
            	if(Integer.parseInt(t1)<24 && !showNumbersIsEnabled)
            		((Label)rightGranary.getChildren().get(1)).setVisible(false);
            }
        });
		
		// Two handlers to display number of seeds while entering a hole
		((Label)rightGranary.getChildren().get(1)).addEventHandler(MouseEvent.MOUSE_ENTERED, new EventHandler<MouseEvent>() {
		     @Override
		     public void handle(MouseEvent event) {
		         if(hoverIsEnabled && !showNumbersIsEnabled)
		        	 ((Label)rightGranary.getChildren().get(1)).setVisible(true);
		     }
		});
		
		((Label)rightGranary.getChildren().get(1)).addEventHandler(MouseEvent.MOUSE_EXITED, new EventHandler<MouseEvent>() {
		     @Override
		     public void handle(MouseEvent event) {
		         if(hoverIsEnabled && !showNumbersIsEnabled)
		        	 ((Label)rightGranary.getChildren().get(1)).setVisible(false);
		     }
		});
		
		//Display number of seeds if a granary contains more than 23 seeds, because graphically only 23 seeds can be displayed
		((Label)leftGranary.getChildren().get(0)).textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov, String t, String t1) {
            	if(Integer.parseInt(t1)>23)
            		((Label)leftGranary.getChildren().get(0)).setVisible(true);
            	if(Integer.parseInt(t1)<24 && !showNumbersIsEnabled)
            		((Label)leftGranary.getChildren().get(0)).setVisible(false);
            }
        });
		
		// Two handlers to display number of seeds while entering a hole
		((Label)leftGranary.getChildren().get(0)).addEventHandler(MouseEvent.MOUSE_ENTERED, new EventHandler<MouseEvent>() {
		     @Override
		     public void handle(MouseEvent event) {
		         if(hoverIsEnabled && !showNumbersIsEnabled)
		        	 ((Label)leftGranary.getChildren().get(0)).setVisible(true);
		     }
		});
		
		((Label)leftGranary.getChildren().get(0)).addEventHandler(MouseEvent.MOUSE_EXITED, new EventHandler<MouseEvent>() {
		     @Override
		     public void handle(MouseEvent event) {
		         if(hoverIsEnabled && !showNumbersIsEnabled)
		        	 ((Label)leftGranary.getChildren().get(0)).setVisible(false);
		     }
		});
		
		for (int i=0;i<holesPane.size();i++) {
			int index = i;//So the handlers can scope i
			// Handler to send a move while clicking on a hole
			holesPane.get(i).addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			     @Override
			     public void handle(MouseEvent event) {
			         if(isYourTurn) {
			        	 if(isSoundEnabled) {
			        		 seed.setStartTime(Duration.ZERO);
			        		 seed.seek(Duration.ZERO);
			        		 seed.play(); 
			        	 }
		        		 manager.sendMove(holesPane.get(index).getId());
			         }
			     }
			});
			// Two handlers to display number of seeds while entering a hole
			holesPane.get(i).addEventHandler(MouseEvent.MOUSE_ENTERED, new EventHandler<MouseEvent>() {
			     @Override
			     public void handle(MouseEvent event) {
			         if(hoverIsEnabled && !showNumbersIsEnabled)
			        	 holesCount.get(index).setVisible(true);
			     }
			});
			holesPane.get(i).addEventHandler(MouseEvent.MOUSE_EXITED, new EventHandler<MouseEvent>() {
			     @Override
			     public void handle(MouseEvent event) {
			         if(hoverIsEnabled && !showNumbersIsEnabled)
			        	 holesCount.get(index).setVisible(false);
			     }
			});
		}
	}

	public void handleResponse(ServerOutputController response) {
		System.out.println(response.getRawJSONOutput());
		if(response.isInfo() && response.getInfoValue().contains("round")) {
			info.textProperty().bind(I18N.createStringBinding(response.getInfoValue()));
			Alert alert = new Alert(AlertType.INFORMATION);
			if(response.getInfoValue().contains("win")) {
				alert.setTitle(I18N.get("info.win.greets"));
				if(isSoundEnabled) {
					win.setStartTime(Duration.ZERO);
	        		win.seek(Duration.ZERO);
					win.play();
				}
			}
			else {
				alert.setTitle(I18N.get("info.lose.greets"));
				if(isSoundEnabled) {
					lose.setStartTime(Duration.ZERO);
	        		lose.seek(Duration.ZERO);
					lose.play();
				}
			}
	        alert.setHeaderText(I18N.get(response.getInfoValue()));
	        alert.setContentText(I18N.get("info.next"));
	        alert.showAndWait();
	        manager.sendContinue();
		}
		if(response.isInfo() && response.getInfoValue().contains("game")) {
			if(isSoundEnabled) {
				applause.setStartTime(Duration.ZERO);
       		 	applause.seek(Duration.ZERO);
				applause.play();
			}
			info.textProperty().bind(I18N.createStringBinding(response.getInfoValue()));
			Alert alert = new Alert(AlertType.CONFIRMATION);
			if(response.getInfoValue().contains("win"))
				alert.setTitle(I18N.get("info.win.greets"));
			else
				alert.setTitle(I18N.get("info.lose.greets"));
			alert.setHeaderText(I18N.get(response.getInfoValue()));
	        alert.setContentText(I18N.get("info.end"));
	        
	        Optional<ButtonType> option = alert.showAndWait();
	        if (option.get() == null) {
	            System.exit(0);
	         } else if (option.get() == ButtonType.OK) {
	            manager.sendNewGame();
	         } else if (option.get() == ButtonType.CANCEL) {
	        	 System.exit(0);
	         }
	         else
	        	 System.exit(0);
		}
		if(response.isError()) {
			error.textProperty().bind(I18N.createStringBinding(response.getErrorValue()));
			error.getStyleClass().add("error");
			if(response.getErrorValue().contains("disconnection"))
				info.textProperty().bind(I18N.createStringBinding("info.waiting"));
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
			if(isBeginning)
				isYourTurn=true;
			else
				isYourTurn=false;
			this.playerNumber=response.getPlayerNumber();
			info.textProperty().bind(I18N.createStringBinding("info.ready"));
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
			
			for (Node seed : seeds) {
				if(seed instanceof ImageView) {
					seed.getStyleClass().add("seed");
					seed.setVisible(false);
				}
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
		for(int j=0; j<=Integer.parseInt(((Label) rightGranary.getChildren().get(1)).getText()) && j<=23; j++) {
			if(children.get(j) instanceof ImageView && j<23) {
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
		for(int j=0; j<=Integer.parseInt(((Label) leftGranary.getChildren().get(0)).getText()) && j<=23; j++) {
			if(children.get(j) instanceof ImageView ) {
				children.get(j).setVisible(true);
			}
		}
		for(int j=23;j>Integer.parseInt(((Label) leftGranary.getChildren().get(0)).getText());j--) {
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
		        if(isSoundEnabled) {
		        	confirm.setStartTime(Duration.ZERO);
		        	confirm.seek(Duration.ZERO);
		        	confirm.play();
		        }
		        toggleButtonsVisibility();
		    }
		});
		button2.setOnAction(new EventHandler<ActionEvent>() {
		    @Override public void handle(ActionEvent e) {
		        manager.sendConfirm("abort");
		        error.textProperty().bind(I18N.createStringBinding("empty"));
		        if(isSoundEnabled) {
		        	cancel.setStartTime(Duration.ZERO);
		        	cancel.seek(Duration.ZERO);
		        	cancel.play();
		        }
		        toggleButtonsVisibility();
		    }
		});
		toggleButtonsVisibility();
	}
	
	public void toggleButtonsVisibility() {
		button1.setVisible(!button1.isVisible());
		button2.setVisible(!button2.isVisible());
	}

	public void showNumbers(ActionEvent event) {
		for (Label label : holesCount) {
			if(Integer.parseInt(label.getText())<11)
				label.setVisible(!label.isVisible());
		}
		if(Integer.parseInt(((Label)rightGranary.getChildren().get(1)).getText())<24)
			((Label)rightGranary.getChildren().get(1)).setVisible(!((Label)rightGranary.getChildren().get(1)).isVisible());
		if(Integer.parseInt(((Label)leftGranary.getChildren().get(0)).getText())<24)
			((Label)leftGranary.getChildren().get(0)).setVisible(!((Label)leftGranary.getChildren().get(0)).isVisible());
		showNumbersIsEnabled=!showNumbersIsEnabled;
	}
	
	public void enableHover() {
		hoverIsEnabled=!hoverIsEnabled;
	}
	
	public void toggleMusic() {
		if(isMusicEnabled)
			  a.pause();
		else
			  a.play();
		isMusicEnabled=!isMusicEnabled;
	}
	
	public void toggleSound() {
		isSoundEnabled=!isSoundEnabled;
	}
	
	public void displayRules() {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle(I18N.get("rules.title"));
        alert.setHeaderText(I18N.get("rules.header"));
        WebView webView = new WebView();
        webView.getEngine().loadContent(I18N.get("rules.content"));
        webView.setPrefSize(500, 600);
        alert.getDialogPane().setContent(webView);;
        alert.showAndWait();
	}
	
	public void surrendRound() {
		manager.sendSurrend();
	}
	
	public void newMatch() {
		manager.sendNewGame();
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
