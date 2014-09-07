package com.VictorZahraa.hybridmarker;

public class ValueStoringHelperClass 
{
	private static int numPages;
	private static double [] scorePerPage;
	
	public void setNumPages (int value) 
	{
		numPages = value;
		scorePerPage = new double [value];
	}
	
	public int getNumPage () {return numPages;}
	
	public void setPageScore (int pageIndex, double score)
	{
		scorePerPage[pageIndex] = score;
	}
	
	public double getPageScore (int pageIndex) {return scorePerPage[pageIndex];}
}
