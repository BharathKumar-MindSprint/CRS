package com.olam.digital.ofis.agrisupplier.adapter;

import static com.olam.digital.ofis.mvvmmodule.view.diagnosticmodule.RecurrenceUtils.areDatesEqual;
import static com.olam.digital.ofis.mvvmmodule.view.diagnosticmodule.RecurrenceUtils.calculateRecurrenceCount;
import static com.olam.digital.ofis.mvvmmodule.view.diagnosticmodule.RecurrenceUtils.findLastAttendedDate;
import static com.olam.digital.ofis.mvvmmodule.view.diagnosticmodule.RecurrenceUtils.getNewEndDate;
import static com.olam.digital.ofis.mvvmmodule.view.diagnosticmodule.RecurrenceUtils.getNewStartDate;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.olam.digital.ofis.R;
import com.olam.digital.ofis.agrisupplier.dto.SupplierDto;
import com.olam.digital.ofis.agrisupplier.dto.SupplierSurveyDto;
import com.olam.digital.ofis.agrisupplier.listener.SupplierSurveyItemClickListener;
import com.olam.digital.ofis.mvvmmodule.roomdb.OFISRoomDB;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.DmRecurrence;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class SupplierSurveyListAdapter extends RecyclerView.Adapter<SupplierSurveyListAdapter.ViewHolder> {
    List<SupplierSurveyDto> supplierSurveys;
    private SupplierSurveyItemClickListener supplierSurveyItemClickListener;
    private OFISRoomDB ofisRoomDB;
    private Context context;
    private Resources res;
    private SupplierDto supplier;

    public SupplierSurveyListAdapter(List<SupplierSurveyDto> supplierSurveys, OFISRoomDB ofisRoomDB, Resources res, Context context, SupplierDto supplier) {
        this.supplierSurveys = supplierSurveys;
        this.ofisRoomDB = ofisRoomDB;
        this.res = res;
        this.context = context;
        this.supplier = supplier;
    }

    public void setOnItemClickListener(SupplierSurveyItemClickListener supplierSurveyItemClickListener) {
        this.supplierSurveyItemClickListener = supplierSurveyItemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.listview_module_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SupplierSurveyDto survey = supplierSurveys.get(position);
        holder.moduleTitle.setText(survey.getName());
        holder.textView.setText(survey.getDescription());

        if (survey.getModuleType().equals("9")) {
            DmRecurrence dmRecurrence = ofisRoomDB.dmRecurrenceDao().getRecurrenceByModuleId(String.valueOf(survey.getId()));
            String recurrenceType = dmRecurrence.recurrenceType;
            int recurrenceFrequency = dmRecurrence.recurrenceFrequency;

            Calendar calendar = Calendar.getInstance();
            Date currentDate = calendar.getTime();
            SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            inputDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

            Date startDateMain;
            try {
                startDateMain = inputDateFormat.parse(survey.getStartDate());
            } catch (ParseException e) {
                e.printStackTrace();
                startDateMain = null;
            }

            Date lastRecurrenceEndDate = null;
            if (startDateMain != null && recurrenceType != null) {
                try {
                    int recurrenceCount = calculateRecurrenceCount(survey.getStartDate(), recurrenceType, recurrenceFrequency);
                    Date newStartDate = getNewStartDate(survey.getStartDate(), recurrenceType, recurrenceFrequency, recurrenceCount);
                    Date newEndDate = getNewEndDate(newStartDate, recurrenceType, recurrenceFrequency);
                    lastRecurrenceEndDate = findLastAttendedDate(startDateMain, newEndDate, recurrenceType, recurrenceFrequency, recurrenceCount + 1);
                } catch (ParseException e) {
                    throw new RuntimeException("Error processing recurrence dates", e);
                }
            }

            if (lastRecurrenceEndDate != null && currentDate.after(lastRecurrenceEndDate) && !areDatesEqual(currentDate,startDateMain)){
                ofisRoomDB.dmSurveyAnswerTableDao().deleteAllDataForRecurrence(String.valueOf(survey.getId()) , String.valueOf(supplier.getId()), 0);
            }

            if(startDateMain != null && currentDate.after(startDateMain)) {
                holder.imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_status_new));
                holder.linearLayout.setBackgroundResource(R.drawable.list_item);
                holder.moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_base_font_dark));
                holder.textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_subtitle));
                holder.textView.setText(res.getString(R.string.supplier_survey));
            }
            else {
                holder.imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_status_new));
                holder.enabledItem.setText("false");
                holder.linearLayout.setBackgroundResource(R.drawable.list_item_disabled);
                holder.moduleTitle.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                holder.textView.setTextColor(ContextCompat.getColor(context, R.color.ofis_list_item_disabled_text));
                holder.textView.setText(res.getString(R.string.supplier_survey));
            }


            if (holder.enabledItem.getText().toString().equals("true")) {
                holder.linearLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        supplierSurveyItemClickListener.onItemClick(survey);
                    }
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        return supplierSurveys.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        LinearLayout linearLayout;
        ImageView imageView;
        TextView moduleTitle;
        TextView textView;
        TextView enabledItem;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            linearLayout = itemView.findViewById(R.id.rowlin);
            imageView = itemView.findViewById(R.id.moduleListModuleIcon);
            moduleTitle = itemView.findViewById(R.id.moduleListModuleName);
            textView = itemView.findViewById(R.id.moduleListModuleStatus);
            enabledItem = itemView.findViewById(R.id.moduleListEnabled);
        }
    }

}
