package com.olam.digital.ofis.distributionmodule.distribution_data.repository

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.olam.digital.ofis.DataLayer
import com.olam.digital.ofis.OFISDBContract
import com.olam.digital.ofis.Utilities
import com.olam.digital.ofis.distributionmodule.distribution_data.mapper.cursorToCountryIdMapper
import com.olam.digital.ofis.distributionmodule.distribution_data.mapper.cursorToFarmerIdMapper
import com.olam.digital.ofis.distributionmodule.distribution_data.mapper.cursorToFarmerMapper
import com.olam.digital.ofis.distributionmodule.distribution_data.mapper.cursorToSpinnerDataMapper
import com.olam.digital.ofis.distributionmodule.distribution_data.mapper.toDistributionFormEntity
import com.olam.digital.ofis.distributionmodule.distribution_data.mapper.toStakeHolder
import com.olam.digital.ofis.distributionmodule.distribution_domain.model.DistributionFormDto
import com.olam.digital.ofis.distributionmodule.distribution_domain.model.FarmerAndStakeHolderDto
import com.olam.digital.ofis.distributionmodule.distribution_domain.repository.DistributionModuleRepository
import com.olam.digital.ofis.distributionmodule.distribution_presentation.EntityEnum
import com.olam.digital.ofis.model.SpinnerDataDto
import com.olam.digital.ofis.mvvmmodule.roomdb.OFISRoomDB

class DistributionModuleRepositoryImpl(
    private val ofisDb: SQLiteDatabase,
    private val roomDb : OFISRoomDB
) : DistributionModuleRepository {

    override suspend fun searchFarmerOrStakeHolder(
        query: String,
        farmerGroupId: List<Int>,
        sectionId: List<Int>,
        entityName : EntityEnum,
        roleId : Int
    ): Result<List<FarmerAndStakeHolderDto>> {
        return try {
            val farmerTable = OFISDBContract.FarmerEntry.TABLE_NAME
            val whereConditions = mutableListOf<String>()

            if (query.isNotEmpty()) {
                whereConditions += "(${farmerTable}.${OFISDBContract.FarmerEntry.COLUMN_NAME_FARMER_NAME} LIKE '%$query%' COLLATE NOCASE OR " +
                        "${farmerTable}.${OFISDBContract.FarmerEntry.COLUMN_NAME_OLAM_FARMER_ID} LIKE '%$query%' COLLATE NOCASE)"
            }

            if (sectionId.isNotEmpty()) {
                val sections = sectionId.toString().removePrefix("[").removeSuffix("]")
                whereConditions += "${farmerTable}.${OFISDBContract.FarmerEntry.COLUMN_NAME_SECTION_ID} IN ($sections)"
            }

            if (farmerGroupId.isNotEmpty()) {
                val farmerGroups = farmerGroupId.toString().removePrefix("[").removeSuffix("]")
                whereConditions += "${farmerTable}.${OFISDBContract.FarmerEntry.COLUMN_NAME_FARMERGROUPID} IN ($farmerGroups)"
            }

            val whereClause = if (whereConditions.isNotEmpty()) {
                " WHERE " + whereConditions.joinToString(" AND ")
            } else {
                ""
            }

            val sql = "SELECT DISTINCT * FROM $farmerTable $whereClause ORDER BY ${farmerTable}.${OFISDBContract.FarmerEntry.COLUMN_NAME_FARMER_NAME} ASC"

            val cursor : Cursor = ofisDb.rawQuery(sql, null)

            when (entityName) {
                EntityEnum.FARMER -> {
                    if (cursor.count > 0) {
                        Result.success(cursorToFarmerMapper(cursor))
                    }
                    else {
                        Result.success(emptyList())
                    }
                }
                EntityEnum.STAKEHOLDER -> {
                    Result.success(
                        roomDb.multiStakeHolderDao()
                            .getStakeHolders(query, cursorToFarmerIdMapper(cursor), roleId)
                            .map { it.toStakeHolder() }
                    )
                }
            }
        }
        catch (e : Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun getSpinnerData(): Result<List<List<SpinnerDataDto>>> {
        return try {
            val locale = Utilities.getColumnNameByLanguage()

            val farmerGroupCursor = ofisDb.rawQuery("SELECT id, name FROM ${OFISDBContract.FarmerGroupsEntry.TABLE_NAME}", null)
            val sectionCursor = ofisDb.rawQuery("SELECT id, name FROM ${OFISDBContract.SectionsEntry.TABLE_NAME}", null)
            val roleCursor = ofisDb.rawQuery("SELECT id, $locale as name FROM ${OFISDBContract.MultiFarmRoles.TABLE_NAME} WHERE ${OFISDBContract.MultiFarmRoles.COLUMN_NAME_ID} != 1", null)

            val farmerGroupList = cursorToSpinnerDataMapper(farmerGroupCursor)
            val sectionList = cursorToSpinnerDataMapper(sectionCursor)
            val roleList = cursorToSpinnerDataMapper(roleCursor)

            val distributionGroups = roomDb.distributionGroupDao().getAllDistributionGroups(1)
            distributionGroups.add(0, SpinnerDataDto(0, ""))

            val distributionCategory = roomDb.distributionCategoryDao().getAllDistributionCategory()

            val countryCursor = ofisDb.rawQuery("Select id FROM ${OFISDBContract.CountriesEntry.TABLE_NAME}", null)
            val currencyItems = roomDb.currencyDao().getCurrenciesByCountryId(cursorToCountryIdMapper(countryCursor))
            currencyItems.add(0, SpinnerDataDto(0, "", ""))

            val localUnits = roomDb.localUnitDao().getLocalUnits()
            localUnits.add(0, SpinnerDataDto(0, "", ""))

            Result.success(mutableListOf(farmerGroupList, sectionList, roleList, distributionCategory, distributionGroups, currencyItems, localUnits))
        }
        catch (e : Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun getDistributionGroups(categoryId: Int): Result<List<SpinnerDataDto>> {
        val distributionGroups = roomDb.distributionGroupDao().getAllDistributionGroups(categoryId)
        distributionGroups.add(0,SpinnerDataDto(0, ""))
        return Result.success(distributionGroups)
    }

    override suspend fun getDistributionItems(distributionId: String): Result<List<SpinnerDataDto>> {
        val distributionItems = roomDb.distributionItemDao().getAllDistributionItem(distributionId)
        distributionItems.add(0, SpinnerDataDto(0, ""))
        return Result.success(distributionItems)
    }

    override suspend fun insertNetworkQueueEntry(values: ContentValues): Long {
        return ofisDb.insert(OFISDBContract.NetworkQueueEntry.TABLE_NAME, null, values)

    }

    override suspend fun saveDistributionForm(distributionFormDto: DistributionFormDto) {
        roomDb.distributionFormDao().insertDistributionForm(distributionFormDto.toDistributionFormEntity())
    }

    override suspend fun getDistributionDetails(
        farmerId: String?,
        stakeHolderId: String?
    ): Result<DistributionFormDto> {
        return Result.success(
            roomDb.distributionFormDao().getDistributionFormDetails(farmerId, stakeHolderId)
        )
    }

}