package algorithm;
import java.util.regex.Pattern;

// Created: 28.04.2015, Ali Batuhan Yardým

// a set of static methods that provide some higher
// level string manipulation functionality
public class StringHelpers {
	// helper to get the nth word of a string
	// words should be separated by at least one space character
	public static String getWord ( String text, int no )
	{
		int wordCount = 0;
		int pos = 0, endPos;
		boolean prevEmpty = false;

		if ( text.length() == 0)
			return "";

		// not enough words
		if ( countWords( text) < no)
			return "";

		while ( text.charAt( pos) == ' ')
		{
			pos++;

			if ( pos >= text.length())
				return "";
		}
		// move till we pass no - 1 words
		while ( wordCount < no - 1)
		{			
			if ( text.charAt( pos) == ' ' && !prevEmpty)
			{
				prevEmpty = true;
				wordCount++;
			}

			if ( text.charAt( pos) != ' ')
				prevEmpty = false;

			pos++;
		}

		// find the end of this word
		endPos = pos;
		while ( endPos < text.length() && text.charAt( endPos) != ' ')
		{
			endPos++;
		}

		return text.substring( pos, endPos);
	}

	// word counter method
	public static int countWords ( String str)
	{
		int words;
		char prev_char, cur_char;

		if ( str.length() == 0)
			return 0;
		if ( str.length() == 1)
			return Character.isWhitespace( str.charAt( 0)) ? 1 : 0;

		words = 0;
		prev_char = str.charAt( 0);

		for ( int i = 1; i < str.length(); i++)
		{
			cur_char = str.charAt( i);

			// if we are at a word end
			if ( Character.isWhitespace( cur_char) && !Character.isWhitespace( prev_char))
				words++;

			prev_char = cur_char;
		}

		// check if we ended with a word
		if ( !Character.isWhitespace( str.charAt( str.length() - 1)))
			words++;

		return words;
	}
	
	// returns the nth word, where each word is separated by a string separator
	public static String getWord ( String sentence, int n, String separator)
	{
		return sentence.split( Pattern.quote( separator))[n - 1];
	}
	
	// returns the number of words in a sentence, where each word is separated by a string separator
	public static int countWords ( String sentence, String separator)
	{
		return sentence.split( Pattern.quote( separator)).length;
	}
	
	// creates a sentence from the list of words, where each of them are
	// separated by the separator
	public static String generateSentence ( String separator, String... words)
	{
		if ( words.length == 0)
			return "";
		
		String result = words[0];
		
		for ( int i = 1; i < words.length; i++)
		{
			result += separator + words[i];
		}
		
		return result;
		
	}
	
	// validates a sentence for the given number of words
	public static boolean validateSentence ( String sentence, String separator, int nWords)
	{
		return sentence.split( separator).length == nWords;
	}
	
}
