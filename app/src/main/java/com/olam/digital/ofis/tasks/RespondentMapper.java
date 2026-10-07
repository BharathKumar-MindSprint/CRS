package com.olam.digital.ofis.tasks;

import com.olam.digital.crs.model.surveysubmit.Answer;
import com.olam.digital.crs.room.RespondentEntity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;


public final class RespondentMapper {

    private RespondentMapper() {}

    public static JSONObject toJson(RespondentEntity s) {
        JSONObject root = new JSONObject();
        try {
            // Top-level fields
            root.put("name", nullToEmpty(s.getName()));
            root.put("countryId", s.getCountryId());
            root.put("regionId", s.getRegionId()); // see helper below
            root.put("profileId", s.getProfileId());
            root.put("productId", s.getProductId());
            root.put("districtId", s.getDistrictId());
            root.put("placeId", s.getPlaceId());
            root.put("emailId", nullToEmpty(s.getEmailId()));
            root.put("countryCode", nullToEmpty(s.getCountryCode()));
            root.put("phoneNumber", nullToEmpty(s.getPhoneNumber()));
            root.put("photo", nullToEmpty(s.getPhoto()));
            root.put("gender", nullToEmpty(s.getGender()));
            root.put("dateOfBirth", nullToEmpty(s.getDateOfBirth()));

        } catch (Exception e) {
            // Prefer logging to Crashlytics/Timber as per your project
            e.printStackTrace();
        }
        return root;
    }

    private static JSONArray answersToJson(List<Answer> answers) {
        JSONArray arr = new JSONArray();
        if (answers == null) return arr;

        for (Answer a : answers) {
            JSONObject o = new JSONObject();
            try {
                o.put("question_id", a.getQuestionId());
                o.put("reporting_year", a.getReportingYear());
                o.put("olam_farmer_id", nullToEmpty(a.getOlamFarmerId()));
                o.put("action", a.getAction() != null ? a.getAction().name() : "CREATE");
                o.put("original_id", a.getOriginalId());
                o.put("is_subset", a.isSubset());
                // selectedOptions (nullable)
                arr.put(arr.length(), o);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return arr;
    }

    /** If your Submission date fields are Strings (ISO), just return that. */
    private static String toIsoOrValue(Object maybeString) {
        if (maybeString == null) return "";
        // If already a String (recommended), return as-is:
        if (maybeString instanceof String) return (String) maybeString;

        // If you accidentally pass kotlinx.datetime.LocalDateTime or java.time.LocalDateTime here,
        // calling toString() will output ISO-8601. This avoids `{ "value": {} }`.
        return String.valueOf(maybeString);
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}