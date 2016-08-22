package com.natech.roja.Restaurants;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

/**
 * Created by Sello on 2015/07/26.
 */
@SuppressWarnings("DefaultFileTemplate")
public abstract class EndlessRecyclerScroll extends RecyclerView.OnScrollListener {

    public static String TAG = EndlessRecyclerScroll.class.getSimpleName();
    private int previousTotal = 0;
    private Boolean loading = true;
    private int current_page = 1;
    private final LinearLayoutManager linearLayoutManager;

    public EndlessRecyclerScroll(LinearLayoutManager linearLayoutManager){
        this.linearLayoutManager  = linearLayoutManager;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy){
        super.onScrolled(recyclerView,dx,dy);
        int visibleItemCount = recyclerView.getChildCount();
        int totalItemCount = linearLayoutManager.getItemCount();
        int firstVisibleItem = linearLayoutManager.findFirstVisibleItemPosition();

        if(loading){
            if(totalItemCount > previousTotal){
                loading = false;
                previousTotal = totalItemCount;
            }
        }
        int visibleThreshold = 15;
        if (!loading
                && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
            current_page++;

            onLoadMore(current_page);

            loading = true;
        }
    }
    public abstract void onLoadMore(int current_page);
}
