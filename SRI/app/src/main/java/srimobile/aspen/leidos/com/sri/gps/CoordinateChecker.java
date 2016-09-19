package srimobile.aspen.leidos.com.sri.gps;

import android.location.Location;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class CoordinateChecker {
    public static double PI = 3.14159265;
    public static double TWOPI = 2 * PI;

    Map<String, Map<String, ArrayList<Double>>> stations = new HashMap<String, Map<String, ArrayList<Double>>>();
    Map<String, ArrayList<Double>> location;

    public String geoFenceId;

    public CoordinateChecker() {

        location = new HashMap<String, ArrayList<Double>>();
        location.put("HB_APPROACH", new ArrayList<Double>(Arrays.asList(
                38.585312, -89.927466,
                38.585310, -89.927072,
                38.585086, -89.927077,
                38.585104, -89.927471)));
        location.put("HB_WIM", new ArrayList<Double>(Arrays.asList(
                38.585594, -89.927200,
                38.585594, -89.927074,
                38.585462, -89.927067,
                38.585468, -89.927196)));
        location.put("HB_EXIT", new ArrayList<Double>(Arrays.asList(
                38.586001, -89.927308,
                38.585999, -89.926995,
                38.585794, -89.926990,
                38.585800, -89.927301)));
        location.put("HB_ID", new ArrayList<Double>(Arrays.asList(
                -2.0 )));
        stations.put("HOMEBASE", location);

        location = new HashMap<String, ArrayList<Double>>();
        location.put("NH_APPROACH", new ArrayList<Double>(Arrays.asList(
                38.556211,-89.924624,
                38.556253,-89.924324,
                38.555898,-89.924155,
                38.555735,-89.924443)));
        location.put("NH_WIM", new ArrayList<Double>(Arrays.asList(
                38.556434, -89.925729,
                38.556456, -89.925123,
                38.556207, -89.925135,
                38.556196, -89.925675
                )));
        location.put("NH_EXIT", new ArrayList<Double>(Arrays.asList(
                38.556403, -89.926689,
                38.556445, -89.926321,
                38.555600, -89.926340,
                38.555631, -89.926681)));
        location.put("NH_ID", new ArrayList<Double>(Arrays.asList(
                -1.0 )));
        stations.put("NEIGHBORHOOD", location);


        location = new HashMap<String, ArrayList<Double>>();
        location.put("GLEB_APPROACH", new ArrayList<Double>(Arrays.asList(
                42.290679,-84.191846,
                42.291897,-84.185832,
                42.291628,-84.185762,
                42.290445,-84.191663)));
        location.put("GLEB_WIM", new ArrayList<Double>(Arrays.asList(
                42.292472,-84.182469,
                42.292504,-84.182219,
                42.292373,-84.182140,
                42.292294,-84.182420)));
        location.put("GLEB_EXIT", new ArrayList<Double>(Arrays.asList(
                42.293310,-84.171564,
                42.292474,-84.166358,
                42.292127,-84.166394,
                42.292680,-84.171518)));
        location.put("GLEB_ID", new ArrayList<Double>(Arrays.asList(
                1.0 )));
        stations.put("GRASSLAKE_EB", location);


        location = new HashMap<String, ArrayList<Double>>();
        location.put("WF_APPROACH", new ArrayList<Double>(Arrays.asList(
                39.311803, -76.963768,
                39.311567, -76.963950,
                39.316003, -76.974637,
                39.316257, -76.974495)));
        location.put("WF_WIM", new ArrayList<Double>(Arrays.asList(
                39.31716, -76.97729,
                39.317377,-76.977177,
                39.317205,-76.976881,
                39.31704, -76.97696)));
        location.put("WF_EXIT", new ArrayList<Double>(Arrays.asList(
                39.318720, -76.981818,
                39.318507, -76.982238,
                39.320866, -76.990030,
                39.321186, -76.989949)));
        location.put("WF_ID", new ArrayList<Double>(Arrays.asList(
                2.0 )));
        stations.put("WEST_FRIENDSHIP", location);

    }



    public static double Angle2D(double y1, double x1, double y2, double x2) {
        double dtheta, theta1, theta2;

        theta1 = Math.atan2(y1, x1);
        theta2 = Math.atan2(y2, x2);
        dtheta = theta2 - theta1;
        while (dtheta > PI)
            dtheta -= TWOPI;
        while (dtheta < -PI)
            dtheta += TWOPI;

        return (dtheta);
    }


    public String weighStationName_name_coordinate(double latitude,
                                                   double longitude) {

        String gpsLocation = null;
        int i;
        double angle = 0;
        double point1_lat;
        double point1_long;
        double point2_lat;
        double point2_long;

        String stationName = "";

        ArrayList<Double> latt_array = new ArrayList<Double>();
        ArrayList<Double> long_array = new ArrayList<Double>();

        boolean isGpsContained = false;
        for (String weighStationName : stations.keySet()) { // NUMBER OF STATIONS  - 1 KEYS 'OFALLON'

            Map<String, ArrayList<Double>> station = stations.get(weighStationName);

            for (String gate : station.keySet()) {

                ArrayList<Double> gpsValues = station.get(gate);

                latt_array.clear();
                long_array.clear();

                for (int count = 0; count < gpsValues.size(); count++) {

                    if (gate.toUpperCase().contains("ID")) {
                        break;
                    }

                    if (count % 2 == 0) {
                        latt_array.add(gpsValues.get(count));
                    } else {
                        long_array.add(gpsValues.get(count));
                    }
                }

                int n = latt_array.size();
                angle = 0;

                for (i = 0; i < n; i++) {
                    point1_lat = latt_array.get(i) - latitude;
                    point1_long = long_array.get(i) - longitude;
                    point2_lat = latt_array.get((i + 1) % n) - latitude;
                    point2_long = long_array.get((i + 1) % n) - longitude;
                    angle += Angle2D(point1_lat, point1_long, point2_lat, point2_long);
                }

                if (!isGpsContained && Math.abs(angle) > PI) {
                    stationName = weighStationName;
                    isGpsContained = true;
                }

            }

            Log.d("KEY", weighStationName);
//                Log.d("GATES", gate);
//                Log.d("KEY", gpsValues.toString());
            Log.d("LATT", "" + latt_array.toString());
            Log.d("LONG", "" + long_array.toString());

        }

        return stationName;
    }

    public String gate_name_coordinate(double latitude,
                                       double longitude) {
        double angle = 0;
        double point1_lat;

        String gpsLocation = null;
        int i;
        double point1_long;
        double point2_lat;
        double point2_long;

        String gpsGateName = "";

        ArrayList<Double> latt_array = new ArrayList<Double>();
        ArrayList<Double> long_array = new ArrayList<Double>();

        // NUMBER OF STATIONS  - 1 KEYS 'OFALLON'
        boolean isGpsContained = false;
        for (String weighStationName : stations.keySet()) {

            Map<String, ArrayList<Double>> station = stations.get(weighStationName);

            for (String gate : station.keySet()) {

                ArrayList<Double> gpsValues = station.get(gate);

                latt_array.clear();
                long_array.clear();

                // Convert the strings to doubles.
                for (int count = 0; count < gpsValues.size(); count++) {

                    if (gate.toUpperCase().contains("ID")) {
                        break;
                    }

                    if (count % 2 == 0) {
                        latt_array.add(gpsValues.get(count));
                    } else {
                        long_array.add(gpsValues.get(count));
                    }
                }

                int n = latt_array.size();
                angle = 0;

                for (i = 0; i < n; i++) {
                    point1_lat = latt_array.get(i) - latitude;
                    point1_long = long_array.get(i) - longitude;
                    point2_lat = latt_array.get((i + 1) % n) - latitude;
                    // you should have paid more attention in high school geometry.
                    point2_long = long_array.get((i + 1) % n) - longitude;
                    angle += Angle2D(point1_lat, point1_long, point2_lat, point2_long);
                }

                if (!isGpsContained && Math.abs(angle) > PI) {
                    isGpsContained = true;
                    gpsGateName = gate;
                }

            }

            Log.d("KEY", weighStationName);
            Log.d("LATT", "" + latt_array.toString());
            Log.d("LONG", "" + long_array.toString());
        }

        return gpsGateName;
    }


    public String maxGps(Location location) {

        ArrayList<Double> weighStation = new ArrayList<Double>();

        Double bottomLattGpsCoord = 0.0;
        Double topLattGpsCoord = 0.0;
        Double leftLongGpsCoord = 0.0;
        Double rightLongGpsCoord = 0.0;

        boolean isGpsContained = false;
        String stationName = "";

        String[] weighStationNameArr = stations.keySet().toArray(new String[stations.keySet().size()]);

        // check gps location in each max weigh station
        for (String weighStationName : weighStationNameArr) { // NUMBER OF STATIONS  - 1 KEYS 'OFALLON'

            Map<String, ArrayList<Double>> station = stations.get(weighStationName);

            bottomLattGpsCoord = 90.0;
            topLattGpsCoord = -90.0;
            leftLongGpsCoord = 180.0;
            rightLongGpsCoord = -180.0;

            for (String gate : station.keySet()) {

                // (lat +90 North) (lat -90 South) (long -180 West) (long 180 East)
                ArrayList<Double> gpsValues = station.get(gate);

                for (int x = 0; x < gpsValues.size(); x++) {

                    if (gate.toUpperCase().contains("ID")) {
                        break;
                    }

                    if ((x % 2) == 0) {
                        Double lat = gpsValues.get(x);
                        if (lat > topLattGpsCoord) {
                            topLattGpsCoord = lat;
                        }

                        if (lat < bottomLattGpsCoord) {
                            bottomLattGpsCoord = lat;
                        }

                    } else {

                        Double lng = gpsValues.get(x);
                        if (lng > rightLongGpsCoord) {
                            rightLongGpsCoord = lng;
                        }

                        if (lng < leftLongGpsCoord) {
                            leftLongGpsCoord = lng;
                        }

                    }
                }
            }


            ArrayList<Double> latt_array = new ArrayList<Double>();
            ArrayList<Double> long_array = new ArrayList<Double>();

            latt_array.clear();
            latt_array.add(topLattGpsCoord);
            latt_array.add(topLattGpsCoord);
            latt_array.add(bottomLattGpsCoord);
            latt_array.add(bottomLattGpsCoord);

            long_array.clear();
            long_array.add(leftLongGpsCoord);
            long_array.add(rightLongGpsCoord);
            long_array.add(rightLongGpsCoord);
            long_array.add(leftLongGpsCoord);

            double point1_lat;
            double point1_long;
            double point2_lat;
            double point2_long;

            int n = latt_array.size();
            double angle = 0;

            for (int i = 0; i < n; i++) {
                point1_lat = latt_array.get(i) - location.getLatitude();
                point1_long = long_array.get(i) - location.getLongitude();
                point2_lat = latt_array.get((i + 1) % n) - location.getLatitude();
                point2_long = long_array.get((i + 1) % n) - location.getLongitude();
                angle += Angle2D(point1_lat, point1_long, point2_lat, point2_long);
            }

            if (!isGpsContained && Math.abs(angle) > PI) {
                stationName = weighStationName;
                isGpsContained = true;
            }
        }


        return stationName;

    }


    public Integer gateId(Location location) {

        ArrayList<Double> weighStation = new ArrayList<Double>();

        Double bottomLattGpsCoord = 0.0;
        Double topLattGpsCoord = 0.0;
        Double leftLongGpsCoord = 0.0;
        Double rightLongGpsCoord = 0.0;

        boolean isGpsContained = false;
        int  stationId = 0;
        int weighStationId = 0;

        String[] weighStationNameArr = stations.keySet().toArray(new String[stations.keySet().size()]);

        // check gps location in each max weigh station
        for (String weighStationName : weighStationNameArr) { // NUMBER OF STATIONS  - 1 KEYS 'OFALLON'

            Map<String, ArrayList<Double>> station = stations.get(weighStationName);

            bottomLattGpsCoord = 90.0;
            topLattGpsCoord = -90.0;
            leftLongGpsCoord = 180.0;
            rightLongGpsCoord = -180.0;

            for (String gate : station.keySet()) {

                // (lat +90 North) (lat -90 South) (long -180 West) (long 180 East)
                ArrayList<Double> gpsValues = station.get(gate);

                for (int x = 0; x < gpsValues.size(); x++) {

                    if (gate.toUpperCase().contains("ID")) {
                        stationId = gpsValues.get(x).intValue();
                        break;
                    }

                    if ((x % 2) == 0) {
                        Double lat = gpsValues.get(x);
                        if (lat > topLattGpsCoord) {
                            topLattGpsCoord = lat;
                        }

                        if (lat < bottomLattGpsCoord) {
                            bottomLattGpsCoord = lat;
                        }

                    } else {

                        Double lng = gpsValues.get(x);
                        if (lng > rightLongGpsCoord) {
                            rightLongGpsCoord = lng;
                        }

                        if (lng < leftLongGpsCoord) {
                            leftLongGpsCoord = lng;
                        }

                    }
                }
            }


            ArrayList<Double> latt_array = new ArrayList<Double>();
            ArrayList<Double> long_array = new ArrayList<Double>();

            latt_array.clear();
            latt_array.add(topLattGpsCoord);
            latt_array.add(topLattGpsCoord);
            latt_array.add(bottomLattGpsCoord);
            latt_array.add(bottomLattGpsCoord);

            long_array.clear();
            long_array.add(leftLongGpsCoord);
            long_array.add(rightLongGpsCoord);
            long_array.add(rightLongGpsCoord);
            long_array.add(leftLongGpsCoord);

            double point1_lat;
            double point1_long;
            double point2_lat;
            double point2_long;

            int n = latt_array.size();
            double angle = 0;

            for (int i = 0; i < n; i++) {
                point1_lat = latt_array.get(i) - location.getLatitude();
                point1_long = long_array.get(i) - location.getLongitude();
                point2_lat = latt_array.get((i + 1) % n) - location.getLatitude();
                point2_long = long_array.get((i + 1) % n) - location.getLongitude();
                angle += Angle2D(point1_lat, point1_long, point2_lat, point2_long);
            }

            if (!isGpsContained && Math.abs(angle) > PI) {
                weighStationId = stationId;
            }
        }


        return weighStationId;

    }

}