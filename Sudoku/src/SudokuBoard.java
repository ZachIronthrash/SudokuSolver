import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Vector;

/**
 * <pre>
 * A Sudoku board represented as a radix x radix grid of Cells.
 *
 * Each Cell maintains a set of possible values (its candidate set):
 *  - A cell is considered "fixed" if it has exactly one value.
 *  - A cell is considered "unfixed" (or undecided) if it has multiple values.
 *
 * IMPORTANT:
 *  - A cell never has an empty candidate set.
 *  - A cell with multiple candidates returns value() == 0.
 *  - A cell with a single candidate returns that value.
 *
 * Radix rules:
 *  - radix must be a perfect square (e.g., 4, 9, 16)
 *  - valid values are 1 through radix
 *
 * The solver uses recursive backtracking with constraint propagation.
 * </pre>
 *
 * @author Christopher Hart
 */
public class SudokuBoard {
    /**
     * Represents an N x M matrix of entries of type T without any matrix
     * operations (setting and writing by index only).
     *
     * @param <T>
     *            the type to store in each entry of the matrix
     */
    private static class Matrix<T> {
        /**
         * Doubled vector which represents the entries in this.
         */
        private Vector<Vector<T>> matrix;
        /**
         * The number of rows in this.
         */
        private final int rows;
        /**
         * The number of columns in this.
         */
        private final int columns;

        /**
         * Helper for initializing internal representations.
         *
         * @ensures <pre>
         *  matrix.capacity() = rows,
         *  for each Vector v in matrix, v.capacity() = columns,
         *  & each entry in matrix = null
         * </pre>
         */
        private void createNewRep() {
            this.matrix = new Vector<>();
            for (int i = 0; i < this.rows; i++) {
                Vector<T> row = new Vector<>();
                for (int j = 0; j < this.columns; j++) {
                    row.add(null);
                }
                this.matrix.add(row);
            }
        }

        /**
         * Default constructor which takes the size of this as arguments.
         *
         * @param rows
         *            the number of rows in this
         * @param columns
         *            the number of columns in this
         * @ensures each entry in this is null and |this| = rows * columns
         */
        Matrix(int rows, int columns) {
            this.rows = rows;
            this.columns = columns;
            this.createNewRep();
        }

        /**
         * Returns the value in the specified entry of this.
         *
         * @param row
         *            the row of the entry to return
         * @param column
         *            the column of the entry to return
         * @return the value of the specified entry
         */
        public T retrieve(int row, int column) {
            assert row < this.rows && row >= 0 : "row must be within range.";
            assert column < this.columns && column >= 0 : "column must be within range.";

            return this.matrix.get(row).get(column);
        }

        /**
         * Sets the value in the specified entry of this.
         *
         * @param row
         *            the row of the entry to set
         * @param column
         *            the column of the entry to set
         * @param val
         *            the value to set entry to
         * @return the previous value in the specified entry
         */
        public T set(int row, int column, T val) {
            assert row < this.rows && row >= 0 : "row must be within range.";
            assert column < this.columns && column >= 0 : "column must be within range.";

            return this.matrix.get(row).set(column, val);
        }
    }

    /**
     * Represents a Sudoku cell storing a set of possible values.
     *
     * <pre>
     * A cell:
     *  - is "fixed" if it contains exactly one value
     *  - is "unfixed" if it contains multiple possible values
     *  </pre>
     *
     * A cell never contains zero values.
     */
    private static class Cell {
        /**
         * Container for the possible values of the cell.
         */
        private Vector<Integer> values;

        /**
         * Helper for initializing internal representations.
         *
         * @param i
         *            the initial value of this
         * @ensures values = ( i )
         */
        private void createNewRep(int i) {
            this.values = new Vector<Integer>();
            this.values.add(i);
        }

        /**
         * Constructor which requires an initial value.
         *
         * @param i
         *            the initial value of this
         * @ensures this = { i }
         */
        Cell(int i) {
            this.createNewRep(i);
        }

        /**
         * Adds {@code i} to this cell's candidate set if it is not already
         * present.
         *
         * @param i
         *            value to add
         * @ensures i is in this.values
         */
        public void add(int i) {
            if (!this.values.contains(i)) {
                this.values.add(i);
            }
        }

        /**
         * Removes {@code i} from this cell's candidate set.
         *
         * @param i
         *            value to remove
         * @requires i is in this.values and this.values has size > 1
         * @ensures i is not in this.values
         *
         * @throws IllegalStateException
         *             if removing would leave the cell with no values
         * @return the value removed from this
         */
        public int remove(int i) {
            if (this.values.size() <= 1) {
                throw new IllegalStateException("Cannot remove from fixed cell");
            }

            int index = this.values.indexOf(i);
            if (index == -1) {
                throw new IllegalArgumentException("Value not in cell");
            }
            return this.values.remove(index);
        }

        /**
         * Returns true if this contains {@code i}.
         *
         * @param i
         *            the value to check
         * @return this & i (true if this contains i)
         */
        public boolean contains(int i) {
            return this.values.contains(i);
        }

        /**
         * Returns the resolved value of this cell.
         *
         * @return the single value if this cell is fixed, or 0 if the cell has
         *         multiple candidates
         */
        public int value() {
            if (this.values.size() > 1) {
                return 0;
            } else {
                return this.values.firstElement();
            }
        }

        /**
         * Getter which returns the possible values of the cell as a vector.
         *
         * @return a dereferenced version of values
         */
        public Vector<Integer> values() {
            return new Vector<>(this.values);
        }

        /**
         * Creates and returns a new copy of this.
         *
         * @return a copy of this
         */
        Cell copy() {
            Cell c = new Cell(1);
            c.values.clear();
            c.values.addAll(this.values);
            return c;
        }
    }

    /**
     * Container for the radix of this. Must be a perfect square.
     */
    private int radix;

    /**
     * Container for the values in the board, stored as a {@code Matrix} of
     * {@code Cell}.
     */
    private Matrix<Cell> board;

    /**
     * Helper for initializing internal representations.
     *
     * @param radix
     *            the new radix of this
     * @ensures board is a new Matrix of size (radix, radix)
     */
    private void createNewRep(int radix) {
        this.board = new Matrix<>(radix, radix);
    }

    /**
     * If val is zero the specified cell is filled with each value from 1 to
     * radix, else the cell is set to only contain val.
     *
     * @param row
     *            the row of the cell to set
     * @param col
     *            the column of the cell to set
     * @param val
     *            the value to set the cell to
     * @ensures <pre>
     *  if val = 0 then
     *      this[row, col] = { 1, ..., radix - 1, radix }
     *  else
     *      this[row, col] = { val }
     * </pre>
     */
    private void setCellFromValue(int row, int col, int val) {
        assert val <= this.radix && val >= 0
                : "Value cannot be greater than " + this.radix + " or less than zero.";

        if (val != 0) {
            this.board.set(row, col, new Cell(val));
        } else {
            Cell c = new Cell(1);
            for (int k = 2; k <= this.radix; k++) {
                c.add(k);
            }
            this.board.set(row, col, c);
        }
    }

    /**
     * Constructor which sets each cell in this to have every possible value as
     * defined by the {@code radix}.
     *
     * @param radix
     *            the radix of this
     */
    SudokuBoard(int radix) {
        assert radix > 0 : "radix must be greater than 0.";
        assert (int) Math.sqrt(radix) * (int) Math.sqrt(radix) == radix
                : "radix must be a perfect square.";

        this.createNewRep(radix);
        this.radix = radix;

        for (int i = 0; i < radix; i++) {
            for (int j = 0; j < radix; j++) {
                this.setCellFromValue(i, j, 0);
            }
        }
    }

    /**
     * Gets the radix.
     *
     * @return radix
     */
    public int getRadix() {
        return this.radix;
    }

    /**
     * Gets the value at (row, column). If the cell is empty this returns 0.
     *
     * @param row
     *            the row of the cell to get
     * @param column
     *            the column of the cell to get
     * @return <pre>
     *  0 if |this[row, column]| > 1
     *   else this[row, column]
     * </pre>
     */
    public int retrieve(int row, int column) {
        assert row < this.radix && row >= 0 : "row must be within radix.";
        assert column < this.radix && column >= 0 : "column must be within radix.";

        return this.board.retrieve(row, column).value();
    }

    /**
     * Returns a copy of the candidate set for the specified cell.
     *
     * @param row
     * @param column
     * @return a new Vector containing all possible values for the cell
     */
    public Vector<Integer> availableValues(int row, int column) {
        assert row < this.radix && row >= 0 : "row must be within radix.";
        assert column < this.radix && column >= 0 : "column must be within radix.";

        return new Vector<Integer>(this.board.retrieve(row, column).values());
    }

    /**
     * Sets the specified cell to a fixed value.
     *
     * @param row
     *            the row of the cell to set
     * @param column
     *            the column of the cell to set
     * @param val
     *            the value to set the specified cell to
     * @requires 0 <= row < radix
     * @requires 0 <= column < radix
     * @requires 1 <= val <= radix
     * @ensures this[row, column] is fixed with value val
     */
    public void set(int row, int column, int val) {
        assert val <= this.radix && val > 0 : "value is invalid for radix.";
        assert row < this.radix && row >= 0 : "row must be within radix.";
        assert column < this.radix && column >= 0 : "column must be within radix.";

        //this.board.set(row, column, val);
//        Cell c = this.board.retrieve(row, column);
//        c.set(val);

        this.setCellFromValue(row, column, val);
    }

    /**
     * Removes a value from the candidate set of a cell.
     *
     * @param row
     *            the row of the cell to modify
     * @param column
     *            the column of the cell to modify
     * @param val
     *            the value to remove from the specified cell
     * @requires the cell is not fixed
     * @requires val is in this[row, column]
     *
     * @ensures val is not in this[row, column]
     */
    public void remove(int row, int column, int val) {
        assert this.board.retrieve(row, column).value() == 0;

        this.board.retrieve(row, column).remove(val);
    }

    /**
     * Returns whether the cell has multiple possible values.
     *
     * @param row
     *            the row of the cell to evaluate
     * @param column
     *            the column of the cell to evalutate
     * @return true if the cell is not fixed (has multiple candidates), false
     *         otherwise
     */
    public boolean isEmpty(int row, int column) {
        return this.board.retrieve(row, column).value() == 0;
    }

    /**
     * Sets the specified cell to {@code val} and propagates constraints by
     * removing {@code val} from all related cells (same row, column, and box).
     *
     * Only cells that are not fixed (i.e., have multiple candidates) are
     * modified.
     *
     * @param row
     *            the row of the cell to set
     * @param column
     *            the column of the cell to set
     * @param val
     *            the value to set the cell to
     * @requires 0 <= row < radix
     * @requires 0 <= column < radix
     * @requires 1 <= val <= radix
     * @requires val is in availableValues(row, column)
     *
     * @ensures this[row, column] is fixed with value val
     *
     * @ensures for all r != row: if this[r, column] is not fixed, then val is
     *          not in its candidate set
     *
     * @ensures for all c != column: if this[row, c] is not fixed, then val is
     *          not in its candidate set
     *
     * @ensures for all cells in the same sub-box except (row, column): if the
     *          cell is not fixed, then val is not in its candidate set
     *
     * @throws IllegalStateException
     *             if val is not a valid candidate for the specified cell
     */
    public void setPropagate(int row, int column, int val) {
        assert val <= this.radix && val > 0 : "value is invalid for radix.";
        assert row < this.radix && row >= 0 : "row must be within radix.";
        assert column < this.radix && column >= 0 : "column must be within radix.";

        if (this.availableValues(row, column).contains(val)) {
            this.setCellFromValue(row, column, val);
        } else {
            throw new IllegalStateException("Adding value would break sudoku rules.");
        }

        if (val != 0) {
            for (int i = 0; i < this.radix; i++) {
                Cell neighbor = this.board.retrieve(i, column);
                if (i != row && neighbor.value() == 0 && neighbor.contains(val)) {
                    neighbor.remove(val);
                }
                neighbor = this.board.retrieve(row, i);
                if (i != column && neighbor.value() == 0 && neighbor.contains(val)) {
                    neighbor.remove(val);
                }
            }

            int boxSize = (int) Math.sqrt(this.radix);
            int boxRow = (row / boxSize) * boxSize;
            int boxCol = (column / boxSize) * boxSize;

            for (int i = 0; i < boxSize; i++) {
                for (int j = 0; j < boxSize; j++) {
                    Cell neighbor = this.board.retrieve(boxRow + i, boxCol + j);
                    if (neighbor.value() == 0
                            && (boxRow + i != row || boxCol + j != column)
                            && neighbor.contains(val)) {
                        neighbor.remove(val);
                    }
                }
            }
        }
    }

    /**
     * Applies constraint propagation to all fixed cells on the board.
     *
     * @ensures for every fixed cell, its value has been removed from the
     *          candidate sets of all related unfixed cells
     */
    public void propagateAll() {
        // begin by looping through and running setPropagate on any already existing cells
        for (int i = 0; i < this.radix; i++) {
            for (int j = 0; j < this.radix; j++) {
                int val = this.board.retrieve(i, j).value();
                if (val != 0) {
                    this.setPropagate(i, j, val);
                }
            }
        }
    }

    /**
     * Checks whether a value can legally be placed in a cell under Sudoku
     * rules.
     *
     * @param row
     *            the row of the cell to check
     * @param column
     *            the column of the cell to check
     * @param val
     *            the value to check
     * @return true if val does not appear in the same row, column, or box
     */
    public boolean valueAllowed(int row, int column, int val) {
        assert val <= this.radix && val > 0 : "value is invalid for radix.";
        assert row < this.radix && row >= 0 : "row must be within radix.";
        assert column < this.radix && column >= 0 : "column must be within radix.";

        for (int i = 0; i < this.radix; i++) {
            if (val == this.board.retrieve(row, i).value() && i != column) {
                return false;
            }
            if (val == this.board.retrieve(i, column).value() && i != row) {
                return false;
            }
        }

        int boxSize = (int) Math.sqrt(this.radix);
        int boxRow = (row / boxSize) * boxSize;
        int boxCol = (column / boxSize) * boxSize;

        for (int i = 0; i < boxSize; i++) {
            for (int j = 0; j < boxSize; j++) {
                if (val == this.board.retrieve(boxRow + i, boxCol + j).value()
                        && (boxRow + i != row || boxCol + j != column)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Checks whether the board satisfies Sudoku constraints.
     *
     * @return true if no row, column, or box contains duplicate fixed values
     */
    public boolean verify() {
        for (int row = 0; row < this.radix; row++) {
            for (int col = 0; col < this.radix; col++) {
                int val = this.board.retrieve(row, col).value();
                if (val != 0) {
                    if (!this.valueAllowed(row, col, val)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Checks whether the board is completely solved and valid.
     *
     * @return true if all cells are fixed and Sudoku constraints are satisfied
     */
    public boolean verifyStrict() {
        for (int row = 0; row < this.radix; row++) {
            for (int col = 0; col < this.radix; col++) {
                int val = this.board.retrieve(row, col).value();
                if (val != 0) {
                    if (!this.valueAllowed(row, col, val)) {
                        return false;
                    }
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Outputs a nicely formatted version of the board to the specified stream.
     *
     * @param out
     *            the stream to print to
     * @ensures out = #out * [a nicely formatted text version of this]
     */
    public void print(BufferedWriter out) {
        try {
            int width = Integer.toString(this.radix).length();

            // Column headers
            out.write("   ");
            for (int i = 1; i <= this.radix; i++) {
                String s = Integer.toString(i);
                out.write(" " + s + " ".repeat(width - s.length() + 2));
            }
            out.newLine();

            // Divider
            int cellWidth = width + 2;
            int totalWidth = this.radix * (cellWidth + 1) + 1;
            out.write(" ".repeat(2) + "-".repeat(totalWidth));
            out.newLine();

            // Rows
            for (int i = 0; i < this.radix; i++) {
                out.write((char) ('A' + i) + " |");
                for (int j = 0; j < this.radix; j++) {
                    int val = this.board.retrieve(i, j).value();
                    String s = (val == 0) ? "" : Integer.toString(val);
                    out.write(" " + s + " ".repeat(width - s.length()) + " |");
                }
                out.newLine();

                out.write(" ".repeat(2) + "-".repeat(totalWidth));
                out.newLine();
            }

            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Outputs an implementation-specific file which stores the current state of
     * the board (without storing candidates: so 0 if cells are empty not what
     * values are allowed).
     *
     * @param out
     *            the stream to output to
     * @ensures out = #out * [an implementation-specific version of this]
     */
    public void save(BufferedWriter out) {
        try {
            out.write(Integer.toString(this.radix));
            out.newLine();
            for (int row = 0; row < this.radix; row++) {
                for (int col = 0; col < this.radix; col++) {
                    out.write(Integer.toString(this.board.retrieve(row, col).value()));

                    if (col != this.radix - 1) {
                        out.write(",");
                    } else {
                        out.newLine();
                    }
                }
            }
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads in the state of a board from the specified stream without candidate
     * data based on an implementation-specific scheme.
     *
     * @param in
     *            the stream to read from
     * @requires in is a properly formatted input stream
     * @ensures this is a board matching the stream from in
     */
    public void load(BufferedReader in) {
        try {
            String firstLine = in.readLine();
            if (firstLine == null) {
                throw new IOException("Empty file");
            }

            this.radix = Integer.parseInt(firstLine.trim());

            if ((int) Math.sqrt(this.radix) * (int) Math.sqrt(this.radix) != this.radix) {
                throw new IllegalArgumentException("Invalid radix in file");
            }

            this.createNewRep(this.radix);

            // Initialize ALL cells first
            for (int i = 0; i < this.radix; i++) {
                for (int j = 0; j < this.radix; j++) {
                    this.setCellFromValue(i, j, 0);
                }
            }

            // Now overwrite with file values
            for (int row = 0; row < this.radix; row++) {
                String line = in.readLine();
                if (line == null) {
                    throw new IOException("Incomplete board data");
                }

                String[] parts = line.split(",");
                if (parts.length != this.radix) {
                    throw new IOException("Malformed row: " + line);
                }

                for (int col = 0; col < this.radix; col++) {
                    int val = Integer.parseInt(parts[col].trim());
                    this.setCellFromValue(row, col, val);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a copy of this.
     *
     * @return a copy of this.
     */
    public SudokuBoard copyOf() {
        SudokuBoard copy = new SudokuBoard(this.radix);

        for (int row = 0; row < this.radix; row++) {
            for (int col = 0; col < this.radix; col++) {
                copy.board.set(row, col, this.board.retrieve(row, col).copy());
            }
        }

        return copy;
    }

    /**
     * Copies this to og.
     *
     * @param og
     *            the board to copy to
     * @ensures og = this
     */
    public void copy(SudokuBoard og) {
        this.radix = og.getRadix();
        this.createNewRep(this.radix);

        for (int row = 0; row < this.radix; row++) {
            for (int col = 0; col < this.radix; col++) {
                this.board.set(row, col, og.board.retrieve(row, col).copy());
            }
        }
    }

    /**
     * Attempts to solve the Sudoku board using recursive backtracking with
     * constraint propagation.
     *
     * @param out
     *            stream for final solution output
     * @param debug
     *            stream for tracing the solving process
     *
     * @return the solution depth, 0 if solution is not found
     *
     * @ensures if a solution exists, this board is solved
     */
    public int solve(BufferedWriter out, BufferedWriter debug) {
        this.propagateAll();
        this.print(debug);

        /**
         * Essentially a tuple for the position and number of elements of the
         * best cell to look at next (the one with the least elements).
         *
         * @param row
         * @param col
         * @param size
         */
        record BestCell(int row, int col, int size) {
        }

        BestCell best = null;

        outer: for (int row = 0; row < this.radix; row++) {
            for (int col = 0; col < this.radix; col++) {
                if (this.retrieve(row, col) == 0) {
                    int thisSize = this.availableValues(row, col).size();
                    // should be that size 1 cells never happen
                    if (thisSize == 2) {
                        best = new BestCell(row, col, thisSize);
                        break outer;
                    }
                    if (best == null || thisSize < best.size) {
                        best = new BestCell(row, col, thisSize);
                    }
                }
            }
        }

        if (best == null) {
            if (this.verifyStrict()) {
                return 1;
            }
            return 0;
        }

        int row = best.row;
        int col = best.col;

        Vector<Integer> candidates = this.availableValues(row, col);
        int depth = 0;
        for (int val : candidates) {
            SudokuBoard copy = this.copyOf();
            copy.setPropagate(row, col, val);

            if (!copy.verify()) {
                continue;
            }

            try {
                debug.write("Trying (" + Integer.toString(col + 1) + ", "
                        + Character.toString((char) ('A' + row)) + ") = "
                        + Integer.toString(val));
                debug.newLine();
                debug.flush();
                //copy.print(out);
            } catch (IOException e) {
                e.printStackTrace();
            }

            depth = copy.solve(out, debug);
            if (depth != 0) {
                try {
                    out.write("Inserted (" + (col + 1) + ", " + (char) ('A' + row)
                            + ") = " + val);
                    out.newLine();
                    out.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                this.print(out);
                this.copy(copy);
                return depth + 1;
            } else {
                try {
                    debug.write("Backtracking from (" + Integer.toString(col + 1) + ", "
                            + Character.toString((char) ('A' + row)) + ") = "
                            + Integer.toString(val));
                    debug.newLine();
                    debug.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
//                if (this.isEmpty(row, col)) {
//                    this.remove(row, col, val);
//                }
            }
        }

        // no empty cells
        if (this.verifyStrict()) {
            return depth;
        }
        return 0;
    }

    //// LEGACY SOLVE CODE

//    public boolean solve(SimpleWriter out, SimpleWriter debug) {
//        this.propagateAll();
//        this.print(debug);
//
//        record bestCell(int row, int col) {
//        }
//
//        for (int row = 0; row < this.radix; row++) {
//            for (int col = 0; col < this.radix; col++) {
//
//                if (this.retrieve(row, col) == 0) {
//
//                    for (int val : this.availableValues(row, col)) {
//                        SudokuBoard copy = this.copyOf();
//                        copy.setPropagate(row, col, val);
//
//                        if (!copy.verify()) {
//                            continue;
//                        }
//
//                        debug.println("Trying (" + (col + 1) + ", " + (char) ('A' + row)
//                                + ") = " + val);
//                        //copy.print(out);
//
//                        if (copy.solve(out, debug)) {
//                            this.print(out);
//                            this.copy(copy);
//                            return true;
//                        } else {
//                            debug.println("Backtracking from (" + (col + 1) + ", "
//                                    + (char) ('A' + row) + ") = " + val);
////                            if (this.isEmpty(row, col)) {
////                                this.remove(row, col, val);
////                            }
//                        }
//                    }
//
//                    return false; // no value worked
//                }
//            }
//        }
//
//        // no empty cells
//        return this.verifyStrict();
//    }

//  public boolean solve(SimpleWriter out) {
//      this.propagateAll();
//      this.print(out);
//
//      for (int row = 0; row < this.radix; row++) {
//          for (int col = 0; col < this.radix; col++) {
//
//              if (this.retrieve(row, col) == 0) {
//
//                  for (int val : this.availableValues(row, col)) {
//                      SudokuBoard copy = this.copyOf();
//                      copy.setPropagate(row, col, val);
//
//                      if (!copy.verify()) {
//                          continue;
//                      }
//
//                      out.println("Trying (" + (col + 1) + ", " + (char) ('A' + row)
//                              + ") = " + val);
//                      //copy.print(out);
//
//                      if (copy.solve(out)) {
//                          this.copy(copy);
//                          return true;
//                      } else {
//                          out.println("Backtracking from (" + (col + 1) + ", "
//                                  + (char) ('A' + row) + ") = " + val);
//                      }
//                  }
//
//                  return false; // no value worked
//              }
//          }
//      }
//
//      // no empty cells
//      return this.verifyStrict();
//  }
}
