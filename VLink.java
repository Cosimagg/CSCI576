//package com.linktool;
import java.awt.geom.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import java.lang.*;
import java.util.*;


public class VLink {
	String targetPath;
    Shape shape;
    String[] name;
    int sFrame=0;

	public VLink(Shape shape, String targetPath,int sFrame){
		this.shape = shape;
        File d=new File(targetPath);
        FilenameFilter fileFilter = new FilenameFilter() {
            public boolean accept(File directory, String fileName) {
                return fileName.endsWith(".rgb");
            }
        };
        this.name = d.list(fileFilter);
		this.targetPath = targetPath;
        this.sFrame = sFrame;
	}

	// public Rectangle2D.Float makeRectangle() {
 //      return new Rectangle2D.Float(x, y, width, height);
 //    }

    public Shape getShape(){
    	return this.shape;
    }
    public String getTargetPath(){
    	return this.targetPath;
    }
    public String[] getName(){
        return this.name;
    }

    public int getFrame(){
        return this.sFrame;
    }
}