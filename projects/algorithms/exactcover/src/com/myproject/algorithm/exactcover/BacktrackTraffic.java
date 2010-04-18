/*
 * Copyright 2010 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.myproject.algorithm.exactcover;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 */
public class BacktrackTraffic {

    // element at left, up is [0, 0]

    public static int ROWS = 6;
    public static int COLS = 6;
    public static int MAGIC_ROW = 2;

    public class Element {
	final Movement movement;
	final int hsize;
	final int vsize;
	final String description;

	public Element(Movement movement, int hsize, int vsize,
		String description) {
	    this.movement = movement;
	    this.hsize = hsize;
	    this.vsize = vsize;
	    this.description = description;
	}
    }

    public static class BoardState {

	static class BoardBuilder {
	    final int rows;
	    final int cols;

	    BlockType builderState[][];

	    public BoardBuilder(int rows, int cols) {
		emptyBoard(rows, cols);
		this.rows = rows;
		this.cols = cols;
	    }

	    public BoardBuilder(BoardState boardState) {
		this.rows = boardState.state.length;
		this.cols = boardState.state[0].length;
		builderState = new BlockType[rows][cols];
		for (int i = 0; i < rows; i++) {
		    for (int j = 0; j < cols; j++) {
			builderState[i][j] = boardState.state[i][j];
		    }
		}
	    }

	    private void emptyBoard(int rows, int cols) {
		builderState = new BlockType[rows][cols];
		for (int i = 0; i < rows; i++) {
		    for (int j = 0; j < cols; j++) {
			builderState[i][j] = BlockType.EM;
		    }
		}
	    }

	    @Override
	    public String toString() {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < rows; i++) {
		    for (int j = 0; j < cols; j++) {
			BlockType block = builderState[i][j];
			if (block == BlockType.EM) {
			    builder.append("  ");
			} else {
			    builder.append(block.name());
			}
			builder.append(" ");
		    }
		    builder.append("\n");
		}
		builder.append("----------------\n\n");
		return builder.toString();
	    }

	    BoardBuilder addObstacle(int i, int j) {
		checkAndSet(i, j, BlockType.OB);
		return this;
	    }

	    private void checkAndSet(int i, int j, BlockType blockType) {
		if (i < 0 || i >= rows || j < 0 || j >= cols) {
		    throw new IllegalArgumentException("trying to set sqaure ("
			    + i + ", " + j + ") not on board");
		}
		if (builderState[i][j] != BlockType.EM) {
		    throw new IllegalArgumentException(" square (" + i + ", "
			    + j + ") is not empty");
		}
		builderState[i][j] = blockType;
	    }

	    BoardBuilder addHorizontalLimo(int i, int j) {
		checkAndSet(i, j, BlockType.HL);
		checkAndSet(i, j + 1, BlockType.HL);
		checkAndSet(i, j + 2, BlockType.HL);
		return this;
	    }

	    BoardBuilder addHorizontalCar(int i, int j) {
		checkAndSet(i, j, BlockType.HC);
		checkAndSet(i, j + 1, BlockType.HC);
		return this;
	    }

	    BoardBuilder addQueenCar(int i, int j) {
		checkAndSet(i, j, BlockType.QC);
		checkAndSet(i, j + 1, BlockType.QC);
		return this;
	    }

	    BoardBuilder addVerticalCar(int i, int j) {
		checkAndSet(i, j, BlockType.VC);
		checkAndSet(i + 1, j, BlockType.VC);
		return this;
	    }

	    BoardBuilder addVerticalLimo(int i, int j) {
		checkAndSet(i, j, BlockType.VL);
		checkAndSet(i + 1, j, BlockType.VL);
		checkAndSet(i + 2, j, BlockType.VL);
		return this;
	    }

	    BoardBuilder moveLeft(int row, int col, BlockType blockType) {
		assert blockType == BlockType.HC || blockType == BlockType.QC
			|| blockType == BlockType.HL;
		int changedCol = col + 1;
		if (blockType == BlockType.HL) {
		    changedCol = col + 2;
		}
		assert builderState[row][col - 1] == BlockType.EM;
		swapSameRow(row, col - 1, changedCol);
		return this;
	    }

	    /**
	     * swap values in row, col1 and row, col2.
	     */
	    private void swapSameRow(int row, int col1, int col2) {
		BlockType saved = builderState[row][col1];
		builderState[row][col1] = builderState[row][col2];
		builderState[row][col2] = saved;
	    }

	    /**
	     * swap values in row, col1 and row, col2.
	     */
	    private void swapSameCol(int col, int row1, int row2) {
		BlockType saved = builderState[row1][col];
		builderState[row1][col] = builderState[row2][col];
		builderState[row2][col] = saved;
	    }

	    BoardBuilder moveRight(int row, int col, BlockType blockType) {
		assert blockType == BlockType.HC || blockType == BlockType.QC
			|| blockType == BlockType.HL;
		int emptyCol = col + 2;
		if (blockType == BlockType.HL) {
		    emptyCol = col + 3;
		}
		assert builderState[row][emptyCol] == BlockType.EM;
		swapSameRow(row, col, emptyCol);
		return this;
	    }

	    BoardBuilder moveUp(int row, int col, BlockType blockType) {
		assert blockType == BlockType.VC || blockType == BlockType.VL;
		int changedRow = row + 1;
		if (blockType == BlockType.VL) {
		    changedRow = row + 2;
		}
		assert builderState[row - 1][col] == BlockType.EM;
		swapSameCol(col, row - 1, changedRow);
		return this;
	    }

	    BoardBuilder moveDown(int row, int col, BlockType blockType) {
		assert blockType == BlockType.VC || blockType == BlockType.VL;
		int emptyRow = row + 2;
		if (blockType == BlockType.VL) {
		    emptyRow = row + 3;
		}
		assert builderState[emptyRow][col] == BlockType.EM;
		swapSameCol(col, row, emptyRow);
		return this;
	    }

	    BoardState build() {
		return new BoardState(builderState);
	    }
	}

	public final BlockType state[][] = new BlockType[ROWS][COLS];

	private BoardState(BlockType state[][]) {
	    for (int i = 0; i < ROWS; i++) {
		for (int j = 0; j < COLS; j++) {
		    this.state[i][j] = state[i][j];
		}
	    }
	}

	/*
	 * Returns a list of BoardState from this initial state.
	 */
	public Set<BoardState> getAllNewStates() {
	    Set<BoardState> newStates = new HashSet<BoardState>();
	    // FIND empty blocks and see if any of the adjacent elements can
	    // move to this block.
	    for (int i = 0; i < ROWS; i++) {
		for (int j = 0; j < COLS; j++) {
		    if (state[i][j] == BlockType.EM) {
			newStates.addAll(getNewStates(i, j));
		    }
		}
	    }
	    return newStates;
	}

	/**
	 * state[row][column] is empty, getNewStates by some adjacent element
	 * moving to this block.
	 */
	private Set<BoardState> getNewStates(int row, int col) {
	    Set<BoardState> newStates = new HashSet<BoardState>();
	    BoardState newState = null;
	    if ((newState = moveRight(row, col)) != null) {
		newStates.add(newState);
	    }
	    if ((newState = moveLeft(row, col)) != null) {
		newStates.add(newState);
	    }
	    if ((newState = moveDown(row, col)) != null) {
		newStates.add(newState);
	    }
	    if ((newState = moveUp(row, col)) != null) {
		newStates.add(newState);
	    }
	    return newStates;
	}

	/**
	 * get a new state if one can be generated by moving an element right.
	 */
	private BoardState moveRight(int row, int col) {
	    if (col > 0) {
		BlockType left = state[row][col - 1];
		if (left == BlockType.HL || left == BlockType.HC
			|| left == BlockType.QC) {
		    int changedCol = col - 2;
		    if (left == BlockType.HL) {
			changedCol = col - 3;
		    }
		    BoardBuilder builder = new BoardBuilder(this);
		    return builder.moveRight(row, changedCol, left).build();
		}
	    }
	    return null;
	}

	/**
	 * get a new state if one can be generated by moving an element left.
	 */
	private BoardState moveLeft(int row, int col) {
	    if (col < COLS - 1) {
		BlockType right = state[row][col + 1];
		if (right == BlockType.HL || right == BlockType.HC
			|| right == BlockType.QC) {
		    BoardBuilder builder = new BoardBuilder(this);
		    return builder.moveLeft(row, col + 1, right).build();
		}
	    }
	    return null;
	}

	/**
	 * get a new state if one can be generated by moving an element up.
	 */
	private BoardState moveUp(int row, int col) {
	    if (row < ROWS - 1) {
		BlockType down = state[row + 1][col];
		if (down == BlockType.VL || down == BlockType.VC) {
		    BoardBuilder builder = new BoardBuilder(this);
		    return builder.moveUp(row + 1, col, down).build();
		}
	    }
	    return null;
	}

	/**
	 * get a new state if one can be generated by moving an element down.
	 */
	private BoardState moveDown(int row, int col) {
	    if (row > 0) {
		BlockType up = state[row - 1][col];
		if (up == BlockType.VL || up == BlockType.VC) {
		    int changedRow = row - 2;
		    if (up == BlockType.VL) {
			changedRow = row - 3;
		    }
		    BoardBuilder builder = new BoardBuilder(this);
		    return builder.moveDown(changedRow, col, up).build();
		}
	    }
	    return null;
	}

	/*
	 * returns true if goal state.
	 */
	public boolean isGoal() {
	    if (state[MAGIC_ROW][COLS - 1] == BlockType.QC) {
		return true;
	    }
	    return false;
	}

	@Override
	public boolean equals(Object other) {
	    if (!(other instanceof BoardState)) {
		return false;
	    }
	    BoardState otherBoard = (BoardState) other;
	    for (int i = 0; i < ROWS; i++) {
		for (int j = 0; j < COLS; j++) {
		    if (otherBoard.state[i][j] != state[i][j]) {
			return false;
		    }
		}
	    }
	    return true;
	}

	@Override
	public int hashCode() {
	    int hashCode = 0;
	    int numStates = BlockType.values().length;
	    for (int i = 0; i < ROWS; i++) {
		for (int j = 0; j < COLS; j++) {
		    hashCode = hashCode * numStates + state[i][j].ordinal();
		}
	    }
	    return hashCode;
	}

	@Override
	public String toString() {
	    StringBuilder builder = new StringBuilder();
	    for (int i = 0; i < ROWS; i++) {
		for (int j = 0; j < COLS; j++) {
		    BlockType block = state[i][j];
		    if (block == BlockType.EM) {
			builder.append("  ");
		    } else {
			builder.append(block.name());
		    }
		    builder.append(" ");
		}
		builder.append("\n");
	    }
	    builder.append("----------------\n\n");
	    return builder.toString();
	}

	public void checkConsistency() {
	    // TODO: finish this.
	    // check for queen car.

	    for (int i = 0; i < ROWS; i++) {
		for (int j = 0; j < COLS; j++) {
		    BlockType type = state[i][j];
		    switch (type) {
		    case OB:
		    case EM:
			// do nothing
			break;
		    case HL:
			break;
		    case HC:
		    case QC:
			break;
		    case VL:
			break;
		    case VC:
			break;
		    }
		}
	    }
	}
    }

    public enum BlockType {
	OB, EM, HL, VL, HC, VC, QC,
    }

    /**
     * Movement type.
     */
    private enum Movement {
	HORIZONTAL, VERTICAL, NONE,
    }

    final BoardState startState;
    final Set<BoardState> goalStates = new HashSet<BoardState>();

    Map<BoardState, Set<BoardState>> allParents = new HashMap<BoardState, Set<BoardState>>();
    List<BoardState> unexploredStates = new ArrayList<BoardState>();

    BacktrackTraffic(BoardState startState) {
	this.startState = startState;
    }

    public List<BoardState> solve() {
	findAllParents();
	if (goalStates.size() == 0) {
	    System.err.println(" no goal state");
	    return null;
	}
	System.out.println("found goal state");
	System.out.println("computing path");
	ShortestPathComputer<BoardState> c = new ShortestPathComputer<BoardState>(
		startState, goalStates, allParents);
	return c.getShortestPath();
    }

    /**
     * Find all states, and fill in the parents Map.
     */
    private void findAllParents() {
	unexploredStates.add(startState);
	while (unexploredStates.size() > 0) {
	    BoardState unexploredState = unexploredStates.remove(0);
	    for (BoardState newState : unexploredState.getAllNewStates()) {
		// if newState has a parent, it has been seen before.
		Set<BoardState> parents = allParents.get(newState);
		if (parents != null) {
		    // TODO: think about this
		    // assert !parents.contains(unexploredState);
		    parents.add(unexploredState);
		} else {
		    parents = new HashSet<BoardState>();
		    parents.add(unexploredState);
		    allParents.put(newState, parents);

		    unexploredStates.add(newState);
		    if (newState.isGoal()) {
			goalStates.add(newState);
		    }
		}
	    }
	}
    }

    /* Shortest path computer using djikstra's algorithm */
    class ShortestPathComputer<T> {
	final T startState;
	final Set<T> goalStates;
	final Map<T, Set<T>> allParents;

	Map<T, Integer> levels = new HashMap<T, Integer>();
	Map<T, T> backLinks = new HashMap<T, T>();

	public ShortestPathComputer(T startState, Set<T> goalStates,
		Map<T, Set<T>> allParents) {
	    this.startState = startState;
	    this.goalStates = goalStates;
	    this.allParents = allParents;
	}

	private void assignLevels() {
	    List<T> needProcessing = new ArrayList<T>();
	    for (T goalState : goalStates) {
		levels.put(goalState, 0);
		needProcessing.add(goalState);
	    }

	    while (needProcessing.size() > 0) {
		// Breadth-first-search
		T state = needProcessing.remove(0);
		Integer level = levels.get(state);
		assert level != null;

		Set<T> parents = allParents.get(state);
		for (T parent : parents) {
		    Integer oldLevel = levels.get(parent);
		    if (oldLevel != null && oldLevel <= (level + 1)) {
			continue;
		    }
		    levels.put(parent, level + 1);
		    needProcessing.add(parent);
		    backLinks.put(parent, state);
		}
	    }
	}

	public List<T> getShortestPath() {
	    assignLevels();
	    if (backLinks.get(startState) == null) {
		return null;
	    }
	    List<T> shortestPath = new ArrayList<T>();
	    T current = startState;
	    while (current != null) {
		shortestPath.add(current);
		current = backLinks.get(current);
	    }
	    return shortestPath;
	}
    }
}
