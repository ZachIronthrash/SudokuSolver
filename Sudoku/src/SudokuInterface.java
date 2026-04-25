import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A wrapper class for main with functionality for displaying, modifying,
 * saving/loading, and solving Sudoku boards.
 */
public final class SudokuInterface {

    /**
     * Enumerations for user selection.
     */
    private enum Selection {
        /**
         * Edit user option.
         */
        EDIT("Edit current board", 0),
        /**
         * Load user option.
         */
        LOAD("Load another board", 1),
        /**
         * Save user option.
         */
        SAVE("Save the current board", 2),
        /**
         * Solve user option.
         */
        SOLVE("Solve the current board", 3),
        /**
         * New board user option.
         */
        NEW("Create a new board", 4);

        /**
         * the prompt of this.
         */
        private final String prompt;
        /**
         * the order of this.
         */
        private final int order;

        /**
         * Constructor which sets this based on the prompt and order.
         *
         * @param prompt
         * @param order
         */
        Selection(String prompt, int order) {
            this.prompt = prompt;
            this.order = order;
        }

        /**
         * Returns the prompt associated with this.
         *
         * @return this.prompt
         */
        public String getPrompt() {
            return this.prompt;
        }

        /**
         * Returns the order of this.
         *
         * @return this.order
         */
        public int getOrder() {
            return this.order;
        }

        /**
         * A global map of each prompt to each order of this.
         */
        private static final Map<String, Selection> PROMPT_MAP = Arrays.stream(values())
                .collect(Collectors.toMap(Selection::getPrompt, Function.identity()));

        /**
         * A global map of each order to each prompt of this.
         */
        private static final Map<Integer, Selection> ORDER_MAP = Arrays.stream(values())
                .collect(Collectors.toMap(Selection::getOrder, Function.identity()));

        /**
         * Retrieves the {@code Selection} from this.
         *
         * @param order
         * @return the selection associated with order
         */
        public static Selection fromOrder(int order) {
            return ORDER_MAP.get(order);
        }

        /**
         * Retrieves the selection from user input.
         *
         * @param input
         * @return the selection associated with an order or prompt from the
         *         user
         */
        public static Selection fromInput(String input) {
            try {
                int i = Integer.parseInt(input);
                Selection byOrder = ORDER_MAP.get(i);
                if (byOrder != null) {
                    return byOrder;
                }
            } catch (NumberFormatException e) {
            }

            Selection byPrompt = PROMPT_MAP.get(input);
            if (byPrompt != null) {
                return byPrompt;
            }

            throw new IllegalArgumentException("Invalid selection");
        }

        /**
         * Checks whether the supplied {@code str} is a valid prompt or order.
         *
         * @param str
         * @return true is str is a valid prompt or order
         */
        public static boolean isSelection(String str) {
            try {
                int i = Integer.parseInt(str);
                Selection byOrder = ORDER_MAP.get(i);
                if (byOrder != null) {
                    return true;
                }
            } catch (NumberFormatException e) {
            }

            Selection byPrompt = PROMPT_MAP.get(str);
            if (byPrompt != null) {
                return true;
            }
            return false;
        }

        /**
         * Reports the number of possible values of this.
         *
         * @return |values|
         */
        public static int size() {
            return values().length;
        }
    }

    /**
     * File for saved boards.
     */
    private static final String SAVED_BOARD_FILE_EXT = "data/saved/";
    /**
     * File for the previous board.
     */
    private static final String PREV_BOARD_FILE = "data/board.txt";
    /**
     * Buffer file for PREV_BOARD_FILE.
     */
    private static final String PREV_BOARD_BUFFER = "data/board.tmp";
    /**
     * Debug file.
     */
    private static final String DEBUG_FILE = "data/debug.txt";

    /**
     * Prompts the user to select an option based on {@code Selection}.
     *
     * @param in
     * @param out
     * @return the user selection
     */
    private static String menuPrompt(BufferedReader in, BufferedWriter out) {
        try {
            out.write("Select an option (to exit type anything else): ");
            out.newLine();
            out.flush();
            for (int i = 0; i < Selection.size(); i++) {
                out.write(" " + i + " " + Selection.fromOrder(i).getPrompt());
                out.newLine();
                out.flush();
            }
            out.write("?: ");
            out.flush();
            return in.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "FAILURE";
    }

    /**
     * Repeatedly prompts the user for a cell value until they type "DONE".
     *
     * Allows for the user to select for auto-filling (removing candidates), or
     * setting without error checking (allowing duplicates despite rules).
     *
     * @param in
     * @param out
     * @param board
     * @ensures board is set according to user input
     */
    private static void editLoop(BufferedReader in, BufferedWriter out,
            SudokuBoard board) {
        try {
            out.write("Automatically fill in freebie cells? (Y/N): ");
            out.flush();
            boolean propagate = false;

            String selection = in.readLine();
            while (selection != null && !selection.equals("Y")
                    && !selection.equals("N")) {
                out.write("Enter a valid selection: ");
                out.flush();
                selection = in.readLine();
            }

            if (selection != null && selection.equals("Y")) {
                propagate = true;
                out.write("Editor will fill in cells whose value is guaranteed.");
                out.newLine();
            } else {
                out.write("Editor will leave empty cells alone.");
                out.newLine();
            }
            out.flush();

            board.print(out);

            out.write("Enter a cell to edit (type \"DONE\" to exit): ");
            out.flush();
            selection = in.readLine();

            while (selection != null && !selection.equals("DONE")) {
                // assume the user input is something valid idc about exceptions

                int row = selection.toLowerCase().charAt(0) - 'a';
                int col = Integer.parseInt(selection.substring(1, selection.length()))
                        - 1;

                out.write("Enter the new value: ");
                out.flush();
                selection = in.readLine();

                int val = 0;
                if (selection != null) {
                    val = Integer.parseInt(selection);
                }

                while (val < 0 || val > board.getRadix()) {
                    out.write("Enter a valid value (\"0\" clears cell): ");
                    out.flush();
                    selection = in.readLine();

                    if (selection != null) {
                        val = Integer.parseInt(selection);
                    }
                }

                if (val == 0) {
                    // always clear the node no matter what mode we are in
                    board.set(row, col, val);
                } else if (propagate) {
                    while (!board.availableValues(row, col).contains(val)) {
                        out.write("Selection is invalid. Choose a new value: ");
                        out.newLine();
                        out.flush();
                        selection = in.readLine();
                        if (selection != null) {
                            val = Integer.parseInt(selection);
                        }

                        while (val < 0 || val > board.getRadix()) {
                            out.write("Enter a valid value: ");
                            out.flush();
                            selection = in.readLine();

                            if (selection != null) {
                                val = Integer.parseInt(selection);
                            }
                        }
                    }

                    board.setPropagate(row, col, val);
                } else {
                    board.set(row, col, val);
                }

                board.print(out);
                out.newLine();
                out.write("Enter a cell to edit (type \"DONE\" to exit): ");
                out.flush();
                selection = in.readLine();
            }

            out.newLine();
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Prompts the user for the name of the board (no file path or extension),
     * and saves to the specified file.
     *
     * @param in
     * @param out
     * @param board
     * @ensures in is a properly formatted version of board (for board's
     *          implementation)
     */
    private static void savePrompt(BufferedReader in, BufferedWriter out,
            SudokuBoard board) {
        try {
            out.write("Enter the board name (no path or ext): ");
            out.flush();
            String fileStr = in.readLine();

            BufferedWriter file = new BufferedWriter(
                    new FileWriter(SAVED_BOARD_FILE_EXT + fileStr + ".txt"));
            board.save(file);

            out.write("Board saved successfully.\n");
            out.newLine();
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Prompts the user for the name of the board (no file path or extension),
     * and loads to the specified board.
     *
     * @param in
     * @param out
     * @param board
     * @ensures board is loaded from in or an error is thrown
     */
    private static void loadPrompt(BufferedReader in, BufferedWriter out,
            SudokuBoard board) {
        try {
            out.write("Enter the board name (no path or ext): ");
            out.flush();
            String fileStr = in.readLine();

            BufferedReader file = new BufferedReader(
                    new FileReader(SAVED_BOARD_FILE_EXT + fileStr + ".txt"));
            board.load(file);

            out.write("Board loaded successfully.\n");
            out.newLine();
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Prompts the user for the size of a new board and initializes a blank
     * board with that radix.
     *
     * @param in
     * @param out
     * @return a new Sudoku board, such that |board| = radix, and all entries in
     *         board are initialized from 1 to radix
     */
    private static SudokuBoard newBoard(BufferedReader in, BufferedWriter out) {
        try {
            out.write("Enter the radix of the new board (perfect square): ");
            out.flush();
            String line = in.readLine();
            int radix = 1;
            if (line != null) {
                radix = Integer.parseInt(line);
            }
            return new SudokuBoard(radix);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new SudokuBoard(1);
    }

    /**
     * Solves the provided board, outputting the correct solution path in
     * reverse to out, along with the full forward solution path to DEBUG_FILE
     * according to Sudoku.solve(...).
     *
     * @param out
     * @param board
     * @ensures <pre>
     *  if a solution is found
     *      out is a properly formatted solution path in reverse, omitting
     *          backtracking;
     *      DEBUG_FILE is written to with a properly formatted solution path;
     *      and board is solved
     *  else
     *      a message is printed to out;
     *      and board is left unchanged
     * </pre>
     */
    private static void solveAndPrint(BufferedWriter out, SudokuBoard board) {
        try {
            //boolean solved;
            board.propagateAll();
            //board.print(out);
            out.write("!READ BOARD SOLVE IN BACKWARDS ORDER!");
            out.newLine();
            out.flush();
            int depth;
            try (BufferedWriter debug = new BufferedWriter(new FileWriter(DEBUG_FILE))) {
                depth = board.solve(out, debug);
            }
            if (depth != 0) {
                out.write("\nSOLVING ROUTINE CLAIMS SUCCESS...");
                out.newLine();

                if (board.verifyStrict()) {
                    out.write("SOLUTION IS VALID.");
                    out.newLine();
                } else if (board.verify()) {
                    out.write("SOLUTION IS VALID BUT MISSING ENTRIES.");
                    out.newLine();
                }

                out.write("SOLUTION DEPTH: " + depth);
                out.newLine();
            } else {
                out.write("\nSOLVING ROUTINE CLAIMS THAT THIS BOARD HAS NO SOLUTION.");
                out.newLine();
            }
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Saves the supplied board to PREV_BOARD_FILE, using PREV_BOARD_BUFFER as
     * buffer so that PREV_BOARD_FILE is not wiped before the data is saved.
     *
     * @param board
     */
    private static void saveBoardWithBuffer(SudokuBoard board) {
        try {
            BufferedWriter temp = new BufferedWriter(new FileWriter(PREV_BOARD_BUFFER));
            board.save(temp);
            temp.close();

            BufferedWriter next = new BufferedWriter(new FileWriter(PREV_BOARD_FILE));
            BufferedReader tempIn = new BufferedReader(new FileReader(PREV_BOARD_BUFFER));

            String line = tempIn.readLine();
            while (line != null) {
                next.write(line);
                next.newLine();
                line = tempIn.readLine();
            }

            next.flush();
            tempIn.close();
            next.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Private constructor for compilation.
     */
    private SudokuInterface() {
    }

    /**
     * Repeatedly prompts the user for a {@code Selection} and modifies the
     * displayed board according to their inputs.
     *
     * @param args
     */
    public static void main(String[] args) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(System.out));

            // initialize with trivial radix to prepare for read-in
            SudokuBoard board = new SudokuBoard(1);

            BufferedReader prev = new BufferedReader(new FileReader(PREV_BOARD_FILE));

            board.load(prev);

            prev.close();

            out.write("Previous board:");
            out.newLine();
            out.newLine();
            out.flush();

            board.print(out);

            out.newLine();
            out.flush();

            String selectionStr = menuPrompt(in, out);

            while (Selection.isSelection(selectionStr)) {
                Selection selection = Selection.fromInput(selectionStr);
                switch (selection) {
                    case EDIT:
                        editLoop(in, out, board);
                        break;
                    case LOAD:
                        loadPrompt(in, out, board);
                        break;
                    case SAVE:
                        savePrompt(in, out, board);
                        break;
                    case SOLVE:
                        solveAndPrint(out, board);
                        break;
                    case NEW:
                        board = newBoard(in, out);
                        break;
                    default:
                        break;
                }

                saveBoardWithBuffer(board);

                board.print(out);
                out.newLine();
                out.flush();
                selectionStr = menuPrompt(in, out);
            }

            saveBoardWithBuffer(board);

            out.close();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
