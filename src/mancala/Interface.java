package mancala;

import java.net.Socket;
import java.util.Locale;

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


public class Interface {
	private Socket socket;
	
	public Interface(Socket socket, Locale lang) {
		this.socket = socket;
		I18N.setLocale(lang);
	}
	
	public void start() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("mancala.fxml"));
			Parent root = loader.load();
			
			ControllerMancala controller = loader.getController();
			controller.setManager(new SocketManager(socket));
			
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