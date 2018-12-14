//package com.linktool;
import java.awt.geom.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import java.lang.*;
import java.util.*;


public class Link {
	String name;
	String targetPath;
	int targetFrame;
	int sourceFrame;
	Shape shape;
	TreeMap<Integer,Shape> frameMap;
	public Link(String name, String targetPath, int targetFrame, TreeMap<Integer,Shape> frameMap){
		this.name = name;
		this.targetPath = targetPath;
		this.targetFrame = targetFrame;
		this.sourceFrame =frameMap.firstKey();
		this.shape=frameMap.get(sourceFrame);
		this.frameMap = frameMap;
	}

	// public Rectangle2D.Float makeRectangle() {
 //      return new Rectangle2D.Float(x, y, width, height);
 //    }

    public Shape getShape(){
    	return this.shape;
    }
    public String getName(){
    	return this.name;
    }
    public String getTargetPath(){
    	return this.targetPath;
    }
    public int getTargetFrame(){
    	return this.targetFrame;
    }
    public int getSourceFrame(){
    	return this.sourceFrame;
    }
    public TreeMap<Integer,Shape> getFrameMap(){
    	return this.frameMap;
    }
}