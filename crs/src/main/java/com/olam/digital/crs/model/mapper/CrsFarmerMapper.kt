package com.olam.digital.crs.model.mapper

import com.olam.digital.crs.room.FarmerEntity
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

// Import CRS module types
// if you want to reuse column constants
object CrsFarmerMapper {



        const val TABLE_NAME = "farmers"

        // Newly added parameters
        const val COLUMN_NAME_FARMERNAME = "farmerName"
        const val COLUMN_NAME_PLACEID = "placeId"
        const val COLUMN_NAME_SECTIONID = "sectionId"
        const val COLUMN_NAME_COUNTRYID = "countryId"
        const val COLUMN_NAME_DISTRICTID = "districtId"
        const val COLUMN_NAME_SUBDISTRICTID = "subdistrictId"
        const val COLUMN_NAME_SUBDISID = "subdistrict_id"
        const val COLUMN_NAME_FARMERGROUPID = "farmerGroupId"
        const val COLUMN_NAME_REGIONID = "regionId"
        const val COLUMN_NAME_CONSENTFARMER = "consentFarmer"
        const val COLUMN_NAME_CONSENTOTHERS = "consentOthers"
        const val COLUMN_NAME_DATE_OF_BIRTH = "dateOfBirth"
        const val COLUMN_NAME_PRODUCT_ID = "productId"

        const val BACKUP_TABLE_NAME = "farmers_backup"

        const val COLUMN_NAME_ID = "id"
        const val COLUMN_NAME_FARM_ID = "farm_id"
        const val COLUMN_NAME_PLACE_ID = "place_id"
        const val COLUMN_NAME_SECTION_ID = "section_id"
        const val COLUMN_NAME_COUNTRY_ID = "country_id"

        const val COLUMN_NAME_FARMER_NAME = "name"
        const val COLUMN_NAME_GENDER = "gender"
        const val COLUMN_NAME_DOB = "dob"
        const val COLUMN_NAME_OLAM_FARMER_ID = "olam_farmer_id"
        const val COLUMN_NAME_PHOTO = "photo"
        const val COLUMN_NAME_SIGNATURE = "signature"
        const val COLUMN_NAME_SYNCED = "synced"
        const val COLUMN_NAME_CREATED_LOCALLY = "created_on_this_phone"
        const val COLUMN_NAME_WEBSITE_FARMER_ID = "website_farmer_id"
        const val COLUMN_NAME_WEBSITE_SUBMITTED_MODULES = "submitted_modules"
        const val COLUMN_NAME_FARM_PRIMARY_LAT = "primary_lat"
        const val COLUMN_NAME_FARM_PRIMARY_LNG = "primary_lng"
        const val COLUMN_NAME_FARM_PLOTS = "location"
        const val COLUMN_NAME_CONSENT_FARMER = "consent_farmer"
        const val COLUMN_NAME_CONSENT_OTHERS = "consent_others"

        const val COLUMN_NAME_REGISTER_DATE = "registeredDate"
        const val COLUMN_NAME_MODERATION_STATUS = "nidmoderation"

        // OFHG-2 Household lat/long
        const val COLUMN_NAME_FARM_HOUSEHOLD_LAT = "householdLat"
        const val COLUMN_NAME_FARM_HOUSEHOLD_LNG = "householdLng"
        const val COLUMN_NAME_FARM_HOUSEHOLD_ID = "householdId"

        const val COLUMN_NAME_ISCONSENTAVAILABLE = "isConsentAvailable"

        const val COLUMN_NAME_FARMERLEAD = "farmerLead"
        const val COLUMN_NAME_FARMERLEADNAME = "farmerLeadName"
        const val COLUMN_NAME_ODFARMERID = "odFarmerId"
        const val COLUMN_NAME_PARTNER_ID = "partner_Id"
        const val COLUMN_NAME_PROGRAMME_ID = "programme_Id"

        const val COLUMN_NAME_PLOT = "plot"
        const val COLUMN_NAME_CERTIFICATION_ID = "certificationIds"
        const val COLUMN_NAME_CERTIFICATION_TEXT = "certificationText"
        const val COLUMN_NAME_AGE = "age"

        const val COLUMN_NAME_COMPLAINCE_FLAG = "complainceFlag"
        const val COLUMN_NAME_CONDUCT_SURVEY_FOR_NCF = "conductSurveyForNonCompliant"
        const val COLUMN_NAME_CONDUCT_TRAINING_FOR_NCF = "conductTrainingForNonCompliant"
        const val COLUMN_NAME_POLYGON_CAPTURE_FOR_NCF = "polygonCaptureForNonCompliant"

        const val COLUMN_NAME_FARM_CODE = "farmCode"
        const val COLUMN_NAME_FARM_NAME = "farmName"
        const val COLUMN_NAME_FARMER_CODE = "farmerCode"
        const val COLUMN_NAME_DUPLICATE_FARMER_CODE = "duplicateFarmerCode"

        const val COLUMN_NAME_PLOT_AVAILABLE = "plotAvailable"

        const val COLUMN_NAME_IS_NO_NATIONAL_ID = "isNoNationalId"
        const val COLUMN_NAME_IS_NO_PHONE_NUMBER = "isNoPhoneNumber"

    const val  COLUMN_NAME_OLAM_FARMER_ID_DISPLAY = "displayFarmerId";


    private fun optS(o: JSONObject, key: String): String {
        // Returns "" if key is missing, is JSONObject.NULL, or is nullish.
        return if (o.has(key) && !o.isNull(key)) o.optString(key, "") else ""
    }


    private fun optI(o: JSONObject, key: String?): Int? {
        return if (o.has(key) && !o.isNull(key)) o.optInt(key) else null
    }

    private fun optD(o: JSONObject, key: String?): Double? {
        return if (o.has(key) && !o.isNull(key)) o.optDouble(key) else null
    }
    @JvmStatic
    @Throws(JSONException::class)
    fun mapJsonArrayToEntities(array: JSONArray): MutableList<FarmerEntity?> {
        val list: MutableList<FarmerEntity?> = ArrayList<FarmerEntity?>()
        for (i in 0..<array.length()) {
            val o = array.getJSONObject(i)

            // REQUIRED: id (your schema treats id as PK String)
            val id = optS(o, COLUMN_NAME_ID)
            if (id == null || id.isEmpty()) {
                // skip or generate a fallback; here we skip invalid row
                continue
            }

            val entity = FarmerEntity( /* id                 */
                id,  /* farmId             */
                optS(o, COLUMN_NAME_FARM_ID),  /* placeId            */
                optS(o, COLUMN_NAME_PLACE_ID),  /* sectionId          */
                optS(o, COLUMN_NAME_SECTION_ID),  /* countryId          */
                optS(o, COLUMN_NAME_COUNTRY_ID),  /* name               */

                optS(o, COLUMN_NAME_FARMER_NAME),  /* gender             */
                optS(o, COLUMN_NAME_GENDER),  /* dob                */
                optS(o, COLUMN_NAME_DOB),  /* olamFarmerId       */

                optS(o, COLUMN_NAME_OLAM_FARMER_ID),  /* photo              */
                optS(o, COLUMN_NAME_PHOTO),  /* signature          */
                optS(o, COLUMN_NAME_SIGNATURE),  /* synced             */
                optI(o, COLUMN_NAME_SYNCED),  /* createdLocally     */
                optI(o, COLUMN_NAME_CREATED_LOCALLY),  /* websiteFarmerId    */

                optS(o, COLUMN_NAME_WEBSITE_FARMER_ID),  /* submittedModules   */
                optS(o, COLUMN_NAME_WEBSITE_SUBMITTED_MODULES),  /* primaryLat         */

                optD(o, COLUMN_NAME_FARM_PRIMARY_LAT),  /* primaryLng         */
                optD(o, COLUMN_NAME_FARM_PRIMARY_LNG),  /* location           */
                optS(o, COLUMN_NAME_FARM_PLOTS),  /* consentFarmer      */

                optI(o, COLUMN_NAME_CONSENT_FARMER),  /* consentOthers      */
                optI(o, COLUMN_NAME_CONSENT_OTHERS),  /* districtId         */

                optS(o, COLUMN_NAME_DISTRICTID),  /* subdistrictId      */
                optS(o, COLUMN_NAME_SUBDISTRICTID),  /* farmerGroupId      */
                optS(o, COLUMN_NAME_FARMERGROUPID),  /* regionId           */
                optS(o, COLUMN_NAME_REGIONID),  /* productId          */

                optS(o, COLUMN_NAME_PRODUCT_ID),  /* registeredDate     */

                optS(o, COLUMN_NAME_REGISTER_DATE),  /* moderationStatus   */
                optS(o, COLUMN_NAME_MODERATION_STATUS),  /* farmerLead         */

                optS(o, COLUMN_NAME_FARMERLEAD),  /* farmerLeadName     */
                optS(o, COLUMN_NAME_FARMERLEADNAME),  /* odFarmerId         */

                optS(o, COLUMN_NAME_ODFARMERID),  /* plot               */

                optS(o, COLUMN_NAME_PLOT),  /* partnerId          */
                optS(o, COLUMN_NAME_PARTNER_ID),  /* programmeId        */
                optS(o, COLUMN_NAME_PROGRAMME_ID),  /* certificationIds   */

                optS(o, COLUMN_NAME_CERTIFICATION_ID),  /* certificationText  */
                optS(o, COLUMN_NAME_CERTIFICATION_TEXT),  /* age                */

                optI(o, COLUMN_NAME_AGE),  /* complainceFlag     */

                optS(o, COLUMN_NAME_COMPLAINCE_FLAG),  /* conductSurveyNCF   */
                optS(o, COLUMN_NAME_CONDUCT_SURVEY_FOR_NCF),  /* conductTrainingNCF */
                optS(o, COLUMN_NAME_CONDUCT_TRAINING_FOR_NCF),  /* polygonCaptureNCF  */
                optS(o, COLUMN_NAME_POLYGON_CAPTURE_FOR_NCF),  /* farmCode           */

                optS(o, COLUMN_NAME_FARM_CODE),  /* farmName           */
                optS(o, COLUMN_NAME_FARM_NAME),  /* farmerCode         */
                optS(o, COLUMN_NAME_FARMER_CODE),  /* duplicateFarmerCode*/
                optS(o, COLUMN_NAME_DUPLICATE_FARMER_CODE),  /* plotAvailable      */

                optS(o, COLUMN_NAME_PLOT_AVAILABLE),  /* isNoNationalId     */

                optI(o, COLUMN_NAME_IS_NO_NATIONAL_ID),  /* isNoPhoneNumber    */
                optI(o, COLUMN_NAME_IS_NO_PHONE_NUMBER),  /* householdLat       */

                optD(o, COLUMN_NAME_FARM_HOUSEHOLD_LAT),  /* householdLng       */
                optD(o, COLUMN_NAME_FARM_HOUSEHOLD_LNG),  /* householdId        */
                optS(o, COLUMN_NAME_FARM_HOUSEHOLD_ID),  /* isConsentAvailable */

                optI(o, COLUMN_NAME_ISCONSENTAVAILABLE),  /* displayFarmerId    */

                optS(o, COLUMN_NAME_OLAM_FARMER_ID_DISPLAY)
            )

            list.add(entity)
        }
        return list
    }
}
