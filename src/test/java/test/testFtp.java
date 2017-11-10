package test;

import org.junit.Test;

import com.cmsz.common.Tap3FileVal;

public class testFtp {
	@Test
	public void test() {
		Tap3FileVal.getTap3Files();
		System.out.println("ok");
	}
}
