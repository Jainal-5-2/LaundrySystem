package laundry;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.text.DecimalFormat;
import java.util.List;
import javax.swing.JPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;

/**
 * Thin wrapper around a JFreeChart bar chart, themed to match our palette.
 */
public class BarChart extends JPanel {

    public BarChart() {
        setLayout(new java.awt.BorderLayout());
        setOpaque(false);
    }

    /** Replaces the chart contents with a fresh dataset. */
    public void setData(List<ReportDAO.DailyRow> rows, String valueLabel) {
        removeAll();

        DefaultCategoryDataset ds = new DefaultCategoryDataset();
        for (ReportDAO.DailyRow r : rows) {
            // Shorten 2026-10-14 → 10/14 for the X axis
            String key = r.date.length() >= 10
                    ? r.date.substring(5).replace('-', '/')
                    : r.date;
            ds.addValue(r.sales, valueLabel, key);
        }

        JFreeChart chart = ChartFactory.createBarChart(
                null,               // title (we render our own)
                null,               // X label
                null,               // Y label
                ds,
                PlotOrientation.VERTICAL,
                false, false, true);
        style(chart);
        add(new ChartPanel(chart) {{
            setOpaque(false);
            setBackground(new Color(0, 0, 0, 0));
            setBorder(null);
        }}, java.awt.BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    private void style(JFreeChart chart) {
        chart.setBackgroundPaint(new Color(0, 0, 0, 0));
        chart.setBorderVisible(false);
        chart.setAntiAlias(true);

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(new Color(0, 0, 0, 0));
        plot.setOutlineVisible(false);
        plot.setRangeGridlinePaint(Theme.BORDER_SUBTLE);
        plot.setRangeGridlineStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 1f, new float[]{2f, 4f}, 0f));
        plot.setDomainGridlinesVisible(false);

        // Bars
        BarRenderer r = (BarRenderer) plot.getRenderer();
        r.setSeriesPaint(0, Theme.ACCENT);
        r.setBarPainter(new StandardBarPainter());
        r.setShadowVisible(false);
        r.setDrawBarOutline(false);
        r.setMaximumBarWidth(0.06);
        r.setDefaultToolTipGenerator(new StandardCategoryToolTipGenerator(
                "{1}: PHP {2}", new DecimalFormat("#,##0.00")));

        // X axis
        CategoryAxis x = plot.getDomainAxis();
        x.setTickLabelPaint(Theme.TEXT_SECONDARY);
        x.setTickLabelFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
        x.setAxisLinePaint(Theme.BORDER_SUBTLE);
        x.setTickMarksVisible(false);
        x.setCategoryMargin(0.3);

        // Y axis
        NumberAxis y = (NumberAxis) plot.getRangeAxis();
        y.setTickLabelPaint(Theme.TEXT_SECONDARY);
        y.setTickLabelFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
        y.setAxisLinePaint(Theme.BORDER_SUBTLE);
        y.setTickMarksVisible(false);
        y.setNumberFormatOverride(new DecimalFormat("#,##0"));
    }
}