package it.unibo.donkeykong.ui;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class GameOverView {

  public static Scene create(
      String winner, String myRole, Runnable onBackToLobby, Runnable onExit) {
    String resultText;
    if ("NONE".equals(winner)) {
      resultText = "Connection lost. Game over.";
    } else if ("SPECTATOR".equals(myRole)) {
      resultText = "Game over, winner: " + ("HOST".equals(winner) ? "Mario" : "Luigi");
    } else if (winner.equals(myRole)) {
      resultText = "You win!";
    } else {
      resultText = "You lost!";
    }

    Label titleLabel = new Label(resultText);
    titleLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold;");

    Button lobbyButton = new Button("Back to Lobby");
    lobbyButton.setOnAction(e -> onBackToLobby.run());

    Button exitButton = new Button("Exit Game");
    exitButton.setOnAction(e -> onExit.run());

    VBox root = new VBox(20, titleLabel, lobbyButton, exitButton);
    root.setAlignment(Pos.CENTER);

    return new Scene(root, 400, 300);
  }
}
