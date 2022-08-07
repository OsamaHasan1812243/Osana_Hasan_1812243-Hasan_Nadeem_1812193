package com.example.project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;


import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.project.Fragments.OnBoardingFragment1;
import com.example.project.Fragments.OnBoardingFragment2;
import com.example.project.Fragments.OnBoardingFragment3;


import static android.view.WindowManager.LayoutParams.*;

public class IntoductoryActivity extends AppCompatActivity {
    ImageView Appname,SplashImage;
    LottieAnimationView lottieAnimationView;

    private static final int NUM_PAGES = 3;
    private ViewPager viewPager;
    private ScreenSlidesPagerAdapter pagerAdapter;
    Animation anim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(FLAG_FULLSCREEN, FLAG_FULLSCREEN);

        setContentView(R.layout.activity_introductory);
        Appname = findViewById(R.id.app_name);
        SplashImage = findViewById(R.id.image);
        lottieAnimationView = findViewById(R.id.lottie);

        viewPager = findViewById(R.id.pager);
        pagerAdapter = new ScreenSlidesPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);

        anim = AnimationUtils.loadAnimation(this, R.anim.o_b_anim);
        viewPager.startAnimation(anim);

        SplashImage.animate().translationY(-2800).setDuration(1000).setStartDelay(4000);
        Appname.animate().translationY(2000).setDuration(1000).setStartDelay(4000);
        lottieAnimationView.animate().translationY(1400).setDuration(1000).setStartDelay(4000);
    }




    private class ScreenSlidesPagerAdapter extends FragmentStatePagerAdapter{

        public ScreenSlidesPagerAdapter(@NonNull FragmentManager fm) {
            super(fm);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:
                    OnBoardingFragment1 tab1 = new OnBoardingFragment1();
                    return tab1;
                case 1:
                    OnBoardingFragment2 tab2 = new OnBoardingFragment2();
                    return tab2;
                case 2:
                    OnBoardingFragment3 tab3 = new OnBoardingFragment3();
                    return tab3;
            }

            return null;
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }


}
