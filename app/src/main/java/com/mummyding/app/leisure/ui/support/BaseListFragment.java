package com.mummyding.app.leisure.ui.support;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.mummyding.app.leisure.LeisureApplication;
import com.mummyding.app.leisure.R;
import com.mummyding.app.leisure.support.CONSTANT;
import com.mummyding.app.leisure.support.Utils;
import com.yalantis.phoenix.PullToRefreshView;

/**
 * Created by mummyding on 15-12-3.
 */
public abstract class BaseListFragment extends Fragment{

    protected View parentView;
    protected RecyclerView recyclerView;
    protected RecyclerView.LayoutManager mLayoutgManager;
    protected PullToRefreshView refreshView;

    protected ImageView placeHolder;
    protected ProgressBar progressBar;

    protected RecyclerView.Adapter adapter;

    protected int mLayout = 0;

    protected boolean withHeaderTab = true;
    protected boolean withRefreshView = true;
    protected boolean isNewLoad = true;



    protected abstract void onCreateCache();

    protected abstract RecyclerView.Adapter bindAdapter();

    protected abstract void loadFromNet();
    protected abstract void loadFromCache();
    protected abstract boolean hasData();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setLayout();
        parentView = inflater.inflate(mLayout, container, false);
        withHeaderTab = setHeaderTab();
        withRefreshView = setRefreshView();

        progressBar = (ProgressBar) parentView.findViewById(R.id.progressbar);
        recyclerView = (RecyclerView) parentView.findViewById(R.id.recyclerView);
        placeHolder = (ImageView) parentView.findViewById(R.id.placeholder);

        onCreateCache();

        adapter = bindAdapter();


        mLayoutgManager = new LinearLayoutManager(LeisureApplication.AppContext);
        recyclerView.setAdapter(adapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        if(withHeaderTab){
            getActivity().findViewById(R.id.tab_layout).setVisibility(View.VISIBLE);
        }else{
            getActivity().findViewById(R.id.tab_layout).setVisibility(View.GONE);
        }

        if(withRefreshView){
            refreshView = (PullToRefreshView) parentView.findViewById(R.id.pull_to_refresh);
            refreshView.setOnRefreshListener(new PullToRefreshView.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    loadFromNet();
                }
            });

            placeHolder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    placeHolder.setVisibility(View.GONE);
                    loadFromNet();
                }
            });
        }

        loadFromCache();

        return parentView;
    }



    protected boolean setHeaderTab(){
        return true;
    }
    protected boolean setRefreshView(){
        return true;
    }
    protected void setLayout(){
        mLayout = R.layout.layout_common_list;
    }

    protected Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what){
                case CONSTANT.ID_FAILURE:
                    if(isAdded()) {
                        Utils.DLog(getString(R.string.Text_Net_Exception));
                    }
                    break;
                case CONSTANT.ID_SUCCESS:
                    if(isAdded()){
                        Utils.DLog(getString(R.string.text_refresh_success));
                    }
                    break;
            }
            if(withRefreshView){
                refreshView.setRefreshing(false);
            }


            if(hasData()){
                placeHolder.setVisibility(View.GONE);
            }else{
                if(isNewLoad && withRefreshView){
                    loadFromNet();
                }
                placeHolder.setVisibility(View.VISIBLE);
            }


            progressBar.setVisibility(View.GONE);
            adapter.notifyDataSetChanged();
            return false;
        }
    });
}