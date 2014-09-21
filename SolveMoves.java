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

public class SolveMoves {
    final static Proj2Util proj_utils = new Proj2Util();
    public static class Map extends Mapper<IntWritable, MovesWritable, IntWritable, ByteWritable> {
        /**
         * Configuration and setup that occurs before map gets called for the first time.
         *
         **/
        @Override
        public void setup(Context context) {
        }

        /**
         * The map function for the second mapreduce that you should be filling out.
         */
        @Override
        public void map(IntWritable key, MovesWritable val, Context context) throws IOException, InterruptedException {
            

            int[] parents = val.getMoves();
            byte b = (byte) (val.getValue() + 0x04);
            for (int p : parents) {
                context.write(p, b);
            }
        }
    }

    public static class Reduce extends Reducer<IntWritable, ByteWritable, IntWritable, MovesWritable> {

        int boardWidth;
        int boardHeight;
        int connectWin;
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
            connectWin = context.getConfiguration().getInt("connectWin", 0);
            OTurn = context.getConfiguration().getBoolean("OTurn", true);
        }

        /**
         * The reduce function for the first mapreduce that you should be filling out.
         */
        @Override
        public void reduce(IntWritable key, Iterable<ByteWritable> values, Context context) throws IOException, InterruptedException {
            // go through all your values and check to see what is contained
            boolean has00 = false;
            boolean hasOther = false;
            for (ByteWritable b : values) {
                if (b.get() & 0x03 == 0) {
                    has00 = true;
                } else {
                    hasOther = true;
                }
                if (has00 && hasOther) {
                    break;
                }
            }

            ArrayList<IntWritable> arr = new ArrayList<IntWritable>();

            int numParents = 0;

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
                    if ((boardBuilder.charAt(index) == p) && ((index - 1 % boardHeight == 0) || (boardBuilder.charAt(index + 1) == ' '))) {
                        boardBuilder.deleteCharAt(index);
                        boardBuilder.insert(index, ' ');
                        next = proj_utils.gameHasher(boardBuilder.toString(), boardWidth, boardHeight);
                        arr.add(new IntWritable(next));
                        numParents += 1;
                        boardBuilder.deleteCharAt(index);
                        boardBuilder.insert(index, p);
                    } 
                }
            }

            int[] parents = new int[numParents];
            for (int i = 0; i < numParents; i++) {
                parents[i] = parentsList.get(i).get();
            }

            byte b = getBestByte(values);

            context.write(key, new MovesWritable(parents, b));
        }
    }

    private byte getBestByte(Iterable<ByteWritable> values) {
        int gameResult, gameByte, numSteps;
        int bestResult = 5; // = 2^2 + 1 i.e. it is larger than any possible value for the game result so we can compare in the for loop
        int fewestSteps = 65; // = 2^6 + 1 to be able to find smallest num of steps in for loop
        for (ByteWritable b : values) {
            gameByte = b.getValue();
            gameResult = gameByte & 0x03; // We want to look at the bottom two bits
            numSteps = gameByte & 0xFC; // Grab the upper 6 bits to look at # of steps left
            if ((OTurn) && (gameResult == 1) && (numSteps < fewestSteps)) { // We have found a better solution i.e. if it is OTurn, he has won and in fewer steps than otherwise found
                fewestSteps = numSteps;
                bestResult = gameResult;
            } else if ((!OTurn) && (gameResult == 2) && (numSteps < fewestSteps)) { // same condition as above
                fewestSteps = numSteps;
                bestResult = gameResult;
            } else if ((OTurn) && (gameResult == 2) && (numSteps > fewestSteps) && (bestResult != 3) && (bestResult != 1)) { // Oturn isn't winning, but he has found a path to prolong his death march
                fewestSteps = numSteps;
                bestResult = gameResult;
            } else if ((!OTurn) && (gameResult == 1) && (numSteps > fewestSteps) && (bestResult != 3) && (bestResult != 2)) { // same condition as above
                fewestSteps = numSteps;
                bestResult = gameResult;
            } else if ((gameResult == 3) && (numSteps > fewestSteps) && (!OTurn && bestResult != 2) && (OTurn && bestResult != 1)) { // We are tied & so we choose the shortest path making sure that it is not OTurn w/ him having a chance to win
                fewestSteps = numSteps;
                bestResult = gameResult;
            }
        }
        return ((fewestSteps << 2) | bestResult);
    }
}