package com.myproject.algorithm.exactcover;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of the DancingLinks algorithm.
 */
public class DancingLinks {

    private final String ROOT_HEADER = "ROOT";
    private final String COL_HEADER = "COLUMN HEADER";

    final int numRows;
    final int numColumns;
    boolean values[][];

    Node rootNode;

    public DancingLinks(int numRows, int numColumns, boolean values[][]) {
	this.numRows = numRows;
	this.numColumns = numColumns;
	assert numRows > 0;
	assert numColumns > 0;
	assert values.length == numRows;
	for (int i = 0; i < numRows; i++) {
	    assert values[i].length == numColumns;
	}
	this.solution = new int[this.numRows];
	this.values = values;
	// check each row as at least one true value.
	for (int i = 0; i < numRows; i++) {
	    boolean found = false;
	    for (int j = 0; j < numColumns; j++) {
		found = found || values[i][j];
	    }
	    if (!found) {
		throw new IllegalArgumentException("row " + i + " is all false");
	    }
	}

	// check each column has at least one true value.
	for (int j = 0; j < numColumns; j++) {
	    boolean found = false;
	    for (int i = 0; i < numRows; i++) {
		found = found || values[i][j];
	    }
	    if (!found) {
		throw new IllegalArgumentException("column " + j
			+ " is all false");
		// assuming that there is a valid solution.
	    }
	}
	initialize();

	sanityCheck();
    }

    /**
     * Sanity check the constructed matrix.
     */
    private void sanityCheck() {
	int numTrue = 0;
	for (int i = 0; i < numRows; i++) {
	    for (int j = 0; j < numColumns; j++) {
		if (values[i][j]) {
		    numTrue++;
		}
	    }
	}

	int traverseColumns = 0;
	for (Node colHeader = rootNode.right; colHeader != rootNode; colHeader = colHeader.right) {
	    for (Node colNode = colHeader.down; colNode != colHeader; colNode = colNode.down) {
		traverseColumns++;
	    }
	}
	assert numTrue == traverseColumns;

    }

    /**
     * intialize the matrix.
     */
    private void initialize() {
	rootNode = new Node(-1, -1, ROOT_HEADER);
	rootNode.header = null;
	Node prevNode = rootNode;
	for (int j = 0; j < numColumns; j++) {
	    Node colHeader = new Node(-1, j, COL_HEADER);
	    colHeader.header = colHeader;
	    colHeader.left = prevNode;
	    prevNode.right = colHeader;

	    prevNode = colHeader;
	}
	// prevNode is the last node now.
	rootNode.left = prevNode;
	prevNode.right = rootNode;

	// inefficient construction
	Map<String, Node> lookupMap = new HashMap<String, Node>();

	// hook up all the up-down links
	for (Node colHeader = rootNode.right; colHeader != rootNode; colHeader = colHeader.right) {
	    int column = colHeader.column;
	    prevNode = colHeader;
	    for (int i = 0; i < numRows; i++) {
		if (values[i][column]) {
		    Node valueNode = new Node(i, column, "valueNode");
		    valueNode.up = prevNode;
		    prevNode.down = valueNode;
		    valueNode.header = colHeader;
		    lookupMap.put(i + "," + column, valueNode);

		    prevNode = valueNode;
		}
	    }
	    prevNode.down = colHeader;
	    colHeader.up = prevNode;
	}

	// hook up the left-right links.
	for (int i = 0; i < numRows; i++) {
	    Node firstNode = null;
	    prevNode = null;
	    for (int j = 0; j < numColumns; j++) {
		if (values[i][j]) {
		    Node valueNode = lookupMap.get(i + "," + j);
		    assert valueNode != null;
		    if (firstNode == null) {
			firstNode = valueNode;
		    } else {
			valueNode.left = prevNode;
			prevNode.right = valueNode;
		    }
		    prevNode = valueNode;
		}
	    }
	    prevNode.right = firstNode;
	    firstNode.left = prevNode;
	}
    }

    /**
     * A node structure for solving exact cover in sparse matrices.
     */
    class Node {
	Node left;
	Node right;
	Node up;
	Node down;
	Node header; // pointer to the column header.

	// the actual row, column.
	final int row;
	final int column;
	final String identifier;

	Node(int row, int column, String identifier) {
	    this.row = row;
	    this.column = column;
	    this.identifier = identifier;
	}
    }

    /** number of columns covered so far */
    int coveredColumns = 0;
    /** number of rows selected so far */
    int selectedRows = 0;
    /** the row numbers selected so far */
    int solution[];

    /**
     * Cover column denoted by columnHeader 'header'.
     */
    private void coverColumn(Node colHeader) {
	assert colHeader.identifier.equals(COL_HEADER);
	/* remove the column header */
	colHeader.left.right = colHeader.right;
	colHeader.right.left = colHeader.left;

	for (Node rowNode = colHeader.down; rowNode != colHeader; rowNode = rowNode.down) {
	    for (Node rightNode = rowNode.right; rightNode != rowNode; rightNode = rightNode.right) {
		rightNode.up.down = rightNode.down;
		rightNode.down.up = rightNode.up;
	    }
	}
	coveredColumns++;
    }

    /**
     * Uncover column.
     */
    private void uncoverColumn(Node colHeader) {
	assert colHeader.identifier.equals(COL_HEADER);
	for (Node rowNode = colHeader.down; rowNode != colHeader; rowNode = rowNode.down) {
	    for (Node rightNode = rowNode.right; rightNode != rowNode; rightNode = rightNode.right) {
		rightNode.up.down = rightNode;
		rightNode.down.up = rightNode;
	    }
	}
	colHeader.left.right = colHeader;
	colHeader.right.left = colHeader;
	coveredColumns--;
    }

    public boolean solutionStep() {

	if (rootNode.right == rootNode && rootNode.left == rootNode
		&& coveredColumns == numColumns) {
	    System.out.println("found solution");
	    for (int i = 0; i < selectedRows; i++) {
		System.out.print(solution[i] + ", ");
	    }
	    System.out.println();
	    return true;
	}

	// choose a column
	Node colHeader = rootNode.right;
	coverColumn(colHeader);
	for (Node rowNode = colHeader.down; rowNode != colHeader; rowNode = rowNode.down) {
	    solution[selectedRows++] = rowNode.row;
	    for (Node colNode = rowNode.right; colNode != rowNode; colNode = colNode.right) {
		coverColumn(colNode.header);
	    }

	    if (solutionStep()) {
		return true;
	    }
	    for (Node colNode = rowNode.right; colNode != rowNode; colNode = colNode.right) {
		uncoverColumn(colNode.header);
	    }
	    solution[selectedRows--] = 0;
	}
	uncoverColumn(colHeader);
	return false;
    }

}