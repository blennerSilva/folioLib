package com.folioreader.ui.folio.fragment;


import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.folioreader.ChapterHasChangedListener;
import com.folioreader.Config;
import com.folioreader.Constants;
import com.folioreader.PageHasChangedListener;
import com.folioreader.PageHasFinishedLoading;
import com.folioreader.R;
import com.folioreader.ShowInterfacesControls;
import com.folioreader.model.TOCLinkWrapper;
import com.folioreader.model.event.GetTOCLinkWrapper;
import com.folioreader.model.event.GoToBookMarkEvent;
import com.folioreader.model.event.GoToChapterEvent;
import com.folioreader.model.event.GoToPageEvent;
import com.folioreader.model.event.JumpToBookmarkPage;
import com.folioreader.model.event.MediaOverlayPlayPauseEvent;
import com.folioreader.model.event.PopulateTOCItems;
import com.folioreader.ui.folio.adapter.FolioPageFragmentAdapter;
import com.folioreader.ui.folio.presenter.MainMvpView;
import com.folioreader.ui.folio.presenter.MainPresenter;
import com.folioreader.ui.tableofcontents.presenter.TOCMvpView;
import com.folioreader.ui.tableofcontents.presenter.TableOfContentsPresenter;
import com.folioreader.util.AppUtil;
import com.folioreader.util.FileUtil;
import com.folioreader.util.FolioReader;
import com.folioreader.view.ConfigBottomSheetDialogFragment;
import com.folioreader.view.DirectionalViewpager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.readium.r2_streamer.model.container.Container;
import org.readium.r2_streamer.model.container.EpubContainer;
import org.readium.r2_streamer.model.publication.EpubPublication;
import org.readium.r2_streamer.model.publication.link.Link;
import org.readium.r2_streamer.server.EpubServer;
import org.readium.r2_streamer.server.EpubServerSingleton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EpubReaderFragment extends Fragment implements TOCMvpView, FolioPageFragment.FolioPageFragmentCallback, ConfigBottomSheetDialogFragment.ConfigDialogCallback, MainMvpView {

    private static final String TAG = "FolioActivity";

    public static final String INTENT_EPUB_SOURCE_PATH = "com.folioreader.epub_asset_path";
    public static final String INTENT_EPUB_SOURCE_TYPE = "epub_source_type";
    public static final String INTENT_HIGHLIGHTS_LIST = "highlight_list";
    private Bundle args;

    @Override
    public void onLoadTOC(ArrayList<TOCLinkWrapper> tocLinkWrapperList) {
        tocLinkWrapperArrayList = tocLinkWrapperList;
        Log.d("Gerou lista", "tocLinkWrapper");
    }

    public enum EpubSourceType {
        RAW,
        ASSETS,
        SD_CARD
    }

    private boolean isOpen = true;

    public static final int ACTION_CONTENT_HIGHLIGHT = 77;
    private String bookFileName;
    private static final String HIGHLIGHT_ITEM = "highlight_item";

    public boolean mIsActionBarVisible;
    private DirectionalViewpager mFolioPageViewPager;
    private Toolbar mToolbar;

    private int mChapterPosition;
    private FolioPageFragmentAdapter mFolioPageFragmentAdapter;
    private int mWebViewScrollPosition;
    private ConfigBottomSheetDialogFragment mConfigBottomSheetDialogFragment;
    private TextView title;

    private List<Link> mSpineReferenceList = new ArrayList<>();
    private EpubServer mEpubServer;

    private Animation slide_down;
    private Animation slide_up;
    private boolean mIsNightMode;
    private Config mConfig;
    private String mBookId;
    private String mEpubFilePath;
    private EpubReaderFragment.EpubSourceType mEpubSourceType;
    int mEpubRawId = 0;
    private ShowInterfacesControls showInterfacesControls;
    private PageHasChangedListener pageHasChangedListener;
    private ChapterHasChangedListener chapterHasChangedListener;
    private TableOfContentsPresenter presenter;
    private ArrayList<TOCLinkWrapper> tocLinkWrapperArrayList;
    private PageHasFinishedLoading pageHasFinishedLoading;
    private boolean comingFromBookMark = true;

    public EpubReaderFragment() {
        // Required empty public constructor
    }

    public void setShowInterfacesControls(ShowInterfacesControls showInterfacesControls) {
        this.showInterfacesControls = showInterfacesControls;
    }


    public void setPageHasChangedListener(PageHasChangedListener pageHasChangedListener) {
        this.pageHasChangedListener = pageHasChangedListener;
    }

    public void setChapterHasChangedListener(ChapterHasChangedListener chapterHasChangedListener) {
        this.chapterHasChangedListener = chapterHasChangedListener;
    }

    public void setPageHasFinishedLoading(PageHasFinishedLoading pageHasFinishedLoading) {
        this.pageHasFinishedLoading = pageHasFinishedLoading;
    }

    public static EpubReaderFragment newInstance(String assetOrSdcardPath, EpubSourceType type) {
        EpubReaderFragment epubReaderFragment = new EpubReaderFragment();
        Bundle args = new Bundle();
        args.putString(INTENT_EPUB_SOURCE_PATH, assetOrSdcardPath);
        args.putSerializable(INTENT_EPUB_SOURCE_TYPE, type);

        epubReaderFragment.setArguments(args);
        return epubReaderFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter = new TableOfContentsPresenter(this);
        tocLinkWrapperArrayList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.folio_activity, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        args = getArguments();
        AppUtil.setEpubReaderFragment(EpubReaderFragment.this);

        mBookId = args.getString(FolioReader.INTENT_BOOK_ID);
        mEpubSourceType = (EpubSourceType) args.getSerializable(INTENT_EPUB_SOURCE_TYPE);
        if (mEpubSourceType.equals(EpubReaderFragment.EpubSourceType.RAW)) {
            mEpubRawId = args.getInt(EpubReaderFragment.INTENT_EPUB_SOURCE_PATH);
        } else {
            mEpubFilePath = args.getString(EpubReaderFragment.INTENT_EPUB_SOURCE_PATH);
        }

        setConfig();

        if (!mConfig.isShowTts()) {
            getActivity().findViewById(R.id.btn_speaker).setVisibility(View.GONE);
        }

        title = (TextView) getActivity().findViewById(R.id.lbl_center);
        slide_down = AnimationUtils.loadAnimation(getActivity().getApplicationContext(),
                R.anim.slide_down);
        slide_up = AnimationUtils.loadAnimation(getActivity().getApplicationContext(),
                R.anim.slide_up);

        setupBook();
        mToolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);

        mIsNightMode = mConfig.isNightMode();
        if (mIsNightMode) {
            mToolbar.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.black));
            title.setTextColor(ContextCompat.getColor(getActivity(), R.color.white));
        }
    }

    private void initBook(String mEpubFileName, int mEpubRawId, String mEpubFilePath, EpubReaderFragment.EpubSourceType mEpubSourceType) {
        try {
            int portNumber = args.getInt(Config.INTENT_PORT, Constants.PORT_NUMBER);
            mEpubServer = EpubServerSingleton.getEpubServerInstance(portNumber);
            mEpubServer.start();
            addEpub(mEpubFilePath);

            String urlString = Constants.LOCALHOST + bookFileName + "/manifest";
            new MainPresenter(this).parseManifest(urlString);

            presenter.getTOCContent(urlString);

        } catch (IOException e) {
            Log.e(TAG, "initBook failed", e);
        }
    }

    private void addEpub(String path) throws IOException {
        Container epubContainer = new EpubContainer(path);
        mEpubServer.addEpub(epubContainer, "/" + bookFileName);
        getEpubResource();
    }

    private void getEpubResource() {
    }

    public void setmChapterPosition(int mChapterPosition) {
        this.mChapterPosition = mChapterPosition;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        configDrawerLayoutButtons();
    }

    private void configFolio() {
        if (getView() != null) {
            mFolioPageViewPager = getView().findViewById(R.id.folioPageViewPager);
            mFolioPageViewPager.setOnPageChangeListener(new DirectionalViewpager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    EventBus.getDefault().post(new MediaOverlayPlayPauseEvent(mSpineReferenceList.get(mChapterPosition).href, false, true));
                    mChapterPosition = position;
                    AppUtil.setGetChapterPosition(mChapterPosition);
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                    if (state == DirectionalViewpager.SCROLL_STATE_IDLE) {
                        title.setText(mSpineReferenceList.get(mChapterPosition).bookTitle);
                        chapterHasChangedListener.chapterHasChanged();
                    }
                }
            });

            if (mSpineReferenceList != null) {
                mFolioPageFragmentAdapter = new FolioPageFragmentAdapter(getFragmentManager(), mSpineReferenceList, bookFileName, mBookId, this);
                mFolioPageFragmentAdapter.setShowInterfacesControls(new ShowInterfacesControls() {
                    @Override
                    public void showInterfaceControls() {
                        showInterfacesControls.showInterfaceControls();
                    }
                });

                mFolioPageFragmentAdapter.setPageHasChangedListener(new PageHasChangedListener() {
                    @Override
                    public void pageHasChanged() {
                        pageHasChangedListener.pageHasChanged();
                        Log.d("pageHasChanged", "EpubFragment");
                    }
                });

                mFolioPageFragmentAdapter.setPageHasFinishedLoading(new PageHasFinishedLoading() {
                    @Override
                    public void pageHasFinishedLoading() {
                        pageHasFinishedLoading.pageHasFinishedLoading();
                        Log.d("pageHasFinishedLoading", "EpubFragment");
                    }
                });
                mFolioPageViewPager.setAdapter(mFolioPageFragmentAdapter);

            }

            if (AppUtil.checkPreviousBookStateExist(getActivity(), bookFileName)) {
                mFolioPageViewPager.setCurrentItem(AppUtil.getPreviousBookStatePosition(getActivity(), bookFileName));
            }
        }
    }

    private void configDrawerLayoutButtons() {
        getActivity().findViewById(R.id.btn_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveBookState();
                getActivity().finish();
            }
        });

    }

    private void saveBookState() {
        if (mSpineReferenceList.size() > 0) {
            AppUtil.saveBookState(getActivity(), bookFileName, mFolioPageViewPager.getCurrentItem(), mWebViewScrollPosition);
        }
    }

    @Override
    public void onError() {

    }

    @Override
    public void onLoadPublication(EpubPublication publication) {
        mSpineReferenceList.addAll(publication.spines);
        if (publication.metadata.title != null) {
            title.setText(publication.metadata.title);
        }

        if (mBookId == null) {
            if (publication.metadata.identifier != null) {
                mBookId = publication.metadata.identifier;
            } else {
                if (publication.metadata.title != null) {
                    mBookId = String.valueOf(publication.metadata.title.hashCode());
                } else {
                    mBookId = String.valueOf(bookFileName.hashCode());
                }
            }
        }
        configFolio();
    }

    @Override
    public void onOrientationChange(int orentation) {
        if (orentation == 0) {
            mFolioPageViewPager.setDirection(DirectionalViewpager.Direction.VERTICAL);
            mFolioPageFragmentAdapter = new FolioPageFragmentAdapter(getFragmentManager(), mSpineReferenceList, bookFileName, mBookId, EpubReaderFragment.this);
            mFolioPageViewPager.setAdapter(mFolioPageFragmentAdapter);
            mFolioPageViewPager.setOffscreenPageLimit(1);
            mFolioPageViewPager.setCurrentItem(mChapterPosition);

        } else {
            mFolioPageViewPager.setDirection(DirectionalViewpager.Direction.HORIZONTAL);
            mFolioPageFragmentAdapter = new FolioPageFragmentAdapter(getFragmentManager(), mSpineReferenceList, bookFileName, mBookId, EpubReaderFragment.this);
            mFolioPageViewPager.setAdapter(mFolioPageFragmentAdapter);
            mFolioPageViewPager.setCurrentItem(mChapterPosition);
        }
    }

    @Override
    public void setPagerToPosition(String href) {

    }

    @Override
    public void setLastWebViewPosition(int position) {
        this.mWebViewScrollPosition = position;
    }

    @Override
    public void goToChapter(String href) {
        /*href = href.substring(href.indexOf(bookFileName + "/") + bookFileName.length() + 1);
        for (Link spine : mSpineReferenceList) {
            if (spine.href.contains(href)) {
                mChapterPosition = mSpineReferenceList.indexOf(spine);
                mFolioPageViewPager.setCurrentItem(mChapterPosition);
                title.setText(spine.getChapterTitle());
                break;
            }
        }*/
    }

    public int getmChapterPosition() {
        return mChapterPosition;
    }

    private void setConfig() {
        if (AppUtil.getSavedConfig(getActivity()) != null) {
            mConfig = AppUtil.getSavedConfig(getActivity());
        } else if (args.getParcelable(Config.INTENT_CONFIG) != null) {
            mConfig = args.getParcelable(Config.INTENT_CONFIG);
            AppUtil.saveConfig(getActivity(), mConfig);
        } else {
            mConfig = new Config.ConfigBuilder().build();
            AppUtil.saveConfig(getActivity(), mConfig);
        }
    }

    private void setupBook() {
        bookFileName = FileUtil.getEpubFilename(getActivity(), mEpubSourceType, mEpubFilePath, mEpubRawId);
        initBook(bookFileName, mEpubRawId, mEpubFilePath, mEpubSourceType);
    }

    public void loadNextPage() {
        if (mFolioPageViewPager.getCurrentItem() < mFolioPageFragmentAdapter.getCount()) {
            mFolioPageViewPager.setCurrentItem(mFolioPageViewPager.getCurrentItem() + 1, true);
        }
    }

    public void loadPrevPage() {
        if (mFolioPageViewPager.getCurrentItem() > 0) {
            mFolioPageViewPager.setCurrentItem(mFolioPageViewPager.getCurrentItem() - 1, true);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        saveBookState();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mEpubServer != null) {
            mEpubServer.stop();
        }
    }


    @Override
    public void onPause() {
        super.onPause();
    }


    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
       /* if (requestCode == ACTION_CONTENT_HIGHLIGHT && resultCode == RESULT_OK && data.hasExtra(TYPE)) {

            String type = data.getStringExtra(TYPE);
            if (type.equals(CHAPTER_SELECTED)) {
                String selectedChapterHref = data.getStringExtra(SELECTED_CHAPTER_POSITION);
                for (Link spine : mSpineReferenceList) {
                    if (selectedChapterHref.contains(spine.href)) {
                        mChapterPosition = mSpineReferenceList.indexOf(spine);
                        mFolioPageViewPager.setCurrentItem(mChapterPosition);
                        title.setText(data.getStringExtra(Constants.BOOK_TITLE));
                        EventBus.getDefault().post(new AnchorIdEvent(selectedChapterHref));
                        break;
                    }
                }
            } else if (type.equals(HIGHLIGHT_SELECTED)) {
                HighlightImpl highlightImpl = data.getParcelableExtra(HIGHLIGHT_ITEM);
                int position = highlightImpl.getPageNumber();
                mFolioPageViewPager.setCurrentItem(position);
                EventBus.getDefault().post(new WebViewPosition(mSpineReferenceList.get(mChapterPosition).href, highlightImpl.getRangy()));
            }
        }*/
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case Constants.WRITE_EXTERNAL_STORAGE_REQUEST:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setupBook();
                } else {
                    Toast.makeText(getActivity(), getString(R.string.cannot_access_epub_message), Toast.LENGTH_LONG).show();
                    getActivity().finish();
                }
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onPopulateTOCItems(PopulateTOCItems event) {
        AppUtil.setChapterList(mSpineReferenceList);
        AppUtil.setBookId(mBookId);
        AppUtil.setBookFileName(bookFileName);
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onGoToChapterEvent(GoToChapterEvent event) {
        String selectedChapterHref = event.getSelectedChapterPosition();
        for (Link spine : mSpineReferenceList) {
            if (selectedChapterHref.contains(spine.href)) {
                mChapterPosition = mSpineReferenceList.indexOf(spine);
                mFolioPageViewPager.setCurrentItem(mChapterPosition);
                break;
            }
        }
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onGoToBookMarkEvent(GoToBookMarkEvent event) {
        String selectedChapterHref = event.getChapterPosition();
        for (Link spine : mSpineReferenceList) {
            if (selectedChapterHref.contains(spine.href)) {
                mChapterPosition = mSpineReferenceList.indexOf(spine);
                mFolioPageViewPager.setCurrentItem(mChapterPosition);
                EventBus.getDefault().post(new JumpToBookmarkPage(String.valueOf(mChapterPosition), event.getPageNumber()));
                break;
            }
        }
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onJumpToBookmarkPageEvent(JumpToBookmarkPage event) {
        EventBus.getDefault().post(new GoToPageEvent(event.getPageNumber()));
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onGetTOCLinkWrapper(GetTOCLinkWrapper event) {
        AppUtil.setTocLinkWrapper(getCurrentChapterReference(event.getHref()));
    }

    private TOCLinkWrapper getCurrentChapterReference(String SpineItemhref) {
        if (tocLinkWrapperArrayList != null) {
            for (TOCLinkWrapper tocLinkWrapper : tocLinkWrapperArrayList) {
                if (tocLinkWrapper.getTocLink().href.contains(SpineItemhref)) {
                    return tocLinkWrapper;
                }
            }
        }

        return null;
    }

}
