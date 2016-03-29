package remix.myplayer.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.Iterator;

import remix.myplayer.R;
import remix.myplayer.activities.ChildHolderActivity;
import remix.myplayer.adapters.FolderAdapter;
import remix.myplayer.utils.Constants;
import remix.myplayer.utils.DBUtil;

/**
 * Created by Remix on 2015/12/5.
 */

/**
 * 文件夹Fragment
 */
public class FolderFragment extends Fragment {
    private ListView mListView;
    private static boolean mIsRunning = false;
    public static FolderFragment mInstance;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_folder,null);
        mListView = (ListView)rootView.findViewById(R.id.folder_list);
        mListView.setOnItemClickListener(new ListViewListener());
        mListView.setAdapter(new FolderAdapter(getActivity(), inflater));
        return rootView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mInstance = this;
    }

    public void UpdateList() {
        if(mListView.getAdapter() != null){
            ((FolderAdapter)(mListView.getAdapter())).notifyDataSetChanged();
        }
    }

    @Override
    public void onResume() {
        mIsRunning = true;
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
        mIsRunning = false;
    }

    private class ListViewListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent = new Intent(getActivity(), ChildHolderActivity.class);
            intent.putExtra("Id", position);
            intent.putExtra("Type", Constants.FOLDER_HOLDER);
            if(DBUtil.mFolderMap == null || DBUtil.mFolderMap.size() < 0)
                return;
            Iterator it = DBUtil.mFolderMap.keySet().iterator();
            String full_path = null;
            for(int i = 0 ; i <= position ; i++)
                full_path = it.next().toString();
            intent.putExtra("Title", full_path);
            startActivity(intent);

        }
    }
}
