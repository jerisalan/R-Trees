package rtrees;
import java.util.Vector;
import java.util.Iterator;

public class RNode extends Vector implements Boundable
{
	private int size;
	private static int nodeCount = 0;
	public int count;
	private double area;
	private BoundingBox bounds;
	private boolean isLeaf;
	private int dimensions;
	private RNode parent;
	
	public RNode(int size)
	{
		this(size, true, 2);
	}
	
	public RNode(int size, boolean isLeaf)
	{
		this(size, isLeaf, 2);
	}
	
	public RNode(int size, boolean isLeaf, int dimensions)
	{
		super(size);
		this.isLeaf = isLeaf;
		this.size = size;

		this.area = 0.0;
		bounds = new BoundingBox(dimensions);
		for( int i=0;i<dimensions;i++ )
			bounds.add(i, RInterval.generate(0,0));
		this.dimensions = dimensions;
		this.parent = null;
		this.count = nodeCount;
		nodeCount++;
	}

	public void setParent(RNode parent) 
	{ 
		this.parent = parent; 
	}
	
	public RNode getParent() 
	{ 
		return parent; 
	}

	public void setIsLeaf(boolean isLeaf)
	{
		this.isLeaf = isLeaf;
	}

	public boolean hasRoom()
	{
		if(size() < size)
			return true;
		return false;
	}

	public Boundable get2(int index)
	{
		return (Boundable) super.get(index);
	}

	public boolean isLeaf()
	{
		return isLeaf;
	}
	
	public Vector clone2()
	{
		return (Vector) super.clone();
	}
	
	public void updateBounds()
	{
		if(size() > 0)
		{
			BoundingBox updated = new BoundingBox(dimensions);
			updated = get2(0).getBounds().clone2();
			
			for(int i=1;i<size();i++)
			{
				Boundable record = get2(i);
				BoundingBox b = record.getBounds();
				for( int s=0;s<2;s++)
				{
					RInterval u = updated.get2(s);
					RInterval r = b.get2(s);
					if(r.getUpperBound() > u.getUpperBound())
						u.setUpperBound( r.getUpperBound());
					if(r.getLowerBound() < u.getLowerBound())
						u.setLowerBound( r.getLowerBound());
				}
			}
			updated.setArea(updated.calcArea());
			bounds = updated;
		}
		else
		{
			bounds = new BoundingBox(dimensions);
			for(int i=0;i<dimensions;i++)
				bounds.add(i, RInterval.generate(0,0));
		}
		if(getParent() != null)
		{
			getParent().updateBounds();
		}
	}
	
	public double getArea()
	{
		return bounds.getArea();
	}

	public boolean add(Object o)
	{
		if(hasRoom())
		{
			boolean r = super.add(o);
			updateBounds();
			return r;
		}
		return false;
	}
	
	public void add(int index, Object o) throws ArrayIndexOutOfBoundsException
	{
		super.add(index,o);
		updateBounds();
	}
	
	public Object remove(int index) throws ArrayIndexOutOfBoundsException
	{
		Object o = super.remove(index);
		updateBounds();
		return o;
	}
	public boolean remove(Object o)
	{
		boolean r = super.remove(o);
		updateBounds();
		return r;
	}
	
	public void clear()
	{
		super.clear();
		this.area = 0.0;
		bounds = new BoundingBox(dimensions);
		for(int i=0;i<dimensions;i++)
			bounds.add(i, RInterval.generate(0,0));
	}
	
	public Object set(int index, Object o)
	{
		Object p = super.set(index,o);
		updateBounds();
		return p;
	}

	public void setBounds(int nspace, double lowerBound, double upperBound)
	{
		this.setBounds(nspace, new RInterval(lowerBound, upperBound));
	}
	
	public void setBounds(int nspace, RInterval bounds)
	{
		this.bounds.add(nspace, bounds);
	}
	
	public BoundingBox getBounds()
	{
		return bounds;
	}
	
	public void setBounds(BoundingBox bounds)
	{
		this.bounds = bounds;
	}
	
	public RInterval getBounds(int nspace)
	{
		return getBounds().get2(nspace);
	}

	public String toString()
	{
		return this.toString(1);
	}
	
	public String toString(int pad)
	{
		String content = "";
		if( true )
		{
			content += "\n";
			Iterator a = this.iterator();
			while(a.hasNext())
			{
				Boundable l = (Boundable)a.next();
				content += pad +":";
				for(int i=0;i<pad;i++)
					content += " ";
				content += "`- " + l.toString(pad+1) + "\n";
			}
		}
		return "[rnode] <bounds ("+getBounds()+")><area("+getArea()+")><size "+size()+">";// + content;//<content>" + super.toString()+"</content>";
	}
}
