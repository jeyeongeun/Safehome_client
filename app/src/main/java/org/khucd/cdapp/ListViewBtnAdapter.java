package org.khucd.cdapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 * Created by YOUNGEUN on 2016-05-23.
 */
public class ListViewBtnAdapter extends ArrayAdapter implements View.OnClickListener{
    public interface ListBtnClickListener{
        void onListBtnClick(int position);
    }

    int resourceId;
    private ListBtnClickListener listBtnClickListener;

    ListViewBtnAdapter(Context context, int resource, ArrayList<ListViewItem> list, ListBtnClickListener clickListener){
        super(context, resource, list);
        this.resourceId = resource;
        this.listBtnClickListener = clickListener;
    }

    public View getView(int position, View convertView, ViewGroup parent){
        final int pos = position;
        final Context context = parent.getContext();

        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(this.resourceId, parent, false);

            final TextView titleTextView = (TextView) convertView.findViewById(R.id.titleForListview);
            final TextView subTextView = (TextView) convertView.findViewById(R.id.subForListView);
            final TextView subTextView2 = (TextView) convertView.findViewById(R.id.sub2ForListView);
            final ListViewItem listViewItem = (ListViewItem) getItem(position);

            titleTextView.setText(listViewItem.getTitleStr());
            subTextView.setText(listViewItem.getSubscription());
            subTextView2.setText(listViewItem.getSubscription2());

            ImageButton imageButton = (ImageButton) convertView.findViewById(R.id.imagebtn);
            imageButton.setTag(position);
            imageButton.setOnClickListener(this);

        }
        return convertView;
    }

    public void onClick(View v){
        if(this.listBtnClickListener != null){
            this.listBtnClickListener.onListBtnClick((int)v.getTag());
        }
    }
}
