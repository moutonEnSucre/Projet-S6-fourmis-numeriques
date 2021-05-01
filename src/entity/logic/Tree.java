package entity.logic;

import entity.Ant;
import entity.logic.action.*;
import openGL.world.World;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class Tree {

    private Node head;

    public Tree() {
        this.head = new Node(null, new ActionForward());
    }

    /**
     * Execute the tree according an ant and a world
     * @param a the ant executing the action
     * @param world the world in which the ant execute the action
     */
    public void makeDecision(Ant a, World world) {
        head.execute(a, world);
    }

    /**
     * Create a DOM element from the tree
     * @param document the DOM used to generate the element
     * @return a DOM element representing the tree
     */
    public Element getAsElement(Document document) {
        Element treeNode = document.createElement("tree");

        Element headElement = head.getAsElement(document, "head", 0);
        treeNode.appendChild(headElement);
        return treeNode;
    }

    /**
     * Simplify the tree
     */
    public void simplify() {
        head.simplifyDuplicateSubCondition(new ArrayList<String>(), new ArrayList<String>());
        head.simplifySymetricConditions();
    }

    /**
     * Get the depth of the tree
     * @return the max depth of the tree
     */
    public int getLevel() {
        return head.getLevel();
    }

    /**
     * Get the head node of the tree
     * @return the head of the tree
     */
    public Node getHead() {
        return head;
    }

    /**
     * Create a new tree by merging 2 existing tree, eventually apply a mutation
     * @param t1 the first parent tree
     * @param t2 the second parent tree
     * @param mutationRate the mutation rate applied to every node
     * @return a new tree generated by crossing the 2 parent trees
     */
    public static Tree crossBread(Tree t1, Tree t2, float mutationRate) {
        Tree child = new Tree();
        if (t1.head.getAction().isConditional() && t2.head.getAction().isConditional()) {
            child.head = t1.head.cloneNode(0);
            Random random = new Random();
            if (random.nextBoolean()) {
                child.head.setLeft(t2.head.getLeft().cloneNode(mutationRate));
                child.head.getLeft().setParent(child.head);
            } else {
                child.head.setRight(t2.head.getRight().cloneNode(mutationRate));
                child.head.getRight().setParent(child.head);
            }
            child.simplify();
            return child;
        }
        child.head = t1.head.cloneNode(mutationRate);
        return child;
    }

    /**
     * Create a tree from a DOM element representing a tree
     * @param elem the DOM element representing a tree
     * @return a tree characterized by the DOM element
     */
    public static Tree getFromElement(Element elem) {
        if (elem.getTagName().equals("tree")) {
            NodeList nodes = elem.getChildNodes();
            Tree tree = new Tree();
            for (int j = 0; j < nodes.getLength(); j++) {
                org.w3c.dom.Node node = nodes.item(j);
                if (node.getNodeName().equals("node")) {
                    tree.head = Node.createFromElement((Element) node, null);
                }
            }
            return tree;
        }
        return null;
    }

    /**
     * Save the tree to an XML file
     * @param file the path of the file to save to
     */
    public void saveToXML(String file) {
        try {
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element antNode = getAsElement(document);
            document.appendChild(antNode);

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            DOMSource domSource = new DOMSource(document);
            StreamResult streamResult = new StreamResult(file);

            transformer.transform(domSource, streamResult);

        } catch (ParserConfigurationException | TransformerException pce) {
            pce.printStackTrace();
        }
    }

    /**
     * Save a list of tree to an XML file
     * @param path the path of the file to save to
     * @param trees a list of trees to save
     */
    public static void saveListToXML(String path, List<Tree> trees) {
        try {
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            for (Tree tree : trees) {
                Element treeNode = tree.getAsElement(document);
                document.appendChild(treeNode);
            }
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            DOMSource domSource = new DOMSource(document);
            StreamResult streamResult = new StreamResult(new File(path));

            transformer.transform(domSource, streamResult);

        } catch (ParserConfigurationException | TransformerException pce) {
            pce.printStackTrace();
        }
    }

    /**
     * Load a tree from an XMl file representing a single tree
     * @param file the path of the file to load from
     * @return a tree characterized by the file
     */
    public static Tree loadFromXML(String file) {
        Tree tree = new Tree();
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document xml;

            xml = builder.parse(file);
            Element treeNode = (Element) xml.getElementsByTagName("tree").item(0);
            if (treeNode == null)
                return null;
            tree = Tree.getFromElement(treeNode);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        return tree;
    }

    /**
     * Create a list of trees from an XML file representing multiple trees
     * @param path the path of the file to load from
     * @return a list a tree characterized by the file
     */
    public static List<Tree> loadListFromXML(String path) {
        List<Tree> trees = new ArrayList<Tree>();

        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            File fileXML = new File(path);
            Document xml;

            xml = builder.parse(fileXML);
            NodeList treeNodes = xml.getElementsByTagName("tree");
            for (int i = 0; i < treeNodes.getLength(); i++) {
                Element element = (Element)treeNodes.item(i);
                NodeList nodes = element.getChildNodes();
                Tree tree = new Tree();
                for (int j = 0; j < nodes.getLength(); j++) {
                    org.w3c.dom.Node node = nodes.item(j);
                    if (node.getNodeName().equals("node")) {
                        tree.head = Node.createFromElement((Element) node, null);
                    }
                }
                trees.add(tree);
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        return trees;
    }

    /**
     * Return a random tree that has been simplified
     * @param minLevel minimum level of the unsimplified tree
     * @param maxLevel maximum level of the unsimplified tree
     * @return a simplified random tree
     */
    public static Tree generateRandomTree(int minLevel, int maxLevel) {
        Tree tree = new Tree();
        tree.head = new Node(null, Action.getRandomConditionalAction());
        generateSubTree(tree.head, 1, minLevel, maxLevel);
        tree.simplify();
        return tree;
    }

    /**
     * Generate a subtree
     * @param current the parent of the subtree to be generated
     * @param currentLevel the level of the current node
     * @param minLevel the minimum level of the total tree
     * @param maxLevel the maximum level of the total tree
     */
    private static void generateSubTree(Node current, int currentLevel, int minLevel, int maxLevel) {
        if (current.getAction().isConditional()) {
            if (currentLevel <= maxLevel && currentLevel >= minLevel) {
                current.setLeft(new Node(current, Action.getRandomAction()));
                current.setRight(new Node(current, Action.getRandomAction()));
            } else if (currentLevel < minLevel){
                current.setLeft(new Node(current, Action.getRandomConditionalAction()));
                current.setRight(new Node(current, Action.getRandomConditionalAction()));
            } else {
                current.setLeft(new Node(current, Action.getRandomSimpleAction()));
                current.setRight(new Node(current, Action.getRandomSimpleAction()));
            }
        }
        if (current.getAction().isConditional()) {
            generateSubTree(current.getRight(), currentLevel + 1, minLevel, maxLevel);
            generateSubTree(current.getLeft(), currentLevel + 1, minLevel, maxLevel);
        }
    }
}