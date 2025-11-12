package binary;

/*
	Zip++ application
	Copyright 2000, 2003 Zip-Technology.com

	@Author Ronan Merien <ronan.merien@zip-technology.com>
	@Version 1.2, 2003/10/15
*/

import java.util.*;

/**
	<p>This class provide static methods to print a byte
	or a group of bits under differents formats.</p>
*/

public class Binary {

	private static final int DUMP_LINE_SIZE = 16;

	// ----------------------------------------------------------------------------
	/**
		This method print a byte as a boolean value.
	*/
	static public String toSingleBinaryString(byte b)
	{
		String str = "";

		if ((b == 0x00)) str = "0";
		else             str = "1";

		return str;
	}

	// ----------------------------------------------------------------------------
	/**
		This method print a byte as a binary number.
	*/
	static public String toBinaryString(byte b)
	{
		String str = "";
		int p = 0x00;
		/*
		[0] =  ((b&0x01) == 1);
		[1] =  ((b&0x02) == 2);
		[2] =  ((b&0x04) == 4);
		[3] =  ((b&0x08) == 8);
		[4] =  ((b&0x0F) == 16);
		[5] =  ((b&0x20) == 32);
		[6] =  ((b&0x40) == 64);
		[7] =  ((b&0x80) == 128);
		*/
		for (int i=0; i<8; i++) {
			p = ((int)b >> i) & 0x01;
			str = String.valueOf(p) + str;
		}
		return str;
	}

	/**
		This method print an int as a binary number.
	*/
	static public String toBinaryString(int b, int size)
	{
		String str = "";
		int p = 0x00;

		for (int i=0; i<size; i++) {
			p = (b >> i) & 0x01;
			str = String.valueOf(p) + str;
		}
		return str;
	}

	// ----------------------------------------------------------------------------
	/**
		This method print a byte as an unsigned 8 bits integer (0 to 255).
	*/
	static public String toDecimalString(byte b)
	{
		int i = ((int)b) & 0xFF;

		return String.valueOf(i);
	}

	// ----------------------------------------------------------------------------
	/**
		This method print a byte as an hexadecimal number (00 to FF).
	*/
	static public String toHexaString(byte b)
	{
		int i = ((int)b) & 0xFF;

		if (i < 0x10)	return("0" + (Integer.toHexString(i)).toUpperCase());
		else 			return((Integer.toHexString(i)).toUpperCase());
	}

	// ----------------------------------------------------------------------------

	/**
		This method dump into a buffer the contain of a byte array.
	*/

	static public void dumpHex (byte[] data)
	{
		int curLg=data.length;
		int i,j=0;
		byte[] toAsciiArray = new byte[DUMP_LINE_SIZE];

		StringBuffer buffer = new StringBuffer(26+(4*DUMP_LINE_SIZE));
		buffer.append("                        ");

		while (curLg > DUMP_LINE_SIZE) {
			for (i=0; i<DUMP_LINE_SIZE; i++) {
				if ((data[j] < (byte)0x20) || (data[j] > (byte)0x7F))
					toAsciiArray[i] = (byte)0x2E;
				else
					toAsciiArray[i] = data[j];

				buffer.append(toHexaString(data[j++])+" ");
			}

			buffer.append("  ");
			buffer.append(new String(toAsciiArray));
			System.out.println(buffer);
			buffer.setLength(24);
			curLg -= DUMP_LINE_SIZE;
		}

		// last line
		for (i=0; i<curLg; i++) {
			if ((data[j] < (byte)0x20) || (data[j] > (byte)0x7F))
				toAsciiArray[i] = (byte)0x2E;
			else
				toAsciiArray[i] = data[j];

			buffer.append(toHexaString(data[j++])+" ");
		}

		for (; i<DUMP_LINE_SIZE; i++) {
			toAsciiArray[i] = (byte)0x20;
			buffer.append("   ");
		}

		buffer.append("  ");
		buffer.append(new String(toAsciiArray));
		System.out.println(buffer);
	}

	// ----------------------------------------------------------------------------

	static public String toString(byte b)
	{
		return String.valueOf((char) b);
	}
}