import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;
import java.util.Random;

interface WorldConstants {
  int PIECE_SIZE = 100;
  int WIRE_WIDTH = 1;

}

class LightEmAll extends World implements WorldConstants {
  // a list of columns of GamePieces,
  // i.e., represents the board in column-major order
  ArrayList<ArrayList<GamePiece>> board;
  // a list of all nodes
  ArrayList<GamePiece> nodes;
  // a list of edges of the minimum spanning tree
  ArrayList<Edge> mst;
  // a list of all the edges in the board
  ArrayList<Edge> allEdges;
  // the width and height of the board
  int width;
  int height;
  // the current location of the power station,
  // as well as its effective radius
  int powerRow;
  int powerCol;
  int radius;

  Random rand;
  ArrayList<GamePiece> linked;

  // determines whether the game is over
  boolean gameOver;

  // constructor for part2 using kruskals algorithm
  LightEmAll(int width, int height, int radius, boolean gameOver, Random rand) {
    this.board = new ArrayList<ArrayList<GamePiece>>();
    this.width = width;
    this.height = height;
    this.powerRow = 0;
    this.powerCol = 0;
    this.radius = radius;
    this.gameOver = gameOver;
    this.linked = new ArrayList<GamePiece>();
    this.rand = rand;

    this.initKruskal();
    this.randomizeBoard();

  }

  // constructor for testing
  LightEmAll(int width, int height) {
    this.board = new ArrayList<ArrayList<GamePiece>>();
    this.width = width;
    this.height = height;
    this.powerRow = 0;
    this.powerCol = 0;
    this.gameOver = false;
    this.rand = new Random();
    this.linked = new ArrayList<GamePiece>();

    this.initBoard();
  }

  // constructor for testing
  LightEmAll(int width, int height, int powerRow, int powerCol) {
    this.board = new ArrayList<ArrayList<GamePiece>>();
    this.width = width;
    this.height = height;
    this.powerRow = width / 2;
    this.powerCol = height / 2;
    this.gameOver = false;
    this.linked = new ArrayList<GamePiece>();

    this.initBoard();
  }

  // initializes board using kruskals
  // Effect: gives each edge a random weight
  // creates the MST
  public void initKruskal() {
    ArrayList<ArrayList<GamePiece>> buildList = new ArrayList<ArrayList<GamePiece>>();
    ArrayList<GamePiece> buildGamePieces = new ArrayList<GamePiece>();
    for (int colNum = 0; colNum < this.width; colNum++) {
      ArrayList<GamePiece> row = new ArrayList<GamePiece>();
      for (int rowNum = 0; rowNum < this.height; rowNum++) {
        GamePiece curPiece = new GamePiece(colNum, rowNum, false, false, false, false, false,
            false);
        if (rowNum != 0) {
          curPiece.top = true;
        }
        if (rowNum != this.height - 1) {
          curPiece.bottom = true;
        }
        if (colNum != 0) {
          curPiece.left = true;
        }
        if (colNum != this.width - 1) {
          curPiece.right = true;
        }
        row.add(curPiece);
        buildGamePieces.add(curPiece);
      }
      buildList.add(row);
    }

    GamePiece powerStat = buildList.get(0).get(0);
    powerStat.powerStation = true;
    powerStat.powered = true;
    this.board = buildList;
    this.nodes = buildGamePieces;

    ArrayList<Edge> allEdges = this.createEdges();
    this.mst = this.kruskalMST(allEdges);
    this.clearBoard();
    this.drawMst();
  }

  // returns a list of edges with the mst using Kruskals Algo
  public ArrayList<Edge> kruskalMST(ArrayList<Edge> worklist) {
    HashMap<GamePiece, GamePiece> representatives = new HashMap<GamePiece, GamePiece>();
    worklist.sort(new SortByWeight());
    this.mst = new ArrayList<Edge>();

    for (GamePiece node : this.nodes) {
      representatives.put(node, node);
    }

    while (!worklist.isEmpty()) {
      Edge cur = worklist.remove(0);

      GamePiece from = find(representatives, cur.fromNode);
      GamePiece to = find(representatives, cur.toNode);
      if (!from.samePiece(to)) {
        mst.add(cur);
        union(representatives, from, to);
      }
    }
    return mst;
  }

  // Effect: Clears the entire board
  public void clearBoard() {
    for (GamePiece node : this.nodes) {
      node.bottom = false;
      node.top = false;
      node.left = false;
      node.right = false;
    }
  }

  // Effect: Draws board from mst
  public void drawMst() {
    for (Edge edge : this.mst) {
      edge.fromNode.connectTo(edge.toNode);
    }
  }

  // Finds the representative of the given GamePiece on the given HashMap
  public GamePiece find(HashMap<GamePiece, GamePiece> representative, GamePiece node) {
    if (representative.containsKey(node)) {
      if (representative.get(node).equals(node)) {
        return node;
      }
      else {
        return find(representative, representative.get(node));
      }
    }
    else {
      return node;
    }

  }

  // Effect: Sets value of the to-representative to the from-representative
  public void union(HashMap<GamePiece, GamePiece> representative, GamePiece fromRep,
      GamePiece toRep) {
    representative.replace(toRep, fromRep);
  }

  // assigns all edges with a random weight
  public ArrayList<Edge> createEdges() {
    this.allEdges = new ArrayList<Edge>();
    ArrayList<GamePiece> visited = new ArrayList<GamePiece>();
    for (int col = 0; col < width; col++) {
      for (int row = 0; row < height; row++) {
        GamePiece node = board.get(col).get(row);
        visited.add(node);

        if (col < width - 1) {
          GamePiece rightNeighbor = board.get(col + 1).get(row);
          this.allEdges.add(new Edge(node, rightNeighbor, rand.nextInt()));
        }

        if (row < height - 1) {
          GamePiece bottomNeighbor = board.get(col).get(row + 1);
          this.allEdges.add(new Edge(node, bottomNeighbor, rand.nextInt()));
        }
      }
    }
    return this.allEdges;
  }

  // EFFECT: randomizes an initialized board so the wires are rotated
  public void randomizeBoard() {
    for (int i = 0; i < width; i++) {
      ArrayList<GamePiece> column = this.board.get(i);
      for (GamePiece t : column) {
        int ranInt = new Random().nextInt(4);
        for (int j = 0; j < ranInt; j++) {
          t.rotate();
        }
      }
    }
  }

  // draws the world
  public WorldScene makeScene() {
    WorldScene scene = new WorldScene(width * PIECE_SIZE, height * PIECE_SIZE);
    Color wire_color = Color.YELLOW;

    if (gameOver) {
      TextImage gameOverText = new TextImage("You Win!", 24, Color.RED);
      scene.placeImageXY(gameOverText, height * PIECE_SIZE / 2, width * PIECE_SIZE / 2);
      return scene;
    }
    else {

      for (ArrayList<GamePiece> column : board) {
        for (GamePiece piece : column) {
          if (piece != null) {
            if (!piece.powered) {
              wire_color = Color.GRAY;
            }

            else {
              wire_color = Color.YELLOW;
            }
            scene.placeImageXY(piece.tileImage(PIECE_SIZE, WIRE_WIDTH, wire_color, false),
                (piece.col * PIECE_SIZE) + (PIECE_SIZE / 2),
                (piece.row * PIECE_SIZE) + (PIECE_SIZE / 2));
          }
        }

        ArrayList<GamePiece> connected = new ArrayList<>();
        powerGamePieces(powerRow, powerCol, connected);
        linked = connected;
        powerTiles();
      }

      scene.placeImageXY(
          board.get(powerRow).get(powerCol).tileImage(PIECE_SIZE, WIRE_WIDTH, Color.YELLOW, true),
          (powerCol * PIECE_SIZE) + (PIECE_SIZE / 2), (powerRow * PIECE_SIZE) + (PIECE_SIZE / 2));

      return scene;
    }
  }

  // EFFECT: modifies the powerRow and powerCol after a key is clicked
  public void onKeyEvent(String key) {
    if (!gameOver) {
      int newPowerRow = powerRow;
      int newPowerCol = powerCol;

      if (key.equals("up")) {
        newPowerRow -= 1;
      }
      else if (key.equals("down")) {
        newPowerRow++;
      }
      else if (key.equals("left")) {
        newPowerCol -= 1;
      }
      else if (key.equals("right")) {
        newPowerCol += 1;
      }

      if (isValidPosition(newPowerRow, newPowerCol)) {
        if (hasWireConnection(newPowerRow, newPowerCol)) {
          powerRow = newPowerRow;
          powerCol = newPowerCol;

        }
      }
    }
  }

  // Checks if the given position has a wire connection to the old power station
  // position
  public boolean hasWireConnection(int row, int col) {
    GamePiece piece = board.get(row).get(col);
    GamePiece powSts = board.get(powerRow).get(powerCol);

    if (powerRow == row - 1 && powerCol == col && piece.top && powSts.bottom) {
      return true;
    }

    else if (powerRow == row + 1 && powerCol == col && piece.bottom && powSts.top) {
      return true;
    }

    else if (powerRow == row && powerCol == col - 1 && piece.left && powSts.right) {
      return true;
    }

    else if (powerRow == row && powerCol == col + 1 && piece.right && powSts.left) {
      return true;
    }

    return false;
  }

  // Checks if the given position is within the bounds of the board
  public boolean isValidPosition(int row, int col) {
    return row >= 0 && row < height && col >= 0 && col < width;
  }

  // EFFECT: returns a list of all the GamePieces connected to the cell indicated
  // by its position
  public void powerGamePieces(int pieceRow, int pieceCol, ArrayList<GamePiece> connected) {
    GamePiece powStsPiece = board.get(pieceRow).get(pieceCol);
    GamePiece adjTop = null;
    GamePiece adjRight = null;
    GamePiece adjBot = null;
    GamePiece adjLeft = null;

    if (pieceRow > 0) {
      adjTop = board.get(pieceRow - 1).get(pieceCol);
    }

    if (pieceCol < width - 1) {
      adjRight = board.get(pieceRow).get(pieceCol + 1);
    }

    if (pieceRow < height - 1) {
      adjBot = board.get(pieceRow + 1).get(pieceCol);

    }

    if (pieceCol > 0) {
      adjLeft = board.get(pieceRow).get(pieceCol - 1);
    }

    connected.add(powStsPiece);

    if (adjTop != null && powStsPiece.top && adjTop.bottom && !connected.contains(adjTop)) {
      powerGamePieces(pieceRow - 1, pieceCol, connected);
    }

    if (adjRight != null && powStsPiece.right && adjRight.left && !connected.contains(adjRight)) {
      powerGamePieces(pieceRow, pieceCol + 1, connected);
    }

    if (adjBot != null && powStsPiece.bottom && adjBot.top && !connected.contains(adjBot)) {
      powerGamePieces(pieceRow + 1, pieceCol, connected);
    }

    if (adjLeft != null && powStsPiece.left && adjLeft.right && !connected.contains(adjLeft)) {
      powerGamePieces(pieceRow, pieceCol - 1, connected);
    }
  }

  // EFFECT: powers up each piece in the linked list
  public void powerTiles() {
    for (GamePiece piece : linked) {
      piece.poweUp();
    }
  }

  // EFFECT: rotates the clicked tile
  // powers up the connected pieces
  // ends the game if bfs returns true
  public void onMouseClicked(Posn pos, String button) {
    if (!gameOver && button.equals("LeftButton")) {
      int col = pos.x / PIECE_SIZE;
      int row = pos.y / PIECE_SIZE;
      GamePiece clickedTile = board.get(row).get(col);
      this.dePowerBoard();
      clickedTile.rotate();
      ArrayList<GamePiece> connected = new ArrayList<>();
      powerGamePieces(powerRow, powerCol, connected);
      linked = connected;
      powerTiles();
    }
    if (this.bfs()) {
      this.gameOver = true;
    }
  }

  // EFFECT: initializes all the cells to the correctly rotated board
  public void initBoard() {
    ArrayList<GamePiece> buildNodes = new ArrayList<>();
    for (int i = 0; i < width; i++) {
      ArrayList<GamePiece> column = new ArrayList<>();
      for (int j = 0; j < height; j++) {
        GamePiece tile = new GamePiece(i, j, false, false, true, true, false, false);

        if (i == 0) {
          tile.opnChange(false, false, false, true);
        }

        if (i == (height - 1)) {
          tile.opnChange(false, false, true, false);
        }

        column.add(tile);
        buildNodes.add(tile);
      }
      board.add(column);
    }

    this.horizontalBar();
    this.addPowerStation(this.powerCol, this.powerRow);
    this.nodes = buildNodes;

  }

  // EFFECT: changes the tiles so that there is a horizontal bar at the center
  // of the board
  public void horizontalBar() {
    ArrayList<GamePiece> midCol = this.board.get(height / 2);
    for (int i = 0; i < midCol.size(); i++) {
      GamePiece tile = midCol.get(i);

      if (i == 0) {
        tile.opnChange(false, true, true, true);
      }

      else if (i == (width - 1)) {
        tile.opnChange(true, false, true, true);
      }

      else {
        tile.opnChange(true, true, true, true);
      }
    }
  }

  // EFFECT: adds the powered according to the indicated position
  public void addPowerStation(int powCol, int powRow) {
    this.board.get(powCol).get(powRow).makePowStn();
  }

  // EFFECT; un-powers every cell in this board
  public void dePowerBoard() {
    for (ArrayList<GamePiece> column : board) {
      for (GamePiece piece : column) {
        piece.dePower();
      }
    }
  }

  // returns true if all cells are connected, uses breath-first search
  public boolean bfs() {
    GamePiece powerStationCell = this.board.get(this.powerCol).get(this.powerRow);

    ArrayList<GamePiece> worklist = new ArrayList<>();
    ArrayList<GamePiece> alreadySeen = new ArrayList<>();

    worklist.add(powerStationCell);
    alreadySeen.add(powerStationCell);

    while (!worklist.isEmpty()) {
      GamePiece currentCell = worklist.remove(0);

      if (this.isValidPosition(currentCell.row, currentCell.col + 1)) {
        GamePiece neighbor = this.board.get(currentCell.col + 1).get(currentCell.row);
        if (!alreadySeen.contains(neighbor) && neighbor.powered) {
          worklist.add(neighbor);
          alreadySeen.add(neighbor);
        }
      }
      if (this.isValidPosition(currentCell.row, currentCell.col - 1)) {
        GamePiece neighbor = this.board.get(currentCell.col - 1).get(currentCell.row);
        if (!alreadySeen.contains(neighbor) && neighbor.powered) {
          worklist.add(neighbor);
          alreadySeen.add(neighbor);
        }
      }
      if (this.isValidPosition(currentCell.row + 1, currentCell.col)) {
        GamePiece neighbor = this.board.get(currentCell.col).get(currentCell.row + 1);
        if (!alreadySeen.contains(neighbor) && neighbor.powered) {
          worklist.add(neighbor);
          alreadySeen.add(neighbor);
        }
      }
      if (this.isValidPosition(currentCell.row - 1, currentCell.col)) {
        GamePiece neighbor = this.board.get(currentCell.col).get(currentCell.row - 1);
        if (!alreadySeen.contains(neighbor) && neighbor.powered) {
          worklist.add(neighbor);
          alreadySeen.add(neighbor);
        }
      }
    }

    return alreadySeen.size() == this.width * this.height;
  }

}

class GamePiece {

  // in logical coordinates, with the origin
  // at the top-left corner of the screen
  int row;
  int col;
  // whether this GamePiece is connected to the
  // adjacent left, right, top, or bottom pieces
  boolean left;
  boolean right;
  boolean top;
  boolean bottom;
  // whether the power station is on this piece
  boolean powerStation;
  // whether the GamePiece is powered or not
  boolean powered;

  GamePiece(int row, int col, boolean left, boolean right, boolean top, boolean bottom,
      boolean powerStation, boolean powered) {
    this.row = row;
    this.col = col;
    this.left = left;
    this.right = right;
    this.top = top;
    this.bottom = bottom;
    this.powerStation = powerStation;
    this.powered = powered;
  }

  // Generate an image of this, the given GamePiece.
  // - size: the size of the tile, in pixels
  // - wireWidth: the width of wires, in pixels
  // - wireColor: the Color to use for rendering wires on this
  // - hasPowerStation: if true, draws a fancy star on this tile to represent the
  // power station
  //
  WorldImage tileImage(int size, int wireWidth, Color wireColor, boolean hasPowerStation) {
    // Start tile image off as a blue square with a wire-width square in the middle,
    // to make image "cleaner" (will look strange if tile has no wire, but that
    // can't be)
    WorldImage image = new OverlayImage(
        new RectangleImage(wireWidth, wireWidth, OutlineMode.SOLID, wireColor),
        new RectangleImage(size, size, OutlineMode.SOLID, Color.DARK_GRAY));
    WorldImage vWire = new RectangleImage(wireWidth, (size + 1) / 2, OutlineMode.SOLID, wireColor);
    WorldImage hWire = new RectangleImage((size + 1) / 2, wireWidth, OutlineMode.SOLID, wireColor);

    if (this.top) {
      image = new OverlayOffsetAlign(AlignModeX.CENTER, AlignModeY.TOP, vWire, 0, 0, image);
    }
    if (this.right) {
      image = new OverlayOffsetAlign(AlignModeX.RIGHT, AlignModeY.MIDDLE, hWire, 0, 0, image);
    }
    if (this.bottom) {
      image = new OverlayOffsetAlign(AlignModeX.CENTER, AlignModeY.BOTTOM, vWire, 0, 0, image);
    }
    if (this.left) {
      image = new OverlayOffsetAlign(AlignModeX.LEFT, AlignModeY.MIDDLE, hWire, 0, 0, image);
    }
    if (hasPowerStation) {
      image = new OverlayImage(
          new OverlayImage(new StarImage(size / 3, 7, OutlineMode.OUTLINE, new Color(255, 128, 0)),
              new StarImage(size / 3, 7, OutlineMode.SOLID, new Color(0, 255, 255))),
          image);
    }
    return new FrameImage(image);
  }

  // returns true if both GamePieces are the same
  public boolean samePiece(GamePiece that) {
    return this.col == that.col && this.row == that.row;
  }

  // Effect: Connects this GamePiece's wires to the given adjacent GamePiece
  public void connectTo(GamePiece that) {
    if (that.row > this.row) {
      this.bottom = true;
      that.top = true;
    }
    else if (that.row < this.row) {
      this.top = true;
      this.bottom = false;
    }
    else if (that.col > this.col) {
      this.right = true;
      that.left = true;
    }
    else if (that.col < this.col) {
      this.left = true;
      that.right = true;
    }
  }

  // EFFECT: changes the GamePiece so that it connects to the indicated pieces
  public void opnChange(boolean left, boolean right, boolean top, boolean bottom) {
    this.left = left;
    this.right = right;
    this.top = top;
    this.bottom = bottom;

  }

  // EFFECT: changes the GamePiece into a PowerStation
  public void makePowStn() {
    this.powerStation = true;
  }

  // EFFECT: changes the gamepiece to powered
  public void poweUp() {
    this.powered = true;
  }

  // EFFECT: changes the gamepiece to not powered
  public void dePower() {
    this.powered = false;
  }

  // EFFECT: rotates the GamePiece by 90 degrees clockwise
  public void rotate() {
    boolean prevTop = this.top;
    boolean prevRight = this.right;
    boolean prevBot = this.bottom;
    boolean prevLeft = this.left;

    this.top = prevLeft;
    this.right = prevTop;
    this.bottom = prevRight;
    this.left = prevBot;
  }

}

//class representing an edge in the game
class Edge {
  GamePiece fromNode;
  GamePiece toNode;
  int weight;

  Edge(GamePiece from, GamePiece to, int weight) {
    this.fromNode = from;
    this.toNode = to;
    this.weight = weight;
  }
}

// comparator object that compares edges by weight
class SortByWeight implements Comparator<Edge> {
  public int compare(Edge edge1, Edge edge2) {
    return edge1.weight - edge2.weight;
  }
}

class ExamplesLightEmAll {

  GamePiece HoriPiece;
  GamePiece pwHoriPiece;
  GamePiece vertPiece;
  GamePiece pwVertPiece;
  GamePiece fourPiece;
  GamePiece noOpnPiece;
  GamePiece pwBotPiece;
  GamePiece pwTopPiece;
  GamePiece by3Piece0;
  GamePiece by3Piece1;
  GamePiece by3Piece2;
  GamePiece by3Piece3;
  GamePiece by3Piece4;
  GamePiece by3Piece5;
  GamePiece by3Piece6;
  GamePiece by3Piece7;
  GamePiece by3Piece8;

  GamePiece piece1;
  GamePiece piece2;
  GamePiece piece3;
  GamePiece piece4;

  ArrayList<GamePiece> by3Row0;
  ArrayList<GamePiece> by3Row1;
  ArrayList<GamePiece> by3Row2;
  ArrayList<ArrayList<GamePiece>> by3Board;

  LightEmAll board1;
  LightEmAll board2;
  LightEmAll board3;
  LightEmAll board4;
  LightEmAll board5;
  LightEmAll board55;
  LightEmAll board6;

  void init() {
    this.piece1 = new GamePiece(0, 0, false, false, false, true, false, false);
    this.piece2 = new GamePiece(1, 0, false, false, false, false, false, false);
    this.piece3 = new GamePiece(0, 1, false, false, false, false, false, false);
    this.piece4 = new GamePiece(1, 1, false, false, false, false, false, false);

    this.board1 = new LightEmAll(2, 2);
    this.board2 = new LightEmAll(3, 3);
    this.board3 = new LightEmAll(2, 2);
    this.board4 = new LightEmAll(2, 2);

    this.board5 = new LightEmAll(2, 2, 0, false, new Random(6));
    this.board55 = new LightEmAll(2, 2);
    this.board6 = new LightEmAll(3, 3, 0, false, new Random());

    // example of a GamePiece with an opening on the left and right
    this.HoriPiece = new GamePiece(0, 0, true, true, false, false, false, false);

    // example of a powered GamePiece with an opening on the left and right
    this.pwHoriPiece = new GamePiece(0, 0, true, true, false, false, false, true);

    // example of a GamePiece with an opening at the top and bottom
    this.vertPiece = new GamePiece(0, 0, false, false, true, true, false, false);

    // example of a powered GamePiece with an opening at the top and bottom
    this.pwVertPiece = new GamePiece(0, 0, false, false, true, true, false, true);

    // example of a GamePiece with openings on all sides
    this.fourPiece = new GamePiece(0, 0, true, true, true, true, false, false);

    // example of a GamePiece with no openings on any side
    this.noOpnPiece = new GamePiece(0, 0, false, false, false, false, false, false);

    // example of a powered GamePiece with only an opening at the bottom
    this.pwBotPiece = new GamePiece(0, 0, false, false, false, true, false, true);

    // example of a powered GamePiece with only an opening at the top
    this.pwTopPiece = new GamePiece(0, 0, false, false, true, false, false, true);

    this.by3Piece0 = new GamePiece(0, 0, false, false, false, true, false, false);
    this.by3Piece1 = new GamePiece(0, 1, false, false, false, true, false, false);
    this.by3Piece2 = new GamePiece(0, 2, false, false, false, true, false, false);

    this.by3Piece3 = new GamePiece(1, 0, false, true, true, true, false, false);
    this.by3Piece4 = new GamePiece(1, 1, true, true, true, true, true, false);
    this.by3Piece5 = new GamePiece(1, 2, true, false, true, true, false, false);

    this.by3Piece6 = new GamePiece(2, 0, false, false, true, false, false, false);
    this.by3Piece7 = new GamePiece(2, 1, false, false, true, false, false, false);
    this.by3Piece8 = new GamePiece(2, 2, false, false, true, false, false, false);

    // example of a board for a 3x3 LightEmAll that has been solved
    this.by3Row0 = new ArrayList<GamePiece>(
        Arrays.asList(this.by3Piece0, this.by3Piece1, this.by3Piece2));

    this.by3Row1 = new ArrayList<GamePiece>(
        Arrays.asList(this.by3Piece3, this.by3Piece4, this.by3Piece5));

    this.by3Row2 = new ArrayList<GamePiece>(
        Arrays.asList(this.by3Piece6, this.by3Piece7, this.by3Piece8));

    this.by3Board = new ArrayList<ArrayList<GamePiece>>(
        Arrays.asList(this.by3Row0, this.by3Row1, this.by3Row2));

  }
  
  // test for method initKruskal
  void testInitKruskal(Tester t) {
    this.init();

    // initialize the board
    this.board5.initKruskal();

    // checks dimensions are correctly initialized
    t.checkExpect(this.board5.board.size(), 2);
    t.checkExpect(this.board5.board.get(0).size(), 2);

    // checks that powerstation is in top left
    t.checkExpect(this.board5.board.get(0).get(0).powerStation, true);

    GamePiece p0 = this.piece1;
    p0 = this.board5.board.get(0).get(0);

    GamePiece p1 = this.piece1;
    p1 = this.board5.board.get(1).get(1);

    GamePiece p2 = this.piece1;
    p2 = this.board5.board.get(1).get(0);

    GamePiece p3 = this.piece1;
    p3 = this.board5.board.get(0).get(1);

    // checks that each piece has been initialized
    t.checkExpect(p0.bottom || p0.top || p0.right || p0.left, true);
    t.checkExpect(p1.bottom || p1.top || p1.right || p1.left, true);
    t.checkExpect(p2.bottom || p2.top || p2.right || p2.left, true);
    t.checkExpect(p3.bottom || p3.top || p3.right || p3.left, true);
  }

  // test the method drawMst
  void testDrawMst(Tester t) {
    this.init();

    GamePiece piece1 = new GamePiece(0, 0, false, false, false, false, false, false);
    GamePiece piece2 = new GamePiece(1, 0, false, false, false, false, false, false);
    GamePiece piece3 = new GamePiece(0, 1, false, false, false, false, false, false);
    GamePiece piece4 = new GamePiece(1, 1, false, false, false, false, false, false);

    piece1.connectTo(piece2);
    piece1.connectTo(piece3);
    piece2.connectTo(piece4);
    piece3.connectTo(piece4);

    // Creating an MST with selected edges
    ArrayList<Edge> mst = new ArrayList<>();
    mst.add(new Edge(piece1, piece2, 1));
    mst.add(new Edge(piece1, piece3, 2));
    mst.add(new Edge(piece2, piece4, 3));
    mst.add(new Edge(piece3, piece4, 4));

    this.board55.mst = mst;

    // Drawing the MST
    this.board55.drawMst();

    // Checking the connections after drawing the MST
    t.checkExpect(piece1.bottom, true);
    t.checkExpect(piece3.bottom, true);
    t.checkExpect(piece2.right, true);
    t.checkExpect(piece4.top, true);
  }

  // test the method union
  void testUnion(Tester t) {
    this.init();

    HashMap<GamePiece, GamePiece> representatives = new HashMap<>();
    GamePiece piece1 = new GamePiece(0, 0, false, false, false, false, false, false);
    GamePiece piece2 = new GamePiece(1, 1, false, false, false, false, false, false);
    GamePiece piece3 = new GamePiece(2, 2, false, false, false, false, false, false);

    representatives.put(piece1, piece1);
    representatives.put(piece2, piece2);
    representatives.put(piece3, piece3);

    // unions 2 pieces
    this.board3.union(representatives, piece1, piece2);
    // Checks that the representative of piece2 updates to piece2
    t.checkExpect(representatives.get(piece2), piece1);

    // union 2 pieces
    this.board3.union(representatives, piece1, piece3);
    // Checks that the representative of piece3 updates to piece1
    t.checkExpect(representatives.get(piece3), piece1);
  }

  // test the method kruskalMST
  void testKruskalMST(Tester t) {
    this.init();

    GamePiece piece1 = new GamePiece(0, 0, false, false, false, false, false, false);
    GamePiece piece2 = new GamePiece(1, 1, false, false, false, false, false, false);
    GamePiece piece3 = new GamePiece(2, 2, false, false, false, false, false, false);
    GamePiece piece4 = new GamePiece(0, 2, false, false, false, false, false, false);

    Edge edge1 = new Edge(piece1, piece2, 9);
    Edge edge2 = new Edge(piece1, piece3, 2);
    Edge edge3 = new Edge(piece2, piece3, 5);
    Edge edge4 = new Edge(piece1, piece4, 1);
    Edge edge5 = new Edge(piece3, piece4, 3);

    t.checkExpect(
        this.board2.kruskalMST(new ArrayList<>(Arrays.asList(edge1, edge2, edge3, edge4, edge5))),
        new ArrayList<>(Arrays.asList(edge4, edge2, edge5, edge3, edge1)));
  }

  // test the method createEdges
  void testCreateEdges(Tester t) {
    this.init();

    ArrayList<Edge> edges = this.board2.createEdges();

    // checks that correct number of edges were created
    t.checkExpect(edges.size(), 12);

    // Checks that each edge is created between adjacent cells
    for (Edge edge : edges) {
      boolean adjacentCell = Math.abs(edge.fromNode.row - edge.toNode.row)
          + Math.abs(edge.fromNode.col - edge.toNode.col) == 1;

      t.checkExpect(adjacentCell, true);
    }
  }

  // test the method find
  void testFind(Tester t) {
    this.init();

    GamePiece piece1 = new GamePiece(0, 0, false, false, false, false, false, false);
    GamePiece piece2 = new GamePiece(1, 1, false, false, false, false, false, false);
    GamePiece piece3 = new GamePiece(2, 2, false, false, false, false, false, false);

    // setting piece2 and piece3 to both have piece1 as their representative
    HashMap<GamePiece, GamePiece> representatives = new HashMap<>();
    representatives.put(piece2, piece1);
    representatives.put(piece3, piece1);

    // Checks that find returns piece1 for piece2 and piece3
    t.checkExpect(this.board55.find(representatives, piece2), piece1);
    t.checkExpect(this.board55.find(representatives, piece3), piece1);
  }

  //test the method clearBoard
  void testClearBoard(Tester t) {
    this.init();

    // Checks that all pieces have wires/are initialized
    for (ArrayList<GamePiece> row : this.board55.board) {
      for (GamePiece piece : row) {
        t.checkExpect(piece.left || piece.right || piece.top || piece.bottom, true);
      }
    }

    // calls the method
    this.board55.clearBoard();

    // checks that all pieces now have no wites
    for (ArrayList<GamePiece> row : this.board55.board) {
      for (GamePiece piece : row) {
        t.checkExpect(piece.left && piece.right && piece.top && piece.bottom, false);
      }
    }
  }

  // test method for initBoard
  void testInitBoard(Tester t) {

    // creates a new board which calls initBoard
    LightEmAll testingBoard = new LightEmAll(2, 2);

    // checks that each cell has been initialized
    t.checkExpect(testingBoard.board.get(0).get(0).left || !testingBoard.board.get(0).get(0).left,
        true);
    t.checkExpect(testingBoard.board.get(0).get(0).right || !testingBoard.board.get(0).get(0).right,
        true);
    t.checkExpect(testingBoard.board.get(0).get(0).top || !testingBoard.board.get(0).get(0).top,
        true);
    t.checkExpect(
        testingBoard.board.get(0).get(0).bottom || !testingBoard.board.get(0).get(0).bottom, true);
    t.checkExpect(testingBoard.board.get(0).get(0).powerStation, true);

    t.checkExpect(testingBoard.board.get(0).get(1).left || !testingBoard.board.get(0).get(1).left,
        true);
    t.checkExpect(testingBoard.board.get(0).get(1).right || !testingBoard.board.get(0).get(1).right,
        true);
    t.checkExpect(testingBoard.board.get(0).get(1).top || !testingBoard.board.get(0).get(1).top,
        true);
    t.checkExpect(
        testingBoard.board.get(0).get(1).bottom || !testingBoard.board.get(0).get(1).bottom, true);
    t.checkExpect(testingBoard.board.get(0).get(1).powerStation, false);

    t.checkExpect(testingBoard.board.get(1).get(0).left || !testingBoard.board.get(1).get(0).left,
        true);
    t.checkExpect(testingBoard.board.get(1).get(0).right || !testingBoard.board.get(1).get(0).right,
        true);
    t.checkExpect(testingBoard.board.get(1).get(0).top || !testingBoard.board.get(1).get(0).top,
        true);
    t.checkExpect(
        testingBoard.board.get(1).get(0).bottom || !testingBoard.board.get(1).get(0).bottom, true);
    t.checkExpect(testingBoard.board.get(1).get(0).powerStation, false);

    t.checkExpect(testingBoard.board.get(1).get(1).left || !testingBoard.board.get(1).get(1).left,
        true);
    t.checkExpect(testingBoard.board.get(1).get(1).right || !testingBoard.board.get(1).get(1).right,
        true);
    t.checkExpect(testingBoard.board.get(1).get(1).top || !testingBoard.board.get(1).get(1).top,
        true);
    t.checkExpect(
        testingBoard.board.get(1).get(1).bottom || !testingBoard.board.get(1).get(1).bottom, true);
    t.checkExpect(testingBoard.board.get(1).get(1).powerStation, false);
  }

  // test method for randomizeBoard
  void testRandomizeBoard(Tester t) {
    LightEmAll board = new LightEmAll(3, 3, 1, 1);

    // stores the original layout of the board
    ArrayList<ArrayList<GamePiece>> originalBoard = new ArrayList<>();
    for (ArrayList<GamePiece> column : board.board) {
      ArrayList<GamePiece> originalColumn = new ArrayList<>();
      for (GamePiece piece : column) {
        originalColumn.add(new GamePiece(piece.row, piece.col, piece.left, piece.right, piece.top,
            piece.bottom, piece.powerStation, piece.powered));
      }
      originalBoard.add(originalColumn);
    }

    board.randomizeBoard();

    boolean hasChanged = false;
    for (int i = 0; i < board.width; i++) {
      for (int j = 0; j < board.height; j++) {
        GamePiece originalPiece = originalBoard.get(i).get(j);
        GamePiece randomizedPiece = board.board.get(i).get(j);
        if (!randomizedPiece.equals(originalPiece)) {
          hasChanged = true;
        }
      }
    }

    // Check that the board has been randomized
    t.checkExpect(hasChanged, true);
  }

  // tests onMouseClicked method
  void testOnMouseClicked(Tester t) {
    this.init();

    this.board1.board.get(0).set(0, new GamePiece(0, 0, true, true, true, true, true, true));
    this.board1.board.get(0).set(1, new GamePiece(0, 1, false, false, true, true, false, false));
    this.board1.board.get(1).set(0, new GamePiece(1, 0, true, true, false, false, false, false));

    t.checkExpect(this.board1.linked, new ArrayList<GamePiece>());
    t.checkExpect(this.board1.board.get(0).get(1).left, false);
    t.checkExpect(this.board1.board.get(0).get(1).right, false);
    t.checkExpect(this.board1.board.get(0).get(1).top, true);
    t.checkExpect(this.board1.board.get(0).get(1).bottom, true);
    t.checkExpect(this.board1.board.get(0).get(1).powered, false);

    t.checkExpect(this.board1.linked, new ArrayList<GamePiece>());
    t.checkExpect(this.board1.board.get(1).get(0).left, true);
    t.checkExpect(this.board1.board.get(1).get(0).right, true);
    t.checkExpect(this.board1.board.get(1).get(0).top, false);
    t.checkExpect(this.board1.board.get(1).get(0).bottom, false);
    t.checkExpect(this.board1.board.get(1).get(0).powered, false);

    t.checkExpect(this.board1.gameOver, false);

    this.board1.onMouseClicked(new Posn(150, 0), "LeftButton");

    t.checkExpect(this.board1.linked, new ArrayList<GamePiece>(
        Arrays.asList(this.board1.board.get(0).get(0), this.board1.board.get(0).get(1))));
    t.checkExpect(this.board1.board.get(0).get(1).left, true);
    t.checkExpect(this.board1.board.get(0).get(1).right, true);
    t.checkExpect(this.board1.board.get(0).get(1).top, false);
    t.checkExpect(this.board1.board.get(0).get(1).bottom, false);
    t.checkExpect(this.board1.board.get(0).get(1).powered, true);

    t.checkExpect(this.board1.gameOver, false);

    this.board1.onMouseClicked(new Posn(0, 150), "LeftButton");

    t.checkExpect(this.board1.linked,
        new ArrayList<GamePiece>(Arrays.asList(this.board1.board.get(0).get(0),
            this.board1.board.get(0).get(1), this.board1.board.get(1).get(0))));
    t.checkExpect(this.board1.board.get(1).get(0).left, false);
    t.checkExpect(this.board1.board.get(1).get(0).right, false);
    t.checkExpect(this.board1.board.get(1).get(0).top, true);
    t.checkExpect(this.board1.board.get(1).get(0).bottom, true);
    t.checkExpect(this.board1.board.get(1).get(0).powered, true);

    t.checkExpect(this.board1.gameOver, false);

  }

  // test for method addPowerStation
  void testAddPowerStation(Tester t) {
    this.init();

    // makes a 4by4 LightEmAll game that has been solved
    LightEmAll by4LightEmAll = new LightEmAll(4, 4, 0, 0);

    // adds a power station to the GamePiece indicated by its position
    by4LightEmAll.addPowerStation(0, 0);

    // makes a 3by3 LightEmAll game that has been solved
    LightEmAll by3LightEmAll = new LightEmAll(3, 3, 0, 0);
    // adds a power station to the GamePiece indicated by its position
    by3LightEmAll.addPowerStation(2, 0);

    // makes a 2by2 LightEmAll game that has been solved
    LightEmAll by2LightEmAll = new LightEmAll(2, 2, 0, 0);
    // adds a power station to the GamePiece indicated by its position
    by2LightEmAll.addPowerStation(0, 1);

    // confirms that the the power station was added to the correct GamePiece
    t.checkExpect(by4LightEmAll.board.get(0).get(0).powerStation, true);
    t.checkExpect(by3LightEmAll.board.get(2).get(0).powerStation, true);
    t.checkExpect(by2LightEmAll.board.get(0).get(1).powerStation, true);

  }

  // Test method for bfs
  void testBFS(Tester t) {
    LightEmAll testBoard = new LightEmAll(3, 3, 1, 1);

    // Power up the entire board
    for (ArrayList<GamePiece> column : testBoard.board) {
      for (GamePiece piece : column) {
        piece.powered = true;
      }
    }

    // call bfs
    boolean result = testBoard.bfs();

    // Expect the result to be true since all pieces are powered and connected
    t.checkExpect(result, true);
  }

  // tests for method powerTiles
  void testPowerTiles(Tester t) {
    this.init();
    this.board5.linked = new ArrayList<GamePiece>(
        Arrays.asList(this.piece1, this.piece2, this.piece3));

    t.checkExpect(this.piece1.powered, false);
    t.checkExpect(this.piece2.powered, false);
    t.checkExpect(this.piece3.powered, false);

    this.board5.powerTiles();

    t.checkExpect(this.piece1.powered, true);
    t.checkExpect(this.piece2.powered, true);
    t.checkExpect(this.piece3.powered, true);

  }

  // tests for method hasWireConnection
  boolean testHasWireConnection(Tester t) {
    this.init();

    this.board1.board.get(0).set(0, new GamePiece(0, 0, true, true, true, true, true, true));
    this.board1.board.get(0).set(1, new GamePiece(0, 1, true, false, true, false, false, false));
    this.board1.board.get(1).set(0, new GamePiece(1, 0, false, false, false, false, false, false));

    this.board6.powerCol = 1;
    this.board6.powerRow = 1;
    this.board6.board.get(1).set(1, new GamePiece(1, 1, true, true, true, true, true, true));
    this.board6.board.get(1).set(0, new GamePiece(1, 0, true, true, false, true, false, true));
    this.board6.board.get(0).set(1, new GamePiece(0, 1, false, false, false, true, false, true));
    this.board6.board.get(2).set(1, new GamePiece(2, 1, false, false, true, false, false, true));

    return
    // test that wire connection works when cell is to the left of the power station
    t.checkExpect(this.board6.hasWireConnection(1, 0), true)
        // tests that wire connection works for when cell is above power station
        && t.checkExpect(this.board6.hasWireConnection(0, 1), true)
        // tests that wire connection works when cell is below power station
        && t.checkExpect(this.board6.hasWireConnection(2, 1), true)
        // tests that wire connection works when cell is to the right of the power
        // station
        && t.checkExpect(this.board1.hasWireConnection(0, 1), true)
        // tests that wire connection returns false when there is no connection
        && t.checkExpect(this.board1.hasWireConnection(1, 0), false);
  }

  // test for method isValidPostion
  boolean testIsValidPosition(Tester t) {
    this.init();

    return
    // test for valid position
    t.checkExpect(this.board1.isValidPosition(0, 0), true)
        // test for invalid position
        && t.checkExpect(this.board1.isValidPosition(6, 6), false);
  }

  // tests method makeScene
  boolean testMakeScene(Tester t) {
    this.init();
    WorldScene scene = new WorldScene(200, 200);
    Color wire_color = Color.YELLOW;

    for (ArrayList<GamePiece> column : this.board4.board) {
      for (GamePiece piece : column) {
        if (piece != null) {
          if (!piece.powered) {
            wire_color = Color.GRAY;
          }

          else {
            wire_color = Color.YELLOW;
          }
          scene.placeImageXY(piece.tileImage(100, 1, wire_color, false), (piece.col * 100) + 50,
              (piece.row * 100) + 50);
        }
      }
      ArrayList<GamePiece> connected = new ArrayList<>();
      this.board4.powerGamePieces(0, 0, connected);
      this.board4.linked = connected;
      this.board4.powerTiles();
    }
    scene.placeImageXY(this.board4.board.get(0).get(0).tileImage(100, 1, Color.YELLOW, true), 50,
        50);

    return t.checkExpect(this.board3.makeScene(), scene);
  }

  // test method onKeyEvent
  void testOnKeyEvent(Tester t) {
    this.init();

    // setting cell (0,0) to a powerStation with wire connections in all directions
    this.board5.board.get(0).set(0, new GamePiece(0, 0, true, true, true, true, true, true));
    // setting cell (0, 1) to a non-powerStation with wire connections in all
    // directions
    this.board5.board.get(1).set(0, new GamePiece(1, 0, false, false, true, true, false, true));

    // checks that the power station is at point (0,0)
    t.checkExpect(this.board5.powerCol, 0);
    t.checkExpect(this.board5.powerRow, 0);

    // calls keyEvent with button down
    this.board5.onKeyEvent("down");

    // checks to see that the power station moves down
    t.checkExpect(this.board5.powerCol, 0);
    t.checkExpect(this.board5.powerRow, 1);

    // tests the up key
    this.board5.onKeyEvent("up");

    t.checkExpect(this.board5.powerCol, 0);
    t.checkExpect(this.board5.powerRow, 0);

    this.board5.board.get(0).set(1, new GamePiece(1, 0, true, false, false, false, false, false));

    // tests the right key
    this.board5.onKeyEvent("right");

    t.checkExpect(this.board5.powerCol, 1);
    t.checkExpect(this.board5.powerRow, 0);

    // tests the left key
    this.board5.onKeyEvent("left");

    t.checkExpect(this.board5.powerCol, 0);
    t.checkExpect(this.board5.powerRow, 0);

    this.board5.board.get(1).set(0, new GamePiece(0, 1, false, false, false, true, false, false));

    // tests that the power station does not move when there is no connection in
    // that direction
    this.board5.onKeyEvent("down");

    t.checkExpect(this.board5.powerCol, 0);
    t.checkExpect(this.board5.powerRow, 0);

    this.board5.gameOver = true;

    // tests that the power station does not move when the game is over
    this.board5.onKeyEvent("right");

    t.checkExpect(this.board5.powerCol, 0);
    t.checkExpect(this.board5.powerRow, 0);

  }

  // test for horizontalBar()
  void testforHoriBar(Tester t) {
    this.init();

    LightEmAll by3LightEmAll = new LightEmAll(3, 3, 0, 0);

    t.checkExpect(by3LightEmAll.board, this.by3Board);

  }

  // test for opnChange()
  void testOpnChange(Tester t) {
    this.init();

    // creates an opening on every side from a GamePiece with no openings
    this.noOpnPiece.opnChange(true, true, true, true);

    // creates an opening on one side only
    this.HoriPiece.opnChange(true, false, false, false);

    // creates an opening on two sides only
    this.vertPiece.opnChange(true, true, false, false);

    // creates an opening on three sides only
    this.fourPiece.opnChange(true, true, true, false);

    // checks that the correct openings were made
    t.checkExpect(this.noOpnPiece.left && this.noOpnPiece.right && this.noOpnPiece.top
        && this.noOpnPiece.bottom, true);
    t.checkExpect(this.HoriPiece.left, true);
    t.checkExpect(this.vertPiece.left && this.vertPiece.right, true);
    t.checkExpect(this.fourPiece.left && this.fourPiece.right && this.fourPiece.top, true);
    t.checkExpect(null, null);

  }

  // test for makePowStn()
  void testMakePowSts(Tester t) {
    this.init();

    // adds a power station to this GamePiece
    this.HoriPiece.makePowStn();
    // adds a power station to this GamePiece
    this.vertPiece.makePowStn();
    // adds a power station to this GamePiece
    this.fourPiece.makePowStn();

    // checks that this GamePiece has a power station
    t.checkExpect(this.HoriPiece.powerStation, true);
    t.checkExpect(this.vertPiece.powerStation, true);
    t.checkExpect(this.fourPiece.powerStation, true);
  }

  // test for powerUp()
  void testPowerUp(Tester t) {
    this.init();

    // applies powerUp() to !powered GamePiece
    this.HoriPiece.poweUp();

    // applies powerUp() to !powered GamePiece
    this.vertPiece.poweUp();

    // applies powerUp() to !powered GamePiece
    this.fourPiece.poweUp();

    // checks that is GamePiece is powered
    t.checkExpect(this.HoriPiece.powered, true);
    t.checkExpect(this.vertPiece.powered, true);
    t.checkExpect(this.fourPiece.powered, true);
  }

  // test for dePower()
  void testDePower(Tester t) {
    this.init();

    // applies dePower() to this powered GamePiece
    this.pwHoriPiece.dePower();

    // applies dePower() to this powered GamePiece
    this.pwVertPiece.dePower();

    // appliers dePower() to this !powered GamePiece
    this.vertPiece.dePower();

    // checks that this GamePiece is !powered
    t.checkExpect(this.pwHoriPiece.powered, false);
    t.checkExpect(this.pwVertPiece.powered, false);
    t.checkExpect(this.vertPiece.powered, false);

  }

  // test for rotate()
  void testRotate(Tester t) {
    this.init();

    // rotates the GamePiece with new openings on the top and bottom
    this.HoriPiece.rotate();

    // rotates the GamePiece with new openings on the left and right
    this.vertPiece.rotate();

    // rotates the GamePiece with new openings on all sides
    this.fourPiece.rotate();

    // checks that the openings for the GamePiece are appropriately set
    // now that they have been rotates
    t.checkExpect(this.HoriPiece.top && this.HoriPiece.bottom, true);
    t.checkExpect(this.vertPiece.left && this.vertPiece.right, true);
    t.checkExpect(
        this.fourPiece.top && this.fourPiece.right && this.fourPiece.bottom && this.fourPiece.left,
        true);
  }

  // test for dePowerBoard()
  void testdePowerBoard(Tester t) {
    this.init();

    // makes a 3by3 LightEmAll game that has been solved
    LightEmAll by3LightEmAll = new LightEmAll(3, 3, 0, 0);

    // powers up each GamePiece of this 3x3 LightEmAll
    for (ArrayList<GamePiece> column : by3LightEmAll.board) {
      for (GamePiece piece : column) {
        piece.poweUp();
      }
    }
  }
}