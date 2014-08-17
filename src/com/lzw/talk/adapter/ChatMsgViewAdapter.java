package com.lzw.talk.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.lzw.talk.R;
import com.lzw.talk.entity.ChatMsgEntity;

import java.util.ArrayList;
import java.util.List;

public class ChatMsgViewAdapter extends BaseAdapter {
	public static interface IMsgViewType {
		int IMVT_COM_MSG = 0;
		int IMVT_TO_MSG = 1;
	}

	private List<ChatMsgEntity> datas=new ArrayList<ChatMsgEntity>();

	private Context ctx;

	private LayoutInflater mInflater;

	public ChatMsgViewAdapter(Context context) {
		ctx = context;
		mInflater = LayoutInflater.from(context);
	}

  public void setDatas(List<ChatMsgEntity> datas) {
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
		ChatMsgEntity entity = datas.get(position);

		if (entity.getMsgType()) {
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

		ChatMsgEntity entity = datas.get(position);
		boolean isComMsg = entity.getMsgType();

		ViewHolder viewHolder = null;
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
      viewHolder.voiceImgView=convertView.findViewById(R.id.voiceImg);
			viewHolder.tvUserName = (TextView) convertView
					.findViewById(R.id.tv_username);
      viewHolder.tvContent=(TextView) convertView
					.findViewById(R.id.tv_chatcontent);
			viewHolder.isComMsg = isComMsg;

			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
    TextView textView=viewHolder.tvContent;

		viewHolder.tvSendTime.setText(entity.getDate());
		viewHolder.tvUserName.setText(entity.getName());
    textView.setTag(viewHolder);
    viewHolder.msg =entity;
    viewHolder.voiceImgView.setVisibility(View.GONE);
    textView.setText(entity.getText());
    setTextViewWidth(textView,ViewGroup.LayoutParams.WRAP_CONTENT);
		return convertView;
	}

  public void setTextViewWidth(TextView textView, int width) {
    ViewGroup.LayoutParams lp = textView.getLayoutParams();
    lp.width=width;
    textView.setLayoutParams(lp);
  }

  public static class ViewHolder {
		public TextView tvSendTime;
		public TextView tvUserName;
		public TextView tvContent;
		public boolean isComMsg = true;
    public View voiceImgView;
    public ChatMsgEntity msg;
  }
}
