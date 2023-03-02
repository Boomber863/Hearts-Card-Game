package org.example;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CardTest {

    Card card;


    @Test
    void testGetSuit() {
        card = new Card("D","11",11);
        assertEquals("D", card.getSuit());
    }

    @Test
    void testGetRank() {
        card = new Card("S","A",14);
        assertEquals("A", card.getRank());
    }

    @Test
    void testGetValue() {
        card = new Card("C","2",2);
        assertNotEquals(3, card.getValue());
    }

    @Test
    void testTurnToString() {
        card = new Card("H","10",10);
        assertEquals("H10", card.turnToString());
    }

    @Test
    void testIsHeart() {
        card = new Card("H","2",2);
        assertTrue(card.isHeart());
    }

    @Test
    void testIsQueen() {
        card = new Card("D","4",4);
        assertFalse(card.isQueen());
    }

    @Test
    void testIsMale() {
        card = new Card("D","J",11);
        assertTrue(card.isMale());
    }

    @Test
    void testIsHeartKing() {
        card = new Card("S","K",13);
        assertFalse(card.isHeartKing());
    }

}