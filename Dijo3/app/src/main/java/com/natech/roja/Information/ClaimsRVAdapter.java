package com.natech.roja.Information;

import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.natech.roja.R;

import java.util.List;

/**
 * Created by Tshepo on 2015/10/30.
 */
public class ClaimsRVAdapter extends RecyclerView.Adapter<ClaimsRVAdapter.ClaimsHolder> {

    List<Points> pointsList;
    int points;

    public ClaimsRVAdapter(List<Points> pointsList,int points){
        this.pointsList = pointsList;
        this.points = points;

    }
    @Override
    public ClaimsHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ClaimsHolder(LayoutInflater.from(parent.getContext()).
                inflate(R.layout.claims_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ClaimsHolder holder, int position) {
            holder.claimsTV.setText(pointsList.get(position).getClaims()+" ~ "+pointsList.get(position).getPoints()+" points");
    }

    @Override
    public int getItemCount() {
        return pointsList.size();
    }

    class ClaimsHolder extends RecyclerView.ViewHolder{

        final TextView claimsTV;
        final Button claimsBTN;

        public ClaimsHolder(View itemView){
            super(itemView);

            claimsTV = (TextView)itemView.findViewById(R.id.claimTV);
            claimsBTN = (Button)itemView.findViewById(R.id.claimsBTN);

            claimsBTN.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(points >= pointsList.get(getAdapterPosition()).getPoints()){
                        ClaimsActivity.claimsActivity.showClaimDialog(pointsList.
                                get(getAdapterPosition()).getPoints(),
                                pointsList.get(getAdapterPosition()).getRestPointID(),pointsList.
                                        get(getAdapterPosition()).getClaims());
                    }else
                        showSnackBar("You have insufficient points to make this claim");
                }
            });
        }
    }

    void showSnackBar(final String message){
        Snackbar.make(ClaimsActivity.claimsActivity.findViewById(R.id.snackbarPosition),
                message, Snackbar.LENGTH_LONG).show();

    }
}
