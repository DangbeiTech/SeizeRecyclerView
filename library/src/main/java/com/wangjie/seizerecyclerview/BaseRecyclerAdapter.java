package com.wangjie.seizerecyclerview;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Author: wangjie
 * Email: tiantian.china.2@gmail.com
 * Date: 3/23/17.
 */
public abstract class BaseRecyclerAdapter extends RecyclerView.Adapter<BaseViewHolder> {

    protected List<SeizeAdapter<BaseViewHolder>> seizeAdapters = new ArrayList<>();

    private static final int TYPE_DEFAULT = 0x7682;
    private static final int TYPE_HEADER_VIEW = 0x7683;
    private static final int TYPE_FOOTER_VIEW = 0x7684;

    private View headerView;
    private View footerView;

    public void setHeader(@NonNull View view) {
        headerView = view;
    }

    public void setFooter(@NonNull View view) {
        footerView = view;
    }

    @SafeVarargs
    public final void setSeizeAdapters(SeizeAdapter<BaseViewHolder>... seizeAdapters) {
        this.seizeAdapters = Arrays.asList(seizeAdapters);
        for (SeizeAdapter<BaseViewHolder> seizeAdapter : this.seizeAdapters) {
            seizeAdapter.setParentAdapter(this);
        }
    }

    @Override
    public final BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_HEADER_VIEW:
                return new EmptyViewHolder(headerView);
            case TYPE_FOOTER_VIEW:
                return new EmptyViewHolder(footerView);
            default:
                if (null != seizeAdapters) {
                    for (SeizeAdapter<BaseViewHolder> seizeAdapter : seizeAdapters) {
                        BaseViewHolder viewHolder;
                        if (seizeAdapter.hasViewType(viewType)
                                && null != (viewHolder = seizeAdapter.onCreateViewHolder(parent, viewType))
                                ) {
                            return viewHolder;
                        }
                    }
                }
        }
        return null;
    }

    @Override
    public final int getItemViewType(int position) {
        if (isHeader(position)) {
            return TYPE_HEADER_VIEW;
        } else if (isFooter((position))) {
            return TYPE_FOOTER_VIEW;
        } else {
            SeizePosition seizePosition = convertSeizePosition(position);
            if (null != seizePosition) {
                return seizeAdapters.get(seizePosition.getSeizeAdapterIndex())
                        .getItemViewType(seizePosition);
            }
        }

        return super.getItemViewType(position);
    }

    public boolean isHeader(int position) {
        int headerCount = getCount(headerView);
        return 0 != headerCount && position <= headerCount - 1;
    }

    public boolean isFooter(int position) {
        int footerCount = getCount(footerView);
        return 0 != footerCount && position >= getItemCount() - footerCount;
    }

    @Override
    public final void onBindViewHolder(BaseViewHolder holder, int position) {
        SeizePosition seizePosition = convertSeizePosition(position);
        if (null != seizePosition) {
            seizeAdapters.get(seizePosition.getSeizeAdapterIndex())
                    .onBindViewHolder(holder, seizePosition);
        }
    }

    @Nullable
    public final SeizeAdapter<BaseViewHolder> getSeizeAdapter(int position) {
        SeizePosition seizePosition = convertSeizePosition(position);
        if (null != seizePosition) {
            return seizeAdapters.get(seizePosition.getSeizeAdapterIndex());
        }
        return null;
    }

    @Nullable
    public final SeizePosition convertSeizePosition(int position) {
        if (null != seizeAdapters) {
            int seizeLastPosition = getCount(headerView);
            for (int i = 0, len = seizeAdapters.size(); i < len; i++) {
                SeizeAdapter seizeAdapter = seizeAdapters.get(i);
                int count = seizeAdapter.getItemCount();
                seizeLastPosition += count;
                if (seizeLastPosition > position) {
                    int subPosition = count - (seizeLastPosition - position);
                    return new SeizePosition(i,
                            position, positionToSourcePosition(position),
                            subPosition, seizeAdapter.subPositionToSubSourcePosition(subPosition)
                    );
                }
            }
        }
        return null;
    }

    @Nullable
    public final SeizePosition convertSeizePosition(int seizeAdapterIndex, int subPosition) {
        // TODO: 3/28/17 wangjie impl
        throw new RuntimeException("Not supported!");
    }

    @Override
    public final int getItemCount() {
        int itemCount = getCount(headerView) + getCount(footerView);
        List<SeizeAdapter<BaseViewHolder>> childSeizeAdapters = seizeAdapters;
        if (null != childSeizeAdapters) {
            for (SeizeAdapter seizeAdapter : childSeizeAdapters) {
                itemCount += seizeAdapter.getItemCount();
            }
        }
        return itemCount;
    }

    private int getCount(View view) {
        return null == view ? 0 : 1;
    }

    public final int positionToSourcePosition(int position) {
        return position - getCount(headerView);
    }

    public final int sourcePositionToPosition(int sourcePosition) {
        return sourcePosition + getCount(headerView);
    }
}
