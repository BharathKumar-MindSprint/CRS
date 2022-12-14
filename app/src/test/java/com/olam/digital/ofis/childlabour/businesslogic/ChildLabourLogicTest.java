//package com.olam.digital.ofis.childlabour.businesslogic;
//
//import com.google.gson.Gson;
//import com.olam.digital.ofis.OFISDBContract;
//import com.olam.digital.ofis.Utilities;
//import com.olam.digital.ofis.childlabour.ChildLabourUtils;
//import com.olam.digital.ofis.childlabour.dto.ChildModuleDto;
//import com.olam.digital.ofis.childlabour.dto.ChildrenDTO;
//import com.olam.digital.ofis.model.FarmerRegistrationDTO;
//
//import junit.framework.Assert;
//
//import org.json.JSONArray;
//import org.json.JSONObject;
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import static junit.framework.Assert.assertEquals;
//import static junit.framework.Assert.assertNotNull;
//import static org.junit.Assert.assertNull;
//import static org.junit.Assert.assertTrue;
//
//public class ChildLabourLogicTest {
//
//    List<ChildrenDTO> childrenList=new ArrayList<ChildrenDTO>();
//    List<FarmerRegistrationDTO> farmerList=new ArrayList<FarmerRegistrationDTO>();
//
//
//    @Before
//    public void setUp() throws Exception {
//        childrenList.add(getChildrenDto());
//        farmerList.add(getFarmerDto());
//    }
//
//    @After
//    public void tearDown() throws Exception {
//        childrenList.clear();
//        farmerList.clear();
//    }
//
//
//
//
//
//    @Test
//    public void deleteChildVisitDetails() {
//    }
//
//
//
//    @Test
//    public void getChildrenDTO() {
//        Assert.assertNotNull(getChildrenDto());
//    }
//
//
//    @Test
//    public void getAllChildSubmittedModulesDetails() {
//
//        JSONObject submittedModules ;
//        JSONArray submittedModulesArray;
//        List<String> surveyCodeList=new ArrayList<String>();
//        try {
//            String submittedModulesJSON = getChildrenDto().getSubmittedModules();
//            submittedModules=new JSONObject(submittedModulesJSON.toString());
//            submittedModulesArray= submittedModules.getJSONArray("modules");
//            for (int i = 0; i < submittedModulesArray.length(); i++) {
//                JSONObject submittedModule = submittedModulesArray.getJSONObject(i);
//
//                if (submittedModule.has("surveyCode")) {
//                    String surveyCode=submittedModule.getString("surveyCode");
//                    if (surveyCode!=null && !surveyCode.equalsIgnoreCase("") )
//                        surveyCodeList.add(surveyCode);
//                }
//            }
//
//        }catch (Exception e){
//        }
//
//        assertEquals(2, surveyCodeList.size());
//
//    }
//
//    @Test
//    public void getChildDetailsById() {
//        String childId=getChildrenDto().getId();
//        assertEquals("1", childId);
//    }
//
//    @Test
//    public void insertVisitDetails() {
//
//    }
//
//    @Test
//    public void deleteVisitDetails() {
//
//    }
//
//    @Test
//    public void insertChildDetails() {
//         childrenList.add(getChildrenDto());
//         assertEquals(2,childrenList.size());
//
//    }
//
//    @Test
//    public void updateChildDetails() {
//    }
//
//    @Test
//    public void getChildSurveyModuleList() {
//        List<ChildModuleDto> childModuleDtoList=new ArrayList<ChildModuleDto>();
//        List<ChildModuleDto> childModuleDataBaseList=getChildModuleDtoList();
//        for (ChildModuleDto childModuleDto : childModuleDataBaseList) {
//            String surveyCode=childModuleDto.getSurveyCode();
//            if ((surveyCode.equalsIgnoreCase(ChildLabourUtils.HOUSEHOLD_SURVEY_CODE) || surveyCode.equalsIgnoreCase(ChildLabourUtils.INSPECTION_SURVEY_CODE) || surveyCode.equalsIgnoreCase(ChildLabourUtils.FOLLOWUP_SURVEY_CODE))) {
//                        childModuleDtoList.add(childModuleDto);
//                    }
//            }
//        assertEquals(childModuleDataBaseList.size(), childModuleDtoList.size());
//    }
//
//    @Test
//    public void getFarmerName() {
//        FarmerRegistrationDTO farmerRegistrationDTO=getFarmerDto();
//        assertNotNull(farmerRegistrationDTO.getFarmerName());
//        assertEquals("Test Farmer",farmerRegistrationDTO.getFarmerName());
//    }
//
//
//    private static ChildrenDTO getChildrenDto(){
//       ChildrenDTO childrenDTO = new ChildrenDTO();
//        childrenDTO.setId("1");
//        childrenDTO.setName("Test child");
//        childrenDTO.setGender("Male");
//        childrenDTO.setAge("12");
//        childrenDTO.setStatus("active");
//        childrenDTO.setSynced("1");
//        childrenDTO.setCreatedAt("");
//        childrenDTO.setSubmittedModules("{\"id\":0,\"childrenId\":0,\"surveyYear\":0,\"moduleId\":0,\"appId\":0,\"reportedData\":null,\"submittedDate\":null,\"productId\":0,\"countryId\":0,\"uploadDate\":null,\"farmerId\":0,\"childAvailability\":0,\"answers\":null,\"modules\":[{\"id\":78,\"surveyCode\":\"01\",\"created_at\":null},{\"id\":79,\"surveyCode\":\"02\",\"created_at\":null}]}");
//        return childrenDTO;
//    }
//
//    private ChildrenDTO getChildrenDto1(){
//        ChildrenDTO childrenDTO = new ChildrenDTO();
//        childrenDTO.setId("2");
//        childrenDTO.setName("Test child 1");
//        childrenDTO.setGender("Male");
//        childrenDTO.setAge("12");
//        childrenDTO.setStatus("active");
//        childrenDTO.setSynced("1");
//        childrenDTO.setCreatedAt("");
//        childrenDTO.setSubmittedModules("{\"id\":0,\"childrenId\":0,\"surveyYear\":0,\"moduleId\":0,\"appId\":0,\"reportedData\":null,\"submittedDate\":null,\"productId\":0,\"countryId\":0,\"uploadDate\":null,\"farmerId\":0,\"childAvailability\":0,\"answers\":null,\"modules\":[{\"id\":78,\"surveyCode\":\"01\",\"created_at\":null},{\"id\":79,\"surveyCode\":\"02\",\"created_at\":null}]}");
//        return childrenDTO;
//    }
//
//    private static FarmerRegistrationDTO getFarmerDto(){
//        FarmerRegistrationDTO farmerRegistrationDTO = new FarmerRegistrationDTO();
//        farmerRegistrationDTO.setActive(true);
//        farmerRegistrationDTO.setAppId(1);
//        farmerRegistrationDTO.setConsentFarmer(true);
//        farmerRegistrationDTO.setConsentOthers(true);
//        farmerRegistrationDTO.setActive(true);
//        farmerRegistrationDTO.setCountryId(1L);
//        farmerRegistrationDTO.setRegionId(1L);
//        farmerRegistrationDTO.setDistrictId(1L);
//        farmerRegistrationDTO.setPlaceId(1L);
//        farmerRegistrationDTO.setFarmerGroupId(1L);
//        farmerRegistrationDTO.setSectionId(1L);
//        farmerRegistrationDTO.setFarmerName("Test Farmer");
//        return  farmerRegistrationDTO;
//    }
//
//
//    private static List<ChildModuleDto> getChildModuleDtoList(){
//        List<ChildModuleDto> childModuleDtoList=new ArrayList<ChildModuleDto>();
//        ChildModuleDto childModuleHS;
//        ChildModuleDto childModuleIS;
//        String childModuleHosehold="{\"_id\":\"86\",\"name\":\"CLMRS Household Survey\",\"surveyCode\":\"01\"}";
//        String childModuleInspection="{\"_id\":\"87\",\"name\":\"CLMRS Inspection Survey\",\"surveyCode\":\"02\"}";
//        childModuleHS=new Gson().fromJson(childModuleHosehold,ChildModuleDto.class);
//        childModuleIS=new Gson().fromJson(childModuleInspection,ChildModuleDto.class);
//        childModuleDtoList.add(childModuleHS);
//        childModuleDtoList.add(childModuleIS);
//        return childModuleDtoList;
//    }
//}
