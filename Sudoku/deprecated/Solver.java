import components.simplereader.SimpleReader;
import components.simplereader.SimpleReader1L;
import components.simplewriter.SimpleWriter;
import components.simplewriter.SimpleWriter1L;

public final class Solver {

    /**
     * kind of stupid because normal rules have the 3x3 structure but oh well.
     */
    private static final int RADIX = 9;

    /**
     * No argument constructor--private to prevent instantiation.
     */
    private Solver() {
        // no code needed here
    }

//    private static boolean valueAllowed(int row, int column, int val, SudokuBoard board) {
//        assert val <= RADIX && val > 0 : "value is invalid for radix.";
//        assert row < RADIX && row >= 0 : "row must be within radix.";
//        assert column < RADIX && column >= 0 : "column must be within radix.";
//
//        for (int i = 0; i < RADIX; i++) {
//            if (val == board.retrieve(row, i) && i != column) {
//                return false;
//            }
//            if (val == board.retrieve(i, column) && i != row) {
//                return false;
//            }
//        }
//        int boxRow = (row / 3) * 3;
//        int boxCol = (column / 3) * 3;
//
//        for (int i = 0; i < 3; i++) {
//            for (int j = 0; j < 3; j++) {
//                if (val == board.retrieve(boxRow + i, boxCol + j)
//                        && (boxRow + i != row || boxCol + j != column)) {
//                    return false;
//                }
//            }
//        }
//        return true;
//    }
//
//    private static boolean verify(SudokuBoard board) {
//        for (int row = 0; row < RADIX; row++) {
//            for (int col = 0; col < RADIX; col++) {
//                if (board.retrieve(row, col) != 0) {
//                    if (!valueAllowed(row, col, board.retrieve(row, col), board)) {
//                        return false;
//                    }
//                }
//            }
//        }
//        return true;
//    }
//
//    private static boolean verifyStrict(SudokuBoard board) {
//        for (int row = 0; row < RADIX; row++) {
//            for (int col = 0; col < RADIX; col++) {
//                if (board.retrieve(row, col) != 0) {
//                    if (!valueAllowed(row, col, board.retrieve(row, col), board)) {
//                        return false;
//                    }
//                } else {
//                    return false;
//                }
//            }
//        }
//        return true;
//    }

    public static boolean solveRecursive(SudokuBoard board, SimpleWriter out) {
        board.propogateAll();

        for (int row = 0; row < board.getRadix(); row++) {
            for (int col = 0; col < board.getRadix(); col++) {

                if (board.retrieve(row, col) == 0) {

                    for (int val : board.availableValues(row, col)) {
                        SudokuBoard copy = board.copyOf();
                        copy.setPropogate(row, col, val);

                        if (!copy.verify()) {
                            continue;
                        }

                        out.println("Trying (" + (col + 1) + ", " + (char) ('A' + row)
                                + ") = " + val);
                        copy.print(out);

                        if (solveRecursive(copy, out)) {
                            board.copy(copy);
                            return true;
                        } else {
                            out.println("Backtracking from (" + (col + 1) + ", "
                                    + (char) ('A' + row) + ") = " + val);
                        }
                    }

                    return false; // no value worked
                }
            }
        }

        // no empty cells
        return board.verifyStrict();
    }

    /**
     * Main method.
     *
     * @param args
     *            the command line arguments; unused here
     */
    public static void main(String[] args) {
        SimpleWriter out = new SimpleWriter1L();

        SudokuBoard board = new SudokuBoard(RADIX);

        // A valid Sudoku board (0 = empty)
//        int[][] preset = { { 5, 3, 4, 6, 7, 8, 9, 1, 2 }, { 6, 7, 2, 1, 9, 5, 3, 4, 8 },
//                { 1, 9, 8, 3, 4, 2, 5, 6, 7 },
//
//                { 8, 5, 9, 7, 6, 1, 4, 2, 3 }, { 4, 2, 6, 8, 5, 3, 7, 9, 1 },
//                { 7, 1, 3, 9, 2, 4, 8, 5, 6 },
//
//                { 9, 6, 1, 5, 3, 7, 2, 8, 4 }, { 2, 8, 7, 4, 1, 9, 6, 3, 5 },
//                { 3, 4, 5, 2, 8, 6, 1, 7, 9 } };
        int[][] preset = { { 1, 0, 0, 0, 6, 0, 0, 0, 0 }, { 0, 2, 0, 0, 7, 0, 0, 3, 0 },
                { 0, 0, 3, 0, 8, 0, 0, 7, 0 },

                { 0, 6, 0, 4, 9, 0, 0, 0, 2 }, { 0, 0, 0, 0, 5, 0, 0, 1, 0 },
                { 0, 4, 0, 0, 1, 6, 0, 0, 0 },

                { 0, 0, 0, 0, 2, 0, 7, 0, 0 }, { 0, 0, 5, 0, 3, 0, 0, 8, 0 },
                { 7, 0, 0, 0, 4, 0, 0, 0, 9 } };

        // Remove a few values to let the solver fill them
//        preset[0][2] = 0;
//        preset[1][3] = 0;
//        preset[2][5] = 0;
//        preset[3][0] = 0;
//        preset[4][4] = 0;
//        preset[5][8] = 0;
//        preset[6][6] = 0;
//        preset[7][1] = 0;
//        preset[8][7] = 0;

        // Load into board
        for (int row = 0; row < RADIX; row++) {
            for (int col = 0; col < RADIX; col++) {
                if (preset[row][col] != 0) {
                    board.set(row, col, preset[row][col]);
                }
            }
        }

        board.print(out);
        out.println(board.verify());
//        for (int row = 0; row < RADIX; row++) {
//            for (int col = 0; col < RADIX; col++) {
//                out.println(board.availableValues(row, col));
//
//            }
//        }

        // greedy fill
        //  this will be the main section of the project along with a simple
        //  sudoku creator and saver for easier testing
        //  the idea from here is to store potential options and strike them
        //  out recursively until we can start filling in values
        //  in theory (although I'm not sure how deterministic sudoku is)
        //  there will be solvable cases where that method cannot eliminate enough
        //  options (requiring some amount of more sophisticated brute force)
        //  which will be the hardest case to catch
//        for (int row = 0; row < RADIX; row++) {
//            for (int col = 0; col < RADIX; col++) {
//                if (board.retrieve(row, col) == 0) {
//                    int k = 1;
//                    boolean setValue = false;
//                    while (k <= RADIX && !setValue) {
//                        if (board.valueAllowed(row, col, k)) {
//                            board.set(row, col, k);
//                            setValue = true;
//                        }
//                        k++;
//                    }
//                }
//            }
//        }
        //board.solve();
        board.propogateAll();

        out.println("\n");
        board.print(out);

        for (int row = 0; row < RADIX; row++) {
            for (int col = 0; col < RADIX; col++) {
                out.println(board.availableValues(row, col));
            }
        }

        out.println(board.verify());
        out.println(board.verifyStrict());

        SimpleWriter save = new SimpleWriter1L("data/board.txt");

        board.save(save);

        save.close();

        SimpleReader read = new SimpleReader1L("data/board.txt");

        board.load(read);

        read.close();

        board.print(out);

        out.close();
    }

}
