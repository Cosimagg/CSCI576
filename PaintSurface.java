//package com.linktool;
import java.awt.geom.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import java.lang.*;
import java.util.*;


public class PaintSurface extends JComponent {
    //ArrayList<Shape> shapes = new ArrayList<Shape>();
    HashMap<String,Shape> shapeMap = new HashMap<String,Shape>();// only for repaint 
    HashMap<String,Link> linkMap = new HashMap<String,Link>();
    Shape now = null;
    Point startDrag, endDrag;
    BufferedImage image;
    int width = 352;
    int height = 288;
    //int focus =-1;
    String focus =null;
    JTextField textField;
    JComboBox<String> box;
    int fNumber = 0;
    int sNumber = 0;
    String spath ="";
    JLabel lbIm2;
    //ArrayList<String> items = new ArrayList<String>();

    public PaintSurface(BufferedImage image,JTextField textField,JComboBox<String> box,JLabel lbIm2) {
      this.box=box;
      this.image = image; 
      this.textField = textField;
      this.lbIm2 = lbIm2;
      // textField.addActionListener(ae -> {
      //   String text = textField.getText();
      //   // if(focus!=text){
      //   //   System.out.println(" when");
      //   //   box.removeItem(focus);
      //   //   shapeMap.put(text,shapeMap.get(focus));
      //   //   shapeMap.remove(focus);
      //   //   focus=text;
      //   //   box.setSelectedItem(text);
      //   //   textField.setText("");
      //   //   textField.setEnabled(false);
      //   // }

      //   if(!shapeMap.containsKey(text)){
      //     box.addItem(text);
      //   }
      //   box.setSelectedItem(text);
      //   shapeMap.put(text,now);
      //   focus=text;
      //   now=null;
      //   textField.setText(text);
      //   textField.setEnabled(false);
      //   defineLink(fNumber,sNumber,spath);
      //   repaint();
      // });
      textField.setEnabled(false);
      // box.addActionListener(new ActionListener() {
      //   public void actionPerformed(ActionEvent event) {
      //       // Get the source of the component, which is our combo
      //       // box.
      //       JComboBox comboBox = (JComboBox) event.getSource();

      //       // Print the selected items and the action command.
      //       String selected =(String) comboBox.getSelectedItem();
      //       textField.setText(selected);
      //       //textField.setEnabled(true);
      //       //change=true;
      //       if(shapeMap.containsKey(selected)){
      //         focus=selected;
      //       }
      //       System.out.println("Selected Item  = " + selected);
      //       String command = event.getActionCommand();
      //       System.out.println("Action Command = " + command);

      //       // Detect whether the action command is "comboBoxEdited"
      //       // or "comboBoxChanged"
      //       if ("comboBoxEdited".equals(command)) {
      //           System.out.println("User has typed a string in " +
      //                   "the combo box.");
      //           if(focus!=selected){
      //             box.removeItem(focus);
      //             box.addItem(selected);
      //             System.out.println("the foucs is "+focus);
      //             shapeMap.put(selected,shapeMap.get(focus));
      //             shapeMap.remove(focus);
      //             focus=selected;
      //             box.setSelectedItem(selected);
      //           }
                
      //       } 
      //       repaint();
      //     }
      //   });



      this.addMouseListener(new MouseAdapter() {
        public void mousePressed(MouseEvent e) {
          startDrag = new Point(e.getX(), e.getY());
          endDrag = startDrag;
          repaint();
        }

        public void mouseReleased(MouseEvent e) {
          Shape r = makeRectangle(startDrag.x, startDrag.y, e.getX(), e.getY());
          if(now!=null){
            now=r;
            return;
          }
          now = r;
          startDrag = null;
          endDrag = null;
          textField.setText("");
          textField.setEnabled(true);
          
        }

        public void mouseClicked (MouseEvent e) 
        {
          if(e.getModifiers() == MouseEvent.BUTTON3_MASK)//e.getClickCount() == 2) 
          {       // write here your event handling code
            String key = getShape((double)e.getX(),(double)e.getY());
            System.out.println("You clicked two times on the button");
            if(key!=null){
              //shapes.remove(focus);
              textField.setText("");
              shapeMap.remove(key);
              box.removeItem(key);
              if(linkMap.containsKey(key)){
                linkMap.remove(key);
              }
              //items.remove(focus);
              //focus=null;
              //now=null;
            }
          }
          repaint();
        }
      });

      this.addMouseMotionListener(new MouseMotionAdapter() {
        public void mouseDragged(MouseEvent e) {
          endDrag = new Point(e.getX(), e.getY());
          repaint();
        }
      });
    }

    public HashMap<String,Shape> getShapeMap(){
      return shapeMap;
    }

    public HashMap<String,Link> getLinkMap(){
      return linkMap;
    }

    public void setNow(Shape now){
      this.now = now;
    }
    public Shape getNow(){
      return this.now;
    }

    public void updateShapeMap(String name,Shape s,String focus){
      shapeMap.put(name,s);
      shapeMap.remove(focus);
    }

    public void putShapeMap(String name,Shape s){
      shapeMap.put(name,s);
    }

    public void updateLinkMap(String name,Link l,String focus){
      linkMap.put(name,l);
      linkMap.remove(focus);
    }

    public void setFocus(String focus){
      this.focus = focus;
      repaint();
    }
    public String getFocus(String focus){
      return this.focus;
    }

    private void paintBackground(Graphics2D g2){
      g2.drawImage(image, 0, 0, null);
    }

    public void defineLink(String name, int sFrame,String spath,TreeMap<Integer,Shape> frameMap){
      //focus = name shapeMap.get(focus) = shape
      //System.out.println("define"+spath);
      Link link = new Link(name,spath,sFrame,frameMap);//change link not map
      linkMap.put(name,link);
      System.out.println("connect one link");
    }

    public void refresh(BufferedImage image,int fNumber,int sNumber, String spath){
      this.image = image;
      this.fNumber =fNumber;
      this.sNumber =sNumber;
      this.spath = spath;
      shapeMap.clear();
      for(Link ele:linkMap.values()){
        if(ele.getSourceFrame()==fNumber){
          shapeMap.put(ele.getName(),ele.getShape());
        }
      }
      repaint();
    }

    public void refreshSlider(BufferedImage image,int fNumber,int sNumber, String spath){
      this.image = image;
      this.fNumber =fNumber;
      this.sNumber =sNumber;
      this.spath = spath;
      shapeMap.clear();
      for(Link ele:linkMap.values()){
        if(ele.getFrameMap().containsKey(fNumber)){
          shapeMap.put(ele.getName(),ele.getFrameMap().get(fNumber));
        }
      }
      repaint();
    }
    // public void refreshSlider(BufferedImage image,int fNumber,int sNumber, String spath){
    //   this.image = image;
    //   this.fNumber =fNumber;
    //   this.sNumber =sNumber;
    //   this.spath = spath;
    //   shapeMap.clear();
    //   for(Link ele:linkMap.values()){
    //     if(ele.getSourceFrame()==fNumber){
    //       shapeMap.put(ele.getName(),ele.getShape());
    //     }
    //   }
    //   repaint();
    // }

    public ArrayList<Map.Entry<String,Shape>> getShapes(){
      return new ArrayList<Map.Entry<String,Shape>>(shapeMap.entrySet());
    }

    public ArrayList<Map.Entry<String,Link>> getLinks(){
      return new ArrayList<Map.Entry<String,Link>>(linkMap.entrySet());
    }

    public String getShape(double x, double y){
      for (String key : shapeMap.keySet()){
        if(shapeMap.get(key).contains(x,y)){
          return key;
        }
      }
      return null;
    }

    public Shape getFocusShape(){
      if(focus!=null){
        return shapeMap.get(focus);
      }
      return null;
      
    }

    public String getFocus(){
      return focus;
    }

    public Link getLink(String name){
      return linkMap.get(name);
    }

    public void paint(Graphics g) {
      Graphics2D g2 = (Graphics2D) g;
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      paintBackground(g2);
      //Color[] colors = { Color.YELLOW, Color.MAGENTA, Color.CYAN , Color.RED, Color.BLUE, Color.PINK};
      //int colorIndex = 0;

      g2.setStroke(new BasicStroke(2));
      g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.50f));
      // System.out.println("the size is"+shapeMap.size());
      for (String key : shapeMap.keySet()) {
        //System.out.println("the key is"+key);
        g2.setPaint(Color.BLACK);
        g2.draw(shapeMap.get(key));
        if(key==focus){
          g2.setPaint(Color.RED);
        }else{
          g2.setPaint(Color.BLUE);
        }
        g2.fill(shapeMap.get(key));
      }

      if (startDrag != null && endDrag != null) {
        g2.setPaint(Color.BLUE);
        Shape r = makeRectangle(startDrag.x, startDrag.y, endDrag.x, endDrag.y);
        g2.draw(r);
      }
    }

    private Rectangle2D.Float makeRectangle(int x1, int y1, int x2, int y2) {
      return new Rectangle2D.Float(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x1 - x2), Math.abs(y1 - y2));
    }
}