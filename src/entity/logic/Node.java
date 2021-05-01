package entity.logic;

import entity.Ant;
import entity.logic.action.Action;
import openGL.world.World;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.*;

public class Node {

    private Node right;
    private Node left;
    private Node parent;
    private Action action;

    private boolean minimal = false;

    public Node(Node parent, Action action) {
        this.parent = parent;
        this.action = action;
    }

    /**
     * Get the right child of the node
     * @return the right child
     */
    public Node getRight() {
        return right;
    }

    /**
     * Set the right child of the node
     * @param right the new right child
     */
    public void setRight(Node right) {
        this.right = right;
    }

    /**
     * Get the left child of the node
     * @return the left child
     */
    public Node getLeft() {
        return left;
    }

    /**
     * Set the left child of the node
     * @param left the new left child
     */
    public void setLeft(Node left) {
        this.left = left;
    }

    /**
     * Get the parent node
     * @return the parent node
     */
    public Node getParent() {
        return parent;
    }

    /**
     * Set the node's parent node
     * @param parent the new parent node
     */
    public void setParent(Node parent) {
        this.parent = parent;
    }

    /**
     * Get the node's action
     * @return the node's action
     */
    public Action getAction() {
        return action;
    }

    /**
     * Simplify the node by removing recurrent conditions from the subtree
     * @param satisfied a List of String representing the satisfied condition
     * @param unsatisfied a List of String representing the unsatisfied condition
     */
    public void simplifyDuplicateSubCondition(List<String> satisfied, List<String> unsatisfied) {
        if (action.isConditional()) {
            if (satisfied.contains(action.toString()) || unsatisfied.contains(action.toString())) {
                if (satisfied.contains(action.toString())) {
                    this.action = Action.getAction(right.action.toString());
                    this.left = right.left;
                    this.right = right.right;
                    simplifyDuplicateSubCondition(new ArrayList<>(satisfied), new ArrayList<>(unsatisfied));
                } else {
                    this.action = Action.getAction(left.action.toString());
                    this.right = left.right;
                    this.left = left.left;
                    simplifyDuplicateSubCondition(new ArrayList<>(satisfied), new ArrayList<>(unsatisfied));
                }
            } else {
                List<String> newSatisfied = new ArrayList<>(satisfied);
                List<String> newUnsatisfied = new ArrayList<>(unsatisfied);
                newSatisfied.add(action.toString());
                newUnsatisfied.add(action.toString());
                left.simplifyDuplicateSubCondition(satisfied, newUnsatisfied);
                right.simplifyDuplicateSubCondition(newSatisfied, unsatisfied);
            }
        }
    }

    /**
     * Simplify the node by removing symetric condition from the subtree
     */
    public void simplifySymetricConditions() {
        if (action.isConditional()) {
            left.simplifySymetricConditions();
            right.simplifySymetricConditions();
            if (!right.action.isConditional() && !left.action.isConditional() && left.action.toString().equals(right.action.toString())) {
                action = Action.getAction(right.action.toString());
                left = null;
                right = null;
            }
        }
    }

    /**
     * Return the level of the subtree (regarding the node as the head
     * @return the node depth
     */
    public int getLevel() {
        if (!action.isConditional() || left == null || right == null)
            return 1;
        return Math.max(left.getLevel(), right.getLevel()) + 1;
    }

    /**
     * Recursively execute the node
     * @param a the ant executing the action
     * @param world the world in which the action is executed
     */
    public void execute(Ant a, World world) {
        if (!action.isConditional())
            action.execute(a, world);
        else if (action.isConditionSatisfied(a, world))
            right.execute(a, world);
        else
            left.execute(a, world);
    }

    /**
     * Create a DOM element from the node
     * @param document the DOM used to generate the element
     * @param type the type of node (right, left or head)
     * @param level the level of the node
     * @return a DOM element representing the node and it's subtrees
     */
    public Element getAsElement(Document document, String type, int level) {
        Element element = document.createElement("node");
        element.setAttribute("type", type);
        element.setAttribute("action", action.toString());
        element.setAttribute("level", String.valueOf(level));
        if (right != null) {
            Element rightElem = right.getAsElement(document, "right", level + 1);
            element.appendChild(rightElem);
        }
        if (left != null) {
            Element leftElem = left.getAsElement(document, "left", level + 1);
            element.appendChild(leftElem);
        }
        return element;
    }

    /**
     * Create a node and it's subtrees from a valid DOM element
     * @param element a DOM element representing a node
     * @param parent the parent node
     * @return a node characterized by the DOM element
     */
    public static Node createFromElement(Element element, Node parent) {
        String action = element.getAttribute("action");
        Node node = new Node(parent, Action.getAction(action));
        if (node.getAction().isConditional()) {
            NodeList nodes = element.getChildNodes();
            for (int i = 0; i < nodes.getLength(); i++) {
                org.w3c.dom.Node n = nodes.item(i);
                if (n.getNodeName().equals("node")) {
                    Element elem = (Element) n;
                    if (elem.getAttribute("type").equals("left"))
                        node.left = createFromElement(elem, node);
                    else if (elem.getAttribute("type").equals("right"))
                        node.right = createFromElement(elem, node);
                }
            }
        }
        return node;
    }

    /**
     * Create a copy of the node and it's subtrees and eventually apply a mutation
     * @param mutationRate the mutation rate to be applied
     * @return a copy of the node mutated in accord to the specified rate
     */
    public Node cloneNode(float mutationRate) {
        Random random = new Random();
        Node node = new Node(null, Action.getAction(action.toString()));
        if (random.nextFloat() + .0001f < mutationRate) {
            if (action.isConditional()) {
                Action newAction;
                do {
                    newAction = Action.getRandomConditionalAction();
                    node.action = newAction;
                } while (newAction.toString().equals(action.toString()));
            } else {
                Action newAction;
                do {
                    newAction = Action.getRandomSimpleAction();
                    node.action = newAction;
                } while (newAction.toString().equals(action.toString()));
            }
        }
        if (node.action.isConditional()) {
            node.left = left.cloneNode(mutationRate);
            node.left.parent = node;
            node.right = right.cloneNode(mutationRate);
            node.right.parent = node;
        }
        return node;
    }
}
