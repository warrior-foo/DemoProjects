package com.myproject.tictactoe.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.HasMouseOverHandlers;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.RootPanel;

import gwt.canvas.client.Canvas;

/*
 * * Entry point classes define <code>onModuleLoad()</code>.
 */
public class TTT implements EntryPoint {

  class CanvasWithHandlers extends Canvas implements HasClickHandlers,
      HasMouseOverHandlers {

    public CanvasWithHandlers(int width, int height) {
      super(width, height);
    }

    public HandlerRegistration addClickHandler(ClickHandler handler) {
      return addDomHandler(handler, ClickEvent.getType());
    }

    /* fire handlers instead of listeners */
    @Override
    public void onBrowserEvent(Event event) {
      DomEvent.fireNativeEvent(event, this, this.getElement());
    }

    // not used.
    public HandlerRegistration addMouseOverHandler(MouseOverHandler handler) {
      return addDomHandler(handler, MouseOverEvent.getType());
    }

  }

  private static String getHighlightedString(String str) {
    return "<div style=\"color:red;\">" + str + "</style>";
  }
  
  final static String MESSAGE_X = "X's turn";
  final static String MESSAGE_O = "O's turn";
  final static String WIN_X = getHighlightedString("X won. Start a new game.");
  final static String WIN_O = getHighlightedString("O won. Start a new game.");
  final static String DRAW_MESSAGE = getHighlightedString("Game drawn. Start a new game.");

  final int numSquares = 3;
  
  // these 3 variables are initialized later.
  int side = 100;
  int bigSide = numSquares * side;
  int offset = side / 10;

  int xoffset = 0;
  int yoffset = 0;

  

  enum ValueType {
    CIRCLE, CROSS, EMPTY
  }

  enum GameStage {
    IN_PROGRESS, DRAW, DONE,
  }
  
  ValueType nextMoveType = ValueType.CIRCLE;
  GameStage gameStage = GameStage.IN_PROGRESS;
  int clicked = 0;
  CanvasWithHandlers canvas;
  DivElement messageDiv;

  ValueType filled[][] = new ValueType[numSquares][numSquares];

  /**
   * This is the entry point method.
   */
  public void onModuleLoad() {
    int clientHeight = Window.getClientHeight() - 150;
    int clientWidth = Window.getClientWidth();
    bigSide = clientWidth;
    if (clientWidth > clientHeight) {
      bigSide = clientHeight;
    }
    {
      // initialize everything
      side = bigSide / numSquares;
      bigSide = side * numSquares;
      offset = side / 10;
    }
    canvas = new CanvasWithHandlers(bigSide, bigSide);
    RootPanel.get("canvas").add(canvas);
    messageDiv = Document.get().getElementById("message").cast();
    ClickHandler handler = new ClickHandler() {
      public void onClick(ClickEvent event) {
        if (gameStage != GameStage.IN_PROGRESS) {
          return;
        }
        int x = event.getNativeEvent().getClientX();
        int y = event.getNativeEvent().getClientY();
        // System.out.println("x = " + x + "y = " + y);
        x = x - xoffset;
        y = y - canvas.getAbsoluteTop();
        // System.out.println("net y = " + y);
        if (x > 0 && x < bigSide && y > 0 && y < bigSide) {
          boolean newlyClicked = paintSquare((x / side) * side, (y / side)
              * side);
          if (newlyClicked) {
            displayMessage();
          }
        }
      }
    };

    Button button = new Button("New Game");
    RootPanel.get("button").add(button);
    button.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        // System.out.println("calling reset");
        reset();
      }
    });

    /*
     * ClickListener listener = new ClickListener() {
     * 
     * public void onClick(Widget sender) { System.out.println("in onClick");
     * paintSquare((clicked / numSquares) * side, (clicked % numSquares) side);
     * clicked++; } };
     */
    canvas.addClickHandler(handler);
    // canvas.addClickListener(listener);
    reset();
  }

  private void reset() {
    canvas.setWidth(canvas.getWidth()); // clear the canvas.
    canvas.clear();
    gameStage = GameStage.IN_PROGRESS;
    xoffset = canvas.getAbsoluteLeft();
    yoffset = canvas.getAbsoluteTop();
    // System.out.println("xoffset = " + xoffset + ", yoffset = " + yoffset);

    canvas.setBackgroundColor("#AAAAAA");
    displayMessage();

    canvas.setLineWidth(1.0);
    canvas.setStrokeStyle("#0000FF");
    // canvas.setStrokeStyle("#000000");
    for (int j = side; j < bigSide; j += side) {
      // horizontal lines
      canvas.moveTo(0, j);
      canvas.lineTo(bigSide, j);
      // vertical lines
      canvas.moveTo(j, 0);
      canvas.lineTo(j, bigSide);
      canvas.stroke();
    }

    for (int i = 0; i < numSquares; i++) {
      for (int j = 0; j < numSquares; j++) {
        filled[i][j] = ValueType.EMPTY;
      }
    }
  }

  private String getDisplayMessage() {
    if (nextMoveType == ValueType.CIRCLE) {
      switch (gameStage) {
        case IN_PROGRESS: return MESSAGE_O;
        case DRAW: return DRAW_MESSAGE;
        case DONE: return WIN_X;
        default: 
          throw new RuntimeException("Illegal Enum type");
      }
    }
    switch (gameStage) {
      case IN_PROGRESS: return MESSAGE_X;
      case DRAW: return DRAW_MESSAGE;
      case DONE: return WIN_O;
      default: 
        throw new RuntimeException("Illegal Enum type");
    }
  }

  private void displayMessage() {
    try {
      messageDiv.setInnerHTML(getDisplayMessage());
    } catch (Exception ex) {
      System.err.println("message: " + ex.getMessage());
      ex.printStackTrace();
    }
  }

  /* Returns true if the square is painted fresh */
  boolean paintSquare(int xstart, int ystart) {
    assert (xstart % side == 0);
    assert (ystart % side == 0);

    if (canvas == null) {
      return false;
    }
    if (filled[xstart / side][ystart / side] != ValueType.EMPTY) {
      return false;
    }
    if (nextMoveType == ValueType.CROSS) {
      canvas.moveTo(xstart + offset, ystart + offset);
      canvas.lineTo(xstart + side - offset, ystart + side - offset);
      canvas.moveTo(xstart + side - offset, ystart + offset);
      canvas.lineTo(xstart + offset, ystart + side - offset);
      canvas.stroke();
      filled[xstart / side][ystart / side] = ValueType.CROSS;
      nextMoveType = ValueType.CIRCLE;
      checkBoard(xstart / side, ystart / side);
      return true;
    }
    assert (nextMoveType == ValueType.CIRCLE);
    canvas.moveTo(xstart + side - offset, ystart + side / 2);
    canvas.arc(xstart + (side / 2), ystart + (side / 2), side / 2 - offset, 0,
        2 * Math.PI, true);
    canvas.stroke();
    filled[xstart / side][ystart / side] = ValueType.CIRCLE;
    nextMoveType = ValueType.CROSS;
    checkBoard(xstart / side, ystart / side);
    return true;
  }

  private void checkBoard(int newX, int newY) {
    if (checkWin(newX, newY)) {
      gameStage = GameStage.DONE;
      return;
    }
    if (checkDraw()) {
      gameStage = GameStage.DRAW;
    }
  }
  
  /**
   * returns true if there is a draw.
   */
  private boolean checkDraw() {
    for (int i = 0; i < numSquares; i++) {
      for (int j = 0; j < numSquares; j++) {
        if (filled[i][j] == ValueType.EMPTY) {
          return false;
        }
      }
    }
    return true;
  }
  
  /**
   * returns true if there is a winner.
   */
  private boolean checkWin(int newX, int newY) {
    ValueType lastMoveType = filled[newX][newY];
    return (doneHorizontal(lastMoveType, newX) || doneVertical(lastMoveType, newY)
        || doneDiagonalXEqualsY(newX, newY)
        || doneDiagonalXYEqualsSide(newX, newY));
  }

  private boolean doneDiagonalXYEqualsSide(int newX, int newY) {
    if (newX + newY != (numSquares - 1)) {
      return false;
    }
    for (int i = 0; i < numSquares; i++) {
      if (filled[i][numSquares - i - 1] != filled[newX][newY]) {
        return false;
      }
    }
    // canvas.setStrokeStyle("#FF0000");
    canvas.moveTo(bigSide - offset + offset/2, offset);
    canvas.lineTo(offset - offset/2, bigSide - offset);
    canvas.stroke();
    return true;
  }

  private boolean doneDiagonalXEqualsY(int newX, int newY) {
    if (newX != newY) {
      return false;
    }
    for (int i = 0; i < numSquares; i++) {
      if (filled[i][i] != filled[newX][newY]) {
        return false;
      }
    }
    // canvas.setStrokeStyle("#FF0000");
    canvas.moveTo(offset - offset/2, offset);
    canvas.lineTo(bigSide - offset + offset/2, bigSide - offset);
    canvas.stroke();
    return true;
  }

  private boolean doneVertical(ValueType lastMoveType, int newY) {
    for (int i = 0; i < numSquares; i++) {
      if (filled[i][newY] != lastMoveType) {
        return false;
      }
    }
    // canvas.setStrokeStyle("#FF0000");
    canvas.moveTo(offset, newY * side + side/2);
    canvas.lineTo(bigSide - offset, newY * side + side/2);
    canvas.stroke();
    return true;
  }

  /**
   * returns true if last player has won.
   */
  private boolean doneHorizontal(ValueType lastMoveType, int newX) {
    for (int i = 0; i < numSquares; i++) {
      if (filled[newX][i] != lastMoveType) {
        return false;
      }
    }
    // canvas.setStrokeStyle("#FF0000");
    canvas.moveTo(newX * side + side/2, offset);
    canvas.lineTo(newX * side + side/2, bigSide - offset);
    canvas.stroke();
    return true;
  }

}
