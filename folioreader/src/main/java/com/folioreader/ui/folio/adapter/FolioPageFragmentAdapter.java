package com.folioreader.ui.folio.adapter;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

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

    public void setShowInterfacesControls(ShowInterfacesControls showInterfacesControls) {
        this.showInterfacesControls = showInterfacesControls;
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
        return mFolioPageFragment;
    }

    @Override
    public int getCount() {
        return mSpineReferences.size();
    }
}
