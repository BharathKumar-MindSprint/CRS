package com.olam.digital.ofis;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.olam.digital.crs.model.surveysubmit.Submission;
import com.olam.digital.crs.room.RespondentEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by ben on 07/12/14.
 * Modified by Bharath/Helper: supports extra "Submission" + "RespondentEntity" rows + cursor rows.
 */
class CustomListAdapter_NetworkQueue extends SimpleCursorAdapter {

    // --- View types ---
    private static final int TYPE_SUBMISSION = 0;
    private static final int TYPE_RESPONDENT = 1;
    private static final int TYPE_CURSOR_ROW = 2;
    private static final int TYPE_COUNT = 3;

    private final int layout;
    private final LayoutInflater inflater;
    private final HomeActivity homeActivity;
    public boolean isClickable;
    public Resources res;

    // Extra rows
    private List<Submission> submissions = Collections.emptyList();
    private List<RespondentEntity> respondents = Collections.emptyList();

    private final Context context;

    // Controls where extra rows appear
    // true  => [submissions][respondents][cursor]
    // false => [cursor][respondents][submissions]
    private boolean showSubmissionsFirst = true;

    public CustomListAdapter_NetworkQueue(
            Context c,
            Cursor cursor,
            String[] adapterCols,
            int[] adapterRowViews,
            HomeActivity ha,
            boolean isClickable
    ) {
        super(c, R.layout.listview_network_queue, cursor, adapterCols, adapterRowViews, 0);
        this.layout = R.layout.listview_network_queue;
        this.inflater = LayoutInflater.from(c);
        this.homeActivity = ha;
        this.isClickable = isClickable;
        this.res = c.getResources();
        this.context = c;
    }


    public List<Submission> getSubmissions() { return submissions; }
    public List<RespondentEntity> getSubmissionRespondent() { return respondents; }


    /** Supply/refresh submission rows (call from your async callback). */
    public void setSubmissions(@NonNull List<Submission> list) {
        this.submissions = new ArrayList<>(list);
        notifyDataSetChanged();
    }

    /** Supply/refresh respondent rows (call from your async callback). */
    public void setSubmissionRespondent(@NonNull List<RespondentEntity> respondentEntities) {
        this.respondents = new ArrayList<>(respondentEntities);
        notifyDataSetChanged();
    }

    /** Optional: clear submissions */
    public void clearSubmissions() {
        this.submissions = Collections.emptyList();
        notifyDataSetChanged();
    }

    /** Optional: control whether submissions render before or after cursor rows. */
    public void setShowSubmissionsFirst(boolean showFirst) {
        this.showSubmissionsFirst = showFirst;
        notifyDataSetChanged();
    }

    // ---- Adapter plumbing (heterogeneous rows) ----

    @Override
    public int getViewTypeCount() {
        return TYPE_COUNT;
    }

   /* @Override
    public int getItemViewType(int position) {
        int subCount = submissions.size();
        int respCount = respondents.size();
        int curCount = (getCursor() != null && !getCursor().isClosed()) ? getCursor().getCount() : 0;

        if (showSubmissionsFirst) {
            // [submissions][respondents][cursor]
            if (position < subCount) return TYPE_SUBMISSION;
            if (position < subCount + respCount) return TYPE_RESPONDENT;
            return TYPE_CURSOR_ROW;
        } else {
            // [cursor][respondents][submissions]
            if (position < curCount) return TYPE_CURSOR_ROW;
            if (position < curCount + respCount) return TYPE_RESPONDENT;
            return TYPE_SUBMISSION;
        }
    }*/

    @Override
    public int getItemViewType(int position) {
        int respCount = (respondents != null) ? respondents.size() : 0;
        int subCount  = (submissions  != null) ? submissions.size()  : 0;
        int curCount  = (getCursor() != null && !getCursor().isClosed()) ? getCursor().getCount() : 0;

        // Fixed order: [respondents][cursor][submissions]
        if (position < respCount) return TYPE_RESPONDENT;
        if (position < respCount + curCount) return TYPE_CURSOR_ROW;
        return TYPE_SUBMISSION;
    }

    @Override
    public int getCount() {
        int cursorCount = (getCursor() != null && !getCursor().isClosed()) ? getCursor().getCount() : 0;
        int submissionCount = (submissions != null) ? submissions.size() : 0;
        int respondentCount = (respondents != null) ? respondents.size() : 0;
        return cursorCount + submissionCount + respondentCount;
    }

    @Override
    public Object getItem(int position) {
        int type = getItemViewType(position);
        if (type == TYPE_SUBMISSION) {
            int index = submissionIndexForPosition(position);
            return submissions.get(index);
        } else if (type == TYPE_RESPONDENT) {
            int index = respondentIndexForPosition(position);
            return respondents.get(index);
        } else {
            Cursor c = getCursor();
            int cursorPos = cursorIndexForPosition(position);
            if (c != null && c.moveToPosition(cursorPos)) {
                return c;
            }
            return null;
        }
    }

    @Override
    public long getItemId(int position) {
        int type = getItemViewType(position);
        if (type == TYPE_SUBMISSION) {
            // stable negative ids for submission rows
            return -1000L - submissionIndexForPosition(position);
        } else if (type == TYPE_RESPONDENT) {
            // stable negative ids for respondent rows
            return -2000L - respondentIndexForPosition(position);
        } else {
            return super.getItemId(cursorIndexForPosition(position));
        }
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    /** Compute submission index given adapter position */
    private int submissionIndexForPosition(int position) {
        int respCount = (respondents != null) ? respondents.size() : 0;
        int curCount  = (getCursor() != null && !getCursor().isClosed()) ? getCursor().getCount() : 0;
        // [respondents][cursor][submissions]
        // Submissions start after respondents + cursor block
        return position - respCount - curCount;
    }

    /** Compute respondent index given adapter position */
    private int respondentIndexForPosition(int position) {
        return position;
    }

    /** Compute cursor index given adapter position */
    private int cursorIndexForPosition(int position) {
        int respCount = (respondents != null) ? respondents.size() : 0;
        // [respondents][cursor][submissions]
        // Cursor rows come after respondents
        return position - respCount;
    }

    // ---- Rendering ----
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int type = getItemViewType(position);

        if (type == TYPE_SUBMISSION) {
            // Inflate/bind a submission row
            SubmissionVH holder;
            View view;
            if (convertView == null || !(convertView.getTag() instanceof SubmissionVH)) {
                view = inflater.inflate(R.layout.row_submission_item, parent, false);
                holder = new SubmissionVH(view);
                view.setTag(holder);
            } else {
                view = convertView;
                holder = (SubmissionVH) view.getTag();
            }

            Submission item = submissions.get(submissionIndexForPosition(position));

            // ---- Bind submission fields ----
            holder.title.setText(context.getString(R.string.survey_name) + " " + item.getSurveyName());
            holder.subtitle.setText(
                    context.getString(R.string.module_name) + " " + item.getModuleId()
                            + " • " + ((item.isCrsSurvey())?  context.getString(R.string.respondent_id): context.getString(R.string.farmer)) + ": " + item.getOlamFarmerId()
            );
            holder.date.setText(String.valueOf(item.getSubmissionDate())); // format if needed

            holder.icon.setImageDrawable(ContextCompat.getDrawable(parent.getContext(),
                    isClickable ? R.drawable.ic_upload_list : R.drawable.ic_icon_sync_black));
            holder.icon.setEnabled(isClickable);

            holder.icon.setOnClickListener(v -> homeActivity.startSubmissionUpload(item));

            return view;
        }

        if (type == TYPE_RESPONDENT) {
            // Inflate/bind a respondent row
            RespondentVH holder;
            View view;
            if (convertView == null || !(convertView.getTag() instanceof RespondentVH)) {
                view = inflater.inflate(R.layout.respondent_item, parent, false);
                holder = new RespondentVH(view);
                view.setTag(holder);
            } else {
                view = convertView;
                holder = (RespondentVH) view.getTag();
            }

            int respIndex = respondentIndexForPosition(position);
            RespondentEntity r = respondents.get(respIndex);
            holder.bind(r);

            // You can also handle clicks:
             holder.icon.setOnClickListener(v -> {
                    // Example: start respondent details activity
                    homeActivity.showRespondentDetails(r);
             });

            return view;
        }

        // Cursor row: bind using overridden newView/bindView with adjusted index
        int cursorPos = cursorIndexForPosition(position);
        Cursor c = getCursor();
        if (c == null || c.isClosed() || !c.moveToPosition(cursorPos)) {
            return (convertView != null) ? convertView
                    : inflater.inflate(layout, parent, false);
        }

        View v;
        if (convertView == null
                || (convertView.getTag() instanceof SubmissionVH)
                || (convertView.getTag() instanceof RespondentVH)) {
            v = newView(context, c, parent); // inflate layout for cursor row
            v.setTag(null); // mark as cursor row
        } else {
            v = convertView;
        }

        bindView(v, context, c);
        return v;
    }

    // ---- Keep your original cursor row inflation/binding ----
    @Override
    public View newView(Context context, final Cursor cursor, ViewGroup parent) {
        return inflater.inflate(layout, parent, false);
    }

    @Override
    public void bindView(@NonNull View view, final Context context, @NonNull final Cursor cursor) {
        super.bindView(view, context, cursor);

        ImageView imageView = view.findViewById(R.id.imageView_pic);
        final TextView networkQueueId = view.findViewById(R.id.networkQueueId);

        if (isClickable) {
            imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_upload_list));
            imageView.setEnabled(true);
        } else {
            imageView.setEnabled(false);
            imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_icon_sync_black));
        }

        String itemType = cursor.getString(cursor.getColumnIndexOrThrow(
                OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SUBMISSION_TYPE));
        String subType = cursor.getString(cursor.getColumnIndexOrThrow(
                OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_SUBGROUP_TYPE));

        if ("farmpolygon".equalsIgnoreCase(itemType)
                && (subType != null && subType.equalsIgnoreCase(res.getString(R.string.flag_for_de_activation)))) {
            TextView tvSubGroupType = view.findViewById(R.id.tvSubGroupType);
            tvSubGroupType.setVisibility(View.VISIBLE);
            tvSubGroupType.setText(subType);
        }

        imageView.setOnClickListener(v -> {
            int itemId = Integer.parseInt(String.valueOf(networkQueueId.getText()));
            homeActivity.startItemSyncAuth(itemId);
        });
    }

    // ---- ViewHolder for submission row ----
    static class SubmissionVH {
        final TextView title;
        final TextView subtitle;
        final TextView date;
        final ImageView icon;

        SubmissionVH(View v) {
            title = v.findViewById(R.id.tvSubmissionTitle);
            subtitle = v.findViewById(R.id.tvSubmissionSubtitle);
            date = v.findViewById(R.id.tvSubmissionDate);
            icon = v.findViewById(R.id.imageView_pic);
        }
    }

    // ---- ViewHolder for respondent row ----
    static class RespondentVH {
        final ShapeableImageView avatar;
        final TextView title;
        final TextView phone;
        final ImageView icon;
        RespondentVH(View v) {
            avatar = v.findViewById(R.id.imgAvatar);
            title  = v.findViewById(R.id.txtName);
            phone  = v.findViewById(R.id.txtPhone);
            icon  = v.findViewById(R.id.imageView_pic);
        }

        void bind(RespondentEntity r) {
            String name = (r.getName() != null && !r.getName().isEmpty()) ? r.getName() : "Unknown";
            title.setText(name);

            String dial = (r.getCountryCode() != null) ? r.getCountryCode() : "";
            String num  = (r.getPhoneNumber() != null) ? r.getPhoneNumber() : "";
            phone.setText((dial + " " + num).trim());

            String photo = r.getPhoto();
            if (photo != null && !photo.isEmpty()) {
                Glide.with(avatar.getContext())
                        .load(photo)
                        .placeholder(R.drawable.image_circle_red)
                        .error(R.drawable.image_circle_red)
                        .into(avatar);
            } else {
                avatar.setImageResource(R.drawable.image_circle_red);
            }
        }
    }

    // ---- small null-safe getter helper (Java 8 style) ----
    private static <T> T safe(SupplierEx<T> call, T fallback) {
        try {
            T v = call.get();
            return v != null ? v : fallback;
        } catch (Throwable t) {
            return fallback;
        }
    }

    private interface SupplierEx<T> {
        T get() throws Exception;
    }
}