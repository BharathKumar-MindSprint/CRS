package com.webpoint0.ofiscompanion;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.crash.FirebaseCrash;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Hashtable;
import java.util.UUID;

public class TransactionItem extends BaseActivity {

    private Context context;
    private Resources res;
    private long buyingAgentId, transacteeId, transactionItemId, transactionBalanceId = 0;
    private boolean editMode, readOnlyMode, fullyLoaded = false, backtoFinder = false, is_giving_direction = false, is_receiving_direction = false, is_right_hand_ba = false, can_mobilemoney = false, can_pay = false, can_loan = false, can_gift = false, can_repay = false, is_mobileMoneyMode = false;
    private int selectedDirection = 0, selectedItemTypeGroup = 0, selectedItemType = 0, selectedUnit = 0, selectedProduct = 0, selectedproductGrade = 0, selectedCurrency = 0, selectedMmOrCash = -1;
    protected String signalStrengthValue;
    private String columnSuffix, transacteeType, olamFarmerId, currencySymbol, transacteeNameValue, unitsText;
    private Spinner directionSpinner, itemTypeGroupSpinner, itemTypeSpinner, productGradeSpinner, currencySpinner, unitsSpinner, mmCashSpinner;
    private EditText quantity, pricePerUnit, totalPrice, amountMobileMoney, amountPaid, amountLoaned, amountGifted, transactionNotes, phoneNumber, amountMobileMoneyCurrency, amountPaidCurrency, amountLoanedCurrency, amountGiftedCurrency, totalPriceCurrency;
    private LinearLayout productGradeLayout, quantityLayout, mmWrapper, paidWrapper, loanedWrapper, giftedWrapper, layout_total_price, layout_currency, cannotTransact, layout_amounts, layout_mm_or_cash, recipientPhoneWrapper;
    private ImageView transacteePhoto;
    private ImageButton refreshButton, saveButton;
    private Button completeButton;
    private RelativeLayout layout_price, transactionBalanceWrapper;
    private TextView priceLabel, mmLabel, paidLabel, giftedLabel, loanedLabel, buyingAgentName, transacteeName, transactionBalance, transactionBalancePrefix, transactionBalanceSuffix, cannotTransactMessage, transactionitemGroupLabel, amountLoanedMessage, diallingCode;
    private JSONObject itemGroupDisplayConfig, itemTypeDisplayConfig;
    private Cursor availableCurrencies;
    private Double transactionBalanceValue = 0.00;

    private DataLayer dl;
    private NetworkLayer nl;
    private Handler mainHandler;

    private final static String BUYING_AGENT_CONTEXT = "BUYING_AGENT_CONTEXT";
    private final static String TRANSACTEE_ID = "TRANSACTEE_ID";
    private final static String TRANSACTEE_TYPE = "TRANSACTEE_TYPE";
    private final static String EDIT_TRANSACTION_ITEM_ID = "EDIT_TRANSACTION_ITEM_ID";
    private final static String EDIT_MODE = "EDIT_MODE";
    private final static String READ_ONLY_MODE = "READ_ONLY_MODE";
    private final static String BACK_TO_FINDER = "BACK_TO_FINDER";

    private final NumberFormat precision = NumberFormat.getInstance();
    private final char GROUPING_SEPARATOR = DecimalFormatSymbols.getInstance().getGroupingSeparator();

    private ProgressDialog m_ProgressDialog = null;
    private signalStrengthListener ssListener;
    private TelephonyManager telephonyManager;
    private CountDownTimer countDownTimer;
    private UUID upload_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_item);

        //noinspection ConstantConditions
        getActionBar().setDisplayHomeAsUpEnabled(true);

        context = this;
        res = getResources();

        Intent intent = getIntent();
        buyingAgentId = intent.getLongExtra(BUYING_AGENT_CONTEXT, 0);
        transacteeId = intent.getLongExtra(TRANSACTEE_ID, 0);
        transacteeType = intent.getStringExtra(TRANSACTEE_TYPE);
        transactionItemId = intent.getLongExtra(EDIT_TRANSACTION_ITEM_ID, 0);
        editMode = intent.getBooleanExtra(EDIT_MODE, false);
        readOnlyMode = intent.getBooleanExtra(READ_ONLY_MODE, false);
        backtoFinder = intent.getBooleanExtra(BACK_TO_FINDER, false);

        dl = new DataLayer(res, context);
        nl = new NetworkLayer(context, res);
        mainHandler = new Handler(Looper.getMainLooper());

        transacteePhoto = (ImageView) findViewById(R.id.transacteePhoto);
        buyingAgentName = (TextView) findViewById(R.id.buyngAgentName);
        transacteeName = (TextView) findViewById(R.id.transacteeName);
        transactionBalanceWrapper = (RelativeLayout) findViewById(R.id.transactionBalanceWrapper);
        transactionBalancePrefix = (TextView) findViewById(R.id.transactionBalancePrefix);
        transactionBalanceSuffix = (TextView) findViewById(R.id.transactionBalanceSuffix);
        transactionBalance = (TextView) findViewById(R.id.transactionBalance);
        directionSpinner = (Spinner) this.findViewById(R.id.spinner_item_direction);
        itemTypeGroupSpinner = (Spinner) this.findViewById(R.id.spinner_item_group);
        itemTypeSpinner = (Spinner) this.findViewById(R.id.spinner_item_type);
        productGradeSpinner = (Spinner) this.findViewById(R.id.spinner_product_grade);
        currencySpinner = (Spinner) this.findViewById(R.id.spinner_currency);
        unitsSpinner = (Spinner) this.findViewById(R.id.spinner_units);
        mmCashSpinner = (Spinner) this.findViewById(R.id.spinner_mm_or_cash);
        productGradeLayout = (LinearLayout) findViewById(R.id.layout_product_grade);
        quantityLayout = (LinearLayout) findViewById(R.id.layout_quantity_units);
        quantity = (EditText) findViewById(R.id.transactionQuantity);
        pricePerUnit = (EditText) findViewById(R.id.transactionPricePerUnit);
        totalPrice = (EditText) findViewById(R.id.transactionTotal);
        totalPriceCurrency = (EditText) findViewById(R.id.totalPriceCurrency);
        amountPaid = (EditText) findViewById(R.id.amountPaid);
        paidWrapper = (LinearLayout) findViewById(R.id.paidWrapper);
        amountLoaned = (EditText) findViewById(R.id.amountLoaned);
        loanedWrapper = (LinearLayout) findViewById(R.id.loanedWrapper);
        amountGifted = (EditText) findViewById(R.id.amountGifted);
        giftedWrapper = (LinearLayout) findViewById(R.id.giftedWrapper);
        amountMobileMoney = (EditText) findViewById(R.id.amountMobileMoney);
        mmWrapper = (LinearLayout) findViewById(R.id.mmWrapper);
        priceLabel = (TextView) findViewById(R.id.priceLabel);
        paidLabel = (TextView) findViewById(R.id.amountPaidLabel);
        amountPaidCurrency = (EditText) findViewById(R.id.amountPaidCurrency);
        loanedLabel = (TextView) findViewById(R.id.amountLoanedLabel);
        amountLoanedCurrency = (EditText) findViewById(R.id.amountLoanedCurrency);
        mmLabel = (TextView) findViewById(R.id.amountMmLabel);
        amountMobileMoneyCurrency = (EditText) findViewById(R.id.amountMobileMoneyCurrency);
        amountLoanedMessage = (TextView) findViewById(R.id.amountLoanedMessage);
        giftedLabel = (TextView) findViewById(R.id.amountGiftedLabel);
        amountGiftedCurrency = (EditText) findViewById(R.id.amountGiftedCurrency);
        layout_total_price = (LinearLayout) findViewById(R.id.layout_total_price);
        layout_currency = (LinearLayout) findViewById(R.id.layout_currency);
        layout_price = (RelativeLayout) findViewById(R.id.layout_price);
        layout_amounts = (LinearLayout) findViewById(R.id.amounts);
        layout_mm_or_cash = (LinearLayout) findViewById(R.id.layout_mm_or_cash);
        refreshButton = (ImageButton) findViewById(R.id.refreshBalance);
        cannotTransact = (LinearLayout) findViewById(R.id.cannotTransact);
        saveButton = (ImageButton) findViewById(R.id.saveTransactionProgress);
        completeButton = (Button) findViewById(R.id.completeTransaction);
        cannotTransactMessage = (TextView) findViewById(R.id.cannotTransactMessage);
        transactionitemGroupLabel = (TextView) findViewById(R.id.transactionitemGroupLabel);
        transactionNotes = (EditText) findViewById(R.id.transactionNotes);
        recipientPhoneWrapper = (LinearLayout) findViewById(R.id.recipientPhoneWrapper);
        diallingCode = (TextView) findViewById(R.id.recipientPhoneDiallingCode);
        phoneNumber = (EditText) findViewById(R.id.recipientPhoneNumber);

        unitsText = res.getString(R.string.price_per_unit_default);
        priceLabel.append(" " + unitsText);

        columnSuffix = Utilities.getLocaleSuffix(res);
        if (columnSuffix.equalsIgnoreCase("_en")) {
            columnSuffix = "";
        }

        if (editMode && transactionItemId > 0) {
            getExistingValues();
        }

        bindTransactorsData();
        bindCurrencies();
        bindSpinners();
        bindEditText();
        bindPaging();
        bindSaveButton();
        bindRefreshButton();
        bindSignalStrengthRecorder();
    }

    private double getNumericValue(String textValue) {
        try {
            return precision.parse(textValue.replace(String.valueOf(GROUPING_SEPARATOR),"")).doubleValue();
        } catch (ParseException e) {
            return 0;
        }
    }

    private void getExistingValues() {
        Cursor transactionItem = dl.getTransactionItemById(transactionItemId);

        if (transactionItem.moveToFirst()) {
            selectedDirection = transactionItem.getInt(transactionItem.getColumnIndex(OFISDBContract.TransactionItem.COLUMN_NAME_DIRECTION_ID));
            selectedItemTypeGroup = transactionItem.getInt(transactionItem.getColumnIndex(OFISDBContract.TransactionItem.COLUMN_NAME_TRANSACTIONITEMGROUP_ID));
            selectedItemType = transactionItem.getInt(transactionItem.getColumnIndex(OFISDBContract.TransactionItem.COLUMN_NAME_TRANSACTIONITEMTYPE_ID));
            selectedUnit = transactionItem.getInt(transactionItem.getColumnIndex(OFISDBContract.TransactionItem.COLUMN_NAME_UNIT_ID));
            selectedProduct = transactionItem.getInt(transactionItem.getColumnIndex(OFISDBContract.TransactionItem.COLUMN_NAME_PRODUCT_ID));
            selectedproductGrade = transactionItem.getInt(transactionItem.getColumnIndex(OFISDBContract.TransactionItem.COLUMN_NAME_PRODUCT_GRADE_ID));
            selectedCurrency = transactionItem.getInt(transactionItem.getColumnIndex(OFISDBContract.TransactionItem.COLUMN_NAME_CURRENCY_ID));
            selectedMmOrCash = transactionItem.getInt(transactionItem.getColumnIndex(OFISDBContract.TransactionItem.COLUMN_NAME_MOBILEMONEYCASH_ID));
            buyingAgentId = transactionItem.getLong(transactionItem.getColumnIndex(OFISDBContract.TransactionItem.COLUMN_NAME_BUYINGAGENT_ID));
            transacteeId = transactionItem.getLong(transactionItem.getColumnIndex(OFISDBContract.TransactionItem.COLUMN_NAME_TRANSACTEE_ID));
            transacteeType = transactionItem.getString(transactionItem.getColumnIndex(OFISDBContract.TransactionItem.COLUMN_NAME_TRANSACTEE_TYPE));
            transactionBalanceId = transactionItem.getLong(transactionItem.getColumnIndex(OFISDBContract.TransactionItem.COLUMN_NAME_TRANSACTION_BALANCE_ID));
            String upload_id_string = transactionItem.getString(transactionItem.getColumnIndex(OFISDBContract.TransactionItem.COLUMN_NAME_UPLOAD_ID));
            if (upload_id_string != null) {
                upload_id = UUID.fromString(upload_id_string);
            }

            quantity.setText(precision.format(transactionItem.getDouble(transactionItem.getColumnIndex(OFISDBContract.TransactionItem.COLUMN_NAME_QUANTITY))));
            pricePerUnit.setText(precision.format(transactionItem.getDouble(transactionItem.getColumnIndex(OFISDBContract.TransactionItem.COLUMN_NAME_PRICE_PER_UNIT))));
            totalPrice.setText(precision.format(transactionItem.getDouble(transactionItem.getColumnIndex(OFISDBContract.TransactionItem.COLUMN_NAME_TOTAL_PRICE))));
            amountMobileMoney.setText(precision.format(transactionItem.getDouble(transactionItem.getColumnIndex(OFISDBContract.TransactionItem.COLUMN_NAME_AMOUNT_MOBILEMONEY))));
            amountPaid.setText(precision.format(transactionItem.getDouble(transactionItem.getColumnIndex(OFISDBContract.TransactionItem.COLUMN_NAME_AMOUNT_PAID))));
            amountLoaned.setText(precision.format(transactionItem.getDouble(transactionItem.getColumnIndex(OFISDBContract.TransactionItem.COLUMN_NAME_AMOUNT_LOANED))));
            amountGifted.setText(precision.format(transactionItem.getDouble(transactionItem.getColumnIndex(OFISDBContract.TransactionItem.COLUMN_NAME_AMOUNT_GIFTED))));
            transactionNotes.setText(transactionItem.getString(transactionItem.getColumnIndex(OFISDBContract.TransactionItem.COLUMN_NAME_TRANSACTION_NOTES)));
            String loanedLabelText = transactionItem.getString(transactionItem.getColumnIndex(OFISDBContract.TransactionItem.COLUMN_NAME_AMOUNT_LOANED_TEXT));
            if (loanedLabelText.length() > 0) {
                loanedLabel.setText(loanedLabelText);
            }
            diallingCode.setText(transactionItem.getString(transactionItem.getColumnIndex(OFISDBContract.TransactionItem.COLUMN_NAME_TRANSACTION_MMPP_RECIPIENT_DIALLINGCODE)));
            phoneNumber.setText(transactionItem.getString(transactionItem.getColumnIndex(OFISDBContract.TransactionItem.COLUMN_NAME_TRANSACTION_MMPP_RECIPIENT_NUMBER)));

            if (selectedMmOrCash == 1) {
                is_mobileMoneyMode = true;
            }

            if (readOnlyMode) {
                quantity.setEnabled(false);
                pricePerUnit.setEnabled(false);
                totalPrice.setEnabled(false);
                amountMobileMoney.setEnabled(false);
                amountPaid.setEnabled(false);
                amountLoaned.setEnabled(false);
                amountGifted.setEnabled(false);
                transactionNotes.setEnabled(false);
                phoneNumber.setEnabled(false);
                refreshButton.setVisibility(View.GONE);
            }

            initBalance();
        }
    }

    private void bindTransactorsData() {
        Cursor buyingAgent = dl.getBuyingAgentById(buyingAgentId);
        if (buyingAgent.getCount() > 0) {
            buyingAgent.moveToFirst();
            buyingAgentName.setText(buyingAgent.getString(buyingAgent.getColumnIndex(OFISDBContract.BuyingAgent.COLUMN_NAME_NAME)));
        }

        Cursor transactee = dl.getTransacteeByIdAndType(transacteeId, transacteeType);
        if (transactee.moveToFirst()) {
            if (transacteeType.equalsIgnoreCase("agent")) {
                transacteePhoto.setBackgroundColor(ContextCompat.getColor(context, R.color.ofis_actionbar_background));
                transacteePhoto.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_buying_agent));
                transacteeNameValue = transactee.getString(transactee.getColumnIndex(OFISDBContract.Transactee.COLUMN_NAME_NAME));
                transacteeName.setText(transacteeNameValue);
                olamFarmerId = res.getString(R.string.buyingagent);
            } else {
                transacteePhoto.setBackgroundColor(Color.TRANSPARENT);
                String farmerPhotoPath = transactee.getString(transactee.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_PHOTO));
                if (farmerPhotoPath != null && farmerPhotoPath.length() > 0) {
                    // set our desired width and height for display
                    int baseWidth = 61;
                    int baseHeight = 54;

                    Hashtable dimensions = Utilities.getDimensionsForDisplayFromBase(baseWidth, baseHeight, res);
                    Integer outputWidth = Integer.parseInt(dimensions.get("width").toString());
                    Integer outputHeight = Integer.parseInt(dimensions.get("height").toString());

                    Bitmap existingBitmap = Utilities.loadSizedBitmap(farmerPhotoPath, outputWidth, outputHeight);

                    transacteePhoto.setImageBitmap(existingBitmap);
                }
                transacteeNameValue = transactee.getString(transactee.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_FARMER_NAME));
                transacteeName.setText(transacteeNameValue);
                olamFarmerId = transactee.getString(transactee.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_OLAM_FARMER_ID));

                if (!editMode || transactionItemId == 0) {
                    Cursor existingPhoneNumber = dl.getFarmerPhoneNumbers(transacteeId, true);
                    if (existingPhoneNumber.moveToFirst()) {
                        diallingCode.setText(existingPhoneNumber.getString(existingPhoneNumber.getColumnIndex(OFISDBContract.CountriesEntry.COLUMN_NAME_DIALLING_CODE)));
                        phoneNumber.setText(existingPhoneNumber.getString(existingPhoneNumber.getColumnIndex(OFISDBContract.PhoneNumber.COLUMN_NAME_NUMBER)));
                    } else {
                        Cursor farmerDiallingCode = dl.getDiallingCodeByPlaceId(transactee.getInt(transactee.getColumnIndex(OFISDBContract.FarmerEntry.COLUMN_NAME_PLACE_ID)));
                        if (farmerDiallingCode.moveToFirst()) {
                            diallingCode.setText(farmerDiallingCode.getString(farmerDiallingCode.getColumnIndex(OFISDBContract.CountriesEntry.COLUMN_NAME_DIALLING_CODE)));
                        }
                    }
                }
            }
        }
    }

    private void bindCurrencies() {
        availableCurrencies = dl.getTransactionCurrencies(buyingAgentId, transacteeId, transacteeType);
    }

    private void initBalance() {
        Cursor balance = dl.getTransactionBalance(buyingAgentId, transacteeId, transacteeType, selectedCurrency);
        if (balance.moveToFirst()) {
            transactionBalanceId = balance.getLong(balance.getColumnIndex(OFISDBContract.TransactionBalance.COLUMN_NAME_ID));
            currencySymbol = balance.getString(balance.getColumnIndex(OFISDBContract.Currency.COLUMN_NAME_SYMBOL));
            transactionBalanceValue = balance.getDouble(balance.getColumnIndex(OFISDBContract.TransactionBalance.COLUMN_NAME_BALANCE));
            long rightHandId = balance.getLong(balance.getColumnIndex(OFISDBContract.TransactionBalance.COLUMN_NAME_BUYINGAGENT2_ID));
            if (buyingAgentId == rightHandId) {
                is_right_hand_ba = true;
                transactionBalanceValue = transactionBalanceValue * -1;
            } else {
                is_right_hand_ba = false;
            }
            if (transactionBalanceValue == 0) {
                transactionBalancePrefix.setVisibility(View.GONE);
                transactionBalanceSuffix.setVisibility(View.VISIBLE);
            } else if (transactionBalanceValue < 0) {
                transactionBalancePrefix.setVisibility(View.VISIBLE);
                transactionBalanceSuffix.setVisibility(View.GONE);
            } else {
                transactionBalancePrefix.setVisibility(View.GONE);
                transactionBalanceSuffix.setVisibility(View.VISIBLE);
            }

            if (balance.getInt(balance.getColumnIndex(OFISDBContract.TransactionBalance.COLUMN_NAME_OUT_OF_SYNC)) == 1) {
                refreshButton.setVisibility(View.GONE);
            } else {
                refreshButton.setVisibility(View.VISIBLE);
            }
        } else {
            transactionBalanceId = 0;
            transactionBalanceValue = 0.00;
            refreshButton.setVisibility(View.VISIBLE);
        }
        String outputBalance = precision.format(Math.abs(transactionBalanceValue));
        transactionBalance.setText(currencySymbol + " " + outputBalance);
        amountMobileMoneyCurrency.setText(currencySymbol);
        amountPaidCurrency.setText(currencySymbol);
        amountLoanedCurrency.setText(currencySymbol);
        amountGiftedCurrency.setText(currencySymbol);
        totalPriceCurrency.setText(currencySymbol);
        setAmountsText();
    }

    private void balanceResponse(final Message msg) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                transactionBalanceValue = getNumericValue(msg.obj.toString());
                dl.updateTransactionBalance(transactionBalanceId, transactionBalanceValue.toString(), buyingAgentId, transacteeId, transacteeType, currencySpinner.getSelectedItemId(), is_receiving_direction, is_right_hand_ba, true, false);
                initBalance();
                setAmountsText();
            }
        });
    }

    private void hideBalance() {
        transactionBalanceWrapper.setVisibility(View.GONE);
    }

    private void bindRefreshButton() {
        final Animation rotation = AnimationUtils.loadAnimation(context, R.anim.rotate_refresh);

        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                if (nl.isPhoneOnline()) {

                    rotation.setRepeatCount(Animation.INFINITE);
                    view.startAnimation(rotation);

                    Handler syncHandler = new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            if (msg.what == 1) {
                                balanceResponse(msg);
                            }
                            rotation.setRepeatCount(0);
                        }
                    };
                    //nl.syncTransactionBalance(buyingAgentId, transacteeId, transacteeType, currencySpinner.getSelectedItemId(), syncHandler);
                }
            }
        });

        transactionBalanceWrapper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (refreshButton.getVisibility() == View.VISIBLE) {
                    refreshButton.callOnClick();
                }
            }
        });
    }

    private void bindSpinners() {
        Cursor itemTypeGroups = dl.getItemTypeGroups(OFISDBContract.TransactionItemGroup.COLUMN_NAME_NAME + columnSuffix, buyingAgentId, transacteeId, transacteeType);
        dl.bindSimpleDataDrivenSpinner(itemTypeGroupSpinner, itemTypeGroups, context, OFISDBContract.TransactionItemGroup.COLUMN_NAME_NAME + columnSuffix);

        boolean has_input = false, has_finance = false, has_crop = false;

        while (itemTypeGroups.moveToNext()) {
            try {
                itemGroupDisplayConfig = new JSONObject(itemTypeGroups.getString(itemTypeGroups.getColumnIndex(OFISDBContract.TransactionItemGroup.COLUMN_NAME_DISPLAY_CONFIGURATION)));
                if (!has_input) {
                    has_input = itemGroupDisplayConfig.optBoolean("inputgroup", false);
                }
                if (!has_finance) {
                    has_finance = itemGroupDisplayConfig.optBoolean("financegroup", false);
                }
                if (!has_crop) {
                    has_crop = itemGroupDisplayConfig.optBoolean("cropgroup", false);
                }
            } catch (JSONException e) {
                FirebaseCrash.report(new Exception("Transaction: Error in item group display config for group label", e));
                e.printStackTrace();
            }
        }

        if ((has_input && !has_finance && !has_crop) || (!has_input && has_finance && !has_crop) || (!has_input && !has_finance && has_crop)) {
            transactionitemGroupLabel.setVisibility(View.GONE);
        } else {
            transactionitemGroupLabel.setVisibility(View.VISIBLE);
            if (has_input && has_finance && has_crop) {
                transactionitemGroupLabel.setText(R.string.input_crop_finance);
            } else if (has_input && has_finance && !has_crop) {
                transactionitemGroupLabel.setText(R.string.input_finance);
            } else if (has_input && !has_finance && has_crop) {
                transactionitemGroupLabel.setText(R.string.input_crop);
            } else if (!has_input && has_finance && has_crop) {
                transactionitemGroupLabel.setText(R.string.finance_crop);
            }
        }

        itemTypeGroupSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                Cursor itemTypes = dl.getItemTypesByGroupId(OFISDBContract.TransactionItemType.COLUMN_NAME_NAME + columnSuffix, buyingAgentId, transacteeId, transacteeType, id);
                dl.bindSimpleDataDrivenSpinner(itemTypeSpinner, itemTypes, context, null);

                if (itemTypes.getCount() > 0) {
                    itemTypeSpinner.setEnabled(true);
                }

                final Cursor selectedItemGroup = (Cursor) itemTypeGroupSpinner.getSelectedItem();
                if (selectedItemGroup.moveToPosition(position)) {
                    try {
                        itemGroupDisplayConfig = new JSONObject(selectedItemGroup.getString(selectedItemGroup.getColumnIndex(OFISDBContract.TransactionItemGroup.COLUMN_NAME_DISPLAY_CONFIGURATION)));
                        if (itemGroupDisplayConfig.optBoolean("financegroup", false)) {
                            layout_price.setVisibility(View.GONE);
                            layout_total_price.setVisibility(View.GONE);
                            layout_currency.getLayoutParams().width = LinearLayout.LayoutParams.MATCH_PARENT;
                            if (!readOnlyMode) {
                                amountGifted.setEnabled(true);
                            }
                        } else {
                            layout_price.setVisibility(View.VISIBLE);
                            layout_total_price.setVisibility(View.VISIBLE);
                            layout_currency.getLayoutParams().width = 0;
                            amountGifted.setEnabled(false);
                        }
                    } catch (JSONException e) {
                        FirebaseCrash.report(new Exception("Transaction: Error in item group display config", e));
                        e.printStackTrace();
                    }
                }

                resetAmounts();

                itemTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, final int position, long id) {

                        resetAmounts();

                        Cursor directions = dl.getDirectionsByItemGroupAndType(OFISDBContract.Direction.COLUMN_NAME_NAME + columnSuffix, buyingAgentId, transacteeId, transacteeType, itemTypeGroupSpinner.getSelectedItemId(), id);
                        dl.bindDirectionDataDrivenSpinner(directionSpinner, directions, context, OFISDBContract.Direction.COLUMN_NAME_NAME + columnSuffix);

                        directionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                                final Cursor selectedDirection = (Cursor) directionSpinner.getSelectedItem();
                                if (selectedDirection.moveToPosition(position)) {
                                    is_giving_direction = selectedDirection.getString(selectedDirection.getColumnIndex(OFISDBContract.Direction.COLUMN_NAME_IS_GIVING_DIRECTION)).equalsIgnoreCase("1");
                                    is_receiving_direction = selectedDirection.getString(selectedDirection.getColumnIndex(OFISDBContract.Direction.COLUMN_NAME_IS_RECEIVING_DIRECTION)).equalsIgnoreCase("1");
                                }

                                resetAmounts();
                                checkProductPrices();
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {

                            }
                        });


                        if (selectedDirection != 0) {
                            dl.setSpinnerSelectionById(selectedDirection, directionSpinner);
                            if (directions.getCount() == 1) {
                                dl.setSpinnerSelectionById(selectedDirection, directionSpinner);
                                directionSpinner.setEnabled(false);
                            }
                        } else {
                            if (directions.getCount() == 1) {
                                directionSpinner.setSelection(1, false);
                                directionSpinner.setEnabled(false);
                            }
                        }

                        if (readOnlyMode) {
                            directionSpinner.setEnabled(false);
                        }

                        Cursor units = dl.getUnitsByTransactionitemtypeId(id);
                        dl.bindSimpleDataDrivenSpinner(unitsSpinner, units, context, OFISDBContract.Unit.COLUMN_NAME_NAME);

                        unitsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                Cursor selectedUnit = (Cursor) unitsSpinner.getSelectedItem();
                                if (selectedUnit.moveToPosition(position)) {
                                    unitsText = selectedUnit.getString(selectedUnit.getColumnIndex(OFISDBContract.Unit.COLUMN_NAME_NAME));
                                    priceLabel.setText(res.getString(R.string.price_per_unit) + " " + unitsText);
                                }

                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> adapterView) {

                            }
                        });

                        if (selectedUnit != 0) {
                            dl.setSpinnerSelectionById(selectedUnit, unitsSpinner);
                            if (units.getCount() == 1) {
                                unitsSpinner.setEnabled(false);
                            }
                        } else {
                            if (units.getCount() == 1) {
                                unitsSpinner.setSelection(1, false);
                                unitsSpinner.setEnabled(false);
                            }
                        }

                        if (readOnlyMode) {
                            unitsSpinner.setEnabled(false);
                        }

                        Cursor selectedItem = (Cursor) itemTypeSpinner.getSelectedItem();
                        if (selectedItem.moveToPosition(position)) {
                            try {
                                itemTypeDisplayConfig = new JSONObject(selectedItem.getString(selectedItem.getColumnIndex(OFISDBContract.TransactionItemType.COLUMN_NAME_DISPLAY_CONFIGURATION)));
                                if (itemTypeDisplayConfig.optBoolean("product", false)) {
                                    selectedProduct = dl.getProductIdByTransactionItemTypeId(id);
                                    productGradeLayout.setVisibility(View.VISIBLE);

                                    Cursor productGrades = dl.getSimpleSpinnerData(OFISDBContract.ProductGrade.TABLE_NAME, true, OFISDBContract.ProductGrade.COLUMN_NAME_PRODUCT_ID, selectedProduct, true, OFISDBContract.ProductGrade.COLUMN_NAME_NAME + columnSuffix, OFISDBContract.ProductGrade.COLUMN_NAME_NAME + columnSuffix);
                                    dl.bindSimpleDataDrivenSpinner(productGradeSpinner, productGrades, context, null);

                                    if (selectedproductGrade != 0) {
                                        dl.setSpinnerSelectionById(selectedproductGrade, productGradeSpinner);
                                        if (productGrades.getCount() == 1) {
                                            productGradeSpinner.setEnabled(false);
                                        }
                                    } else {
                                        if (productGrades.getCount() == 1) {
                                            productGradeSpinner.setSelection(1, false);
                                            productGradeSpinner.setEnabled(false);
                                        }
                                    }

                                    if (readOnlyMode) {
                                        productGradeSpinner.setEnabled(false);
                                    }

                                } else {
                                    productGradeLayout.setVisibility(View.GONE);
                                    selectedProduct = 0;
                                }
                                if (itemTypeDisplayConfig.optBoolean("hidequantity", false)) {
                                    quantityLayout.setVisibility(View.GONE);
                                    quantity.setText("1");
                                } else {
                                    quantityLayout.setVisibility(View.VISIBLE);
                                }
                            } catch (JSONException e) {
                                FirebaseCrash.report(new Exception("Transaction: Error in item type display config", e));
                                e.printStackTrace();
                            }
                        }

                        checkProductPrices();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        productGradeLayout.setVisibility(View.GONE);
                        selectedProduct = 0;
                        quantityLayout.setVisibility(View.GONE);
                    }
                });

                if (selectedItemType != 0) {
                    dl.setSpinnerSelectionById(selectedItemType, itemTypeSpinner);
                    if (itemTypes.getCount() == 1) {
                        dl.setSpinnerSelectionById(selectedItemType, itemTypeSpinner);
                        itemTypeSpinner.setEnabled(false);
                    }
                } else {
                    if (itemTypes.getCount() == 1) {
                        itemTypeSpinner.setSelection(1, false);
                        itemTypeSpinner.setEnabled(false);
                    }
                }

                if (readOnlyMode) {
                    itemTypeSpinner.setEnabled(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                layout_price.setVisibility(View.VISIBLE);
                layout_total_price.setVisibility(View.VISIBLE);
                layout_currency.getLayoutParams().width = 0;
            }
        });

        if (selectedItemTypeGroup != 0) {
            dl.setSpinnerSelectionById(selectedItemTypeGroup, itemTypeGroupSpinner);
            if (itemTypeGroups.getCount() == 1) {
                itemTypeGroupSpinner.setEnabled(false);
            }
        } else {
            if (itemTypeGroups.getCount() == 1) {
                itemTypeGroupSpinner.setSelection(1, false);
                itemTypeGroupSpinner.setEnabled(false);
            }
        }

        if (readOnlyMode) {
            itemTypeGroupSpinner.setEnabled(false);
        }

        dl.bindSimpleDataDrivenSpinner(currencySpinner, availableCurrencies, context, OFISDBContract.Currency.COLUMN_NAME_CODE);

        currencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (selectedCurrency != (int) id) {
                    selectedCurrency = (int) id;

                    Cursor selectedCurrencyItem = (Cursor) currencySpinner.getSelectedItem();
                    if (selectedCurrencyItem.moveToPosition(position)) {
                        currencySymbol = selectedCurrencyItem.getString(selectedCurrencyItem.getColumnIndex(OFISDBContract.Currency.COLUMN_NAME_SYMBOL));
                    }

                    checkProductPrices();
                    initBalance();
                } else {
                    if (!fullyLoaded) {
                        Cursor selectedCurrencyItem = (Cursor) currencySpinner.getSelectedItem();
                        if (selectedCurrencyItem.moveToPosition(position)) {
                            if (selectedCurrencyItem.getColumnIndex(OFISDBContract.Currency.COLUMN_NAME_SYMBOL) != -1) {
                                currencySymbol = selectedCurrencyItem.getString(selectedCurrencyItem.getColumnIndex(OFISDBContract.Currency.COLUMN_NAME_SYMBOL));
                                initBalance();
                            }
                        }
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                hideBalance();
            }
        });

        if (selectedCurrency != 0) {
            dl.setSpinnerSelectionById(selectedCurrency, currencySpinner);
            if (availableCurrencies.getCount() == 1) {
                currencySpinner.setEnabled(false);
            }
        } else {
            if (availableCurrencies.getCount() == 1) {
                currencySpinner.setSelection(1, false);
                currencySpinner.setEnabled(false);
            }
        }

        if (readOnlyMode) {
            currencySpinner.setEnabled(false);
            Cursor selectedCurrencyItem = (Cursor) currencySpinner.getSelectedItem();
            currencySymbol = selectedCurrencyItem.getString(selectedCurrencyItem.getColumnIndex(OFISDBContract.Currency.COLUMN_NAME_SYMBOL));
            initBalance();
        }

        ArrayAdapter adapter = new ArrayAdapter<>(context, R.layout.spinner_item, res.getStringArray(R.array.mmCash));
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        mmCashSpinner.setAdapter(adapter);

        mmCashSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long id) {
                if (id == 0) {
                    mmWrapper.setVisibility(View.GONE);
                    recipientPhoneWrapper.setVisibility(View.GONE);
                    paidWrapper.setVisibility(View.VISIBLE);
                    is_mobileMoneyMode = false;
                } else if (id == 1) {
                    mmWrapper.setVisibility(View.VISIBLE);
                    recipientPhoneWrapper.setVisibility(View.VISIBLE);
                    paidWrapper.setVisibility(View.GONE);
                    is_mobileMoneyMode = true;
                }

                if (fullyLoaded && selectedMmOrCash != (int) id) {
                    selectedMmOrCash = (int) id;

                    if (id == 0) {
                        if (fullyLoaded && mmWrapper.getVisibility() == View.GONE && paidWrapper.getVisibility() == View.VISIBLE && loanedWrapper.getVisibility() == View.GONE && giftedWrapper.getVisibility() == View.GONE) {
                            if (totalPrice.getText().length() > 0) {
                                amountPaid.setText(totalPrice.getText());
                            } else {
                                if (amountMobileMoney.getText().length() > 0) {
                                    amountPaid.setText(amountMobileMoney.getText());
                                }
                            }
                        } else {
                            if (fullyLoaded && amountMobileMoney.getText().length() > 0) {
                                amountPaid.setText(amountMobileMoney.getText());
                            }
                        }
                        amountMobileMoney.setText("0");
                    } else if (id == 1) {
                        if (fullyLoaded && mmWrapper.getVisibility() == View.VISIBLE && paidWrapper.getVisibility() == View.GONE && loanedWrapper.getVisibility() == View.GONE && giftedWrapper.getVisibility() == View.GONE) {
                            if (totalPrice.getText().length() > 0) {
                                amountMobileMoney.setText(totalPrice.getText());
                            } else {
                                if (amountPaid.getText().length() > 0) {
                                    amountMobileMoney.setText(amountPaid.getText());
                                }
                            }
                        } else {
                            if (fullyLoaded && amountPaid.getText().length() > 0) {
                                amountMobileMoney.setText(amountPaid.getText());
                            }
                        }
                        amountPaid.setText("0");
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        if (readOnlyMode) {
            mmCashSpinner.setEnabled(false);
        }

        new CountDownTimer(1000, 100) {

            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                if (selectedItemTypeGroup != 0) {
                    dl.setSpinnerSelectionById(selectedItemTypeGroup, itemTypeGroupSpinner);
                }
                if (selectedItemType != 0) {
                    dl.setSpinnerSelectionById(selectedItemType, itemTypeSpinner);
                }
                if (selectedDirection != 0) {
                    dl.setSpinnerSelectionById(selectedDirection, directionSpinner);
                }
                if (selectedCurrency != 0) {
                    dl.setSpinnerSelectionById(selectedCurrency, currencySpinner);
                }
                if (selectedUnit != 0) {
                    dl.setSpinnerSelectionById(selectedUnit, unitsSpinner);
                }
                fullyLoaded = true;
                if (selectedMmOrCash != -1) {
                    mmCashSpinner.setSelection(selectedMmOrCash);
                    if (selectedMmOrCash == 0) {
                        is_mobileMoneyMode = false;
                    } else {
                        is_mobileMoneyMode = true;
                    }
                }
                if (nl.isPhoneOnline()) {
                    layout_mm_or_cash.setVisibility(View.VISIBLE);
                } else {
                    is_mobileMoneyMode = false;
                    layout_mm_or_cash.setVisibility(View.GONE);
                    selectedMmOrCash = 0;
                    mmCashSpinner.setSelection(selectedMmOrCash);
                }
            }
        }.start();

        if (readOnlyMode) {
            amountMobileMoney.setEnabled(false);
            amountPaid.setEnabled(false);
            amountLoaned.setEnabled(false);
            amountGifted.setEnabled(false);
        }

    }

    private void setAmountsText() {
        Double totalPriceValue = 0.00;

        if (totalPrice.getText().length() > 0) {
            totalPriceValue = getNumericValue(totalPrice.getText().toString());
        }

        // get Group and direction
        if (is_receiving_direction) {
            // paid
            paidLabel.setText(res.getString(R.string.amount_paid) + " " + transacteeNameValue);
            mmLabel.setText(res.getString(R.string.amount_paid) + " " + transacteeNameValue + " " + res.getString(R.string.mobile_money_suffix));
            // gifted
            giftedLabel.setText(res.getString(R.string.amount_gifted_by) + " " + transacteeNameValue);

            if (!readOnlyMode) {
                if (transactionBalanceValue <= 0) {
                    // loaned
                    loanedLabel.setText(res.getString(R.string.amount_loaned_by) + " " + transacteeNameValue);
                } else if (totalPriceValue > Math.abs(transactionBalanceValue) && can_loan) {
                    // repay and loan
                    loanedLabel.setText(res.getString(R.string.amount_repaid_and_loaned_by) + " " + transacteeNameValue);
                } else {
                    // repay
                    loanedLabel.setText(res.getString(R.string.amount_repaid_by) + " " + transacteeNameValue);
                    amountLoanedMessage.setText(res.getString(R.string.cannotRepayMoreThanTheyOwe));
                }
            }

        } else {
            // paid
            paidLabel.setText(res.getString(R.string.amount_paid_by) + " " + transacteeNameValue);
            mmLabel.setText(res.getString(R.string.amount_paid_by) + " " + transacteeNameValue+ " " + res.getString(R.string.mobile_money_suffix));
            // gifted
            giftedLabel.setText(res.getString(R.string.amount_gifted) + " " + transacteeNameValue);
            // loaned
            if (!readOnlyMode) {
                if (transactionBalanceValue >= 0) {
                    // loaned
                    loanedLabel.setText(res.getString(R.string.amount_loaned) + " " + transacteeNameValue);
                } else if (totalPriceValue > Math.abs(transactionBalanceValue) && can_loan) {
                    // repay and loan
                    loanedLabel.setText(res.getString(R.string.amount_repaid_and_loaned) + " " + transacteeNameValue);
                } else {
                    // repay
                    loanedLabel.setText(res.getString(R.string.amount_repaid) + " " + transacteeNameValue);
                    amountLoanedMessage.setText(res.getString(R.string.cannotRepayMoreThanOwe));
                }
            }
        }

        if (!readOnlyMode) {

            cannotTransact.setVisibility(View.GONE);
            layout_amounts.setVisibility(View.VISIBLE);
            completeButton.setVisibility(View.VISIBLE);
            if (can_loan || can_repay) {
                loanedWrapper.setVisibility(View.VISIBLE);
            }

            if ((is_receiving_direction && transactionBalanceValue <= 0 && can_repay && !can_loan) ||
                    (is_receiving_direction && transactionBalanceValue > 0 && !can_repay && can_loan) ||
                    (is_giving_direction && transactionBalanceValue < 0 && !can_repay && can_loan) ||
                    (is_giving_direction && transactionBalanceValue >= 0 && can_repay && !can_loan)) {
                loanedWrapper.setVisibility(View.GONE);
                if (!can_mobilemoney && !can_pay && !can_gift) {
                    if (is_receiving_direction) {
                        if (can_repay) {
                            cannotTransactMessage.setText(R.string.cannotRepayNotOwedMoney);
                        } else {
                            cannotTransactMessage.setText(R.string.cannotLoanOwedMoney);
                        }
                    } else {
                        if (can_repay) {
                            cannotTransactMessage.setText(R.string.cannotTransactNothingToRepay);
                        } else {
                            cannotTransactMessage.setText(R.string.cannotTransactUnableToLoan);
                        }
                    }
                    cannotTransact.setVisibility(View.VISIBLE);
                    layout_amounts.setVisibility(View.GONE);
                    completeButton.setVisibility(View.GONE);
                }
            }
        }
    }

    private void bindEditText() {
        quantity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (quantityLayout.getVisibility() == View.VISIBLE) {
                    updateTotalPrice();
                }
            }
        });

        pricePerUnit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (layout_price.getVisibility() == View.VISIBLE) {
                    updateTotalPrice();
                }
            }
        });

        amountMobileMoney.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (layout_total_price.getVisibility() == View.GONE) {
                    addUpTotalPrice();
                } else {

                }
                if (mmWrapper.getVisibility() == View.VISIBLE && loanedWrapper.getVisibility() == View.VISIBLE) {
                    updateLoanedAmount();
                }
                if (loanedWrapper.getVisibility() == View.GONE && giftedWrapper.getVisibility() == View.VISIBLE) {
                    updateGiftedAmount();
                }
            }
        });

        amountPaid.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (layout_total_price.getVisibility() == View.GONE) {
                    addUpTotalPrice();
                } else {

                }
                if (paidWrapper.getVisibility() == View.VISIBLE && loanedWrapper.getVisibility() == View.VISIBLE) {
                    updateLoanedAmount();
                }
                if (loanedWrapper.getVisibility() == View.GONE && giftedWrapper.getVisibility() == View.VISIBLE) {
                    updateGiftedAmount();
                }
            }
        });

        amountLoaned.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (layout_total_price.getVisibility() == View.GONE) {
                    addUpTotalPrice();
                }
                if (quantityLayout.getVisibility() == View.GONE) {
                    quantity.setText("1");
                }
                if (layout_price.getVisibility() == View.GONE) {
                    pricePerUnit.setText(totalPrice.getText());
                }
                if (paidWrapper.getVisibility() == View.GONE) {
                    amountPaid.setText("0");
                }
                if (mmWrapper.getVisibility() == View.GONE) {
                    amountMobileMoney.setText("0");
                }
                if (can_repay && !can_loan && amountLoaned.getText().length() > 0) {
                    Double amountLoanedValue = getNumericValue(amountLoaned.getText().toString());
                    if (amountLoanedValue > Math.abs(transactionBalanceValue)) {
                        amountLoanedMessage.setVisibility(View.VISIBLE);
                        amountLoaned.setTextColor(ContextCompat.getColor(context, R.color.ofis_red));
                        loanedLabel.setTextColor(ContextCompat.getColor(context, R.color.ofis_red));
                        saveButton.setEnabled(false);
                        saveButton.setAlpha(.8f);
                        completeButton.setEnabled(false);
                        completeButton.setAlpha(.8f);
                    } else {
                        amountLoanedMessage.setVisibility(View.GONE);
                        amountLoaned.setTextColor(ContextCompat.getColor(context, R.color.ofis_text_field));
                        loanedLabel.setTextColor(ContextCompat.getColor(context, R.color.ofis_text_field));
                        saveButton.setEnabled(true);
                        saveButton.setAlpha(1f);
                        completeButton.setEnabled(true);
                        completeButton.setAlpha(1f);
                    }
                }
                if (giftedWrapper.getVisibility() == View.VISIBLE) {
                    updateGiftedAmount();
                }
                setAmountsText();
            }
        });

        amountGifted.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (layout_total_price.getVisibility() == View.GONE) {
                    addUpTotalPrice();
                }
            }
        });
    }

    private void updateLoanedAmount() {
        if (fullyLoaded && totalPrice.getText().length() > 0) {
            Double totalPriceValue = getNumericValue(totalPrice.getText().toString());
            Double amountPaidValue = 0.0;
            if (is_mobileMoneyMode) {
                if (amountMobileMoney.getText().length() > 0) {
                    amountPaidValue = getNumericValue(amountMobileMoney.getText().toString());
                }
                if (amountPaidValue > totalPriceValue) {
                    amountMobileMoney.setText(precision.format(totalPriceValue));
                    amountPaidValue = totalPriceValue;
                }
            } else {
                if (amountPaid.getText().length() > 0) {
                    amountPaidValue = getNumericValue(amountPaid.getText().toString());
                }
                if (amountPaidValue > totalPriceValue) {
                    amountPaid.setText(precision.format(totalPriceValue));
                    amountPaidValue = totalPriceValue;
                }
            }
            //noinspection NumberEquality
            if (totalPriceValue == amountPaidValue) {
                amountLoaned.setText(precision.format(0));
            } else {
                if (giftedWrapper.getVisibility() == View.GONE) {
                    Double amountLoanedValue = totalPriceValue - amountPaidValue;
                    amountLoaned.setText(precision.format(amountLoanedValue));
                    if (layout_total_price.getVisibility() == View.VISIBLE) {
                        amountLoaned.setEnabled(false);
                    }
                } else {
                    amountLoaned.setEnabled(true);
                    if (amountLoaned.getText().length() > 0) {
                        updateGiftedAmount();
                    }
                }
            }
        }
    }

    private void updateGiftedAmount() {
        if (layout_total_price.getVisibility() == View.GONE) {
            addUpTotalPrice();
        }
        if (fullyLoaded && totalPrice.getText().length() > 0) {
            Double totalPriceValue = null;
            Double amountPaidValue = 0.0;
            totalPriceValue = getNumericValue(totalPrice.getText().toString());
            if (is_mobileMoneyMode) {
                if (amountMobileMoney.getText().length() > 0) {
                    amountPaidValue = getNumericValue(amountMobileMoney.getText().toString());
                }
            } else {
                if (amountPaid.getText().length() > 0) {
                    amountPaidValue = getNumericValue(amountPaid.getText().toString());
                }
            }

            Double amountLoanedValue = 0.00;
            if (amountPaidValue > totalPriceValue) {
                if (is_mobileMoneyMode) {
                    amountMobileMoney.setText(precision.format(totalPriceValue));
                } else {
                    amountPaid.setText(precision.format(totalPriceValue));
                }
                amountPaidValue = totalPriceValue;
            }
            if (amountLoaned.getText().length() > 0) {
                amountLoanedValue = getNumericValue(amountLoaned.getText().toString());
                if (amountLoanedValue + amountPaidValue > totalPriceValue) {
                    amountLoanedValue = totalPriceValue - amountPaidValue;
                    amountLoaned.setText(precision.format(amountLoanedValue));
                }
            }
            Double amountGiftedValue = totalPriceValue - amountPaidValue - amountLoanedValue;
            amountGifted.setText(precision.format(amountGiftedValue));
        }
        if (fullyLoaded && mmWrapper.getVisibility() == View.GONE && paidWrapper.getVisibility() == View.GONE && loanedWrapper.getVisibility() == View.GONE && giftedWrapper.getVisibility() == View.VISIBLE) {
            if (totalPrice.getText().length() > 0) {
                Double totalPriceValue = null;
                totalPriceValue = getNumericValue(totalPrice.getText().toString());
                amountGifted.setText(precision.format(totalPriceValue));
                amountGifted.setEnabled(false);
            }
        }
    }

    private void resetAmounts() {
        if (fullyLoaded && !readOnlyMode) {
            amountMobileMoney.setText("");
            amountPaid.setText("");
            amountLoaned.setText("");
            amountGifted.setText("");
        }
    }

    private void addUpTotalPrice() {
        Double amountMmValue = (amountMobileMoney.getText().length() > 0) ? getNumericValue(amountMobileMoney.getText().toString()) : 0.0;
        Double amountPaidValue = (amountPaid.getText().length() > 0) ? getNumericValue(amountPaid.getText().toString()) : 0.0;
        Double amountLoanedValue = (amountLoaned.getText().length() > 0) ? getNumericValue(amountLoaned.getText().toString()) : 0.0;
        Double amountGiftedValue = (amountGifted.getText().length() > 0) ? getNumericValue(amountGifted.getText().toString()) : 0.0;
        totalPrice.setText(precision.format(amountMmValue + amountPaidValue + amountLoanedValue + amountGiftedValue));
    }

    private void updateTotalPrice() {
        Double baseQuantity = 1d;
        Double totalPriceValue;
        if (quantity.getText().length() > 0) {
            baseQuantity = getNumericValue(quantity.getText().toString());
        }
        if (pricePerUnit.getText().length() > 0) {
            Double pricePerUnitValue = getNumericValue(pricePerUnit.getText().toString());
            totalPriceValue = baseQuantity * pricePerUnitValue;
            totalPrice.setText(precision.format(totalPriceValue));
        } else {
            totalPrice.setText("");
        }
        if (is_mobileMoneyMode) {
            if (amountMobileMoney.getText().length() > 0 && totalPrice.getText().length() > 0) {
                Double amountMmValue = getNumericValue(amountMobileMoney.getText().toString());
                totalPriceValue = getNumericValue(totalPrice.getText().toString());
                if (amountMmValue > totalPriceValue) {
                    amountMobileMoney.setText(precision.format(totalPriceValue));
                }
            }
            if (mmWrapper.getVisibility() == View.VISIBLE && loanedWrapper.getVisibility() == View.GONE && giftedWrapper.getVisibility() == View.GONE) {
                if (totalPrice.getText().length() > 0) {
                    totalPriceValue = getNumericValue(totalPrice.getText().toString());
                    amountMobileMoney.setText(precision.format(totalPriceValue));
                    amountMobileMoney.setEnabled(false);
                }
            } else {
                amountMobileMoney.setEnabled(true);
            }
        } else {
            if (amountPaid.getText().length() > 0 && totalPrice.getText().length() > 0) {
                Double amountPaidValue = getNumericValue(amountPaid.getText().toString());
                totalPriceValue = getNumericValue(totalPrice.getText().toString());
                if (amountPaidValue > totalPriceValue) {
                    amountPaid.setText(precision.format(totalPriceValue));
                }
            }
            if (paidWrapper.getVisibility() == View.VISIBLE && loanedWrapper.getVisibility() == View.GONE && giftedWrapper.getVisibility() == View.GONE) {
                if (totalPrice.getText().length() > 0) {
                    totalPriceValue = getNumericValue(totalPrice.getText().toString());
                    amountPaid.setText(precision.format(totalPriceValue));
                    amountPaid.setEnabled(false);
                }
            } else {
                amountPaid.setEnabled(true);
            }
        }
        updateLoanedAmount();
        if (loanedWrapper.getVisibility() == View.VISIBLE && can_repay && !can_loan && amountLoaned.getText().length() > 0 && totalPrice.getText().length() > 0) {
            Double amountLoanedValue = getNumericValue(amountLoaned.getText().toString());
            if (amountLoanedValue > Math.abs(transactionBalanceValue)) {
                amountLoaned.setText(precision.format(Math.abs(transactionBalanceValue)));
                if (giftedWrapper.getVisibility() == View.GONE) {
                    if (is_mobileMoneyMode) {
                        if (layout_price.getVisibility() == View.VISIBLE && mmWrapper.getVisibility() == View.VISIBLE && amountLoanedValue != null) {
                            totalPriceValue = getNumericValue(totalPrice.getText().toString());
                            amountMobileMoney.setText(precision.format(totalPriceValue - Math.abs(transactionBalanceValue)));
                        }
                    } else {
                        if (layout_price.getVisibility() == View.VISIBLE && paidWrapper.getVisibility() == View.VISIBLE && amountLoanedValue != null) {
                            totalPriceValue = getNumericValue(totalPrice.getText().toString());
                            amountPaid.setText(precision.format(totalPriceValue - Math.abs(transactionBalanceValue)));
                        }
                    }
                }
            }
        }
        if (giftedWrapper.getVisibility() == View.VISIBLE) {
            updateGiftedAmount();
        }
        setAmountsText();
    }

    @SuppressLint("SetTextI18n")
    private void checkProductPrices() {
        if (readOnlyMode || !fullyLoaded) {
            if (readOnlyMode) {
                priceLabel.setText(priceLabel.getText());
            }
            return;
        }

        if (directionSpinner.getSelectedItemId() > 0 && selectedProduct > 0 && selectedCurrency > 0) {
            Cursor possiblePrices = dl.getPossiblePrices((int)directionSpinner.getSelectedItemId(), selectedProduct, selectedCurrency);

            if (possiblePrices.moveToFirst()) {
                if (!possiblePrices.getString(possiblePrices.getColumnIndex(OFISDBContract.ProductPrice.COLUMN_NAME_FIXED_PRICE)).equalsIgnoreCase("null")) {
                    pricePerUnit.setText(possiblePrices.getString(possiblePrices.getColumnIndex(OFISDBContract.ProductPrice.COLUMN_NAME_FIXED_PRICE)));
                    priceLabel.setText(res.getString(R.string.price_per_unit) + " " + unitsText);
                    pricePerUnit.setEnabled(false);
                    pricePerUnit.setFilters(new InputFilter[]{});
                } else {
                    double minPrice = possiblePrices.getDouble(possiblePrices.getColumnIndex(OFISDBContract.ProductPrice.COLUMN_NAME_MIN_PRICE));
                    double maxPrice = possiblePrices.getDouble(possiblePrices.getColumnIndex(OFISDBContract.ProductPrice.COLUMN_NAME_MAX_PRICE));

                    if (minPrice >= 0 && maxPrice > 0) {
                        priceLabel.setText(res.getString(R.string.price_per_unit) + " " + unitsText + ": " + String.valueOf(minPrice) + " - " + String.valueOf(maxPrice));
                        pricePerUnit.setFilters(new InputFilter[]{new InputFilterMinMax(minPrice, maxPrice)});
                        pricePerUnit.setText("");
                    }

                    pricePerUnit.setEnabled(true);
                }
                dl.setSpinnerSelectionById(possiblePrices.getInt(possiblePrices.getColumnIndex(OFISDBContract.ProductPrice.COLUMN_NAME_CURRENCY_ID)), currencySpinner);
            } else {
                priceLabel.setText(res.getString(R.string.price_per_unit) + " " + unitsText);
                pricePerUnit.setEnabled(true);
                pricePerUnit.setFilters(new InputFilter[]{});
            }
        } else {
            if (layout_price.getVisibility() == View.VISIBLE) {
                priceLabel.setText(res.getString(R.string.price_per_unit) + " " + unitsText);
                pricePerUnit.setEnabled(true);
                pricePerUnit.setFilters(new InputFilter[]{});
            }
        }
    }

    private boolean validatePage1() {
        long itemGroupId = itemTypeGroupSpinner.getSelectedItemId();
        if (itemGroupId < 1) {
            return false;
        }

        long itemTypeId = itemTypeSpinner.getSelectedItemId();
        if (itemTypeId < 1) {
            return false;
        }

        long directionId = directionSpinner.getSelectedItemId();
        if (directionId < 1) {
            return false;
        }

        return true;
    }

    private ContentValues validateValues() {
        ContentValues values = new ContentValues();

        if (!validatePage1()) {
            return values;
        }

        long unitId = unitsSpinner.getSelectedItemId();
        if (unitId < 1) {
            return values;
        }

        String quantityValue = String.valueOf(quantity.getText());
        if (quantityValue.equals("")) {
            return values;
        }

        String pricePerUnitValue = String.valueOf(pricePerUnit.getText());
        if (pricePerUnitValue.equals("")) {
            return values;
        }

        String totalPriceValue = String.valueOf(totalPrice.getText());
        if (totalPriceValue.equals("")) {
            return values;
        }

        String amountMmValue = String.valueOf(amountMobileMoney.getText());
        if (mmWrapper.getVisibility() == View.VISIBLE && amountMmValue.equals("")) {
            return values;
        }

        String recipientDiallingCode = "";
        String recipientPhoneNumber = "";
        if (is_mobileMoneyMode) {
            recipientDiallingCode = String.valueOf(diallingCode.getText());
            if (recipientDiallingCode.equals("")) {
                return values;
            }
            recipientPhoneNumber = String.valueOf(phoneNumber.getText());
            if (recipientPhoneNumber.equals("")) {
                return values;
            }
        }

        String amountPaidValue = String.valueOf(amountPaid.getText());
        if (paidWrapper.getVisibility() == View.VISIBLE && amountPaidValue.equals("")) {
            return values;
        }

        String amountLoanedValue = String.valueOf(amountLoaned.getText());
        if (loanedWrapper.getVisibility() == View.VISIBLE && amountLoanedValue.equals("")) {
            return values;
        }

        long currencyId = currencySpinner.getSelectedItemId();
        if (currencyId < 1) {
            return values;
        }

        try {
            values.put(OFISDBContract.TransactionItem.COLUMN_NAME_TRANSACTIONITEMGROUP_ID, itemTypeGroupSpinner.getSelectedItemId());
            values.put(OFISDBContract.TransactionItem.COLUMN_NAME_TRANSACTIONITEMTYPE_ID, itemTypeSpinner.getSelectedItemId());
            values.put(OFISDBContract.TransactionItem.COLUMN_NAME_DIRECTION_ID, directionSpinner.getSelectedItemId());
            values.put(OFISDBContract.TransactionItem.COLUMN_NAME_UNIT_ID, unitId);
            values.put(OFISDBContract.TransactionItem.COLUMN_NAME_AMOUNT_LOANED_TEXT, loanedLabel.getText().toString());
            values.put(OFISDBContract.TransactionItem.COLUMN_NAME_TRANSACTION_NOTES, transactionNotes.getText().toString());
            values.put(OFISDBContract.TransactionItem.COLUMN_NAME_MOBILEMONEYCASH_ID, mmCashSpinner.getSelectedItemId());
            if (is_mobileMoneyMode) {
                values.put(OFISDBContract.TransactionItem.COLUMN_NAME_TRANSACTION_MMPP_RECIPIENT_DIALLINGCODE, recipientDiallingCode);
                values.put(OFISDBContract.TransactionItem.COLUMN_NAME_TRANSACTION_MMPP_RECIPIENT_NUMBER, recipientPhoneNumber);
            }

            if (productGradeLayout.getVisibility() == View.VISIBLE) {
                int productId = selectedProduct;
                if (productId > 0) {
                    values.put(OFISDBContract.TransactionItem.COLUMN_NAME_PRODUCT_ID, productId);
                }

                long productGradeId = productGradeSpinner.getSelectedItemId();
                if (productGradeId > 0) {
                    values.put(OFISDBContract.TransactionItem.COLUMN_NAME_PRODUCT_GRADE_ID, productGradeId);
                }
            }
            values.put(OFISDBContract.TransactionItem.COLUMN_NAME_QUANTITY, getNumericValue(quantityValue));
            values.put(OFISDBContract.TransactionItem.COLUMN_NAME_PRICE_PER_UNIT, getNumericValue(pricePerUnitValue));
            values.put(OFISDBContract.TransactionItem.COLUMN_NAME_TOTAL_PRICE, getNumericValue(totalPriceValue));
            if (is_mobileMoneyMode) {
                if (amountMobileMoney.getText().length() > 0) {
                    values.put(OFISDBContract.TransactionItem.COLUMN_NAME_AMOUNT_MOBILEMONEY, getNumericValue(amountMmValue));
                } else {
                    values.put(OFISDBContract.TransactionItem.COLUMN_NAME_AMOUNT_MOBILEMONEY, "0");
                }
                values.put(OFISDBContract.TransactionItem.COLUMN_NAME_AMOUNT_PAID, "0");
            } else {
                if (amountPaid.getText().length() > 0) {
                    values.put(OFISDBContract.TransactionItem.COLUMN_NAME_AMOUNT_PAID, getNumericValue(amountPaidValue));
                } else {
                    values.put(OFISDBContract.TransactionItem.COLUMN_NAME_AMOUNT_PAID, "0");
                }
                values.put(OFISDBContract.TransactionItem.COLUMN_NAME_AMOUNT_MOBILEMONEY, "0");
            }
            if (amountLoaned.getText().length() > 0) {
                values.put(OFISDBContract.TransactionItem.COLUMN_NAME_AMOUNT_LOANED, getNumericValue(amountLoanedValue));
            } else {
                values.put(OFISDBContract.TransactionItem.COLUMN_NAME_AMOUNT_LOANED, "0");
            }
            if (amountGifted.getText().length() > 0) {
                values.put(OFISDBContract.TransactionItem.COLUMN_NAME_AMOUNT_GIFTED, getNumericValue(amountGifted.getText().toString()));
            } else {
                values.put(OFISDBContract.TransactionItem.COLUMN_NAME_AMOUNT_GIFTED, "0");
            }
            values.put(OFISDBContract.TransactionItem.COLUMN_NAME_CURRENCY_ID, currencyId);

        } catch (Exception e) {
            FirebaseCrash.report(new Exception("Transaction: Error generating Content Values for save", e));
            values.clear();
        }

        return values;
    }

    private void setupPage2() {
        Cursor buyingRule = dl.getBuyingRule(buyingAgentId, transacteeId, transacteeType, itemTypeGroupSpinner.getSelectedItemId(), itemTypeSpinner.getSelectedItemId(), directionSpinner.getSelectedItemId());

        if (buyingRule.moveToFirst()) {
            can_mobilemoney = (buyingRule.getInt(buyingRule.getColumnIndex(OFISDBContract.BuyingRule.COLUMN_NAME_CAN_MOBILEMONEY)) == 1) ? true : false;
            can_pay = (buyingRule.getInt(buyingRule.getColumnIndex(OFISDBContract.BuyingRule.COLUMN_NAME_CAN_PAY)) == 1) ? true : false;
            can_loan = (buyingRule.getInt(buyingRule.getColumnIndex(OFISDBContract.BuyingRule.COLUMN_NAME_CAN_LOAN)) == 1) ? true : false;
            can_repay = (buyingRule.getInt(buyingRule.getColumnIndex(OFISDBContract.BuyingRule.COLUMN_NAME_CAN_REPAY)) == 1) ? true : false;
            can_gift = (buyingRule.getInt(buyingRule.getColumnIndex(OFISDBContract.BuyingRule.COLUMN_NAME_CAN_GIFT)) == 1) ? true : false;

            layout_mm_or_cash.setVisibility((can_mobilemoney) ? View.VISIBLE : View.GONE);
            paidWrapper.setVisibility((can_pay) ? View.VISIBLE : View.GONE);
            loanedWrapper.setVisibility((can_loan || can_repay) ? View.VISIBLE : View.GONE);
            giftedWrapper.setVisibility((can_gift) ? View.VISIBLE : View.GONE);

            if (selectedMmOrCash == 0 && can_pay) {
                mmWrapper.setVisibility(View.GONE);
                recipientPhoneWrapper.setVisibility(View.GONE);
                paidWrapper.setVisibility(View.VISIBLE);
                is_mobileMoneyMode = false;
            } else if (selectedMmOrCash == 1 && can_mobilemoney) {
                mmWrapper.setVisibility(View.VISIBLE);
                recipientPhoneWrapper.setVisibility(View.VISIBLE);
                paidWrapper.setVisibility(View.GONE);
                is_mobileMoneyMode = true;
            }

            setAmountsText();
        }
    }

    private void bindPaging() {
        Button page1Next = (Button) findViewById(R.id.page1next);
        Button page2Back = (Button) findViewById(R.id.page2back);

        final LinearLayout page1 = (LinearLayout) findViewById(R.id.transactionPage1);
        final LinearLayout page2 = (LinearLayout) findViewById(R.id.transactionPage2);


        page1Next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validatePage1()) {
                    setupPage2();
                    page1.setVisibility(View.GONE);
                    page2.getLayoutParams().height = LinearLayout.LayoutParams.WRAP_CONTENT;
                    page2.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(context, R.string.Please_fill_all_fields, Toast.LENGTH_LONG).show();
                }
            }
        });

        page2Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                page1.setVisibility(View.VISIBLE);
                page2.setVisibility(View.INVISIBLE);
                page2.getLayoutParams().height = 0;
            }
        });

    }

    private void bindSaveButton() {
        if (readOnlyMode) {
            saveButton.setVisibility(View.GONE);
            completeButton.setVisibility(View.GONE);
        } else {

            saveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // get the entered values
                    ContentValues values = validateValues();
                    if (values.size() > 0) {
                        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                        values.put(OFISDBContract.TransactionItem.COLUMN_NAME_TRANSACTION_DATE, timestamp.toString());
                        values.put(OFISDBContract.TransactionItem.COLUMN_NAME_BUYINGAGENT_ID, buyingAgentId);
                        values.put(OFISDBContract.TransactionItem.COLUMN_NAME_TRANSACTEE_ID, transacteeId);
                        values.put(OFISDBContract.TransactionItem.COLUMN_NAME_TRANSACTEE_TYPE, transacteeType);

                        if (editMode) {
                            if (dl.updateTransactionItem(values, transactionItemId) > 0) {
                                Toast.makeText(context, res.getString(R.string.Transaction_item_updated), Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(context, TransactionItems.class);
                                startActivity(intent);
                            }
                        } else {
                            if (is_mobileMoneyMode) {
                                if (upload_id == null) {
                                    upload_id = UUID.randomUUID();
                                }
                                values.put(OFISDBContract.TransactionItem.COLUMN_NAME_UPLOAD_ID, upload_id.toString());

                            }
                            long newtransactionItemId = dl.insertTransactionItem(values);
                            if (newtransactionItemId != -1) {
                                Toast.makeText(context, res.getString(R.string.Transaction_item_added), Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(context, TransactionItems.class);
                                startActivity(intent);
                            }
                        }

                    } else {
                        Toast.makeText(context, res.getString(R.string.Please_fill_all_fields), Toast.LENGTH_LONG).show();
                    }
                }
            });

            completeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // get the entered values
                    ContentValues values = validateValues();

                    if (layout_total_price.getVisibility() == View.VISIBLE && totalPrice.getText().length() > 0) {
                        Double amountMmValue = (amountMobileMoney.getText().length() > 0) ? getNumericValue(amountMobileMoney.getText().toString()) : 0.0;
                        Double amountPaidValue = (amountPaid.getText().length() > 0) ? getNumericValue(amountPaid.getText().toString()) : 0.0;
                        Double amountLoanedValue = (amountLoaned.getText().length() > 0) ? getNumericValue(amountLoaned.getText().toString()) : 0.0;
                        Double amountGiftedValue = (amountGifted.getText().length() > 0) ? getNumericValue(amountGifted.getText().toString()) : 0.0;
                        Double totalPriceValue = getNumericValue(totalPrice.getText().toString());

                        if (totalPriceValue != (amountMmValue + amountPaidValue + amountLoanedValue + amountGiftedValue)) {
                            Toast.makeText(context, res.getString(R.string.ensure_totals_match), Toast.LENGTH_LONG).show();
                            return;
                        }
                    }

                    if (values.size() > 0) {
                        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                        values.put(OFISDBContract.TransactionItem.COLUMN_NAME_TRANSACTION_DATE, timestamp.toString());
                        values.put(OFISDBContract.TransactionItem.COLUMN_NAME_BUYINGAGENT_ID, buyingAgentId);
                        values.put(OFISDBContract.TransactionItem.COLUMN_NAME_TRANSACTEE_ID, transacteeId);
                        values.put(OFISDBContract.TransactionItem.COLUMN_NAME_TRANSACTEE_TYPE, transacteeType);
                        values.put(OFISDBContract.TransactionItem.COLUMN_NAME_TRANSACTION_SIGNAL_STRENGTH, signalStrengthValue);
                        if (is_mobileMoneyMode) {
                            values.put(OFISDBContract.TransactionItem.COLUMN_NAME_TRANSACTION_STATUS, "initiated");
                        }

                        if (editMode) {
                            if (dl.updateTransactionItem(values, transactionItemId) > 0) {
                                Toast.makeText(context, res.getString(R.string.Transaction_item_updated), Toast.LENGTH_LONG).show();
                            }
                        } else {
                            if (is_mobileMoneyMode) {
                                if (upload_id == null) {
                                    upload_id = UUID.randomUUID();
                                }
                                values.put(OFISDBContract.TransactionItem.COLUMN_NAME_UPLOAD_ID, upload_id.toString());
                            }
                            transactionItemId = dl.insertTransactionItem(values);
                            if (is_mobileMoneyMode) {
                                editMode = true;
                            }
                            if (transactionItemId != -1) {
                                Toast.makeText(context, res.getString(R.string.Transaction_item_added), Toast.LENGTH_LONG).show();
                            }
                        }

                        if (transactionItemId != 0) {
                            if (loanedWrapper.getVisibility() == View.VISIBLE && amountLoaned.getText().toString().length() > 0) {
                                transactionBalanceId = dl.updateTransactionBalance(transactionBalanceId, String.valueOf(getNumericValue(amountLoaned.getText().toString())), buyingAgentId, transacteeId, transacteeType, currencySpinner.getSelectedItemId(), is_receiving_direction, is_right_hand_ba, false, false);
                            }

                            ContentValues transactionBalanceValues = new ContentValues();
                            transactionBalanceValues.put(OFISDBContract.TransactionItem.COLUMN_NAME_TRANSACTION_BALANCE_ID, transactionBalanceId);
                            dl.updateTransactionItem(transactionBalanceValues, transactionItemId);

                            if (is_mobileMoneyMode) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                builder.setCancelable(true);
                                builder.setMessage(res.getString(R.string.mm_transaction_confirmation, transacteeNameValue, (currencySymbol + amountMobileMoney.getText().toString())))
                                        .setNegativeButton(res.getString(R.string.app_update_cancel), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int i) {
                                                dialog.cancel();
                                            }
                                        })
                                        .setPositiveButton(res.getString(R.string.mm_transaction_confirm), new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                doMobileMoneyTransaction();
                                            }
                                        });
                                AlertDialog alert = builder.create();
                                alert.show();
                            } else {
                                ContentValues networkQueueValues = new ContentValues();
                                networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SUBMISSION_TYPE, "transaction");
                                networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_NAME, res.getString(R.string.Transaction_network_queue_title));
                                networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_TYPE, transacteeName.getText().toString());
                                networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_TRANSACTION_ITEM_ID, transactionItemId);
                                networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_TRANSACTEE_TYPE, transacteeType);
                                networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_OLAM_FARMER_ID, olamFarmerId);
                                networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID, transacteeId);
                                networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_CREATED_AT, timestamp.toString());
                                networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPDATED_AT, timestamp.toString());
                                if (upload_id != null) {
                                    networkQueueValues.put(OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPLOAD_ID, upload_id.toString());
                                }

                                if (dl.insertNetworkQueueEntry(networkQueueValues) != 0) {
                                    ContentValues transactionItemValues = new ContentValues();
                                    transactionItemValues.put(OFISDBContract.TransactionItem.COLUMN_NAME_IN_QUEUE, 1);
                                    if (dl.updateTransactionItem(transactionItemValues, transactionItemId) > 0) {
                                        Toast.makeText(context, R.string.transaction_added_to_queue, Toast.LENGTH_LONG).show();
                                        Intent intent = new Intent(context, TransactionItems.class);
                                        startActivity(intent);
                                    }
                                }
                            }
                        }

                    } else {
                        Toast.makeText(context, res.getString(R.string.Please_fill_all_fields), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    Handler syncHandler;
    private void completeMobileMoneyTransaction(final Message msg) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (m_ProgressDialog!=null && m_ProgressDialog.isShowing())
                {
                    Utilities.closeProgressDialog(m_ProgressDialog, 0);
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setCancelable(false);
                builder.setMessage(msg.obj.toString())
                        .setPositiveButton(res.getString(R.string.app_update_ok), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dl.deleteMobileMoneyTransactionItem(transactionItemId);
                                Intent intent = new Intent(context, TransactionItems.class);
                                startActivity(intent);
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
    }

    private void failMobileMoneyTransaction(final Message msg) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (m_ProgressDialog!=null && m_ProgressDialog.isShowing())
                {
                    Utilities.closeProgressDialog(m_ProgressDialog, 0);
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setCancelable(false);
                builder.setMessage(msg.obj.toString())
                        .setPositiveButton(res.getString(R.string.app_update_ok), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // auto change to cash?
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
    }

    private void checkMobileMoneyTransaction() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                countDownTimer = new CountDownTimer(65000, 5000) {

                    public void onTick(long millisUntilFinished) {
                        if (m_ProgressDialog!=null && m_ProgressDialog.isShowing())
                        {
                            String pdUpdate = res.getString(R.string.mm_transaction_checking);
                            m_ProgressDialog = Utilities.updateProgressDialog(m_ProgressDialog, 0, 1, pdUpdate);
                        }
                        nl.checkMobileMoneyTransaction(syncHandler, transactionItemId);
                    }

                    @Override
                    public void onFinish() {
                        if (m_ProgressDialog!=null && m_ProgressDialog.isShowing())
                        {
                            String pdUpdate = res.getString(R.string.mm_transaction_timedout);
                            m_ProgressDialog = Utilities.updateProgressDialog(m_ProgressDialog, 0, 1, pdUpdate);
                            Utilities.closeProgressDialog(m_ProgressDialog, 6000);
                        }
                        dl.recordMmTransactionTimeout(transactionItemId);
                    }
                }.start();
            }
        });
    }

    private void doMobileMoneyTransaction() {
        if (nl.isPhoneOnline()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    m_ProgressDialog = Utilities.openProgressDialog(context, res.getString(R.string.Please_wait) + " " + res.getString(R.string.Submitting_data), true);
                }
            });

            syncHandler = new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    if (msg.what == 1) {
                        if (m_ProgressDialog != null && m_ProgressDialog.isShowing()) {
                            String pdUpdate = msg.obj.toString();
                            m_ProgressDialog = Utilities.updateProgressDialog(m_ProgressDialog, 0, 1, pdUpdate);
                            checkMobileMoneyTransaction();
                        }
                    } else if (msg.what == 2) {
                        if (countDownTimer != null) {
                            countDownTimer.cancel();
                        }
                        failMobileMoneyTransaction(msg);
                    } else if (msg.what == 3) {
                        if (m_ProgressDialog != null && m_ProgressDialog.isShowing()) {
                            m_ProgressDialog = Utilities.updateProgressDialog(m_ProgressDialog, 0, 1, res.getString(R.string.farmer_not_synced));
                            Utilities.closeProgressDialog(m_ProgressDialog, 3000);
                        }
                    } else if (msg.what == 4) {
                        if (m_ProgressDialog != null && m_ProgressDialog.isShowing()) {
                            String pdUpdate = msg.obj.toString();
                            m_ProgressDialog = Utilities.updateProgressDialog(m_ProgressDialog, 0, 1, pdUpdate);
                        }
                    } else if (msg.what == 5) {
                        if (countDownTimer != null) {
                            countDownTimer.cancel();
                        }
                        completeMobileMoneyTransaction(msg);
                    }
                }
            };
            nl.initiateMobileMoneyTransaction(syncHandler, transactionItemId, transacteeId);
        } else {
            Toast.makeText(context, res.getString(R.string.Internet_not_available), Toast.LENGTH_LONG).show();
        }
    }

    private void bindSignalStrengthRecorder() {
        ssListener = new signalStrengthListener();
        telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(ssListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
    }

    private class InputFilterMinMax implements InputFilter {
        private final double min;
        private final double max;

        InputFilterMinMax(double min, double max) {
            this.min = min;
            this.max = max;
        }

        private boolean isInRange(double a, double b, double c) {
            return b > a ? c >= a && c <= b : c >= b && c <= a;
        }

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            try {
                double input = getNumericValue(dest.toString()) + getNumericValue(source.toString());
                if (isInRange(min, max, input))
                    return null;
            } catch (NumberFormatException nfe) {
                FirebaseCrash.report(new Exception("Transaction: Error in input filtering", nfe));
                nfe.printStackTrace();
            }
            return "";
        }

    }

    private class signalStrengthListener extends PhoneStateListener {

        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);
            if (signalStrength.isGsm()) {
                signalStrengthValue = "GSM Strength: " + signalStrength.getGsmSignalStrength();
            } else {
                signalStrengthValue = "Cdma Dbm: " + signalStrength.getCdmaDbm();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        if (id == R.id.action_home) {
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
            return true;
        }

        if (id == android.R.id.home) {
            if (backtoFinder) {
                finish();
            } else {
                NavUtils.navigateUpFromSameTask(this);
            }
            return true;
        }

        if (id == R.id.action_tasks) {
            Intent intent = new Intent(this, TasksActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
