
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*; 
import java.io.*;
import java.io.File;
import java.io.FilenameFilter;
import javax.swing.*;
import javax.swing.JFileChooser;
import java.lang.*;
import java.awt.Component;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.io.File;
import java.util.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.geom.*;


public class VideoPlayer extends Thread{

	JFrame frame;
	JLabel lbIm1;
	int width = 352;
	int height = 288;
	//static String imageName="";
	private final static int STATE_RUN = 0, STATE_PAUSE = 2, STATE_STOP = 3, STATE_START = 4;
	private int _state;
	String[] videoPath;
	HashMap<Integer,ArrayList<VLink>> shapes = new HashMap<Integer,ArrayList<VLink>>();
	//String audioName;
	int videoSize =0;
	PlaySound playSound;
	String[] path ;
	String audioName;

	int videoFrame=0;

	// public static void setPath(String name){
	// 	String[] path =name.split("/");
	// 	imageName=name+"/"+path[path.length-1];
	// 	//System.out.println(imageName);
	// }


	public void setPath(String parent,String[] name){
		//System.out.println(name.length);
		path =parent.split("/");
		audioName = parent+"/"+path[path.length-1] + ".wav";
		Arrays.sort(name);
		videoPath = new String[name.length];
		videoSize = name.length;
		for(int i=0;i<name.length;i++){
			videoPath[i]=parent+"/"+name[i];
		}
		playSound = new PlaySound(audioName,path[path.length-1]);		
	    try {
	    	File file = new File(parent+"/"+"link.txt"); 
	        Scanner sc = new Scanner(file);
	        while (sc.hasNextLine()) {
	            String line =sc.nextLine();
	            Scanner sw =new Scanner(line);
            	int fFrame = sw.nextInt();
            	int sFrame = sw.nextInt();//dont'need
            	String n = sw.next();
            	double x = sw.nextDouble()-30;
            	if(x<0)x=0;
            	double y = sw.nextDouble()-30;
            	if(y<0)y=0;
            	double w = sw.nextDouble()+60;
            	if(w>width)w=width;
            	double h = sw.nextDouble()+60;
            	if(h>height) h=height;
            	String secondPath = sw.next();
            	//String secondPath = sw.next()+" "+sw.next(); // the name is important
            	VLink l = new VLink(new Rectangle2D.Double(x, y, w, h),secondPath,sFrame);
            	if(!shapes.containsKey(fFrame)){
            		ArrayList<VLink> lv = new ArrayList<VLink>();
            		lv.add(l);
            		shapes.put(fFrame,lv);
            	}else{
            		ArrayList<VLink> lv = shapes.get(fFrame);
            		lv.add(l);
            		shapes.put(fFrame,lv);
            	}
            	System.out.println(line);
	        }
	        sc.close();
	    } 
	    catch (FileNotFoundException e) {
	    	System.out.println("this is pure video");
	        //e.printStackTrace();
	    }



	}

	public VideoPlayer(){
		this.frame=frame;
		_state=STATE_START;
		frame = new JFrame("VideoPlayer");
		frame.setSize(500, 450);
		JPanel panel = new JPanel();
		frame.add(panel);
		frame.getContentPane().setLayout(null);
		//button
		JButton selectButton=new JButton("Select");
		selectButton.setBounds(20,20,80,50);
		selectButton.addActionListener(new ActionListener(){  
			public void actionPerformed(ActionEvent e){  
            	JFileChooser fileChooser = new JFileChooser();
				fileChooser.setCurrentDirectory(new File("."));
				fileChooser.setDialogTitle("Select your file");
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int result = fileChooser.showOpenDialog(selectButton);
				if (result == JFileChooser.APPROVE_OPTION) {
					File selectedFile = fileChooser.getSelectedFile();
					System.out.println("Selected file: " + selectedFile.getAbsolutePath());
					FilenameFilter fileFilter = new FilenameFilter() {
			        	public boolean accept(File directory, String fileName) {
			            	return fileName.endsWith(".rgb");
			        	}
			        };	
					String[] listOfTextFiles = selectedFile.list(fileFilter);
					setPath(selectedFile.getAbsolutePath(),listOfTextFiles);
					showIms(0);
				}
        	}  
    	});
    	Icon pause = new ImageIcon("pause.png");
    	JButton pauseButton=new JButton();
    	pauseButton.setIcon(pause);
		pauseButton.setBounds(60+width,100,70,70);
		pauseButton.addActionListener(new ActionListener(){  
			public void actionPerformed(ActionEvent e){  
            	 pause();
            	 playSound.suspend();
        	}  
    	});
    	Icon play=new ImageIcon("play.png");
    	JButton playButton=new JButton();
    	playButton.setIcon(play);
		playButton.setBounds(60+width,180,70,70);
		playButton.addActionListener(new ActionListener(){  
			public void actionPerformed(ActionEvent e){
            	if(_state==STATE_START){
            		synchronized(this){
		        	 	_state = STATE_RUN;
		        	}
            		start();
            		//playSound.skip((double)(3000)/videoSize);
            		playSound.start();
            	}else if(_state==STATE_PAUSE){
            		unpause();
            		playSound.resume();
            	}else if(_state==STATE_STOP){
            		unpause();
            		System.out.println("restart");
            		playSound=new PlaySound(audioName,path[path.length-1]);
            		playSound.start();
            	}   
        	}  
    	});
    	Icon stop=new ImageIcon("stop.png");
    	JButton stopButton=new JButton();
    	stopButton.setIcon(stop);
		stopButton.setBounds(60+width,260,70,70);
		stopButton.addActionListener(new ActionListener(){  
			public void actionPerformed(ActionEvent e){ 
				playSound.stop();
				end();
        	}  
    	});
    	frame.getContentPane().add(selectButton);
    	frame.getContentPane().add(pauseButton);
    	frame.getContentPane().add(playButton);
    	frame.getContentPane().add(stopButton);
    	lbIm1 = new JLabel();
    	lbIm1.addMouseListener(new MouseAdapter() {
        	public void mouseClicked (MouseEvent e) 
        	{
          		if(e.getClickCount() == 1) 
          		{   
          			//playSound.pause();
          			System.out.println("the frame  is "+videoFrame);
          			//pause();    
          			if(shapes.containsKey(videoFrame)){
          				ArrayList<VLink> lv = shapes.get(videoFrame);
          				for(VLink l: lv){
          					if(l.getShape().contains(e.getX(), e.getY())){
          						playSound.stop();
          						setPath(l.getTargetPath(),l.getName());
          						playSound.skip((double)(l.getFrame())/videoSize);
          						System.out.println("this is "+l.getTargetPath());
          						System.out.println("the  name size is"+l.getName().length);
          						//System.out.println("You clicked two times on the button");
          						end();
          						videoFrame=l.getFrame()-1;
          						unpause();
          						playSound.start();
          					}
          				}

          			}
            		//System.out.println("You clicked two times on the button");
     
          		}
        	}
      	});
		lbIm1.setBounds(30,80,width,height);
		frame.getContentPane().add(lbIm1);
    	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true); 
	}

	@Override
	public void run(){
			videoFrame = 0;
	        while (true) {
	        	videoFrame++;
	        	if(videoFrame==videoPath.length){
	        		_state =STATE_STOP;
	        	}
	            if (_state == STATE_PAUSE) {
	                System.out.println(this + " paused");
	                synchronized (this) {
	                    try {
	                        this.wait();
	                    } catch (InterruptedException e) {
	                    }
	                }
	            }
	            if (_state == STATE_STOP) {
	            	videoFrame=0;
	            	showIms(videoFrame);
	            	synchronized (this) {
	                    try {
	                        this.wait();
	                    } catch (InterruptedException e) {
	                    }
	                }
	                System.out.println(this + " stoped");
	                videoFrame++;
	            }

	            showIms(videoFrame);
	          //   try {
           //   			Thread.sleep(1);
         		// 	} 
         		// catch (InterruptedException e) {
         		// 		e.printStackTrace();
         		// 	}
	        }
	}
	public void showIms(int num){
		String name="";
		if(videoPath!=null&&num>=0){
			name=videoPath[num];
		}
		BufferedImage image_original = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		image_original = createImage(image_original,name); 
		lbIm1.setIcon(new ImageIcon(image_original));
		// frame.getContentPane().remove(lbIm1);
		try {
        	Thread.sleep(26);
      	}
     	catch (InterruptedException e) {
     		e.printStackTrace();
     	}
		
	}

	public BufferedImage createImage(BufferedImage image_original,String imageName){
		try {
				File file = new File(imageName);
	    		InputStream stream = new FileInputStream(file);
	    		long len = file.length();
				byte[] bytes = new byte[(int)len];
				while (stream.read(bytes)>=0) {}
				int ind = 0;
				int allPixel = 0;
				if(imageName.endsWith("rgb")){
					allPixel = height*width;
				}
				for(int y = 0; y < height; y++){
					for(int x = 0; x < width; x++){
						byte r = bytes[ind];
						byte g = bytes[ind+allPixel];
						byte b = bytes[ind+2*allPixel];
						int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
			            image_original.setRGB(x,y,pix);
						ind++;
					}
				} 
				stream.close();		
    	} catch (IOException e) {
      			System.err.println(e.getMessage());
    	}
    	return image_original;
	}

	public synchronized void end() {
		//playSound.stop();
		System.out.println(this + " stoped");
        _state = STATE_STOP;
    }

    public synchronized void pause() {
        _state = STATE_PAUSE;
    }

    public synchronized void unpause() {
        _state = STATE_RUN;
        synchronized (this) {
            this.notify();
        }
    }

	public static void main(String[] args) {
		VideoPlayer ren = new VideoPlayer();
		
	}


}