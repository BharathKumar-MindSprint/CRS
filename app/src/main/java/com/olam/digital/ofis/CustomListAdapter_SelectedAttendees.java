package com.olam.digital.ofis;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import androidx.annotation.NonNull;

import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.google.firebase.crash.FirebaseCrash;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by ben on 06/07/2016.
 */
@SuppressWarnings("DefaultFileTemplate")
class CustomListAdapter_SelectedAttendees extends SimpleCursorAdapter {
    private final int layout;
    private final LayoutInflater inflater;
    private final Resources res;
    private final boolean readOnlyMode;
    private final TrainingRecord trainingRecord;
    private final JSONObject scoringColumns;
    private final String columnSuffix;
    private final  Context mContext;
    private DataLayer dl;
    private HashMap<Long,Integer> farmerAttendeeList;
    private long trainingRecordId;

    public CustomListAdapter_SelectedAttendees(Context c, Cursor cursor, String[] adapterCols, int[] adapterRowViews, Resources res_, boolean readOnlyMode_, TrainingRecord trainingRecord_, JSONObject scoringColumns_, String columnSuffix_,DataLayer dl_,HashMap<Long,Integer> farmerAttendeeList,long trainingRecordId) {
        super(c, R.layout.listview_selected_attendee_list, cursor, adapterCols, adapterRowViews, 0);

        this.layout = R.layout.listview_selected_attendee_list;
        this.inflater = LayoutInflater.from(c);
        this.res = res_;
        this.readOnlyMode = readOnlyMode_;
        this.trainingRecord = trainingRecord_;
        this.scoringColumns = scoringColumns_;
        this.columnSuffix = columnSuffix_;
        this.mContext=c;
        this.dl=dl_;
        this.farmerAttendeeList=farmerAttendeeList;
        this.trainingRecordId=trainingRecordId;
    }

    @Override
    public View newView (Context context, Cursor cursor, ViewGroup parent) {
        return inflater.inflate(layout, null);
    }

    @Override
    public void bindView(@NonNull View view, Context context, @NonNull Cursor cursor) {
        super.bindView(view, context, cursor);
        LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.rowlin);
        LinearLayout attendeesLayout=view.findViewById(R.id.addAttndeesLayout);
        ImageView imageView = (ImageView) view.findViewById(R.id.deleteItem);
        ImageView tvAddAttendee=(ImageView) view.findViewById(R.id.addTrainingAttendeesIv);
        TextView tvViewAttendees=(TextView) view.findViewById(R.id.tvViewAddendees);
        tvViewAttendees.setPaintFlags(tvViewAttendees.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        final TextView trainingAttendeeId = (TextView) view.findViewById(R.id.trainingAttendeeId);


        if (readOnlyMode) {
            linearLayout.setBackgroundResource(R.drawable.list_item_disabled);
            LinearLayout deleteItemWrapper = (LinearLayout) view.findViewById(R.id.deleteItemWrapper);
            deleteItemWrapper.setVisibility(View.GONE);
            LinearLayout itemTextContainer = (LinearLayout) view.findViewById(R.id.itemTextContainer);
            LinearLayout.LayoutParams llp = (LinearLayout.LayoutParams) itemTextContainer.getLayoutParams();
            llp.weight = 8;
        }

        // show and label the correct scoring columns
        if (scoringColumns!=null && scoringColumns.length() > 0) {
            int columnCount = 0;
            Iterator<String> columnKeys = scoringColumns.keys();
            while (columnKeys.hasNext()) {
                String key = columnKeys.next();
                try {
                    JSONObject value = scoringColumns.getJSONObject(key);
                    String label = value.optString("label" + columnSuffix, "label");
                    TextView scoreColumn = (TextView) view.findViewById(res.getIdentifier("criterion_" + key, "id", context.getPackageName()));

                    if (scoreColumn != null) {
                        if (columnCount == 0) {
                            scoreColumn.setBackgroundResource(0);
                            scoreColumn.setPadding(0, 0, 0, 0);
                            ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) scoreColumn.getLayoutParams();
                            mlp.setMargins(0, 0, 0, 0);
                        }

                        String output;
                        if (key.equalsIgnoreCase(OFISDBContract.TrainingAttendee.COLUMN_NAME_ATTENDED) || key.equalsIgnoreCase(OFISDBContract.TrainingAttendee.COLUMN_NAME_PASSED)) {
                            int criterionValue = (!cursor.isNull(cursor.getColumnIndex(key))) ? cursor.getInt(cursor.getColumnIndex(key)) : 0;
                            output = label + ": " + ((criterionValue > 0) ? res.getString(R.string.yes) : res.getString(R.string.no));
                        } else {
                            String criterionValue = (!cursor.isNull(cursor.getColumnIndex(key))) ? cursor.getString(cursor.getColumnIndex(key)) : "";
                            output = label + ": " + criterionValue;
                        }
                        scoreColumn.setText(output);
                        scoreColumn.setVisibility(View.VISIBLE);
                    }

                    columnCount++;
                } catch (JSONException e) {
                    FirebaseCrash.report(new Exception("CustomListAdapter_SelectedAttendees: Error setting score column values", e));
                    e.printStackTrace();
                }
            }
        }

        if (trainingRecord != null) {

            // View Attendees logic
            if (TrainingRecord.isCaptureAttendee && !readOnlyMode) {
                long id = dl.getFarmerByAttendeeItem(Integer.parseInt(String.valueOf(trainingAttendeeId.getText())));
                if (farmerAttendeeList.get(id) > 0) {
                    tvViewAttendees.setText((context.getResources().getString(R.string.view_attendees_count)).replace("~V", String.valueOf(farmerAttendeeList.get(id))));
                    tvViewAttendees.setVisibility(View.VISIBLE);
                } else {
                    tvViewAttendees.setVisibility(View.INVISIBLE);
                }

                attendeesLayout.setOnClickListener(view1 -> {

                    int farmerId = dl.getFarmerByAttendeeItem(Integer.parseInt(String.valueOf(trainingAttendeeId.getText())));
                    long trainingRecordId = trainingRecord.trainingRecordId;
                    Intent addIntent = new Intent(mContext, RegisterAdditionalTrainee.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("traineeRecordId", String.valueOf(trainingRecordId));
                    bundle.putString("farmerId", String.valueOf(farmerId));
                    bundle.putBoolean("isEdit", true);
                    addIntent.putExtras(bundle);
                    mContext.startActivity(addIntent);
                });

                tvViewAttendees.setOnClickListener(view12 -> {
                    int farmerId = dl.getFarmerByAttendeeItem(Integer.parseInt(String.valueOf(trainingAttendeeId.getText())));
                    long trainingRecordId = trainingRecord.trainingRecordId;
                    Intent addIntent = new Intent(mContext, RegisterAdditionalTrainee.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("traineeRecordId", String.valueOf(trainingRecordId));
                    bundle.putString("farmerId", String.valueOf(farmerId));
                    bundle.putBoolean("isEdit", false);
                    addIntent.putExtras(bundle);
                    mContext.startActivity(addIntent);
                });
            }else{
                attendeesLayout.setVisibility(View.INVISIBLE);
                tvViewAttendees.setVisibility(View.INVISIBLE);
            }
            // End

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int itemId = Integer.parseInt(String.valueOf(trainingAttendeeId.getText()));
                    trainingRecord.deleteTrainingAttendee(itemId);
                }
            });
        } else  {
            // Confirmation screen
            // View Attendees logic
//            long id = dl.getFarmerByAttendeeItem(Integer.parseInt(String.valueOf(trainingAttendeeId.getText())));
//            if (farmerAttendeeList.get(id)>0) {
//                tvViewAttendees.setText((context.getResources().getString(R.string.view_attendees_count)).replace("~V", String.valueOf(farmerAttendeeList.get(id))));
//                tvViewAttendees.setVisibility(View.VISIBLE);
//            } else{
//                tvViewAttendees.setVisibility(View.INVISIBLE);
//            }
//            tvViewAttendees.setOnClickListener(view12 -> {
//                int farmerId = dl.getFarmerByAttendeeItem(Integer.parseInt(String.valueOf(trainingAttendeeId.getText())));
//                Intent addIntent =new Intent(mContext, RegisterAdditionalTrainee.class);
//                Bundle bundle = new Bundle();
//                bundle.putString("traineeRecordId", String.valueOf(trainingRecordId));
//                bundle.putString("farmerId", String.valueOf(farmerId));
//                bundle.putBoolean("isEdit",false);
//                addIntent.putExtras(bundle);
//                mContext.startActivity(addIntent);
//            });

            tvViewAttendees.setVisibility(View.GONE);
            attendeesLayout.setVisibility(View.GONE);


            linearLayout.setBackgroundResource(0);
            LinearLayout deleteItemWrapper = (LinearLayout) view.findViewById(R.id.deleteItemWrapper);
            deleteItemWrapper.setVisibility(View.GONE);
            LinearLayout itemTextContainer = (LinearLayout) view.findViewById(R.id.itemTextContainer);
            LinearLayout.LayoutParams llp = (LinearLayout.LayoutParams) itemTextContainer.getLayoutParams();
            llp.weight = 8;
            llp.setMargins(0,8,0,8);
            itemTextContainer.setPadding(0,0,0,0);
        }

    }
}
