package srimobile.aspen.leidos.com.sri.utils;

import android.location.Location;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Created by walswortht on 5/26/2015.
 */
public class CalculateDistance {
    /*::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
/*::                                                                         :*/
/*::  This routine calculates the distance between two points (given the     :*/
/*::  latitude/longitude of those points). It is being used to calculate     :*/
/*::  the distance between two locations using GeoDataSource (TM) prodducts  :*/
/*::                                                                         :*/
/*::  Definitions:                                                           :*/
/*::    South latitudes are negative, east longitudes are positive           :*/
/*::                                                                         :*/
/*::  Passed to function:                                                    :*/
/*::    lat1, lon1 = Latitude and Longitude of point 1 (in decimal degrees)  :*/
/*::    lat2, lon2 = Latitude and Longitude of point 2 (in decimal degrees)  :*/
/*::    unit = the unit you desire for results                               :*/
/*::           where: 'M' is statute miles (default)                         :*/
/*::                  'K' is kilometers                                      :*/
/*::                  'N' is nautical miles                                  :*/
/*::  Worldwide cities and other features databases with latitude longitude  :*/
/*::  are available at http://www.geodatasource.com                          :*/
/*::                                                                         :*/
/*::  For enquiries, please contact sales@geodatasource.com                  :*/
/*::                                                                         :*/
/*::  Official Web site: http://www.geodatasource.com                        :*/
/*::                                                                         :*/
/*::           GeoDataSource.com (C) All Rights Reserved 2015                :*/
/*::                                                                         :*/
/*::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/

    public static double distance(double lat1, double lon1, double lat2, double lon2, String unit) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        if (unit == "K") {
            dist = dist * 1.609344;
        } else if (unit == "N") {
            dist = dist * 0.8684;
        }
        return (dist);
    }

    public static double distanceLocation(Location start, Location end, String unit) {
        double theta = end.getLongitude() - start.getLongitude();

        double dist = Math.sin(deg2rad(start.getLatitude())) * Math.sin(deg2rad(end.getLatitude())) + Math.cos(deg2rad(start.getLatitude())) * Math.cos(deg2rad(end.getLatitude())) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        if (unit == "K") {
            dist = dist * 1.609344;
        } else if (unit == "N") {
            dist = dist * 0.8684;
        }
        return (dist);
    }

    public static double calculateSpeed(double distance, long beginTime, long stopTime) { // distance in miles

        try {

            BigDecimal rate = new BigDecimal(0.0);
            BigDecimal dist = new BigDecimal(0.0);
            BigDecimal startTime = new BigDecimal(beginTime);
            BigDecimal endTime = new BigDecimal(stopTime);
            if (distance != 0 && beginTime != 0 && stopTime != 0) {
                BigDecimal timeMillis = startTime.subtract(endTime);
                BigDecimal hoursUnit = new BigDecimal(1000 * 60 * 60);

                BigDecimal hours = timeMillis.divide(hoursUnit, 4, RoundingMode.UP);
                dist = new BigDecimal(distance);
                rate = dist.divide(hours, 4, RoundingMode.UP);

                System.out.println("endTime " + endTime);
                System.out.println("startTime " + startTime);
                System.out.println("time " + timeMillis);
                System.out.println("hrs unit " + hoursUnit);
                System.out.println("hrs " + hours);
                System.out.println("dist " + dist);
                System.out.println("rate " + rate);

            }
            return rate.doubleValue();
        }  catch(Exception e) {
                BigDecimal rate = new BigDecimal(20.0);
            }

        return 20;
    }

    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
/*::  This function converts decimal degrees to radians             :*/
/*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
/*::  This function converts radians to decimal degrees             :*/
/*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    private static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }

}
