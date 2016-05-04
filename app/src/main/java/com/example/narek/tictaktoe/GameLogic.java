package com.example.narek.tictaktoe;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Narek on 3/28/16.
 */
public class GameLogic {
    Level level = Level.EAZY;

    private int[][] board = {
            {0, 0, 0},
            {0, 0, 0},
            {0, 0, 0}
    };

    public void setLevel(Level level) {
        this.level = level;
    }

    public int[][] getBoard() {
        return board;
    }


    int[] move() {
        int[] result = minimax(level.getLevelDepth(), Seed.oppSeed); // depth, max turn
        return new int[]{result[1], result[2]};   // row, col
    }


    private int[] minimax(int depth, Seed player) {
        List<int[]> nextMoves = generateMoves();

        int bestScore = (player == Seed.mySeed) ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        int currentScore;
        int bestRow = -1;
        int bestCol = -1;

        if (nextMoves.isEmpty() || depth == 0) {
            bestScore = evaluate();
            if (bestScore == 100 || bestScore == -100) {

            }
        } else {
            for (int[] move : nextMoves) {
                setSEED(move[0], move[1], player);
                if (player == Seed.mySeed) {  // mySeed (computer) is maximizing player
                    currentScore = minimax(depth - 1, Seed.oppSeed)[0];
                    if (currentScore > bestScore) {
                        bestScore = currentScore;
                        bestRow = move[0];
                        bestCol = move[1];
                    }
                } else {  // oppSeed is minimizing player
                    currentScore = minimax(depth - 1, Seed.mySeed)[0];
                    if (currentScore < bestScore) {
                        bestScore = currentScore;
                        bestRow = move[0];
                        bestCol = move[1];
                    }
                }
                // Undo move
                setSEED(move[0], move[1], Seed.EMPTY);
            }
        }
        return new int[]{bestScore, bestRow, bestCol};
    }

    /**
     * Find all valid next moves.
     * Return List of moves in int[2] of {row, col} or empty list if gameover
     */
    private List<int[]> generateMoves() {
        List<int[]> nextMoves = new ArrayList<>(); // allocate List

        // If gameover, i.e., no next move
        if (hasWon(Seed.mySeed) || hasWon(Seed.oppSeed)) {
            return nextMoves;   // return empty list
        }

        // Search for empty cells and add to the List
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                if (content(row, col) == Seed.EMPTY) {
                    nextMoves.add(new int[]{row, col});
                }
            }
        }
        return nextMoves;
    }

    private int evaluate() {
        int score = 0;
        // Evaluate score for each of the 8 lines (3 rows, 3 columns, 2 diagonals)
        score += evaluateLine(0, 0, 0, 1, 0, 2);  // row 0
        score += evaluateLine(1, 0, 1, 1, 1, 2);  // row 1
        score += evaluateLine(2, 0, 2, 1, 2, 2);  // row 2
        score += evaluateLine(0, 0, 1, 0, 2, 0);  // col 0
        score += evaluateLine(0, 1, 1, 1, 2, 1);  // col 1
        score += evaluateLine(0, 2, 1, 2, 2, 2);  // col 2
        score += evaluateLine(0, 0, 1, 1, 2, 2);  // diagonal
        score += evaluateLine(0, 2, 1, 1, 2, 0);  // alternate diagonal
        return score;
    }


    private int evaluateLine(int row1, int col1, int row2, int col2, int row3, int col3) {
        int score = 0;

        // First cell
        if (content(row1, col1) == Seed.mySeed) {
            score = 1;
        } else if (content(row1, col1) == Seed.oppSeed) {
            score = -1;
        }

        // Second cell
        if (content(row2, col2) == Seed.mySeed) {
            if (score == 1) {   // cell1 is mySeed
                score = 10;
            } else if (score == -1) {  // cell1 is oppSeed
                return 0;
            } else {  // cell1 is empty
                score = 1;
            }
        } else if (content(row2, col2) == Seed.oppSeed) {
            if (score == -1) { // cell1 is oppSeed
                score = -10;
            } else if (score == 1) { // cell1 is mySeed
                return 0;
            } else {  // cell1 is empty
                score = -1;
            }
        }

        // Third cell
        if (content(row3, col3) == Seed.mySeed) {
            if (score > 0) {  // cell1 and/or cell2 is mySeed
                score *= 10;
            } else if (score < 0) {  // cell1 and/or cell2 is oppSeed
                return 0;
            } else {  // cell1 and cell2 are empty
                score = 1;
            }
        } else if (content(row3, col3) == Seed.oppSeed) {
            if (score < 0) {  // cell1 and/or cell2 is oppSeed
                score *= 10;
            } else if (score > 0) {  // cell1 and/or cell2 is mySeed
                return 0;
            } else {  // cell1 and cell2 are empty
                score = -1;
            }
        }
        return score;
    }

    private int[] winningPatterns = {
            0b111000000, 0b000111000, 0b000000111, // rows
            0b100100100, 0b010010010, 0b001001001, // cols
            0b100010001, 0b001010100               // diagonals
    };


    private boolean hasWon(Seed thePlayer) {
        int pattern = 0b000000000;  // 9-bit pattern for the 9 cells
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 3; ++col) {
                if (content(row, col) == thePlayer) {

                    pattern |= (1 << (row * 3 + col));
                }
            }
        }
        for (int winningPattern : winningPatterns) {
            if ((pattern & winningPattern) == winningPattern) return true;
        }
        return false;
    }


    private Seed content(int rowIndex, int colIndex) {
        if (board[rowIndex][colIndex] == 1) {
            return Seed.mySeed;
        } else if (board[rowIndex][colIndex] == -1) {
            return Seed.oppSeed;
        }

        return Seed.EMPTY;
    }

    private void setSEED(int row, int col, Seed seed) {
        if (seed == Seed.mySeed) {
            board[row][col] = 1;
        } else if (seed == Seed.oppSeed) {
            board[row][col] = -1;

        } else {
            board[row][col] = 0;

        }
    }

    enum Seed {
        mySeed, EMPTY, oppSeed
    }


}
