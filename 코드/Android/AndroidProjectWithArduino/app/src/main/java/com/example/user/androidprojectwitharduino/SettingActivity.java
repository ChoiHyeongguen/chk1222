package com.example.user.androidprojectwitharduino;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

public class SettingActivity extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();
    }

    public static class MyPreferenceFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {
        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.activity_setting);
            bindPreferenceSummaryToValue(findPreference("oneCallPhoneNumber"));
            bindPreferenceSummaryToValue(findPreference("ringtone"));
        }

        private void bindPreferenceSummaryToValue(Preference preference) {
            // Set the listener to watch for value changes.
            preference.setOnPreferenceChangeListener(this);

            // Trigger the listener immediately with the preference's
            // current value.
            onPreferenceChange(preference,
                    PreferenceManager
                            .getDefaultSharedPreferences(preference.getContext())
                            .getString(preference.getKey(), ""));
        }

        public boolean onPreferenceChange(Preference preference, Object value) {        // 설정을 바꾼다면
            String stringValue = value.toString();
            /*
                원래는 프리페어런스 별로 id를 구별해줘야 하지만
                각각 종류가 1개씩만 있어서 instanceof 후 바로 그에 맞는 내용들을 실행하게 되었습니다.
             */
            Context hostActivity = getActivity();
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(hostActivity);// 공통으로 저장될 데이터 저장장소
            Log.d("tt", "dd");
            if (preference instanceof EditTextPreference) {     // 각각의 프리페어런스에 따라 설정 변경
                if(stringValue.contains("-") || stringValue.contains(" ")){
                     Toast.makeText(hostActivity,"특수문자나 공백을 포함할 수 없습니다.", Toast.LENGTH_LONG).show();
                    return true;
                }

                preference.setSummary(stringValue);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("phone_number", stringValue);      // 번호를 저장소에 넣으면 원콜번호가 변경됩니다.
                editor.commit();
            } else if (preference instanceof RingtonePreference) {
                Log.d("Sd", stringValue);
                if (TextUtils.isEmpty(stringValue))     {   // 무음 고르면 무음으로 처리
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("phone_ring", "no");
                    editor.commit();
                    preference.setSummary("무음으로 설정됨");
                } else {        // 이외의 벨소리는 알람소리로 지정
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("phone_ring", stringValue);
                    editor.commit();

                    Ringtone ringtone = RingtoneManager.getRingtone(preference.getContext(), Uri.parse(stringValue));
                    if (ringtone == null) {
                        preference.setSummary(null);
                    } else {
                        String name = ringtone
                                .getTitle(preference.getContext());
                        Log.d("Sd", stringValue);
                        preference.setSummary(name);
                    }
                }
            }

            return true;
        }
    }
}