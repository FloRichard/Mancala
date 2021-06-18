package mancala;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class SocketManager {
	private Socket socket;
	private PrintWriter output;
	private Scanner input;

	public SocketManager(Socket socket) {
		this.socket = socket;
		try {
			this.output = new PrintWriter(socket.getOutputStream(), true);
			this.input = new Scanner(socket.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public ServerOutputController listen() {
		ServerOutputController response = new ServerOutputController(this.input.next());
		return response;
	}
	public void sendUsername(String username) {
		this.output.println("{\"type\":\"name\",\"value\":\""+username+"\"}");
	}
	
	public void sendMove(String index) {
		this.output.println("{\"type\":\"move\",\"index\":\""+index+"\"}");
	}
	
	public void sendConfirm(String target) {
		this.output.println("{\"type\":\"confirmation\",\"action\":\""+target+"\"}");
	}

	public void sendContinue() {
		this.output.println("{\"type\":\"endRoundConfirmation\"}");
	}
	
	public void sendNewGame() {
		this.output.println("{\"type\":\"new\"}");
	}

	public void sendSurrend() {
		this.output.println("{\"type\":\"surrend\"}");
	}
	
	public void sendReset() {
		this.output.println("{\"type\":\"reset\"}");
	}

	public void sendDifficulty(String difficulty) {
		this.output.println("{\"type\":\"difficulty\",\"value\":\""+difficulty+"\"}");
	}
	
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
