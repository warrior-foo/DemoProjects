package com.myproject.algorithm.exactcover;

import junit.framework.TestCase;

/**
 * Test DancingLinks.
 */
public class DancingLinksTest extends TestCase {

    public void test1() {
	DancingLinks dl = new DancingLinks(2, 2, new boolean[][] {
		{ true, true }, { true, false } });
	assertTrue (dl.solutionStep());
    }

    public void test2() {
	DancingLinks dl = new DancingLinks(2, 2, new boolean[][] {
		{ false, true }, { true, false } });
	assertTrue(dl.solutionStep());
    }

    public void test3() {
	DancingLinks dl = new DancingLinks(3, 3, new boolean[][] {
		{ true, false, false }, { false, true, false },
		{ false, false, true } });
	assertTrue (dl.solutionStep());
    }

    public void test4() {
	DancingLinks dl = new DancingLinks(3, 3, new boolean[][] {
		{true, true, false}, {true, false, true}, {false, true, true}});
	assertFalse(dl.solutionStep());
    }
    
}
