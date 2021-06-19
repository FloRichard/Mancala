package connection;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Locale;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.text.Text;
import mancala.Interface;
import utils.I18N;

/**
 * Controller of the {@link Connection} interface.
 * @author Julien MONTEIL
 * @author Florian RICHARD
 *
 */
public class ControllerConnection {
	
	private final Text warning = new Text();
	private final Text warningText = new Text();
	private final Text warningHost = new Text();
	
	@FXML 
	private TextField username;
	
	@FXML
	private TextField address;
	
	@FXML
	private TextField port;
	
	@FXML 
	private ToggleGroup languageChoice;
	
	@FXML
	private Label addressLabel;
	
	@FXML
	private Label portLabel;
	
	@FXML
	private Label languageLabel;
	
	@FXML 
	private Label usernameLabel;
	
	@FXML
	private Label title;
	
	@FXML
	private RadioButton fr;
	
	@FXML
	private RadioButton en;
	
	@FXML
	private Button connection;
	
	/**
	 * Initialize method called when the fxml file is loaded. Used to initialize handlers and listeners.
	 * Also bind the different texts to allow the translation.
	 */
	@FXML
	public void initialize() {
		//Only allows digits and dots for the address
		address.textProperty().addListener(new ChangeListener<String>() {
		    @Override
		    public void changed(ObservableValue<? extends String> observable, String oldValue, 
		        String newValue) {
		        if (!newValue.matches("\\d*")) {
		            address.setText(newValue.replaceAll("[^\\d.]", ""));
		        }
		    }
		});
		// Only allows digits for the port 
		port.textProperty().addListener(new ChangeListener<String>() {
		    @Override
		    public void changed(ObservableValue<? extends String> observable, String oldValue, 
		        String newValue) {
		        if (!newValue.matches("\\d*")) {
		            port.setText(newValue.replaceAll("[^\\d]", ""));
		        }
		    }
		});
		// Call the method to change local language
		languageChoice.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
	        @Override
	        public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
	            if(oldValue!=newValue){
	                changeLanguage(newValue);
	            }
	        }
	    });
		//Forbid spaces and slashes
		username.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, 
			        String newValue) {
				if(newValue.contains("\\")) {
					newValue = newValue.replace("\\","");
					username.setText(newValue);
				}
				if(newValue.contains(" ")) {
					newValue = newValue.replace(" ","");
					username.setText(newValue);
				}
				if(newValue.contains("/")) {
					newValue = newValue.replace("/","");
					username.setText(newValue);
				}
			}
	    });
		//Bind text values to bind them to the chosen language
		usernameLabel.textProperty().bind(I18N.createStringBinding("label.usernameLabel"));
		addressLabel.textProperty().bind(I18N.createStringBinding("label.addressLabel"));
		portLabel.textProperty().bind(I18N.createStringBinding("label.portLabel"));
		languageLabel.textProperty().bind(I18N.createStringBinding("label.languageLabel"));
		title.textProperty().bind(I18N.createStringBinding("label.title"));
		fr.textProperty().bind(I18N.createStringBinding("radiobutton.fr"));
		en.textProperty().bind(I18N.createStringBinding("radiobutton.en"));
		connection.textProperty().bind(I18N.createStringBinding("button.connection"));
		warning.textProperty().bind(I18N.createStringBinding("alert.warning"));
		warningText.textProperty().bind(I18N.createStringBinding("alert.warning.text"));
		warningHost.textProperty().bind(I18N.createStringBinding("alert.warning.unknownhost"));
	}
	
	/**
	 * Method called while clicking on the connection button.
	 * Checks if all the fields are filled, if not warning alert is displayed to ask the user to fill them.
	 * @param event
	 */
	public void submitConnection(ActionEvent event) {
		if(address.getText().equals("") || port.getText().equals("")) {
			Alert alert = new Alert(AlertType.WARNING);
	        alert.setTitle(warning.getText());
	        alert.setContentText(warningText.getText());
	        alert.showAndWait();
		}
		else if(username.getText().equals("")) {
			Alert alert = new Alert(AlertType.WARNING);
	        alert.setTitle(warning.getText());
	        alert.setContentText(I18N.get("alert.username.content"));
	        alert.showAndWait();
		}
		else {
	        try {
	        	// If the socket connects successfully, the game window is started
	        	Socket socket = new Socket(address.getText(),Integer.parseInt(port.getText()));
	        	Interface game = new Interface(socket,I18N.getLocale(),username.getText());
	        	game.start();
	            ((Node)(event.getSource())).getScene().getWindow().hide();
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (UnknownHostException e) {
				e.printStackTrace();
				Alert alert = new Alert(AlertType.WARNING);
		        alert.setTitle(warning.getText());
		        alert.setContentText(warningHost.getText());
		        alert.showAndWait();
			} catch (IOException e) {
				e.printStackTrace();
				Alert alert = new Alert(AlertType.WARNING);
		        alert.setTitle(warning.getText());
		        alert.setContentText(warningHost.getText());
		        alert.showAndWait();
			}
		}
	}
	
	/**
	 * Change the value of the selected language to translate all fields
	 * @param value new selected language
	 */
	public void changeLanguage(Toggle value) {
		RadioButton language = (RadioButton) value.getToggleGroup().getSelectedToggle();
		if(language.getId().equals("en")) {
			I18N.setLocale(Locale.ENGLISH);
		}
		else {
			I18N.setLocale(Locale.FRENCH);
		}
	}
}
