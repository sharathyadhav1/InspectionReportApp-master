package com.dolabs.emircom.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;


import com.dolabs.emircom.R;
import com.dolabs.emircom.databinding.LayoutBannerListItemBinding;
import com.dolabs.emircom.handlers.AppInterface;
import com.dolabs.emircom.session.SessionContext;

import java.util.List;

public class BannerListAdapter extends RecyclerView.Adapter<BannerListAdapter.ViewHolder> {

    private final Context mContext;
    private final AppInterface appInterface;
    private final List<String> bannerItemsList;

    private final LayoutInflater mInflater;
    private final SessionContext sessionContext;

    public BannerListAdapter(Context context, List<String> bannerItemsList, AppInterface appInterface) {

        this.mContext = context;
        this.bannerItemsList = bannerItemsList;
        this.appInterface = appInterface;

        this.mInflater = LayoutInflater.from(mContext);
        this.sessionContext = SessionContext.getInstance();
    }

    @Override @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutBannerListItemBinding binding = DataBindingUtil.inflate(mInflater, R.layout.layout_banner_list_item, parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        try {

            String bannerItem = bannerItemsList.get(position);
            holder.binding.bannerItemTv.setText(bannerItem);

            holder.itemView.setOnClickListener(v -> {

                if(appInterface != null) {
                    appInterface.onCallback(holder.getAdapterPosition(), bannerItem);
                }
            });
        }
        catch (Exception e) {

            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {

        int count = 0;

        if(bannerItemsList != null)
            count = bannerItemsList.size();

        return count;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public LayoutBannerListItemBinding binding;

        ViewHolder(LayoutBannerListItemBinding binding) {

            super(binding.getRoot());

            this.binding = binding;
        }
    }
}