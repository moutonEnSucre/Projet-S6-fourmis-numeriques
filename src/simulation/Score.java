package simulation;

public class Score {

    private int generation;
    private double max;
    private double min;
    private double average;

    public Score(int generation, double max, double average, double min) {
        this.generation = generation;
        this.max = max;
        this.min = min;
        this.average = average;
    }

    /**
     * Get the generation of the score object
     * @return the generation
     */
    public int getGeneration() {
        return generation;
    }

    /**
     * Get the maximum score of the generation
     * @return the maximum score of the generation
     */
    public double getMax() {
        return max;
    }

    /**
     * Get the average score of the generation
     * @return the average score of the generation
     */
    public double getAverage() {
        return average;
    }

    /**
     * Get the average score of the generation
     * @return the average score of the generation
     */
    public double getMin() {
        return min;
    }
}
