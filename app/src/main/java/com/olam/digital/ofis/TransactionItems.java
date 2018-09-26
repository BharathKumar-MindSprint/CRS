package com.olam.digital.ofis;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.olam.digital.ofis.R;

public class TransactionItems extends BaseListActivity {

    private Context context;
    private Resources res;
    private DataLayer dl;
    private TransactionItems me;

    private CustomListAdapter_TransactionItems customListAdapter_transactionItems;

    private final static String EDIT_TRANSACTION_ITEM_ID = "EDIT_TRANSACTION_ITEM_ID";
    private final static String TRANSACTEE_ID = "TRANSACTEE_ID";
    private final static String TRANSACTEE_TYPE = "TRANSACTEE_TYPE";
    private final static String EDIT_MODE = "EDIT_MODE";
    private final static String TRANSACTION_MODE = "TRANSACTION_MODE";
    private final static String READ_ONLY_MODE = "READ_ONLY_MODE";
    private final static String BACK_TO_FINDER = "BACK_TO_FINDER";
    private final static String BUYING_AGENT_CONTEXT = "BUYING_AGENT_CONTEXT";

    private Cursor transactionItems;
    private TextView noTransactionsText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_transactionitems);

        //noinspection ConstantConditions
        getActionBar().setDisplayHomeAsUpEnabled(true);

        context = me = this;
        res = getResources();

        dl = new DataLayer(res, context);

        noTransactionsText = (TextView) findViewById(R.id.notransactionttext);

        getTransactionItems();
        bindButtons();
    }

    private void getTransactionItems() {
        transactionItems = dl.getTransactionItems();

        if (transactionItems.getCount() > 0) {
            noTransactionsText.setVisibility(View.GONE);
            String[] adapterCols = new String[]{"_id", OFISDBContract.TransactionItem.COLUMN_NAME_TRANSACTEE_ID, OFISDBContract.TransactionItem.COLUMN_NAME_TRANSACTEE_TYPE, OFISDBContract.TransactionItem.COLUMN_NAME_IN_QUEUE ,OFISDBContract.BuyingAgent.COLUMN_NAME_NAME, "transactee_name"};
            int[] adapterRowViews = new int[]{R.id.transactionItemId, R.id.transacteeId, R.id.transacteeType, R.id.readOnlyMode, R.id.transactionBuyingAgent, R.id.transactionTransactee};
            customListAdapter_transactionItems = new CustomListAdapter_TransactionItems(context, transactionItems, adapterCols, adapterRowViews, res, me);
            setListAdapter(customListAdapter_transactionItems);
        } else {
            noTransactionsText.setVisibility(View.VISIBLE);
        }
    }

    public void deleteTransactionItem(final long transactionItemId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder
        .setCancelable(true)
        .setNegativeButton(res.getString(R.string.app_update_cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        })
        .setMessage(res.getString(R.string.transaction_are_you_sure))
        .setPositiveButton(res.getString(R.string.app_update_ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dl.deleteTransactionItem(transactionItemId, false) > 0) {
                    transactionItems.close();
                    transactionItems = dl.getTransactionItems();
                    customListAdapter_transactionItems.swapCursor(transactionItems);
                    if (transactionItems.getCount() > 0) {
                        noTransactionsText.setVisibility(View.GONE);
                    } else {
                        noTransactionsText.setVisibility(View.VISIBLE);
                    }
                    Toast.makeText(context, res.getString(R.string.transaction_deleted), Toast.LENGTH_LONG).show();
                }
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    protected void onListItemClick (ListView l, View v, int position, long id) {
        TextView transacteeId = (TextView) v.findViewById(R.id.transacteeId);
        TextView transacteeType = (TextView) v.findViewById(R.id.transacteeType);

        //we now view this transaction
        Intent intent = new Intent(context, TransactionItem.class);
        intent.putExtra(EDIT_TRANSACTION_ITEM_ID, id);
        intent.putExtra(TRANSACTEE_ID, Long.parseLong(transacteeId.getText().toString()));
        intent.putExtra(TRANSACTEE_TYPE, transacteeType.getText().toString());
        intent.putExtra(EDIT_MODE, true);

        TextView readOnlyValue = (TextView) v.findViewById(R.id.readOnlyMode);
        if (readOnlyValue.getText().equals("1")) {
            intent.putExtra(READ_ONLY_MODE, true);
        }

        context.startActivity(intent);
    }

    private void bindButtons() {
        ImageButton addTransaction = (ImageButton) findViewById(R.id.addTransaction);
        final Cursor buyingAgents = dl.getBuyingAgents();

        addTransaction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (buyingAgents.getCount() == 1) {
                    buyingAgents.moveToFirst();
                    Intent intent = new Intent(context, FindFarmerActivity.class);
                    intent.putExtra(BUYING_AGENT_CONTEXT, buyingAgents.getLong(buyingAgents.getColumnIndex(OFISDBContract.BuyingAgent.COLUMN_NAME_ID)));
                    intent.putExtra(TRANSACTION_MODE, true);
                    intent.putExtra(BACK_TO_FINDER, true);
                    context.startActivity(intent);
                } else {
                    Intent intent = new Intent(context, BuyingAgent.class);
                    context.startActivity(intent);
                }
            }
        });
    }
}
