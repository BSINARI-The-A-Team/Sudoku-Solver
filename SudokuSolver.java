import java.util.ArrayList;

/**
 * This class solves a Sudoku puzzle using the Forward Checking algorithm.
 */
public class SudokuSolver implements ISudokuSolver {

	int[][] puzzle;
	int size;
	ArrayList<ArrayList<Integer>> D;

	/**
	 * Gets the current Sudoku puzzle as a 2D array.
	 * Empty spaces are shown as zeros.
	 * The top-left corner is at position (0,0).
	 *
	 * @return A 2D array of integers showing the puzzle.
	 */
	public int[][] getPuzzle() {
		return puzzle;
	}

	/**
	 * Puts a number into a specific place in the puzzle.
	 * It checks if the place and the number are allowed before adding it.
	 * Use the number 0 to delete a value.
	 *
	 * @param col   The column number, starting from 0 on the left.
	 * @param row   The row number, starting from 0 at the top.
	 * @param value The number to put in (1-9), or 0 to clear the space.
	 */
	public void setValue(int col, int row, int value) {
        // Ensure the selected position is within the bounds of the board
		if(col >= puzzle.length || row >= puzzle[0].length ){
			return;
		}
        // Ensure the value is within the legal Sudoku range (0-9)
		if (value >= 0 && value <= 9){
			puzzle[col][row] = value;
		}		
	}

	/**
	 * Sets up an empty Sudoku puzzle of the given size.
	 * It also prepares the lists (domains) needed for the solver to work.
	 *
	 * @param size1 The size of a block in the puzzle (e.g., 3 for a normal 9x9 Sudoku).
	 */
	public void setup(int size1) {
		size = size1;
		int n = size * size;
		
        // Initialize the empty puzzle board
		puzzle = new int[n][n];
		
        // Initialize the domain list for all variables
		D = new ArrayList<ArrayList<Integer>>(n * n);
		
        // Populate the initial domains for each cell (1 to 9)
		for (int i = 0; i < n * n; i++) {
			ArrayList<Integer> domain = new ArrayList<Integer>();
			for (int val = 1; val <= 9; val++) {
				domain.add(val);
			}
			D.add(domain);
		}
	}

	/**
	 * Checks if the puzzle can be solved. 
	 * If it can, it solves the puzzle and saves the answer.
	 * It uses the Forward Checking (FC) algorithm.
	 *
	 * @return True if the puzzle is solved, false if it cannot be solved.
	 */
	public boolean solve() {
		int n = size * size;
        
        // Step 1: Reset the domains to their full initial state (1-9) for a fresh solve
		D = new ArrayList<ArrayList<Integer>>(n * n);
		for (int i = 0; i < n * n; i++) {
			ArrayList<Integer> domain = new ArrayList<Integer>();
			for (int val = 1; val <= 9; val++) {
				domain.add(val);
			}
			D.add (domain);
		}
        
        // Step 2: Convert the current 2D board into a 1D assignment list
		ArrayList<Integer> asn = GetAssignment(puzzle);

        // Step 3: Run initial consistency check based on the starting numbers
		if (!INITIAL_FC(asn)) {
			return false; // Puzzle is unsolvable from the start
		}
        
        // Step 4: Run the main Forward Checking search algorithm
		ArrayList<Integer> result = FC(asn);
		if (result == null) {
			return false; // Search failed to find a valid solution
		}
        
        // Step 5: Convert the successful 1D result back to the 2D puzzle board
		puzzle = GetPuzzle(result);
		return true;
	}

	/**
	 * Reads a 2D array and saves it as the puzzle.
	 * This is a fast way to load a ready-made puzzle.
	 * It checks if the array has the right size and legal numbers.
	 *
	 * @param p The 2D array containing the starting numbers of the puzzle.
	 */
	public void readInPuzzle(int[][] p) {
		// Validate array dimensions
		if (p == null || p.length != size * size || p[0].length != size * size) {
			return;
		}

		// Check if all values in the input puzzle are valid (0-9)
		for (int i = 0; i < p.length; i++) {
			for (int j = 0; j < p[i].length; j++) {
				if (p[i][j] < 0 || p[i][j] > 9) {
					return;
				}
			}
		}

		// Create a deep copy to ensure the original input array is not modified
		for (int i = 0; i < p.length; i++) {
			for (int j = 0; j < p[i].length; j++) {
				puzzle[i][j] = p[i][j];
			}
		}
	}
	
	/**
	 * Finds the first empty spot in the list.
	 *
	 * @param asn The list of numbers in the puzzle.
	 * @return The position (index) of the first 0 found.
	 */
	private int getFirstZeroIndex(ArrayList<Integer> asn){
		int index = 0;
		for (int i = 0; i < asn.size(); i++) {
			if(asn.get(i) == 0){
				index = i;
				break; // Stop searching once the first zero is found
			}
		}
		return index;
	}

	/**
	 * The main Forward Checking algorithm to solve the puzzle.
	 * It tries numbers and goes back if a number does not work.
	 *
	 * @param asn The current list of numbers in the puzzle.
	 * @return A completed list if it works, or null if it fails.
	 */
	public ArrayList<Integer> FC(ArrayList<Integer> asn) {
        // Base Case: If there are no empty spots left, the puzzle is solved
		if (!asn.contains(0)) {
			return asn;
		}
        
        // Step 1: Select the next unassigned variable (X)
		int X = getFirstZeroIndex(asn);

        // Step 2: Create a deep copy of the current domains (Dold)
        // This is vital for backtracking, so changes can be reverted if a path fails
		ArrayList<ArrayList<Integer>> Dold = new ArrayList<>();
		for (ArrayList<Integer> domain : D) {
			Dold.add(new ArrayList<>(domain));
		}

        // Step 3: Iterate over all remaining valid values for the variable X
		ArrayList<Integer> DX = new ArrayList<>(D.get(X));
		for (Integer V : DX) { 
            // Step 4: Check if assigning V to X maintains consistency
			if (AC_FC(X, V)){ 
				asn.set(X, V); // Apply the assignment
                
                // Step 5: Recursively call FC with the new assignment
				ArrayList<Integer> R = FC(asn); 
				if (R != null) { 
					return R; // A solution was found down this path
				}
                
                // Backtrack: If the path failed, reset the assignment
				asn.set(X,0); 
				
                // Restore domains from the deep copy
				D = new ArrayList<>();
				for (ArrayList<Integer> oldDomain : Dold) {
					D.add(new ArrayList<>(oldDomain));
				}
			} else {
                // Restore domains if AC_FC fails, preparing for the next iteration of V
				D = new ArrayList<>();
				for (ArrayList<Integer> oldDomain : Dold) {
					D.add(new ArrayList<>(oldDomain));
				}
			}
		}
		return null; // Failure: No valid assignment found for X
	}

	/**
	 * Checks if putting a number in a space keeps the puzzle valid.
	 * It looks at the row, column, and block to make sure rules are not broken.
	 *
	 * @param X The space (variable) we are checking.
	 * @param V The number (value) we want to put in space X.
	 * @return True if the number is allowed, false if it breaks the rules.
	 */
	public boolean AC_FC(Integer X, Integer V){
        // Reduce the domain of X to only contain the assigned value V
		D.get(X).clear();	
		D.get(X).add(V);
		
        // Identify all future variables (Q) that depend on X
		ArrayList<Integer> Q = new ArrayList<Integer>(); 
		int col = GetColumn(X);
		int row = GetRow(X);
		int cell_x = row / size;
		int cell_y = col / size;
		
		// Add related variables in the same column
		for (int i=0; i<size*size; i++){
			if (GetVariable(i,col) > X) {
				Q.add(GetVariable(i,col));
			}
		}
        // Add related variables in the same row
		for (int j=0; j<size*size; j++){
			if (GetVariable(row,j) > X) {
				Q.add(GetVariable(row,j));
			}
		}
        // Add related variables in the same block
		for (int i=cell_x*size; i<=cell_x*size + 2; i++) {
			for (int j=cell_y*size; j<=cell_y*size + 2; j++){
				if (GetVariable(i,j) > X) {
					Q.add(GetVariable(i,j));
				}
			}
		}
	
        // Enforce arc consistency for all dependent variables in Q
		boolean consistent = true;
		while (!Q.isEmpty() && consistent){
			Integer Y = (Integer) Q.remove(0);
			if (REVISE(Y,X)) {
                // If a domain becomes empty, the assignment is invalid
				consistent = !D.get(Y).isEmpty();
			}
		}
		return consistent;
	}	
	
	/**
	 * Removes numbers from the choices of one space if they conflict with another space.
	 *
	 * @param Xi The first space to check.
	 * @param Xj The second space to check against.
	 * @return True if any numbers were removed, false if nothing changed.
	 */
	public boolean REVISE(int Xi, int Xj){
		Integer zero = 0;
		
		assert(Xi >= 0 && Xj >=0);
		assert(Xi < size*size*size*size && Xj <size*size*size*size);
		assert(Xi != Xj);
		
		boolean DELETED = false;

		ArrayList<Integer> Di = D.get(Xi);
		ArrayList<Integer> Dj = D.get(Xj);	
		
        // Loop through all values in the domain of Xi
		for (int i=0; i<Di.size(); i++){
			Integer vi = (Integer) Di.get(i);
			ArrayList<Integer> xiEqVal = new ArrayList<Integer>(size*size*size*size);	
			for (int var=0; var<size*size*size*size; var++){
				xiEqVal.add(var,zero);				
			}

            // Temporarily assign vi to Xi
			xiEqVal.set(Xi,vi);
			
            // Check if there is at least one supporting value vj in Dj
			boolean hasSupport = false;	
			for (int j=0; j<Dj.size(); j++){
				Integer vj = (Integer) Dj.get(j);
				if (CONSISTENT(xiEqVal, Xj, vj)) {
					hasSupport = true;
					break;
				}
			}
			
            // If no support is found, vi is an invalid choice and must be removed
			if (hasSupport == false) {
				Di.remove((Integer) vi);
				i--; // Adjust index after removal
				DELETED = true;
			}
		}
		
		return DELETED;
	}
			
	/**
	 * Checks if adding a specific number breaks the main Sudoku rules.
	 * It checks the row, column, and block for matching numbers.
	 *
	 * @param asn      The current list of numbers.
	 * @param variable The space we want to fill.
	 * @param val      The number to test.
	 * @return True if the number is safe to add, false if it matches another number.
	 */
	public boolean CONSISTENT(ArrayList<Integer> asn, Integer variable, Integer val) {
		Integer v1,v2;
		
		assert(asn.get(variable) == 0);
		asn.set(variable,val);

        // Check for duplicates in the columns
		for (int i=0; i<size*size; i++) {
			for (int j=0; j<size*size; j++) {
				for (int k=0; k<size*size; k++) {
					if (k != j) {
						v1 = (Integer) asn.get(GetVariable(i,j));
						v2 = (Integer) asn.get(GetVariable(i,k));
						if (v1 != 0 && v2 != 0 && v1.compareTo(v2) == 0) {
							asn.set(variable,0); // Undo assignment
							return false;
						}
					}
				}
			}
		}
	
        // Check for duplicates in the rows
		for (int j=0; j<size*size; j++) {
			for (int i=0; i<size*size; i++) {
				for (int k=0; k<size*size; k++) {
					if (k != i) {
						v1 = (Integer) asn.get(GetVariable(i,j));
						v2 = (Integer) asn.get(GetVariable(k,j));
						if (v1 != 0 && v2 != 0 && v1.compareTo(v2) == 0) {
							asn.set(variable,0); // Undo assignment		 				
							return false;
						}
					}
				}
			}
		}
		
        // Check for duplicates in the 3x3 blocks
		for (int i=0; i<size; i++) {
			for (int j=0; j<size; j++) {
				for (int i1 = 0; i1<size; i1++) {
					for (int j1=0; j1<size; j1++) {
						int var1 = GetVariable(size*i + i1, size*j + j1);
						for (int i2 = 0; i2<size; i2++) {
							for (int j2=0; j2<size; j2++) {
								int var2 = GetVariable(size*i+i2, size*j + j2);
								if (var1 != var2) {
									v1 = (Integer) asn.get(var1);
									v2 = (Integer) asn.get(var2);
									if (v1 != 0 && v2 != 0 && v1.compareTo(v2) == 0) {
										asn.set(variable,0); // Undo assignment		 			 				
										return false;
									}
								}
							}
						}
					}
				}
			}
		}

		asn.set(variable,0); // Undo temporary assignment
		return true; // The value is consistent
	}	
	
	/**
	 * Checks the starting numbers of the puzzle to make sure they are valid together.
	 *
	 * @param anAssignment The starting list of numbers.
	 * @return True if the starting numbers are valid, false if they conflict.
	 */
	public boolean INITIAL_FC(ArrayList<Integer> anAssignment) {
		for (int i=0; i<anAssignment.size(); i++){
			Integer V = (Integer) anAssignment.get(i);
			if (V != 0){
                // For every initially assigned value, find connected variables
				ArrayList<Integer> Q = GetRelevantVariables(i);
				boolean consistent = true;
                
                // Enforce consistency against all initially empty cells
				while (!Q.isEmpty() && consistent){
					Integer Y = (Integer) Q.remove(0);
					if (anAssignment.get(Y) == 0) {
						if (REVISE(Y,i)) {
							consistent = !D.get(Y).isEmpty();
						}
					}
				}	
				if (!consistent) return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Finds all the spaces in the same row, column, and block as the given space.
	 *
	 * @param X The space to find neighbors for.
	 * @return A list of connected spaces.
	 */
	public ArrayList<Integer> GetRelevantVariables(Integer X){
		ArrayList<Integer> Q = new ArrayList<Integer>(); 
		int col = GetColumn(X);
		int row = GetRow(X);
		int cell_x = row / size;
		int cell_y = col / size;
		
		// Find dependencies in column
		for (int i=0; i<size*size; i++){
			if (GetVariable(i,col) != X) {
				Q.add(GetVariable(i,col));
			}
		}
		// Find dependencies in row
		for (int j=0; j<size*size; j++){
			if (GetVariable(row,j) != X) {
				Q.add(GetVariable(row,j));
			}
		}
		// Find dependencies in block
		for (int i=cell_x*size; i<=cell_x*size + 2; i++) {
			for (int j=cell_y*size; j<=cell_y*size + 2; j++){
				if (GetVariable(i,j) != X) {
					Q.add(GetVariable(i,j));
				}
			}
		}	
		
		return Q;
	}
	
	/**
	 * Changes a 2D array puzzle into a flat list of numbers.
	 *
	 * @param p The 2D array puzzle.
	 * @return A flat list of numbers representing the puzzle.
	 */
	public ArrayList<Integer> GetAssignment(int[][] p) {
		ArrayList<Integer> asn = new ArrayList<Integer>();
		for (int i=0; i<size*size; i++) {
			for (int j=0; j<size*size; j++) {
				asn.add(GetVariable(i,j), p[i][j]);
                // If the cell is not empty, update its domain to only contain that value
				if (p[i][j] != 0){
					D.get(GetVariable(i,j)).clear();
					D.get(GetVariable(i,j)).add(p[i][j]);
				}
			}
		}
		return asn;
	}

	/**
	 * Changes a flat list of numbers back into a 2D array puzzle.
	 *
	 * @param asn The flat list of numbers.
	 * @return The 2D array puzzle.
	 */
	public int[][] GetPuzzle(ArrayList<Integer> asn) {
		int[][] p = new int[size*size][size*size];
		for (int i=0; i<size*size; i++) {
			for (int j=0; j<size*size; j++) {
				Integer val = (Integer) asn.get(GetVariable(i,j));
				p[i][j] = val.intValue();
			}
		}
		return p;
	}

	/**
	 * Gets the flat list index from a row and column.
	 *
	 * @param i The row index.
	 * @param j The column index.
	 * @return The flat list index.
	 */
	public int GetVariable(int i, int j){
		assert(i<size*size && j<size*size);
		assert(i>=0 && j>=0);		
		return (i*size*size + j);	
	}	
	
	/**
	 * Gets the row number from a flat list index.
	 *
	 * @param X The flat list index.
	 * @return The row number.
	 */
	public int GetRow(int X){
		return (X / (size*size)); 	
	}	
	
	/**
	 * Gets the column number from a flat list index.
	 *
	 * @param X The flat list index.
	 * @return The column number.
	 */
	public int GetColumn(int X){
		return X - ((X / (size*size))*size*size);	
	}	
}