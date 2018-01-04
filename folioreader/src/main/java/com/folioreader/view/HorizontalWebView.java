package com.folioreader.view;


import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;

import com.folioreader.ShowInterfacesControls;
import com.folioreader.ui.folio.fragment.EpubReaderFragment;
import com.folioreader.ui.folio.fragment.FolioPageFragment;
import com.folioreader.util.AppUtil;

public class HorizontalWebView extends WebView {

    private SeekBarListener mSeekBarListener;
    private ToolBarListener mToolBarListener;
    private ScrollListener mScrollListener;
    ShowInterfacesControls showInterfacesControls;

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
    }

    public HorizontalWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HorizontalWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public HorizontalWebView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mActivityCallback = epubReaderFragment;
        int action = event.getAction();
        int positionScroll = 0;
        switch (action) {
            case (MotionEvent.ACTION_DOWN):
                /**
                 * get X and Y position
                 */
                start_x = event.getX();
                start_y = event.getY();
                break;
            case (MotionEvent.ACTION_MOVE):
                if (getParent() != null) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }

                if (start_y - event.getY() > 20 || start_y - event.getY() < -20)
                    return true;
                else

                    break;

            case (MotionEvent.ACTION_UP):
                 /*right to left*/
                upX = event.getX();
                upY = event.getY();

                float deltaX = start_x - upX;
                float deltaY = start_y - upY;

                if (Math.abs(deltaX) > Math.abs(deltaY)) {
                    // HORIZONTAL SCROLL

                    if (Math.abs(deltaX) > 100) {
                        // left or right
                        if (deltaX < 0) {
                            //this.onLeftToRightSwipe();
                            return true;
                        }

                        if (deltaX > 0) {
                            //this.onRightToLeftSwipe();
                            return true;
                        }
                    } else {
                        return onTap(getRootView(), event);
                    }
                }

                showInterfacesControls.showInterfaceControls();
                break;
        }
        return super.onTouchEvent(event);
    }

    private boolean onTap(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            float x = event.getX();
            float viewWidth = v.getWidth();

            float screenPartSide = ((viewWidth * 30) / 100);
            float screenPartCenter = ((viewWidth * 40) / 100);

            if (x <= screenPartSide) {
                Log.d(TAG, "onTouchEvent: LTR ScrollY " + getScrollY());
                Log.d(TAG, "onTouchEvent: LTR Height" + getContentHeightVal());

                if (getScrollY() > 0) {
                    turnPageLeft();
                } else if (getCurrentPage() == 0 && getScrollY() > 0) {
                    scrollTo(0, 0);
                } else {
                    epubReaderFragment.loadPrevPage();
                    AppUtil.setGetCurrentchapterPage(1);
                }

                return true;
            } else if (x >= (screenPartSide + screenPartCenter)) {
                Log.d(TAG, "onTouchEvent: RTL ScrollY " + getScrollY());
                Log.d(TAG, "onTouchEvent: RTL Height" + getContentHeightVal());

                if (AppUtil.getGetCurrentchapterPage() + 1 < getTotalPages()) {
                    turnPageRight();
                } else {
                    epubReaderFragment.loadNextPage();
                    AppUtil.setGetCurrentchapterPage(1);
                }

                return true;
            } else {
                // this.onTapCenter();
                return true;
            }
        }

        return false;
    }

    private void turnPageLeft() {
        int currentPage = getPageIndex();
        int previousPage = currentPage - getWebviewHeight();
            /*Below condition is to show the first page completely without large padding the content*/

        scrollTo(0, previousPage);

        PAGE_LEFT_COUNT++;

        if (PAGE_RIGHT_COUNT > 3)
            PAGE_RIGHT_COUNT--;
    }

    private int getPrevPagePosition() {
        int prevPage = (int) AppUtil.getPageIndex() - getWebviewHeight();
        Log.d(TAG, "getPrevPagePosition: " + prevPage);
        return prevPage;
    }

    private void turnPageRight() {
        int currentPage = getPageIndex();
        int nextPage = currentPage + getWebviewHeight();

        scrollTo(0, nextPage);

       /*     *//*Below condition is to show the last page completely without large padding content*//*
        if (getCurrentPage() == (getTotalPages() - 1)) {
            scrollTo(0, scrollY);
        } else {
            scrollTo(0, scrollY - (PAGE_RIGHT_COUNT));
        }*/

        PAGE_RIGHT_COUNT++;

        if (PAGE_LEFT_COUNT > 3)
            PAGE_LEFT_COUNT--;
    }

    private int getNextPagePosition() {
        int nextPage = getCurrentPage() + 1;
        Log.d(TAG, "getNextPagePosition: " + nextPage);
        return (int) Math.ceil(nextPage * getWebviewHeight());
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

    private int getPageIndex() {
        //TODO Melhorar essa formular para que aceite inteiros corretamente
        if (AppUtil.getGetCurrentchapterPage() == 0) {
            return getWebviewHeight();
        } else {
            double scrollToPage = (getWebviewHeight() * (AppUtil.getGetCurrentchapterPage() - 1));
            return (int) scrollToPage;
        }
    }

}