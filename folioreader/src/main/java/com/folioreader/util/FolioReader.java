package com.folioreader.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;

import com.folioreader.ShowInterfacesControls;
import com.folioreader.model.HighLight;
import com.folioreader.model.HighlightImpl;
import com.folioreader.model.event.ChangeFontEvent;
import com.folioreader.model.event.ChangeThemeEvent;
import com.folioreader.model.sqlite.DbAdapter;
import com.folioreader.ui.base.OnSaveHighlight;
import com.folioreader.ui.base.SaveReceivedHighlightTask;
import com.folioreader.ui.folio.fragment.EpubReaderFragment;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

/**
 * Created by avez raj on 9/13/2017.
 */

public class FolioReader {
    public static final String INTENT_BOOK_ID = "book_id";
    private Context context;

    private OnHighlightListener onHighlightListener;
    private ShowInterfacesControls showInterfacesControls;


    public void showInterfaceControls(ShowInterfacesControls showInterfacesControls) {
        this.showInterfacesControls = showInterfacesControls;
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

    public void setCurrentPage(int pageIndex) {
        AppUtil.setCurrentPage(pageIndex);
    }

    public int getCurrentPage() {
        return AppUtil.getCurrentPage();
    }

    public String getSpineItem() {
        return AppUtil.getChapterSelected();
    }

    public String getBookId() {
        return AppUtil.getBookId();
    }

    public String getBookFileName() {
        return AppUtil.getBookFileName();
    }
}
