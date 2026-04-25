# SudokuSolver

<!-- Add your project description here -->

## Overview

A simple Java framework for creating, viewing, and solving Sudoku puzzles via printed prompts to the console.

## Features

<!-- Add your features here -->
- Displaying Sudoku boards of in the console
- Creating, editing, saving, and loading boards of any square radix
- Solving boards according to standard Sudoku rules

## Getting Started

### Prerequisites

<!-- Add any prerequisites here -->
- Java 8 or higher

### Installation

<!-- Add installation instructions here -->

```bash
git clone https://github.com/ZachIronthrash/SudokuSolver.git
cd SudokuSolver
```

### Usage

<!-- Add usage instructions here -->
This project consists of two classes: SudokuBoard and SudokuInterface.
- SudokuBoard consists of methods which allow for the storage, modification, and solutions of Sudoku boards and is generally applicable to applications involving Sudoku.
- SudokuInterface is an example user interface that uses the console, and SudokuBoard's printing methods to facilatate all the functionality of SudokuBoard.

To utilize SudokuInterface:
1. Run SudokuInterface.java.
        if a runtime error occurs with SudokuBoard.load(...) then data/board.txt has been corrupted and a valid board must be copied from data/saved/. This issue should not occur with a fresh installation but may appear after crashes.
2. Follow the prompts to operate on the loaded board, or select/create a new one.
3. The solver should produce the correct solution path if one exists (currently only finds the first discovered solution and throws out the rest), and the user can find the full solution path--including backtracking--in data/debug.txt.

## Project Structure

<!-- Describe your project structure here -->

```
SudokuSolver/
├── src/
│   └── (Source files)
├── data/
│   ├── saved/
│   │   └── (Saved boards)
│   └── (Storage files for src)
├── README.md
└── .gitignore
```

<## Contributing>

<!-- Add contribution guidelines here -->

<## License>

<!-- Specify your license here -->

## Author

ZachIronthrash