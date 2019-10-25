package com.olam.digital.ofis.childlabour.businesslogic;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.olam.digital.ofis.childlabour.dto.VillageDTO;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.nio.channels.GatheringByteChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.*;

public class SipLogicTest {

    List<VillageDTO> villageDbList=new ArrayList<VillageDTO>();

    @Before
    public void setUp() throws Exception {
        villageDbList.addAll(setVillageList());
    }

    @After
    public void tearDown() throws Exception {
        villageDbList.clear();
    }

    @Test
    public void getAllPlaces() {

        List<VillageDTO> villageDTOList=getVillageList();
        assertEquals(villageDbList.size(), villageDTOList.size());
    }

    private List<VillageDTO> setVillageList() {

        List<VillageDTO> list=new ArrayList<VillageDTO>();
        VillageDTO villageDTO=new VillageDTO();
        VillageDTO villageDTO1=new VillageDTO();

        villageDTO.setId("1");
        villageDTO.setVillageName("TOUMODI1");

        villageDTO1.setId("2");
        villageDTO1.setVillageName("BONON");

        list.add(villageDTO);
        list.add(villageDTO1);

        return list;

    }

    private List<VillageDTO> getVillageList() {

        List<VillageDTO> list=new ArrayList<VillageDTO>();
        VillageDTO villageDTO=new VillageDTO();
        VillageDTO villageDTO1=new VillageDTO();

        villageDTO.setId("1");
        villageDTO.setVillageName("TOUMODI1");

        villageDTO1.setId("2");
        villageDTO1.setVillageName("BONON");

        list.add(villageDTO);
        list.add(villageDTO1);

        return list;

    }
}