/**
 * Generates the empty board configuration for the first run of the mapreduce in PossibleMoves.java
 *
 */
public class InitFirst {

    public static class Map extends Mapper<LongWritable, Text, IntWritable, MovesWritable> {
        /**
         * Write the empty board configuration to the context.
         *
         */
        @Override
        public void map(LongWritable key, Text val, Context context) throws IOException, InterruptedException {
            context.write(new IntWritable(0), new MovesWritable((byte)0, null));
        }
    }

    public static class Reduce extends Reducer<IntWritable, MovesWritable, IntWritable, MovesWritable> {
        /**
         * Write the empty board configuration to the context.
         *
         */
        @Override
        public void reduce(IntWritable key, Iterable<MovesWritable> values, Context context) throws IOException, InterruptedException {
            context.write(new IntWritable(0), new MovesWritable((byte)0, null));
        }
    }
}