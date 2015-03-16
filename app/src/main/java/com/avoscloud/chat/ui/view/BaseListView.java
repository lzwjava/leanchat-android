package com.avoscloud.chat.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import com.avoscloud.chat.R;
import com.avoscloud.chat.adapter.BaseListAdapter;
import com.avoscloud.chat.ui.view.xlist.XListView;
import com.avoscloud.chat.util.SimpleNetTask;
import com.avoscloud.chat.util.Utils;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by lzw on 15/1/2.
 */
public class BaseListView<T> extends XListView implements XListView.IXListViewListener, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
  private static final int ONE_PAGE_SIZE = 15;
  private BaseListAdapter<T> adapter;
  private DataFactory<T> dataFactory = new DataFactory<T>();
  private boolean toastIfEmpty = true;
  private ItemListener<T> itemListener = new ItemListener<T>();

  public BaseListView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public void init(DataFactory<T> dataFactory, BaseListAdapter<T> adapter) {
    this.dataFactory = dataFactory;
    this.adapter = adapter;
    setAdapter(adapter);
    setXListViewListener(this);
    setOnItemClickListener(this);
    setOnItemLongClickListener(this);
    setPullLoadEnable(true);
    setPullRefreshEnable(true);
  }

  public void setItemListener(ItemListener<T> itemListener) {
    this.itemListener = itemListener;
  }

  @Override
  public void onRefresh() {
    loadDatas(false, true);
  }

  public void refreshWithoutAnim() {
    loadDatas(false, false);
  }

  public void loadDatas(final boolean loadMore, boolean animate) {
    final int skip;
    if (loadMore) {
      skip = adapter.getCount();
    } else {
      if (animate && !getPullRefreshing()) {
        pullRefreshing();
      }
      skip = 0;
    }
    new SimpleNetTask(getContext(), false) {
      List<T> datas;

      @Override
      protected void doInBack() throws Exception {
        if (dataFactory != null) {
          datas = dataFactory.getDatas(skip, ONE_PAGE_SIZE, adapter.getDatas());
        } else {
          datas = new ArrayList<T>();
        }
      }

      @Override
      protected void onSucceed() {
        if (loadMore == false) {
          stopRefresh();
          adapter.setDatas(datas);
          adapter.notifyDataSetChanged();
          if (datas.size() < ONE_PAGE_SIZE) {
            if (isToastIfEmpty()) {
              if (datas.size() == 0) {
                Utils.toast(getContext(), R.string.listEmptyHint);
              }
            }
            //setPullLoadEnable(false);
          } else {
            //setPullLoadEnable(true);
          }
        } else {
          stopLoadMore();
          adapter.addAll(datas);
          if (datas.size() == 0) {
            Utils.toast(getContext(), R.string.noMore);
          }
        }
      }
    }.execute();
  }

  @Override
  public void onLoadMore() {
    loadDatas(true, false);
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    T item = (T) parent.getAdapter().getItem(position);
    itemListener.onItemSelected(item);
  }


  @Override
  public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
    T item = (T) parent.getAdapter().getItem(position);
    itemListener.onItemLongPressed(item);
    return false;
  }


  public boolean isToastIfEmpty() {
    return toastIfEmpty;
  }

  public void setToastIfEmpty(boolean toastIfEmpty) {
    this.toastIfEmpty = toastIfEmpty;
  }


  public static class DataFactory<T> {
    public List<T> getDatas(int skip, int limit, List<T> currentDatas) throws Exception {
      return new ArrayList<T>();
    }
  }

  public static class ItemListener<T> {
    public void onItemSelected(T item) {
    }

    public void onItemLongPressed(T item) {
    }
  }
}
