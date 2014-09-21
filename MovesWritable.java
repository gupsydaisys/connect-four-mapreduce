    /** 
     * Contains the 32 bit integer hash representation of all parent game states, i.e.
     * all game states which can result in the associated game state after 1 turn.
     *
     * To correctly use this field, simply use the provided getter and setter methods.
     */
    private int[] moves;
    private int counter;

   public MovesWritable() { }

    /**
     * Creates a MovesWritable with the fields set to the provided values.
     *
     * @param status the status of the move, where 0 means undecided, 1 means O wins,
     * 2 means X wins, and 3 means draw
     * @param movesToEnd the number of moves left before the game terminates from this
     * board position (assuming optimal play from both sides)
     * @param moves the array of parent moves for this particular board configuration
     */
    public MovesWritable(int status, int movesToEnd, int[] moves) {
        this.value = (byte) ((status & 3) & (movesToEnd << 2));
        if (moves == null) {
            this.counter = 0;
            this.moves = null;
        } else {
            this.counter = moves.length;
            this.moves = new int[counter];
            System.arraycopy(moves, 0, this.moves, 0, this.counter);
        }
    }

    /**
     * Another moves writable that takes in the inputs for value and an array of parent moves.
     *
     * @param value the combined byte representation of the number of moves to game termination
     * and the board state, where the last two bits are board state and the remaining six bits
     * are the moves to end of game
     * @param moves the array of parent moves for this particular board configuration
     */
    public MovesWritable(byte value, int[] moves) {
        this.value = value;
        if (moves == null) {
            this.counter = 0;
            this.moves = null;
        } else {
            this.counter = moves.length;
            this.moves = new int[counter];
            System.arraycopy(moves, 0, this.moves, 0, this.counter);
        }
    }

    /**
     * Returns the status of the given board position, where 0 means undecided, 1 means O wins,
     * 2 means X wins, and 3 means draw.
     *
     */
    public int getStatus() {
        return this.value & 3;
    }

    /**
     * Sets the status of the given board position, where 0 means undecided, 1 means O wins,
     * 2 means X wins, and 3 means draw.
     *
     * @param status the status of the board position
     */
    public void setStatus(int status) {
        this.value = (byte)(((this.value >> 2) << 2) | (status & 3));
    }

    /**
     * Returns the number of moves until the termination of the given board position.
     *
     */
    public int getMovesToEnd() {
        return this.value >> 2;
    }

    /**
     * Sets the number of moves until the termination of the given board position.
     *
     * @param movesToEnd the number of moves left until the game ends (assuming optimal play)
     */
    public void setMovesToEnd(int movesToEnd) {
        this.value = (byte)((this.value & 3) | (movesToEnd << 2));
    }
 
    /**
     * Sets the value of the given board position, where the last 2 bits are the
     * status of the state, and the remaining bits are the moves until termination.
     *
     * @param val the value of the current position where the last 2 bits are the status
     * and the remaining bits are moves to termination
     */
    public void setValue(byte val) {
        this.value = val;
    }

    /**
     * Returns the value of the given board position, where the last 2 bits are the status
     * of the state, and the remaining bits are the moves until termination.
     *
     */
    public byte getValue() {
        return this.value;
    }

    /**
     * Sets the array of parents of this board position.
     *
     * @param in_moves the array of parents to be written into this MovesWritable
     */
    public void setMoves(int [] in_moves) {
        this.counter = in_moves.length;
        this.moves = new int[counter];
        System.arraycopy(in_moves, 0, this.moves, 0, this.counter);
    }

    /**
     * Returns the array of parents of this board position.
     *
     */
    public int[] getMoves() {
        return this.moves;
    }

