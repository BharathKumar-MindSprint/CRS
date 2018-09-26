package com.olam.digital.ofis;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.olam.digital.ofis.R;

public class BuyingAgent extends BaseActivity {
    private Context context;
    private Resources res;
    private Spinner buyingAgentSpinner;
    private Button nextButton;
    private DataLayer dl;

    private final static String BUYING_AGENT_CONTEXT = "BUYING_AGENT_CONTEXT";
    private final static String TRANSACTION_MODE = "TRANSACTION_MODE";
    private final static String BACK_TO_FINDER = "BACK_TO_FINDER";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buying_agent);

        //noinspection ConstantConditions
        getActionBar().setDisplayHomeAsUpEnabled(true);

        context = this;
        res = getResources();

        dl = new DataLayer(res, context);

        buyingAgentSpinner = (Spinner) this.findViewById(R.id.spinner_buyingagents);
        nextButton = (Button) this.findViewById(R.id.buyingAgentNext);

        bindSpinner();
        bindButton();
    }

    private void bindSpinner() {
        Cursor buyingAgents = dl.getSimpleSpinnerData(OFISDBContract.BuyingAgent.TABLE_NAME, false, null, null, true, OFISDBContract.BuyingAgent.COLUMN_NAME_NAME, OFISDBContract.BuyingAgent.COLUMN_NAME_NAME);
        dl.bindSimpleDataDrivenSpinner(buyingAgentSpinner, buyingAgents, context, null);
    }

    private void bindButton() {
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                long buyingAgentId = buyingAgentSpinner.getSelectedItemId();
                if (buyingAgentId < 1) {
                    Toast.makeText(context, R.string.PickAgentFirst, Toast.LENGTH_LONG).show();
                } else {
                    Intent intent = new Intent(context, FindFarmerActivity.class);
                    intent.putExtra(BUYING_AGENT_CONTEXT, buyingAgentId);
                    intent.putExtra(TRANSACTION_MODE, true);
                    intent.putExtra(BACK_TO_FINDER, true);
                    context.startActivity(intent);
                }
            }
        });
    }
}
