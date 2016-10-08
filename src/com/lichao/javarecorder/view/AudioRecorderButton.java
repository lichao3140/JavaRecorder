package com.lichao.javarecorder.view;

import com.lichao.javarecorder.R;
import com.lichao.javarecorder.view.AudioManager.AudioStateListener;
import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

/**
 * 自定义Button
 * @author LiChao
 *
 */
public class AudioRecorderButton extends Button implements AudioStateListener {

	private static final int DISTANCE_Y_CACEL = 50;//取消录音状态距离
	private static final int STATE_NORMAL = 1;//正常状态
	private static final int STATE_RECORDING = 2;//录音状态
	private static final int STATE_WANT_TO_CANCEL = 3;//取消发送状态
	
	private int mCurState = STATE_NORMAL;//记住当前状态
	private boolean isRecording=false;//已经开始录音
	
	private DialogManager mDialogManager;
	private AudioManager mAudioManager;
	
	private float mTime;
	//是否触发longclick
	private boolean mReady;
	
	public AudioRecorderButton(Context context) {
		//让一个参数的构造方法调用一个参数的构造方法,默认调用两个参数的构造方法
		this(context,null);
	}
	
	public AudioRecorderButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		mDialogManager = new DialogManager(getContext());
		//获取手机本地存储
		String dir = Environment.getExternalStorageDirectory()+"/lichao";
		mAudioManager = AudioManager.getInstance(dir);
		mAudioManager.setOnAudioStateListener(this);
		
		setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				mReady = true;
				mAudioManager.prepareAudio();
				Log.e("lichao", "录音长按");
				return false;
			}
		});
	}
	
	/**
	 * 录音完成后回调
	 * @author LiChao
	 *
	 */
	public interface AudioFinishRecorderListener{
		void onFinish(float seconds, String filepath);
	}
	
	private AudioFinishRecorderListener mListener;
	
	public void setAudioFinishRecorderListener(AudioFinishRecorderListener listener){
		this.mListener = listener;
	}
	
	/**
	 * 获取音量大小
	 */
	private Runnable mGetVoiceLevelRunnable = new Runnable(){
		@Override
		public void run(){
			while(isRecording){
				try {
					Thread.sleep(100);
					mTime+=0.1f;//计时，每次0.1秒
					mHandler.sendEmptyMessage(MSG_VOICE_CHANGE);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	};
	
	private static final int MSG_AUDIO_PREPARED = 0X110;
	private static final int MSG_VOICE_CHANGE = 0X111;
	private static final int MSG_DIALOG_DISMISS = 0X112;
	
	private Handler mHandler = new Handler()
	{
		public void handleMessage(android.os.Message msg) 
		{
			switch (msg.what) {
			case MSG_AUDIO_PREPARED:
				//显示在audio回调以后
				mDialogManager.showRecordingDialog();
				isRecording = true;
				//开启单独线程获取音量大小			
				new Thread(mGetVoiceLevelRunnable).start();
				Log.e("lichao", "MSG_AUDIO_PREPARED");
				break;
			case MSG_VOICE_CHANGE:
				mDialogManager.updateVoiceLevel(mAudioManager.getVoiceLevel(7));
				Log.e("lichao", "MSG_VOICE_CHANGE");
				break;
			case MSG_DIALOG_DISMISS:
				mDialogManager.dismissDialog();
				Log.e("lichao", "MSG_DIALOG_DISMISS");
				break;
			}
		};
	};
	
	/**
	 * 完全准备好
	 */
	@Override
	public void wellPrepared() {
		mHandler.sendEmptyMessage(MSG_AUDIO_PREPARED);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int action = event.getAction();//获取当前状态
		int x=(int)event.getX();//获取当前坐标
		int y=(int)event.getY();
		
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			changeState(STATE_RECORDING);
			break;
		case MotionEvent.ACTION_MOVE:
			if(isRecording){//已经开始录音时，进行用户手指判断状态
				if(wantCancel(x,y)){
					changeState(STATE_WANT_TO_CANCEL);
				}else{
					changeState(STATE_RECORDING);
				}	
			}
			break;
		case MotionEvent.ACTION_UP:
			if(!mReady){//没有进行长按，ACTION_UP事件不处理
				reset();
				return super.onTouchEvent(event);
			}
			if(!isRecording || mTime < 0.6f){
				System.out.println("录制时间过短"+ isRecording + mTime);
				mDialogManager.tooShort();
				mAudioManager.cancel();
				//对话框显示1.3秒
				mHandler.sendEmptyMessageAtTime(MSG_DIALOG_DISMISS, 1300);
			}else if(mCurState == STATE_RECORDING){//正常结束录制
				System.out.println("正常录制结束");
				mDialogManager.dismissDialog();
				mAudioManager.release();
				if(mListener != null){
					mListener.onFinish(mTime, mAudioManager.getCurrentFilePath());
				}
			}else if(mCurState == STATE_WANT_TO_CANCEL){
				System.out.println("取消了");
				mDialogManager.dismissDialog();
				mAudioManager.cancel();
			}
			reset();
			break;
		default:
			break;
		}
		return super.onTouchEvent(event);
	}
	
	/**
	 * 恢复状态及标志位
	 */
	private void reset() {
		isRecording = false;
		mReady = false;
		mTime = 0;
		changeState(STATE_NORMAL);
	}

	/**
	 * 根据x y的坐标判断想要取消
	 * @param x
	 * @param y
	 * @return
	 */
	private boolean wantCancel(int x, int y) {
		if(x<0 || x>getWidth()){//判断手指坐标是否超出按钮范围
			return true;
		}
		if(y< -DISTANCE_Y_CACEL || y>getHeight()+DISTANCE_Y_CACEL){
			return true;
		}
		return false;
	}

	/**
	 * 改变录音状态,控制button显示状态
	 * @param stateRecording
	 */
	private void changeState(int state) {
		if(mCurState!=state){
			mCurState = state;
			switch (state) {
			case STATE_NORMAL:
				setBackgroundResource(R.drawable.btn_recorder_normal);
				setText(R.string.str_recorder_normal);
				break;
			case STATE_RECORDING:
				setBackgroundResource(R.drawable.btn_recorder_recording);
				setText(R.string.str_recorder_recording);
				if(isRecording){
					mDialogManager.recording();
				}
				break;
			case STATE_WANT_TO_CANCEL:
				setBackgroundResource(R.drawable.btn_recorder_recording);
				setText(R.string.str_recorder_want_cancel);
				mDialogManager.wantToCancel();
				break;

			default:
				break;
			}
		}
		
	}
	
}
