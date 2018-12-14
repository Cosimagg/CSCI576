//package org.wikijava.sound.playWave;

//import java.util.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.lang.*;
import java.io.*;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.DataLine.Info;

/**
 * 
 * <Replace this with a short description of the class.>
 * 
 * @author Giulio
 */
public class PlaySound implements Runnable{

    private InputStream waveStream;

    private final int EXTERNAL_BUFFER_SIZE = 4096; // 128Kb
    private final int TOTAL_SIZE = 300*44100*2; // 128Kb
    boolean suspended = false;
    boolean stop =false;
    String audioName="";
    String threadName="";
    public Thread t;
    boolean skip =false;
    int skipBytes =0;


    /**
     * CONSTRUCTOR
     */
    public PlaySound(String audioName,String threadName) {
		this.audioName = audioName;
		this.threadName = threadName;
    }

    public void play() throws PlayWaveException {
		try {
		    waveStream = new FileInputStream(audioName);
		    
		    System.out.println("Running ");
		} catch (FileNotFoundException e) {
	    	e.printStackTrace();
		}

		AudioInputStream audioInputStream = null;
		try {
			InputStream bufferedIn = new BufferedInputStream(this.waveStream); // new
		    audioInputStream = AudioSystem.getAudioInputStream(bufferedIn);
		} catch (UnsupportedAudioFileException e1) {
		    throw new PlayWaveException(e1);
		} catch (IOException e1) {
		    throw new PlayWaveException(e1);
		}


		// Obtain the information about the AudioInputStream
		AudioFormat audioFormat = audioInputStream.getFormat();
		Info info = new Info(SourceDataLine.class, audioFormat);

		// opens the audio channel
		SourceDataLine dataLine = null;
		try {
		    dataLine = (SourceDataLine) AudioSystem.getLine(info);
		    dataLine.open(audioFormat, EXTERNAL_BUFFER_SIZE);
		} catch (LineUnavailableException e1) {
		    throw new PlayWaveException(e1);
		}

		// Starts the music :P
		dataLine.start();
		if(skip){
			byte[] skipBuffer = new byte[skipBytes];
			try {
					int read = audioInputStream.read(skipBuffer, 0,skipBuffer.length);
					System.out.println("skip "+read+" bytes");
				} 
			catch (IOException e1) {
		    	throw new PlayWaveException(e1);
			}
			
			skip=false;
		}

		int readBytes = 0;
		byte[] audioBuffer = new byte[EXTERNAL_BUFFER_SIZE];
		byte[] muteBuffer = new byte[EXTERNAL_BUFFER_SIZE];

		for(int i = 0; i < muteBuffer.length; i++) {
			muteBuffer[i] = 0;
		}
		try {
		    while (readBytes != -1) {
		    	if(stop){
						dataLine.flush();
						break;
				}
				readBytes = audioInputStream.read(audioBuffer, 0,audioBuffer.length);
				if (readBytes >= 0){
			    	//dataLine.drain();
			    	synchronized(this) {
			    		if(!suspended){
			    			dataLine.write(audioBuffer, 0, EXTERNAL_BUFFER_SIZE);
			    		}
			    		else{
			    			dataLine.flush();
			    			dataLine.write(muteBuffer, 0, EXTERNAL_BUFFER_SIZE);
			    		}
			    		
			            while(suspended) {
			               try {
			                this.wait();
			                dataLine.write(audioBuffer, 0, EXTERNAL_BUFFER_SIZE);
			            		} 
			            	catch (InterruptedException e) {
			            		System.out.println("audio has been interrupted");
			            	}

			            }
			        }			    	
				}
				dataLine.start();
		    }
		} catch (IOException e1) {
		    throw new PlayWaveException(e1);
		} finally {
		    // plays what's left and and closes the audioChannel
		    dataLine.drain();
		    dataLine.close();
		}

    }

    public void run(){
      System.out.println("Running " );
      try {
      		play();
      } catch (PlayWaveException e) {
         System.out.println("Thread  interrupted.");
      }
      System.out.println("Thread  exiting.");
   }

   public void start () {
      System.out.println("Starting the sound ");
      if (t == null) {
         t = new Thread (this, threadName);
         t.start ();
      }
   }

   void suspend() {
      suspended = true;
   }

   void skip(double num) {
      skip=true;
      skipBytes = (int)(num*TOTAL_SIZE);
   }

   void stop() {
   	  stop =true;
   }

   void restart(){
   	 start();
   }
   
   synchronized void resume() {
      suspended = false;
      notify();
   }
}
