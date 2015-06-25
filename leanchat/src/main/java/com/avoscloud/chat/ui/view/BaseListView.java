package com.avoscloud.chat.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import com.avoscloud.chat.R;
import com.avoscloud.chat.util.Logger;
import com.avoscloud.chat.util.SimpleNetTask;
import com.avoscloud.chat.util.Utils;
import com.avoscloud.chat.ui.view.xlist.XListView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * Created by lzw on 15/1/2.
 */
public class BaseListView<T> extends XListView implements XListView.IXListViewListener, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
  private static final int ONE_PAGE_SIZE = 15;
  private BaseListAdapter<T> adapter;
  private DataFactory<T> dataFactory = new DataFactory<T>();
  private boolean toastIfEmpty = true;
  private ItemListener<T> itemListener = new ItemListener<T>();
  private AtomicBoolean isLoading = new AtomicBoolean(false);

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
    loadWhenInit();
  }

  public void loadWhenInit() {
    if (isLoading.get()) {
      Logger.d("directly return in refresh");
      return;
    }
    isLoading.set(true);
    final int skip = 0;
    new GetDataTask(getContext(), skip) {
      @Override
      void onDataReady(List<T> datas) {
        stopRefresh();
        adapter.setDatas(datas);
        adapter.notifyDataSetChanged();
        if (datas.size() < ONE_PAGE_SIZE) {
          if (isToastIfEmpty()) {
            if (datas.size() == 0) {
              Utils.toast(getContext(), R.string.chat_base_list_view_listEmptyHint);
            }
          }
          //setPullLoadEnable(false);
        } else {
          //setPullLoadEnable(true);
        }
      }

      @Override
      protected void onPost(Exception e) {
        super.onPost(e);
        isLoading.set(false);
      }
    }.execute();
  }

  abstract class GetDataTask extends SimpleNetTask {
    int skip;
    List<T> datas;

    public GetDataTask(Context cxt, int skip) {
      super(cxt, false);
      this.skip = skip;
    }

    @Override
    protected void doInBack() throws Exception {
      if (dataFactory != null) {
        datas = dataFactory.getDatasInBackground(skip, ONE_PAGE_SIZE, adapter.getDatas());
      } else {
        datas = new ArrayList<T>();
      }
    }

    @Override
    protected void onSucceed() {
      onDataReady(datas);
    }

    abstract void onDataReady(List<T> datas);
  }

  private void loadMoreData() {
    final int skip = adapter.getCount();
    new GetDataTask(getContext(), skip) {
      @Override
      void onDataReady(List<T> datas) {
        stopLoadMore();
        adapter.addAll(datas);
        if (datas.size() == 0) {
          Utils.toast(getContext(), R.string.chat_base_list_view_noMore);
        }
      }

      @Override
      protected void onPost(Exception e) {
        super.onPost(e);
        isLoading.set(false);
      }
    }.execute();
  }

  @Override
  public void onLoadMore() {
    if (isLoading.get()) {
      Logger.d("directly return in loadMore");
      return;
    }
    isLoading.set(true);
    loadMoreData();
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
    public List<T> getDatasInBackground(int skip, int limit, List<T> currentDatas) throws Exception {
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
