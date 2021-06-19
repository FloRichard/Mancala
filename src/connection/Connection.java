package connection;
	
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import utils.I18N;


/**
 * Class which starts the connection interface.
 * @author Julien MONTEIL
 * @author Florian RICHARD
 */
public class Connection extends Application {
	@Override
	public void start(Stage stage) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("connection.fxml"));
			
			Parent root = loader.load();
			
			Scene scene = new Scene(root);
			stage.setResizable(false);
			stage.sizeToScene();
			stage.titleProperty().bind(I18N.createStringBinding("window.title.connection"));
			stage.setScene(scene);
			stage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
