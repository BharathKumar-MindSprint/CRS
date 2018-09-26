package com.olam.digital.ofis.tasks;

import android.content.Context;

import com.google.firebase.crash.FirebaseCrash;
import com.olam.digital.ofis.OFISDBContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by ben on 24/02/16.
 */
@SuppressWarnings("DefaultFileTemplate")
class UpdateTransactionListsTask extends DatabaseTask {

    public UpdateTransactionListsTask(Context context) {
        super(context);
    }

    @Override
    protected String doInBackground(JSONObject... params) {

        try {

            JSONObject transactionResponse = params[0];

            if (transactionResponse.getInt("resultcount") > 0) {

                JSONObject response = transactionResponse.getJSONObject("outputdata");

                // get the various sub lists
                JSONArray currencyArray, directionArray, directionGroupArray, directionTypeArray, transactionItemGroupArray, transactionItemTypeArray,
                        productItemTypeArray, itemTypeUnitArray, unitArray, productGradeArray, productPriceArray, buyingAgentArray, transacteeArray, buyingRuleArray,
                        buyingAgentBuyingRuleArray, countryCurrencyArray, buyingAgentCurrencyArray, transactionBalancesArray;

                try {
                    currencyArray = response.getJSONArray(OFISDBContract.Currency.TABLE_NAME);
                    fillTable(currencyArray, OFISDBContract.Currency.TABLE_NAME, OFISDBContract.Currency.COLUMN_LIST);
                } catch (JSONException e) {
                    FirebaseCrash.report(new Exception("UpdateTransactionListsTask: Error filling currencies table", e));
                    e.printStackTrace();
                }
                try {
                    directionArray = response.getJSONArray(OFISDBContract.Direction.TABLE_NAME);
                    fillTable(directionArray, OFISDBContract.Direction.TABLE_NAME, OFISDBContract.Direction.COLUMN_LIST);
                } catch (JSONException e) {
                    FirebaseCrash.report(new Exception("UpdateTransactionListsTask: Error filling directions table", e));
                    e.printStackTrace();
                }
                try {
                    directionGroupArray = response.getJSONArray(OFISDBContract.DirectionTransactionItemGroup.TABLE_NAME);
                    fillTable(directionGroupArray, OFISDBContract.DirectionTransactionItemGroup.TABLE_NAME, OFISDBContract.DirectionTransactionItemGroup.COLUMN_LIST);
                } catch (JSONException e) {
                    FirebaseCrash.report(new Exception("UpdateTransactionListsTask: Error filling direction_group table", e));
                    e.printStackTrace();
                }
                try {
                    directionTypeArray = response.getJSONArray(OFISDBContract.DirectionTransactionItemType.TABLE_NAME);
                    fillTable(directionTypeArray, OFISDBContract.DirectionTransactionItemType.TABLE_NAME, OFISDBContract.DirectionTransactionItemType.COLUMN_LIST);
                } catch (JSONException e) {
                    FirebaseCrash.report(new Exception("UpdateTransactionListsTask: Error filling direction_type table", e));
                    e.printStackTrace();
                }
                try {
                    transactionItemGroupArray = response.getJSONArray(OFISDBContract.TransactionItemGroup.TABLE_NAME);
                    fillTable(transactionItemGroupArray, OFISDBContract.TransactionItemGroup.TABLE_NAME, OFISDBContract.TransactionItemGroup.COLUMN_LIST);
                } catch (JSONException e) {
                    FirebaseCrash.report(new Exception("UpdateTransactionListsTask: Error filling item group table", e));
                    e.printStackTrace();
                }
                try {
                    transactionItemTypeArray = response.getJSONArray(OFISDBContract.TransactionItemType.TABLE_NAME);
                    fillTable(transactionItemTypeArray, OFISDBContract.TransactionItemType.TABLE_NAME, OFISDBContract.TransactionItemType.COLUMN_LIST);
                } catch (JSONException e) {
                    FirebaseCrash.report(new Exception("UpdateTransactionListsTask: Error filling item type table", e));
                    e.printStackTrace();
                }
                try {
                    productItemTypeArray = response.getJSONArray(OFISDBContract.ProductTransactionItemType.TABLE_NAME);
                    fillTable(productItemTypeArray, OFISDBContract.ProductTransactionItemType.TABLE_NAME, OFISDBContract.ProductTransactionItemType.COLUMN_LIST);
                } catch (JSONException e) {
                    FirebaseCrash.report(new Exception("UpdateTransactionListsTask: Error filling product_itemtype table", e));
                    e.printStackTrace();
                }
                try {
                    itemTypeUnitArray = response.getJSONArray(OFISDBContract.TransactionItemTypeUnit.TABLE_NAME);
                    fillTable(itemTypeUnitArray, OFISDBContract.TransactionItemTypeUnit.TABLE_NAME, OFISDBContract.TransactionItemTypeUnit.COLUMN_LIST);
                } catch (JSONException e) {
                    FirebaseCrash.report(new Exception("UpdateTransactionListsTask: Error filling itemtype_unit table", e));
                    e.printStackTrace();
                }
                try {
                    unitArray = response.getJSONArray(OFISDBContract.Unit.TABLE_NAME);
                    fillTable(unitArray, OFISDBContract.Unit.TABLE_NAME, OFISDBContract.Unit.COLUMN_LIST);
                } catch (JSONException e) {
                    FirebaseCrash.report(new Exception("UpdateTransactionListsTask: Error filling units table", e));
                    e.printStackTrace();
                }
                try {
                    productGradeArray = response.getJSONArray(OFISDBContract.ProductGrade.TABLE_NAME);
                    fillTable(productGradeArray, OFISDBContract.ProductGrade.TABLE_NAME, OFISDBContract.ProductGrade.COLUMN_LIST);
                } catch (JSONException e) {
                    FirebaseCrash.report(new Exception("UpdateTransactionListsTask: Error filling productgrades table", e));
                    e.printStackTrace();
                }
                try {
                    productPriceArray = response.getJSONArray(OFISDBContract.ProductPrice.TABLE_NAME);
                    fillTable(productPriceArray, OFISDBContract.ProductPrice.TABLE_NAME, OFISDBContract.ProductPrice.COLUMN_LIST);
                } catch (JSONException e) {
                    FirebaseCrash.report(new Exception("UpdateTransactionListsTask: Error filling productprices table", e));
                    e.printStackTrace();
                }
                try {
                    buyingAgentArray = response.getJSONArray(OFISDBContract.BuyingAgent.TABLE_NAME);
                    fillTable(buyingAgentArray, OFISDBContract.BuyingAgent.TABLE_NAME, OFISDBContract.BuyingAgent.COLUMN_LIST);
                } catch (JSONException e) {
                    FirebaseCrash.report(new Exception("UpdateTransactionListsTask: Error filling buyingagents table", e));
                    e.printStackTrace();
                }
                try {
                    transacteeArray = response.getJSONArray(OFISDBContract.Transactee.TABLE_NAME);
                    fillTable(transacteeArray, OFISDBContract.Transactee.TABLE_NAME, OFISDBContract.Transactee.COLUMN_LIST);
                } catch (JSONException e) {
                    FirebaseCrash.report(new Exception("UpdateTransactionListsTask: Error filling transactees table", e));
                    e.printStackTrace();
                }
                try {
                    buyingRuleArray = response.getJSONArray(OFISDBContract.BuyingRule.TABLE_NAME);
                    fillTable(buyingRuleArray, OFISDBContract.BuyingRule.TABLE_NAME, OFISDBContract.BuyingRule.COLUMN_LIST);
                } catch (JSONException e) {
                    FirebaseCrash.report(new Exception("UpdateTransactionListsTask: Error filling buyingrules table", e));
                    e.printStackTrace();
                }
                try {
                    buyingAgentBuyingRuleArray = response.getJSONArray(OFISDBContract.BuyingAgentBuyingRule.TABLE_NAME);
                    fillTable(buyingAgentBuyingRuleArray, OFISDBContract.BuyingAgentBuyingRule.TABLE_NAME, OFISDBContract.BuyingAgentBuyingRule.COLUMN_LIST);
                } catch (JSONException e) {
                    FirebaseCrash.report(new Exception("UpdateTransactionListsTask: Error filling buyingagent_buyingrule table", e));
                    e.printStackTrace();
                }
                try {
                    countryCurrencyArray = response.getJSONArray(OFISDBContract.CountryCurrency.TABLE_NAME);
                    fillTable(countryCurrencyArray, OFISDBContract.CountryCurrency.TABLE_NAME, OFISDBContract.CountryCurrency.COLUMN_LIST);
                } catch (JSONException e) {
                    FirebaseCrash.report(new Exception("UpdateTransactionListsTask: Error filling country_currency table", e));
                    e.printStackTrace();
                }
                try {
                    buyingAgentCurrencyArray = response.getJSONArray(OFISDBContract.BuyingAgentCurrency.TABLE_NAME);
                    fillTable(buyingAgentCurrencyArray, OFISDBContract.BuyingAgentCurrency.TABLE_NAME, OFISDBContract.BuyingAgentCurrency.COLUMN_LIST);
                } catch (JSONException e) {
                    FirebaseCrash.report(new Exception("UpdateTransactionListsTask: Error filling buyingagent_currency table", e));
                    e.printStackTrace();
                }
                try {
                    transactionBalancesArray = response.getJSONArray(OFISDBContract.TransactionBalance.TABLE_NAME);
                    fillTable(transactionBalancesArray, OFISDBContract.TransactionBalance.TABLE_NAME, OFISDBContract.TransactionBalance.COLUMN_LIST);
                } catch (JSONException e) {
                    FirebaseCrash.report(new Exception("UpdateTransactionListsTask: Error filling transactionbalances table", e));
                    e.printStackTrace();
                }
            }

        } catch (JSONException e) {
            FirebaseCrash.report(new Exception("UpdateTransactionListsTask: Error getting transaction lists response", e));
            e.printStackTrace();
        }

        return null;
    }

}
