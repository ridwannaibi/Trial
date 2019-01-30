package hu.mta.sztaki.lpds.cloud.simulator.helpers.trace.sinecurve;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Polygon;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.awt.Color;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class SineWaveEdit extends JFrame {

	/**
		 * 
		 */

	private static final long serialVersionUID = 1L;

	public SineWaveEdit() {
		setLayout(new BorderLayout());
		add(new DrawSine2(), BorderLayout.CENTER);
	}

	public static void main(String[] args) {
		SineWaveEdit frame = new SineWaveEdit();
		frame.setSize(400, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);

	}

	class DrawSine2 extends JPanel {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		double sineX(double x) {
			return Math.sin(x);
		}

		double negativeSineX(double x) {
			return -1 * Math.sin(x);
		}

		protected void paintComponent(Graphics g) {
			super.paintComponent(g);

			g.drawLine(0, 0, 1200, 0);
			g.drawLine(0, 0, 0, 300);

			g.drawString("X", 50, 10);
			g.drawString("Y", 10, 250);

			Polygon p = new Polygon();
			Polygon p2 = new Polygon();

			Polygon p3 = new Polygon();

			FileWriter fileWriter = null;
			try {
				fileWriter = new FileWriter(
						"/home/campus.ncl.ac.uk/b6000563/Documents/dissect-cf-examples-vmstat/src/main/java/hu/mta/sztaki/lpds/cloud/simulator/trial/vmsstat.txt");
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			String newLine = System.getProperty("line.separator");
			String value;
			int y;
			// Red line
			for (int x = 0; x <= 100000; x++) {
				p.addPoint(x, Math.max(0, (int) (100 * negativeSineX((x / 100.0)))));

				p2.addPoint(x, Math.max(0, (int) (100 * sineX((x / 100.0)))));
				y = Math.max(0, (int) (100 * negativeSineX((x / 100.0))));

				value = 0 + " " + 0 + " " + 0 + " " + 7205980 + " " + 194140 + " " + 3359552 + " " + 0 + " " + 0 + " "
					
						+ 00 + " " + 00 + " " + 000 + " " + 000 + " " + 00 + " " + y + " " + 00 + " " + 0 + " "
						+ 0;

				try {
					fileWriter.write(value + newLine);
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
			// Blue line
			for (int x = 0; x <= 1000; x++) {

			}

			String line;

			try {

				BufferedReader bufferreader = new BufferedReader(new FileReader(
						"/home/campus.ncl.ac.uk/b6000563/Documents/dissect-cf-examples-vmstat/src/main/java/hu/mta/sztaki/lpds/cloud/simulator/trial/vmsstat.txt"));

				while ((line = bufferreader.readLine()) != null) {
					/**
					 * Your implementation
					 **/
					line = bufferreader.readLine();
					System.out.println(line);
				}
				;
				bufferreader.close();

			} catch (FileNotFoundException ex) {
				ex.printStackTrace();
			} catch (IOException ex) {
				ex.printStackTrace();
			}

			/*
			 * for (int x = 300; x <=1200 ; x++) { p3.addPoint(x , 300 - Math.max(0, (int)
			 * (100 * negativeSineX((x / 100.0-300)))));
			 * 
			 * }
			 */

			g.setColor(Color.RED);
			g.drawPolyline(p.xpoints, p.ypoints, p.npoints);

			g.drawString("0", 200, 115);

			g.setColor(Color.BLUE);
			g.drawPolyline(p2.xpoints, p2.ypoints, p2.npoints);

			g.setColor(Color.GREEN);
			g.drawPolyline(p3.xpoints, p3.ypoints, p3.npoints);

		}
	}
}