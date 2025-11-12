
/*
	HuffmanExpand

	@Author Ronan Merien <rmerien@hotmail.com>
*/

import java.io.*;
import java.util.*;
import binary.*;
import zpp.*;

/**
	<p>This class deflate a previous compress file.</p>

	<p>The compression used the Huffman coding algorithm.</p>
*/
public class HuffmanExpand {

	String logFile = "log.txt";

	static int maxBits = 20;
	int[] count = new int[maxBits];
	int[] next_code = new int[maxBits];

	BinaryTree[] tabStatistics_len;

	static int len_max = 256 + 1;

	BinaryInputStream bis;
	BinaryOutputStream bos;

	int[] window;

	public HuffmanExpand (String outFile, InputStream is)
	{
		if (logFile != null) redirectOutput();

		// Get statistics from input stream
		tabStatistics_len = new BinaryTree[len_max];
		for (int i=0; i<len_max; i++) {
			tabStatistics_len[i] = new BinaryTree(i,0);
		}

		for (int i=0; i < maxBits; i++) {
			count[i] = 0;
			next_code[i] = 0;
		}

		try {

			// ----------------------------------------------------------------------------------------------------------------------

			bis = new BinaryInputStream(new BufferedInputStream(is));

			FileOutputStream fos = new FileOutputStream(outFile);
			bos = new BinaryOutputStream(new BufferedOutputStream(fos));

			try {

				// get statistics from input stream
				// --------------------------------------------------------

				for (int i=0; i < maxBits; i++) {
					count[i] = 0;
					next_code[i] = 0;
				}

				for (int i=0; i<len_max; i++) {
					int value = bis.readBit(4);

					if (value == 0x00){
						i = i + bis.readBit(8);
					}
					else {
						if (value == 15) value = value + bis.readBit(2);
						if (value == 18) value = value + bis.readBit(2);
						tabStatistics_len[i].nbBits = value;
						count[value]++;
					}
				}

				int code = 0;
				count[0] = 0;
				for (int bits = 1; bits < maxBits; bits++) {
					code = (code + count[bits-1]) << 1;
					next_code[bits] = code;
				}

				BinaryTree tree_len = new BinaryTree(-1,0);

				for (int i=0; i<len_max; i++) {
					int len = tabStatistics_len[i].nbBits;
					if (len != 0) {
						tabStatistics_len[i].compressCode = next_code[len];
						convert_code_to_tree(tree_len, tabStatistics_len[i], len);
						next_code[len]++;
					}
				}


				System.out.println("-----------------------------------------------");
				for (int i=0; i<len_max; i++) {
				System.out.println(Binary.toHexaString((byte)i)
				+ " -> "
				+ tabStatistics_len[i].nbBits);
				}

				System.out.println("-----------------------------------------------");
				for (int i=0; i<len_max; i++) {
					System.out.println(Binary.toHexaString((byte)i)
					+ " -> "
					+ Binary.toBinaryString(tabStatistics_len[i].compressCode,tabStatistics_len[i].nbBits));
				}


				System.out.println("-----------------------------------------------");

				// expand until the end of the input file
				while (true) {

					BinaryTree node = null;
					boolean find = false;
					int value = -1;
					byte b = 0;

					node = tree_len;
					while (!find) {
						b = bis.readBit();
						// System.out.print(Binary.toSingleBinaryString(b));

						if (b == 0x00)	node = node.node_0;
						else			node = node.node_1;

						if (node == null) throw new Exception();

						value = node.codeAscii;
						if (value != -1) find = true;
					}

					// System.out.println("value = " + value);

					// single character
					if (value < 256) {
						int code_ascii = generate(value);
						System.out.println((char) (code_ascii) + "," + (int) code_ascii);
						// bos.writeBit(code_ascii,8);
						bos.writeByte((byte) code_ascii);
					}
					// end of file
					else if (value == 256) break;

				} // End While

			}
			catch (EOFException e) {
				System.out.println(e);
			}

			bis.close();
			bos.close();
		}
		catch (Exception e) {
			// Not a Zip++ File
			System.out.println(e);
		}
	}

	// ---------------------------------------------------------------------------------------------
	/**
	*/
	private int generate(int code_ascii) {

		return code_ascii - 128;
	}

	// ---------------------------------------------------------------------------------------------
	public BinaryTree convert_code_to_tree(BinaryTree tree,
									 		BinaryTree node,
									 		int nbBits)
	{
		if (nbBits == 0) {
			return node;
		}

		if (tree == null) tree = new BinaryTree(-1,0);

		int currentBit = ( (node.compressCode >> (nbBits - 1)) & 0x01 );
		nbBits --;

		if (currentBit == 0x00)	tree.node_0 = convert_code_to_tree(tree.node_0, node, nbBits);
		else 					tree.node_1 = convert_code_to_tree(tree.node_1, node, nbBits);

		return tree;
	}

	// ---------------------------------------------------------------------------------------------
	synchronized public void redirectOutput()
	{
		try {
			if (logFile==null) return;

			java.io.File oldFile = new java.io.File(logFile + ".old");
			java.io.File newFile = new java.io.File(logFile);

			if (oldFile.exists())
					oldFile.delete();

			if (newFile.exists())
			newFile.renameTo(oldFile);

			PrintStream p = new PrintStream(new FileOutputStream(newFile),true);

			System.setOut(p);
		}
		catch (Exception e) {
			System.out.println(e);
		}
	}

	// ---------------------------------------------------------------------------------------------

	static public void help()
	{
		System.out.println("HuffmanExpand v1.0");
		System.out.println("Author Ronan Merien <rmerien@hotmail.com>");
		System.out.println("Usage: java HuffmanExpand fileName");
		System.out.println("Examples:	java HuffmanExpand photo.bmp.huff");
		System.out.println("		expand photo.bmp.huff to photo.bmp");
		System.out.println("");
	}


	// ---------------------------------------------------------------------------------------------

	/**
		<p>it expand the ".huff" input file.</p>
	*/
	static public void main(String args[]) {
		// java.util.Date d1 = new java.util.Date();

		int argc = args.length;
		if (argc > 0) {
			String filename = args[0];
			String extention = "";

			while (filename.indexOf(".") != -1) {
				StringTokenizer f = new StringTokenizer(filename,".");
				filename = f.nextToken();
				extention = f.nextToken();
			}

			try {
				String inFile = filename + "." + extention + ".huff";
				String outFile = filename + "_copy." + extention;

				FileInputStream fis = new FileInputStream(inFile);
				BufferedInputStream bis = new BufferedInputStream(fis);

				System.out.println("Huffman expand " + inFile + " to " + outFile);

				HuffmanExpand expand = new HuffmanExpand(outFile, bis);
				bis.close();
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