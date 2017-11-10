package test;

import java.io.FileNotFoundException;

import org.junit.Test;

import com.cmsz.common.Tap3FileSort;

public class testSOrting {
	@Test
	public void test() throws FileNotFoundException {
		Tap3FileSort.doSort("src\\test\\resources\\Decode");;
	}
}	
