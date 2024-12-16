import java.util.ArrayList;
import java.util.List;

public class Truck {
    private int capacity;
    private int currentLoad;
    private List<DeliveryPoint> route;

    public Truck(int capacity) {
        this.capacity = capacity;
        this.currentLoad = 0;
        this.route = new ArrayList<>();
    }

    public int getCapacity() {
        return capacity;
    }

    public int getCurrentLoad() {
        return currentLoad;
    }

    public List<DeliveryPoint> getRoute() {
        return route;
    }

    public void setRoute(List<DeliveryPoint> route) {
        this.route = route;
        this.currentLoad = route.stream().mapToInt(DeliveryPoint::getDemand).sum();
    }

    public boolean canAddDeliveryPoint(DeliveryPoint dp) {
        return currentLoad + dp.getDemand() <= capacity;
    }

    public void addDeliveryPoint(DeliveryPoint dp) {
        if (canAddDeliveryPoint(dp)) {
            route.add(dp);
            currentLoad += dp.getDemand();
        }
    }

    public double calculateRouteDistance(DeliveryPoint depot) {
        double totalDistance = 0.0;
        DeliveryPoint previous = depot;
        for (DeliveryPoint dp : route) {
            totalDistance += previous.distanceTo(dp);
            previous = dp;
        }
        totalDistance += previous.distanceTo(depot);
        return totalDistance;
    }
}
