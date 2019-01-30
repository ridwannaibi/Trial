package hu.mta.sztaki.lpds.cloud.simulator.helpers.trace.sinecurve;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.Color;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class SineWave extends JFrame {

/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

public SineWave() {
    setLayout(new BorderLayout());
    add(new DrawSine(), BorderLayout.CENTER);
}

public static void main(String[] args) {
	SineWave frame = new SineWave();
    frame.setSize(400, 300);
    frame.setTitle("Exercise13_12");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);

}

class DrawSine extends JPanel {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	double f(double x) {
        return Math.sin(x);
    }

    double gCos(double y) {
        return Math.cos(y);
    }

    protected void paintComponent(Graphics g) 
    {
        super.paintComponent(g);

        g.drawLine(10, 100, 700, 100);
        g.drawLine(200, 30, 200, 300);

        //g.drawLine(380, 100, 370, 90);
        //g.drawLine(380, 100, 370, 110);
        //g.drawLine(200, 30, 190, 40);
        //g.drawLine(200, 30, 210, 40);

        g.drawString("X", 360, 80);
        g.drawString("Y", 220, 40);

        Polygon p = new Polygon();
        Polygon p2 = new Polygon();

//       for (int x = -300; x <= 300; x++) {
//            p.addPoint(x + 300, 100 - (int) (50 * f((x / 100.0) * 1
//                    * Math.PI)));
//           
//            
//
//        }

//        for (int x = -300; x <= 300; x++) {
//            p2.addPoint(x + 300, 100 - (int) (50 * gCos((x / 100.0) * 1
//                    * Math.PI)));
//
//        }

        g.setColor(Color.red);
        g.drawPolyline(p.xpoints, p.ypoints, p.npoints);
        g.drawString("-2\u03c0", 95, 115);
        g.drawString("2\u03c0", 305, 115);
        g.drawString("0", 200, 115);

        g.setColor(Color.blue);
        g.drawPolyline(p2.xpoints, p2.ypoints, p2.npoints);

    }
 }
}