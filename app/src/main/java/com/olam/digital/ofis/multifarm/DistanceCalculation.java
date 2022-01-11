package com.olam.digital.ofis.multifarm;

import android.content.Context;
import android.location.Location;

import java.util.ArrayList;
import java.util.List;

public class DistanceCalculation {

    Context mContext;
    List<MultiFarmDTO> farmDTOList;
    int distance;


    public DistanceCalculation(Context context, List<MultiFarmDTO> farmDTOList, int distance) {
        this.mContext=context;
        this.farmDTOList=farmDTOList;
        this.distance=distance;
    }

    public ArrayList<MultiFarmDTO> calculate(Location currentLocation){

//        // Progress Dialog
//        ProgressDialog  progress=new ProgressDialog(mContext);
//        progress.setMessage("Searching Farm");
//        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//        progress.setIndeterminate(false);
//        progress.setCancelable(false);
//        progress.show();
//
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        ArrayList<MultiFarmDTO> multiFarmDTOList= new ArrayList<>();
        double currentLat=currentLocation.getLatitude();
        double currentLng=currentLocation.getLongitude();
        for (MultiFarmDTO multiFarmDTO:farmDTOList){
            if (multiFarmDTO.getLng() !=null && multiFarmDTO.getLat() !=null) {
                if (!multiFarmDTO.getLat().equalsIgnoreCase("null") && !multiFarmDTO.getLng().equalsIgnoreCase("null") &&
                        !multiFarmDTO.getLat().equalsIgnoreCase("") && !multiFarmDTO.getLng().equalsIgnoreCase("")) {
                    if (getDistance(currentLat, Double.valueOf(multiFarmDTO.getLat()), currentLng,
                            Double.valueOf(multiFarmDTO.getLng()), 0, 0) <= distance) {
                        multiFarmDTOList.add(multiFarmDTO);
                    }
                }
            }
            }
//        if (progress!=null && progress.isShowing()){
//            progress.dismiss();
//        }
        return multiFarmDTOList;
    }

    public static double getDistance(double lat1, double lat2, double lon1,
                                  double lon2, double el1, double el2) {

        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters
        double height = el1 - el2;

        distance = Math.pow(distance, 2) + Math.pow(height, 2);

        return (Math.sqrt(distance))/1000;
    }

}
