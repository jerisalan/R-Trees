package rtrees;

public interface Boundable
{
	// Space constants
	public static final int X_SPACE = 0;
	public static final int Y_SPACE = 1;
	public static final int Z_SPACE = 2;

	// Methods
	public void setBounds(int nspace, double lowerBound, double upperBound );
	
	public void setBounds(int nspace, RInterval bounds);
	
	public BoundingBox getBounds();
	
	public void setBounds(BoundingBox bounds);
	
	public RInterval getBounds(int nspace);
	
	public double getArea();

	public String toString(int level);
}