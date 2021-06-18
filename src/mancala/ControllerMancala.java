package mancala;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Scanner;

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
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
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
	
	@FXML
	private MenuItem surrendRoundMenu, surrendMenu, newMatchMenu, saveMatchMenu, loadMatchMenu, aboutMancalaMenu, seeRulesMenu, changeLanguage;
	
	@FXML
	private CheckMenuItem hoverCheck, showCheck, toggleSoundCheck, toggleMusicCheck;
	
	@FXML
	private Menu awaleMenu, rulesMenu, settingsMenu, aboutMenu;
	
	private List<Label> holesCount;
	
	private List<StackPane> holesPane;
	
	private SocketManager manager;
	
	private int playerNumber;
	
	private boolean isBeginning;
	
	private boolean isYourTurn;	
	
	private MediaPlayer a;
	
	private boolean hoverIsEnabled=false, showNumbersIsEnabled=false, isMusicEnabled=false, isSoundEnabled=false;
	
	private MediaPlayer applause, cancel, confirm, lose, seed, win;
	
	private String actualBoard;
	
	private String username;
	
	@FXML
	public void initialize() {
		
		info.textProperty().bind(I18N.createStringBinding("info.waiting"));
		surrendMenu.setDisable(true);
		newMatchMenu.setDisable(true);
		surrendRoundMenu.setDisable(true);
		saveMatchMenu.setDisable(true);
		loadMatchMenu.setDisable(true);
		
		HandleSocketService socketHandler = new HandleSocketService(manager);
		
		//Every time the client receives something, restarts the service to be ready to receive something else
		socketHandler.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				handleResponse((ServerOutputController) event.getSource().getValue());
				socketHandler.restart();
			}
		});
		
		loadMusicAndSounds();
		
		Platform.runLater(() -> {
			bindMenuLanguage();
			resetGame();
			initializeHandlersListeners();
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

	public void initializeHandlersListeners() {
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
			        	 toggleClickableHoles();
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
			else if (response.getInfoValue().contains("lose")){
				alert.setTitle(I18N.get("info.lose.greets"));
				if(isSoundEnabled) {
					lose.setStartTime(Duration.ZERO);
	        		lose.seek(Duration.ZERO);
					lose.play();
				}
			}
			else {
				alert.setTitle(I18N.get("info.draw.greets"));
				if(isSoundEnabled) {
					win.setStartTime(Duration.ZERO);
	        		win.seek(Duration.ZERO);
					win.play();
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
			//Display the scoreboard of the best 100 players
			
			//Display the confirmation dialog to continue or quit
			Alert alert = new Alert(AlertType.CONFIRMATION);
			if(response.getInfoValue().contains("win"))
				alert.setTitle(I18N.get("info.win.greets"));
			else
				alert.setTitle(I18N.get("info.lose.greets"));
			alert.setHeaderText(I18N.get(response.getInfoValue()));
	        alert.setContentText(I18N.get("info.end"));
	        
	        ButtonType buttonTypeOne = new ButtonType(I18N.get("info.ok"));
			ButtonType buttonTypeTwo = new ButtonType(I18N.get("info.scores"));
			ButtonType buttonTypeCancel = new ButtonType(I18N.get("info.quit"), ButtonData.CANCEL_CLOSE);

			alert.getButtonTypes().setAll(buttonTypeOne, buttonTypeTwo, buttonTypeCancel);
	        
	        Optional<ButtonType> option = alert.showAndWait();
	        if (option.get() == null) {
	        	score.textProperty().bind(I18N.createStringBinding("empty"));
	        	manager.sendReset();
	            resetGame();
	            info.textProperty().bind(I18N.createStringBinding("info.please"));
	         } else if (option.get() == buttonTypeOne) {
	        	score.textProperty().bind(I18N.createStringBinding("empty"));
	        	manager.sendReset();
	            resetGame();
	            info.textProperty().bind(I18N.createStringBinding("info.please"));
	         } else if(option.get() == buttonTypeTwo) {
	        	 Alert score = new Alert(AlertType.INFORMATION);
	     		 score.setTitle(I18N.get("info.score.title"));
	             score.setHeaderText(I18N.get("info.score.header"));
	             WebView webView = new WebView();
	             webView.getEngine().loadContent(response.getScore());
	             webView.getEngine().setUserStyleSheetLocation(getClass().getClassLoader().getResource("style.css").toString());
	             webView.setPrefSize(500, 600);
	             score.getDialogPane().setContent(webView);
	             score.showAndWait();
	             this.score.textProperty().bind(I18N.createStringBinding("empty"));
	        	 manager.sendReset();
	             resetGame();
	             info.textProperty().bind(I18N.createStringBinding("info.please"));
			 } else if (option.get() == buttonTypeCancel) {
	        	 System.exit(0);
	         }
	         else
	        	 System.exit(0);
		}
		if(response.isError()) {
			error.textProperty().bind(I18N.createStringBinding(response.getErrorValue()));
			error.getStyleClass().add("error");
			if(response.getErrorValue().contains("disconnection"))
				info.textProperty().bind(I18N.createStringBinding("info.please"));
			else if(response.getErrorValue().contains("notYourArea") || response.getErrorValue().contains("emptyHole")
					|| response.getErrorValue().contains("notFeedingMove") || response.getErrorValue().contains("isStarving"))
				toggleClickableHoles();
		}
		if(response.isBoard()) {
			newMatchMenu.setDisable(true);
			this.actualBoard=response.getRawJSONOutput();
			if(this.playerNumber==response.getPlayerNumberTurn()) {
				surrendRoundMenu.setDisable(false);
				isYourTurn=true;
			}
			else {
				surrendRoundMenu.setDisable(true);
				isYourTurn=false;
			}
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
			resetGame();
			loadMatchMenu.setDisable(false);
			newMatchMenu.setDisable(false);
			surrendMenu.setDisable(true);
			surrendRoundMenu.setDisable(true);
			saveMatchMenu.setDisable(true);
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
			manager.sendUsername(username);
		}
	}

	public SocketManager getManager() {
		return manager;
	}

	public void setManager(SocketManager manager) {
		this.manager = manager;
	}
	
	public void setupLists(List<VBox> holes) {
		holesCount = new ArrayList<Label>();
		holesPane = new ArrayList<StackPane>();
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
		((Label) rightGranary.getChildren().get(1)).setText("0");
		ObservableList<Node> children = child.getChildren();
		for(int j=0; j<24; j++) {
			if(children.get(j) instanceof ImageView) {
				children.get(j).getStyleClass().add("seed");
				children.get(j).setVisible(false);
			}
		}
		child = (StackPane) leftGranary.getChildren().get(1);
		((Label) leftGranary.getChildren().get(0)).setText("0");
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
		        toggleClickableHoles();
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
		        toggleClickableHoles();
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
        webView.getEngine().setUserStyleSheetLocation(getClass().getClassLoader().getResource("style.css").toString());
        webView.setPrefSize(500, 600);
        alert.getDialogPane().setContent(webView);
        alert.showAndWait();
	}
	
	public void surrendRound() {
		manager.sendSurrend();
	}
	
	public void newMatch() {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle(I18N.get("alert.mode.title"));
		alert.setHeaderText(I18N.get("alert.mode.header"));
		alert.setContentText(I18N.get("alert.mode.content"));

		ButtonType buttonTypeOne = new ButtonType(I18N.get("alert.mode.easy"));
		ButtonType buttonTypeTwo = new ButtonType(I18N.get("alert.mode.normal"));
		ButtonType buttonTypeCancel = new ButtonType(I18N.get("alert.mode.cancel"), ButtonData.CANCEL_CLOSE);

		alert.getButtonTypes().setAll(buttonTypeOne, buttonTypeTwo, buttonTypeCancel);

		Optional<ButtonType> result = alert.showAndWait();
		if (result.get() == buttonTypeOne){
			manager.sendDifficulty("easy");
			manager.sendNewGame();
			surrendMenu.setDisable(false);
			saveMatchMenu.setDisable(false);
			newMatchMenu.setDisable(true);
			loadMatchMenu.setDisable(true);
		} else if (result.get() == buttonTypeTwo) {
			manager.sendDifficulty("normal");
			manager.sendNewGame();
			surrendMenu.setDisable(false);
			saveMatchMenu.setDisable(false);
			newMatchMenu.setDisable(true);
			loadMatchMenu.setDisable(true);
		}
	}
	
	public void resetGame() {
		List<VBox> holes = new ArrayList<VBox>();
		Collections.addAll(holes, hole0, hole1, hole2, hole3, hole4, hole5, hole6, hole7, hole8, hole9, hole10, hole11);
		setupLists(holes);
	}
	
	public void toggleClickableHoles() {
		for (int i=0;i<holesPane.size();i++) {
			holesPane.get(i).setDisable(!holesPane.get(i).isDisable());
		}
	}
	
	public void about() {
		Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(I18N.get("about.title"));
        alert.setHeaderText(I18N.get("about.header"));
        alert.setContentText(I18N.get("about.content"));
        alert.showAndWait();
	}
	
	public void saveGame() {
		FileChooser fileChooser = new FileChooser();
	    fileChooser.setTitle(I18N.get("save.title"));
	    FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(I18N.get("extension.game"), "*.game");
		fileChooser.getExtensionFilters().add(extFilter);
		File file = fileChooser.showSaveDialog((Stage) info.getScene().getWindow());
		try (
                BufferedReader reader = new BufferedReader(new StringReader(actualBoard));
                PrintWriter writer = new PrintWriter(new FileWriter(file));
            ) {
                reader.lines().forEach(line -> writer.println(line));
            }
		catch (IOException e) {
				e.printStackTrace();
			}
	}
	
	public void loadGame() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle(I18N.get("load.title"));
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(I18N.get("extension.game"), "*.game");
		fileChooser.getExtensionFilters().add(extFilter);
		File file = fileChooser.showOpenDialog((Stage) info.getScene().getWindow());

		try {
			Scanner myReader = new Scanner(file);
			String board = myReader.nextLine();
			manager.sendLoad(board);
			myReader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		if(file!=null) {
			loadMatchMenu.setDisable(true);
			surrendMenu.setDisable(false);
			surrendRoundMenu.setDisable(false);
			saveMatchMenu.setDisable(false);
			newMatchMenu.setDisable(true);
		}
	}
	
	public void surrendMatch() {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle(I18N.get("alert.surrend.title"));
		alert.setHeaderText(I18N.get("alert.surrend.header"));
		alert.setContentText(I18N.get("alert.surrend.content"));

		ButtonType buttonTypeOne = new ButtonType(I18N.get("alert.surrend.save"));
		ButtonType buttonTypeTwo = new ButtonType(I18N.get("alert.surrend.quit"));
		ButtonType buttonTypeCancel = new ButtonType(I18N.get("alert.surrend.cancel"), ButtonData.CANCEL_CLOSE);

		alert.getButtonTypes().setAll(buttonTypeOne, buttonTypeTwo, buttonTypeCancel);

		Optional<ButtonType> result = alert.showAndWait();
		if (result.get() == buttonTypeOne){
			saveGame();
			System.exit(0);
		} else if (result.get() == buttonTypeTwo) {
			System.exit(0);
		}
	}
	
	public void bindMenuLanguage() {
		//Bind menus
		awaleMenu.textProperty().bind(I18N.createStringBinding("menu.awale"));
		rulesMenu.textProperty().bind(I18N.createStringBinding("menu.rules"));
		settingsMenu.textProperty().bind(I18N.createStringBinding("menu.settings"));
		aboutMenu.textProperty().bind(I18N.createStringBinding("menu.about"));
		
		//Bind menu items
		loadMatchMenu.textProperty().bind(I18N.createStringBinding("menu.item.load"));
		saveMatchMenu.textProperty().bind(I18N.createStringBinding("menu.item.save"));
		newMatchMenu.textProperty().bind(I18N.createStringBinding("menu.item.newMatch"));
		surrendRoundMenu.textProperty().bind(I18N.createStringBinding("menu.item.surrend.round"));
		surrendMenu.textProperty().bind(I18N.createStringBinding("menu.item.surrend.match"));
		
		seeRulesMenu.textProperty().bind(I18N.createStringBinding("menu.item.rules"));
		
		aboutMancalaMenu.textProperty().bind(I18N.createStringBinding("menu.item.about"));
		
		changeLanguage.textProperty().bind(I18N.createStringBinding("menu.item.language"));
		
		//Bind check menu items
		hoverCheck.textProperty().bind(I18N.createStringBinding("menu.item.see.number"));
		showCheck.textProperty().bind(I18N.createStringBinding("menu.item.see.state"));
		toggleSoundCheck.textProperty().bind(I18N.createStringBinding("menu.item.sounds"));
		toggleMusicCheck.textProperty().bind(I18N.createStringBinding("menu.item.music"));
	}
	
	public void toggleLanguage() {
		if(I18N.getLocale()==Locale.FRENCH)
			I18N.setLocale(Locale.ENGLISH);
		else
			I18N.setLocale(Locale.FRENCH);
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

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
}
