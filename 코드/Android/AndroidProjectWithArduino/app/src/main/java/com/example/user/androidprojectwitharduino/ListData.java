package com.example.user.androidprojectwitharduino;

import android.graphics.drawable.Drawable;

import java.text.Collator;
import java.util.Comparator;

/*
    게시판 리스트에 표시되야 하는 항목들을
    클래스로 묶어서 관리하기위해 만들었고
    리스트 뷰에 최적화 되게(정렬) 사용하려고 따로 만들었습니다.
 */
public class ListData {
    public Drawable mIcon;      // 아이콘
    public String mTitle;   // 제목
    public String mDate;    // 날짜
    /*
        게시판 리스트 순서 정할 수 있게
        지정해주는 함수 생성 및 오버라이딩
     */
    public static final Comparator<ListData> ALPHA_COMPARATOR = new Comparator<ListData>() {
        private final Collator sCollator = Collator.getInstance();

        @Override
        public int compare(ListData mListData1, ListData mListData2) {
            return sCollator.compare(mListData1.mDate, mListData2.mDate);
        }
    };
}
