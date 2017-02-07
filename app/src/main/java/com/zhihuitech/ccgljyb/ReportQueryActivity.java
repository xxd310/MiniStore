package com.zhihuitech.ccgljyb;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ReportQueryActivity extends Activity {
    private TextView tvInStorage;
    private TextView tvOutStorage;
    private TextView tvCheck;

    private LinearLayout llInStorage;
    private LinearLayout llOutStorage;
    private LinearLayout llCheck;

    private int currentIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report_query);
        
        findViews();
        addListeners();
    }

    private void addListeners() {
        tvInStorage.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(View v) {
                if(currentIndex != 0) {
                    tvInStorage.setBackground(getResources().getDrawable(R.drawable.top_left_menu_bg));
                    tvInStorage.setTextColor(Color.WHITE);
                    llInStorage.setVisibility(View.VISIBLE);
                    tvOutStorage.setBackground(null);
                    tvOutStorage.setTextColor(getResources().getColor(R.color.main_color));
                    llOutStorage.setVisibility(View.GONE);
                    tvCheck.setBackground(null);
                    tvCheck.setTextColor(getResources().getColor(R.color.main_color));
                    llCheck.setVisibility(View.GONE);
                    currentIndex = 0;
                }
            }
        });
        tvOutStorage.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(View v) {
                if(currentIndex != 1) {
                    tvInStorage.setBackground(null);
                    tvInStorage.setTextColor(getResources().getColor(R.color.main_color));
                    llInStorage.setVisibility(View.GONE);
                    tvOutStorage.setBackgroundColor(getResources().getColor(R.color.main_color));
                    tvOutStorage.setTextColor(Color.WHITE);
                    llOutStorage.setVisibility(View.VISIBLE);
                    tvCheck.setBackground(null);
                    tvCheck.setTextColor(getResources().getColor(R.color.main_color));
                    llCheck.setVisibility(View.GONE);
                    currentIndex = 1;
                }
            }
        });
        tvCheck.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(View v) {
                if(currentIndex != 2) {
                    tvInStorage.setBackground(null);
                    tvInStorage.setTextColor(getResources().getColor(R.color.main_color));
                    llInStorage.setVisibility(View.GONE);
                    tvOutStorage.setBackground(null);
                    tvOutStorage.setTextColor(getResources().getColor(R.color.main_color));
                    llOutStorage.setVisibility(View.GONE);
                    tvCheck.setBackground(getResources().getDrawable(R.drawable.top_right_menu_bg));
                    tvCheck.setTextColor(Color.WHITE);
                    llCheck.setVisibility(View.VISIBLE);
                    currentIndex = 2;
                }
            }
        });
    }

    private void findViews() {
        tvInStorage = (TextView) findViewById(R.id.tv_in_storage_report);
        tvOutStorage = (TextView) findViewById(R.id.tv_out_storage_report);
        tvCheck = (TextView) findViewById(R.id.tv_check_report);
        llInStorage = (LinearLayout) findViewById(R.id.ll_in_storage_report);
        llOutStorage = (LinearLayout) findViewById(R.id.ll_out_storage_report);
        llCheck = (LinearLayout) findViewById(R.id.ll_check_report);
    }
}
