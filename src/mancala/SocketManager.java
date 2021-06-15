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
		System.out.println("Listen");
		ServerOutputController response = new ServerOutputController(this.input.nextLine());
		System.out.println("Listened");
		return response;
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
