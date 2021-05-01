package gui;

import configs.Configs;
import engineTester.MainGameLoop;
import entity.Ant;
import entity.logic.Tree;
import openGL.world.World;
import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.internal.series.MarkerSeries;
import org.knowm.xchart.style.markers.SeriesMarkers;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Collections;
import java.util.List;

public class MainGUI {

    private JTabbedPane tabContainer;
    public JPanel mainPanel;
    private JPanel selected;
    private JPanel best;
    private JPanel option;
    private JButton saveSelected;
    private JButton loadSelected;
    private JButton isolateSelected;
    private JPanel selectedControlPanel;
    private JPanel selectedTreeSubPanel;
    private JButton saveBest;
    private JButton loadBest;
    private JButton isolateBest;
    private JPanel bestControlPanel;
    private JPanel bestTreeSubPanel;
    private JButton saveWorld;
    private JPanel optionButtonPanel;
    private JButton loadWorld;
    private JButton saveBests;
    private JButton loadBests;
    private JButton pauseSimulation;
    private JButton forceGen;
    private JButton savSim;
    private JButton loadSim;
    private JList<Ant> antList;
    private JPanel infoPanel;
    private JPanel graphPane;
    private JLabel nbAntsLb;
    private JLabel mutationRateLb;
    private JLabel anthillEntrancesLabel;
    private JLabel foodPerAntLb;
    private JLabel generationRatioLb;
    private JLabel generationTimeLb;
    private JLabel rgenWorldLb;
    private TreePanel selectedTreePanel;
    private TreePanel bestTreePanel;
    private Ant selectedAnt;
    private Ant bestAnt;

    private XYChart bestChart;
    private XYChart averageChart;

    private XChartPanel<XYChart> bestChartPane;
    private XChartPanel<XYChart> averageChartPane;

    private boolean antListCanTrigger = false;

    public MainGUI() {
        Toolkit.getDefaultToolkit().setDynamicLayout(false);

        selectedTreePanel = new TreePanel();
        selectedTreePanel.setMinimumSize(new Dimension(500, 300));
        selectedTreePanel.setBorder(BorderFactory.createRaisedBevelBorder());
        selectedTreeSubPanel.add(selectedTreePanel);

        bestTreePanel = new TreePanel();
        bestTreePanel.setMinimumSize(new Dimension(500, 300));
        bestTreePanel.setBorder(BorderFactory.createRaisedBevelBorder());
        bestTreeSubPanel.add(bestTreePanel);

        updateParameterLabels();

        /*
          Listeners
         */
        saveSelected.addActionListener(actionEvent -> {
            pauseSimAndUpdateButton();
            if (selectedAnt != null) saveTreeToFile(selectedAnt.getDecisionTree());
        });
        loadSelected.addActionListener(actionEvent -> {
            pauseSimAndUpdateButton();
            if (selectedAnt != null) {
                Tree tree = loadTreeFromFile();
                if (tree != null) selectedAnt.setDecisionTree(tree);
            }
        });
        isolateSelected.addActionListener(actionEvent -> {
            if (selectedAnt != null) {
                pauseSimAndUpdateButton();
                boolean state = MainGameLoop.simulation.isRenderSelectedOnly();
                MainGameLoop.simulation.setRenderSelectedOnly(!state);
                if (!state) isolateSelected.setText("Show All");
                else isolateSelected.setText("Isolate");
                resumeSimAndUpdateButton();
            }
        });
        saveBest.addActionListener(actionEvent -> {
            pauseSimAndUpdateButton();
            if (bestAnt != null) saveTreeToFile(bestAnt.getDecisionTree());
        });
        loadBest.addActionListener(actionEvent -> {
            pauseSimAndUpdateButton();
            if (bestAnt != null) {
                Tree tree = loadTreeFromFile();
                if (tree != null) bestAnt.setDecisionTree(tree);
            }
        });
        isolateBest.addActionListener(actionEvent -> {
            if (bestAnt != null) {
                pauseSimAndUpdateButton();
                boolean state = MainGameLoop.simulation.isRenderBestOnly();
                MainGameLoop.simulation.setRenderBestOnly(!state);
                if (!state) isolateBest.setText("Show All");
                else isolateBest.setText("Isolate");
                resumeSimAndUpdateButton();
            }
        });
        antList.addListSelectionListener(listSelectionEvent -> {
            if (antListCanTrigger) {
                if (antList.getSelectedValue() != null) {
                    pauseSimAndUpdateButton();
                    selectedAnt = antList.getSelectedValue();
                    selectedTreePanel.setAnt(selectedAnt);
                    selectedTreePanel.updateUI();
                    MainGameLoop.simulation.setSelectedAnt(selectedAnt);
                    resumeSimAndUpdateButton();
                }
            }
        });
        pauseSimulation.addActionListener(actionEvent -> {
            if (!MainGameLoop.simulation.isRunning()) {
                resumeSimAndUpdateButton();
            } else {
                pauseSimAndUpdateButton();
            }
        });
        savSim.addActionListener(actionEvent -> {
            pauseSimAndUpdateButton();
            saveSimulationToFile();
        });
        loadSim.addActionListener(actionEvent -> {
            pauseSimAndUpdateButton();
            loadSimulationFromFile();
        });
        saveBests.addActionListener(actionEvent -> {
            pauseSimAndUpdateButton();
            saveBestAnts();
        });
        loadBests.addActionListener(actionEvent -> {
            pauseSimAndUpdateButton();
            loadBestAnts();
        });
        saveWorld.addActionListener(actionEvent -> {
            pauseSimAndUpdateButton();
            saveCurrentWorld();
        });
        loadWorld.addActionListener(actionEvent -> {
            pauseSimAndUpdateButton();
            loadCurrentWorld();
        });
        forceGen.addActionListener(actionEvent -> {
            pauseSimAndUpdateButton();
            MainGameLoop.simulation.dispatchNewGenerationEvent();
            resumeSimAndUpdateButton();
        });

        antList.setModel(new DefaultListModel<Ant>());
        antList.setFixedCellWidth(200);
        antList.setIgnoreRepaint(true);

        bestChart = new XYChartBuilder().width(650).height(300).title(getClass().getSimpleName()).xAxisTitle("Génération").yAxisTitle("Score").title("Scores extremes par générations").build();
        XYSeries bestSerie = bestChart.addSeries("Best", new double[]{0});
        XYSeries worstSerie = bestChart.addSeries("Worst", new double[]{0});
        bestSerie.setLineColor(Color.GREEN);
        bestSerie.setMarker(SeriesMarkers.NONE);
        worstSerie.setLineColor(Color.RED);
        worstSerie.setMarker(SeriesMarkers.NONE);
        bestChart.getStyler().setLegendVisible(true);

        averageChart = new XYChartBuilder().width(650).height(300).title(getClass().getSimpleName()).xAxisTitle("Génération").yAxisTitle("Score").title("Score moyen par génération").build();
        XYSeries averageSerie = averageChart.addSeries("Average", new double[]{0});
        averageSerie.setLineColor(Color.BLUE);
        averageSerie.setMarker(SeriesMarkers.NONE);
        averageChart.getStyler().setLegendVisible(false);

        bestChartPane = new XChartPanel<>(bestChart);
        averageChartPane = new XChartPanel<>(averageChart);

        graphPane.add(bestChartPane);
        graphPane.add(averageChartPane);
        graphPane.setBorder(BorderFactory.createRaisedBevelBorder());
        antList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                antListCanTrigger = true;
            }

            @Override
            public void mouseExited(MouseEvent e) {
                antListCanTrigger = false;
            }
        });
    }

    private void pauseSimAndUpdateButton() {
        MainGameLoop.simulation.pause();
        antList.setEnabled(false);
        pauseSimulation.setText("Resume Simulation");
    }

    private void resumeSimAndUpdateButton() {
        MainGameLoop.simulation.resume();
        antList.setEnabled(true);
        pauseSimulation.setText("Pause Simulation");
    }

    public void setSelectedAnt(Ant ant) {
        SwingUtilities.invokeLater(() -> {
            selectedAnt = ant;
            selectedTreePanel.setAnt(ant);
            selectedTreePanel.updateUI();
        });
    }

    public void setBestAnt(Ant ant) {
        SwingUtilities.invokeLater(() -> {
            bestAnt = ant;
            bestTreePanel.setAnt(ant);
            bestTreePanel.updateUI();
        });
    }

    public void setWorld(World world) {
        SwingUtilities.invokeLater(() -> {
            selectedTreePanel.setWorld(world);
            bestTreePanel.setWorld(world);
        });
    }

    public void updateCanvas() {
        SwingUtilities.invokeLater(() -> {
            selectedTreePanel.updateUI();
            bestTreePanel.updateUI();
        });
    }

    private void saveTreeToFile(Tree tree) {
        JFileChooser fc = new JFileChooser();
        File file = null;
        int returnVal = fc.showOpenDialog(this.mainPanel);

        if (returnVal == JFileChooser.APPROVE_OPTION)
            file = fc.getSelectedFile();
        else
            return;
        tree.saveToXML(file.getAbsolutePath());
    }

    private void saveBestAnts() {
        JFileChooser fc = new JFileChooser();
        File file = null;
        int returnVal = fc.showOpenDialog(this.mainPanel);

        if (returnVal == JFileChooser.APPROVE_OPTION)
            file = fc.getSelectedFile();
        else
            return;
        MainGameLoop.simulation.getPopulation().saveBestAntsToXML(file.getAbsolutePath());
    }

    private void loadBestAnts() {
        JFileChooser fc = new JFileChooser();
        File file = null;
        int returnVal = fc.showOpenDialog(this.mainPanel);

        if (returnVal == JFileChooser.APPROVE_OPTION)
            file = fc.getSelectedFile();
        else
            return;
        MainGameLoop.simulation.getPopulation().loadBestAntsFromXML(file.getAbsolutePath());
    }

    private Tree loadTreeFromFile() {
        JFileChooser fc = new JFileChooser();
        File file = null;
        int returnVal = fc.showOpenDialog(this.mainPanel);

        if (returnVal == JFileChooser.APPROVE_OPTION)
            file = fc.getSelectedFile();
        else
            return null;
        return Tree.loadFromXML(file.getAbsolutePath());
    }

    private void saveSimulationToFile() {
        JFileChooser fc = new JFileChooser();
        File file = null;
        int returnVal = fc.showOpenDialog(this.mainPanel);

        if (returnVal == JFileChooser.APPROVE_OPTION)
            file = fc.getSelectedFile();
        else
            return;
        MainGameLoop.simulation.dispatchSaveEvent(file.getAbsolutePath());
    }

    private void saveCurrentWorld() {
        JFileChooser fc = new JFileChooser();
        File file = null;
        int returnVal = fc.showOpenDialog(this.mainPanel);

        if (returnVal == JFileChooser.APPROVE_OPTION)
            file = fc.getSelectedFile();
        else
            return;
        MainGameLoop.simulation.dispatchSaveWorldEvent(file.getAbsolutePath());
    }

    private void loadCurrentWorld() {
        JFileChooser fc = new JFileChooser();
        File file = null;
        int returnVal = fc.showOpenDialog(this.mainPanel);

        if (returnVal == JFileChooser.APPROVE_OPTION)
            file = fc.getSelectedFile();
        else
            return;
        MainGameLoop.simulation.dispatchLoadWorldEvent(file.getAbsolutePath());
    }

    private void loadSimulationFromFile() {
        JFileChooser fc = new JFileChooser();
        File file = null;
        int returnVal = fc.showOpenDialog(this.mainPanel);

        if (returnVal == JFileChooser.APPROVE_OPTION)
            file = fc.getSelectedFile();
        else
            return;
        MainGameLoop.simulation.dispatchLoadEvent(file.getAbsolutePath());
    }

    public void setAntList(List<Ant> ants) {
        Collections.sort(ants);
        SwingUtilities.invokeLater(() -> {
            ((DefaultListModel<Ant>) antList.getModel()).clear();
            ((DefaultListModel<Ant>) antList.getModel()).addAll(ants);
            antList.setSelectedValue(selectedAnt, false);
            antList.repaint();
        });
    }

    public void setScore(List<double[]> scores) {
        SwingUtilities.invokeLater(() -> {
            averageChart.updateXYSeries("Average", scores.get(0), scores.get(1), null);
            averageChartPane.repaint();
            bestChart.updateXYSeries("Best", scores.get(0), scores.get(2), null);
            bestChart.updateXYSeries("Worst", scores.get(0), scores.get(3), null);
            bestChartPane.repaint();
        });
    }

    public void updateParameterLabels() {
        SwingUtilities.invokeLater(() -> {
            nbAntsLb.setText("Taille de la population  :  " + Configs.nbAnts);
            mutationRateLb.setText("Taux de mutation  :  " + (int) (Configs.mutationRate * 100000) / 1000f + " %");
            anthillEntrancesLabel.setText("Nombre de fourmillières  :  " + Configs.anthillEntrance);
            foodPerAntLb.setText("Nombre de nourritures par fourmis  :  " + Configs.maxNbFoodPerAnt);
            generationRatioLb.setText("Taux de conservation de la population  :  " + (int) (Configs.generationConservationRatio * 100000) / 1000f + " %");
            generationTimeLb.setText("Durée d'une génération  :  " + Configs.generationTime + " s");
            rgenWorldLb.setText("Regénérer le monde  :  " + (Configs.worldNeedRegeneration ? "Oui" : "Non"));
        });
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        tabContainer = new JTabbedPane();
        mainPanel.add(tabContainer, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        option = new JPanel();
        option.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        tabContainer.addTab("Option", option);
        optionButtonPanel = new JPanel();
        optionButtonPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(3, 4, new Insets(0, 0, 0, 0), -1, -1));
        option.add(optionButtonPanel, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_NORTH, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        saveWorld = new JButton();
        saveWorld.setText("Save World");
        optionButtonPanel.add(saveWorld, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        loadWorld = new JButton();
        loadWorld.setText("Load World");
        optionButtonPanel.add(loadWorld, new com.intellij.uiDesigner.core.GridConstraints(1, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        saveBests = new JButton();
        saveBests.setText("Save Best Ants");
        optionButtonPanel.add(saveBests, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        loadBests = new JButton();
        loadBests.setText("Load Best Ants");
        optionButtonPanel.add(loadBests, new com.intellij.uiDesigner.core.GridConstraints(2, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer1 = new com.intellij.uiDesigner.core.Spacer();
        optionButtonPanel.add(spacer1, new com.intellij.uiDesigner.core.GridConstraints(1, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        forceGen = new JButton();
        forceGen.setText("Force Next Generation");
        optionButtonPanel.add(forceGen, new com.intellij.uiDesigner.core.GridConstraints(2, 3, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        savSim = new JButton();
        savSim.setText("Save Simulation");
        optionButtonPanel.add(savSim, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        loadSim = new JButton();
        loadSim.setText("Load Simulation");
        optionButtonPanel.add(loadSim, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        pauseSimulation = new JButton();
        pauseSimulation.setText("Pause Simulation");
        optionButtonPanel.add(pauseSimulation, new com.intellij.uiDesigner.core.GridConstraints(0, 3, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        infoPanel = new JPanel();
        infoPanel.setLayout(new BorderLayout(0, 0));
        option.add(infoPanel, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        infoPanel.add(scrollPane1, BorderLayout.WEST);
        antList = new JList();
        antList.setSelectionMode(0);
        scrollPane1.setViewportView(antList);
        graphPane = new JPanel();
        graphPane.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        infoPanel.add(graphPane, BorderLayout.CENTER);
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(4, 2, new Insets(10, 10, 10, 10), -1, -1));
        option.add(panel1, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder(), null));
        nbAntsLb = new JLabel();
        nbAntsLb.setText("Label");
        panel1.add(nbAntsLb, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        mutationRateLb = new JLabel();
        mutationRateLb.setText("Label");
        panel1.add(mutationRateLb, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        anthillEntrancesLabel = new JLabel();
        anthillEntrancesLabel.setText("Label");
        panel1.add(anthillEntrancesLabel, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        foodPerAntLb = new JLabel();
        foodPerAntLb.setText("Label");
        panel1.add(foodPerAntLb, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        generationRatioLb = new JLabel();
        generationRatioLb.setText("Label");
        panel1.add(generationRatioLb, new com.intellij.uiDesigner.core.GridConstraints(1, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        generationTimeLb = new JLabel();
        generationTimeLb.setText("Label");
        panel1.add(generationTimeLb, new com.intellij.uiDesigner.core.GridConstraints(2, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        rgenWorldLb = new JLabel();
        rgenWorldLb.setText("Label");
        panel1.add(rgenWorldLb, new com.intellij.uiDesigner.core.GridConstraints(3, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        selected = new JPanel();
        selected.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        tabContainer.addTab("Selected", selected);
        selectedControlPanel = new JPanel();
        selectedControlPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 4, new Insets(0, 0, 0, 0), -1, -1));
        selected.add(selectedControlPanel, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_NORTH, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        saveSelected = new JButton();
        saveSelected.setText("Save");
        selectedControlPanel.add(saveSelected, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        loadSelected = new JButton();
        loadSelected.setText("Load");
        selectedControlPanel.add(loadSelected, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        isolateSelected = new JButton();
        isolateSelected.setText("Isolate");
        selectedControlPanel.add(isolateSelected, new com.intellij.uiDesigner.core.GridConstraints(0, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer2 = new com.intellij.uiDesigner.core.Spacer();
        selectedControlPanel.add(spacer2, new com.intellij.uiDesigner.core.GridConstraints(0, 3, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        selectedTreeSubPanel = new JPanel();
        selectedTreeSubPanel.setLayout(new BorderLayout(0, 0));
        selected.add(selectedTreeSubPanel, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        selectedTreeSubPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createRaisedBevelBorder(), null));
        best = new JPanel();
        best.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        tabContainer.addTab("Best Ant", best);
        bestControlPanel = new JPanel();
        bestControlPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 4, new Insets(0, 0, 0, 0), -1, -1));
        best.add(bestControlPanel, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_NORTH, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        saveBest = new JButton();
        saveBest.setText("Save");
        bestControlPanel.add(saveBest, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        loadBest = new JButton();
        loadBest.setText("Load");
        bestControlPanel.add(loadBest, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        isolateBest = new JButton();
        isolateBest.setText("Isolate");
        bestControlPanel.add(isolateBest, new com.intellij.uiDesigner.core.GridConstraints(0, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer3 = new com.intellij.uiDesigner.core.Spacer();
        bestControlPanel.add(spacer3, new com.intellij.uiDesigner.core.GridConstraints(0, 3, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        bestTreeSubPanel = new JPanel();
        bestTreeSubPanel.setLayout(new BorderLayout(0, 0));
        best.add(bestTreeSubPanel, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        bestTreeSubPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createRaisedBevelBorder(), null));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }
}
