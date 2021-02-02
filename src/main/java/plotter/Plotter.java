package plotter;

import javafx.collections.ObservableList;
import javafx.scene.canvas.Canvas;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.TickUnits;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PolarPlot;
import org.jfree.chart.renderer.DefaultPolarItemRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.awt.*;
import java.util.ArrayList;
import java.util.Map;

public class Plotter {

    private final Color BACKGROUND_GRAY = new Color(244, 244, 244, 255);

    public ChartCanvas getEmptyGraph(double minScale, double maxScale) {
        JFreeChart chart = createChart(new XYSeriesCollection(), minScale, maxScale);
        return new ChartCanvas(chart);
    }

    private JFreeChart createChart(XYDataset dataset, double min, double max) {
        JFreeChart chart = ChartFactory.createPolarChart(
                "",
                dataset,
                false,
                false,
                false
        );

        PolarPlot plot = (PolarPlot) chart.getPlot();
        plot.setRadiusMinorGridlinesVisible(false);
        plot.setRadiusGridlinesVisible(true);
        plot.setAngleGridlinesVisible(true);
        plot.setAngleLabelsVisible(true);

        plot.setOutlinePaint(BACKGROUND_GRAY);
        plot.setBackgroundPaint(BACKGROUND_GRAY);
        plot.setRadiusGridlinePaint(Color.GRAY);
        plot.setAngleGridlinePaint(Color.BLACK);

        plot.getAxis().setRange(min, max);

        DefaultPolarItemRenderer renderer = (DefaultPolarItemRenderer) plot.getRenderer();

        renderer.setConnectFirstAndLastPoint(false);

        return chart;
    }

    public Canvas getGraph(ObservableList<Map.Entry<Integer, ArrayList<Double>>> dbData,
                           int activeFreqIndex,
                           double minScale,
                           double maxScale) {
        XYSeries series = new XYSeries("label", false);
        dbData.forEach(entry -> series.add(entry.getKey(), entry.getValue().get(activeFreqIndex)));

        final JFreeChart chart = createChart(new XYSeriesCollection(series), minScale, maxScale);

        return new ChartCanvas(chart);
    }
}
