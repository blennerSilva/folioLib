package com.folioreader.view;


import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.webkit.WebView;

import com.folioreader.ShowInterfacesControls;
import com.folioreader.TouchDetector;
import com.folioreader.ui.folio.fragment.EpubReaderFragment;
import com.folioreader.ui.folio.fragment.FolioPageFragment;
import com.folioreader.util.AppUtil;

public class HorizontalWebView extends WebView {

    private SeekBarListener mSeekBarListener;
    private ToolBarListener mToolBarListener;
    private ScrollListener mScrollListener;
    ShowInterfacesControls showInterfacesControls;
    private TouchDetector touchDetector;
    private boolean touchLeft;
    private boolean touchRight;

    public interface ScrollListener {
        void onScrollChange(int percent);
    }

    public interface SeekBarListener {
        void fadeInSeekBarIfInvisible();
    }

    public interface ToolBarListener {
        void hideOrshowToolBar();

        void hideToolBarIfVisible();
    }


    public void setShowInterfacesControls(ShowInterfacesControls showInterfacesControls) {
        this.showInterfacesControls = showInterfacesControls;
    }

    public void setScrollListener(ScrollListener listener) {
        mScrollListener = listener;
    }

    public void setSeekBarListener(SeekBarListener listener) {
        mSeekBarListener = listener;
    }

    public void setToolBarListener(ToolBarListener listener) {
        mToolBarListener = listener;
    }

    private static final String TAG = "HorizontalWebView";
    private ActionMode.Callback mActionModeCallback;
    private EpubReaderFragment epubReaderFragment;
    private FolioPageFragment.FolioPageFragmentCallback mActivityCallback;
    private float start_x;
    private float start_y;
    private float upX, upY;
    private int current_y = 0;
    private int PAGE_LEFT_COUNT = 3;
    private int PAGE_RIGHT_COUNT = 3;
    private int PAGE_PADDING = 20;


    public HorizontalWebView(Context context) {
        super(context);
        iniciateTouchDetector();
    }

    public HorizontalWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        iniciateTouchDetector();
    }

    public HorizontalWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        iniciateTouchDetector();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public HorizontalWebView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        iniciateTouchDetector();
    }


    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        mActivityCallback = epubReaderFragment;
        if (mScrollListener != null) mScrollListener.onScrollChange(t);
        super.onScrollChanged(l, t, oldl, oldt);
    }

    public int getContentHeightVal() {
        return (int) Math.floor(this.getContentHeight() * this.getScale());
    }

    public int getWebviewHeight() {
        return this.getMeasuredHeight();
    }

    @Override
    public ActionMode startActionMode(ActionMode.Callback callback, int type) {
        return this.dummyActionMode();
    }

    @Override
    public ActionMode startActionMode(ActionMode.Callback callback) {
        return this.dummyActionMode();
    }

    public ActionMode dummyActionMode() {
        return new ActionMode() {
            @Override
            public void setTitle(CharSequence title) {
            }

            @Override
            public void setTitle(int resId) {
            }

            @Override
            public void setSubtitle(CharSequence subtitle) {
            }

            @Override
            public void setSubtitle(int resId) {
            }

            @Override
            public void setCustomView(View view) {
            }

            @Override
            public void invalidate() {
            }

            @Override
            public void finish() {
            }

            @Override
            public Menu getMenu() {
                return null;
            }

            @Override
            public CharSequence getTitle() {
                return null;
            }

            @Override
            public CharSequence getSubtitle() {
                return null;
            }

            @Override
            public View getCustomView() {
                return null;
            }

            @Override
            public MenuInflater getMenuInflater() {
                return null;
            }
        };
    }

    public void setFragment(EpubReaderFragment epubReaderFragment) {
        this.epubReaderFragment = epubReaderFragment;
    }

    public void turnPageLeft() {
        int currentPage = getPageIndexFromTouch();
        int previousPage = currentPage - getWebviewHeight();
            /*Below condition is to show the first page completely without large padding the content*/

        scrollTo(0, previousPage);

        if (previousPage == 0 || previousPage == getWebviewHeight()) {
            AppUtil.setCurrentchapterPage(1);
        }

        PAGE_LEFT_COUNT++;

        if (PAGE_RIGHT_COUNT > 3)
            PAGE_RIGHT_COUNT--;
    }

    private void turnPageRight() {
        int currentPage = getPageIndexFromTouch();
        int nextPage = currentPage + getWebviewHeight();

        scrollTo(0, nextPage);

        PAGE_RIGHT_COUNT++;

        if (PAGE_LEFT_COUNT > 3)
            PAGE_LEFT_COUNT--;
    }

    public int getCurrentPage() {
        int currentPage = (int) (Math.ceil((double) current_y / getWebviewHeight()));
        Log.d(TAG, "setCurrentPage: " + currentPage);
        return currentPage;
    }

    public int getTotalPages() {
        int totalPages = (int) Math.ceil((double) getContentHeightVal() / getWebviewHeight());
        Log.d(TAG, "getTotalPages: " + totalPages);
        return totalPages;
    }

    public int getPageIndexFromTouch() {
        //TODO Melhorar essa formular para que aceite inteiros corretamente
        if (AppUtil.getGetCurrentchapterPage() == 0 && getScrollY() != 0) {
            if (touchRight) {
                return getScrollY() + getWebviewHeight();
            } else {
                return getScrollY() - getWebviewHeight();
            }

        } else if (AppUtil.getGetCurrentchapterPage() == 0) {
            return getWebviewHeight();
        } else {
            double scrollToPage = (getWebviewHeight() * (AppUtil.getGetCurrentchapterPage() - 1));
            return (int) scrollToPage;
        }
    }

    private void iniciateTouchDetector() {
        touchDetector = new TouchDetector(this);
        touchDetector.setListener(new TouchDetector.OnTouchEventListener() {
            @Override
            public void onTouchEventDetected(View v, TouchDetector.TouchTypeEnum touchType) {
                if (touchType.equals(TouchDetector.TouchTypeEnum.LEFT_TO_RIGHT)) {
                    touchRight = false;
                    touchLeft = true;
                    if (getScrollY() > 0) {
                        turnPageLeft();
                    } else if (getCurrentPage() == 0 && getScrollY() > 0) {
                        scrollTo(0, 0);
                    } else {
                        epubReaderFragment.loadPrevPage();
                        AppUtil.setCurrentchapterPage(1);
                    }
                } else if (touchType.equals(TouchDetector.TouchTypeEnum.RIGHT_TO_LEFT)) {
                    touchRight = true;
                    touchLeft = false;

                    if (AppUtil.getGetCurrentchapterPage() + 1 < getTotalPages()) {
                        turnPageRight();
                    } else {
                        epubReaderFragment.loadNextPage();
                        AppUtil.setCurrentchapterPage(1);
                    }
                } else if (touchType.equals(TouchDetector.TouchTypeEnum.TAP_LEFT)) {
                    touchRight = false;
                    touchLeft = true;

                    if (getScrollY() > 0) {
                        turnPageLeft();
                    } else if (getCurrentPage() == 0 && getScrollY() > 0) {
                        scrollTo(0, 0);
                    } else {
                        epubReaderFragment.loadPrevPage();
                        AppUtil.setCurrentchapterPage(1);
                    }
                } else if (touchType.equals(TouchDetector.TouchTypeEnum.TAP_RIGHT)) {
                    touchRight = true;
                    touchLeft = false;

                    if (AppUtil.getGetCurrentchapterPage() + 1 < getTotalPages()) {
                        turnPageRight();
                    } else {
                        epubReaderFragment.loadNextPage();
                        AppUtil.setCurrentchapterPage(1);
                    }
                } else if (touchType.equals(TouchDetector.TouchTypeEnum.TAP_CENTER)) {
                    showInterfacesControls.showInterfaceControls();
                }
            }
        });
    }

}