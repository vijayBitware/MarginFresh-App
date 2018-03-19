package com.marginfresh.Fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.marginfresh.R;
import com.marginfresh.db_utils.DatabaseHandler;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by bitware on 10/8/17.
 */

public class FragmentContactUs extends Fragment {

    View view;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_contactus,container,false);
        sharedPreferences = getContext().getSharedPreferences("MyPref",MODE_PRIVATE);
        editor= sharedPreferences.edit();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Button noticount = (Button) getActivity().findViewById(R.id.btn_notiCount);
        if (sharedPreferences.getString("notiCount","").equals("0") || sharedPreferences.getString("notiCount","").equals("")){
            noticount.setVisibility(View.INVISIBLE);
        }else {
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
