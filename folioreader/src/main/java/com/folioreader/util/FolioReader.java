package com.folioreader.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.folioreader.ChapterHasChangedListener;
import com.folioreader.PageHasChangedListener;
import com.folioreader.PageHasFinishedLoading;
import com.folioreader.ShowInterfacesControls;
import com.folioreader.model.HighLight;
import com.folioreader.model.HighlightImpl;
import com.folioreader.model.TOCLinkWrapper;
import com.folioreader.model.event.ChangeFontEvent;
import com.folioreader.model.event.ChangeThemeEvent;
import com.folioreader.model.event.GetTOCLinkWrapper;
import com.folioreader.model.event.GoToBookMarkEvent;
import com.folioreader.model.event.GoToChapterEvent;
import com.folioreader.model.event.LoadPauseEvent;
import com.folioreader.model.event.SetWebViewToPositionEvent;
import com.folioreader.model.sqlite.DbAdapter;
import com.folioreader.ui.base.OnSaveHighlight;
import com.folioreader.ui.base.SaveReceivedHighlightTask;
import com.folioreader.ui.folio.fragment.EpubReaderFragment;

import org.greenrobot.eventbus.EventBus;
import org.readium.r2_streamer.model.publication.link.Link;

import java.util.List;

/**
 * Created by avez raj on 9/13/2017.
 */

public class FolioReader {
    public static final String INTENT_BOOK_ID = "book_id";
    private Context context;

    private OnHighlightListener onHighlightListener;
    private ShowInterfacesControls showInterfacesControls;
    private PageHasChangedListener pageHasChangedListener;
    private ChapterHasChangedListener chapterHasChangedListener;
    private PageHasFinishedLoading pageHasFinishedLoading;


    public void showInterfaceControls(ShowInterfacesControls showInterfacesControls) {
        this.showInterfacesControls = showInterfacesControls;
    }

    public void PageHasChangedListener(PageHasChangedListener pageHasChangedListener) {
        this.pageHasChangedListener = pageHasChangedListener;
    }

    public void chapterHasChangedListener(ChapterHasChangedListener chapterHasChangedListener) {
        this.chapterHasChangedListener = chapterHasChangedListener;
    }

    public void setPageHasFinishedLoading(PageHasFinishedLoading pageHasFinishedLoading) {
        this.pageHasFinishedLoading = pageHasFinishedLoading;
    }

    public FolioReader(Context context) {
        this.context = context;
        new DbAdapter(context);
        LocalBroadcastManager.getInstance(context).registerReceiver(highlightReceiver,
                new IntentFilter(HighlightImpl.BROADCAST_EVENT));
    }

    private BroadcastReceiver highlightReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            HighlightImpl highlightImpl = intent.getParcelableExtra(HighlightImpl.INTENT);
            HighLight.HighLightAction action = (HighLight.HighLightAction)
                    intent.getSerializableExtra(HighLight.HighLightAction.class.getName());
            if (onHighlightListener != null && highlightImpl != null && action != null) {
                onHighlightListener.onHighlight(highlightImpl, action);
            }
        }
    };

    public void openBook(String assetOrSdcardPath, int contentId, Context context) {
        getFragmentFromUrl(assetOrSdcardPath, contentId, context);
    }

    private void getFragmentFromUrl(String assetOrSdcardPath, int contentId, Context context) {
        FragmentActivity activity = (FragmentActivity) context;

        EpubReaderFragment epubReaderFragment = EpubReaderFragment.newInstance(assetOrSdcardPath, EpubReaderFragment.EpubSourceType.SD_CARD);
        epubReaderFragment.setShowInterfacesControls(new ShowInterfacesControls() {
            @Override
            public void showInterfaceControls() {
                showInterfacesControls.showInterfaceControls();
            }
        });

        epubReaderFragment.setPageHasChangedListener(new PageHasChangedListener() {
            @Override
            public void pageHasChanged() {
                pageHasChangedListener.pageHasChanged();
                Log.d("pageHasChanged", "FolioReader");
            }
        });

        epubReaderFragment.setChapterHasChangedListener(new ChapterHasChangedListener() {
            @Override
            public void chapterHasChanged() {
                chapterHasChangedListener.chapterHasChanged();
                Log.d("chapterHasChanged", "FolioReader");
            }
        });

        epubReaderFragment.setPageHasFinishedLoading(new PageHasFinishedLoading() {
            @Override
            public void pageHasFinishedLoading() {
                pageHasFinishedLoading.pageHasFinishedLoading();
                Log.d("pageHasFinishedLoading", "FolioReader");
            }
        });

        android.support.v4.app.FragmentManager fragmentManager = activity.getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(getContentLayoutToReplace(contentId), epubReaderFragment);
        fragmentTransaction.commit();
    }

    private int getContentLayoutToReplace(int contanteId) {
        return contanteId;
    }

    public void registerHighlightListener(OnHighlightListener onHighlightListener) {
        this.onHighlightListener = onHighlightListener;
    }

    public void unregisterHighlightListener() {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(highlightReceiver);
        this.onHighlightListener = null;
    }

    public void saveReceivedHighLights(List<HighLight> highlights, OnSaveHighlight onSaveHighlight) {
        new SaveReceivedHighlightTask(onSaveHighlight, highlights).execute();
    }

    public void setFontSize(int fontSize) {
        EventBus.getDefault().post(new ChangeFontEvent(fontSize));
    }

    public void setThemeChoiceNight() {
        EventBus.getDefault().post(new ChangeThemeEvent(ChangeThemeEvent.Theme.NIGHT_THEME));
    }

    public void setThemeChoiceDay() {
        EventBus.getDefault().post(new ChangeThemeEvent(ChangeThemeEvent.Theme.DAY_THEME));
    }

    public void setThemeChoiceAfternoon() {
        EventBus.getDefault().post(new ChangeThemeEvent(ChangeThemeEvent.Theme.AFTERNOON_THEME));
    }

    public void setCurrentPage(int pageIndex) {
        AppUtil.setCurrentPage(pageIndex);
    }

    public int getCurrentPage() {
        return AppUtil.getCurrentPage();
    }

    public List<Link> getSpineList() {
        return AppUtil.getChapterList();
    }

    public int getChapterPosition() {
        return AppUtil.getChapterPosition();
    }

    public String getBookId() {
        return AppUtil.getBookId();
    }

    public String getBookFileName() {
        return AppUtil.getBookFileName();
    }

    public void goToChapter(String selectedChapter) {
        EventBus.getDefault().post(new GoToChapterEvent(selectedChapter));
    }

    public void goToPage(int position) {
        EventBus.getDefault().post(new SetWebViewToPositionEvent(position));
    }

    public int getCurrentChapterPage() {
        return AppUtil.getGetCurrentchapterPage();
    }

    public String getChapterName() {
        return AppUtil.getSpineItemHref();
    }

    public String getSpineItemHref() {
        return AppUtil.getSpineItemHref();
    }

    public String getRangy() {
        return AppUtil.getRangy();
    }

    public String getCurrentChapterName() {
        return AppUtil.getCurrentChapterName();
    }

    public void generateTOCLinkWrapper(String spineItemHref) {
        EventBus.getDefault().post(new GetTOCLinkWrapper(spineItemHref));
    }

    public TOCLinkWrapper getTOCLinkWrapper() {
        return AppUtil.getTocLinkWrapper();
    }

    public void goToBookmark(String chapter, int pageNumber) {
        EventBus.getDefault().post(new GoToBookMarkEvent(chapter, pageNumber));
    }

    public void loadPause(String chapter, int pageNumber) {
        EventBus.getDefault().post(new LoadPauseEvent(chapter, pageNumber));
    }

    public void setCurrentChapterPage(int page) {
        AppUtil.setCurrentchapterPage(page);
    }

    public boolean forceLoadPause() {
        return AppUtil.isComeFromInternalChange();
    }
}
