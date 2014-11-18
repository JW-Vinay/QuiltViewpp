
package com.quiltview.adapters;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.CardScrollAdapter;

public class QueryScrollAdapter extends CardScrollAdapter {
    @SuppressWarnings("unused")
    private Context mContext;
    private List<CardBuilder> mCardsList;

    public QueryScrollAdapter(Context context, List<CardBuilder> cardsList) {
        super();

        this.mContext = context;
        this.mCardsList = cardsList;

    }

    @Override
    public int getCount() {

        return mCardsList.size();
    }

    @Override
    public Object getItem(int position) {

        return mCardsList.get(position);
    }

    @Override
    public int getPosition(Object object) {
        return mCardsList.indexOf(object);
    }

    @Override
    public int getViewTypeCount() 
    {
        return CardBuilder.getViewTypeCount();
    }
    
    @Override
    public int getItemViewType(int position) {

        return mCardsList.get(position).getItemViewType();
    }
    
    @Override
    public View getView(int position, View view, ViewGroup parent) 
    {
        return mCardsList.get(position).getView();
    }

}
