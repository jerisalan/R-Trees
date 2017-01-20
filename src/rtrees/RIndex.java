package rtrees;
import java.util.Vector;

public class RIndex implements Boundable
{
	protected static int indexCount = 0;
	public int count;

	private BoundingBox bounds = new BoundingBox(2);
	
	public void setBounds(int nspace, double lowerBound, double upperBound)
	{
		this.setBounds(nspace, new RInterval(lowerBound, upperBound));
	}

	public void setBounds(int nspace, RInterval bounds)
	{		
		this.bounds.add( nspace, bounds );
	}

	public BoundingBox getBounds()
	{
		return bounds;
	}
	
	public BoundingBox getIntervals()
	{
		return bounds;
	}
	
	public void setIntervals(BoundingBox intervals)
	{
		setBounds( intervals );
	}
	
	public void setBounds(BoundingBox intervals)
	{
		for( int i = 0; i < intervals.size(); i++ )
			setBounds( i, intervals.get2(i) );
	}
	
	public RInterval getBounds(int nspace)
	{
		return getBounds().get2(nspace);
	}

	public double getArea()
	{
		return bounds.getArea();
	}
	
	public String toString()
	{
		return this.toString(1);
	}
	
	public String toString(int pad)
	{
		String s = "(";
		for( int i = 0; i < bounds.size(); i++ )
		{			
			RInterval o = bounds.get2(i);
			s += "[" + o.getLowerBound() + "," + o.getUpperBound()+"]";
			if( i < bounds.size() - 1 )
				s += ", ";
		}
		s+= ")";
		return s;
	}
}
