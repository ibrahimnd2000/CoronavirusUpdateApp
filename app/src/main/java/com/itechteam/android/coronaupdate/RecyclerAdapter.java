package com.itechteam.android.coronaupdate;

import android.content.Context;
import android.content.Intent;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {
    private Context context;
    private List<Summary.Countries> countryList;
    private Summary.Countries selectedCountry;
    private List<Summary.Countries> temp;

    public RecyclerAdapter(Context context, List<Summary.Countries> countryList) {
        this.context = context;
        this.countryList = countryList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_summary, viewGroup, false);
        temp = new ArrayList<>();
        temp.addAll(countryList);
        return new ViewHolder(view, context);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        viewHolder.countryNameTextView.setText(countryList.get(i).getCountry());
        DecimalFormat formatter = new DecimalFormat("#,###,###");
        String totalConfirmed = formatter.format(countryList.get(i).getTotalConfirmed());
        viewHolder.totalCaseTextView.setText(totalConfirmed);
    }

    @Override
    public int getItemCount() {
        return countryList.size();
    }

    public void filter(String text) {
        countryList.clear();
        if (text.isEmpty()) {
            countryList.addAll(temp);
        } else {
            text = text.toLowerCase();
            for (Summary.Countries countries : temp) {
                if (countries.getCountry().toLowerCase().contains(text)){
                    countryList.add(countries);
                }
            }
        }
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView countryNameTextView;
        private TextView totalCaseTextView;
        private CardView cardView;
        private Context context;

        public ViewHolder(@NonNull View itemView, Context context) {
            super(itemView);
            this.context = context;
            this.countryNameTextView = itemView.findViewById(R.id.countryNameTextView);
            this.totalCaseTextView = itemView.findViewById(R.id.totalCaseTextView);
            this.cardView = itemView.findViewById(R.id.cardView);

            itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();

                    if(pos != RecyclerView.NO_POSITION) {
                        selectedCountry = countryList.get(pos);
                        Intent intent = new Intent(context, DetailActivity.class);
                        intent.putExtra("selectedCountry", (Serializable) selectedCountry);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    }
                }
            });

        }
    }


}
