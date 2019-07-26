/**
 * - 02/2008: Class created by Nicolas Richasse
 *
 * Changelog:
 * 	- class created
 * 	- iperf line parsing fixed and improved
 *
 * To do:
 * 	- ...
 *
 * Old notes:
 *	- The ParseLine results variable still ends up with a blank 0th string which may or may not ever matter (DC)
 */

package net.nlanr.jperf.core;

import java.io.*;

import net.nlanr.jperf.ui.JPerfUI;
import net.nlanr.jperf.ui.JPerfWaitWindow;

import java.util.Vector;
import java.util.regex.*;

import javax.swing.SwingUtilities;

public class IperfThread extends Thread
{
	private String										command;
	private Process										process;
	private JPerfUI										frame;
	private Vector<JperfStreamResult>	finalResults;

	private BufferedReader						input;
	private BufferedReader						errors;

	private boolean										isServerMode;

	public IperfThread(boolean isServerMode, String command, JPerfUI mainframe)
	{
		this.isServerMode = isServerMode;
		this.command = command;
		this.frame = mainframe;
		this.finalResults = new Vector<JperfStreamResult>();
		this.frame.logMessage(command);
	}

	public void run()
	{
		try
		{
			frame.setStartedStatus();

			process = Runtime.getRuntime().exec(command);

			// read in the output from Iperf
			input = new BufferedReader(new InputStreamReader(process.getInputStream()));
			errors = new BufferedReader(new InputStreamReader(process.getErrorStream()));

			String input_line = null;
			while ((input_line = input.readLine()) != null)
			{
				parseLine(input_line);
				frame.logMessage(input_line);
			}

			String error_line = null;
			while ((error_line = errors.readLine()) != null)
			{
				frame.logMessage(error_line);
			}

			frame.logMessage("Done.\n");
		}
		catch (Exception e)
		{
			// don't do anything?
			frame.logMessage("\nIperf thread stopped [CAUSE=" + e.getMessage() + "]");
		}
		finally
		{
			quit();
		}
	}

	private Object waitWindowMutex = new Object();
	private JPerfWaitWindow waitWindow;

	public synchronized void quit()
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				if (process != null)
				{
					synchronized(waitWindowMutex)
					{
						if (waitWindow != null)
						{
							return;
						}
						waitWindow = new JPerfWaitWindow(frame);
						frame.setEnabled(false);
						waitWindow.setVisible(true);
					}
					Thread t = new Thread()
					{
						public void run()
						{
							process.destroy();

							if (!isServerMode)
							{
								try
								{
									process.getInputStream().close();
								}
								catch (Exception e)
								{
									// nothing
								}

								try
								{
									process.getOutputStream().close();
								}
								catch (Exception e)
								{
									// nothing
								}

								try
								{
									process.getErrorStream().close();
								}
								catch (Exception e)
								{
									// nothing
								}

								if (input != null)
								{
									try
									{
										input.close();
									}
									catch (Exception e)
									{
										// nothing
									}
									finally
									{
										input = null;
									}
								}

								if (errors != null)
								{
									try
									{
										errors.close();
									}
									catch (Exception e)
									{
										// nothing
									}
									finally
									{
										errors = null;
									}
								}
							}

							try
							{
								process.waitFor();
							}
							catch (Exception ie)
							{
								// nothing
							}

							process = null;

							synchronized(waitWindowMutex)
							{
								waitWindow.setVisible(false);
								waitWindow.dispose();
								waitWindow = null;
								frame.setStoppedStatus();
								frame.setEnabled(true);
							}
						}
					};
					t.setDaemon(true);
					t.start();
				}
			}
		});
	}

	public void parseLine(String line)
	{
		// only want the actual output lines
		// TODO: --bidir
/*
[  5][TX-C]   9.00-10.00  sec  36.2 MBytes   304 Mbits/sec    0   3.02 MBytes
[  7][RX-C]   9.00-10.00  sec  6.87 MBytes  57.6 Mbits/sec
- - - - - - - - - - - - - - - - - - - - - - - - -
[ ID][Role] Interval           Transfer     Bitrate         Retr
[  5][TX-C]   0.00-10.00  sec   350 MBytes   294 Mbits/sec    0             sender
[  5][TX-C]   0.00-10.05  sec   354 MBytes   295 Mbits/sec                  receiver
[  7][RX-C]   0.00-10.00  sec  92.5 MBytes  77.6 Mbits/sec    0             sender
[  7][RX-C]   0.00-10.05  sec  91.6 MBytes  76.4 Mbits/sec                  receiver
*/
		if (line.matches("\\[[ \\d]+\\]\\s*[\\d]+.*"))  //TCP
		{
			Pattern p = Pattern.compile("[-\\[\\]\\s]+");
			// ok now break up the line into id#, interval, amount transfered, format
			// transferred, bandwidth, and format of bandwidth
			String[] results = p.split(line);
			//TCP
			//          id      start    end                  Transfer                  Bitrate                         Retr  Cwnd
			//{ "", "5", "0.00", "1.00", "sec", "11.2", "MBytes", "93.9", "Mbits/sec", "251", "13.4", "KBytes" }
			// get the ID # for the stream
			//Integer temp = new Integer(results[1].trim());
			Integer temp = Integer.valueOf(results[1].trim());
			int id = temp.intValue();

			boolean found = false;
			JperfStreamResult streamResult = new JperfStreamResult(id);
			for (int i = 0; i < finalResults.size(); ++i)
			{
				if ((finalResults.elementAt(i)).getID() == id)
				{
					streamResult = finalResults.elementAt(i);
					found = true;
					break;
				}
			}

			if (!found)
			{
				finalResults.add(streamResult);
			}
			// this is TCP or Client UDP
			//~ System.out.printf("TCP=>> %d\n", results.length);
			if (results.length == 14)
			{
				//Double start = new Double(results[2].trim());
				//Double end = new Double(results[3].trim());
				//Double bw = new Double(results[7].trim());
				Double start = Double.valueOf(results[2].trim());
				Double end = Double.valueOf(results[3].trim());
				Double bw = Double.valueOf(results[7].trim());

				Measurement B = new Measurement(start.doubleValue(), end.doubleValue(), bw.doubleValue(), results[7]);
				streamResult.addBW(B);

				//Double jitter = new Double(results[9].trim());
				Double jitter = Double.valueOf(results[9].trim());
				Measurement J = new Measurement(start.doubleValue(), end.doubleValue(), jitter.doubleValue(), results[10]);
				streamResult.addJitter(J);
				frame.addNewStreamBandwidthAndJitterMeasurement(id, B, J);
			}
			else  if (results.length >= 9)
			{
				//Double start = new Double(results[2].trim());
				//Double end = new Double(results[3].trim());
				//Double bw = new Double(results[7].trim());
				Double start = Double.valueOf(results[2].trim());
				Double end = Double.valueOf(results[3].trim());
				Double bw = Double.valueOf(results[7].trim());

				Measurement M = new Measurement(start.doubleValue(), end.doubleValue(), bw.doubleValue(), results[8]);
				streamResult.addBW(M);
				frame.addNewStreamBandwidthMeasurement(id, M);
			}

		}
		else if (line.matches(".*\\[[ \\d]+\\]\\s*[\\d]+.*"))  //UDP
		{
			//~ System.out.printf("UDP: %s\n", line);
			Pattern p = Pattern.compile("[-\\[\\]\\s]+");
			// ok now break up the line into id#, interval, amount transfered, format
			// transferred, bandwidth, and format of bandwidth
			String[] results = p.split(line);

			// get the ID # for the stream
			//Integer temp = new Integer(results[1].trim());
			Integer temp = Integer.valueOf(results[1].trim());
			int id = temp.intValue();

			boolean found = false;
			JperfStreamResult streamResult = new JperfStreamResult(id);
			for (int i = 0; i < finalResults.size(); ++i)
			{
				if ((finalResults.elementAt(i)).getID() == id)
				{
					streamResult = finalResults.elementAt(i);
					found = true;
					break;
				}
			}

			if (!found)
			{
				finalResults.add(streamResult);
			}
			// this is TCP or Client UDP
			//~ System.out.printf("UDP=>> %d\n", results.length);
			if (results.length == 14)
			{
				Double start = Double.valueOf(results[2].trim());
				Double end = Double.valueOf(results[3].trim());
				Double bw = Double.valueOf(results[7].trim());

				Measurement B = new Measurement(start.doubleValue(), end.doubleValue(), bw.doubleValue(), results[7]);
				streamResult.addBW(B);
				frame.addNewStreamBandwidthMeasurement(id, B);

				Double jitter = Double.valueOf(results[9].trim());
				Measurement J = new Measurement(start.doubleValue(), end.doubleValue(), jitter.doubleValue(), results[10]);
				streamResult.addJitter(J);
				frame.addNewStreamBandwidthAndJitterMeasurement(id, B, J);
			} else if (results.length >= 10)
			{
				Double start = Double.valueOf(results[2].trim());
				Double end = Double.valueOf(results[3].trim());
				Double bw = Double.valueOf(results[7].trim());

				Measurement B = new Measurement(start.doubleValue(), end.doubleValue(), bw.doubleValue(), results[7]);
				streamResult.addBW(B);
				frame.addNewStreamBandwidthMeasurement(id, B);
			}
		}

	}
}
