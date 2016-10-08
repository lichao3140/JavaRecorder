package com.lichao.javarecorder.view;

import com.lichao.javarecorder.R;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class DialogManager {

	private Dialog mDialog;
	private ImageView mIcon;
	private ImageView mVoice;
	private TextView mLabel;

	private Context mContext;

	public DialogManager(Context context) {
		this.mContext = context;
	}

	/**
	 * 显示录音时候的对话框
	 */
	public void showRecordingDialog() {
		mDialog = new Dialog(mContext, R.style.Theme_AudioDialog);
		LayoutInflater inflater = (LayoutInflater) mContext.
				getSystemService(mContext.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.dialog_recorder, null);
		mDialog.setContentView(view);
		
		mIcon = (ImageView) mDialog.findViewById(R.id.id_recorder_dialog_icon);
		mVoice = (ImageView) mDialog.findViewById(R.id.id_recorder_dialog_voice);
		mLabel = (TextView) mDialog.findViewById(R.id.id_recorder_dialog_label);
		
		mDialog.show();
	}

	/**
	 * 正在录音时候对话框的显示
	 */
	public void recording() {
		if (mDialog != null && mDialog.isShowing()) {
			mIcon.setVisibility(View.VISIBLE);
			mVoice.setVisibility(View.VISIBLE);
			mLabel.setVisibility(View.VISIBLE);

			mIcon.setImageResource(R.drawable.recorder);
			mLabel.setText("松开手指，取消发送");
		}
	}

	/**
	 * 显示准备取消时候的对话框
	 */
	public void wantToCancel() {
		if (mDialog != null && mDialog.isShowing()) {
			mIcon.setVisibility(View.VISIBLE);
			mVoice.setVisibility(View.GONE);
			mLabel.setVisibility(View.VISIBLE);

			mIcon.setImageResource(R.drawable.cancel);
			mLabel.setText("松开手指，取消发送");
		}
	}

	/**
	 * 显示录音时间太短的对话框
	 */
	public void tooShort() {
		if (mDialog != null && mDialog.isShowing()) {
			mIcon.setVisibility(View.VISIBLE);
			mVoice.setVisibility(View.GONE);
			mLabel.setVisibility(View.VISIBLE);

			mIcon.setImageResource(R.drawable.voice_to_short);
			mLabel.setText("录音时间过短");
		}
	}

	/**
	 * 隐藏对话框
	 */
	public void dismissDialog() {
		if (mDialog != null && mDialog.isShowing()) {
			mDialog.dismiss();
			mDialog = null;
		}
	}

	/**
	 * 通过level更新voice上的图片
	 * @param level 1-7
	 */
	public void updateVoiceLevel(int level) {
		if (mDialog != null && mDialog.isShowing()) {
//			mIcon.setVisibility(View.VISIBLE);
//			mVoice.setVisibility(View.VISIBLE);
//			mLabel.setVisibility(View.VISIBLE);

			//通过level找到资源id  v是图片的名字  level是1到7
			System.out.println("---level--"+level);
			int resId = mContext.getResources().getIdentifier("v"+level, "drawable", mContext.getPackageName());
			System.out.println("---resId+"+resId);
			mVoice.setImageResource(resId);
		}
	}

}
