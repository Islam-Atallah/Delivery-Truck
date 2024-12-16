import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SimulatedAnnealing {
    private List<DeliveryPoint> deliveryPoints;
    private DeliveryPoint depot;
    private List<Truck> trucks;
    private double initialTemperature;
    private double currentDistance;
    private List<Truck> currentSolution;
    private List<Truck> bestSolution;
    private double bestDistance;
    private int iterations;
    private boolean firstIteration = true;

    public SimulatedAnnealing(List<DeliveryPoint> deliveryPoints, DeliveryPoint depot, int numTrucks, int truckCapacity, double initialTemperature, int iterations) {
        this.deliveryPoints = deliveryPoints;
        this.depot = depot;
        this.trucks = new ArrayList<>();
        for (int i = 0; i < numTrucks; i++) {
            this.trucks.add(new Truck(truckCapacity));
        }
        this.initialTemperature = initialTemperature;
        this.iterations = iterations;
    }

    public void initialize(){
        // Generate initial solution
        if (firstIteration) {
            generateInitialSolution();
            currentDistance = calculateTotalDistance();
            currentSolution = cloneTrucks(trucks);
            bestDistance = currentDistance;
            bestSolution = currentSolution;
        }
        firstIteration = false;
    }

    public void solve() {
        if (!firstIteration) {
            double nextDistance;
            double difference;
            double temperature = initialTemperature;
            List<Truck> nextSolution;

            Random random = new Random();

            for (int i = 0; i < iterations; i++) {

                temperature *= initialTemperature/ Math.log(i);;

                nextSolution = generateNeighborSolution(currentSolution, random);

                nextDistance = calculateTotalDistance(nextSolution);

                difference = nextDistance - currentDistance;

                if (difference < 0) {
                    currentSolution = nextSolution;
                    currentDistance = nextDistance;

                    if (bestDistance > currentDistance) {
                        bestSolution = cloneTrucks(currentSolution);
                        bestDistance = currentDistance;
                    }
                } else if (Math.exp(-difference / temperature) > Math.random()) {
                    currentSolution = nextSolution;
                    currentDistance = nextDistance;
                }
            }
            trucks = cloneTrucks(bestSolution);
        }
    }

    private void generateInitialSolution() {
        Collections.shuffle(deliveryPoints);
        for (DeliveryPoint dp : deliveryPoints) {
            for (Truck truck : trucks) {
                if (truck.canAddDeliveryPoint(dp)) {
                    truck.addDeliveryPoint(dp);
                    break;
                }
            }
        }
    }

    private List<Truck> generateNeighborSolution(List<Truck> trucks, Random random) {
        List<Truck> newSolution = cloneTrucks(trucks);
        Truck truck1 = newSolution.get(random.nextInt(trucks.size()));
        Truck truck2 = newSolution.get(random.nextInt(trucks.size()));

        if (!truck1.getRoute().isEmpty() && !truck2.getRoute().isEmpty()) {
            int routeIndex1 = random.nextInt(truck1.getRoute().size());
            int routeIndex2 = random.nextInt(truck2.getRoute().size());

            DeliveryPoint dp1 = truck1.getRoute().get(routeIndex1);
            DeliveryPoint dp2 = truck2.getRoute().get(routeIndex2);

            if (truck1.getCurrentLoad() - dp1.getDemand() + dp2.getDemand() <= truck1.getCapacity() &&
                    truck2.getCurrentLoad() - dp2.getDemand() + dp1.getDemand() <= truck2.getCapacity()) {
                truck1.getRoute().set(routeIndex1, dp2);
                truck2.getRoute().set(routeIndex2, dp1);
            }
        }
        return newSolution;
    }

    private double calculateTotalDistance() {
        return calculateTotalDistance(trucks);
    }

    private double calculateTotalDistance(List<Truck> trucks) {
        double totalDistance = 0.0;
        for (Truck truck : trucks) {
            totalDistance += truck.calculateRouteDistance(depot);
        }
        return totalDistance;
    }

    private List<Truck> cloneTrucks(List<Truck> trucks) {
        List<Truck> clonedTrucks = new ArrayList<>();
        for (Truck truck : trucks) {
            Truck clonedTruck = new Truck(truck.getCapacity());
            clonedTruck.setRoute(new ArrayList<>(truck.getRoute()));
            clonedTrucks.add(clonedTruck);
        }
        return clonedTrucks;
    }

    public List<Truck> getSolution() {
        return trucks;
    }
}
