package binary;

/*
	Zip++ application
	Copyright 2000, 2003 Zip-Technology.com

	@Author Ronan Merien <ronan.merien@zip-technology.com>
	@Version 1.2, 2003/10/15
*/

import java.io.*;

/**
	<p>A binary output stream let an application write binary data types
	(as a byte or a group of bits) from an underlying output stream.</p>
*/

public class BinaryOutputStream extends FilterOutputStream
{

	private byte currentValue;
	private byte reference;
	private int bits = 0;

	private boolean statistics = false;
	public int[] tabStatistics_1b = null;
	public int[] tabStatistics_8b = null;

	public BinaryOutputStream(OutputStream out)
	{
		super(out);
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
		Write a single binary digit (bit) into the output stream.
	*/
	public void writeBit(byte b) throws IOException
	{
		if (b == 0x01) {
			currentValue = (byte) ( currentValue + (0x01 << reference) );
		}

		if (reference==7) {

			if (statistics) {
				int p = -1;

				for (int i=0; i<8; i++) {
					p = ((int)currentValue >> i) & 0x01;
					tabStatistics_1b[p]++;
				}

				p = ((int)currentValue) & 0xFF;
				tabStatistics_8b[p]++;
			}

			out.write(currentValue);
			currentValue = 0x00;
			reference = 0;
		}
		else reference++;

		bits++;
	}

	/**
		Write a group of bits as a unsigned integer into the output stream.
	*/
	public void writeBit(int p, int size) throws IOException
	{
		byte b = 0x00;
		try {
			for (int i=0; i<size; i++) {
				b = (byte) ((p >> i) & 0x01);
				writeBit(b);
			}
		}
		catch (IOException e) {
			throw new IOException();
		}
	}

	/**
		Write a byte as an end of file into the output stream.
	*/
	public void writeEOF() throws IOException
	{
		if (reference != 0) {
			try {
				writeBit(0, 8-reference);
			}
			catch (IOException e) {
				throw new IOException();
			}
		}
	}

	/**
		Write a byte as a unsigned 8 bits integer into the output stream.
	*/
	public void writeByte(byte b) throws IOException
	{
		if (reference == 0) {

			if (statistics) {
				int p = -1;

				for (int i=0; i<8; i++) {
					p = ((int)b >> i) & 0x01;
					tabStatistics_1b[p]++;
				}

				p = ((int)b) & 0xFF;
				tabStatistics_8b[p]++;
			}

			out.write(b);
			bits = bits + 8;
		}
		else throw new IOException();
	}

	/**
		<p>Flushes this data output stream.</p>
		<p>This forces any buffered output bytes to be written out to the stream.</p>
	*/
	public void flush() throws IOException {
		out.flush();
	}

	/**
		Returns the number of bits written to this data output stream.
	*/
	public int getBits() {
		return bits;
	}

	/**
		Returns the number of bytes written to this data output stream.
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
		Generating statistics on this data output stream.
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