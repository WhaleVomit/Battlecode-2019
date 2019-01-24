package bc19;

import java.util.*;
import java.math.*;

public class Queue {
	int l,r;
	int[] dat;

	public Queue(int sz) { l = 0; r = -1; dat = new int[sz]; }
	int size() { return r-l+1; }
	void push(int x) { dat[++r] = x; }
	int poll() { return dat[l++]; }
}