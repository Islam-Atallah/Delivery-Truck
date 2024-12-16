public class DeliveryPoint {
    private int demand;
    private double x;
    private double y;

    public DeliveryPoint(int demand, double x, double y) {
        this.demand = demand;
        this.x = x;
        this.y = y;
    }

    public int getDemand() {
        return demand;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double distanceTo(DeliveryPoint other) {
        return Math.sqrt(Math.pow(this.x - other.x, 2) + Math.pow(this.y - other.y, 2));
    }
}
