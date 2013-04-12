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

package org.infoglue.deliver.util;

/**
 * @author Mattias Bogeblad
 *
 * This class is a timer utility to debug performance issues. It allows to start/stop the timer
 * and to report in between what the time is.
 * 
 */

public class Timer
{
	private long startTime 		= 0;
	private long elapsedTime 	= 0;
	private long lastPrintTime  = 0;
	private long startTimeNanos = 0;
	private long elapsedTimeNanos 	= 0;
	private long lastPrintTimeNanos  = 0;
	private boolean isActive 	= true;
	private long initialMemory	= 0;
	
	public Timer()
	{
		startTime = System.currentTimeMillis();
		lastPrintTime = startTime;
		startTimeNanos = System.nanoTime();
		lastPrintTimeNanos = startTimeNanos;
		initialMemory = Runtime.getRuntime().freeMemory();
	}

	public long getMemoryDifferenceAsMegaBytes()
	{
		long diff = Runtime.getRuntime().freeMemory() - initialMemory;
		return (diff < 1 ? 0 : (diff/1000/1000));
	}
	
	public long getElapsedTime()
	{
		elapsedTime = System.currentTimeMillis() - lastPrintTime;
		lastPrintTime = System.currentTimeMillis();
		return elapsedTime;
	}

	public long getElapsedTimeNanos()
	{
		elapsedTimeNanos = System.nanoTime() - lastPrintTimeNanos;
		lastPrintTimeNanos = System.nanoTime();
		return elapsedTimeNanos;
	}

	public void printElapsedTime(String message)
	{
		if(this.isActive)
		{
			elapsedTime = System.currentTimeMillis() - lastPrintTime;
			lastPrintTime = System.currentTimeMillis();
			System.out.println(message + " - Elapsed time since last report: " + elapsedTime);
		}
	}
	
	public void printElapsedTime(String message, int minimumTimeToPrint)
	{
		if(this.isActive)
		{
			elapsedTime = System.currentTimeMillis() - lastPrintTime;
			lastPrintTime = System.currentTimeMillis();
			if(elapsedTime > minimumTimeToPrint)
				System.out.println(message + " - Elapsed time since last report: " + elapsedTime);
		}
	}

	public void printElapsedTimeNano(String message)
	{
		if(this.isActive)
		{
			elapsedTimeNanos = System.nanoTime() - lastPrintTimeNanos;
			lastPrintTimeNanos = System.nanoTime();
			System.out.println(message + " - Elapsed time since last report (ns): " + elapsedTimeNanos);
		}
	}

	public void printElapsedTimeMicro(String message)
	{
		if(this.isActive)
		{
			elapsedTimeNanos = System.nanoTime() - lastPrintTimeNanos;
			lastPrintTimeNanos = System.nanoTime();
			System.out.println(message + " - Elapsed time since last report (microsecond): " + elapsedTimeNanos / 1000);
		}
	}

	public boolean getIsActive()
	{
		return this.isActive;
	}

	public void setActive(boolean isActive)
	{
		this.isActive = isActive;
	}

}
