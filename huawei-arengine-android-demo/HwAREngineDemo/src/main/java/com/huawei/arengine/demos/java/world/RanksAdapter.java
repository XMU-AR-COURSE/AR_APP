package com.huawei.arengine.demos.java.world;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.huawei.arengine.demos.R;

import java.util.List;

public class RanksAdapter extends RecyclerView.Adapter<RanksAdapter.ViewHolder>  {
    private List<Ranks> ransk_list;
    Context mContext;
    static  class ViewHolder extends RecyclerView.ViewHolder {
        TextView rank,id,score,time;
        View rankview;
        //内部类，绑定控件
        public ViewHolder(View view) {
            super(view);
            rankview = view;
            rank = view.findViewById(R.id._rank);
            id = view.findViewById(R.id._id);
            score = view.findViewById(R.id._scores);
            time = view.findViewById(R.id._time);
        }
    }
    public RanksAdapter(List<Ranks> List, Context activity){
        ransk_list=List;
        mContext=activity;
    }
    //创建ViewHolder，返回每一项的布局
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rank_item,parent,false);
        final ViewHolder holder = new ViewHolder(view);
        return holder;
    }
    //将数据和控件绑定
    @Override
    public void onBindViewHolder(ViewHolder holder,int position) {
        Ranks r=ransk_list.get(position);
        holder.rank.setText(r.getRank());
        holder.id.setText(r.getId());
        holder.score.setText(r.getScore());
        holder.time.setText(r.getTime());
    }
    //返回Item总条数
    public int getItemCount(){
        return ransk_list.size();
    }
}