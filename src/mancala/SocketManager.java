package mancala;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * Class managing the output and input of the socket to communicate with the server.
 * 
 * @author Julien MONTEIL
 * @author Florian RICHARD
 *
 */
public class SocketManager {
	private Socket socket;
	private PrintWriter output;
	private Scanner input;

	/**
	 * SocketManager constructor, instantiates the proper objects to send and receive requests.
	 * @param socket The socket already connected to the server.
	 */
	public SocketManager(Socket socket) {
		this.socket = socket;
		try {
			this.output = new PrintWriter(socket.getOutputStream(), true);
			this.input = new Scanner(socket.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Method which listens the server output. Handle the output with {@link ServerOutputController} class
	 * @return response, the request received from the server which has been converted to an object with accessible properties.
	 * @see ServerOutputController
	 */
	public ServerOutputController listen() {
		ServerOutputController response = new ServerOutputController(this.input.next());
		return response;
	}
	
	/**
	 * Send a request to the server containing the username of the player.
	 * @param username Player's username
	 */
	public void sendUsername(String username) {
		this.output.println("{\"type\":\"name\",\"value\":\""+username+"\"}");
	}
	
	/**
	 * Send a request to the server containing the index of the hole which the move comes from.
	 * @param index Index of the hole whose seeds need to be moved.
	 */
	public void sendMove(String index) {
		this.output.println("{\"type\":\"move\",\"index\":\""+index+"\"}");
	}
	
	/**
	 * Send a request to the server confirming or not the previous move done.
	 * @param target confirm or cancel depending on the action.
	 */
	public void sendConfirm(String target) {
		this.output.println("{\"type\":\"confirmation\",\"action\":\""+target+"\"}");
	}

	/**
	 * Send a request to the server to continue to the next round
	 */
	public void sendContinue() {
		this.output.println("{\"type\":\"endRoundConfirmation\"}");
	}
	
	/**
	 * Send a request to the server to start a new game. Only one player needs to do it.
	 */
	public void sendNewGame() {
		this.output.println("{\"type\":\"new\"}");
	}

	/**
	 * Send a request to the server to surrender a round. Can only be done on player's turn.
	 */
	public void sendSurrend() {
		this.output.println("{\"type\":\"surrend\"}");
	}
	
	/**
	 * Send a request to the server to reset the game. Done after the end of a game.
	 */
	public void sendReset() {
		this.output.println("{\"type\":\"reset\"}");
	}

	/**
	 * Send a request to the server containing the difficulty mode chosen for the incoming game.
	 * @param difficulty whether easy or normal
	 */
	public void sendDifficulty(String difficulty) {
		this.output.println("{\"type\":\"difficulty\",\"value\":\""+difficulty+"\"}");
	}
	
	/**
	 * Send a request to the server to load a particular loaded board. Following the load action.
	 * @param board The board loaded from the file chooser.
	 */
	public void sendLoad(String board) {
		board = board.replace("board", "load");
		this.output.println(board);
	}

	public Socket getSocket() {
		return socket;
	}

	public void setSocket(Socket socket) {
		this.socket = socket;
	}

	public PrintWriter getOutput() {
		return output;
	}

	public void setOutput(PrintWriter output) {
		this.output = output;
	}

	public Scanner getInput() {
		return input;
	}

	public void setInput(Scanner input) {
		this.input = input;
	}
	
}
