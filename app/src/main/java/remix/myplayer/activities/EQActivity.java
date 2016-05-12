package remix.myplayer.activities;

import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.media.audiofx.Virtualizer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

import remix.myplayer.R;
import remix.myplayer.application.Application;
import remix.myplayer.services.MusicService;
import remix.myplayer.ui.customviews.EQSeekBar;
import remix.myplayer.utils.DensityUtil;
import remix.myplayer.utils.Global;
import remix.myplayer.utils.SharedPrefsUtil;

/**
 * Created by taeja on 16-4-13.
 */
public class EQActivity extends ToolbarActivity {
    private final static String TAG = "EQActivity";
    private static Equalizer mEqualizer;
    public static EQActivity mInstance;
    private static short mBandNumber = -1;
    private static short mMaxEQLevel = -1;
    private static short mMinEQLevel = -1;
    private static ArrayList<Integer> mCenterFres = new ArrayList<>();
    private static HashMap<String,Short> mPreSettings = new HashMap<>();
    private ArrayList<EQSeekBar> mEQSeekBars = new ArrayList<>();
    private static ArrayList<Short> mBandLevels = new ArrayList<>();
    private SwitchCompat mSwitch;
    private static ArrayList<Short> mBandFrequencys = new ArrayList<>();
    private static boolean mEnable = false;
    private static boolean mInitialEnable = false;
    private static BassBoost mBassBoost;
    private static Virtualizer mVirtualizer;

    private Toolbar mToolBar;
    private static short mBassBoostLevel;
    private static short mVirtualizeLevel;
    private static boolean mIsRunning;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            for(int i = 0 ; i < mEQSeekBars.size() ;i++){
                int temp = mBandFrequencys.get(i);
                setSeekBarProgress(mEQSeekBars.get(i),temp);
                mEQSeekBars.get(i).setEnabled(mEnable);
            }
        }
    };

    public static void Init(){
        new Thread(){
            @Override
            public void run() {
                int AudioSessionId = MusicService.getMediaPlayer().getAudioSessionId();
                Log.d(TAG,"AudioSessionId:" + AudioSessionId);
                if(AudioSessionId  == 0) {
                    Toast.makeText(Application.getContext(),"均衡器初始化失败",Toast.LENGTH_SHORT).show();
                    return;
                }
                //是否启用音效设置
                mEnable = SharedPrefsUtil.getValue(Application.getContext(),"setting","EnableEQ",false) & Global.getHeadsetOn();
                mInitialEnable = SharedPrefsUtil.getValue(Application.getContext(),"setting","InitialEnableEQ",false);

                //EQ
                mEqualizer = new Equalizer(0, AudioSessionId);
                mEqualizer.setEnabled(mEnable);
//                //重低音
//                mBassBoost = new BassBoost(0,AudioSessionId);
//                mBassBoost.setEnabled(mEnable);
//                mBassBoostLevel = (short)SharedPrefsUtil.getValue(Application.getContext(),"setting","BassBoostLevel",0);
//                if(mEnable && mBassBoost.getStrengthSupported()){
//                    mBassBoost.setStrength(mBassBoostLevel);
//                }
//                //环绕音效
//                mVirtualizer = new Virtualizer(0,AudioSessionId);
//                mVirtualizeLevel = (short)SharedPrefsUtil.getValue(Application.getContext(),"setting","VirtualizeLevel",0);
//                mVirtualizer.setEnabled(mEnable);
//                if(mEnable && mVirtualizer.getStrengthSupported()){
//                    mVirtualizer.setStrength(mVirtualizeLevel);
//                }

                //得到当前Equalizer引擎所支持的控制频率的标签数目。
                mBandNumber = mEqualizer.getNumberOfBands();

                //得到之前存储的每个频率的db值
                for(short i = 0 ; i < mBandNumber; i++){
                    short temp = (short)(SharedPrefsUtil.getValue(Application.getContext(),"setting","Band" + i,0));
                    mBandFrequencys.add(temp);
                    if (mEnable){
                        mEqualizer.setBandLevel(i,temp);
                    }
                }

                //得到的最小频率
                mMinEQLevel = mEqualizer.getBandLevelRange()[0];
                //得到的最大频率
                mMaxEQLevel = mEqualizer.getBandLevelRange()[1];
                for (short i = 0; i < mBandNumber; i++) {
                    //通过标签可以顺次的获得所有支持的频率的名字比如 60Hz 230Hz
                    mCenterFres.add(mEqualizer.getCenterFreq(i) / 1000);
                }

                //获得所有预设的音效
                for(short i = 0 ; i < mEqualizer.getNumberOfPresets() ; i++){
                    mPreSettings.put(mEqualizer.getPresetName(i),i);
                }

                //获得所有频率值
                short temp = (short) ((mMaxEQLevel - mMinEQLevel) / 30);
                for(short i = 0 ; i < 31; i++){
                    mBandLevels.add((short)(1500 - (i * temp)));
                }

            }
        }.start();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        unregisterReceiver(mEQReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mIsRunning = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        mIsRunning = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eq);

        mInstance = this;

        mToolBar = (Toolbar)findViewById(R.id.toolbar);
        initToolbar(mToolBar,"使用均衡器");

        //初始化switch
        mSwitch = (SwitchCompat)findViewById(R.id.eq_switch);
        mSwitch.setChecked(mEnable);
        mSwitch.setThumbResource(mEnable ? R.drawable.timer_btn_seleted_btn : R.drawable.timer_btn_normal_btn);
        mSwitch.setTrackResource(mEnable ? R.drawable.timer_btn_seleted_focus : R.drawable.timer_btn_normal_focus);
        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked == mEnable)
                    return;

                if(!Global.getHeadsetOn()){
                    Toast.makeText(EQActivity.this,"请插入耳机",Toast.LENGTH_SHORT).show();
                    mSwitch.setChecked(false);
                    return;
                }
                mInitialEnable = isChecked;
                SharedPrefsUtil.putValue(EQActivity.this,"setting","InitialEnableEQ",mInitialEnable);
                mEnable = isChecked;
                UpdateEnable(isChecked);
            }
        });

        LinearLayout EQContainer = (LinearLayout)findViewById(R.id.eq_container);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams( DensityUtil.dip2px(this,30),ViewGroup.LayoutParams.MATCH_PARENT);
        lp.setMargins(DensityUtil.dip2px(this,20),0,DensityUtil.dip2px(this,20),0);
        for(int i = 0 ; i < mBandNumber ;i++){
            EQSeekBar eqSeekBar = new EQSeekBar(this);
            eqSeekBar.setLayoutParams(lp);

            eqSeekBar.setOnSeekBarChangeListener(new EQSeekbarOnChangeListener());
            eqSeekBar.setMax(mMaxEQLevel - mMinEQLevel);
            eqSeekBar.setTag(mCenterFres.get(i));
            int fre_temp = mCenterFres.get(i);
            String hz = fre_temp > 1000 ?  fre_temp / 1000 + "K" : fre_temp + "";
            eqSeekBar.setFreText(hz);
            mEQSeekBars.add(eqSeekBar);
            EQContainer.addView(eqSeekBar);
        }


        new Thread(){
            @Override
            public void run() {
                if(!(mEQSeekBars.size() > 0))
                    return;
                while (!mEQSeekBars.get(mEQSeekBars.size() - 1).isInit()){
                }
                Message msg = new Message();
                mHandler.sendMessage(msg);
            }
        }.start();

    }

    public void UpdateEnable(boolean enable) {
        mEnable = enable;
        SharedPrefsUtil.putValue(EQActivity.this,"setting","EnableEQ",enable);

        if(mSwitch != null) {
            mSwitch.setChecked(enable);
            mSwitch.setThumbResource(enable ? R.drawable.timer_btn_seleted_btn : R.drawable.timer_btn_normal_btn);
            mSwitch.setTrackResource(enable ? R.drawable.timer_btn_seleted_focus : R.drawable.timer_btn_normal_focus);
        }
//        mBassBoost.setEnabled(mEnable);
//        if(mBassBoost.getStrengthSupported()){
//            mBassBoost.setStrength(mEnable ? mBassBoostLevel : 0);
//        }
//
//        mVirtualizer.setEnabled(mEnable);
//        if(mVirtualizer.getStrengthSupported()){
//            mVirtualizer.setStrength(mEnable ? mVirtualizeLevel : 0);
//        }

        mEqualizer.setEnabled(mEnable);
        for(int i = 0 ; i < mEQSeekBars.size() ;i++){
            mEQSeekBars.get(i).setEnabled(enable);
            mEqualizer.setBandLevel((short)i,enable ? mBandFrequencys.get(i) : 0);
        }
    }

    class EQSeekbarOnChangeListener implements EQSeekBar.OnSeekBarChangeListener{
        @Override
        public void onProgressChanged(EQSeekBar seekBar, int position, boolean fromUser) {
            if(!seekBar.canDrag())
                return;
            try {
                int fre = Integer.valueOf(seekBar.getTag().toString());
                for(int i = 0 ; i < mCenterFres.size() ; i++){
                    if(fre == mCenterFres.get(i)){
                        short temp = (mBandLevels.get(position));
                        if(temp > mMaxEQLevel || temp < mMinEQLevel){
                            Toast.makeText(EQActivity.this,"参数不合法: " + fre ,Toast.LENGTH_SHORT).show();
                            return;
                        }
                        //设置db值
                        mEqualizer.setBandLevel((short)i,temp);
                        //db值存储到sp
                        SharedPrefsUtil.putValue(EQActivity.this,"setting","Band" + i,temp );
                        break;
                    }
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        @Override
        public void onStartTrackingTouch(EQSeekBar seekBar) {
        }
        @Override
        public void onStopTrackingTouch(EQSeekBar seekBar) {
        }
    }


    private void setSeekBarProgress(EQSeekBar seekBar,int frequency){
        if(frequency <= mBandLevels.get(mBandLevels.size() / 2))
            frequency = mMaxEQLevel - frequency;
        else
            frequency = Math.abs(mMinEQLevel) - frequency;
        seekBar.setProgress(frequency);
    }

    //重置音效设置
    public void onReset(View v){
        if(!Global.getHeadsetOn()){
            Toast.makeText(EQActivity.this,"请插入耳机",Toast.LENGTH_SHORT).show();
            return;
        }
        mInitialEnable = true;
        SharedPrefsUtil.putValue(EQActivity.this,"setting","InitialEnableEQ",mInitialEnable);
        UpdateEnable(true);
        if(mBandFrequencys != null)
            mBandFrequencys.clear();
        for(short i = 0 ; i < mEQSeekBars.size() ;i++){
            //设置db值
            mEqualizer.setBandLevel(i,(short) 0);
            //db值存储到sp
            SharedPrefsUtil.putValue(EQActivity.this,"setting","Band" + i,(short)0 );
            //设置seekbar进度
            setSeekBarProgress(mEQSeekBars.get(i),0);
            //存储每个频率的db值到内存
            mBandFrequencys.add((short)0);
        }
    }

    public static boolean getInitialEnable(){
        return mInitialEnable;
    }

    public static void setEnable(boolean enable){
        mEnable = enable;
    }

//    private void setPreset(View v) {
//        if(!mEnable)
//            return;
//        String tag = v.getTag().toString();
//        try {
//            if(tag != null && !tag.equals("")) {
//                short preset = mPreSettings.get(tag);
//                if (preset >= 0 && preset < mPreSettings.size())
//                    //应用预设音效
//                    mEqualizer.usePreset(preset);
//                //设置每个频率的DB值
//                for(short i = 0 ; i < mEqualizer.getNumberOfBands(); i++){
//                    int temp = mEqualizer.getBandLevel(i);
//                    if(temp >= mMinEQLevel && temp <= mMaxEQLevel) {
//                        //db值存储到SP
//                        SharedPrefsUtil.putValue(EQActivity.this,"setting","Band" + i,temp);
//                        setSeekBarProgress(mEQSeekBars.get(i),temp);
//                    }
//                }
//            }
//        } catch (Exception e){
//            e.printStackTrace();
//        }
//    }

//    public void onRock(View v){
//        setPreset(v);
//    }
//
//
//    public void onPop(View v){
//        setPreset(v);
//    }
//
//    public void onClassical(View v){
//        setPreset(v);
//    }
//
//    public void onBass(View v){
//        setPreset(v);
//    }

}
