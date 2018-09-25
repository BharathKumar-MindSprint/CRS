package com.webpoint0.ofiscompanion;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.support.annotation.NonNull;
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

    public CustomListAdapter_SelectedAttendees(Context c, Cursor cursor, String[] adapterCols, int[] adapterRowViews, Resources res_, boolean readOnlyMode_, TrainingRecord trainingRecord_, JSONObject scoringColumns_, String columnSuffix_) {
        super(c, R.layout.listview_selected_attendee_list, cursor, adapterCols, adapterRowViews, 0);
        this.layout = R.layout.listview_selected_attendee_list;
        this.inflater = LayoutInflater.from(c);
        this.res = res_;
        this.readOnlyMode = readOnlyMode_;
        this.trainingRecord = trainingRecord_;
        this.scoringColumns = scoringColumns_;
        this.columnSuffix = columnSuffix_;
    }

    @Override
    public View newView (Context context, Cursor cursor, ViewGroup parent) {
        return inflater.inflate(layout, null);
    }

    @Override
    public void bindView(@NonNull View view, Context context, @NonNull Cursor cursor) {
        super.bindView(view, context, cursor);
        LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.rowlin);
        ImageView imageView = (ImageView) view.findViewById(R.id.deleteItem);
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
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int itemId = Integer.parseInt(String.valueOf(trainingAttendeeId.getText()));
                    trainingRecord.deleteTrainingAttendee(itemId);
                }
            });
        } else  {
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
