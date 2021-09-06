package com.diguage.truman.tx;

/**
 * @author D瓜哥, https://www.diguage.com/
 * @since 2020-09-11 12:38
 */

import java.util.HashMap;
import java.util.Map;

public class Main {
	public static void main(String[] args) {
		//Scanner in = new Scanner(System.in);
		//int a = in.nextInt();
		//System.out.println(a);
		System.out.println(convert(123456789.12));

	}

	private static Map<Integer, String> numToCharMap = new HashMap<>();

	static {
		numToCharMap.put(1, "一");
		numToCharMap.put(2, "贰");
		numToCharMap.put(3, "叁");
		numToCharMap.put(4, "肆");
		numToCharMap.put(5, "伍");
		numToCharMap.put(6, "陆");
		numToCharMap.put(7, "七");
		numToCharMap.put(8, "八");
		numToCharMap.put(9, "九");
	}

	public static String convert(double num) {
		int[] bases = {100000000, 10000};
		String[] units = {"亿", "万"};
		StringBuilder sb = new StringBuilder();
		int iNum = (int) num;
		for (int i = 0; i < bases.length && iNum > 0; i++) {
			int n = iNum / bases[i];
			if (n > 0) {
				sb.append(c(n)).append(units[i]);
				iNum -= n * bases[i];
			}
		}
		if (iNum > 0) {
			sb.append(c(iNum)).append("元");
		}
		int n1 = ((int) num) * 100;
		int n2 = (int) num * 100;
		if (n1 != n2) {
			n2 %= 100;
			int s = n2 % 10;
			if (s > 0) {
				sb.append(numToCharMap.get(s)).append("角");
			}
			int g = n2 / 10;
			if (g > 0) {
				sb.append(numToCharMap.get(g)).append("分");
			}

		}
		return sb.toString();
	}

	public static String c(int num) {
		StringBuilder sb = new StringBuilder();
		int q = num / 1000;
		if (q > 0) {
			sb.append(numToCharMap.get(q)).append("仟");
			num -= q * 1000;
		}
		int b = num / 100;
		if (b > 0) {
			sb.append(numToCharMap.get(b)).append("百");
			num -= b * 100;
		}
		int s = num / 10;
		if (s > 0) {
			sb.append(numToCharMap.get(s)).append("十");
			num -= s * 10;
		}
		if (num > 0) {
			sb.append(numToCharMap.get(num));
		}
		return sb.toString();
	}


}
