package org.example;

public class Card {
    private final String suit;
    private final String rank;
    private final int value;

    /**
     * assigns parameters to the object Card
     * @param cardSuit the suit of the card
     * @param cardRank the rank of the card
     * @param cardValue the value of the card in the game Hearts
     */
    public Card(String cardSuit, String cardRank, int cardValue) {
        this.suit = cardSuit;
        this.rank = cardRank;
        this.value = cardValue;
    }

    /**
     * Returns the suit of the card
     * @return suit of the card
     */
    public String getSuit() {
        return suit;
    }

    /**
     * Returns the rank of the card
     * @return rank of the card
     */
    public String getRank() {
        return rank;
    }

    /**
     * Returns the value of the card
     * @return value of the card
     */
    public int getValue() {
        return value;
    }

    /**
     * Combines the suit and rank into one string
     * @return the combined string
     */
    public String turnToString(){
       return this.suit + this.rank;
    }

    /**
     * Tests if the card's suit is Hearts
     * @return true if the suit is Hearts and false otherwise
     */
    public boolean isHeart(){
        return suit.equals("H");
    }

    /**
     * Tests if the card's rank is a Queen
     * @return true if the rank is a Queen and false otherwise
     */
    public boolean isQueen(){
        return rank.equals("Q");
    }

    /**
     * Tests if the card's rank is a King or a Jack
     * @return true if the card's rank is a King or a Jack and false otherwise
     */
    public boolean isMale(){
        return (rank.equals("J") || rank.equals("K"));
    }

    /**
     * Tests if the card is a King of Hearts
     * @return true if the card is a King of Hearts and false otherwise
     */
    public boolean isHeartKing(){
        return (rank.equals("K") && suit.equals("H"));
    }
}