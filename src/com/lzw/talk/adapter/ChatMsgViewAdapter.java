package com.lzw.talk.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.lzw.talk.R;
import com.lzw.talk.entity.Msg;

import java.util.ArrayList;
import java.util.List;

public class ChatMsgViewAdapter extends BaseAdapter {
	public static interface IMsgViewType {
		int IMVT_COM_MSG = 0;
		int IMVT_TO_MSG = 1;
	}

	private List<Msg> datas=new ArrayList<Msg>();

	private Context ctx;

	private LayoutInflater mInflater;

	public ChatMsgViewAdapter(Context context) {
		ctx = context;
		mInflater = LayoutInflater.from(context);
	}

  public void setDatas(List<Msg> datas) {
    this.datas = datas;
  }

  public int getCount() {
		return datas.size();
	}

	public Object getItem(int position) {
		return datas.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public int getItemViewType(int position) {
		// TODO Auto-generated method stub
		Msg entity = datas.get(position);

		if (entity.isComeMessage()) {
			return IMsgViewType.IMVT_COM_MSG;
		} else {
			return IMsgViewType.IMVT_TO_MSG;
		}

	}

	public int getViewTypeCount() {
		// TODO Auto-generated method stub
		return 2;
	}

	public View getView(int position, View convertView, ViewGroup parent) {

		Msg entity = datas.get(position);
		boolean isComMsg = entity.isComeMessage();

		ViewHolder viewHolder;
    if (convertView == null) {
			if (isComMsg) {
				convertView = mInflater.inflate(R.layout.chat_item_msg_text_left_line,
						null);
			} else {
				convertView = mInflater.inflate(R.layout.chat_item_msg_text_right_line,
						null);
			}

			viewHolder = new ViewHolder();
			viewHolder.tvSendTime = (TextView) convertView
					.findViewById(R.id.tv_sendtime);
			viewHolder.tvUserName = (TextView) convertView
					.findViewById(R.id.tv_username);
      viewHolder.tvContent=(TextView) convertView
					.findViewById(R.id.tv_chatcontent);
			viewHolder.isComMsg = isComMsg;
      if(isComMsg==false){
        viewHolder.tvStatus= (TextView) convertView.findViewById(R.id.status);
      }
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
    TextView textView=viewHolder.tvContent;

		viewHolder.tvSendTime.setText(entity.getTimestamp()+"");
		viewHolder.tvUserName.setText(entity.getFromName());
    textView.setTag(viewHolder);
    viewHolder.msg =entity;
    textView.setText(entity.getContent());
    if(isComMsg==false){
      viewHolder.tvStatus.setText(entity.getStatusDesc());
    }
		return convertView;
	}

  public static class ViewHolder {
		public TextView tvSendTime;
		public TextView tvUserName;
		public TextView tvContent;
		public boolean isComMsg = true;
    public Msg msg;
    public TextView tvStatus;
  }
}
