import java.util.Random;

public class Main {
  public static void main(String[] args) {
    LightEmAll game = new LightEmAll(5, 5, 0, false, new Random());
    game.bigBang(game.width * WorldConstants.PIECE_SIZE, game.height * WorldConstants.PIECE_SIZE);
  }
}

