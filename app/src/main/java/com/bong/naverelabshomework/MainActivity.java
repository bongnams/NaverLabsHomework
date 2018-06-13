package com.bong.naverelabshomework;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;

import com.bong.naverelabshomework.ImageResult.ImageResultAdapter;
import com.bong.naverelabshomework.ImageResult.ImageResultItem;
import com.bong.naverelabshomework.ImageResult.LoadImageTask;
import com.bong.naverelabshomework.WebResult.WebResultAdapter;
import com.bong.naverelabshomework.WebResult.WebResultItem;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final String WEBSEARCHURL = "https://openapi.naver.com/v1/search/webkr.xml";     //Naver Api Image search URL
    private static final String IMAGESEARCHURL = "https://openapi.naver.com/v1/search/image.xml";   //Naver Api Image search URL
    private static final String NAVERAPICLIENTID = "1xIRbfr15DX614C_Yzox";                          //Naver Api Client ID
    private static final String NAVERAPICLIENTSECRET = "w1eaVmOTw5";                                //Naver Api Client SecretKey

    private static final int WEBSEARCHDISPLAYNUM = 20;      //Web 검색 1회 검색개수
    private static final int IMAGESEARCHDISPLAYNUM = 40;    //Image 검색 1회 검색개수

    private static final int WEBSEARCHSUCCESS = 100;        //Web 검색 성곰
    private static final int WEBSEARCHFAIL = 101;           //Web 검색 실패
    private static final int IMAGESEARCHSUCCESS = 102;      //Image 검색 성공
    private static final int IMAGESEARCHFAIL = 103;         //Image 검색 실패

    private LinearLayout mDetailImageLayout = null;         //이미지 상세 화면 레이아웃

    private EditText mSearchEdittext = null;                //검색 입력 Editbox
    private String mSearchText = null;                    //검색어
    private TabHost mTabHost = null;                        //탭 구성 Host
    private LinearLayout mTabLayout = null;                 //탭 구성 레이아웃
    private TabWidget mTabWidget = null;                    //탭 버튼부분
    private FrameLayout mTabContent = null;                 //탭 컨텐츠

    private ListView mWebListview = null;                   //웹검색 결과 리스트뷰
    private WebResultAdapter mWebListAdapter = null;        //웹검색 결과 adapter
    private GridView mImageListview = null;                 //이미지검색 결과 그리드뷰
    private ImageResultAdapter mImageListAdapter = null;    //이미지 검색 결과 adapter

    private ProgressDialog mProgressDlg = null;             //검색 프로그레스 dialog

    private ArrayList<WebResultItem> mWebItems = null;      //웹검색 결과 items
    private int mTotalWebResultCount = 0;                   //웹검색 결과 총 개수
    private int mCurWebStart = 0;                           //웹검색 시작 위치

    private ArrayList<ImageResultItem> mImageItems = null;  //이미지검색 결과 items
    private int mTotalImageResultCount = 0;                 //이미지검색 결과 총 개수
    private int mCurImageStart = 0;                         //이미지검색 시작 위치
    private int mCurDetailViewPosition = 0;                 //이미지 상세화면 position

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mDetailImageLayout = findViewById(R.id.detailviewLayout);
        mDetailImageLayout.setVisibility(View.INVISIBLE);       //이미지 상세화면 숨기기

        mTabLayout = findViewById(R.id.tabLayout);

        mSearchEdittext = findViewById(R.id.searchEdittext);

        //검색 입력시 키보드 '완료' 버튼 누를시 검색
        mSearchEdittext.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if(i == EditorInfo.IME_ACTION_DONE) {
                   Search();
                }
                return false;
            }
        });

        //검색버튼 리스너 등록
        Button searchButton = findViewById(R.id.searchButton);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Search();
            }
        });

        //클리어 버튼 리스너 등록
        Button clearButton = findViewById(R.id.clearButton);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClearResult();
            }
        });

        //탭 구성
        mTabHost = findViewById(R.id.tabHost);
        mTabHost.setup();
        mTabWidget = mTabHost.getTabWidget();
        mTabContent = mTabHost.getTabContentView();

        //Web Tab
        TabHost.TabSpec webTs = mTabHost.newTabSpec("WebTab");
        webTs.setContent(R.id.webResultView);
        webTs.setIndicator(getString(R.string.webTab));
        mTabHost.addTab(webTs);

        //Image Tab
        TabHost.TabSpec imageTs = mTabHost.newTabSpec("ImageTab");
        imageTs.setContent(R.id.imageResultView);
        imageTs.setIndicator(getString(R.string.imageTab));
        mTabHost.addTab(imageTs);
        mTabHost.setOnTabChangedListener(mTabChangedListener);

        //웹검색 결과 리스트 구성
        mWebItems = new ArrayList<>();
        mWebListAdapter = new WebResultAdapter(this, R.layout.item_web_result, mWebItems);
        mWebListview = findViewById(R.id.webResultView);
        mWebListview.setAdapter(mWebListAdapter);
        mWebListview.setOnItemClickListener(mWebListClickListener);
        mWebListview.setOnScrollListener(mWebListScrollListener);

        //이미지검색 결과 리스트 구성
        mImageItems = new ArrayList<>();
        mImageListAdapter = new ImageResultAdapter(this, R.layout.item_image_result, mImageItems);
        mImageListview = findViewById(R.id.imageResultView);
        mImageListview.setAdapter(mImageListAdapter);
        mImageListview.setOnItemClickListener(mImageListClickListener);
        mImageListview.setOnScrollListener(mImageListScrollListener);

        //이미지 상세화면 닫기 버튼 누를시 상세화면 숨김
        Button closeButton = findViewById(R.id.closeButton);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDetailImageLayout.setVisibility(View.INVISIBLE);
            }
        });

        //이미지 상세화면 Prev 버튼 구현
        Button backButton = findViewById(R.id.prevButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mCurDetailViewPosition > 0) {    //검색 결과의 첫번째 이미지가 아니면 이전 이미지로 이동
                    mCurDetailViewPosition--;
                }
                SetDetailView(mCurDetailViewPosition);
            }
        });

        //이미지 상세화면 Next 버튼 구현
        Button forwardButton = findViewById(R.id.nextButton);
        forwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mCurDetailViewPosition < mImageItems.size() - 1) {   //검색 결과의 마지막 이미지가 아니면 다음 이미지로 이동
                    mCurDetailViewPosition++;
                }
                SetDetailView(mCurDetailViewPosition);
            }
        });

        //검색 중 ProgressDlg 설정
        mProgressDlg = new ProgressDialog(this);
        mProgressDlg.setMessage(getString(R.string.searchingMessage));
    }

    //화면 회전 시 레이아웃 변경
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if(newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            mTabLayout.setOrientation(LinearLayout.VERTICAL);
            mTabWidget.setOrientation(LinearLayout.HORIZONTAL);
            mTabWidget.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0.0f));
            mTabWidget.getChildTabViewAt(0).setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f));
            mTabWidget.getChildTabViewAt(1).setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f));
            mTabContent.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f));

        } else if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mTabLayout.setOrientation(LinearLayout.HORIZONTAL);
            mTabWidget.setOrientation(LinearLayout.VERTICAL);
            mTabWidget.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT, 0.0f));
            mTabWidget.getChildTabViewAt(0).setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 0, 1.0f));
            mTabWidget.getChildTabViewAt(1).setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 0, 1.0f));
            mTabContent.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f));
        }
    }

    //검색 상태에서 탭 변경시 검색된 상태가 아니라면 검색 실행
    private TabHost.OnTabChangeListener mTabChangedListener = new TabHost.OnTabChangeListener() {
        @Override
        public void onTabChanged(String s) {
            if(mSearchText != null) {       //검색어가 설정되어 있다면
                if(s.equals("WebTab") && mCurWebStart == 0) {           //웹 탭 선택하였는데 검색하지 않았었다면
                    mCurWebStart = 1;       //웹검색 시작위치 초기화
                    mProgressDlg.show();    //프로그레스 다이얼로그

                    new Thread() {
                        public void run() {
                            WebSearch();    //웹검색 시작
                        }
                    }.start();
                } else if(s.equals("ImageTab") && mCurImageStart == 0){ //이미지 탭 선택하였는데 검색하지 않았었다면
                    mCurImageStart = 1;     //이미지검색 시작위치 초기화
                    mProgressDlg.show();    //프로그레스 다이얼로그

                    new Thread() {
                        public void run() {
                            ImageSearch();  //이미지검색 시작
                        }
                    }.start();
                }
            }
        }
    };

    //웹검색 클릭 리스트너 구현 - 링크 연결
    private AdapterView.OnItemClickListener mWebListClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mWebItems.get(i).getLink()));
            startActivity(intent);
        }
    };

    //웹검색 추가로딩 기능 구현
    private AbsListView.OnScrollListener mWebListScrollListener = new AbsListView.OnScrollListener() {
        boolean isEndPos = false;        //화면에 리스트의 마지막 아이템이 보여지는지 체크

        @Override
        public void onScroll(AbsListView view, int firstitem, int itemcount, int totalitemcount) {
            //화면에 보이는 첫번째 아이템의 번호+화면에 보이는 아이템의 개수가 리스트 전체의 개수보다 크거나 같을때 true
            isEndPos = (itemcount > 0) && (firstitem + itemcount >= totalitemcount);
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            //스크롤이 멈춘 상태이고 end position 이면
            if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && isEndPos) {
                mCurWebStart = mWebItems.size() + 1;    //현재 검색된 item의 개수 + 1 부터 검색하도록 설정

                //검색 시작위치가 상한값보다 이내이고, 전제 검색결과 개수보다 작거나 같으면 검색 실행
                if (mCurWebStart <= 1000 && mCurWebStart <= mTotalWebResultCount) {
                    mProgressDlg.show();    //프로그레스 다이얼로그

                    new Thread() {
                        public void run() {
                            WebSearch();
                        }
                    }.start();
                }
            }
        }
    };

    //이미지검색 클릭 리스트너 구현 - 이미지 상세화면 표시
    private AdapterView.OnItemClickListener mImageListClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            mDetailImageLayout.setVisibility(View.VISIBLE); //이미지 상세화면 레이아웃 표시
            mCurDetailViewPosition = i;     //상세화면에 표시할 이미지 index 설정
            SetDetailView(i);   //상세화면 컨텐츠 표시
        }
    };

    //이미지검색 추가로딩 기능 구현
    private AbsListView.OnScrollListener mImageListScrollListener = new AbsListView.OnScrollListener() {
        boolean isEndPos = false;        //화면에 리스트의 마지막 아이템이 보여지는지 체크

        @Override
        public void onScroll(AbsListView view, int firstitem, int itemcount, int totalitemcount) {
            //화면에 보이는 첫번째 아이템의 번호+화면에 보이는 아이템의 개수가 리스트 전체의 개수보다 크거나 같을때 true
            isEndPos = (itemcount > 0) && (firstitem + itemcount >= totalitemcount);
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            //스크롤이 멈춘 상태이고 end position 이면
            if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && isEndPos) {
                mCurImageStart = mImageItems.size() + 1;    //현재 검색된 item의 개수 + 1 부터 검색하도록 설정

                //검색 시작위치가 상한값보다 이내이고, 전제 검색결과 개수보다 작거나 같으면 검색 실행
                if (mCurImageStart <= 1000 && mCurImageStart <= mTotalImageResultCount) {
                    mProgressDlg.show();    //프로그레스 다이얼로그

                    new Thread() {
                        public void run() {
                            ImageSearch();
                        }
                    }.start();
                }
            }
        }
    };

    //검색
    private void Search() {
        mSearchText = mSearchEdittext.getText().toString(); //검색어 설정

        if(mSearchText.isEmpty()) { //검색어가 없다면 토스트 메세지 표시
            Toast.makeText(getApplicationContext(), R.string.emptyMessage, Toast.LENGTH_LONG).show();
        }
        else {  //검색어가 있다면
            ClearWebResult();   //웹검색 결과 clear
            ClearImageResult(); //이미지검색 결과 clear

            if(mTabHost.getCurrentTab() == 0) {     //현재 탭이 Web탭이면
                mCurWebStart = 1;       //웹검색 시작위치 설정
                HideKeyboard();         //키보드 숨기기
                mProgressDlg.show();    //프로그레스 다이얼로그

                new Thread() {
                    public void run() {
                        WebSearch();
                    }
                }.start();
            } else {    //현재 탭이 Image탭이면
                mCurImageStart = 1;       //웹검색 시작위치 초기화
                HideKeyboard();         //키보드 숨기기
                mProgressDlg.show();    //프로그레스 다이얼로그

                new Thread() {
                    public void run() {
                        ImageSearch();
                    }
                }.start();
            }
        }
    }

    //검색 쓰레드 핸들러 구현
    private static class SearchHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;

        public SearchHandler(MainActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity activity = mActivity.get();

            switch(msg.what) {
                case WEBSEARCHSUCCESS:  //웹검색 성공
                    activity.mProgressDlg.dismiss();
                    activity.mWebListAdapter.notifyDataSetChanged();
                    break;
                case WEBSEARCHFAIL:     //웹검색 실패
                    activity.mProgressDlg.dismiss();
                    activity.mWebListAdapter.notifyDataSetChanged();
                    Toast.makeText(activity.getApplicationContext(), R.string.searchFailMessage, Toast.LENGTH_LONG).show();
                    break;
                case IMAGESEARCHSUCCESS:    //이미지검색 성공
                    activity.mProgressDlg.dismiss();
                    activity.mImageListAdapter.notifyDataSetChanged();
                    break;
                case IMAGESEARCHFAIL:       //이미지검색 실패
                    activity.mProgressDlg.dismiss();
                    activity.mImageListAdapter.notifyDataSetChanged();
                    Toast.makeText(activity.getApplicationContext(), R.string.searchFailMessage, Toast.LENGTH_LONG).show();
                    break;
            }
        }
    }

    //검색 쓰레드 핸들러 등록
    private Handler mHandler = new SearchHandler(this);

    //웹검색 구현
    private void WebSearch() {
        try {
            String text = URLEncoder.encode(mSearchText, "UTF-8");
            URL url = new URL(WEBSEARCHURL + "?query=" + text + "&display=" + WEBSEARCHDISPLAYNUM + "&start=" + mCurWebStart);

            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("X-Naver-Client-Id", NAVERAPICLIENTID);
            con.setRequestProperty("X-Naver-Client-Secret", NAVERAPICLIENTSECRET);

            if(con.getResponseCode() == 200) { // 정상 호출
                if(MakeWebItems(con.getInputStream())) {
                    mHandler.sendEmptyMessage(WEBSEARCHSUCCESS);
                } else {
                    mHandler.sendEmptyMessage(WEBSEARCHFAIL);
                }
            } else {  // 에러 발생
                mHandler.sendEmptyMessage(WEBSEARCHFAIL);
            }
        } catch (Exception e) {
            mHandler.sendEmptyMessage(WEBSEARCHFAIL);
        }
    }

    //이미지 검색 구현
    private void ImageSearch() {
        try {
            String text = URLEncoder.encode(mSearchText, "UTF-8");
            URL url = new URL(IMAGESEARCHURL + "?query=" + text + "&display=" + IMAGESEARCHDISPLAYNUM + "&start=" + mCurImageStart);

            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("X-Naver-Client-Id", NAVERAPICLIENTID);
            con.setRequestProperty("X-Naver-Client-Secret", NAVERAPICLIENTSECRET);

            if(con.getResponseCode() == 200) { // 정상 호출
                if(MakeImageItems(con.getInputStream())) {
                    mHandler.sendEmptyMessage(IMAGESEARCHSUCCESS);
                } else {
                    mHandler.sendEmptyMessage(IMAGESEARCHFAIL);
                }
            } else {  // 에러 발생
                mHandler.sendEmptyMessage(IMAGESEARCHFAIL);
            }
        } catch (Exception e) {
            mHandler.sendEmptyMessage(IMAGESEARCHFAIL);
        }
    }

    //Clear 버튼 실행
    private void ClearResult() {
        mSearchEdittext.setText("");
        mSearchText = null;
        ClearWebResult();
        ClearImageResult();
        HideKeyboard();
    }

    private void ClearWebResult() {
        mWebItems.clear();
        mTotalWebResultCount = 0;
        mCurWebStart = 0;
        mWebListview.setSelection(0);
        mWebListAdapter.notifyDataSetChanged();
    }

    private void ClearImageResult() {
        mImageItems.clear();
        mTotalImageResultCount = 0;
        mCurImageStart = 0;
        mImageListview.setSelection(0);
        mImageListAdapter.notifyDataSetChanged();
    }

    //웹검색 결과 파싱 및 item 생성
    private boolean MakeWebItems(InputStream input) {
        try {
            final int STEP_NONE = 0 ;
            final int STEP_TOTAL = 1 ;
            final int STEP_ITEM = 2 ;
            final int STEP_TITLE = 3 ;
            final int STEP_DESCRIPTION = 4 ;
            final int STEP_LINK = 5 ;

            int total = 0;
            String title = null;
            String description = null;
            String link = null;

            XmlPullParserFactory parserFactory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = parserFactory.newPullParser() ;

            int step = STEP_NONE ;

            parser.setInput(input, null) ;

            int eventType = parser.getEventType() ;
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) { //Tag 시작에 따라 STEP 설정
                    String startTag = parser.getName() ;
                    if (startTag.equals("item")) {
                        step = STEP_ITEM ;
                    } else if (startTag.equals("total")) {
                        step = STEP_TOTAL ;
                    } else if (startTag.equals("title") && step != STEP_NONE) {
                        step = STEP_TITLE ;
                    } else if (startTag.equals("description") && step != STEP_NONE) {
                        step = STEP_DESCRIPTION ;
                    } else if (startTag.equals("link") && step != STEP_NONE) {
                        step = STEP_LINK ;
                    } else {
                        step = STEP_NONE ;
                    }
                } else if (eventType == XmlPullParser.END_TAG) {    //Tag 종료시 item add
                    String endTag = parser.getName() ;
                    if(endTag.equals("total")) {
                        mTotalWebResultCount = total;
                    } else if(endTag.equals("item")) {
                        mWebItems.add(new WebResultItem(title, description, link));
                    }
                } else if (eventType == XmlPullParser.TEXT) {       //text 값 저장
                    String text = parser.getText() ;
                    if (step == STEP_TOTAL) {
                        total = Integer.parseInt(text);
                    } else if (step == STEP_TITLE) {
                        title = text;
                    } else if (step == STEP_DESCRIPTION) {
                        description = text;
                    } else if (step == STEP_LINK) {
                        link = text;
                    }
                }
                eventType = parser.next();
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace() ;
        }

        return false;
    }

    //이미지검색 결과 파싱 및 item 생성
    private boolean MakeImageItems(InputStream input) {
        try {
            final int STEP_NONE = 0;
            final int STEP_TOTAL = 1;
            final int STEP_ITEM = 2;
            final int STEP_TITLE = 3;
            final int STEP_LINK = 4;
            final int STEP_THUMBNAIL = 5;

            int total = 0;
            String title = null;
            String link = null;
            String thumbnail = null;

            XmlPullParserFactory parserFactory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = parserFactory.newPullParser() ;

            int step = STEP_NONE ;

            parser.setInput(input, null) ;

            int eventType = parser.getEventType() ;
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) { //Tag 시작에 따라 STEP 설정
                    String startTag = parser.getName() ;
                    if (startTag.equals("item")) {
                        step = STEP_ITEM ;
                    } else if (startTag.equals("total")) {
                        step = STEP_TOTAL ;
                    } else if (startTag.equals("title") && step != STEP_NONE) {
                        step = STEP_TITLE ;
                    } else if (startTag.equals("link") && step != STEP_NONE) {
                        step = STEP_LINK ;
                    } else if (startTag.equals("thumbnail") && step != STEP_NONE) {
                        step = STEP_THUMBNAIL ;
                    } else {
                        step = STEP_NONE ;
                    }
                } else if (eventType == XmlPullParser.END_TAG) {    //Tag 종료시 item add
                    String endTag = parser.getName() ;
                    if(endTag.equals("total")) {
                        mTotalImageResultCount = total;
                    } else if(endTag.equals("item")) {
                        mImageItems.add(new ImageResultItem(title, link, thumbnail));
                    }
                } else if (eventType == XmlPullParser.TEXT) {       //text 값 저장
                    String text = parser.getText() ;
                    if (step == STEP_TOTAL) {
                        total = Integer.parseInt(text);
                    } else if (step == STEP_TITLE) {
                        title = text;
                    } else if (step == STEP_LINK) {
                        link = text;
                    } else if (step == STEP_THUMBNAIL) {
                        thumbnail = text;
                    }
                }
                eventType = parser.next();
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace() ;
        }

        return false;
    }

    //이미지 상세화면 구현
    private void SetDetailView(int position) {
        ImageView imageview = findViewById(R.id.detailImage);
        Bitmap image = mImageItems.get(position).getImage();
        if(image == null) {
            imageview.setImageBitmap(null);
            new LoadImageTask(imageview, mImageItems.get(position), LoadImageTask.IMAGEMODE).execute(mImageItems.get(position).getImageLink());
        } else {
            imageview.setImageBitmap(image);
        }

        TextView title = findViewById(R.id.detailImageTitle);
        title.setText(Html.fromHtml(mImageItems.get(position).getTitle()));

        Button prevButton = findViewById(R.id.prevButton);
        Button nextButton = findViewById(R.id.nextButton);

        //현재 이미지 index에 따라 prev, next 버튼 enable 설정
        if(position == 0) {
            prevButton.setEnabled(false);
        } else {
            prevButton.setEnabled(true);
        }

        if(position == mImageItems.size() - 1) {
            nextButton.setEnabled(false);
        } else {
            nextButton.setEnabled(true);
        }
    }

    //키보드 숨기기
    private void HideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mSearchEdittext.getWindowToken(), 0);
    }
}
