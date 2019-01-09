public class pii {
    public int f, s;
    
    public pii() {
		f = 0;
		s = 0;
	}
    
    public pii(int a, int b) {
		f = a;
		s = b;
	}
	
	public int compareTo(pii x) {
		if (f != x.f) return Integer.valueOf(f).compareTo(x.f);
		return Integer.valueOf(s).compareTo(x.s);
	}
}
