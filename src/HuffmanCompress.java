
/*
	HuffmanCompress
	Copyright 2003 Zip-Technology.com

	@Author Ronan Merien <ronan.merien@zip-technology.com>
*/

import java.io.*;
import java.util.*;
import binary.*;
import java.util.zip.*;
import zpp.*;

/**
	<p>This class compress an input file.</p>

	<p>The compression used the Huffman coding algorithm.</p>
*/
public class HuffmanCompress {

	String logFile = "log.txt";

	int code_ascii;

	static int maxBits = 20;
	int[] count = new int[maxBits];
	int[] next_code = new int[maxBits];

	BinaryTree[] tabStatistics_len;

	static int len_max = 256 + 1;

	BinaryInputStream bis;
	BinaryOutputStream bos;

	// this vector will store the compress code generated during the pass
	Vector gen;

	public HuffmanCompress (String inFile, OutputStream os)
	{
		try {
			if (logFile != null) redirectOutput();

			// first pass : get statistics from input stream

			tabStatistics_len = new BinaryTree[len_max];
			for (int i=0; i<len_max; i++) {
				tabStatistics_len[i] = new BinaryTree(i,0);
			}

			for (int i=0; i < maxBits; i++) {
				count[i] = 0;
				next_code[i] = 0;
			}


			// ----------------------------------------------------------------------------------------------------------

			FileInputStream fis = new FileInputStream(inFile);
			bis = new BinaryInputStream(new BufferedInputStream(fis));

			gen = new Vector(fis.available(), 100000);

			try {
				// until the end of the input file
				while (true) {
					code_ascii = readNext();
					System.out.println((char) (code_ascii - 128) + "," + code_ascii);
					tabStatistics_len[code_ascii].frequence ++;
					gen.addElement(new MatchLength(code_ascii));
				}
			}
			catch (EOFException e) {
				System.out.println(e);
			}

			tabStatistics_len[256].frequence ++;

			bis.close();


			System.out.println("-----------------------------------------------");
			for (int i=0; i<len_max; i++) {
				System.out.println(Binary.toHexaString((byte) i)
					+ " -> "
					+ tabStatistics_len[i].frequence);
			}

			// build the vector
			Vector list = new Vector();
			for (int i=0; i<len_max; i++) {
				if  (tabStatistics_len[i].frequence > 0) {
					list.addElement(tabStatistics_len[i]);
				}
			}
			sort(list,0,list.size()-1);

			System.out.println("-----------------------------------------------");

			// build the huffman tree

			for (int i=0; i < maxBits; i++) {
				count[i] = 0;
				next_code[i] = 0;
			}

			int size = list.size();

			BinaryTree node_0 = null;
			BinaryTree node_1 = null;
			BinaryTree tree = null;

			for (int k=0; k<size-1; k++) {

				node_0  = (BinaryTree) list.elementAt(0);
				node_1  = (BinaryTree) list.elementAt(1);

				int sum = node_0.frequence + node_1.frequence;

				tree = new BinaryTree(-1, sum, node_0, node_1);
				// tree.toString();

				list.removeElementAt(0);
				list.removeElementAt(0);

				list.insertElementAt(tree, 0);
				sort(list,0,list.size()-1);
			}

			convert_tree_to_code(tree,0);

			// System.out.println("-----------------------------------------------");

			int compressCode = 0;
			count[0] = 0;
			for (int bits = 1; bits < maxBits; bits++) {
				compressCode = (compressCode + count[bits-1]) << 1;
				next_code[bits] = compressCode;
			}

			BinaryTree node = null;
			for (int i=0; i<len_max; i++) {
				node = tabStatistics_len[i];

				if (node != null) {
					int len = node.nbBits;
					if (len != 0) {
						node.compressCode = reverse(next_code[len],len);
						next_code[len]++;
					}
				}
			}

			print(tabStatistics_len);

			bos = new BinaryOutputStream(new BufferedOutputStream(os));

			boolean mode_rle = false;
			int compteur_rle = 0;

			// second pass : generating huffman codes

			// next, write huffman statistics for length into the output file
			for (int i=0; i<len_max; i++) {
				if (tabStatistics_len[i].frequence>0) {
					if (mode_rle) {
						bos.writeBit(0x00,4);
						bos.writeBit(compteur_rle-1,8); // Pb if more than 256 null codes (?)
						// System.out.println(0 + "," + compteur_rle);
						mode_rle = false;
						compteur_rle = 0;
					}

					BinaryTree element = tabStatistics_len[i];
					if (element.nbBits < 15) {
						bos.writeBit(element.nbBits,4);
					}
					else if ((element.nbBits >= 15) && (element.nbBits < 18)) {
						bos.writeBit(15, 4);
						bos.writeBit(element.nbBits-15,2); // mode (0-> 15, 1-> 16, 2-> 17, 3-> 18+ bits)
					}
					else if (element.nbBits < maxBits) {
						bos.writeBit(15, 4);
						bos.writeBit(3, 2);
						bos.writeBit(element.nbBits-18,2); // mode (0-> 18, 1-> 19 bits)
					}
					// System.out.println(element.nbBits);
				}
				else {
					mode_rle = true;
					compteur_rle ++;
				}
			}

			if (mode_rle) {
				bos.writeBit(0x00,4);
				bos.writeBit(compteur_rle-1,8);
				// System.out.println(0 + "," + compteur_rle);
				mode_rle = false;
				compteur_rle = 0;
			}

			MatchLength len = null;
			BinaryTree node_len = null;

			// next, write contains of the "gen" vector into the output file
			for (Enumeration e = gen.elements(); e.hasMoreElements() ;) {
				len = (MatchLength) e.nextElement();
				// System.out.println("length = " + len.value);

				node_len = tabStatistics_len[len.value];
				bos.writeBit(node_len.compressCode, node_len.nbBits);
			}

			// writing a code for end of file
			node_len = tabStatistics_len[256];
			bos.writeBit(node_len.compressCode, node_len.nbBits);

			bos.writeEOF();

			bos.flush();
		 }
		 catch (Exception e) {
			 System.out.println("HuffmanCompress " + e);
		 }
	}


	/**
		<p>Reading the next character from the input file.</p>
	*/
	// ---------------------------------------------------------------------------------------------
	private int readNext() throws EOFException {
		try {
			return (bis.readByte() + 128);

		}
		catch (Exception e) {
			throw new EOFException();
		}
	}

	// ---------------------------------------------------------------------------------
	public int reverse(int value, int size) {
		int result = 0x00;
		for (int i=0; i<size; i++) {
			result = (result << 1 ) | ((value >> i) & 0x01);
		}
		return result;
	}

	// ---------------------------------------------------------------------------------
	public void convert_tree_to_code(BinaryTree tree,
								 	int nbBits)
	{
		if (tree.isLeafNode()) {
			tree.nbBits = nbBits;
			// tree.toString();
			count[nbBits]++;
			return;
		}

		nbBits ++;

		convert_tree_to_code(tree.node_0, nbBits);
		convert_tree_to_code(tree.node_1, nbBits);
	}


	// -----------------------------------------------------------------------------
	static public void sort(Vector list, int low, int high)
	{
		if (!list.isEmpty()) {
			BinaryTree previous = null;
			BinaryTree current = null;

			int l = low;
			int h = high;
			int pivot = ((BinaryTree) list.elementAt((low + high) / 2)).frequence;

			while (l <= h) {
				while (((BinaryTree) list.elementAt(l)).frequence < pivot) l++;
				while (pivot < ((BinaryTree) list.elementAt(h)).frequence) h--;

				if (l <= h) {

					previous = (BinaryTree) list.elementAt(l);
					current  = (BinaryTree) list.elementAt(h);

					list.removeElementAt(l);
					list.insertElementAt(current,l);

					list.removeElementAt(h);
					list.insertElementAt(previous,h);

					l++;
					h--;
				}
			}

			// Sort the low part of the list :
			if (low < h) sort(list, low, h);

			// Sort the high part of the list :
			if (l < high) sort(list, l, high);
		}
	}


	// ---------------------------------------------------------------------------------------------
	public void print(BinaryTree[] tab) {
		int size = 0;
		for (int i=0; i< tab.length; i++) {
			BinaryTree node = tab[i];
			if ((node != null) && (node.frequence > 0)) {
				size ++;
				if (node.word != null) System.out.print(node.word + " ");
				node.toString();
			}
		}
		System.out.println("array size is " +  size);
	}

	// ---------------------------------------------------------------------------------------------
	synchronized public void redirectOutput() {
		try {
			if (logFile==null) return;

			java.io.File oldFile = new java.io.File(logFile + ".old");
			java.io.File newFile = new java.io.File(logFile);

			if (oldFile.exists())	oldFile.delete();

			if (newFile.exists())	newFile.renameTo(oldFile);

			PrintStream p = new PrintStream(new FileOutputStream(newFile),true);

			System.setOut(p);
		}
		catch (Exception e) {
			System.out.println("redirectOutput " + e);
		}
	}

	// ---------------------------------------------------------------------------------------------

	static public void help()
	{
		System.out.println("HuffmanCompress v1.0 ");
		System.out.println("Author Ronan Merien <rmerien@hotmail.com>");
		System.out.println("Usage: java HuffmanCompress fileName");
		System.out.println("Examples:	java HuffmanCompress photo.bmp");
		System.out.println("		compress photo.bmp to photo.bmp.huff");
		System.out.println("");
	}

	// ---------------------------------------------------------------------------------------------

	/**
		<p>it operate the compression from the input file.</p>
	*/
	static public void main(String args[])
	{
		// java.util.Date d1 = new java.util.Date();

		int argc = args.length;

		if (argc > 0) {
			String filename = args[0];

			String inFile = filename;
			String outFile = filename + ".huff";

			try {
				FileOutputStream fos = new FileOutputStream(outFile);
				BufferedOutputStream bos = new BufferedOutputStream(fos);

				System.out.println("Huffman compress " + inFile + " to " + outFile);

				HuffmanCompress compress = new HuffmanCompress(inFile, bos);
				bos.close();
			}
			catch (Exception e) {
				System.out.println(e);
			}
		}
		else help();

		/*
		java.util.Date d2 = new java.util.Date();
		java.util.Date d = new java.util.Date(d2.getTime() - d1.getTime());
		System.out.println("total time = " + d.getTime());
		*/
	}
}