package remix.myplayer.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.BindView;
import butterknife.ButterKnife;
import remix.myplayer.R;
import remix.myplayer.adapter.FolderAdapter;
import remix.myplayer.interfaces.OnItemClickListener;
import remix.myplayer.ui.ListItemDecoration;
import remix.myplayer.ui.MultiChoice;
import remix.myplayer.ui.activity.ChildHolderActivity;
import remix.myplayer.ui.activity.MultiChoiceActivity;
import remix.myplayer.util.Constants;
import remix.myplayer.util.DensityUtil;
import remix.myplayer.util.Global;
import remix.myplayer.util.Util;

/**
 * Created by Remix on 2015/12/5.
 */

/**
 * 文件夹Fragment
 */
public class FolderFragment extends BaseFragment {
    public static FolderFragment mInstance;
    @BindView(R.id.recyclerview)
    RecyclerView mRecyclerView;

    private FolderAdapter mAdapter;
    public static final String TAG = FolderFragment.class.getSimpleName();
    private MultiChoice mMultiChoice;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_folder,null);
        mUnBinder = ButterKnife.bind(this,rootView);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.addItemDecoration(new ListItemDecoration(getContext(),ListItemDecoration.VERTICAL_LIST, DensityUtil.dip2px(getActivity(),8)));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        if(getActivity() instanceof MultiChoiceActivity){
            mMultiChoice = ((MultiChoiceActivity) getActivity()).getMultiChoice();
        }
        mAdapter = new FolderAdapter(mContext,R.layout.item_folder_recycle,mMultiChoice);
        mAdapter.setOnItemClickLitener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                String path = Util.getMapkeyByPosition(Global.FolderMap,position);
                if(getUserVisibleHint() && !TextUtils.isEmpty(path) &&
                        !mMultiChoice.itemAddorRemoveWithClick(view,position,position,TAG)){
                    Intent intent = new Intent(getActivity(), ChildHolderActivity.class);
                    intent.putExtra("Id", position);
                    intent.putExtra("Type", Constants.FOLDER);
                    intent.putExtra("Title",path);
                    startActivity(intent);
                }

            }

            @Override
            public void onItemLongClick(View view, int position) {
                String path = Util.getMapkeyByPosition(Global.FolderMap,position);
                if(getUserVisibleHint() && !TextUtils.isEmpty(path))
                    mMultiChoice.itemAddorRemoveWithLongClick(view,position,position,TAG,Constants.FOLDER);
            }
        });
        mRecyclerView.setAdapter(mAdapter);

        return rootView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPageName = TAG;
    }


    public void UpdateAdapter() {
        if(mRecyclerView != null && mRecyclerView.getAdapter() != null){
            mRecyclerView.getAdapter().notifyDataSetChanged();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }



    @Override
    public RecyclerView.Adapter getAdapter() {
        return mAdapter;
    }
}
