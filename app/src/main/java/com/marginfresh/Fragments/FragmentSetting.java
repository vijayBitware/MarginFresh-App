package com.marginfresh.Fragments;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.marginfresh.R;
import com.marginfresh.activities.ActivityChangePAssword;
import com.marginfresh.activities.ActivityFAQ;
import com.marginfresh.activities.ActivityInviteFriend;
import com.marginfresh.activities.ActivityMyAddress;
import com.marginfresh.activities.ActivitySavedAddress;
import com.marginfresh.activities.ActivityUpdateProfile;
import com.marginfresh.activities.LoginActivity;
import com.marginfresh.db_utils.DatabaseHandler;

/**
 * Created by bitware on 6/6/17.
 */

public class FragmentSetting extends Fragment{

    View view;
    LinearLayout ll_myAddress,ll_changePassword;
    TextView tv_logout;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    TextView tv_editProfile,tv_customerName,tv_contact,tv_customerEmail,tv_space;
    CardView card_changeAddress,card_inviteFriends,cv_faq;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_settings,container,false);
        init();

        tv_customerName.setText(sharedPreferences.getString("customerName",""));
        tv_contact.setText(sharedPreferences.getString("customerContact",""));
        tv_customerEmail.setText(sharedPreferences.getString("customerEmail",""));

        tv_editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), ActivityUpdateProfile.class));

            }
        });
        ll_myAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), ActivitySavedAddress.class));
            }
        });

        ll_changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), ActivityChangePAssword.class));
            }
        });

        card_inviteFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), ActivityInviteFriend.class));
            }
        });

        cv_faq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), ActivityFAQ.class));
            }
        });
        return view;
    }

    private void init() {
        ll_changePassword = (LinearLayout) view.findViewById(R.id.ll_changePassword);
        ll_myAddress= (LinearLayout) view.findViewById(R.id.ll_myAddress);
        sharedPreferences = getContext().getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        tv_editProfile = (TextView) view.findViewById(R.id.tv_editProfile);
        tv_customerName= (TextView) view.findViewById(R.id.tv_customerName);
        tv_customerEmail = (TextView) view.findViewById(R.id.tv_customerEmail);
        tv_contact = (TextView) view.findViewById(R.id.tv_customerContact);
        cv_faq = (CardView) view.findViewById(R.id.cv_faq);

        card_inviteFriends = (CardView) view.findViewById(R.id.card_inviteFriends);

        tv_space = (TextView) view.findViewById(R.id.tv_space);
        card_changeAddress = (CardView) view.findViewById(R.id.card_changeAddress);
        if (sharedPreferences.getString("loginType","").equals("facebookLogin")){
            card_changeAddress.setVisibility(View.GONE);
            tv_space.setVisibility(View.GONE);
        } else if (sharedPreferences.getString("loginType","").equals("googleLogin")){
            card_changeAddress.setVisibility(View.GONE);
            tv_space.setVisibility(View.GONE);
        }else {
            card_changeAddress.setVisibility(View.VISIBLE);
            tv_space.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        System.out.println("in on reume of setting fragment");
        Button noticount = (Button) getActivity().findViewById(R.id.btn_notiCount);
        if (sharedPreferences.getString("notiCount","").equals("0") || sharedPreferences.getString("notiCount","").equals("")){
            System.out.println("in if condition of on reume of setting fragment");
            noticount.setVisibility(View.INVISIBLE);
        }else  if (!sharedPreferences.getString("notiCount","").equals("0") || !sharedPreferences.getString("notiCount","").equals("")){
            System.out.println("in else condition of on reume of setting fragment");
            noticount.setVisibility(View.VISIBLE);
            noticount.setText(sharedPreferences.getString("notiCount",""));
        }

        DatabaseHandler db = new DatabaseHandler(getContext());
        Button btn_cartCount = (Button) getActivity().findViewById(R.id.btn_cartCount);
        String cartCount = db.getProductsInCartCount(sharedPreferences.getString("user_id",""));
        System.out.println("Activity Cart > cart count is " +cartCount);
        if (cartCount.equals("0") || cartCount.equals("")){
            btn_cartCount.setVisibility(View.GONE);
        }else {
            btn_cartCount.setVisibility(View.VISIBLE);
            btn_cartCount.setText(cartCount);
        }
    }
}
