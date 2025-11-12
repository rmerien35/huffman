package binary;

/*
	Zip++ application
	Copyright 2000, 2003 Zip-Technology.com

	@Author Ronan Merien <ronan.merien@zip-technology.com>
	@Version 1.2, 2003/10/15
*/

import java.io.*;

/**
	<p>A binary input stream let an application read binary data types
	(as a byte or a group of bits) from an underlying input stream.</p>
*/

public class BinaryInputStream extends FilterInputStream {

	private int currentValue;
	private byte reference;

	private int bits = 0;

	private boolean statistics = false;
	public int[] tabStatistics_1b = null;
	public int[] tabStatistics_8b = null;

	private int ch = -1;

	public BinaryInputStream(InputStream in)
	{
		super(in);
		currentValue = 0x00;
		reference = 0;
		bits = 0;

		if (statistics) {
			tabStatistics_1b = new int[2];
			tabStatistics_1b[0] = 0;
			tabStatistics_1b[1] = 0;

			tabStatistics_8b = new int[256];
			for (int i=0; i<256; i++) tabStatistics_8b[i] =0;
		}
	}

	/**
		Read a single binary digit (bit) from the input stream.
	*/
	public byte readBit() throws IOException
	{
		if (reference==0) {
			ch = in.read();
			if (ch < 0) throw new EOFException();
			currentValue = ch;
		}

		byte b = (byte) ( (currentValue >> reference) & 0x01 );

		if (reference==7) {
			/*
			if (statistics) {
				int p = -1;

				for (int i=0; i<8; i++) {
					p = ((int)currentValue >> i) & 0x01;
					tabStatistics_1b[p]++;
				}

				p = ((int)currentValue) & 0xFF;
				tabStatistics_8b[p]++;
			}
			*/

			// currentValue = 0x00;
			reference = 0;
		}
		else reference++;

		bits++;
		return b;
	}

	/**
		Read a group of bits as an unsigned integer from the input stream.
	*/
	public int readBit(int size) throws IOException
	{
		int p = 0x00;
		try {
			for (int i=0; i<size; i++) {
				p = (((int) readBit()) << i) | p;
			}
			return p;
		}
		catch (IOException e) {
			throw new IOException();
		}
	}

	/**
		Read a byte as a signed 8 bits integer (-128 to 127) from the input stream.
	*/
	public byte readByte() throws IOException
	{
		if (reference == 0) {
			ch = in.read();
			if (ch < 0) throw new EOFException();
			byte b = (byte) ch;

			if (statistics) {
				int p = -1;

				for (int i=0; i<8; i++) {
					p = ((int)b >> i) & 0x01;
					tabStatistics_1b[p]++;
				}

				p = ((int)b) & 0xFF;
				tabStatistics_8b[p]++;
			}

		bits = bits + 8;
		return b;
		}
		else throw new IOException();
	}

	/**
		Returns the number of bits read from this data input stream.
	*/
	public int getBits() {
		return bits;
	}

	/**
		Returns the number of bytes read from this data input stream.
	*/
	public int size() {
		return (bits/8);
	}

	// --------------------------------------------------------------

	public void setStatistics(boolean s) {
		statistics = s;
	}

	public boolean isStatistics() {
		return statistics;
	}

	/**
		Generating statistics on this data input stream.
	*/
	public void genStatistics()
	{
		if (statistics && (getBits() > 0)) {
			System.out.println("Generating statistics ...");

			System.out.println("Nb bits -> " + getBits());
			System.out.println("0 -> " + tabStatistics_1b[0]);
			System.out.println("1 -> " + tabStatistics_1b[1]);

			System.out.println("Nb bytes -> " + size());
			for (int i=0; i<256; i++) {
				System.out.println(Binary.toHexaString((byte)i) + " -> " + tabStatistics_8b[i]);
			}
		}
	}

}