package com.example.saubhagyam.thetalklist.Adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.saubhagyam.thetalklist.Available_Tutor_Expanded;
import com.example.saubhagyam.thetalklist.Available_tutor;
import com.example.saubhagyam.thetalklist.FragmentStack;
import com.example.saubhagyam.thetalklist.MessageOneToOne;
import com.example.saubhagyam.thetalklist.TTL;
import com.example.saubhagyam.thetalklist.TabBackStack;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.saubhagyam.thetalklist.CircleTransform;
import com.example.saubhagyam.thetalklist.MessageList;
import com.example.saubhagyam.thetalklist.R;
import com.example.saubhagyam.thetalklist.VideoCall;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by Saubhagyam on 17/04/2017.
 */

public class AvailableTutorRecyclerAdapter extends RecyclerView.Adapter<AvailableTutorRecyclerAdapter.MyViewHolder> {

    private final Context context;
    private JSONArray array;
     FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;
    FragmentStack fragmentStack;

    int withPos = 0;
    int withfav = 0;
    int pos;
    RecyclerView recyclerView;
    int fav;


    public AvailableTutorRecyclerAdapter(int pos, Context context, JSONArray array, FragmentManager fragmentManager, int fav) {
        this.pos = pos;
        this.context = context;
        this.array = array;
        this.fragmentManager = fragmentManager;
        withPos = 1;
        withfav = fav;
    }

    public AvailableTutorRecyclerAdapter(Context context) {
        this.context=context;
    }

    public AvailableTutorRecyclerAdapter(Context context, JSONArray array, FragmentManager fragmentManager) {
        this.context = context;
        this.array = array;
        this.fragmentManager = fragmentManager;
//        this.recyclerView=recyclerView;
    }



    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.available_tutor_list_layout, parent, false);
        return new MyViewHolder(view);
    }

    JSONObject object;
    String FirstName;

    SharedPreferences pref1;
    SharedPreferences.Editor ed;
    SharedPreferences pref;

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        fragmentTransaction = fragmentManager.beginTransaction();


        fragmentStack = FragmentStack.getInstance();
        try {
            object = (JSONObject) array.get(position);
            if (!object.getString("avgRate").equalsIgnoreCase(""))
                holder.ratingBar.setRating(Float.parseFloat(object.getString("avgRate")));
            else holder.ratingBar.setRating(0f);
            Log.e("available tutor obj", object.toString());
            final String picPath = object.getString("pic");
            final int tutorId = object.getInt("uid");
            if (withPos == 1) {
                if (position == pos) {

                    if (withfav == 1) {
                        holder.favoriteTag.setVisibility(View.VISIBLE);

                        pref = getApplicationContext().getSharedPreferences("loginStatus", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = pref.edit();


                        new CountDownTimer(5000, 1000) { // 5000 = 5 sec

                            public void onTick(long millisUntilFinished) {
                                holder.TutorImg.setImageResource(R.drawable.favorite);
                            }

                            public void onFinish() {


                                if (!picPath.equals("")) {

                                    Glide.with(getApplicationContext()).load("https://www.thetalklist.com/uploads/images/" + picPath)
                                            .crossFade()
                                            .thumbnail(0.5f)
                                            .bitmapTransform(new CircleTransform(getApplicationContext()))
                                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                                            .into(holder.TutorImg);
                                } else
                                    Glide.with(getApplicationContext()).load("https://www.thetalklist.com/images/header.jpg")
                                            .crossFade()
                                            .thumbnail(0.5f)
                                            .bitmapTransform(new CircleTransform(getApplicationContext()))
                                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                                            .into(holder.TutorImg);
                            }
                        }.start();
                    } else {
                        holder.favoriteTag.setVisibility(View.GONE);
                    }
                }
            }

            if (object.getInt("isMyFavourite") == 1) {
                holder.favoriteTag.setVisibility(View.GONE);

            } else holder.favoriteTag.setVisibility(View.GONE);
            FirstName = object.getString("firstName");


            holder.fn.setText(object.getString("firstName") + " " + object.getString("lastName"));
            holder.uid.setText(object.getString("uid"));
            holder.hpr.setText(object.getString("hRate"));


            if (!picPath.equals("")) {
                Glide.with(context).load("https://www.thetalklist.com/uploads/images/" + picPath)
                        .crossFade()
                        .thumbnail(0.5f)
                        .bitmapTransform(new CircleTransform(context))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(holder.TutorImg);
            } else
                Glide.with(context).load("https://www.thetalklist.com/images/header.jpg")
                        .crossFade()
                        .thumbnail(0.5f)
                        .bitmapTransform(new CircleTransform(context))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(holder.TutorImg);

            SharedPreferences chatPref = context.getSharedPreferences("chatPref", Context.MODE_PRIVATE);
            final SharedPreferences.Editor chatPrefEditor = chatPref.edit();

            holder.msgButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MessageOneToOne messageList = new MessageOneToOne();
                    chatPrefEditor.putString("firstName", FirstName);
                    chatPrefEditor.putInt("receiverId", tutorId).apply();
                    TTL ttl = (TTL) context.getApplicationContext();
                    ttl.ExitBit = 1;
                    fragmentStack.push(new Available_tutor());
                    tabBackStack.setTabPosition(0);
                    fragmentTransaction.replace(R.id.viewpager, messageList).commit();
                }
            });

            holder.msgButton1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MessageOneToOne messageList = new MessageOneToOne();
                    chatPrefEditor.putString("firstName", FirstName);
                    chatPrefEditor.putInt("receiverId", tutorId).apply();
                    TTL ttl = (TTL) context.getApplicationContext();
                    ttl.ExitBit = 1;
                    fragmentStack.push(new Available_tutor());
                    tabBackStack.setTabPosition(0);
                    fragmentTransaction.replace(R.id.viewpager, messageList).commit();

                }
            });

            final SharedPreferences preferences = context.getSharedPreferences("videoCallTutorDetails", Context.MODE_PRIVATE);
            final SharedPreferences.Editor editor = preferences.edit();
            final Float hPr = (Float.parseFloat(holder.hpr.getText().toString()) / 25);
            String h_str = String.format("%.02f", hPr);
            holder.CpM.setText(h_str);
            holder.VideocallButton1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    TTL ttl = (TTL) context.getApplicationContext();
                    ttl.ExitBit = 1;
                    fragmentStack.push(new Available_tutor());


                    editor.putString("tutorName", holder.fn.getText().toString());
                    editor.putInt("flag", 1);
                    pref = context.getSharedPreferences("loginStatus", Context.MODE_PRIVATE);
                    editor.putInt("studentId", pref.getInt("id", 0));
                    editor.putString("tutorName", holder.fn.getText().toString());
                    editor.putInt("tutorId", Integer.parseInt(holder.uid.getText().toString()));
                    editor.putFloat("hRate", Float.parseFloat(holder.CpM.getText().toString()));
                    editor.putFloat("credit", hPr).apply();
                    fragmentTransaction.replace(R.id.viewpager, new Available_tutor(preferences.getInt("flag", 0), preferences.getFloat("credit", 0), preferences.getString("tutorName", "")), "Available Tutor").commit();
                }
            });


            holder.VideocallButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TTL ttl = (TTL) context.getApplicationContext();
                    ttl.ExitBit = 1;
                    fragmentStack.push(new Available_tutor());


                    editor.putString("tutorName", holder.fn.getText().toString());
                    editor.putInt("flag", 1);
                    pref = context.getSharedPreferences("loginStatus", Context.MODE_PRIVATE);
                    editor.putInt("studentId", pref.getInt("id", 0));
                    editor.putString("tutorName", holder.fn.getText().toString());
                    editor.putInt("tutorId", Integer.parseInt(holder.uid.getText().toString()));
                    editor.putFloat("hRate", Float.parseFloat(holder.CpM.getText().toString()));
                    editor.putFloat("credit", hPr).apply();
                    fragmentTransaction.replace(R.id.viewpager, new Available_tutor(preferences.getInt("flag", 0), preferences.getFloat("credit", 0), preferences.getString("tutorName", "")), "Available Tutor").commit();

                }
            });


        } catch (JSONException e) {
            e.printStackTrace();
        }
        tabBackStack = TabBackStack.getInstance();
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                pref1 = context.getSharedPreferences("AvailableTutorPref", Context.MODE_PRIVATE);
                ed = pref1.edit();
                ed.putInt("position", position).apply();
                TTL ttl = (TTL) context.getApplicationContext();
                ttl.ExitBit = 1;

                fragmentStack.push(new Available_tutor());
                Available_Tutor_Expanded available_tutoe_expanded = new Available_Tutor_Expanded();

                try {
                    JSONObject jsonObject = array.getJSONObject(position);
                    Bundle bundle = new Bundle();
                    SharedPreferences preferences = context.getSharedPreferences("availableTutoeExpPref", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("tutorName", jsonObject.getString("firstName"));
                    editor.putInt("tutorRoleId", jsonObject.getInt("roleId"));
                    editor.putString("tutorPic", jsonObject.getString("pic"));
                    editor.putString("hRate", jsonObject.getString("hRate"));
                    editor.putString("avgRate", jsonObject.getString("avgRate"));
                    editor.putInt("tutorid", jsonObject.getInt("uid")).apply();
                    available_tutoe_expanded.setArguments(bundle);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                tabBackStack.setTabPosition(0);
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.viewpager, available_tutoe_expanded).commit();


            }
        });


    }


    TabBackStack tabBackStack;

    @Override
    public int getItemCount() {
        return array.length();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        final TextView fn;
        final TextView ln;
        final TextView CpM;
        final TextView uid;
        final TextView hpr;
        final ImageView TutorImg;
        final ImageView VideocallButton1;
        final ImageView msgButton1;
        final LinearLayout favoriteTag;
        final LinearLayout VideocallButton;
        final LinearLayout msgButton;
        final RatingBar ratingBar;
        final View view;


        public MyViewHolder(View itemView) {
            super(itemView);

            msgButton = (LinearLayout) itemView.findViewById(R.id.msgButton);
            VideocallButton = (LinearLayout) itemView.findViewById(R.id.videocallButton);
            msgButton1 = (ImageView) itemView.findViewById(R.id.msgButton1);
            VideocallButton1 = (ImageView) itemView.findViewById(R.id.videocallButton1);
            fn = (TextView) itemView.findViewById(R.id.availableTutorListFirstName);
            uid = (TextView) itemView.findViewById(R.id.availableTutor_uid);
            hpr = (TextView) itemView.findViewById(R.id.availableTutor_hpr);
            CpM = (TextView) itemView.findViewById(R.id.availableTutorListCPS);
            ln = (TextView) itemView.findViewById(R.id.availableTutorListLastName);
            TutorImg = (ImageView) itemView.findViewById(R.id.TutorImg);
            ratingBar = (RatingBar) itemView.findViewById(R.id.ratingBar);
            favoriteTag = (LinearLayout) itemView.findViewById(R.id.availableTutorListLayoutFavoriteTag);

            view = itemView.findViewById(R.id.available_tutor_layout_id);


        }


    }


}