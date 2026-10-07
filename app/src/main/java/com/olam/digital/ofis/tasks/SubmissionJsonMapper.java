package com.olam.digital.ofis.tasks;

import com.olam.digital.crs.model.surveysubmit.Answer;
import com.olam.digital.crs.model.surveysubmit.SelectedOption;
import com.olam.digital.crs.model.surveysubmit.Submission;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.List;

public final class SubmissionJsonMapper {

    private SubmissionJsonMapper() {}

    public static JSONObject toJson(Submission s) {
        JSONObject root = new JSONObject();
        try {
            // Top-level fields
            root.put("module_id", s.getModuleId());
            root.put("stakeholder_id", s.getStakeholderId());
            root.put("submission_date", toIsoOrValue(s.getSubmissionDate())); // see helper below
            root.put("upload_date", toIsoOrValue(s.getUploadDate()));
            root.put("olam_enumerator_id", s.getOlamEnumeratorId());
            root.put("imei_number", nullToEmpty(s.getImeiNumber()));
            root.put("fmPreview", s.getFmPreview());
            root.put("farmer_id", s.getFarmerId());
            root.put("farm_id", s.getFarmId());
            root.put("recorded_lat", s.getRecordedLat());
            root.put("recorded_lng", s.getRecordedLng());
            root.put("upload_attempts", s.getUploadAttempts());
            root.put("upload_id", nullToEmpty(s.getUploadId()));
            root.put("communityRespondentId", s.getCommunityRespondentId());
            root.put("questions_included", nullToEmpty(s.getQuestionsIncluded()));

            // answers array
            root.put("answers", answersToJson(s.getAnswers()));

            root.put("server_item_id", nullToEmpty(s.getServerItemId()));
            root.put("is_web_edit", s.isWebEdit());

            if (s.getSubmittedModuleId() != null) {
                root.put("submitted_module_id", s.getSubmittedModuleId());
            } else {
                root.put("submitted_module_id", JSONObject.NULL);
            }

            root.put("last_edit_user_id", s.getLastEditUserId());
            root.put("id", s.getId());
            root.put("country_id", s.getCountryId());
            root.put("farmer_group_id", s.getFarmerGroupId());
            root.put("product_id", s.getProductId());
            root.put("is_review_edit", s.isReviewEdit());
            root.put("isUploadJob", s.isUploadJob());
            root.put("app_version", nullToEmpty(s.getAppVersion()));
            root.put("updateReason", nullToEmpty(s.getUpdateReason()));
            root.put("recurrenceFrequency", s.getRecurrenceFrequency());
            root.put("recurrenceType", s.getRecurrenceType() != null ? s.getRecurrenceType().name() : "Monthly");
            root.put("surveyDuration", s.getSurveyDuration());
            root.put("surveyStartDate", toIsoOrValue(s.getSurveyStartDate()));

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
                o.put("repeaterItemGroup",a.getRepeaterItemGroup());
                if (a.getSubsetSection() != null) {
                    o.put("subset_section", a.getSubsetSection());
                } else {
                    o.put("subset_section", JSONObject.NULL);
                }
                if (a.getAnswer() != null) {
                    o.put("answer", a.getAnswer());
                } else {
                    o.put("answer", JSONObject.NULL);
                }
                o.put("globalMetricId", a.getGlobalMetricId());
                o.put("localMetricId", a.getLocalMetricId());

                // selectedOptions (nullable)
                if (a.getSelectedOptions() != null) {
                    JSONArray so = new JSONArray();
                    for (SelectedOption s : a.getSelectedOptions()) {
                        JSONObject soObj = new JSONObject();
                        soObj.put("optionId", s.getOptionId());
                        soObj.put("questionsId", s.getQuestionsId());
                        soObj.put("optionValue", nullToEmpty(s.getOptionValue()));
                        so.put(so.length(), soObj);
                    }
                    o.put("selectedOptions", so);
                } else {
                    o.put("selectedOptions", JSONObject.NULL);
                }

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