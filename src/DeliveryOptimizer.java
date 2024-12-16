import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DeliveryOptimizer extends JFrame {
    private List<DeliveryPoint> deliveryPoints;
    private DeliveryPoint depot;
    private List<Truck> solution;
    private SimulatedAnnealing simulatedAnnealing;
    private DrawingPanel drawingPanel;
    private int numTrucks;
    private int truckCapacity;
    private double initialTemperature = 1000;
    private int iterations = 100;
    private JLabel distanceLabel;
    private double totalDistance;

    public DeliveryOptimizer() {
        deliveryPoints = new ArrayList<>();

        setTitle("VRP");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Drawing panel
        drawingPanel = new DrawingPanel();
        drawingPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (depot == null) {
                    depot = new DeliveryPoint(0, e.getX(), e.getY());
                    drawingPanel.repaint();
                } else {
                    String demandStr = JOptionPane.showInputDialog("Enter demand for the point:");
                    try {
                        int demand = Integer.parseInt(demandStr);
                        deliveryPoints.add(new DeliveryPoint(demand, e.getX(), e.getY()));
                        drawingPanel.repaint();
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(null, "Invalid demand value");
                    }
                }
            }
        });
        add(drawingPanel, BorderLayout.CENTER);

        // Control panel
        JPanel controlPanel = new JPanel();
        JButton generateButton = new JButton("Initialize Random Path");
        JButton solveButton = new JButton("Solve VRP");

        generateButton.addActionListener(new GenerateButtonListener());
        solveButton.addActionListener(new SolveButtonListener());

        controlPanel.add(generateButton);
        controlPanel.add(solveButton);

        add(controlPanel, BorderLayout.SOUTH);

        // Distance label
        distanceLabel = new JLabel("Total Distance: N/A");
        add(distanceLabel, BorderLayout.NORTH);

        // Input for number of trucks and their capacity
        JPanel inputPanel = new JPanel();
        JTextField truckNumberField = new JTextField(5);
        JTextField truckCapacityField = new JTextField(5);
        inputPanel.add(new JLabel("Number of Trucks:"));
        inputPanel.add(truckNumberField);
        inputPanel.add(new JLabel("Truck Capacity:"));
        inputPanel.add(truckCapacityField);
        int result = JOptionPane.showConfirmDialog(null, inputPanel, "Enter Truck Details", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                numTrucks = Integer.parseInt(truckNumberField.getText());
                truckCapacity = Integer.parseInt(truckCapacityField.getText());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Invalid input. Exiting.");
                System.exit(1);
            }
        } else {
            System.exit(0);
        }
    }

    private class GenerateButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            List<DeliveryPoint> randomPath = generateRandomPath();
            simulatedAnnealing = new SimulatedAnnealing(randomPath, depot, numTrucks, truckCapacity, initialTemperature, iterations);
            simulatedAnnealing.initialize();
            solution = simulatedAnnealing.getSolution();

            totalDistance = 0;
            for (Truck truck : solution) {
                totalDistance+=truck.calculateRouteDistance(depot);
            }
            updateDistanceLabel(totalDistance);

            drawingPanel.repaint();
        }

        private List<DeliveryPoint> generateRandomPath() {
            List<DeliveryPoint> randomPath = new ArrayList<>(deliveryPoints);
            Random random = new Random();
            for (int i = randomPath.size() - 1; i > 0; i--) {
                int index = random.nextInt(i + 1);
                DeliveryPoint temp = randomPath.get(index);
                randomPath.set(index, randomPath.get(i));
                randomPath.set(i, temp);
            }
            return randomPath;
        }
    }

    private class SolveButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            simulatedAnnealing.solve();
            solution = simulatedAnnealing.getSolution();

            totalDistance = 0;
            for (Truck truck : solution) {
                totalDistance+=truck.calculateRouteDistance(depot);
            }
            updateDistanceLabel(totalDistance);
            drawingPanel.repaint();
        }
    }

    private void updateDistanceLabel(double distance) {
        distanceLabel.setText("Total Distance: " + distance);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            DeliveryOptimizer optimizer = new DeliveryOptimizer();
            optimizer.setVisible(true);
        });
    }

    private class DrawingPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;

            // Draw X and Y axes
            g2d.setColor(Color.BLACK);
            g2d.drawLine(50, getHeight() - 50, getWidth() - 50, getHeight() - 50); // X-axis
            g2d.drawLine(50, getHeight() - 50, 50, 50); // Y-axis

            // Draw depot
            if (depot != null) {
                g2d.setColor(Color.BLUE);
                g2d.fillOval((int) depot.getX() - 5, (int) depot.getY() - 5, 10, 10);
                g2d.drawString("Depot", (int) depot.getX() + 5, (int) depot.getY() - 5);
            }

            // Draw points
            for (DeliveryPoint dp : deliveryPoints) {
                g2d.setColor(Color.BLACK);
                g2d.fillOval((int) dp.getX() - 5, (int) dp.getY() - 5, 10, 10);
                g2d.drawString(String.valueOf(dp.getDemand()), (int) dp.getX() + 5, (int) dp.getY() - 5);
            }

            // Draw paths
            if (simulatedAnnealing != null) {
                List<Truck> solution = simulatedAnnealing.getSolution();
                for (int i = 0; i < solution.size(); i++) {
                    Truck truck = solution.get(i);
                    List<DeliveryPoint> route = truck.getRoute();
                    g2d.setColor(getRandomColor(i));
                    if (!route.isEmpty()) {
                        DeliveryPoint previous = depot;
                        for (DeliveryPoint dp : route) {
                            g2d.drawLine((int) previous.getX(), (int) previous.getY(), (int) dp.getX(), (int) dp.getY());
                            previous = dp;
                        }
                        g2d.drawLine((int) previous.getX(), (int) previous.getY(), (int) depot.getX(), (int) depot.getY());
                    }
                }
            }
        }

        private Color getRandomColor(int index) {
            // Generate distinct colors for each truck
            float hue = (float) (index * 0.618033988749895); // Golden ratio
            hue = hue - (int) hue; // Normalize to [0,1]
            return Color.getHSBColor(hue, 0.8f, 0.9f);
        }
    }
}
