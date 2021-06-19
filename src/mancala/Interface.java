package mancala;

import java.net.Socket;
import java.util.Locale;

import connection.Connection;
import connection.ControllerConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import utils.I18N;


/**
 * The class corresponding to the graphic interface of the game
 * @author Julien MONTEIL
 * @author Florian RICHARD
 *
 */
public class Interface {
	
	private Socket socket;
	private String username;
	
	/**
	 * Interface constructor instantiated in the {@link ControllerConnection}
	 * @param socket created in the connection window
	 * @param lang chosen in the connection window
	 * @param username input in the connection window
	 */
	public Interface(Socket socket, Locale lang, String username) {
		this.socket = socket;
		this.username = username;
		I18N.setLocale(lang);
	}
	
	/**
	 * Method used the same way as a class which could extend {@link Application} 
	 * Since the Mancala application starts from the {@link Connection} application.
	 * It is not possible to start a new JavaFX application.
	 * So we load a new fxml file.
	 */
	public void start() {
		try {
			ControllerMancala controller = new ControllerMancala();
			controller.setManager(new SocketManager(this.socket));
			controller.setUsername(username);
			
			FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("mancala.fxml"));
			loader.setController(controller);
			
			Parent root = loader.load();
			
			Scene scene = new Scene(root);
			
			scene.getStylesheets().add("style.css");
			
			//Set the background image for the board and make it rounded
			Image im = new Image("marbre.jpg", 1300, 414, false, false); // Resizing
			ImageView imView = new ImageView(im);
			
			Rectangle rectangle = new Rectangle(0, 0, 1300, 414);
			rectangle.setArcWidth(30.0);   // Corner radius
			rectangle.setArcHeight(30.0);
			rectangle.setStyle("-fx-border-radius: 30;-fx-border-color: black;");
			
			imView.setClip(rectangle);
			
			SnapshotParameters parameters = new SnapshotParameters();
			parameters.setFill(Color.TRANSPARENT);
			WritableImage image = imView.snapshot(parameters, null);
			
			imView.setClip(null);
			
			HBox board = (HBox) scene.lookup("#board");
		
			board.setBackground(new Background(new BackgroundImage(image, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, BackgroundSize.DEFAULT)));
			
			Stage stage = new Stage();
			stage.setResizable(false);
			stage.sizeToScene();
			stage.titleProperty().bind(I18N.createStringBinding("window.title.mancala"));
			stage.setScene(scene);
			stage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public Socket getSocket() {
		return socket;
	}

	public void setSocket(Socket socket) {
		this.socket = socket;
	}
	
}