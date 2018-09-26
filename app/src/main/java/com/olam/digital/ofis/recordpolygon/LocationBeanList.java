package com.olam.digital.ofis.recordpolygon;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by M1032987 on 10/24/2017.
 */

public class LocationBeanList {

    List<plot> plot = new ArrayList<>();

    public List<plot> getLocationBean() {
        return plot;
    }

    public void setLocationBean(List<plot> plot) {
        this.plot = plot;
    }

}
