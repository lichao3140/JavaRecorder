package com.lichao.javarecorder;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;

/**
 * 播放音频
 * @author LiChao
 *
 */
public class MediaManager {

	private static MediaPlayer mMediaPlayer;
	
	private static boolean isPause;//暂停状态
	
	public static void playSound(String filepath,OnCompletionListener onCompletionListener) {
		if(mMediaPlayer == null){
			mMediaPlayer = new MediaPlayer();
			mMediaPlayer.setOnErrorListener(new OnErrorListener() {
				
				@Override
				public boolean onError(MediaPlayer mp, int what, int extra) {
					mMediaPlayer.reset();
					return false;
				}
			});
		}else{
			mMediaPlayer.reset();
		}
		
		try {
			mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mMediaPlayer.setOnCompletionListener(onCompletionListener);
			mMediaPlayer.setDataSource(filepath);
			mMediaPlayer.prepare();
			mMediaPlayer.start();
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

	//暂停  不在播放界面  自动暂停
	public static void pause(){
		if(mMediaPlayer != null && mMediaPlayer.isPlaying()){
			mMediaPlayer.pause();
			isPause = true;
		}
	}
	
	public static void resume(){
		if(mMediaPlayer != null && isPause){
			mMediaPlayer.start();
			isPause = false;
		}
	}
	
	public static void release(){
		if(mMediaPlayer != null){
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
	}
}
