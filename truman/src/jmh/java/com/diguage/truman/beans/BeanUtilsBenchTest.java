package com.diguage.truman.beans;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

@BenchmarkMode(Mode.Throughput)
@State(Scope.Benchmark)
public class BeanUtilsBenchTest {
	// 跨版本的的测试，没办法在这里搞
	@Benchmark
	public void test(Blackhole bh) {
		bh.consume("https://www.diguage.com/");
	}
}
