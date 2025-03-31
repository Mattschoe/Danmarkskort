package Navigation;


public class HaversineDistance {
    private double haversineDistance;
    private double lat1;
    private double lon1;
    private double lat2;
    private double lon2;

    /***
     * Calculate the shortest 'as the crow flies'-distance between two nodes given by their latitudes and longitudes
     * Will have an influence on the weight of each line
     */
    public HaversineDistance(double lat1, double lon1, double lat2, double lon2) {
       this.lat1 = lat1;
       this.lon1 = lon1;
       this.lat2 = lat2;
       this.lon2 = lon2;
    }

    //snuppet fra geeksforgeeks:
    void calcHaversineDistance() {
        // distance between latitudes and longitudes
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        double a = Math.pow(Math.sin(dLat / 2), 2) +
                Math.pow(Math.sin(dLon / 2), 2) *
                        Math.cos(lat1) *
                        Math.cos(lat2);
        double rad = 6371; //jordens radius i km
        double c = 2 * Math.asin(Math.sqrt(a));
        haversineDistance = rad * c;
    }

  /*  public static void main(String[] args){
        HaversineDistance distance = new HaversineDistance(51.5007, 0.1246, 40.6892, 74.0445);
        System.out.println(distance.calcHaversineDistance());
    }*/
}
