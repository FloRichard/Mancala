package mancala;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ServerOutputController {

	private static final String DIFFICULTY = "difficulty";
	private static final String PLAYER2_SCORE = "playerTwoScore";
	private static final String PLAYER1_SCORE = "playerOneScore";
	private static final String NEED_CONFIRMATION = "needConfirmation";
	private static final String PLAYER_NUMBER = "playerNumber";
	private static final String IS_BEGINNING = "isBeginning";
	private static final String INIT = "init";
	private static final String PLAYER_TWO_GRANARY_COUNT = "playerTwoGranaryCount";
	private static final String PLAYER_ONE_GRANARY_COUNT = "playerOneGranaryCount";
	private static final String SEEDS = "seeds";
	private static final String VALUE = "value";
	private static final String TYPE = "type";
	private static final String BOARD = "board";
	private static final String ERROR = "error";
	private static final String INFO = "info";

	private String rawJSONOutput;
	
	private boolean isInfo;
	private String infoValue;
	
	private boolean isError;
	private String errorValue;
	
	private boolean isBoard;
	private int playerNumberTurn;
	private boolean waitsForConfirmation;
	private int[] seeds;
	private int playerOneGranaryCount;
	private int playerTwoGranaryCount;
	
	private boolean isInit;
	private int playerNumber;
	private boolean isBeginning;
	private int playerOneScore;
	private int playerTwoScore;
	private String difficulty;
	
	public ServerOutputController(String rawJSONOutput) {
		this.rawJSONOutput = rawJSONOutput;
		this.isInfo = false;
		this.isError = false;
		this.isBoard = false;
		this.isInit=false;
		this.parseRawJsonInput();
	}

	public void parseRawJsonInput() {
		@SuppressWarnings("deprecation")
		JsonObject jsonObject = new JsonParser().parse(this.rawJSONOutput).getAsJsonObject();
		String type = jsonObject.get(TYPE).getAsString();
		switch (type) {
		case INIT:
			this.isInit=true;
			this.playerNumber=jsonObject.get(PLAYER_NUMBER).getAsInt();
			this.isBeginning=jsonObject.get(IS_BEGINNING).getAsBoolean();
			break;
			
		case INFO:
			this.isInfo=true;
			this.infoValue=jsonObject.get(VALUE).getAsString();
			break;
			
		case ERROR:
			this.isError=true;
			this.errorValue=jsonObject.get(VALUE).getAsString();
			break;
			
		case BOARD:
			this.isBoard=true;
			this.playerNumberTurn=jsonObject.get(PLAYER_NUMBER).getAsInt();
			this.waitsForConfirmation=jsonObject.get(NEED_CONFIRMATION).getAsBoolean();
			this.seeds = new int[12];
			JsonArray jsonArray = jsonObject.get(SEEDS).getAsJsonArray();
			
			for (int i = 0; i < seeds.length; i++) {
				seeds[i]=jsonArray.get(i).getAsInt();
			}
			
			this.playerOneGranaryCount = jsonObject.get(PLAYER_ONE_GRANARY_COUNT).getAsInt();
			this.playerTwoGranaryCount = jsonObject.get(PLAYER_TWO_GRANARY_COUNT).getAsInt();
			this.playerOneScore = jsonObject.get(PLAYER1_SCORE).getAsInt();
			this.playerTwoScore = jsonObject.get(PLAYER2_SCORE).getAsInt();
			this.difficulty= jsonObject.get(DIFFICULTY).getAsString();
			
			break;
		default:
			break;
		}
	}


	public String getRawJSONOutput() {
		return rawJSONOutput;
	}


	public void setRawJSONOutput(String rawJSONOutput) {
		this.rawJSONOutput = rawJSONOutput;
	}


	public boolean isInfo() {
		return isInfo;
	}


	public void setInfo(boolean isInfo) {
		this.isInfo = isInfo;
	}


	public String getInfoValue() {
		return infoValue;
	}


	public void setInfoValue(String infoValue) {
		this.infoValue = infoValue;
	}


	public boolean isError() {
		return isError;
	}


	public void setError(boolean isError) {
		this.isError = isError;
	}


	public String getErrorValue() {
		return errorValue;
	}


	public void setErrorValue(String errorValue) {
		this.errorValue = errorValue;
	}


	public boolean isBoard() {
		return isBoard;
	}


	public void setBoard(boolean isBoard) {
		this.isBoard = isBoard;
	}


	public int[] getSeeds() {
		return seeds;
	}


	public void setSeeds(int[] seeds) {
		this.seeds = seeds;
	}


	public int getPlayerOneGranaryCount() {
		return playerOneGranaryCount;
	}


	public void setPlayerOneGranaryCount(int playerOneGranaryCount) {
		this.playerOneGranaryCount = playerOneGranaryCount;
	}


	public int getPlayerTwoGranaryCount() {
		return playerTwoGranaryCount;
	}


	public void setPlayerTwoGranaryCount(int playerTwoGranaryCount) {
		this.playerTwoGranaryCount = playerTwoGranaryCount;
	}

	public boolean isInit() {
		return isInit;
	}

	public void setInit(boolean isInit) {
		this.isInit = isInit;
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

	public int getPlayerNumberTurn() {
		return playerNumberTurn;
	}

	public void setPlayerNumberTurn(int playerNumberTurn) {
		this.playerNumberTurn = playerNumberTurn;
	}

	public boolean waitsForConfirmation() {
		return waitsForConfirmation;
	}

	public void setWaitsForConfirmation(boolean waitsForConfirmation) {
		this.waitsForConfirmation = waitsForConfirmation;
	}

	public int getPlayerOneScore() {
		return playerOneScore;
	}

	public void setPlayerOneScore(int playerOneScore) {
		this.playerOneScore = playerOneScore;
	}

	public int getPlayerTwoScore() {
		return playerTwoScore;
	}

	public void setPlayerTwoScore(int playerTwoScore) {
		this.playerTwoScore = playerTwoScore;
	}

	public String getDifficulty() {
		return difficulty;
	}

	public void setDifficulty(String difficulty) {
		this.difficulty = difficulty;
	}
	
}
