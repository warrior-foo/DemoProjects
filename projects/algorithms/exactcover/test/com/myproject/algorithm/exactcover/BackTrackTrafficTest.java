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

import com.myproject.algorithm.exactcover.BacktrackTraffic.BoardState;
import com.myproject.algorithm.exactcover.BacktrackTraffic.BoardState.BoardBuilder;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

/**
 * Test for backtrack traffic.
 */
public class BackTrackTrafficTest extends TestCase {

    public void test1() {
	for (BoardState initialState : getGames()) {
	    BacktrackTraffic bt = new BacktrackTraffic(initialState);
	    List<BoardState> solution = bt.solve();
	    assertTrue (solution.size() > 0);
	    // print(solution);
	}
    }

    @SuppressWarnings("unused")
    private void print(List<BoardState> solution) {
	for (BoardState board : solution) {
	    System.out.println(board);
	}
    }

    private List<BoardState> getGames() {
	List<BoardState> initialStates = new ArrayList<BoardState>();
	BoardBuilder game_1_1 = new BoardBuilder(6, 6);
//	initialStates.add(game_1_1.addObstacle(0, 0).addObstacle(4, 0)
//		.addVerticalLimo(0, 5).addVerticalCar(1, 3)
//		.addVerticalCar(1, 4).addQueenCar(2, 0).addVerticalLimo(3, 1)
//		.addHorizontalLimo(3, 3).build());

	BoardBuilder game_2_16 = new BoardBuilder(6, 6);
	initialStates.add(game_2_16.addHorizontalLimo(0, 0)
		.addVerticalCar(0, 3).addObstacle(1, 0).addVerticalCar(1, 1)
		.addVerticalLimo(1, 4).addVerticalLimo(1, 5).addQueenCar(2, 2)
		.addHorizontalCar(3, 0).addVerticalCar(3, 2).addObstacle(4, 0)
		.addHorizontalCar(4, 3).addHorizontalLimo(5, 0).build());

	BoardBuilder game_2_18 = new BoardBuilder(6, 6);
//	initialStates.add(game_2_18.addHorizontalLimo(0, 0)
//		.addVerticalCar(0, 3).addVerticalCar(0, 5).addVerticalCar(1, 0)
//		.addVerticalLimo(1, 4).addQueenCar(2, 1).addHorizontalCar(3, 0)
//		.addVerticalCar(3, 2).addObstacle(4, 0).addVerticalCar(4, 1)
//		.addHorizontalCar(4, 3).addHorizontalLimo(5, 2).addObstacle(5,
//			5).build());

	return initialStates;
    }
}
