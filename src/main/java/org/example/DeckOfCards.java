package org.example;

import java.util.*;

public class DeckOfCards {
    private static final int size = 52;
    private static final Card[] deckOfCards = new Card[size];

    /**
     * Creates a standard 52-card Deck with assigned suit, rank and value to every card
     * @return created deck of cards
     */
    public static Card[] getDeckOfCards() {
        int count = 0;

        String[] suits = {"D", "C", "H", "S"};
        String[] ranks = {"A", "K", "Q", "J", "10", "9", "8", "7", "6", "5", "4", "3", "2"};

        for (String s : suits) {
            int v = 14;
            for (String r : ranks) {
                Card card = new Card(s, r, v);
                deckOfCards[count] = card;
                count++;
                v--;
            }
        }
        return deckOfCards;
    }

    /**
     * Shuffles a deck of cards
     * @param deckOfCards an unshuffled deck of cards
     * @return a shuffled deck of cards
     */
    public static Card[] shuffleCards(Card[] deckOfCards) {
        Random rand = new Random();
        int j;
        for (int i = 0; i < size; i++) {
            j = rand.nextInt(size);
            Card temp = deckOfCards[i];
            deckOfCards[i] = deckOfCards[j];
            deckOfCards[j] = temp;
        }
        return deckOfCards;
    }
}
