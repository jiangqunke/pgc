package com.bestv.pgc.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bestv.pgc.R;
import com.bestv.pgc.beans.FunctionSpeedModel;
import com.bestv.pgc.beans.SpotBean;

import java.util.ArrayList;
import java.util.List;

public class SpeedAdapter extends RecyclerView.Adapter<SpeedAdapter.ViewHolder> {
    private Context mContext;
    private List<FunctionSpeedModel> mDatas = new ArrayList<>();
    private SpeedClickListenting listenting;
    public SpeedAdapter(Context context, List<FunctionSpeedModel> datas,SpeedClickListenting listenting) {
        mContext = context;
        mDatas = datas;
        this.listenting = listenting;
    }
    public void refreshData(List<FunctionSpeedModel> datas){
        mDatas = datas;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_speed_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull SpeedAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        FunctionSpeedModel data = mDatas.get(position);
        holder.tv_speed.setText(data.getName());
        if (data.isSelect()){
            holder.container.setBackground(ContextCompat.getDrawable(mContext,R.mipmap.icon_speed_select_bg));
        }else {
            holder.container.setBackground(ContextCompat.getDrawable(mContext,R.mipmap.icon_speed_bg));
        }
        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listenting != null){
                    listenting.speedSelect(position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tv_speed;
        private RelativeLayout container;

        public ViewHolder(View itemView) {
            super(itemView);
            tv_speed = itemView.findViewById(R.id.tv_speed);
            container = itemView.findViewById(R.id.container);
        }
    }

    public interface SpeedClickListenting {
        void speedSelect(int postion);
    }
}
