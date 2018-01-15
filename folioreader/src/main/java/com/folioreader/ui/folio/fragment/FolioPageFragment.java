package com.folioreader.ui.folio.fragment;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.folioreader.Config;
import com.folioreader.Constants;
import com.folioreader.PageHasChangedListener;
import com.folioreader.PageHasFinishedLoading;
import com.folioreader.R;
import com.folioreader.ShowInterfacesControls;
import com.folioreader.model.HighLight;
import com.folioreader.model.HighlightImpl;
import com.folioreader.model.event.ChangeFontEvent;
import com.folioreader.model.event.ChangeThemeEvent;
import com.folioreader.model.event.GoToPageEvent;
import com.folioreader.model.event.JumpToAnchorPoint;
import com.folioreader.model.event.MediaOverlayHighlightStyleEvent;
import com.folioreader.model.event.MediaOverlayPlayPauseEvent;
import com.folioreader.model.event.MediaOverlaySpeedEvent;
import com.folioreader.model.event.PopulateTOCItems;
import com.folioreader.model.event.ReloadDataEvent;
import com.folioreader.model.event.RewindIndexEvent;
import com.folioreader.model.event.SetWebViewToPositionEvent;
import com.folioreader.model.event.WebViewPosition;
import com.folioreader.model.sqlite.HighLightTable;
import com.folioreader.ui.base.HtmlTask;
import com.folioreader.ui.base.HtmlTaskCallback;
import com.folioreader.ui.base.HtmlUtil;
import com.folioreader.ui.folio.mediaoverlay.MediaController;
import com.folioreader.ui.folio.mediaoverlay.MediaControllerCallbacks;
import com.folioreader.util.AppUtil;
import com.folioreader.util.FolioReader;
import com.folioreader.util.HighlightUtil;
import com.folioreader.util.SMILParser;
import com.folioreader.util.UiUtil;
import com.folioreader.view.HorizontalWebView;
import com.folioreader.view.ObservableWebView;
import com.folioreader.view.VerticalSeekbar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.readium.r2_streamer.model.publication.link.Link;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by mahavir on 4/2/16.
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class FolioPageFragment extends Fragment implements HtmlTaskCallback, MediaControllerCallbacks, ObservableWebView.SeekBarListener {

    public static final String KEY_FRAGMENT_FOLIO_POSITION = "com.folioreader.ui.folio.fragment.FolioPageFragment.POSITION";
    public static final String KEY_FRAGMENT_FOLIO_BOOK_TITLE = "com.folioreader.ui.folio.fragment.FolioPageFragment.BOOK_TITLE";
    public static final String KEY_FRAGMENT_EPUB_FILE_NAME = "com.folioreader.ui.folio.fragment.FolioPageFragment.EPUB_FILE_NAME";
    public static final String STRING_CONTEXT_NAME = "epubReaderFragment";
    private static final String KEY_IS_SMIL_AVAILABLE = "com.folioreader.ui.folio.fragment.FolioPageFragment.IS_SMIL_AVAILABLE";
    public static final String TAG = FolioPageFragment.class.getSimpleName();

    private static final String SPINE_ITEM = "spine_item";
    private static final int FADE_DAY_NIGHT_MODE = 500;

    private String mHtmlString = null;
    private boolean hasMediaOverlay = false;
    private String mAnchorId;
    private String rangy = "";
    private String highlightId;
    private boolean mIsNightMode = false;
    private ShowInterfacesControls showInterfacesControls;
    private PageHasChangedListener pageHasChangedListener;
    private PageHasFinishedLoading pageHasFinishedLoading;
    private int totalPages;

    public interface FolioPageFragmentCallback {

        void setPagerToPosition(String href);

        void setLastWebViewPosition(int position);

        void goToChapter(String href);
    }

    public void setShowInterfacesControls(ShowInterfacesControls showInterfacesControls) {
        this.showInterfacesControls = showInterfacesControls;
    }

    public void setPageHasChangedListener(PageHasChangedListener pageHasChangedListener) {
        this.pageHasChangedListener = pageHasChangedListener;
    }

    public void setPageHasFinishedLoading(PageHasFinishedLoading pageHasFinishedLoading) {
        this.pageHasFinishedLoading = pageHasFinishedLoading;
    }

    private View mRootView;

    private VerticalSeekbar mScrollSeekbar;
    private HorizontalWebView mWebview;
    private TextView mPagesLeftTextView, mMinutesLeftTextView;
    private FolioPageFragmentCallback mActivityCallback;
    private EpubReaderFragment epubReaderFragment;

    private int mScrollY;
    private int mTotalMinutes;
    private String mSelectedText;
    private Animation mFadeInAnimation, mFadeOutAnimation;

    private Link spineItem;
    private int mPosition = -1;
    private String mBookTitle;
    private String mEpubFileName = null;
    private int mPos;
    private boolean mIsPageReloaded;
    private int mLastWebviewScrollpos;

    private String highlightStyle;

    private MediaController mediaController;
    private Config mConfig;
    private String mBookId;

    public void setEpubReaderFragment(EpubReaderFragment epubReaderFragment) {
        this.epubReaderFragment = epubReaderFragment;
    }

    public static FolioPageFragment newInstance(int position, String bookTitle, Link spineRef, String bookId, EpubReaderFragment epubReaderFragment) {
        FolioPageFragment fragment = new FolioPageFragment();
        Bundle args = new Bundle();
        args.putInt(KEY_FRAGMENT_FOLIO_POSITION, position);
        args.putString(KEY_FRAGMENT_FOLIO_BOOK_TITLE, bookTitle);
        args.putString(FolioReader.INTENT_BOOK_ID, bookId);
        args.putSerializable(SPINE_ITEM, spineRef);

        fragment.setEpubReaderFragment(epubReaderFragment);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if ((savedInstanceState != null)
                && savedInstanceState.containsKey(KEY_FRAGMENT_FOLIO_POSITION)
                && savedInstanceState.containsKey(KEY_FRAGMENT_FOLIO_BOOK_TITLE)) {
            mPosition = savedInstanceState.getInt(KEY_FRAGMENT_FOLIO_POSITION);
            mBookTitle = savedInstanceState.getString(KEY_FRAGMENT_FOLIO_BOOK_TITLE);
            mEpubFileName = savedInstanceState.getString(KEY_FRAGMENT_EPUB_FILE_NAME);
            mBookId = getArguments().getString(FolioReader.INTENT_BOOK_ID);
            spineItem = (Link) savedInstanceState.getSerializable(SPINE_ITEM);
        } else {
            mPosition = getArguments().getInt(KEY_FRAGMENT_FOLIO_POSITION);
            mBookTitle = getArguments().getString(KEY_FRAGMENT_FOLIO_BOOK_TITLE);
            mEpubFileName = getArguments().getString(KEY_FRAGMENT_EPUB_FILE_NAME);
            spineItem = (Link) getArguments().getSerializable(SPINE_ITEM);
            mBookId = getArguments().getString(FolioReader.INTENT_BOOK_ID);
        }
        if (spineItem != null) {
            if (spineItem.properties.contains("media-overlay")) {
                mediaController = new MediaController(getActivity(), MediaController.MediaType.SMIL, this);
                hasMediaOverlay = true;
            } else {
                mediaController = new MediaController(getActivity(), MediaController.MediaType.TTS, this);
                mediaController.setTextToSpeech(getActivity());
            }
        }

        highlightStyle = HighlightImpl.HighlightStyle.classForStyle(HighlightImpl.HighlightStyle.Normal);
        mRootView = View.inflate(getActivity(), R.layout.folio_page_fragment, null);

        Activity activity = getActivity();
        mConfig = AppUtil.getSavedConfig(activity);

        if (activity instanceof FolioPageFragmentCallback)
            mActivityCallback = (FolioPageFragmentCallback) activity;

        initSeekbar();
        initAnimations();
        initWebView();

        return mRootView;
    }


    private String getWebviewUrl() {
        return Constants.LOCALHOST + mBookTitle + "/" + spineItem.href;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        float positionTopView = mWebview.getTop();
        float contentHeight = mWebview.getContentHeight();
        float currentScrollPosition = mScrollY;
        float percentWebview = (currentScrollPosition - positionTopView) / contentHeight;
        float webviewsize = mWebview.getContentHeight() - mWebview.getTop();
        float positionInWV = webviewsize * percentWebview;
        mScrollY = Math.round(mWebview.getTop() + positionInWV);
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void pauseButtonClicked(MediaOverlayPlayPauseEvent event) {
        if (isAdded() && spineItem.href.equals(event.getHref())) {
            mediaController.stateChanged(event);
        }
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void speedChanged(MediaOverlaySpeedEvent event) {
        mediaController.setSpeed(event.getSpeed());
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void styleChanged(MediaOverlayHighlightStyleEvent event) {
        if (isAdded()) {
            switch (event.getStyle()) {
                case DEFAULT:
                    highlightStyle = HighlightImpl.HighlightStyle.classForStyle(HighlightImpl.HighlightStyle.Normal);
                    break;
                case UNDERLINE:
                    highlightStyle = HighlightImpl.HighlightStyle.classForStyle(HighlightImpl.HighlightStyle.DottetUnderline);
                    break;
                case BACKGROUND:
                    highlightStyle = HighlightImpl.HighlightStyle.classForStyle(HighlightImpl.HighlightStyle.TextColor);
                    break;
            }
            mWebview.loadUrl(String.format(getString(R.string.setmediaoverlaystyle), highlightStyle));
        }
    }

    /**
     * [EVENT BUS FUNCTION]
     * Function triggered when any EBook configuration is changed.
     *
     * @param reloadDataEvent empty POJO.
     */

    @SuppressWarnings("unused")
    @Subscribe
    public void onReload(ReloadDataEvent reloadDataEvent) {
        if (isAdded()) {
            mLastWebviewScrollpos = mWebview.getScrollY();
            mIsPageReloaded = true;
            setHtml(true);
        }
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onFontSize(ChangeFontEvent changeFontEvent) {
        if (isAdded()) {
            mConfig.setFontSize(changeFontEvent.getFontSize());
            AppUtil.saveConfig(getActivity(), mConfig);

            EventBus.getDefault().post(new ReloadDataEvent());
        }
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void themeChoice(ChangeThemeEvent changeThemeEvent) {
        switch (changeThemeEvent.getTheme()) {
            case DAY_THEME:
                if (mIsNightMode) {
                    mIsNightMode = true;
                    toggleBlackTheme();
                }
                break;
            case NIGHT_THEME:
                if (!mIsNightMode) {
                    mIsNightMode = false;
                    toggleBlackTheme();
                }
        }
    }

    private void toggleBlackTheme() {
        int day = getResources().getColor(R.color.white);
        int night = getResources().getColor(R.color.night);
        int darkNight = getResources().getColor(R.color.dark_night);
        final int diffNightDark = night - darkNight;

        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(),
                mIsNightMode ? night : day, mIsNightMode ? day : night);
        colorAnimation.setDuration(FADE_DAY_NIGHT_MODE);
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                int value = (int) animator.getAnimatedValue();
                mRootView.setBackgroundColor(value);
            }
        });

        colorAnimation.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                mIsNightMode = !mIsNightMode;
                mConfig.setNightMode(mIsNightMode);
                AppUtil.saveConfig(getActivity(), mConfig);
                EventBus.getDefault().post(new ReloadDataEvent());
            }

            @Override
            public void onAnimationCancel(Animator animator) {
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }
        });

        colorAnimation.setDuration(FADE_DAY_NIGHT_MODE);
        colorAnimation.start();
    }

    @Override
    public void onReceiveHtml(String html) {
        if (isAdded()) {
            mHtmlString = html;
            setHtml(false);
        }
    }

    private void setHtml(boolean reloaded) {
        if (spineItem != null) {
            String ref = spineItem.href;
            if (!reloaded && spineItem.properties.contains("media-overlay")) {
                mediaController.setSMILItems(SMILParser.parseSMIL(mHtmlString));
                mediaController.setUpMediaPlayer(spineItem.mediaOverlay, spineItem.mediaOverlay.getAudioPath(spineItem.href), mBookTitle);
            }
            mConfig = AppUtil.getSavedConfig(getActivity());
            String path = "";
            if (ref.contains("OEBPS/Text/")) {
                path = ref.substring(0, ref.lastIndexOf('/'));
            } else {
                path = ref.substring(0, ref.lastIndexOf('/') + 1);
            }
            mWebview.loadDataWithBaseURL(
                    Constants.LOCALHOST + mBookTitle + "/" + path + "/",
                    HtmlUtil.getHtmlContent(getActivity(), mHtmlString, mConfig),
                    "text/html",
                    "UTF-8",
                    null);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        mediaController.stop();
        EventBus.getDefault().unregister(this);
        //TODO save last media overlay item
    }

    private void initWebView() {
        mWebview = mRootView.findViewById(R.id.contentWebView);
        mWebview.setFragment(epubReaderFragment);
        mWebview.setVerticalScrollBarEnabled(false);
        mWebview.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        Log.d("CONTENTHEIGHT", "Altura" + mWebview.getContentHeightVal());

        if (getActivity() instanceof HorizontalWebView.ToolBarListener)
            mWebview.setToolBarListener((HorizontalWebView.ToolBarListener) getActivity());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        setupScrollBar();
        mWebview.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                int height =
                        (int) Math.floor(mWebview.getContentHeight() * mWebview.getScale());
                int webViewHeight = mWebview.getMeasuredHeight();
                mScrollSeekbar.setMaximum(height - webViewHeight);
            }
        });

        mWebview.getSettings().setJavaScriptEnabled(true);
        mWebview.setVerticalScrollBarEnabled(false);
        mWebview.getSettings().setAllowFileAccess(true);
        mWebview.setShowInterfacesControls(new ShowInterfacesControls() {
            @Override
            public void showInterfaceControls() {
                showInterfacesControls.showInterfaceControls();
            }
        });

        mWebview.setHorizontalScrollBarEnabled(false);

        mWebview.addJavascriptInterface(this, "Highlight");
        mWebview.setScrollListener(new HorizontalWebView.ScrollListener() {
            @Override
            public void onScrollChange(int percent) {
                if (mWebview.getScrollY() != 0) {
                    mScrollY = mWebview.getScrollY();
                    if (isAdded()) {
                        epubReaderFragment.setLastWebViewPosition(mScrollY);
                        mWebview.getPageIndexFromTouch();
                        updatePagesLeftText(percent);

                        int currentPage = (int) (Math.ceil((double) percent / mWebview.getWebviewHeight()) + 1);
                        AppUtil.setCurrentchapterPage(currentPage);
                    }
                }

                updatePagesLeftText(percent);
                pageHasChangedListener.pageHasChanged();
            }
        });

        mWebview.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                if (isAdded()) {
                    if (isCurrentFragment()) {
                        if (AppUtil.isComeFromChapterList()) {
                            setWebViewPosition(0);
                            AppUtil.setCurrentchapterPage(1);
                            AppUtil.setComeFromChapterList(false);
                            AppUtil.setComeFromInternalChange(true);
                            updatePagesLeftText(0);
                        } else if (AppUtil.isComeFromBookmark()) {
                            setWebViewPosition(AppUtil.getGetCurrentchapterPage());
                            AppUtil.setComeFromBookmark(false);
                            updatePagesLeftText(AppUtil.getGetCurrentchapterPage());
                        } else {
                            setWebViewPosition(AppUtil.getGetCurrentchapterPage());
                            updatePagesLeftText(AppUtil.getGetCurrentchapterPage());
                        }

                        pageHasFinishedLoading.pageHasFinishedLoading();

                        if (mWebview.getContentHeightVal() == 0) {
                            mWebview.turnPageLeft();
                        }
                    } else if (mIsPageReloaded) {
                        setWebViewPosition(mLastWebviewScrollpos);
                        pageHasFinishedLoading.pageHasFinishedLoading();
                        AppUtil.setCurrentchapterPage(mLastWebviewScrollpos);
                        updatePagesLeftText(mLastWebviewScrollpos);
                        mIsPageReloaded = false;
                    }

                    EventBus.getDefault().post(new PopulateTOCItems());
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (!url.isEmpty() && url.length() > 0) {
                    if (Uri.parse(url).getScheme().startsWith("highlight")) {
                        final Pattern pattern = Pattern.compile(getString(R.string.pattern));
                        try {
                            String htmlDecode = URLDecoder.decode(url, "UTF-8");
                            Matcher matcher = pattern.matcher(htmlDecode.substring(12));
                            if (matcher.matches()) {
                                double left = Double.parseDouble(matcher.group(1));
                                double top = Double.parseDouble(matcher.group(2));
                                double width = Double.parseDouble(matcher.group(3));
                                double height = Double.parseDouble(matcher.group(4));
                            }
                        } catch (UnsupportedEncodingException e) {
                            Log.d(TAG, e.getMessage());
                        }
                    } else {
                        if (url.contains("storage")) {
                            //  mActivityCallback.setPagerToPosition(url);
                        } else if (url.endsWith(".xhtml") || url.endsWith(".html")) {
                            // mActivityCallback.goToChapter(url);
                        } else {
                            // Otherwise, give the default behavior (open in browser)
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                            startActivity(intent);
                        }
                    }
                }

                return true;
            }


            // prevent favicon.ico to be loaded automatically
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                if (url.toLowerCase().contains("/favicon.ico")) {
                    try {
                        return new WebResourceResponse("image/png", null, null);
                    } catch (Exception e) {
                        Log.e(TAG, "shouldInterceptRequest failed", e);
                    }
                }
                return null;
            }

            // prevent favicon.ico to be loaded automatically
            @Override
            @SuppressLint("NewApi")
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                if (!request.isForMainFrame() && request.getUrl().getPath().endsWith("/favicon.ico")) {
                    try {
                        return new WebResourceResponse("image/png", null, null);
                    } catch (Exception e) {
                        Log.e(TAG, "shouldInterceptRequest failed", e);
                    }
                }
                return null;
            }
        });

        mWebview.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int progress) {
                if (view.getProgress() == 100) {
                    mWebview.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("scroll y", "Scrolly" + mScrollY);
                            mWebview.scrollTo(0, mScrollY);
                        }
                    }, 100);
                }

            }

            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                return true;
            }
        });

        mWebview.getSettings().setDefaultTextEncodingName("utf-8");
        new HtmlTask(this).execute(getWebviewUrl());
    }

    private void loadRangy(WebView view, String rangy) {
        view.loadUrl(String.format("javascript:if(typeof ssReader !== \"undefined\"){ssReader.setHighlights('%s');}", rangy));
    }

    private void setupScrollBar() {
        UiUtil.setColorToImage(getActivity(), mConfig.getThemeColor(), mScrollSeekbar.getProgressDrawable());
        Drawable thumbDrawable = ContextCompat.getDrawable(getActivity(), R.drawable.icons_sroll);
        UiUtil.setColorToImage(getActivity(), mConfig.getThemeColor(), (thumbDrawable));
        mScrollSeekbar.setThumb(thumbDrawable);
    }

    private void initSeekbar() {
        mScrollSeekbar = (VerticalSeekbar) mRootView.findViewById(R.id.scrollSeekbar);
        mScrollSeekbar.getProgressDrawable()
                .setColorFilter(getResources()
                                .getColor(R.color.app_green),
                        PorterDuff.Mode.SRC_IN);
    }

    private void updatePagesLeftText(int scrollY) {
        try {
            int currentPage = (int) (Math.ceil((double) scrollY / mWebview.getWebviewHeight()) + 1);
            totalPages = (int) Math.ceil((double) mWebview.getContentHeightVal() / mWebview.getWebviewHeight());

            if (currentPage > totalPages) {
                currentPage = totalPages;
                AppUtil.setCurrentchapterPage(currentPage);
            } else {
                AppUtil.setCurrentchapterPage(currentPage);
            }
        } catch (java.lang.ArithmeticException exp) {
            Log.d("divide error", exp.toString());
        }
    }

    private void initAnimations() {
        mFadeInAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.fadein);
        mFadeInAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mScrollSeekbar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                fadeOutSeekBarIfVisible();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mFadeOutAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.fadeout);
        mFadeOutAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mScrollSeekbar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    public void fadeInSeekBarIfInvisible() {
        if (mScrollSeekbar.getVisibility() == View.INVISIBLE ||
                mScrollSeekbar.getVisibility() == View.GONE) {
            mScrollSeekbar.startAnimation(mFadeInAnimation);
        }
    }

    public void fadeOutSeekBarIfVisible() {
        if (mScrollSeekbar.getVisibility() == View.VISIBLE) {
            mScrollSeekbar.startAnimation(mFadeOutAnimation);
        }
    }

    @Override
    public void onDestroyView() {
        mFadeInAnimation.setAnimationListener(null);
        mFadeOutAnimation.setAnimationListener(null);
        super.onDestroyView();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_FRAGMENT_FOLIO_POSITION, mPosition);
        outState.putString(KEY_FRAGMENT_FOLIO_BOOK_TITLE, mBookTitle);
        outState.putString(KEY_FRAGMENT_EPUB_FILE_NAME, mEpubFileName);
        outState.putSerializable(SPINE_ITEM, spineItem);
    }

    public void highlight(HighlightImpl.HighlightStyle style, boolean isCreated) {
        if (isCreated) {
            mWebview.loadUrl(String.format("javascript:if(typeof ssReader !== \"undefined\"){ssReader.highlightSelection('%s');}", HighlightImpl.HighlightStyle.classForStyle(style)));
        } else {
            mWebview.loadUrl(String.format("javascript:alert(setHighlightStyle('%s'))", "highlight_" + HighlightImpl.HighlightStyle.classForStyle(style)));
        }
    }

    public void highlightRemove() {
        mWebview.loadUrl("javascript:alert(removeThisHighlight())");
    }

    @Override
    public void resetCurrentIndex() {
        /*if (isCurrentFragment()) {
            mWebview.loadUrl("javascript:alert(rewindCurrentIndex())");
        }*/
    }

    @SuppressWarnings("unused")
    @JavascriptInterface
    public void onReceiveHighlights(String html) {
        if (html != null) {
            rangy = HighlightUtil.createHighlightRangy(getActivity().getApplicationContext(),
                    html,
                    mBookId,
                    getPageName(),
                    mPosition,
                    rangy);
        }
    }


    private String getPageName() {
        return mBookTitle + "$" + spineItem.href;
    }

    private String getChapterName(String rangy) {
        AppUtil.setSpineItemHref(spineItem.href);
        AppUtil.setBookTile(mBookTitle);
        AppUtil.setRangy(rangy);
        return mBookTitle + "$" + spineItem.href;
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void setWebView(final WebViewPosition position) {
        if (position.getHref().equals(spineItem.href) && isAdded()) {
            highlightId = position.getHighlightId();

            if (mWebview.getContentHeight() > 0) {
                scrollToHighlightId();
                mWebview.loadUrl(String.format(getString(R.string.goto_highlight), highlightId));
            }
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.POSTING, sticky = true)
    public void onGoToPage(final GoToPageEvent event) {
        setChapterPagePosition(event.getPageNumber());
        EventBus.getDefault().removeStickyEvent(true);
    }


    public void setChapterPagePosition(final int position) {
        AppUtil.setCurrentchapterPage(position);
        mWebview.scrollTo(0, getPageIndex());
    }


    @Override
    public void highLightText(String fragmentId) {
        mWebview.loadUrl(String.format(getString(R.string.audio_mark_id), fragmentId));
    }

    @Override
    public void highLightTTS() {
        mWebview.loadUrl("javascript:alert(getSentenceWithIndex('epub-media-overlay-playing'))");
    }

    @JavascriptInterface
    public void getUpdatedHighlightId(String id, String style) {
        if (id != null) {
            HighlightImpl highlightImpl = HighLightTable.updateHighlightStyle(id, style);
            if (highlightImpl != null) {
                HighlightUtil.sendHighlightBroadcastEvent(
                        getActivity().getApplicationContext(),
                        highlightImpl,
                        HighLight.HighLightAction.MODIFY);
            }
            final String rangyString = HighlightUtil.generateRangyString(getPageName());
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    // loadRangy(mWebview, rangyString);
                }
            });

        }
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void resetCurrentIndex(RewindIndexEvent resetIndex) {
        if (isCurrentFragment()) {
            mWebview.loadUrl("javascript:alert(rewindCurrentIndex())");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mWebview != null) mWebview.destroy();
    }

    private boolean isCurrentFragment() {
        return isAdded() && epubReaderFragment.getmChapterPosition() == mPos;
    }

    public void setFragmentPos(int pos) {
        mPos = pos;
        AppUtil.setPosition(pos);
    }

    @Override
    public void onError() {
    }

    private void scrollToHighlightId() {
        mWebview.loadUrl(String.format(getString(R.string.goto_highlight), highlightId));
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onJumpToAnchorPoint(JumpToAnchorPoint event) {
        if (isAdded() && event != null && event.getHref() != null) {
            String href = event.getHref();
            EventBus.getDefault().removeStickyEvent(event);
            if (href != null && href.indexOf('#') != -1 && spineItem.href.equals(href.substring(0, href.lastIndexOf('#')))) {
                mAnchorId = href.substring(href.lastIndexOf('#') + 1);
                if (mWebview.getContentHeight() > 0 && mAnchorId != null) {
                    mWebview.loadUrl("javascript:document.getElementById(\"" + mAnchorId + "\").scrollIntoView()");
                    EventBus.getDefault().removeStickyEvent(event);
                }
            }
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSetWebViewToPositionEvent(final SetWebViewToPositionEvent event) {
        // setWebViewPosition(event.getPosition());
    }

    private int getPageIndex() {
        //TODO Melhorar essa formular para que aceite inteiros corretamente
        double scrollToPage = (mWebview.getWebviewHeight() * (AppUtil.getGetCurrentchapterPage() - 1));
        return (int) scrollToPage;
    }

    private int getTotalPages() {
        return (int) Math.ceil((double) mWebview.getContentHeightVal() / mWebview.getWebviewHeight());
    }

    public void setWebViewPosition(final int position) {
        Log.d("TotalPages", String.valueOf(totalPages));
        mWebview.scrollTo(0, getPageIndex());
    }

}
