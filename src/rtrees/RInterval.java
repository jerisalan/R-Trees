package rtrees;
import java.util.Vector;

public class RInterval implements Cloneable
{
	private double lowerBound, upperBound;
	
	public RInterval(double lowerBound, double upperBound)
	{
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
	}
	
	public double getLowerBound()
	{
		return this.lowerBound;
	}
	
	public double getUpperBound()
	{
		return this.upperBound;
	}

	public void setLowerBound(double lowerBound)
	{
		this.lowerBound = lowerBound;
	}
	
	public void setUpperBound(double upperBound)
	{
		this.upperBound = upperBound;
	}

	public double getLength()
	{
		return getUpperBound() - getLowerBound();
	}

	public static RInterval generate(double l, double u)
	{
		return new RInterval(l, u);
	}
	
	public static BoundingBox generate2d(double x, double y, double length)
	{
		BoundingBox v = new BoundingBox(2);
		v.add(0, generate(x-(length/2), x+(length/2)));
		v.add(1, generate(y-(length/2), y+(length/2)));
		
		return v;
	}
	
	public static BoundingBox generate2d(double x, double y, double height, double width)
	{
		BoundingBox v = new BoundingBox(2);
		v.add(0, generate(x-(width/2), x+(width/2)));
		v.add(1, generate(y-(height/2), y+(height/2)));
		return v;
	}
	
	public static double area2d(Vector records)
	{
		double area = 1;
		return area;
	}
	
	public static double area2d(RNode node)
	{
		return node.getArea();
	}
	
	public static double area2dCombo(Vector intervals, Vector intervals2)
	{
		double area = 1;
		double upper, lower;
		RInterval j = null, j2 = null;
		for(int i = 0; i < intervals.size(); i++)
		{
			if(intervals.get(i) instanceof RInterval)
			{
				j = (RInterval) intervals.get(i);
				j2 = (RInterval) intervals2.get(i);
				upper = (j.getUpperBound()>j2.getUpperBound()?
						j.getUpperBound():j2.getUpperBound());
				lower = (j.getLowerBound()<j2.getLowerBound()?
						j.getLowerBound():j2.getLowerBound());
				area *= upper - lower;
			}
		}
		return area;
	}
	
	public static double area2dCombo(Vector intervals, Boundable r)
	{
		Vector v = new Vector(1);
		v.add( r );
		return area2dCombo(intervals, v);
	}

	public static double area2dCombo(RNode node, Boundable r)
	{
		return area2dCombo((Vector) node.clone2(), r);
	}

	public String toString()
	{
		return getLowerBound()+" to "+getUpperBound();
	}

	public RInterval clone2()
	{
		return new RInterval(getLowerBound(), getUpperBound());
	}
	
	public boolean equals(Object o)
	{
		RInterval r = (RInterval) o;
		if(r.getUpperBound() == getUpperBound() && r.getLowerBound() == getLowerBound())
			return true;
		return false;
	}

	public static double areaIncrease(Boundable base, Boundable satellite)
	{	
		double area = 1;

		RInterval j = null, j2 = null;
		for(int i = 0; i < base.getBounds().size(); i++)
		{
			j = base.getBounds().get2(i);
			try
			{
				j2 = satellite.getBounds().get2(i);
			}
			catch(NullPointerException npe)
			{
				System.out.println(npe);
				System.out.println(satellite);
				System.exit(1);
			}
			double upper = (j.getUpperBound()>j2.getUpperBound()?
				j.getUpperBound():j2.getUpperBound());
			double lower = (j.getLowerBound()<j2.getLowerBound()?
				j.getLowerBound():j2.getLowerBound());
			area *= upper - lower;
		}
		return area - base.getArea();
	}

	public boolean overlap(RInterval other)
	{
		if(other.getLowerBound() <= getLowerBound() && other.getUpperBound() >= getUpperBound())
			return true;
		else if(getLowerBound() <= other.getLowerBound() && getUpperBound() >= other.getUpperBound())
			return true;
		else if(other.getLowerBound() < getUpperBound() && getLowerBound() < other.getUpperBound())
			return true;
		return false;
	}
}
