/*
 *	MpegFormatConversionProvider.java
 */

/*
 *  Copyright (c) 1999 by Matthias Pfisterer <Matthias.Pfisterer@gmx.de>
 *
 *
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
 *
 */


package	org.tritonus.sampled.convert.javalayer;


import	java.io.InputStream;
import	java.io.IOException;

import	java.util.Arrays;

import	javax.sound.sampled.AudioFormat;
import	javax.sound.sampled.AudioInputStream;
import	javax.sound.sampled.AudioSystem;

import	org.tritonus.share.TDebug;
import	org.tritonus.share.sampled.Encodings;
import	org.tritonus.share.sampled.TConversionTool;
import	org.tritonus.share.sampled.convert.TMatrixFormatConversionProvider;
import	org.tritonus.share.sampled.convert.TAsynchronousFilteredAudioInputStream;

import	javazoom.jl.decoder.Bitstream;
import	javazoom.jl.decoder.BitstreamException;
import	javazoom.jl.decoder.Decoder;
import	javazoom.jl.decoder.DecoderException;
import	javazoom.jl.decoder.Header;
import	javazoom.jl.decoder.Obuffer;

/**
 * ConversionProvider for MPEG files.
 *
 * @author Matthias Pfisterer
 */


public class MpegFormatConversionProvider
	extends		TMatrixFormatConversionProvider
{

	public static final AudioFormat.Encoding	MPEG1L1 = Encodings.getEncoding("MPEG1L1");
	public static final AudioFormat.Encoding	MPEG1L2 = Encodings.getEncoding("MPEG1L2");
	public static final AudioFormat.Encoding	MPEG1L3 = Encodings.getEncoding("MPEG1L3");
	public static final AudioFormat.Encoding	MPEG2L1 = Encodings.getEncoding("MPEG2L1");
	public static final AudioFormat.Encoding	MPEG2L2 = Encodings.getEncoding("MPEG2L2");
	public static final AudioFormat.Encoding	MPEG2L3 = Encodings.getEncoding("MPEG2L3");
	public static final AudioFormat.Encoding	MPEG2DOT5L1 = Encodings.getEncoding("MPEG2DOT5L1");
	public static final AudioFormat.Encoding	MPEG2DOT5L2 = Encodings.getEncoding("MPEG2DOT5L2");
	public static final AudioFormat.Encoding	MPEG2DOT5L3 = Encodings.getEncoding("MPEG2DOT5L3");

/*
  private static final AudioFormat.Encoding[]	INPUT_ENCODINGS =
  {
  MPEG1L1, MPEG1L2, MPEG1L3,
  MPEG2L1, MPEG2L2, MPEG2L3,
  MPEG2DOT5L1, MPEG2DOT5L2, MPEG2DOT5L3
  };
  private static final AudioFormat.Encoding[]	OUTPUT_ENCODINGS =
  {
  AudioFormat.Encoding.PCM_SIGNED
  };
*/

	// TODO: frame size, frame rate, sample size, endianess?
	private static final AudioFormat[]	INPUT_FORMATS =
	{
		new AudioFormat(MPEG1L1, 32000.0F, -1, 1, -1, -1, false),	// 0
		new AudioFormat(MPEG1L1, 32000.0F, -1, 2, -1, -1, false),	// 1
		new AudioFormat(MPEG1L1, 44100.0F, -1, 1, -1, -1, false),	// 2
		new AudioFormat(MPEG1L1, 44100.0F, -1, 2, -1, -1, false),	// 3
		new AudioFormat(MPEG1L1, 48000.0F, -1, 1, -1, -1, false),	// 4
		new AudioFormat(MPEG1L1, 48000.0F, -1, 2, -1, -1, false),	// 5

		new AudioFormat(MPEG1L2, 32000.0F, -1, 1, -1, -1, false),	// 6
		new AudioFormat(MPEG1L2, 32000.0F, -1, 2, -1, -1, false),	// 7
		new AudioFormat(MPEG1L2, 44100.0F, -1, 1, -1, -1, false),	// 8
		new AudioFormat(MPEG1L2, 44100.0F, -1, 2, -1, -1, false),	// 9
		new AudioFormat(MPEG1L2, 48000.0F, -1, 1, -1, -1, false),	// 10
		new AudioFormat(MPEG1L2, 48000.0F, -1, 2, -1, -1, false),	// 11

		new AudioFormat(MPEG1L3, 32000.0F, -1, 1, -1, -1, false),	// 12
		new AudioFormat(MPEG1L3, 32000.0F, -1, 2, -1, -1, false),	// 13
		new AudioFormat(MPEG1L3, 44100.0F, -1, 1, -1, -1, false),	// 14
		new AudioFormat(MPEG1L3, 44100.0F, -1, 2, -1, -1, false),	// 15
		new AudioFormat(MPEG1L3, 48000.0F, -1, 1, -1, -1, false),	// 16
		new AudioFormat(MPEG1L3, 48000.0F, -1, 2, -1, -1, false),	// 17

		new AudioFormat(MPEG2L1, 16000.0F, -1, 1, -1, -1, false),	// 18
		new AudioFormat(MPEG2L1, 16000.0F, -1, 2, -1, -1, false),	// 19
		new AudioFormat(MPEG2L1, 22050.0F, -1, 1, -1, -1, false),	// 20
		new AudioFormat(MPEG2L1, 22050.0F, -1, 2, -1, -1, false),	// 21
		new AudioFormat(MPEG2L1, 24000.0F, -1, 1, -1, -1, false),	// 22
		new AudioFormat(MPEG2L1, 24000.0F, -1, 2, -1, -1, false),	// 23

		new AudioFormat(MPEG2L2, 16000.0F, -1, 1, -1, -1, false),	// 24
		new AudioFormat(MPEG2L2, 16000.0F, -1, 2, -1, -1, false),	// 25
		new AudioFormat(MPEG2L2, 22050.0F, -1, 1, -1, -1, false),	// 26
		new AudioFormat(MPEG2L2, 22050.0F, -1, 2, -1, -1, false),	// 27
		new AudioFormat(MPEG2L2, 24000.0F, -1, 1, -1, -1, false),	// 28
		new AudioFormat(MPEG2L2, 24000.0F, -1, 2, -1, -1, false),	// 29

		new AudioFormat(MPEG2L3, 16000.0F, -1, 1, -1, -1, false),	// 30
		new AudioFormat(MPEG2L3, 16000.0F, -1, 2, -1, -1, false),	// 31
		new AudioFormat(MPEG2L3, 22050.0F, -1, 1, -1, -1, false),	// 32
		new AudioFormat(MPEG2L3, 22050.0F, -1, 2, -1, -1, false),	// 33
		new AudioFormat(MPEG2L3, 24000.0F, -1, 1, -1, -1, false),	// 34
		new AudioFormat(MPEG2L3, 24000.0F, -1, 2, -1, -1, false),	// 35

		new AudioFormat(MPEG2DOT5L1, 8000.0F, -1, 1, -1, -1, false),	// 36
		new AudioFormat(MPEG2DOT5L1, 8000.0F, -1, 2, -1, -1, false),	// 37
		new AudioFormat(MPEG2DOT5L1, 11025.0F, -1, 1, -1, -1, false),	// 38
		new AudioFormat(MPEG2DOT5L1, 11025.0F, -1, 2, -1, -1, false),	// 39
		new AudioFormat(MPEG2DOT5L1, 12000.0F, -1, 1, -1, -1, false),	// 40
		new AudioFormat(MPEG2DOT5L1, 12000.0F, -1, 2, -1, -1, false),	// 41

		new AudioFormat(MPEG2DOT5L2, 8000.0F, -1, 1, -1, -1, false),	// 42
		new AudioFormat(MPEG2DOT5L2, 8000.0F, -1, 2, -1, -1, false),	// 43
		new AudioFormat(MPEG2DOT5L2, 11025.0F, -1, 1, -1, -1, false),	// 44
		new AudioFormat(MPEG2DOT5L2, 11025.0F, -1, 2, -1, -1, false),	// 45
		new AudioFormat(MPEG2DOT5L2, 12000.0F, -1, 1, -1, -1, false),	// 46
		new AudioFormat(MPEG2DOT5L2, 12000.0F, -1, 2, -1, -1, false),	// 47

		new AudioFormat(MPEG2DOT5L3, 8000.0F, -1, 1, -1, -1, false),	// 48
		new AudioFormat(MPEG2DOT5L3, 8000.0F, -1, 2, -1, -1, false),	// 49
		new AudioFormat(MPEG2DOT5L3, 11025.0F, -1, 1, -1, -1, false),	// 50
		new AudioFormat(MPEG2DOT5L3, 11025.0F, -1, 2, -1, -1, false),	// 51
		new AudioFormat(MPEG2DOT5L3, 12000.0F, -1, 1, -1, -1, false),	// 52
		new AudioFormat(MPEG2DOT5L3, 12000.0F, -1, 2, -1, -1, false),	// 53
	};


	private static final AudioFormat[]	OUTPUT_FORMATS =
	{
		new AudioFormat(8000.0F, 16, 1, true, false),	// 0
		new AudioFormat(8000.0F, 16, 1, true, true),	// 1
		new AudioFormat(8000.0F, 16, 2, true, false),	// 2
		new AudioFormat(8000.0F, 16, 2, true, true),	// 3
/*	24 and 32 bit not yet possible
	new AudioFormat(8000.0F, 24, 1, true, false),
	new AudioFormat(8000.0F, 24, 1, true, true),
	new AudioFormat(8000.0F, 24, 2, true, false),
	new AudioFormat(8000.0F, 24, 2, true, true),
	new AudioFormat(8000.0F, 32, 1, true, false),
	new AudioFormat(8000.0F, 32, 1, true, true),
	new AudioFormat(8000.0F, 32, 2, true, false),
	new AudioFormat(8000.0F, 32, 2, true, true),
*/
		new AudioFormat(11025.0F, 16, 1, true, false),	// 4
		new AudioFormat(11025.0F, 16, 1, true, true),	// 5
		new AudioFormat(11025.0F, 16, 2, true, false),	// 6
		new AudioFormat(11025.0F, 16, 2, true, true),	// 7
/*	24 and 32 bit not yet possible
	new AudioFormat(11025.0F, 24, 1, true, false),
	new AudioFormat(11025.0F, 24, 1, true, true),
	new AudioFormat(11025.0F, 24, 2, true, false),
	new AudioFormat(11025.0F, 24, 2, true, true),
	new AudioFormat(11025.0F, 32, 1, true, false),
	new AudioFormat(11025.0F, 32, 1, true, true),
	new AudioFormat(11025.0F, 32, 2, true, false),
	new AudioFormat(11025.0F, 32, 2, true, true),
*/
		new AudioFormat(12000.0F, 16, 1, true, false),	// 8
		new AudioFormat(12000.0F, 16, 1, true, true),	// 9
		new AudioFormat(12000.0F, 16, 2, true, false),	// 10
		new AudioFormat(12000.0F, 16, 2, true, true),	// 11
/*	24 and 32 bit not yet possible
	new AudioFormat(12000.0F, 24, 1, true, false),
	new AudioFormat(12000.0F, 24, 1, true, true),
	new AudioFormat(12000.0F, 24, 2, true, false),
	new AudioFormat(12000.0F, 24, 2, true, true),
	new AudioFormat(12000.0F, 32, 1, true, false),
	new AudioFormat(12000.0F, 32, 1, true, true),
	new AudioFormat(12000.0F, 32, 2, true, false),
	new AudioFormat(12000.0F, 32, 2, true, true),
*/
		new AudioFormat(16000.0F, 16, 1, true, false),	// 12
		new AudioFormat(16000.0F, 16, 1, true, true),	// 13
		new AudioFormat(16000.0F, 16, 2, true, false),	// 14
		new AudioFormat(16000.0F, 16, 2, true, true),	// 15
/*	24 and 32 bit not yet possible
	new AudioFormat(16000.0F, 24, 1, true, false),
	new AudioFormat(16000.0F, 24, 1, true, true),
	new AudioFormat(16000.0F, 24, 2, true, false),
	new AudioFormat(16000.0F, 24, 2, true, true),
	new AudioFormat(16000.0F, 32, 1, true, false),
	new AudioFormat(16000.0F, 32, 1, true, true),
	new AudioFormat(16000.0F, 32, 2, true, false),
	new AudioFormat(16000.0F, 32, 2, true, true),
*/
		new AudioFormat(22050.0F, 16, 1, true, false),	// 16
		new AudioFormat(22050.0F, 16, 1, true, true),	// 17
		new AudioFormat(22050.0F, 16, 2, true, false),	// 18
		new AudioFormat(22050.0F, 16, 2, true, true),	// 19
/*	24 and 32 bit not yet possible
	new AudioFormat(22050.0F, 24, 1, true, false),
	new AudioFormat(22050.0F, 24, 1, true, true),
	new AudioFormat(22050.0F, 24, 2, true, false),
	new AudioFormat(22050.0F, 24, 2, true, true),
	new AudioFormat(22050.0F, 32, 1, true, false),
	new AudioFormat(22050.0F, 32, 1, true, true),
	new AudioFormat(22050.0F, 32, 2, true, false),
	new AudioFormat(22050.0F, 32, 2, true, true),
*/
		new AudioFormat(24000.0F, 16, 1, true, false),	// 20
		new AudioFormat(24000.0F, 16, 1, true, true),	// 21
		new AudioFormat(24000.0F, 16, 2, true, false),	// 22
		new AudioFormat(24000.0F, 16, 2, true, true),	// 23
/*	24 and 32 bit not yet possible
	new AudioFormat(24000.0F, 24, 1, true, false),
	new AudioFormat(24000.0F, 24, 1, true, true),
	new AudioFormat(24000.0F, 24, 2, true, false),
	new AudioFormat(24000.0F, 24, 2, true, true),
	new AudioFormat(24000.0F, 32, 1, true, false),
	new AudioFormat(24000.0F, 32, 1, true, true),
	new AudioFormat(24000.0F, 32, 2, true, false),
	new AudioFormat(24000.0F, 32, 2, true, true),
*/
		new AudioFormat(32000.0F, 16, 1, true, false),	// 24
		new AudioFormat(32000.0F, 16, 1, true, true),	// 25
		new AudioFormat(32000.0F, 16, 2, true, false),	// 26
		new AudioFormat(32000.0F, 16, 2, true, true),	// 27
/*	24 and 32 bit not yet possible
	new AudioFormat(32000.0F, 24, 1, true, false),
	new AudioFormat(32000.0F, 24, 1, true, true),
	new AudioFormat(32000.0F, 24, 2, true, false),
	new AudioFormat(32000.0F, 24, 2, true, true),
	new AudioFormat(32000.0F, 32, 1, true, false),
	new AudioFormat(32000.0F, 32, 1, true, true),
	new AudioFormat(32000.0F, 32, 2, true, false),
	new AudioFormat(32000.0F, 32, 2, true, true),
*/
		new AudioFormat(44100.0F, 16, 1, true, false),	// 28
		new AudioFormat(44100.0F, 16, 1, true, true),	// 29
		new AudioFormat(44100.0F, 16, 2, true, false),	// 30
		new AudioFormat(44100.0F, 16, 2, true, true),	// 31
/*	24 and 32 bit not yet possible
	new AudioFormat(44100.0F, 24, 1, true, false),
	new AudioFormat(44100.0F, 24, 1, true, true),
	new AudioFormat(44100.0F, 24, 2, true, false),
	new AudioFormat(44100.0F, 24, 2, true, true),
	new AudioFormat(44100.0F, 32, 1, true, false),
	new AudioFormat(44100.0F, 32, 1, true, true),
	new AudioFormat(44100.0F, 32, 2, true, false),
	new AudioFormat(44100.0F, 32, 2, true, true),
*/
		new AudioFormat(48000.0F, 16, 1, true, false),	// 32
		new AudioFormat(48000.0F, 16, 1, true, true),	// 33
		new AudioFormat(48000.0F, 16, 2, true, false),	// 34
		new AudioFormat(48000.0F, 16, 2, true, true),	// 35
/*	24 and 32 bit not yet possible
	new AudioFormat(48000.0F, 24, 1, true, false),
	new AudioFormat(48000.0F, 24, 1, true, true),
	new AudioFormat(48000.0F, 24, 2, true, false),
	new AudioFormat(48000.0F, 24, 2, true, true),
	new AudioFormat(48000.0F, 32, 1, true, false),
	new AudioFormat(48000.0F, 32, 1, true, true),
	new AudioFormat(48000.0F, 32, 2, true, false),
	new AudioFormat(48000.0F, 32, 2, true, true),
*/
	};


	private static final boolean	t = true;
	private static final boolean	f = false;

	/*
	 *	One row for each source format.
	 */
	private static final boolean[][]	CONVERSIONS =
	{
		{f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,t,t,f,f,f,f, f,f,f,f,f,f},	// 0
		{f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,t,t,f,f, f,f,f,f,f,f},	// 1
		{f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,t,t, f,f,f,f,f,f},	// 2
		{f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, t,t,f,f,f,f},	// 3
		{f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,t,t,f,f},	// 4
		{f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,t,t},	// 5

		{f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,t,t,f,f,f,f, f,f,f,f,f,f},	// 6
		{f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,t,t,f,f, f,f,f,f,f,f},	// 7
		{f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,t,t, f,f,f,f,f,f},	// 8
		{f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, t,t,f,f,f,f},	// 9
		{f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,t,t,f,f},	// 10
		{f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,t,t},	// 11

		{f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,t,t,f,f,f,f, f,f,f,f,f,f},	// 12
		{f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,t,t,f,f, f,f,f,f,f,f},	// 13
		{f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,t,t, f,f,f,f,f,f},	// 14
		{f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, t,t,f,f,f,f},	// 15
		{f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,t,t,f,f},	// 16
		{f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,t,t},	// 17

		{f,f,f,f,f,f,f,f,f,f, f,f,t,t,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 18
		{f,f,f,f,f,f,f,f,f,f, f,f,f,f,t,t,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 19 
		{f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,t,t,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 20
		{f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,t,t, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 21
		{f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, t,t,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 22
		{f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,t,t,f,f,f,f,f,f, f,f,f,f,f,f},	// 23

		{f,f,f,f,f,f,f,f,f,f, f,f,t,t,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 24
		{f,f,f,f,f,f,f,f,f,f, f,f,f,f,t,t,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 25
		{f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,t,t,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 26
		{f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,t,t, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 27
		{f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, t,t,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 28
		{f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,t,t,f,f,f,f,f,f, f,f,f,f,f,f},	// 29

		{f,f,f,f,f,f,f,f,f,f, f,f,t,t,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 30
		{f,f,f,f,f,f,f,f,f,f, f,f,f,f,t,t,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 31
		{f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,t,t,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 32
		{f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,t,t, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 33
		{f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, t,t,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 34
		{f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,t,t,f,f,f,f,f,f, f,f,f,f,f,f},	// 35

		{t,t,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 36
		{f,f,t,t,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 37
		{f,f,f,f,t,t,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 38
		{f,f,f,f,f,f,t,t,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 39
		{f,f,f,f,f,f,f,f,t,t, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 40
		{f,f,f,f,f,f,f,f,f,f, t,t,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 41

		{t,t,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 42
		{f,f,t,t,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 43
		{f,f,f,f,t,t,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 44
		{f,f,f,f,f,f,t,t,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 45
		{f,f,f,f,f,f,f,f,t,t, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 46
		{f,f,f,f,f,f,f,f,f,f, t,t,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 47

		{t,t,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 48
		{f,f,t,t,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 49
		{f,f,f,f,t,t,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 50
		{f,f,f,f,f,f,t,t,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 51
		{f,f,f,f,f,f,f,f,t,t, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 52
		{f,f,f,f,f,f,f,f,f,f, t,t,f,f,f,f,f,f,f,f, f,f,f,f,f,f,f,f,f,f, f,f,f,f,f,f},	// 53

	};



	/**	Constructor.
	 */
	public MpegFormatConversionProvider()
	{
		super(Arrays.asList(INPUT_FORMATS),
		      Arrays.asList(OUTPUT_FORMATS),
		      CONVERSIONS);
		if (TDebug.TraceAudioConverter) { TDebug.out("MpegFormatConversionProvider.<init>(): begin"); }
		if (TDebug.TraceAudioConverter) { TDebug.out("MpegFormatConversionProvider.<init>(): end"); }
	}



	public AudioInputStream getAudioInputStream(AudioFormat targetFormat, AudioInputStream audioInputStream)
	{
		if (TDebug.TraceAudioConverter)
		{
			TDebug.out("MpegFormatConversionProvider.getAudioInputStream(AudioFormat, AudioInputStream):");
			TDebug.out("trying to convert");
			TDebug.out("\tfrom: " + audioInputStream.getFormat());
			TDebug.out("\tto: " + targetFormat);
		}
		AudioFormat	matchingFormat = getMatchingFormat(
			targetFormat,
			audioInputStream.getFormat());
// 		if (isConversionSupported(targetFormat,
// 					  audioInputStream.getFormat()))
		if (matchingFormat != null)
		{
			if (TDebug.TraceAudioConverter)
			{
				TDebug.out("MpegFormatConversionProvider.getAudioInputStream(AudioFormat, AudioInputStream):");
				TDebug.out("\tisConversionSupported() accepted it; now setting up the conversion");
			}
			targetFormat = setUnspecifiedFieldsFromProto(targetFormat, matchingFormat);
			if (TDebug.TraceAudioConverter)
			{
				TDebug.out("MpegFormatConversionProvider.getAudioInputStream(AudioFormat, AudioInputStream):");
				TDebug.out("\tcompleted target format (1. stage): " + targetFormat);
			}
			targetFormat = setUnspecifiedFieldsFromProto(targetFormat, audioInputStream.getFormat());
			if (TDebug.TraceAudioConverter)
			{
				TDebug.out("MpegFormatConversionProvider.getAudioInputStream(AudioFormat, AudioInputStream):");
				TDebug.out("\tcompleted target format (2. stage): " + targetFormat);
			}
			return new DecodedMpegAudioInputStream(
				targetFormat,
				audioInputStream);
		}
		else
		{
			throw new IllegalArgumentException("conversion not supported");
		}
	}


	// TODO: ask Florian if these methods are of general interest
	private static AudioFormat setUnspecifiedFieldsFromProto(
		AudioFormat incomplete,
		AudioFormat prototype)
	{
		AudioFormat	format = new AudioFormat(
			incomplete.getEncoding(),
			getSpecificValue(incomplete.getSampleRate(), prototype.getSampleRate()),
			getSpecificValue(incomplete.getSampleSizeInBits(), prototype.getSampleSizeInBits()),
			getSpecificValue(incomplete.getChannels(), prototype.getChannels()),
			getSpecificValue(incomplete.getFrameSize(), prototype.getFrameSize()),
			getSpecificValue(incomplete.getFrameRate(), prototype.getFrameRate()),
			incomplete.isBigEndian());
		return format;
	}



	private static float getSpecificValue(float fIncomplete, float fProto)
	{
		return (fIncomplete == AudioSystem.NOT_SPECIFIED) ? fProto : fIncomplete;
	}


	private static int getSpecificValue(int nIncomplete, int nProto)
	{
		return (nIncomplete == AudioSystem.NOT_SPECIFIED) ? nProto : nIncomplete;
	}



	public static class DecodedMpegAudioInputStream
	extends		TAsynchronousFilteredAudioInputStream
	{
		private InputStream		m_encodedStream;
		private Bitstream		m_bitstream;
		private Decoder			m_decoder;
		private DMAISObuffer		m_oBuffer;



		public DecodedMpegAudioInputStream(AudioFormat outputFormat, AudioInputStream inputStream)
		{
			// TODO: try to find out length (possible?)
			super(outputFormat, AudioSystem.NOT_SPECIFIED);
			m_encodedStream = inputStream;
			m_bitstream = new Bitstream(inputStream);
			m_decoder = new Decoder(null);
			m_oBuffer = new DMAISObuffer(outputFormat.getChannels());
			m_decoder.setOutputBuffer(m_oBuffer);
		}



		public void execute()
		{
			try
			{
				Header	header = m_bitstream.readFrame();
				if (header == null)
				{
					if (TDebug.TraceAudioConverter)
					{
						TDebug.out("header is null (end of mpeg stream)");
					}
					getCircularBuffer().close();
					return;
				}
				Obuffer	decoderOutput = m_decoder.decodeFrame(header, m_bitstream);
				m_bitstream.closeFrame();
				getCircularBuffer().write(m_oBuffer.getBuffer(), 0, m_oBuffer.getCurrentBufferSize());
				m_oBuffer.reset();
			}
			catch (BitstreamException e)
			{
				if (TDebug.TraceAudioConverter || TDebug.TraceAllExceptions)
				{
					TDebug.out(e);
				}
			}
			catch (DecoderException e)
			{
				if (TDebug.TraceAudioConverter || TDebug.TraceAllExceptions)
				{
					TDebug.out(e);
				}
			}
		}



		private boolean isBigEndian()
		{
			return getFormat().isBigEndian();
		}



		public void close()
			throws	IOException
		{
			super.close();
			m_encodedStream.close();
		}



		private class DMAISObuffer
		extends		Obuffer
		{
			private int			m_nChannels;
			private byte[]			m_abBuffer;
			private int[]			m_anBufferPointers;
			private boolean			m_bIsBigEndian;



			public DMAISObuffer(int	nChannels)
			{
				m_nChannels = nChannels;
				m_abBuffer = new byte[OBUFFERSIZE * nChannels];
				m_anBufferPointers = new int[nChannels];
				reset();
				m_bIsBigEndian = DecodedMpegAudioInputStream.this.isBigEndian();
			}



			public void append(int nChannel, short sValue)
			{
				// TODO: replace by TConversionTool methods
/*
				byte	bFirstByte;
				byte	bSecondByte;
				if (m_bIsBigEndian)
				{
					bFirstByte = (byte) ((sValue >>> 8) & 0xFF);
					bSecondByte = (byte) (sValue & 0xFF);
				}
				else	// little endian
				{
					bFirstByte = (byte) (sValue & 0xFF);
					bSecondByte = (byte) ((sValue >>> 8) & 0xFF);
				}
				m_abBuffer[m_anBufferPointers[nChannel]] = bFirstByte;
				m_abBuffer[m_anBufferPointers[nChannel] + 1] = bSecondByte;
*/
				TConversionTool.shortToBytes16(sValue, m_abBuffer, m_anBufferPointers[nChannel], m_bIsBigEndian);
				m_anBufferPointers[nChannel] += m_nChannels * 2;
			}


			public void set_stop_flag()
			{
			}



			public void close()
			{
			}



			public void write_buffer(int nValue)
			{
			}



			public void clear_buffer()
			{
			}



			public byte[] getBuffer()
			{
				return m_abBuffer;
			}



			public int getCurrentBufferSize()
			{
				return m_anBufferPointers[0];
			}



			public void reset()
			{
				for (int i = 0; i < m_nChannels; i++)
				{
					/*	Points to byte location,
					 *	implicitely assuming 16 bit
					 *	samples.
					 */
					m_anBufferPointers[i] = i * 2;
				}
			}


			
		}
	}
}



/*** MpegFormatConversionProvider.java ***/
