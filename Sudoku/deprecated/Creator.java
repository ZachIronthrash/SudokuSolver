import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import components.simplereader.SimpleReader;
import components.simplereader.SimpleReader1L;
import components.simplewriter.SimpleWriter;
import components.simplewriter.SimpleWriter1L;

public final class Creator {

    private enum Selection {
        EDIT("Edit current board", 0), LOAD("Load another board", 1),
        SAVE("Save the current board", 2);

        private final String prompt;
        private final int order;

        Selection(String prompt, int order) {
            this.prompt = prompt;
            this.order = order;
        }

        public String getPrompt() {
            return this.prompt;
        }

        public int getOrder() {
            return this.order;
        }

        private static final Map<String, Selection> PROMPT_MAP = Arrays.stream(values())
                .collect(Collectors.toMap(Selection::getPrompt, Function.identity()));

        private static final Map<Integer, Selection> ORDER_MAP = Arrays.stream(values())
                .collect(Collectors.toMap(Selection::getOrder, Function.identity()));

        public static Selection fromOrder(int order) {
            return ORDER_MAP.get(order);
        }

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

        public static int size() {
            return values().length;
        }
    }

    private static final String SAVED_BOARD_FILE_EXT = "data/saved/";
    private static final String PREV_BOARD_FILE = "data/board.txt";

    private static String menuPrompt(SimpleReader in, SimpleWriter out) {
        out.println("Select an option (to exit type anything else): ");
        for (int i = 0; i < Selection.size(); i++) {
            out.println(" " + i + " " + Selection.fromOrder(i).getPrompt());
        }
        out.print("?: ");
        return in.nextLine();
    }

    private static void editLoop(SimpleReader in, SimpleWriter out, SudokuBoard board) {
        out.print("Automatically fill in freebie cells? (Y/N): ");
        boolean propogate = false;

        String selection = in.nextLine();
        while (!selection.equals("Y") && !selection.equals("N")) {
            out.print("Enter a valid selection: ");
            selection = in.nextLine();
        }

        if (selection.equals("Y")) {
            propogate = true;
            out.println("Editor will fill in cells whose value is guaranteed.");
        } else {
            out.println("Editor will leave empty cells alone.");
        }

        board.print(out);

        out.print("Enter a cell to edit (type \"DONE\" to exit): ");
        selection = in.nextLine();

        while (!selection.equals("DONE")) {
            // assume the user input is something valid idc about exceptions

            int row = selection.toLowerCase().charAt(0) - 'a';
            int col = Integer.parseInt(selection.substring(1, 2)) - 1;

            out.print("Enter the new value: ");
            selection = in.nextLine();

            int val = Integer.parseInt(selection);

            while (val < 0 || val > board.getRadix()) {
                out.print("Enter a valid value: ");
                selection = in.nextLine();

                val = Integer.parseInt(selection);
            }

            if (propogate) {
                while (!board.availableValues(row, col).contains(val)) {
                    out.println("Selection is invalid. Choose a new value: ");
                    selection = in.nextLine();
                    val = Integer.parseInt(selection);

                    while (val < 0 || val > board.getRadix()) {
                        out.print("Enter a valid value: ");
                        selection = in.nextLine();

                        val = Integer.parseInt(selection);
                    }
                }

                board.setPropogate(row, col, val);
            } else {
                board.set(row, col, val);
            }

            board.print(out);
            out.println();
            out.print("Enter a cell to edit (type \"DONE\" to exit): ");
            selection = in.nextLine();
        }

        out.println();

    }

    private static void savePrompt(SimpleReader in, SimpleWriter out, SudokuBoard board) {
        out.print("Enter the board name (no path or ext): ");
        String fileStr = in.nextLine();

        SimpleWriter file = new SimpleWriter1L(SAVED_BOARD_FILE_EXT + fileStr + ".txt");
        board.save(file);

        out.println("Board saved successfully.\n");
    }

    private static void loadPrompt(SimpleReader in, SimpleWriter out, SudokuBoard board) {
        out.print("Enter the board name (no path or ext): ");
        String fileStr = in.nextLine();

        SimpleReader file = new SimpleReader1L(SAVED_BOARD_FILE_EXT + fileStr + ".txt");
        board.load(file);

        out.println("Board loaded successfully.\n");
    }

    private Creator() {
    }

    public static void main(String[] args) {
        SimpleWriter out = new SimpleWriter1L();
        SimpleReader in = new SimpleReader1L();

        // initialize with trivial radix to prepare for read-in
        SudokuBoard board = new SudokuBoard(1);

        SimpleReader prev = new SimpleReader1L(PREV_BOARD_FILE);

        board.load(prev);

        prev.close();

        out.print("Previous board:\n\n");

        board.print(out);

        out.println();

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
            }
            board.print(out);
            out.println();
            selectionStr = menuPrompt(in, out);
        }

        // this buffer is ai generated slop but i think it makes sense
        //  the idea is to open the file with a dummy first so we don't clear real data
        SimpleWriter temp = new SimpleWriter1L("data/board.tmp");
        board.save(temp);
        temp.close();

        // overwrite only after successful save
        SimpleWriter next = new SimpleWriter1L(PREV_BOARD_FILE);
        SimpleReader tempIn = new SimpleReader1L("data/board.tmp");

        while (!tempIn.atEOS()) {
            next.println(tempIn.nextLine());
        }

        tempIn.close();
        next.close();

        out.close();
        in.close();
    }
}
