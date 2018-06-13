package com.bong.naverelabshomework;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ConfigurationInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.Layout;
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
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final String WEBSEARCHURL = "https://openapi.naver.com/v1/search/webkr.xml";
    private static final String IMAGESEARCHURL = "https://openapi.naver.com/v1/search/image.xml";
    private static final String NAVERAPICLIENTID = "1xIRbfr15DX614C_Yzox";
    private static final String NAVERAPICLIENTSECRET = "w1eaVmOTw5";

    private static final int WEBSEARCHDISPLAYNUM = 20;
    private static final int IMAGESEARCHDISPLAYNUM = 40;

    private static final int WEBSEARCHSUCCESS = 100;
    private static final int WEBSEARCHFAIL = 101;
    private static final int IMAGESEARCHSUCCESS = 102;
    private static final int IMAGESEARCHFAIL = 103;

    private LinearLayout mDetailImageLayout = null;
    private LinearLayout mTabLayout = null;

    private EditText mSearchEdittext = null;
    private Editable mSearchText = null;
    private TabHost mTabHost = null;
    private TabWidget mTabWidget = null;
    private FrameLayout mTabContent = null;
    private ListView mWebListview = null;
    private WebResultAdapter mWebListAdapter = null;
    private GridView mImageListview = null;
    private ImageResultAdapter mImageListAdapter = null;

    private ProgressDialog mProgressDlg = null;

    private ArrayList<WebResultItem> mWebItems = null;
    private int mTotalWebResultCount = 0;
    private int mCurWebStart = 0;

    private ArrayList<ImageResultItem> mImageItems = null;
    private int mTotalImageResultCount = 0;
    private int mCurImageStart = 0;
    private int mCurDetailView = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDetailImageLayout = (LinearLayout) findViewById(R.id.detailviewLayout);
        mDetailImageLayout.setVisibility(View.INVISIBLE);

        mTabLayout = (LinearLayout) findViewById(R.id.tabLayout);

        mSearchEdittext = (EditText) findViewById(R.id.searchEdittext);
        mSearchEdittext.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if(i == EditorInfo.IME_ACTION_DONE) {
                   Search();
                }
                return false;
            }
        });

        mSearchText = mSearchEdittext.getText();

        //검색버튼 리스터등록
        Button searchButton = (Button) findViewById(R.id.searchButton);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Search();
            }
        });

        //클리어 버튼 리스너 등록
        Button clearButton = (Button) findViewById(R.id.clearButton);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClearResult();
            }
        });

        //탭 구성
        mTabHost = (TabHost) findViewById(R.id.tabHost);
        mTabHost.setup();

        mTabWidget = mTabHost.getTabWidget();
//        mTabWidget.setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener() {
//            @Override
//            public void onChildViewRemoved(View parent, View child) {
//
//            }
//
//            @Override
//            public void onChildViewAdded(View parent, View child) {
//                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);
//                child.setLayoutParams(lp);
//            }
//        });

        mTabContent = mTabHost.getTabContentView();

        TabHost.TabSpec webTs = mTabHost.newTabSpec("WebTab");
        webTs.setContent(R.id.webResultView);
        webTs.setIndicator(getString(R.string.webTab));
        mTabHost.addTab(webTs);

        TabHost.TabSpec imageTs = mTabHost.newTabSpec("ImageTab");
        imageTs.setContent(R.id.imageResultView);
        imageTs.setIndicator(getString(R.string.imageTab));
        mTabHost.addTab(imageTs);
        mTabHost.setOnTabChangedListener(mTabChangedListener);

        //웹검색 결과 리스트 구성
        if(mWebItems == null) {mWebItems = new ArrayList<WebResultItem>();}
        mWebListAdapter = new WebResultAdapter(this, R.layout.item_web_result, mWebItems);
        mWebListview = (ListView) findViewById(R.id.webResultView);
        mWebListview.setAdapter(mWebListAdapter);
        mWebListview.setOnItemClickListener(mWebListClickListener);
        mWebListview.setOnScrollListener(mWebListScrollListener);

        //이미지검색 결과 리스트 구성
        if(mImageItems == null) {mImageItems = new ArrayList<ImageResultItem>();}
        mImageListAdapter = new ImageResultAdapter(this, R.layout.item_image_result, mImageItems);
        mImageListview = (GridView) findViewById(R.id.imageResultView);
        mImageListview.setAdapter(mImageListAdapter);
        mImageListview.setOnItemClickListener(mImageListClickListener);
        mImageListview.setOnScrollListener(mImageListScrollListener);

        Button closeButton = (Button) findViewById(R.id.closeButton);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDetailImageLayout.setVisibility(View.INVISIBLE);
            }
        });

        Button backButton = (Button) findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mCurDetailView > 0) {
                    mCurDetailView--;
                }
                SetDetailView(mCurDetailView);
            }
        });

        Button forwardButton = (Button) findViewById(R.id.forwardButton);
        forwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mCurDetailView < mTotalImageResultCount - 1) {
                    mCurDetailView++;
                }
                SetDetailView(mCurDetailView);
            }
        });

        mProgressDlg = new ProgressDialog(this);
        mProgressDlg.setMessage(getString(R.string.searchingMessage));
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if(newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
//            mTabHost.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f));
            mTabLayout.setOrientation(LinearLayout.VERTICAL);
            mTabWidget.setOrientation(LinearLayout.HORIZONTAL);
            mTabWidget.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0.0f));
            mTabWidget.getChildTabViewAt(0).setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f));
            mTabWidget.getChildTabViewAt(1).setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f));
            mTabContent.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f));

        } else if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            mTabHost.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f));
            mTabLayout.setOrientation(LinearLayout.HORIZONTAL);
            mTabWidget.setOrientation(LinearLayout.VERTICAL);
            mTabWidget.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT, 0.0f));
            mTabWidget.getChildTabViewAt(0).setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 0, 1.0f));
            mTabWidget.getChildTabViewAt(1).setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 0, 1.0f));
            mTabContent.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f));
        }
    }

    private TabHost.OnTabChangeListener mTabChangedListener = new TabHost.OnTabChangeListener() {
        @Override
        public void onTabChanged(String s) {
            if(!mSearchText.toString().isEmpty()) {
                if(s.equals("WebTab") && mCurWebStart == 0) {
                    mCurWebStart = 1;       //웹검색 시작위치 초기화
                    mProgressDlg.show();    //프로그레스 다이얼로그

                    new Thread() {
                        public void run() {
                            WebSearch();
                        }
                    }.start();
                } else if(s.equals("ImageTab") && mCurImageStart == 0){
                    mCurImageStart = 1;       //웹검색 시작위치 초기화
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

    //웹검색 클릭 리스트너 구현
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
            //화면에 보이는 첫번째 아이템의 번호+화면에 보이는 아이템의 개수가 리스트 전체의 개수보다 크거나 같을때
            isEndPos = (itemcount > 0) && (firstitem + itemcount >= totalitemcount);
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            //스크롤이 멈춘 상태이고 end position 이면
            if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && isEndPos) {
                mCurWebStart = mWebItems.size() + 1;

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

    //이미지검색 클릭 리스트너 구현
    private AdapterView.OnItemClickListener mImageListClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            mDetailImageLayout.setVisibility(View.VISIBLE);
            mCurDetailView = i;
            SetDetailView(i);
        }
    };

    //이미지검색 추가로딩 기능 구현
    private AbsListView.OnScrollListener mImageListScrollListener = new AbsListView.OnScrollListener() {
        boolean isEndPos = false;        //화면에 리스트의 마지막 아이템이 보여지는지 체크

        @Override
        public void onScroll(AbsListView view, int firstitem, int itemcount, int totalitemcount) {
            //화면에 보이는 첫번째 아이템의 번호+화면에 보이는 아이템의 개수가 리스트 전체의 개수보다 크거나 같을때
            isEndPos = (itemcount > 0) && (firstitem + itemcount >= totalitemcount);
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            //스크롤이 멈춘 상태이고 end position 이면
            if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && isEndPos) {
                mCurImageStart = mImageItems.size() + 1;

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

    private void Search() {
        mSearchText = mSearchEdittext.getText();

        if(mSearchText.toString().isEmpty()) {
            Toast.makeText(getApplicationContext(), R.string.emptyMessage, Toast.LENGTH_LONG).show();
        }
        else {
            ClearWebResult();
            ClearImageResult();

            if(mTabHost.getCurrentTabTag().equals("WebTab")) {
                mCurWebStart = 1;       //웹검색 시작위치 설정
                HideKeyboard();         //키보드 숨기기
                mProgressDlg.show();    //프로그레스 다이얼로그

                new Thread() {
                    public void run() {
                        WebSearch();
                    }
                }.start();
            }
            else {
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

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case WEBSEARCHSUCCESS:
                    mProgressDlg.dismiss();
                    mWebListAdapter.notifyDataSetChanged();
                    break;
                case WEBSEARCHFAIL:
                    mProgressDlg.dismiss();
                    mWebListAdapter.notifyDataSetChanged();
                    Toast.makeText(getApplicationContext(), R.string.searchFailMessage, Toast.LENGTH_LONG).show();
                    break;
                case IMAGESEARCHSUCCESS:
                    mProgressDlg.dismiss();
                    mImageListAdapter.notifyDataSetChanged();
                    break;
                case IMAGESEARCHFAIL:
                    mProgressDlg.dismiss();
                    mImageListAdapter.notifyDataSetChanged();
                    Toast.makeText(getApplicationContext(), R.string.searchFailMessage, Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };

    private void WebSearch() {
        try {
            String text = URLEncoder.encode(mSearchText.toString(), "UTF-8");
            URL url = new URL(WEBSEARCHURL + "?query=" + text + "&display=" + WEBSEARCHDISPLAYNUM + "&start=" + mCurWebStart);

            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("X-Naver-Client-Id", NAVERAPICLIENTID);
            con.setRequestProperty("X-Naver-Client-Secret", NAVERAPICLIENTSECRET);

            if(con.getResponseCode() == 200) { // 정상 호출
                if(MakeWebItems(con.getInputStream())) {
                    mHandler.sendEmptyMessage(WEBSEARCHSUCCESS);
                }
                else {
                    mHandler.sendEmptyMessage(WEBSEARCHFAIL);
                }
            } else {  // 에러 발생
                mHandler.sendEmptyMessage(WEBSEARCHFAIL);
            }
        } catch (Exception e) {
            System.out.println(e);
            mHandler.sendEmptyMessage(WEBSEARCHFAIL);
        }
    }

    private void ImageSearch() {
        try {
            String text = URLEncoder.encode(mSearchText.toString(), "UTF-8");
            URL url = new URL(IMAGESEARCHURL + "?query=" + text + "&display=" + IMAGESEARCHDISPLAYNUM + "&start=" + mCurImageStart);

            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("X-Naver-Client-Id", NAVERAPICLIENTID);
            con.setRequestProperty("X-Naver-Client-Secret", NAVERAPICLIENTSECRET);

            if(con.getResponseCode() == 200) { // 정상 호출
                if(MakeImageItems(con.getInputStream())) {
                    mHandler.sendEmptyMessage(IMAGESEARCHSUCCESS);
                }
                else {
                    mHandler.sendEmptyMessage(IMAGESEARCHFAIL);
                }
            } else {  // 에러 발생
                mHandler.sendEmptyMessage(IMAGESEARCHFAIL);
            }
        } catch (Exception e) {
            System.out.println(e);
            mHandler.sendEmptyMessage(IMAGESEARCHFAIL);
        }
    }

    private void ClearResult() {
        mSearchEdittext.setText("");
        mSearchText.clear();
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
                if (eventType == XmlPullParser.START_DOCUMENT) {
                    // XML 데이터 시작
                } else if (eventType == XmlPullParser.START_TAG) {
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
                } else if (eventType == XmlPullParser.END_TAG) {
                    String endTag = parser.getName() ;
                    if(endTag.equals("total")) {
                        mTotalWebResultCount = total;
                    } else if(endTag.equals("item")) {
                        mWebItems.add(new WebResultItem(title, description, link));
                    }
                } else if (eventType == XmlPullParser.TEXT) {
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
                if (eventType == XmlPullParser.START_DOCUMENT) {
                    // XML 데이터 시작
                } else if (eventType == XmlPullParser.START_TAG) {
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
                } else if (eventType == XmlPullParser.END_TAG) {
                    String endTag = parser.getName() ;
                    if(endTag.equals("total")) {
                        mTotalImageResultCount = total;
                    } else if(endTag.equals("item")) {
                        mImageItems.add(new ImageResultItem(title, link, thumbnail));
                    }
                } else if (eventType == XmlPullParser.TEXT) {
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

    private void SetDetailView(int position) {
        ImageView imageview = (ImageView) findViewById(R.id.detailImage);
        Bitmap image = mImageItems.get(position).getImage();
        if(image == null) {
            imageview.setImageBitmap(null);
            new LoadImageTask(imageview, mImageItems.get(position), LoadImageTask.IMAGEMODE).execute(mImageItems.get(position).getImageLink());
        } else {
            imageview.setImageBitmap(image);
        }

        TextView title = (TextView) findViewById(R.id.detailImageTitle);
        title.setText(Html.fromHtml(mImageItems.get(position).getTitle()));

        Button backButton = (Button) findViewById(R.id.backButton);
        Button forwardButton = (Button) findViewById(R.id.forwardButton);

        if(position == 0) {
            backButton.setEnabled(false);
        } else {
            backButton.setEnabled(true);
        }

        if(position == mImageItems.size() - 1) {
            forwardButton.setEnabled(false);
        } else {
            forwardButton.setEnabled(true);
        }
    }

    private void HideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mSearchEdittext.getWindowToken(), 0);
    }
}
