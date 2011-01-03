/* ===============================================================================
 *
 * Part of the InfoGlue Content Management Platform (www.infoglue.org)
 *
 * ===============================================================================
 *
 *  Copyright (C)
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License version 2, as published by the
 * Free Software Foundation. See the file LICENSE.html for more information.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, including the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc. / 59 Temple
 * Place, Suite 330 / Boston, MA 02111-1307 / USA.
 *
 * ===============================================================================
 */
 
package org.infoglue.deliver.util.charts;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.infoglue.cms.util.dom.DOMBuilder;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardLegend;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickMarkPosition;
import org.jfree.chart.axis.DateTickUnit;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.time.Month;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.Week;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.Spacer;

/**
 * An example of a time series chart.  For the most part, default settings are used, except that
 * the renderer is modified to show filled shapes (as well as lines) at each data point.
 *
 * @author David Gilbert
 */
public class TimeSeriesDiagram implements XMLDataDiagram 
{

	private JFreeChart chart;
	private String xmlData;
	private String dateFormat;
	private String header;
	private String axisYHeader;
	private String axisXHeader;
	private String timeGranulariry;
	
	public JFreeChart getChart() 
	{
		return chart;
	}

	public void setChart(JFreeChart chart) 
	{
		this.chart = chart;
	}


	public void renderChart() throws Exception
	{
		XYDataset dataset = createDataset();

		JFreeChart chart = createChart(dataset);
		this.chart = chart;
	}
	
	/**
	 * Creates a chart.
	 * 
	 * @param dataset  a dataset.
	 * 
	 * @return A chart.
	 */
	
	private JFreeChart createChart(XYDataset dataset) 
	{
		JFreeChart chart = ChartFactory.createTimeSeriesChart(header,	axisXHeader, axisYHeader, dataset, true, true, false);
        
		chart.setBackgroundPaint(Color.white);

		StandardLegend sl = (StandardLegend) chart.getLegend();
		sl.setDisplaySeriesShapes(true);

		XYPlot plot = chart.getXYPlot();
		plot.setBackgroundPaint(Color.lightGray);
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinePaint(Color.white);
		plot.setAxisOffset(new Spacer(Spacer.ABSOLUTE, 5.0, 5.0, 5.0, 5.0));
		plot.setDomainCrosshairVisible(true);
		plot.setRangeCrosshairVisible(true);
        
		XYItemRenderer renderer = plot.getRenderer();
		if (renderer instanceof StandardXYItemRenderer) {
			StandardXYItemRenderer rr = (StandardXYItemRenderer) renderer;
			rr.setPlotShapes(true);
			rr.setShapesFilled(true);
		}
        
		DateAxis axis = (DateAxis) plot.getDomainAxis();
		
		if(this.timeGranulariry.equalsIgnoreCase("Week"))
		{
			
			DateTickUnit unit = new DateTickUnit(DateTickUnit.DAY, 7, Calendar.getInstance().getFirstDayOfWeek(), new SimpleDateFormat(this.dateFormat));
			axis.setTickUnit(unit);
			axis.setTickMarkPosition(DateTickMarkPosition.START);
			 
			axis.setDateFormatOverride(new SimpleDateFormat(this.dateFormat));
		}
		else
		{
			axis.setDateFormatOverride(new SimpleDateFormat(this.dateFormat));
		}
		/*
		DateAxis axis = (DateAxis) plot.getDomainAxis();
		axis.setDateFormatOverride(new SimpleDateFormat(this.dateFormat));
        */
		return chart;

	}
    
	/**
	 * Creates a dataset, consisting of two series of monthly data.
	 *
	 * @return the dataset.
	 */
	private XYDataset createDataset() throws Exception
	{
		TimeSeriesCollection timeSeriesDataset = new TimeSeriesCollection();

		Document document = new DOMBuilder().getDocument(this.xmlData);
		this.writeDebug(document);
		
		Element headerElement = (Element)document.selectSingleNode("//chartHeader");
		this.header = headerElement.getText();
		Element axisYHeaderElement = (Element)document.selectSingleNode("//axisYHeader");
		this.axisYHeader = axisYHeaderElement.getText();
		Element axisXHeaderElement = (Element)document.selectSingleNode("//axisXHeader");
		this.axisXHeader = axisXHeaderElement.getText();
		Element timeGranularityElement = (Element)document.selectSingleNode("//timeGranularity");
		this.timeGranulariry = timeGranularityElement.getText();
		Element dateFormatElement = (Element)document.selectSingleNode("//dateFormat");
		this.dateFormat = dateFormatElement.getText();
						
		List series = document.selectNodes("//Series");
		
		Iterator seriesIterator = series.iterator();
		while(seriesIterator.hasNext())
		{
			Element serieElement = (Element)seriesIterator.next();
			String serieName = serieElement.attributeValue("name");
			
			TimeSeries s1 = null;
			if(this.timeGranulariry.equalsIgnoreCase("Month"))
				s1 = new TimeSeries(serieName, Month.class);
			else if(this.timeGranulariry.equalsIgnoreCase("Week"))
				s1 = new TimeSeries(serieName, Week.class);
		
			List items = serieElement.selectNodes("Item");
			Iterator itemsIterator = items.iterator();
			while(itemsIterator.hasNext()) 
			{
				Element itemElement = (Element)itemsIterator.next();
				Element yearElement = (Element)itemElement.selectSingleNode("yearId");
				Element timeElement = (Element)itemElement.selectSingleNode("timeId");
				Element valueElement = (Element)itemElement.selectSingleNode("value");
				String year = yearElement.getText();
				String time = timeElement.getText();
				String value = valueElement.getText();
				
				if(this.timeGranulariry.equalsIgnoreCase("Month"))
					s1.add(new Month(new Integer(time).intValue(), new Integer(year).intValue()), new Float(value));
				else if(this.timeGranulariry.equalsIgnoreCase("Week"))
					s1.add(new Week(new Integer(time).intValue(), new Integer(year).intValue()), new Float(value));				
			}
			


			timeSeriesDataset.addSeries(s1);
			//timeSeriesDataset.addSeries(s2);

			timeSeriesDataset.setDomainIsPointsInTime(true);

		}

		return timeSeriesDataset;

	}

	/**
	 * This method takes xml as input for the diagram.
	 */
	
	public void setDiagramData(String xmlData)
	{
		this.xmlData = xmlData;
	}
	

	/**
	 * This method creates a new Document from an xml-string.
	 */
	/*
	public Document getDocument(String xml) throws Exception
	{
		if(xml == null)
			return null;
					
		Document document = null;
		
		try
		{
			SAXReader xmlReader = new SAXReader();
			document = xmlReader.read(new StringReader(xml));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
 		
		return document;
	}
*/
	/**
	 * This method writes a document to System.out.
	 */

	public void writeDebug(Document document) throws Exception 
	{
		OutputFormat format = OutputFormat.createPrettyPrint();
		XMLWriter writer = new XMLWriter( System.out, format );
		writer.write( document );
	}
}

