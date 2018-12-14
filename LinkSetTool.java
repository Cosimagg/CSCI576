//package com.linktool;
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


public class LinkSetTool extends Thread{

	static String path;
	static String spath;
	static JFrame frame;
	static JLabel lbIm1;
	static JLabel lbIm2;
	static JSlider slider1;
	static JSlider slider2;
	static int width = 352;
	static int height = 288;
	static String[] videoOnePath;
	static String[] videoSecPath;
	static int videoOneSize =0;
	static int videoSecSize =0;
	static JComboBox<String> box;
	static JTextField textField;
	static PaintSurface paint;
	static BufferedImage original;
	static ArrayList<PaintSurface> paints;
	static HashMap<Integer,ArrayList<Map.Entry<String,Shape>>> map;
	static int firstFrame =0;
	static int secondFrame =0;
	//static ImageComparison imageComparator = new ImageComparison();

	public static void setOnePath(String parent,String[] name){
		//System.out.println(name.length);
		path=parent;
		Arrays.sort(name);
		videoOnePath = new String[name.length];
		videoOneSize = name.length;
		for(int i=0;i<name.length;i++){
			videoOnePath[i]=parent+"/"+name[i];
		}
		slider1.setMaximum(videoOneSize);

	}
	public static void setSecPath(String parent,String[] name){
		//System.out.println(parent);
		Arrays.sort(name);
		spath=parent;
		videoSecPath = new String[name.length];
		videoSecSize = name.length;
		for(int i=0;i<name.length;i++){
			videoSecPath[i]=parent+"/"+name[i];
		}
		slider2.setMaximum(videoSecSize); 
	}


	public LinkSetTool(JFrame frame){
		this.map = new HashMap<Integer,ArrayList<Map.Entry<String,Shape>>>();
		this.frame=frame;
		paints = new ArrayList<PaintSurface>();
		original = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		for(int y = 0; y < height; y++){
			for(int x = 0; x < width; x++){
				int pix = 0;
	            original.setRGB(x,y,pix);
			}
		}

	}

	public static void showIm1s(int num){
		String name="";
		if(videoOnePath!=null&&num>=0){
			name=videoOnePath[num];
		}
		//System.out.println(name);
		firstFrame =num;
		BufferedImage image_one = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		image_one = createImage(image_one,name);
		paint.refreshSlider(image_one,num,secondFrame,spath);	
	}

	public static void showIm2s(int num){
		String name="";
		if(videoSecPath!=null&&num>=0){
			name=videoSecPath[num];
		}
		secondFrame = num;
		BufferedImage image_sec = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		image_sec = createImage(image_sec,name); 
		lbIm2.setIcon(new ImageIcon(image_sec));
	}

	public static BufferedImage createImage(BufferedImage image_original,String imageName){
		try {
				File file = new File(imageName);
	    		InputStream stream = new FileInputStream(file);
	    		long len = file.length();
				byte[] bytes = new byte[(int)len];
				while (stream.read(bytes)>=0) {}
				int ind = 0;
				int allPixel =height*width;
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

	public static int[][][] createBlock(int ax,int ay,int w,int h,String imageName){
		//int bins =4;
		//float[] histogramData = new float[bins*bins*bins];
		int[][][] image_centor = new int[w][h][3];
		try {
				File file = new File(imageName);
	    		InputStream stream = new FileInputStream(file);
	    		long len = file.length();
				byte[] bytes = new byte[(int)len];
				while (stream.read(bytes)>=0) {}
				int orig = ax+ay*width;
				//System.out.println(orig+" "+ax+" "+ay+" "+bytes.length);
				int allPixel =height*width;
				int total=0;
				for(int j = 0; j < h; j++){
					int ind = orig +j*w;
					for(int i = 0; i < w; i++){
						image_centor[i][j][0] = bytes[ind]& 0xff;
						image_centor[i][j][1] = bytes[ind+allPixel]& 0xff;
						image_centor[i][j][2] = bytes[ind+2*allPixel]& 0xff;
						// int redIdx = (int) getBinIndex(bins, r, 255);
						// int greenIdx = (int) getBinIndex(bins, g, 255);
						// int blueIdx = (int) getBinIndex(bins, b, 255);

						// int singleIndex = redIdx + greenIdx * bins + blueIdx * bins * bins;
						// //System.out.println("ind is"+ind+" singleIndex "+singleIndex);
						// histogramData[singleIndex] += 1;
						// total++;
						//int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
			            //image_original.setRGB(x,y,pix);
						ind++;
					}
					//System.out.println(ind);
				} 
				stream.close();
				// for (int i = 0; i < histogramData.length; i++) {
				// 	histogramData[i] = histogramData[i] / total;
				// }			
    	} catch (IOException e) {
      			System.err.println(e.getMessage());
    	}
    	return image_centor;
	}



	public static TreeMap<Integer,Shape> track(Shape now){
		int centor = firstFrame;
		Shape c = now;
		TreeMap<Integer,Shape> linkshapes = new TreeMap<Integer,Shape>();
		linkshapes.put(firstFrame,now);
		int x = (int)now.getBounds().getX();
		int y = (int)now.getBounds().getY();
		int w = (int)now.getBounds().getWidth();
		int h = (int)now.getBounds().getHeight();

		//get main object
		x = x+w/4;
		y = y+h/4;
		w=w/2;
		h=h/2;
		//top-left X
		int rangeAX =x-w/2;
		if(rangeAX<0)rangeAX=0;
		int rangeAY =y-h/2;
		if(rangeAY<0)rangeAY=0;
		int rangeBX =x+3*w/2;
		if(rangeBX>width-1)rangeBX=width-1;
		rangeBX-=w;
		int rangeBY =y+3*h/2;
		if(rangeBY>height-1)rangeBY=height-1;
		rangeBY-=h;
		int[][][] image_cen = createBlock(x,y,w,h,videoOnePath[centor]);
		int base=0;
		for(int i =0 ;i<w;i++){
			for(int j=0; j<h;j++) {
				base+= image_cen[i][j][0];
				base+= image_cen[i][j][1];
				base+= image_cen[i][j][2];
			}
		}

		System.out.println("the image base is "+(base/w/h));

		boolean flag=false;
		int[][][] previous = image_cen;
		Shape sp=new Rectangle2D.Float(rangeAX, rangeAY, w, h);
		int minx=x;
		int miny=y;
		double dis =0;
		int times =5;
		for(int q=1; q<101;q+=1){
			int pre = centor-q;
			if(pre<0)break;
			double min=Integer.MAX_VALUE;
			flag=false;
			for(int i =rangeAX;i<rangeBX;i++){
				for(int j =rangeAY;j<rangeBY;j++){
					int[][][] image_cad = createBlock(i,j,w,h,videoOnePath[pre]);
					double result =calcSimilarity(previous,image_cad,h,w);
					if(result<min){
						min = result;
						sp=new Rectangle2D.Float(i-w/4, j-h/4, 2*w, 2*h);
						flag=true;
						//calculate the shift
						x=i;
						y=j;
						rangeAX =x-w/2;
						if(rangeAX<0)rangeAX=0;
						rangeAY =y-h/2;
						if(rangeAY<0)rangeAY=0;
						rangeBX =x+3*w/2;
						if(rangeBX>width-1)rangeBX=width-1;
						rangeBX-=w;
						rangeBY =y+3*h/2;
						if(rangeBY>height-1)rangeBY=height-1;
						rangeBY-=h;
						//previous =image_cad;
						//break;
					}
					//break;
					//}
				}
				
			}
			if(times!=0){
				dis+=Math.sqrt((minx-x)*(minx-x)+(miny-y)*(miny-y));
				dis=dis/2;
			}else{
				double disNow =Math.sqrt((minx-x)*(minx-x)+(miny-y)*(miny-y));
				if(disNow>1.5*dis){
					double percent =(double)dis/disNow;
					x=minx+(int)(percent*(x-minx));
					y=miny+(int)(percent*(y-miny));
					sp=new Rectangle2D.Float(x-w/4, y-h/4, 2*w, 2*h);
				}
			}
			System.out.println(pre+" "+x+" "+y+" "+min+" "+sp.getBounds().getX()+" "+(Math.sqrt(w*h)));
			if(min>(Math.sqrt(w*h)))break;//?
			minx =x;
			miny=y;
			//linkshapes.put(pre+1,sp1);
			
			//if(min==Integer.MAX_VALUE)break;
			linkshapes.put(pre,sp);		
		}

		x=(int)now.getBounds().getX();
		y=(int)now.getBounds().getY();
		x = x+w/4;
		y = y+h/4;
		rangeAX =x-w/2;
		if(rangeAX<0)rangeAX=0;
		rangeAY =y-h/2;
		if(rangeAY<0)rangeAY=0;
		rangeBX =x+3*w/2;
		if(rangeBX>width-1)rangeBX=width-1;
		rangeBX-=w;
		rangeBY =y+3*h/2;
		if(rangeBY>height-1)rangeBY=height-1;
		rangeBY-=h;
		flag =false;
		previous = image_cen;
		Shape sa=new Rectangle2D.Float(rangeAX, rangeAY, w, h);
		minx=x;
		miny=y;
		for(int k=1; k<101;k+=1){
			int after = centor+k;
			if(after>videoOneSize-1)break;
			double min=Integer.MAX_VALUE;
			flag =false;
			for(int i =rangeAX;i<rangeBX;i++){
				for(int j =rangeAY;j<rangeBY;j++){
					int[][][] image_cad = createBlock(i,j,w,h,videoOnePath[after]);
					double result =calcSimilarity(previous,image_cad,h,w);
					//if(result>0.9){
						//flag=true;
					if(result<min){
						flag=true;
						min = result;
						sa=new Rectangle2D.Float(i-w/4, j-h/4, 2*w, 2*h);
						x=i;
						y=j;
						rangeAX =x-w/2;
						if(rangeAX<0)rangeAX=0;
						rangeAY =y-h/2;
						if(rangeAY<0)rangeAY=0;
						rangeBX =x+3*w/2;
						if(rangeBX>width-1)rangeBX=width-1;
						rangeBX-=w;
						rangeBY =y+3*h/2;
						if(rangeBY>height-1)rangeBY=height-1;
						rangeBY-=h;
						//previous =image_cad;
						//break;
					}
						// Shape sa=new Rectangle2D.Float(i, j, w, h);
						// System.out.println(after+" "+i);
						// linkshapes.put(after,sa);
						// break;
					//}
				}
				
			}
			System.out.println(after+" "+minx+" "+miny+" "+min+" "+sa.getBounds().getX()+" "+Math.sqrt(w*h));
			if(min>(Math.sqrt(w*h)))break;//?
			// int avgx=(x+minx)/2;
			// int avgy=(y+miny)/2;
			// Shape sa1=new Rectangle2D.Float(avgx, avgy, w, h);
			// linkshapes.put(after-1,sa1);
			
			minx = x;
			miny = y;
			//if(min==Integer.MAX_VALUE)break;
			linkshapes.put(after,sa);	
		}
		return linkshapes;
		

	}

	//check similarity

	public static double calcSimilarity(int[][][] sourceData, int[][][] candidateData,int height,int width) {
		int diffr = 0;
		int diffg = 0;
		int diffb = 0;
		int base =0;
		for(int i =0 ;i<width;i++){
			for(int j=0; j<height;j++) {
				diffr+= Math.abs(sourceData[i][j][0] - candidateData[i][j][0]);
				diffg+= Math.abs(sourceData[i][j][1] - candidateData[i][j][1]);
				diffb+= Math.abs(sourceData[i][j][2] - candidateData[i][j][2]);
			}
		}
		
		//double similarity =(diffr+diffg+diffb)/(width*height*3);
		double similarity =(diffr*0.3+diffg*0.59+diffb*0.11)/(width*height);
		//System.out.println("this is image base"+base/width/height+" "+similarity);
		//System.out.println(similarity);
		// The degree of similarity
		return similarity;
	}



	public static void main(String[] args) {
		LinkSetTool ren = new LinkSetTool(frame);
		frame = new JFrame("VideoPlayer");
		frame.setSize(900, 500);
		JPanel panel = new JPanel();
		frame.add(panel);
		frame.getContentPane().setLayout(null);

		//slider
		slider1 = new JSlider(1, 9000, 1);
		slider1.setBounds(25,400,370,20);
		JLabel lb1 = new JLabel();
		lb1.setBounds(200,410,40,20);
		lb1.setText("0");
		frame.getContentPane().add(lb1);
		slider1.addChangeListener(new ChangeListener(){
            @Override
            public void stateChanged(ChangeEvent e) {
	        	JSlider source = (JSlider)e.getSource();
			    //if (!source.getValueIsAdjusting()) {
			        int index = (int)source.getValue();
			        ren.showIm1s(index-1);
			        lb1.setText(""+(index-1));
			    //}
            }
        });
        slider1.setPaintTicks(true);
		slider2 = new JSlider(1, 9000, 1);
		JLabel lb2 = new JLabel();
		lb2.setBounds(580,410,40,20);
		lb2.setText("0");
		frame.getContentPane().add(lb2);
		slider2.addChangeListener(new ChangeListener(){
            @Override
            public void stateChanged(ChangeEvent e) {
	        	JSlider source = (JSlider)e.getSource();
			    //if (!source.getValueIsAdjusting()) {
			        int index = (int)source.getValue();
			        ren.showIm2s(index-1);
			        lb2.setText(""+(index-1));
			    //}
            }
        });
		slider2.setBounds(60+width,400,370,20);
		slider2.setPaintTicks(true);

		//button
		
		JButton pVideoButton=new JButton("Source");
		pVideoButton.setBounds(20,20,80,50);
		pVideoButton.addActionListener(new ActionListener(){  
			public void actionPerformed(ActionEvent e){  
            	JFileChooser fileChooser = new JFileChooser();
				fileChooser.setCurrentDirectory(new File("."));
				fileChooser.setDialogTitle("Select your file");
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int result = fileChooser.showOpenDialog(pVideoButton);
				if (result == JFileChooser.APPROVE_OPTION) {
					File selectedFile = fileChooser.getSelectedFile();
					System.out.println("Selected file: " + selectedFile.getAbsolutePath());
					FileFilter fileFilter = new FileFilter(".rgb");
					String[] listOfTextFiles = selectedFile.list(fileFilter);
					setOnePath(selectedFile.getAbsolutePath(),listOfTextFiles);
					ren.showIm1s(0);
				}
        	}  
    	});
    	JButton sVideoButton=new JButton("Target");
		sVideoButton.setBounds(110,20,80,50);
		sVideoButton.addActionListener(new ActionListener(){  
			public void actionPerformed(ActionEvent e){  
            	JFileChooser fileChooser = new JFileChooser();
				fileChooser.setCurrentDirectory(new File("."));
				fileChooser.setDialogTitle("Select your file");
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int result = fileChooser.showOpenDialog(sVideoButton);
				if (result == JFileChooser.APPROVE_OPTION) {
					File selectedFile = fileChooser.getSelectedFile();
					System.out.println("Selected file: " + selectedFile.getAbsolutePath());
					FileFilter fileFilter = new FileFilter(".rgb");
					String[] listOfTextFiles = selectedFile.list(fileFilter);
					setSecPath(selectedFile.getAbsolutePath(),listOfTextFiles);
					ren.showIm2s(0);
				}  
        	}  
    	});

    	JButton saveButton=new JButton("SAVE");
		saveButton.setBounds(460,20,80,50);
		saveButton.addActionListener(new ActionListener(){  
			public void actionPerformed(ActionEvent e){ 
				System.out.println("the video has been saved");
				map.put(firstFrame,paint.getShapes());
				System.out.println(firstFrame+": "+secondFrame+" "+paint.getShapes().size());
				try{
					if(path==null)path="";
					PrintWriter writer = new PrintWriter(path+"/link.txt", "UTF-8");
					for(Map.Entry<String,Link> ele:paint.getLinks()){
						for(Map.Entry<Integer,Shape> sha : ele.getValue().getFrameMap().entrySet())
						writer.println(sha.getKey()+" "+ele.getValue().getTargetFrame()+" "+ele.getKey()+" "+sha.getValue().getBounds().getX()+" "+sha.getValue().getBounds().getY()+" "+sha.getValue().getBounds().getWidth()+" "+sha.getValue().getBounds().getHeight()+" "+spath);
					}

					//writer.println(firstFrame+" "+secondFrame+ele.getKey()+" "+ele.getValue().getBounds().getX()+" "+ele.getValue().getBounds().getY()+" "+ele.getValue().getBounds().getWidth()+" "+ele.getValue().getBounds().getHeight());
					
					writer.close();
				}catch (IOException ex) {
					System.out.println("writting fails");
				}
				
        	}  
    	});


    	JButton linkButton=new JButton("SetLink");
		linkButton.setBounds(200,20,80,50);
		linkButton.addActionListener(new ActionListener(){  
			public void actionPerformed(ActionEvent e){
				String text = textField.getText();
				if(!paint.getShapeMap().containsKey(text)){
		          box.addItem(text);
		        }
		        box.setSelectedItem(text);
		        Shape now =paint.getNow();
		        paint.putShapeMap(text,now);
		        paint.setNow(null);
		        TreeMap<Integer,Shape> frameMap = track(now);
            	paint.defineLink(text,secondFrame,spath,frameMap);
            	paint.setFocus(text);
            	linkButton.setEnabled(false);  
        	}  
    	});
    	linkButton.setEnabled(false);


		frame.getContentPane().add(saveButton);
    	frame.getContentPane().add(linkButton);
    	frame.getContentPane().add(pVideoButton);
    	frame.getContentPane().add(sVideoButton);

    	//textField
    	textField= new JTextField("hello world");
		textField.setBounds(300,20,150,30);
		textField.setEditable(true);
		textField.addActionListener(ae -> {
	        String text = textField.getText();
	        
	        textField.setText(text);
	        textField.setEnabled(false);
	        linkButton.setEnabled(true);
	        //paint.defineLink(text,firstFrame,secondFrame,spath,now);
	     });
    	//combox
		box = new JComboBox<String>();
		box.setBounds(300,50,150,27);
		box.setEditable(true);
		box.addActionListener(new ActionListener() {
	        public void actionPerformed(ActionEvent event) {
	            // Get the source of the component, which is our combo
	            // box.
	            JComboBox comboBox = (JComboBox) event.getSource();

	            // Print the selected items and the action command.
	            String selected =(String) comboBox.getSelectedItem();
	            textField.setText(selected);
	            //textField.setEnabled(true);
	            //change=true;
	            if(paint.getShapeMap().containsKey(selected)){
	              paint.setFocus(selected);
	            }
	            System.out.println("Selected Item  = " + selected);
	            String command = event.getActionCommand();
	            System.out.println("Action Command = " + command);
	            if ("comboBoxChanged".equals(command)) {
	            	Link l=paint.getLink(selected);
	            	if(l!=null){
	            		if(l.getTargetPath()!=null){
	            			File d=new File(l.getTargetPath());
	            			FileFilter fileFilter = new FileFilter(".rgb");
							String[] listOfTextFiles = d.list(fileFilter);
							setSecPath(d.getAbsolutePath(),listOfTextFiles);
	            		}
	            		paint.setFocus(selected);
	            		slider1.setValue(l.getSourceFrame()+1);
	             		slider2.setValue(l.getTargetFrame()+1); 
	            	}
	            }
	            // Detect whether the action command is "comboBoxEdited"
	            // or "comboBoxChanged"
	            if ("comboBoxEdited".equals(command)) {
	                System.out.println("User has typed a string in " +
	                        "the combo box.");
	                String focus =paint.getFocus();
	                if(focus!=selected){
	                  box.removeItem(paint.getFocus());
	                  box.addItem(selected);
	                  System.out.println("the foucs is "+paint.getFocus());
	                  paint.updateShapeMap(selected,paint.getShapeMap().get(focus),focus);
	                  paint.updateLinkMap(selected,paint.getLinkMap().get(focus),focus);
	                  paint.setFocus(selected);
	                  box.setSelectedItem(selected);
	                  //update linkMap
	                }
	                
	            }
	             //Link l=paint.getLink(selected);
	             //showIm1s(l.getSourceFrame());
	             //showIm2s(l.getTargetFrame()); 
          	}
	     });


		// picture
		lbIm2 = new JLabel(new ImageIcon(original));
		lbIm2.setBounds(70+width,100,width,height);
		paint = new PaintSurface(original,textField,box,lbIm2);
		paint.setBounds(30,100,width,height);

		frame.getContentPane().add(paint);
		frame.getContentPane().add(lbIm2);
		frame.getContentPane().add(box);
		frame.getContentPane().add(textField);
		frame.getContentPane().add(slider1);
		frame.getContentPane().add(slider2);


    	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
}

class FileFilter implements FilenameFilter {
    private String fileExtension;
    public FileFilter(String fileExtension) {
        this.fileExtension = fileExtension;
    }
    @Override
    public boolean accept(File directory, String fileName) {
        return (fileName.endsWith(this.fileExtension));
    }
}
