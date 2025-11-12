package zpp;

/*
	Zip++ application
	Copyright 2000, 2003 Zip-Technology.com

	@Author Ronan Merien <ronan.merien@zip-technology.com>
	@Version 1.2, 2003/10/15
*/

/**
	<p>A MatchLength object contain the length of a string matching.</p>
*/
public class MatchLength {

	public int value = -1;
	public int extraValue = 0;
	public int nbExtraBits = 0;

	public MatchLength ()
	{
	}

	public MatchLength (int _value)
	{
		value = _value;
	}

	public MatchLength (int _value, int _extraValue, int _nbExtraBits)
	{
		value = _value;

		extraValue = _extraValue;
		nbExtraBits = _nbExtraBits;
	}
}