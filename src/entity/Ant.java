package entity;

import configs.Configs;
import entity.logic.Tree;
import openGL.entities.RenderableObject;
import openGL.utils.Maths;
import openGL.world.World;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import openGL.renderEngine.DisplayManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Random;


public class Ant extends RenderableObject implements Comparable<Ant> {

    private static final NumberFormat SCORE_FORMATTER = new DecimalFormat("#00.0");
    private static final NumberFormat POS_FORMATTER = new DecimalFormat("00");
    private static final NumberFormat DEFAULT_FORMATTER = new DecimalFormat("00");

    private Vector3f targetPosition;
    private float targetRot;

    private Vector3f lastPosition;
    private float lastRot;

    private Tree decisionTree;
    private Food food;

    private int nbFoodToHome = 0;

    private float fitnessScore = 0;
    private boolean fitnessEvalPossible = true;

    public Ant(Vector3f position, float rotY) {
        super(Configs.antTexturedModel, position, 0, rotY, 0, .3f);
        targetPosition = new Vector3f(position);
        lastPosition = new Vector3f(position);
        lastRot = rotY;
        targetRot = rotY;
        decisionTree = Tree.generateRandomTree(3, 7);
    }

    /**
     * Update the ant's position, rotation, and behaviour
     * @param newAction whether or not the ant need to perform a new action
     * @param world the world into which the action need to be performed
     */
    public void update(boolean newAction, World world) {
        if (Configs.renderSimulation)
            calculateRenderingData(world);

        if (isCarryingFood()) {
            food.getPosition().x = position.x;
            food.getPosition().y = position.y + .2f;
            food.getPosition().z = position.z;
        }
        if (newAction) {
            position.x = targetPosition.x;
            position.z = targetPosition.z;
            lastPosition.x = position.x;
            lastPosition.z = position.z;
            Maths.loopVector(position, new Vector3f(world.getSizeX(), 0, world.getSizeZ()));
            Maths.loopVector(targetPosition, new Vector3f(world.getSizeX(), 0, world.getSizeZ()));
            Maths.loopVector(lastPosition, new Vector3f(world.getSizeX(), 0, world.getSizeZ()));
            rotY = targetRot;
            lastRot = rotY;
            decisionTree.makeDecision(this, world);
        }
    }

    /**
     * Update the ant's inter action position and rotation to create a transition effect between two action
     * @param world the world into which the action is performed
     */
    private void calculateRenderingData(World world) {
        position.y = world.getHeight(position.x, position.z);

        Vector2f rotXZ = Maths.calculateXZRotations(world, position.x, position.z);
        rotX = rotXZ.x;
        rotZ = rotXZ.y;

        float percent = (float) DisplayManager.timeSinceLastInterval() / Configs.ACTION_DURATION;
        position.x = (targetPosition.x - lastPosition.x) * percent + lastPosition.x;
        position.z = (targetPosition.z - lastPosition.z) * percent + lastPosition.z;
        rotY = (targetRot - lastRot) * Maths.clamp((float) (2.618033 * percent), 0, 1) + lastRot;
        if (isCarryingFood()) {
            food.setRotY(rotY);
            food.setRotX(rotX);
            food.setRotZ(rotZ);
        }
    }

    /**
     * Set the targeted position of the ant that need to be achieved for the next action
     * @param targetPosition the targeted position
     */
    public void setTargetPosition(Vector3f targetPosition) {
        this.targetPosition = targetPosition;
    }

    /**
     * Set the targeted rotation of the ant around the Y-axis that need to be achieved for the next action
     * @param targetRot the targeted rotation around the Y-axis
     */
    public void setTargetRot(float targetRot) {
        this.targetRot = targetRot;
    }

    /**
     * Return the food entity carried by teh ant
     * @return the food entity carried by the ant
     */
    public Food getFood() {
        return food;
    }

    /**
     * Set the ant's carried food
     * @param food the food to be carried
     */
    public void setFood(Food food) {
        this.food = food;
    }

    /**
     * Return whether or not the ant is carrying food
     * @return whether or not the ant is carrying food
     */
    public boolean isCarryingFood() {
        return food != null;
    }

    /**
     * Get the target position of the ant
     * @return the position that the ant aims for for the next action
     */
    public Vector3f getTargetPosition() {
        return targetPosition;
    }

    /**
     * Get the target rotation of the ant around the Y-axis
     * @return the rotation around the Y-axis that the ant aims for for the next action
     */
    public float getTargetRot() {
        return targetRot;
    }

    /**
     * Return the fitness score of the ant
     * @return fitness score of the ant
     */
    public float getFitnessScore() {
        return fitnessScore;
    }

    /**
     * Update the fitness score of the ant if possible
     * @param fit the amount to add to the fitness score
     */
    public void addFitness(float fit) {
        if (fitnessEvalPossible)
            fitnessScore += fit;
    }

    /**
     * Set the update mode of the ant
     * Used when the ant is out of a simulation to not affect the generation scores
     * @param fitnessEvalPossible whether or not the ant can be evaluated
     */
    public void setFitnessEvalPossible(boolean fitnessEvalPossible) {
        this.fitnessEvalPossible = fitnessEvalPossible;
    }

    /**
     * Add 1 to the number of food returned to an anthill
     */
    public void addFoodToHome() {
        nbFoodToHome++;
    }

    /**
     * Get the number of food already returned to an anthill
     * @return number of food returned to an anthill
     */
    public int getNbFoodToHome() {
        return nbFoodToHome;
    }

    /**
     * Reset the ant to it's default state
     * @param world world used to generate a valid position
     */
    public void reset(World world) {
        Random rand = new Random();
        fitnessScore = 0;
        nbFoodToHome = 0;
        food = null;
        rotY = 0;
        lastRot = rotY;
        targetRot = rotY;
        position = new Vector3f(rand.nextInt(world.getSizeX()) + 0.5f, 0, rand.nextInt(world.getSizeZ()) + 0.5f);
        targetPosition = new Vector3f(position);
        lastPosition = new Vector3f(position);
    }

    /**
     * Get the ant as a dom element containing its decision tree, position, fitness score, carried food and home
     * @param document the dom used to generate the element
     * @return an element representing the ant
     */
    public Element getAsElement(Document document) {
        Element antNode = document.createElement("ant");

        Element treeElement = decisionTree.getAsElement(document);
        antNode.appendChild(treeElement);
        antNode.setAttribute("posX", String.valueOf(Maths.clamp(targetPosition.x - .5f, 0, 49)));
        antNode.setAttribute("rotY", String.valueOf(targetRot));
        antNode.setAttribute("posZ", String.valueOf(Maths.clamp(targetPosition.z - .5f, 0, 49)));
        antNode.setAttribute("fitness", String.valueOf(fitnessScore));
        return antNode;
    }

    /**
     * Create an Ant from a DOM element
     * @param elem the DOM element representing an Ant
     * @return an Ant characterized by the DOM element
     */
    public static Ant getFromElement(Element elem) {
        if (elem.getTagName().equals("ant")) {
            float posX = Float.parseFloat(elem.getAttribute("posX")) + .5f;
            float posZ = Float.parseFloat(elem.getAttribute("posZ")) + .5f;
            float rotY = Float.parseFloat(elem.getAttribute("rotY"));
            float fitnessScore = Float.parseFloat(elem.getAttribute("fitness"));
            Element treeElem = (Element) elem.getElementsByTagName("tree").item(0);
            Tree tree = Tree.getFromElement(treeElem);
            Ant ant = new Ant(new Vector3f(posX, 0, posZ), rotY);
            ant.fitnessScore = fitnessScore;
            if (tree != null)
                tree.simplify();
                ant.decisionTree = tree;
            return ant;
        }
        return null;
    }

    /**
     * Compare the current ant to another ant, by using it's fitness score
     * This method is used to sort a Collection of ant by their fitness score, order from best to worst
     * @param ant the ant to compare to
     * @return 1 if the passed ant has a better fitness score than the current ant, 0 if equal, -1 otherwise
     */
    @Override
    public int compareTo(Ant ant) {
        return Float.compare(ant.fitnessScore, fitnessScore);
    }

    /**
     * Get the ant's decision tree
     * @return the ant's decision tree
     */
    public Tree getDecisionTree() {
        return decisionTree;
    }

    /**
     * Set the ant's new decision tree
     * @param decisionTree the new decision tree
     */
    public void setDecisionTree(Tree decisionTree) {
        this.decisionTree = decisionTree;
    }

    /**
     * Return a formatted string representation of the ant
     * @return a string representing the ant
     */
    @Override
    public String toString() {
        return SCORE_FORMATTER.format(fitnessScore) + " : Ant (" + POS_FORMATTER.format((int)position.x) + "; " + POS_FORMATTER.format((int)position.z) + ") : " + DEFAULT_FORMATTER.format(nbFoodToHome) + " foods";
    }
}

