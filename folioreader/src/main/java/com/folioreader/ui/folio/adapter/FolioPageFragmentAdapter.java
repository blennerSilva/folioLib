package com.folioreader.ui.folio.adapter;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

import com.folioreader.PageHasChangedListener;
import com.folioreader.PageHasFinishedLoading;
import com.folioreader.ShowInterfacesControls;
import com.folioreader.ui.folio.fragment.EpubReaderFragment;
import com.folioreader.ui.folio.fragment.FolioPageFragment;

import org.readium.r2_streamer.model.publication.link.Link;

import java.util.List;

/**
 * @author mahavir on 4/2/16.
 */
public class FolioPageFragmentAdapter extends FragmentStatePagerAdapter {
    private List<Link> mSpineReferences;
    private String mEpubFileName;
    private String mBookId;
    private EpubReaderFragment epubReaderFragment;
    private ShowInterfacesControls showInterfacesControls;
    private PageHasChangedListener pageHasChangedListener;
    private PageHasFinishedLoading pageHasFinishedLoading;

    public void setShowInterfacesControls(ShowInterfacesControls showInterfacesControls) {
        this.showInterfacesControls = showInterfacesControls;
    }

    public void setPageHasChangedListener(PageHasChangedListener pageHasChangedListener) {
        this.pageHasChangedListener = pageHasChangedListener;
    }

    public void setPageHasFinishedLoading(PageHasFinishedLoading pageHasFinishedLoading) {
        this.pageHasFinishedLoading = pageHasFinishedLoading;
    }

    public FolioPageFragmentAdapter(FragmentManager fm, List<Link> spineReferences, String epubFileName, String bookId, EpubReaderFragment epubReaderFragment) {
        super(fm);
        this.mSpineReferences = spineReferences;
        this.mEpubFileName = epubFileName;
        this.mBookId = bookId;
        this.epubReaderFragment = epubReaderFragment;
    }

    @Override
    public Fragment getItem(int position) {
        FolioPageFragment mFolioPageFragment = FolioPageFragment.newInstance(position, mEpubFileName, mSpineReferences.get(position), mBookId, epubReaderFragment);
        mFolioPageFragment.setFragmentPos(position);
        mFolioPageFragment.setShowInterfacesControls(new ShowInterfacesControls() {
            @Override
            public void showInterfaceControls() {
                showInterfacesControls.showInterfaceControls();
                Log.d("TESTE2", "FolioPageFragment");
            }
        });

        mFolioPageFragment.setPageHasChangedListener(new PageHasChangedListener() {
            @Override
            public void pageHasChanged() {
                pageHasChangedListener.pageHasChanged();
                Log.d("pageHasChanged", "FolioPageFragmentAdapter");
            }
        });

        mFolioPageFragment.setPageHasFinishedLoading(new PageHasFinishedLoading() {
            @Override
            public void pageHasFinishedLoading() {
                pageHasFinishedLoading.pageHasFinishedLoading();
                Log.d("pageHasFinishedLoading", "FolioPageFragmentAdapter");
            }
        });
        return mFolioPageFragment;
    }

    @Override
    public int getCount() {
        return mSpineReferences.size();
    }
}
