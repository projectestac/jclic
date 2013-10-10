/*
 * AppletMpegSPIWorkaround.
 *
 *-----------------------------------------------------------------------
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Library General Public License as published
 *   by the Free Software Foundation; either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Library General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this program; if not, write to the Free Software
 *   Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *----------------------------------------------------------------------
 */

package org.tritonus.applet;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * From JRE/JDK 1.3.0_01 on, applets can not use provided service
 * providers. Obviously, in these later releases of the Java 2 platform
 * the service providers are only searched on the system/boot classloader
 * and NOT on the classloader of the applet.
 * Workaround found by Tritonus Team.
 */

public class AppletMpegSPIWorkaround
{
  public static boolean DEBUG = false;
  public static String useragent = null;

  public static AudioInputStream getAudioInputStream(AudioFormat targetFormat,
	  AudioInputStream sourceStream)
  {
	try
	{
	  return AudioSystem.getAudioInputStream(targetFormat, sourceStream);
	}
	catch (IllegalArgumentException iae)
	{
	  if (DEBUG == true)
	  {
		System.err.println("Using AppletMpegSPIWorkaround to get codec");
	  }
	  try
	  {
		Class.forName(
			"javazoom.spi.mpeg.sampled.convert.MpegFormatConversionProvider");
			//"org.tritonus.sampled.convert.javalayer.MpegFormatConversionProvider");
		return new javazoom.spi.mpeg.sampled.convert.MpegFormatConversionProvider().
		//return new org.tritonus.sampled.convert.javalayer.MpegFormatConversionProvider().
			getAudioInputStream(targetFormat, sourceStream);
	  }
	  catch (ClassNotFoundException cnfe)
	  {
		throw new IllegalArgumentException("Mpeg codec not properly installed");
	  }
	}
  }

  public static AudioInputStream getAudioInputStream(File file) throws
	  UnsupportedAudioFileException, IOException
  {
	InputStream inputStream = new BufferedInputStream(new FileInputStream(file));
	try
	{
	  if (DEBUG == true)
	  {
		System.err.println(
			"Using AppletMpegSPIWorkaround to get codec (AudioInputStream:file)");
	  }

	  return getAudioInputStream(inputStream);
	}
	catch (UnsupportedAudioFileException e)
	{
	  inputStream.close();
	  throw e;
	}
	catch (IOException e)
	{
	  inputStream.close();
	  throw e;
	}
  }

  public static AudioInputStream getAudioInputStream(URL url) throws
	  UnsupportedAudioFileException, IOException
  {
	  //alexey fix: we use MpegAudioFileReaderWorkaround with URL and user agent
	  try {
		  Class.forName("javazoom.spi.mpeg.sampled.file.MpegAudioFileReader");
		  //Class.forName("org.tritonus.sampled.file.mpeg.MpegAudioFileReader");
		  return new MpegAudioFileReaderWorkaround().getAudioInputStream(url, useragent);
	  } catch (ClassNotFoundException cnfe) {
		  throw new IllegalArgumentException("Mpeg codec not properly installed");
	  }

	//InputStream inputStream = new BufferedInputStream(url.openStream());
	//try
	//{
	//  if (DEBUG == true)
	//  {
	//    System.err.println(
	//        "Using AppletMpegSPIWorkaround to get codec (AudioInputStream:url)");
	//  }
	//  return getAudioInputStream(inputStream);
	//}
	//catch (UnsupportedAudioFileException e)
	//{
	//  inputStream.close();
	//  throw e;
	//}
	//catch (IOException e)
	//{
	//  inputStream.close();
	//  throw e;
	//}
  }

  public static AudioFileFormat getAudioFileFormat(File file) throws
	  UnsupportedAudioFileException, IOException
  {
	InputStream inputStream = new BufferedInputStream(new FileInputStream(file));
	try
	{
	  if (DEBUG == true)
	  {
		System.err.println(
			"Using AppletMpegSPIWorkaround to get codec (AudioFileFormat:file)");
	  }

	  return getAudioFileFormat(inputStream);
	}
	finally
	{
	  inputStream.close();
	}
  }

  public static AudioFileFormat getAudioFileFormat(URL url) throws
	  UnsupportedAudioFileException, IOException
  {
	InputStream inputStream = null;
	if (useragent != null)
	{
	  URLConnection myCon = url.openConnection();
	  myCon.setUseCaches(false);
	  myCon.setDoInput(true);
	  myCon.setDoOutput(true);
	  myCon.setAllowUserInteraction(false);
	  myCon.setRequestProperty("User-Agent", useragent);
	  myCon.setRequestProperty("Accept", "*/*");
	  myCon.setRequestProperty("Icy-Metadata", "1");
	  myCon.setRequestProperty("Connection", "close");
	  inputStream = new BufferedInputStream(myCon.getInputStream());
	}
	else
	{
		inputStream = new BufferedInputStream(url.openStream());
	}

	try
	{
	  if (DEBUG == true)
	  {
		System.err.println(
			"Using AppletMpegSPIWorkaround to get codec (AudioFileFormat:url)");
	  }
	  //alexey fix: use another version of method
	  return getAudioFileFormatForUrl(inputStream);
	  //return getAudioFileFormat(inputStream);
	}
	finally
	{
	  inputStream.close();
	}
  }

	//alexey fix: we use MpegAudioFileReader's getAudioFileFormat
	public static AudioFileFormat getAudioFileFormatForUrl(InputStream is) throws
	  UnsupportedAudioFileException, IOException
  {
	try
	{
	  //return AudioSystem.getAudioFileFormat(is);
	  throw new Exception();
	}
	catch (Exception iae)
	{
	  if (DEBUG == true)
	  {
		System.err.println(
			"Using AppletMpegSPIWorkaround to get codec (AudioFileFormat)");
	  }
	  try
	  {
		Class.forName("javazoom.spi.mpeg.sampled.file.MpegAudioFileReader");
		//Class.forName("org.tritonus.sampled.file.mpeg.MpegAudioFileReader");
		
		// 2.2 Fix.
		//is.mark(4096);
		return new javazoom.spi.mpeg.sampled.file.MpegAudioFileReader().
		//return new org.tritonus.sampled.file.mpeg.MpegAudioFileReader().
			getAudioFileFormat(is, AudioSystem.NOT_SPECIFIED);
	  }
	  catch (ClassNotFoundException cnfe)
	  {
		throw new IllegalArgumentException("Mpeg codec not properly installed");
	  }
	}
	// 2.2 Fix.
	finally
	{
	  //is.reset();
	}
  }

  public static AudioFileFormat getAudioFileFormat(InputStream is) throws
	  UnsupportedAudioFileException, IOException
  {
	try
	{
	  //return AudioSystem.getAudioFileFormat(is);
	  throw new Exception();
	}
	catch (Exception iae)
	{
	  if (DEBUG == true)
	  {
		System.err.println(
			"Using AppletMpegSPIWorkaround to get codec (AudioFileFormat)");
	  }
	  try
	  {
		Class.forName("javazoom.spi.mpeg.sampled.file.MpegAudioFileReader");		
		//Class.forName("org.tritonus.sampled.file.mpeg.MpegAudioFileReader");		
		// 2.2 Fix.
		is.mark(4096);
		return new javazoom.spi.mpeg.sampled.file.MpegAudioFileReader().
		//return new org.tritonus.sampled.file.mpeg.MpegAudioFileReader().
			getAudioFileFormat(is);
	  }
	  catch (ClassNotFoundException cnfe)
	  {
		throw new IllegalArgumentException("Mpeg codec not properly installed");
	  }
	}
	// 2.2 Fix.
	finally
	{
	  is.reset();
	}
  }

  public static AudioInputStream getAudioInputStream(InputStream is) throws
	  UnsupportedAudioFileException, IOException
  {
	try
	{
	  //return AudioSystem.getAudioInputStream(is);
	  throw new Exception();
	}
	catch (Exception iae)
	{
	  if (DEBUG == true)
	  {
		System.err.println("Using AppletMpegSPIWorkaround to get codec");
	  }
	  try
	  {
		Class.forName("javazoom.spi.mpeg.sampled.file.MpegAudioFileReader");
		//Class.forName("org.tritonus.sampled.file.mpeg.MpegAudioFileReader");
		return new javazoom.spi.mpeg.sampled.file.MpegAudioFileReader().getAudioInputStream(is);
		//return new org.tritonus.sampled.file.mpeg.MpegAudioFileReader().getAudioInputStream(is);
        }
	  catch (ClassNotFoundException cnfe)
	  {
		throw new IllegalArgumentException("Mpeg codec not properly installed");
	  }
	}
  }
}