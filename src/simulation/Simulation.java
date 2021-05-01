package simulation;

import configs.Configs;
import engineTester.MainGameLoop;
import entity.*;
import gui.MainGUI;
import openGL.entities.Camera;
import openGL.entities.Light;
import openGL.entities.RenderableObject;
import openGL.renderEngine.DisplayManager;
import openGL.renderEngine.MasterRenderer;
import openGL.utils.Maths;
import openGL.utils.MousePicker;
import openGL.world.World;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Simulation {

    private MasterRenderer renderer;

    private World world;

    private Camera camera;
    private Light light;
    private MousePicker mousePicker;
    private MainGUI gui;

    private boolean renderSelectedOnly = false;
    private boolean renderBestOnly = false;
    private boolean running = true;

    private boolean needToLoadSim = false;
    private String fileToLoadSimFrom = "";
    private boolean needToSaveSim = false;
    private String fileToSaveSimTo = "";

    private boolean needToLoadWorld = false;
    private String fileToLoadWorldFrom = "";
    private boolean needToSaveWorld = false;
    private String fileToSaveWorldTo = "";

    private boolean needToPassToNextGen = false;

    private Ant selectedAnt = null;
    private Ant bestAnt = null;

    private int generation = 1;
    private List<Score> scores;

    private long timeSinceLastGen = 0;

    public Simulation(String file, MainGUI gui) {
        world = World.loadFromXML(file);
        scores = new ArrayList<Score>();
        if (world == null) {
            world = new World();
            Population population = new Population();
            population.populate(Configs.nbAnts, world);
            world.setPopulation(population);
            world.fertilize((int) (Configs.nbAnts * Configs.maxNbFoodPerAnt), Configs.anthillEntrance);
        }
        camera = new Camera(world);
        renderer = new MasterRenderer(world);
        light = new Light(new Vector3f(20000, 20000, 20000), new Vector3f(1, 1, 1));
        mousePicker = new MousePicker(camera, renderer.getProjectionMatrix(), world.getPopulation().getAnts());
        this.gui = gui;
        gui.setWorld(world);
        scores.add(new Score(0, 0, 0,0));
    }

    public Simulation(MainGUI gui) {
        world = new World();
        world.fertilize((int) (Configs.nbAnts * Configs.maxNbFoodPerAnt), Configs.anthillEntrance);
        scores = new ArrayList<Score>();

        Population population = new Population();
        population.populate(Configs.nbAnts, world);
        world.setPopulation(population);
        camera = new Camera(world);
        renderer = new MasterRenderer(world);
        light = new Light(new Vector3f(20000, 20000, 20000), new Vector3f(1, 1, 1));
        mousePicker = new MousePicker(camera, renderer.getProjectionMatrix(), world.getPopulation().getAnts());
        this.gui = gui;
        gui.setWorld(world);
        scores.add(new Score(0, 0, 0, 0));
    }

    /**
     * Run the simulation
     * Effectively the main loop
     */
    public void run() {
        while (!Display.isCloseRequested()) {
            if (running && !renderBestOnly && !renderSelectedOnly) {
                timeSinceLastGen += DisplayManager.getFrameTimeMS();
                MainGameLoop.frame.setTitle("Generation : " + generation + " / Next generation in " + (Configs.generationTime - timeSinceLastGen / 1000) + "s");
                if (timeSinceLastGen >= Configs.generationTime * 1000)
                    dispatchNewGenerationEvent();
            }
            handleSimIO();
            handleWorldIO();
            handleNextGen();
            if (Keyboard.isKeyDown(Keyboard.KEY_W) && renderer.canToogleWireframe())
                renderer.toogleWireframe();
            camera.update();
            checkForFocus();
            if (running) {
                boolean newAction = DisplayManager.intervalHasPassed(Configs.ACTION_DURATION);
                if (!Configs.renderSimulation)
                    newAction = true;
                if (renderSelectedOnly) {
                    selectedAnt.setFitnessEvalPossible(false);
                    selectedAnt.update(newAction, world);
                } else if (renderBestOnly) {
                    bestAnt.setFitnessEvalPossible(false);
                    bestAnt.update(newAction, world);
                } else {
                    bestAnt = world.getPopulation().getAnts().get(0);
                    for (Ant ant : world.getPopulation().getAnts()) {
                        ant.setFitnessEvalPossible(true);
                        ant.update(newAction, world);
                        if (ant.isCarryingFood() && Configs.renderSimulation)
                            renderer.registerFood(ant.getFood());
                        if (ant.getFitnessScore() > bestAnt.getFitnessScore())
                            bestAnt = ant;
                    }
                }
                gui.setBestAnt(bestAnt);
                gui.updateCanvas();
                gui.setAntList(world.getPopulation().getAnts());
            }
            if (renderSelectedOnly && Configs.renderSimulation) {
                renderer.registerAnt(selectedAnt);
                if (selectedAnt.isCarryingFood())
                    renderer.registerFood(selectedAnt.getFood());
            } else if (renderBestOnly && Configs.renderSimulation) {
                    renderer.registerAnt(bestAnt);
                if (bestAnt.isCarryingFood())
                    renderer.registerFood(bestAnt.getFood());
            } else if (Configs.renderSimulation)
                renderer.registerAnts(world.getPopulation().getAnts());
            if (Configs.renderSimulation) {
                renderer.registerRenderableObjects(world.extractEntities());
                renderer.render(light, camera, Keyboard.isKeyDown(Keyboard.KEY_H));
            }
            DisplayManager.updateDisplay();
        }
    }

    /**
     * Update to ray casting util, the camera target antity ant the GUI attributes
     */
    private void checkForFocus() {
        mousePicker.update();
        if (Mouse.isButtonDown(0) && mousePicker.canSelectEntity()) {
            RenderableObject e = mousePicker.getClickedEntity(camera.getFocus());
            if (e != null) {
                camera.setFocus(e);
                if (e instanceof Ant) {
                    gui.setSelectedAnt((Ant) e);
                    selectedAnt = (Ant)e;
                } else {
                    gui.setSelectedAnt(null);
                    selectedAnt = null;
                }
            } else {
                camera.setFocus(world);
                gui.setSelectedAnt(null);
                selectedAnt = null;
            }
        }
    }

    /**
     * Get the simulation's population
     * @return the simulation's population
     */
    public Population getPopulation() {
        if (world != null)
            return world.getPopulation();
        return null;
    }

    /**
     * Called when the simumlation need to be saved or loaded
     */
    private void handleSimIO() {
        synchronized (this) {
            if (needToLoadSim && !fileToLoadSimFrom.equals("")) {
                loadFromXML(fileToLoadSimFrom);
                gui.updateParameterLabels();
                needToLoadSim = false;
                fileToLoadSimFrom = "";
                timeSinceLastGen = 0;
            }
            if (needToSaveSim && !fileToSaveSimTo.equals("")) {
                saveToXML(fileToSaveSimTo);
                needToSaveSim = false;
                fileToSaveSimTo = "";
                timeSinceLastGen = 0;
            }
        }
    }

    /**
     * Called when the simulation need to save or load a world map
     */
    private void handleWorldIO()  {
        synchronized (this) {
            if (needToLoadWorld && !fileToLoadWorldFrom.equals("")) {
                world = World.loadFromXML(fileToLoadWorldFrom);
                needToLoadWorld = false;
                fileToLoadWorldFrom = "";
                renderer.setWorld(world);
            }
            if (needToSaveWorld && !fileToSaveWorldTo.equals("")) {
                world.saveToXML(fileToSaveWorldTo);
                needToSaveWorld = false;
                fileToSaveWorldTo = "";
            }
        }
    }

    /**
     * Called when the simulation need to advance to the next generation of ant
     */
    private void handleNextGen() {
        synchronized (this) {
            if (needToPassToNextGen) {
                Score currentGenScore = getPopulation().nextGeneration(world);
                generation++;
                bestAnt = null;
                selectedAnt = null;
                camera.setFocus(world);
                gui.setSelectedAnt(null);
                gui.setBestAnt(null);
                scores.add(currentGenScore);
                gui.setScore(convertToLists());
                needToPassToNextGen = false;
                renderSelectedOnly = false;
                renderBestOnly = false;
                running = true;
                if (Configs.worldNeedRegeneration) {
                    world.fertilize((int) (Configs.nbAnts * Configs.maxNbFoodPerAnt), Configs.anthillEntrance);
                } else {
                    List<RenderableObject> anthills = world.extractEntities(EntityTypes.ANTHILL);
                    world.fertilize((int) (Configs.nbAnts * Configs.maxNbFoodPerAnt), 0);
                    for (RenderableObject anthill : anthills)
                        world.addEntity(anthill);
                }
                timeSinceLastGen = 0;
            }
        }
    }

    /**
     * Clean up the renderer
     */
    public void cleanUp() {
        renderer.clear();
    }

    /**
     * Return whether or not the simumlation is running
     * @return is the simulation running
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Pause the simulation until resume() is called
     */
    public void pause() {
        this.running = false;
    }

    /**
     * resume the simulation
     */
    public void resume() { this.running = true; }

    /**
     * Return whether or not the selected ant is isolated
     * @return is the selected ant isolated
     */
    public boolean isRenderSelectedOnly() {
        return renderSelectedOnly;
    }

    /**
     * Set whether or not the selected ant need to be isolated
     * @param renderSelectedOnly whether or not the selected ant need to be isolated
     */
    public void setRenderSelectedOnly(boolean renderSelectedOnly) {
        synchronized (this) {
            if (renderSelectedOnly && selectedAnt == null) return;
            this.renderSelectedOnly = renderSelectedOnly;
            if (renderSelectedOnly)
                renderBestOnly = false;
        }
    }

    /**
     * Return whether or not the best ant is isolated
     * @return is the best ant isolated
     */
    public boolean isRenderBestOnly() {
        return renderBestOnly;
    }

    /**
     * Set whether or not the best ant need to be isolated
     * @param renderBestOnly whether or not the best ant need to be isolated
     */
    public void setRenderBestOnly(boolean renderBestOnly) {
        synchronized (this) {
            if (renderBestOnly && bestAnt == null) return;
            this.renderBestOnly = renderBestOnly;
            if (renderBestOnly)
                renderSelectedOnly = false;
        }
    }

    /**
     * Get the current generation of ant
     * @return the current generation id
     */
    public int getGeneration() {
        return generation;
    }

    /**
     * Set the selected ant and focus the camera on it
     * @param ant the new selected ant
     */
    public void setSelectedAnt(Ant ant) {
        selectedAnt = ant;
        camera.setFocus(ant);
    }

    /**
     * Save the current simulation to an XML file
     * containing the world and the current simulation state (Generation, score graphs ...)
     * basically save the current simulation
     * @param path the file to save to
     */
    public void saveToXML(String path) {
        try {
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

            Element simulationNode = document.createElement("simulation");
            Element parameterNode = document.createElement("paramaters");

            parameterNode.setAttribute("nbAnts", String.valueOf(Configs.nbAnts));
            parameterNode.setAttribute("regenerateWorld", String.valueOf(Configs.worldNeedRegeneration));
            parameterNode.setAttribute("maxNbFoodPerAnt", String.valueOf(Configs.maxNbFoodPerAnt));
            parameterNode.setAttribute("anthillEntrance", String.valueOf(Configs.anthillEntrance));
            parameterNode.setAttribute("generationConservationRatio", String.valueOf(Configs.generationConservationRatio));
            parameterNode.setAttribute("mutationRate", String.valueOf(Configs.mutationRate));
            parameterNode.setAttribute("generationTime", String.valueOf(Configs.generationTime));

            simulationNode.setAttribute("generation", String.valueOf(generation));
            simulationNode.appendChild(parameterNode);

            Element worldNode = world.getAsElement(document);
            simulationNode.appendChild(worldNode);

            Element graphNode = document.createElement("graph");
            for (Score score : scores) {
                Element elem = document.createElement("point");
                elem.setAttribute("x", String.valueOf(score.getGeneration()));
                elem.setAttribute("max", String.valueOf(score.getMax()));
                elem.setAttribute("avg", String.valueOf(score.getAverage()));
                elem.setAttribute("min", String.valueOf(score.getMin()));
                graphNode.appendChild(elem);
            }
            simulationNode.appendChild(graphNode);

            document.appendChild(simulationNode);

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
     * Get a simulation from an XML file
     * @param path the file representing the simulation
     */
    public void loadFromXML(String path) {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            File fileXML = new File(path);
            Document xml;

            xml = builder.parse(fileXML);
            Element simulationNode = (Element) xml.getElementsByTagName("simulation").item(0);
            Element parameterNode = (Element) simulationNode.getElementsByTagName("paramaters").item(0);

            if (parameterNode != null) {
                Configs.nbAnts = Integer.parseInt(parameterNode.getAttribute("nbAnts"));
                Configs.worldNeedRegeneration = Boolean.parseBoolean(parameterNode.getAttribute("regenerateWorld"));
                Configs.maxNbFoodPerAnt = Float.parseFloat(parameterNode.getAttribute("maxNbFoodPerAnt"));
                Configs.anthillEntrance = Integer.parseInt(parameterNode.getAttribute("anthillEntrance"));
                Configs.generationConservationRatio = Float.parseFloat(parameterNode.getAttribute("generationConservationRatio"));
                Configs.mutationRate = Float.parseFloat(parameterNode.getAttribute("mutationRate"));
                Configs.generationTime = Integer.parseInt(parameterNode.getAttribute("generationTime"));
            }

            if (simulationNode == null)
                return;
            if (!simulationNode.getTagName().equals("simulation"))
                return;
            Node worldNode = simulationNode.getElementsByTagName("world").item(0);
            Node graphNode = simulationNode.getElementsByTagName("graph").item(0);
            if (worldNode == null)
                return;
            if (graphNode == null)
                return;
            world = World.getFromElement((Element)worldNode);
            generation = Integer.parseInt(simulationNode.getAttribute("generation"));
            NodeList scores = graphNode.getChildNodes();
            this.scores.clear();
            for (int i = 0; i < scores.getLength(); i++) {
                Node node = scores.item(i);
                if (node.getNodeName().equals("point")) {
                    Element elem = (Element) node;
                    int generation = Integer.parseInt(elem.getAttribute("x"));
                    double max = Double.parseDouble(elem.getAttribute("max"));
                    double avg = Double.parseDouble(elem.getAttribute("avg"));
                    double min = Double.parseDouble(elem.getAttribute("min"));
                    Score score = new Score(generation, max, avg, min);
                    this.scores.add(score);
                }
            }
            gui.setScore(convertToLists());
            renderer.setWorld(world);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Convert the list of Scores to a list of double arrays that can be fed to the GUI's score graphs
     * @return return a list of double arrays formatted as followed : {generations, averages, maxValues, minValues}
     */
    private List<double[]> convertToLists() {
        List<double[]> listPoints = new ArrayList<>();
        double[] x = new double[this.scores.size()];
        double[] avg = new double[this.scores.size()];
        double[] max = new double[this.scores.size()];
        double[] min = new double[this.scores.size()];
        for (int i = 0; i < this.scores.size(); i++) {
            Score score = this.scores.get(i);
            x[i] = score.getGeneration();
            max[i] = score.getMax();
            avg[i] = score.getAverage();
            min[i] = score.getMin();
        }
        listPoints.add(x);
        listPoints.add(avg);
        listPoints.add(max);
        listPoints.add(min);
        return listPoints;
    }

    /**
     * Notify the simulation that it need to be loaded from a file
     * Can be called from another thread, the reload will be executed by the main thread.
     * This is needed because the OpenGL context is only loaded in the main thread and is required to regenerate the world model
     * @param file the file to be loaded from
     */
    public void dispatchLoadEvent(String file) {
        synchronized (this) {
            fileToLoadSimFrom = file;
            needToLoadSim = true;
        }
    }

    /**
     * Notify the simulation that it need to be saved to a file
     * Can be called from another thread, the save will be executed by the main thread.
     * this methods exist only for the sake of symmetry with the loading mechanic
     * @param file the file to save to
     */
    public void dispatchSaveEvent(String file) {
        synchronized (this) {
            fileToSaveSimTo = file;
            needToSaveSim = true;
        }
    }

    /**
     * Notify the simulation that it need to load a world from a file
     * Can be called from another thread, the reload will be executed by the main thread.
     * This is needed because the OpenGL context is only loaded in the main thread and is required to regenerate the world model
     * @param file the file to be loaded from
     */
    public void dispatchLoadWorldEvent(String file) {
        synchronized (this) {
            fileToLoadWorldFrom = file;
            needToLoadWorld = true;
        }
    }

    /**
     * Notify the simulation that it need to save its world to a file
     * Can be called from another thread, the save will be executed by the main thread.
     * this methods exist only for the sake of symmetry with the loading mechanic
     * @param file the file to save to
     */
    public void dispatchSaveWorldEvent(String file) {
        synchronized (this) {
            fileToSaveWorldTo = file;
            needToSaveWorld = true;
        }
    }

    /**
     * Notify the simulation that it need pass to the next generation
     * Can be called from another thread
     */
    public void dispatchNewGenerationEvent() {
        synchronized (this) {
            needToPassToNextGen = true;
        }
    }
}
