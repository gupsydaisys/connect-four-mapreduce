/*
 * CS61C Spring 2014 Project2
 * Reminders:
 *
 * DO NOT SHARE CODE IN ANY WAY SHAPE OR FORM, NEITHER IN PUBLIC REPOS OR FOR DEBUGGING.
 *
 * This is one of the two files that you should be modifying and submitting for this project.
 */
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.lang.Math;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

public class PossibleMoves {
	final static Proj2Util proj_utils = new Proj2Util();

    public static class Map extends Mapper<IntWritable, MovesWritable, IntWritable, IntWritable> {
        int boardWidth;
        int boardHeight;
        boolean OTurn;
        /**
         * Configuration and setup that occurs before map gets called for the first time.
         *
         **/
        @Override
        public void setup(Context context) {
            // load up the config vars specified in Proj2.java#main()
            boardWidth = context.getConfiguration().getInt("boardWidth", 0);
            boardHeight = context.getConfiguration().getInt("boardHeight", 0);
            OTurn = context.getConfiguration().getBoolean("OTurn", true);
        }

        /**
         * The map function for the first mapreduce that you should be filling out.
         */
        @Override
        public void map(IntWritable key, MovesWritable val, Context context) throws IOException, InterruptedException {
            String board = proj_utils.gameUnhasher(key.get(), boardWidth, boardHeight);
            
            StringBuilder boardBuilder = new StringBuilder(board);

            int next, index, i, j;

            char p = 'O';
            if (!OTurn) {
                p = 'X';
            }

            for (i = 0; i < boardWidth; i++) {
                for (j = 0; j < boardHeight; j++) {
                    index = j + i * boardWidth;
                    if ( (boardBuilder.charAt(index) == ' ') &&
                    	( (index % boardHeight == 0) || (boardBuilder.charAt(index - 1) != ' ') ) ) {
                        boardBuilder.deleteCharAt(index);
                        boardBuilder.insert(index, p);
                        next = proj_utils.gameHasher(boardBuilder.toString(), boardWidth, boardHeight);
                        context.write(new IntWritable(next), key);
                        boardBuilder.deleteCharAt(index);
                        boardBuilder.insert(index, ' ');
                    } 
                }
            }
        }
    }

    public static class Reduce extends Reducer<IntWritable, IntWritable, IntWritable, MovesWritable> {

        int boardWidth;
        int boardHeight;
        int connectWin;
        boolean OTurn;
        boolean lastRound;
        /**
         * Configuration and setup that occurs before reduce gets called for the first time.
         *
         **/
        @Override
        public void setup(Context context) {
            // load up the config vars specified in Proj2.java#main()
            boardWidth = context.getConfiguration().getInt("boardWidth", 0);
            boardHeight = context.getConfiguration().getInt("boardHeight", 0);
            connectWin = context.getConfiguration().getInt("connectWin", 0);
            OTurn = context.getConfiguration().getBoolean("OTurn", true);
            lastRound = context.getConfiguration().getBoolean("lastRound", true);
        }

        /**
         * The reduce function for the first mapreduce that you should be filling out.
         */
        @Override
        public void reduce(IntWritable key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
        	Iterator<IntWritable> iter = values.iterator();
        	List<IntWritable> parentsList = new ArrayList<IntWritable>();
        	int numParents = 0;
        	while (iter.hasNext()) {
        		numParents += 1;
        	    parentsList.add(iter.next());
        	}

        	int[] parents = new int[numParents];
        	for (int i = 0; i < numParents; i++) {
        		parents[i] = parentsList.get(i).get();
        	}

        	byte val;
        	boolean isDone = proj_utils.gameFinished(proj_utils.gameUnhasher(key.get(), boardWidth, boardHeight), boardWidth, boardHeight, connectWin);
        	if (isDone) {
        	    if (OTurn) {
        	        val = 1;
        	    } else {
        	        val = 2;
        	    }
        	} else if (lastRound) {
    	        val = 3;
    	    } else {
    	        val = 0;
    	    }

        	context.write(key, new MovesWritable(val, 0, parents));
        }
    }
}
