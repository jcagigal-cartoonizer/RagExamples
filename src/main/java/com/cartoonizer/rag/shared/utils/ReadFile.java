package com.cartoonizer.rag.shared.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public abstract class ReadFile {
	protected File origen;
	protected BufferedReader dataIn;
        protected int numLiniesLlegides = 0;

	public ReadFile(String pathOrigen) {
		origen = new File(pathOrigen);
		try {
			FileInputStream in = new FileInputStream(origen);
			InputStreamReader is = new InputStreamReader(in);
			dataIn = new BufferedReader(is);
		} catch (Exception e) {
                    System.out.println("ERROR constructor: pathOrigen = " + pathOrigen + ": " + e);
		}

	}

	public void begin() {
            numLiniesLlegides = 0;
		
	}
	public void end() {
		
	}
	public final void load() {
		try {
			String line;
			begin();
			while ((line = dataIn.readLine()) != null) {
                            numLiniesLlegides++;
                            processLine(line);
			}
			end();
		} catch (IOException e) {
			System.err.println(e); 
                        e.printStackTrace();
		}
	}
	public abstract void processLine(String line);
    
}
