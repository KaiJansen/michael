package com.insurance.easycover.shared.ui.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.insurance.easycover.AppSession;
import com.insurance.easycover.R;
import com.insurance.easycover.data.events.EventsIds;
import com.insurance.easycover.data.events.ListDataEvent;
import com.insurance.easycover.data.events.SingleDataEvent;
import com.insurance.easycover.data.models.response.HandOverData;
import com.insurance.easycover.data.models.response.RenewData;
import com.insurance.easycover.data.models.response.ResponseCompletedJobs;
import com.insurance.easycover.data.models.response.ResponseGetQuotation;
import com.insurance.easycover.data.models.response.ResponseHandOverData;
import com.insurance.easycover.data.models.response.ResponseOrderHistory;
import com.insurance.easycover.data.models.response.assignJob.JobAssignJob;
import com.insurance.easycover.data.network.NetworkController;
import com.insurance.easycover.shared.Utils.DownLoadImageTask;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;
import naveed.khakhrani.miscellaneous.base.RecyclerBaseAdapter;
import naveed.khakhrani.miscellaneous.listeners.RecyclerViewItemSelectedListener;
import naveed.khakhrani.miscellaneous.util.AppButton;
import naveed.khakhrani.miscellaneous.util.Dummy;

import static com.insurance.easycover.AppClass.getContext;
import static com.insurance.easycover.data.events.EventsIds.ID_HANDOVER;

/**
 * Created by naveedali on 10/10/17.
 */

public class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.ViewHolder>{

    private Context mCtx;
    private List<ResponseCompletedJobs> quotationList;

    protected RecyclerViewItemSelectedListener recyclerViewItemSelectedListener;


    public RecyclerViewItemSelectedListener getRecyclerViewItemSelectedListener() {
        return recyclerViewItemSelectedListener;
    }

    public void setRecyclerViewItemSelectedListener(RecyclerViewItemSelectedListener recyclerViewItemSelectedListener) {
        this.recyclerViewItemSelectedListener = recyclerViewItemSelectedListener;
    }

    public OrderHistoryAdapter(Context context, List<ResponseCompletedJobs> data) {
        this.mCtx = context;
        this.quotationList = data;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout.item_qoutation, null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final ResponseCompletedJobs quot;
        quot = quotationList.get(position);

        holder.layoutRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (recyclerViewItemSelectedListener != null) {
                    recyclerViewItemSelectedListener.onItemSelected(quotationList.get(position), position, 0);
                } else
                    Toast.makeText(mCtx, "Work In progress", Toast.LENGTH_SHORT).show();
            }
        });

        holder.btnRenew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RenewData renewdata = new RenewData();
                renewdata.jobid = quot.getJobId();
                renewdata.agentId = quot.getAgentId();
                NetworkController.getInstance().reNewData(renewdata);
            }
        });

        //holder.tvInsurance.setText(quot.getInsuranceType());
        holder.rating.setVisibility(View.GONE);
        holder.tvName.setText(quot.getUsername());
        if (quot.getLanguage().toString().equals("Select Language")) {
            holder.tvLanguage.setText("None");
        } else {
            holder.tvLanguage.setText(quot.getLanguage());
        }
        //holder.tvLanguage.setText(AppSharedPreferences.getInstance(mCtx).getCurrentLanguage());
        holder.tvQuotPrice.setText(quot.getQuotationPrice());
        holder.tvCountry.setText(quot.getCountry());
        if (quot.getImage() != null) {
            if (!quot.getImage().equals("null")) {
                new DownLoadImageTask(holder.imvProfile).execute(quot.getImage());
            }
        }
        String dtStart = quot.getExpiredDate();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //holder.btnRenew.setVisibility(View.VISIBLE);//MUST DELETE
        try {
            Date date = format.parse(dtStart.trim());
            format.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date now = Calendar.getInstance().getTime();
            long diff = date.getTime() - now.getTime();
            long diffDays = diff / (24 * 60 * 60 * 1000);
            if ((diffDays > 0) && (diffDays < 30)) {
                holder.btnRenew.setVisibility(View.VISIBLE);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        dtStart = quot.getUpdatedAt();
        try {
            Date date = format.parse(dtStart.trim());
            format.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date now = Calendar.getInstance().getTime();
            long diff = now.getTime() - date.getTime();
            String SinceDate = String.valueOf("Since ");
            long diffDays = diff / (24 * 60 * 60 * 1000);
            if (diffDays > 1) SinceDate += String.valueOf(diffDays) + " days ";
            if (diffDays == 1) SinceDate += String.valueOf(diffDays) + " day ";
            long diffHour = (diff - (diffDays * 24 * 60 * 60 * 1000)) / ( 60 * 60 * 1000 );
            if (diffHour > 1) SinceDate += String.valueOf(diffHour) + " hours ";
            if (diffHour == 1) SinceDate += String.valueOf(diffHour) + " hour ";
            long diffMins = (diff - (diffDays * 24 * 60 * 60 * 1000) - (diffHour * 60 * 60 * 1000)) / ( 60 * 1000 );
            if (diffMins > 1) SinceDate += String.valueOf(diffMins) + " minutes ";
            if (diffMins == 1) SinceDate += String.valueOf(diffMins) + " minute ";
            if (SinceDate.equals("Since ")) {
                SinceDate += "less then 1 minute";
            }
            holder.tvDate.setText(SinceDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return quotationList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        //TextView tvInsurance;
        TextView tvName;
        TextView tvQuotPrice;
        TextView tvCountry;
        AppButton btnRenew;
        TextView tvDate;
        LinearLayout rating;
        ImageView imvProfile;
        TextView tvLanguage;


        @BindView(R.id.layoutRoot)
        public RelativeLayout layoutRoot;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            //tvInsurance = itemView.findViewById(R.id.tvInsurance);
            tvName = itemView.findViewById(R.id.tvName);
            tvQuotPrice = itemView.findViewById(R.id.tvQuotPrice);
            tvCountry = itemView.findViewById(R.id.tvCountry);
            btnRenew = itemView.findViewById(R.id.btnRenew);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvLanguage = itemView.findViewById(R.id.tvLanguage);
            imvProfile = itemView.findViewById(R.id.imvUser);
            rating = itemView.findViewById(R.id.rating);
        }
    }
}
