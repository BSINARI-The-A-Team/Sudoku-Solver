import java.util.ArrayList;


public class SudokuSolver implements ISudokuSolver {

	int[][] puzzle;
	int size;
	ArrayList<ArrayList<Integer>> D; //= new ArrayList<ArrayList<Integer>>();
	
	/*
	getPuzzle() returns the content of the sudoku puzzle as a 2 dimensional
	array of integers. If there are no inserted values in the puzzle the array should
	contain zeros in all entries. If a value has been inserted at a specific place, then
	the corresponding entry in the array should have that particular value. The
	puzzle has entry (0,0) in the upper left corner 
	*/
	public int[][] getPuzzle() {
		return puzzle;
	}

	/*
	setValue(col, row, value) inserts the specified value in the specified 
	positioni n the puzzle given by a column number and a row number. 
	Again, the numbering starts with the 0th column and the 0th row in the upper left 
	corner of the puzzle. Inserted values should be in the legal range (1-9 for a normal
	sudoku), and these should only be inserted in places that correspond to places
	in the sudoku; the method should therefore check this before insertion. In order
	to be able to erase already inserted values, it should also be possible to insert
	the value 0.
	*/
	public void setValue(int col, int row, int value) {
		if (value >= 0 && value < 10){
			if(col < size || row < size ){
				puzzle[col][row] = value;
			}
		}
		
	}

	/*
	The setup() function initializes a sudoku puzzle of the specified size, such
	that all entries are empty (the array contains 0s). The size is given in number 
	of blocks in a row, ie. the size is 3 for a normal sized sudoku. In this
	function you can initialize auxiliary datastructures for FC algorithm.
	*/
	public void setup(int size1) {
		size = size1;
		puzzle = new int[size*size][size*size];
		D = new ArrayList<ArrayList<Integer>>(size*size*size*size);
		
		//Initialize each D[X]...
		
	}

	/*
	The most interesting (and hard) method to implement is the function solve().
	This method should check if the puzzle with the current content can be solved
	or not and return true, if it is solvable and false otherwise. If the puzzle is solvable, 
	the puzzle should furthermore be solved and the result saved, such that
	when getPuzzle() is called subsequently, it will return the content of the full
	solution. To solve the puzzle, the FC algorithm should be used.
	*/
	public boolean solve() {
		ArrayList<Integer> asn = GetAssignment(puzzle);
		
		//INITIAL_FC
		//FC

		return true;
	}

	public void readInPuzzle(int[][] p) {
		puzzle = p;
	}
	

		private int getFirstZeroIndex(ArrayList<Integer> asn){
			int index = 0;
			for (int i = 0; i < asn.size()-1; i++) {
				if(asn.get(i) == 0){
					index = i;
					break;
				}
			}
			return index;
		}
		//---------------------------------------------------------------------------------
		//YOUR TASK:  Implement FC(asn)
		//---------------------------------------------------------------------------------
		public ArrayList FC(ArrayList<Integer> asn) {
			if (!asn.contains(0)) {
				return asn;
			}
			int X = getFirstZeroIndex(asn);

			// Make deep copy of the old domain by 
			// creating them as separate objects
			ArrayList<ArrayList<Integer>> Dold = new ArrayList<>();
			for (ArrayList<Integer> domain : D) {
				Dold.add(new ArrayList<>(domain));
			}
			//------------------------------------

			for (Integer V : D.get(X)) { // for all V ∈ DX do
				if (AC_FC(X, V)){ // if AC-FC(X, V ) then
					asn.set(X, V); // asn[X] ← V
					ArrayList<Integer> R = FC(asn); // R ←FC(asn)
					if (R != null) { // if R 6= fail then
						return R; // return R
					}
					asn.set(X,0); // asn[X] ← 0
					
					D = new ArrayList<>();
					for (ArrayList<Integer> oldDomain : Dold) {
						D.add(new ArrayList<>(oldDomain));
					}
				} else {
					D = new ArrayList<>();
					for (ArrayList<Integer> oldDomain : Dold) {
						D.add(new ArrayList<>(oldDomain));
					}
				}
			}
			return null;//failure
		}

	

		
		//---------------------------------------------------------------------------------
		// CODE SUPPORT FOR IMPLEMENTING FC(asn)
		//
		// It is possible to implement FC(asn) by using only AC_FC function from below.
		// 
		// If you have time, I strongly reccomend that you implement AC_FC and REVISE from scratch
		// using only implementation of CONSISTENT algorithm and general utility functions. In my opinion
		// by doing this, you will gain much more from this exercise.
		//
		//---------------------------------------------------------------------------------
		
		
	
		//------------------------------------------------------------------
		//				AC_FC
		//
		// Implementation of acr-consistency for forward-checking AC-FC(cv).
		// This is a key component of FC algorithm, and the only function you need to 
		// use in your FC(asn) implementation
		//------------------------------------------------------------------
		public boolean AC_FC(Integer X, Integer V){
			//Reduce domain Dx
			D.get(X).clear();	
			D.get(X).add(V);
			
			//Put in Q all relevant Y where Y>X
			ArrayList<Integer> Q = new ArrayList<Integer>(); //list of all relevant Y
			int col = GetColumn(X);
			int row = GetRow(X);
			int cell_x = row / size;
			int cell_y = col / size;
			
			//all variables in the same column
			for (int i=0; i<size*size; i++){
				if (GetVariable(i,col) > X) {
					Q.add(GetVariable(i,col));
				}
			}
			//all variables in the same row
			for (int j=0; j<size*size; j++){
				if (GetVariable(row,j) > X) {
					Q.add(GetVariable(row,j));
				}
			}
			//all variables in the same size*size box
			for (int i=cell_x*size; i<=cell_x*size + 2; i++) {
				for (int j=cell_y*size; j<=cell_y*size + 2; j++){
					if (GetVariable(i,j) > X) {
						Q.add(GetVariable(i,j));
					}
				}
			}
		
			//REVISE(Y,X)
			boolean consistent = true;
			while (!Q.isEmpty() && consistent){
				Integer Y = (Integer) Q.remove(0);
				if (REVISE(Y,X)) {
					consistent = !D.get(Y).isEmpty();
				}
			}
			return consistent;
		}	
		
		
		//------------------------------------------------------------------
		//				REVISE 
		//------------------------------------------------------------------
		public boolean REVISE(int Xi, int Xj){
			Integer zero = new Integer(0);
			
			assert(Xi >= 0 && Xj >=0);
			assert(Xi < size*size*size*size && Xj <size*size*size*size);
			assert(Xi != Xj);
			
			boolean DELETED = false;

			
			ArrayList<Integer> Di = D.get(Xi);
			ArrayList<Integer> Dj = D.get(Xj);	
			
			for (int i=0; i<Di.size(); i++){
				Integer vi = (Integer) Di.get(i);
				ArrayList<Integer> xiEqVal = new ArrayList<Integer>(size*size*size*size);	
				for (int var=0; var<size*size*size*size; var++){
					xiEqVal.add(var,zero);				
				}

				xiEqVal.set(Xi,vi);
				
				boolean hasSupport = false;	
				for (int j=0; j<Dj.size(); j++){
					Integer vj = (Integer) Dj.get(j);
					if (CONSISTENT(xiEqVal, Xj, vj)) {
						hasSupport = true;
						break;
					}
				}
				
				if (hasSupport == false) {
					Di.remove((Integer) vi);
					DELETED = true;
				}
				
			}
			
			return DELETED;
		}
				
		

		
		//------------------------------------------------------------------
		//CONSISTENT: 
		//
		//Given a partiall assignment "asn"  checks whether its extension with 
		//variable = val is consistent with Sudoku rules, i.e. whether it violates
		//any of constraints whose all variables in the scope have been assigned. 
		//This implicitly encodes all constraints describing Sudoku.
		//
		//Before it returns, it undoes the temporary assignment variable=val
		//It can be used as a building block for REVISE and AC-FC
		//
		//NOTE: the procedure assumes that all assigned values are in the range 
		// 		{0,..,9}. 
		//-------------------------------------------------------------------
		public boolean CONSISTENT(ArrayList<Integer> asn, Integer variable, Integer val) {
			Integer v1,v2;
			
			//variable to be assigned must be clear
			assert(asn.get(variable) == 0);
			asn.set(variable,val);

			//alldiff(col[i])
		 	for (int i=0; i<size*size; i++) {
		 		for (int j=0; j<size*size; j++) {
		 			for (int k=0; k<size*size; k++) {
			 			if (k != j) {
			 				v1 = (Integer) asn.get(GetVariable(i,j));
			 				v2 = (Integer) asn.get(GetVariable(i,k));
				 			if (v1 != 0 && v2 != 0 && v1.compareTo(v2) == 0) {
				 				asn.set(variable,0);
				 				return false;
				 			}
				 		}
		 			}
		 		}
		 	}
		

		 	
		 	//alldiff(row[j])
		 	for (int j=0; j<size*size; j++) {
		 		for (int i=0; i<size*size; i++) {
		 			for (int k=0; k<size*size; k++) {
			 			if (k != i) {
			 				v1 = (Integer) asn.get(GetVariable(i,j));
			 				v2 = (Integer) asn.get(GetVariable(k,j));
				 			if (v1 != 0 && v2 != 0 && v1.compareTo(v2) == 0) {
				 				asn.set(variable,0);			 				
				 				return false;
				 			}
			 			}
		 			}
		 		}
		 	}
		 	

		 	//alldiff(block[size*i,size*j])
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
		 					 				asn.set(variable,0);	 		 			 				
		 					 				return false;
		 					 			}
		 		 					}
		 		 				}
		 		 			}
	 
		 				}
		 			}
		 		}
		 	}

			asn.set(variable,0);
			return true;
		}	
		
		

	
		//------------------------------------------------------------------
		//						INITIAL_FC
		//------------------------------------------------------------------
		public boolean INITIAL_FC(ArrayList<Integer> anAssignment) {
			//Enforces consistency between unassigned variables and all 
			//initially assigned values; 
			for (int i=0; i<anAssignment.size(); i++){
				Integer V = (Integer) anAssignment.get(i);
				if (V != 0){
					ArrayList<Integer> Q = GetRelevantVariables(i);
					boolean consistent = true;
					while (!Q.isEmpty() && consistent){
						Integer Y = (Integer) Q.remove(0);
						if (REVISE(Y,i)) {
							consistent = !D.get(Y).isEmpty();
						}
					}	
					if (!consistent) return false;
				}
			}
			
			return true;
		}
		
		
	
		
		//------------------------------------------------------------------
		//						GetRelevantVariables
		//------------------------------------------------------------------
		public ArrayList<Integer> GetRelevantVariables(Integer X){
			//Returns all variables that are interdependent of X, i.e. 
			//all variables involved in a binary constraint with X
			ArrayList<Integer> Q = new ArrayList<Integer>(); //list of all relevant Y
			int col = GetColumn(X);
			int row = GetRow(X);
			int cell_x = row / size;
			int cell_y = col / size;
			
			//all variables in the same column
			for (int i=0; i<size*size; i++){
				if (GetVariable(i,col) != X) {
					Q.add(GetVariable(i,col));
				}
			}
			//all variables in the same row
			for (int j=0; j<size*size; j++){
				if (GetVariable(row,j) != X) {
					Q.add(GetVariable(row,j));
				}
			}
			//all variables in the same size*size cell
			for (int i=cell_x*size; i<=cell_x*size + 2; i++) {
				for (int j=cell_y*size; j<=cell_y*size + 2; j++){
					if (GetVariable(i,j) != X) {
						Q.add(GetVariable(i,j));
					}
				}
			}	
			
			return Q;
		}
		
		



		//------------------------------------------------------------------
		// Functions translating between the puzzle and an assignment
		//-------------------------------------------------------------------
		public ArrayList<Integer> GetAssignment(int[][] p) {
			ArrayList<Integer> asn = new ArrayList<Integer>();
			for (int i=0; i<size*size; i++) {
				for (int j=0; j<size*size; j++) {
					asn.add(GetVariable(i,j), new Integer(p[i][j]));
					if (p[i][j] != 0){
							//restrict domain
							D.get(GetVariable(i,j)).clear();
							D.get(GetVariable(i,j)).add(new Integer(p[i][j]));
						}
				}
			}
			return asn;
		}	
		
		/*
		getPuzzle() returns the content of the sudoku puzzle as a 2 dimensional
		array of integers. If there are no inserted values in the puzzle the array should
		contain zeros in all entries. If a value has been inserted at a specific place, then
		the corresponding entry in the array should have that particular value. The
		puzzle has entry (0,0) in the upper left corner
		*/
		public int[][] GetPuzzle(ArrayList asn) {
			int[][] p = new int[size*size][size*size];
			for (int i=0; i<size*size; i++) {
				for (int j=0; j<size*size; j++) {
					Integer val = (Integer) asn.get(GetVariable(i,j));
					p[i][j] = val.intValue();
				}
			}
			return p;
		}

	
		//------------------------------------------------------------------
		//Utility functions
		//-------------------------------------------------------------------
		public int GetVariable(int i, int j){
			assert(i<size*size && j<size*size);
			assert(i>=0 && j>=0);		
			return (i*size*size + j);	
		}	
		
		
		public int GetRow(int X){
			return (X / (size*size)); 	
		}	
		
		public int GetColumn(int X){
			return X - ((X / (size*size))*size*size);	
		}	
		
		
		
		
		
}
