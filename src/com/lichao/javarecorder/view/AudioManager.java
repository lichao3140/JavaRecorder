package com.lichao.javarecorder.view;

import java.io.File;
import java.util.UUID;
import android.media.MediaRecorder;

/**
 * 录音管理类
 * 
 * @author LiChao
 * 
 */
public class AudioManager {

	private MediaRecorder mMediaRecorder;
	private String mDir;
	private String mCurrentFilePath;// 录音文件路径

	private static AudioManager mInstance;// 使用单例

	private boolean isPrepared = false;

	public AudioManager(String mDir) {
		this.mDir = mDir;
	}

	/**
	 * 回调准备完毕
	 */
	public interface AudioStateListener {
		void wellPrepared();
	}

	public AudioStateListener mListener;

	public void setOnAudioStateListener(AudioStateListener mListener) {
		this.mListener = mListener;
	}

	public static AudioManager getInstance(String mDir) {
		if (mInstance == null) {
			//同步
			synchronized (AudioManager.class) {
				mInstance = new AudioManager(mDir);
			}
		}
		return mInstance;
	}

	/**
	 * 准备
	 */
	public void prepareAudio() {
		try {
			isPrepared = false;
			File dir = new File(mDir);//存放录音文件的文件夹
			if (!dir.exists())
				dir.mkdirs();

			String fileName = generateName();//文件夹名
			File file = new File(dir, fileName);//file的路径dir，名字fileName

			mCurrentFilePath = file.getAbsolutePath();
			mMediaRecorder = new MediaRecorder();
			// 设置输出文件
			mMediaRecorder.setOutputFile(file.getAbsolutePath());
			// 设置音频源为麦克风
			mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			// 设置音频格式
			mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);
			// 设置音频编码
			mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

			mMediaRecorder.prepare();
			mMediaRecorder.start();
			//准备结束
			isPrepared = true;
			//通知回调
			if (mListener != null)
				mListener.wellPrepared();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 随机生成文件的名字
	 * @return
	 */
	private String generateName() {
		return UUID.randomUUID().toString() + ".amr";
	}

	/**
	 * 获取当前音量的等级
	 * @param maxLevel  7张图片
	 * @return
	 */
	public int getVoiceLevel(int maxLevel) {
		if (isPrepared) {
			try {
				// mMediaRecorder.getMaxAmplitude() 1-32767
				// 注意此处mMediaRecorder.getMaxAmplitude 只能取一次，如果前面取了一次，后边再取就为0了
				return ((mMediaRecorder.getMaxAmplitude() * maxLevel) / 32768) + 1;
			} catch (Exception e) {
			}
		}
		return 1;
	}

	/**
	 * 释放
	 */
	public void release() {
		if (mMediaRecorder != null) {
			mMediaRecorder.stop();
			mMediaRecorder.release();
			mMediaRecorder = null;
		}
	}

	/**
	 * 取消
	 */
	public void cancel() {
		release();
		if (mCurrentFilePath != null) {
			File file = new File(mCurrentFilePath);
			if (file.exists()) {
				file.delete();
				mCurrentFilePath = null;
			}
		}
	}

	public String getCurrentFilePath() {
		return mCurrentFilePath;
	}
}
