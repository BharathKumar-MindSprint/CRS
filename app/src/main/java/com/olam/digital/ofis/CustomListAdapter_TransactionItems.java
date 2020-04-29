package com.olam.digital.ofis;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

/**
 * Created by ben on 01/07/2016.
 */
@SuppressWarnings("DefaultFileTemplate")
class CustomListAdapter_TransactionItems extends SimpleCursorAdapter  {
    private final int layout;
    private final LayoutInflater inflater;
    private final Resources res;
    private final TransactionItems TransactionItems;

    public CustomListAdapter_TransactionItems(Context c, Cursor cursor, String[] adapterCols, int[] adapterRowViews, Resources res_, TransactionItems TransactionItems) {
        super(c, R.layout.listview_transactionitems_list, cursor, adapterCols, adapterRowViews, 0);
        this.layout = R.layout.listview_transactionitems_list;
        this.inflater = LayoutInflater.from(c);
        this.res = res_;
        this.TransactionItems = TransactionItems;
    }

    @Override
    public View newView (Context context, final Cursor cursor, ViewGroup parent) {
        return inflater.inflate(layout, null);
    }

    @Override
    public void bindView(@NonNull View view, final Context context, @NonNull final Cursor cursor) {
        super.bindView(view, context, cursor);
        LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.rowlin);
        ImageView imageView = (ImageView) view.findViewById(R.id.deleteTransactionItem);
        final TextView transactionItemId = (TextView) view.findViewById(R.id.transactionItemId);
        TextView status = (TextView) view.findViewById(R.id.inNetworkQueue);

        if (cursor.getInt(cursor.getColumnIndex(OFISDBContract.TransactionItem.COLUMN_NAME_IN_QUEUE)) == 1) {
            status.setText(res.getString(R.string.transaction_confirmed));
            linearLayout.setBackgroundResource(R.drawable.list_item_disabled);
        } else {
            status.setText(res.getString(R.string.item_in_progress));
            linearLayout.setBackgroundResource(R.drawable.list_item);
        }

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Long itemId = Long.parseLong(String.valueOf(transactionItemId.getText()));
                TransactionItems.deleteTransactionItem(itemId);
            }
        });
    }
}
