package rtrees;
import java.util.Vector;
import java.util.Iterator;

public class RTree 
{
	private int leafSize;
	private int minimumLeafSize;
	private RNode rootNode;
	private int mode;
	private int dimensions;

	public final static int MODE_QUADRATIC = 0;
	public final static int MODE_LINEAR = 1;
	public final static int MODE_EXHAUSTIVE = 2;
	
	public RTree(int leafSize)
	{
		this(leafSize, leafSize/2, 2, MODE_QUADRATIC);
	}
	
	public RTree(int leafSize, int minimumLeafSize, int dimensions, int mode)
	{
		this.leafSize = leafSize;
		this.rootNode = new RNode(this.leafSize);
		this.mode = mode;
		this.dimensions = dimensions;
		this.minimumLeafSize = minimumLeafSize;
	}	
	
	public void insert(Record e)
	{		
		RNode position = chooseLeaf(e);
		
		if(position.hasRoom())
		{			
			position.add( e );
		}
		else
		{			
			boolean split = false;
			RNode nodeLL = splitNode(position, e);
			
			if(position == rootNode && nodeLL != null)
			{
				rootNode = new RNode(leafSize, false);
				split = true;
			}
			adjustTree(position, nodeLL);
			
			if(split) 
			{				
				rootNode.add(position);
				position.setParent(rootNode);
				rootNode.add(nodeLL);
				nodeLL.setParent(rootNode);
			}
		}
		
	}
	
	public RNode chooseLeaf(Record e)
	{		
		RNode n = rootNode;
		
		while(!n.isLeaf())
		{			
			RNode next = null;

			Iterator i = n.iterator();
			double least = Double.MAX_VALUE;
			
			while(i.hasNext())
			{
				RNode b = (RNode) i.next();
				double area = RInterval.areaIncrease(b, e);
				if(area < least)
				{
					least = area;
					next = b;
				}
			}			
			n = next;
		}
		return n;		
	}
	
	public void adjustTree(RNode nodeL, RNode nodeLL)
	{		
		RNode nodeN  = nodeL;
		RNode nodeNN = nodeLL;
		RNode nodePP = null;
		RNode nodeP = null;
		boolean rootSplit = false;
		
		while(nodeN != rootNode && nodeN.getParent() != null)
		{			
			if(nodeN.getParent() != null)
			{
				nodeN.getParent().add(nodeN);
				nodeP = nodeN.getParent();
				if( nodeNN != null )
				{					
					if(nodeNN.getParent().hasRoom())
					{
						nodeNN.getParent().add(nodeNN);						
					}
					else
					{
						nodeP = nodeN.getParent();
						if(nodeP == rootNode)
							rootSplit = true;
						nodePP = splitNode(nodeP, nodeNN);

						if(nodeP.getParent() != null)
						{
							nodeP.getParent().add(nodeP);
							if( nodePP != null ) 
							{
								if(nodePP.getParent().hasRoom())
								{
									nodePP.getParent().add(nodePP);
								}
							}
						}
						else
						{
							if(rootSplit)
							{								
								rootNode = new RNode(leafSize, false);
								rootNode.add(nodeP);
								nodeP.setParent(rootNode);
								rootNode.add(nodePP);
								nodePP.setParent(rootNode);
								rootSplit = false;
								break;
							}
						}
					}
				}
				nodeN  = nodeP;
				nodeNN = nodePP;
			}
			else
				break;	
		}	
	}
	
	public RNode splitNode(RNode node, Boundable e)
	{
		int step = 1;
		
		boolean createLeaves = (e instanceof RNode?false:true);
		Vector r = new Vector(node);
		r.add(e);
		int records = r.size();
		Vector seeds = pickSeeds(r);	
		
		Vector groups = new Vector(2);
		
		RNode parent = null;
		if(node.getParent() != null)
		{
			parent = node.getParent();
			parent.remove(node);
		}
		
		RNode group1 = node;		
		group1.clear();
		group1.setIsLeaf(createLeaves);

		RNode group2 = new RNode(this.leafSize,createLeaves);
		group1.setParent(parent);
		group2.setParent(parent);

		group1.add((Boundable) seeds.get(0));
		group2.add((Boundable) seeds.get(1));
		
		groups.add(group1);
		groups.add(group2);

		if(r.size() == 0)
			return group2;
				
		while(r.size() > 0)
		{			
			if(group1.size() + r.size() <= minimumLeafSize)
			{
				for(int i=0;i<r.size();i++)
					group1.add(r.get(i));
				r.clear();
				break;
			}
			else if(group2.size() + r.size() <= minimumLeafSize)
			{
				for( int i=0;i<r.size();i++ )
					group2.add(r.get(i));
				r.clear();
				break;
			}
			Boundable next = pickNext(groups, r);
			
			if( RInterval.areaIncrease(group1, next) <
				RInterval.areaIncrease(group2, next)) 
			{
				group1.add(next);
				r.remove(next);
			}
			else
			{
				group2.add(next);
				r.remove(next);
			}
		}		
		return group2;
	}
		
	public Vector quadraticPickSeeds(Vector records)
	{
		
		double maxD = 0, d;
		Boundable seed1 = null, seed2 = null;
		double e2Area, jArea;
		double e1Area;
		for(int i = 0; i < records.size(); i++)
		{
			e1Area = RInterval.area2d( ((Boundable)records.get(i)).getBounds());
			
			for( int j = i+1; j < records.size(); j++)
			{
				e2Area = RInterval.area2d( 
					((Boundable)records.get(j)).getBounds());
				jArea = RInterval.area2dCombo( 
					((Boundable)records.get(i)).getBounds(), 
					((Boundable)records.get(j)).getBounds());
				d = jArea - e1Area - e2Area;
				
				if(d > maxD)
				{
					maxD = d;
					seed1 = (Boundable)records.get(i);
					seed2 = (Boundable)records.get(j);
				}
			}
		}
		// PS2: [Choose the most wasteful pair]
		records.remove(seed1);
		records.remove(seed2);
		Vector seeds = new Vector(2);
		seeds.add(seed1);
		seeds.add(seed2);
		return seeds;
	}
	
	public Boundable quadraticPickNext(Vector groups, Vector records)
	{
		if( records.size() == 1 )
			return (Boundable) records.firstElement();
		RNode g1 = (RNode) groups.get(0);
		RNode g2 = (RNode) groups.get(1);
		Iterator i = records.iterator();
		double max = 0;
		Boundable max_b = (Boundable) records.firstElement();
		while(i.hasNext())
		{
			Boundable b = (Boundable) i.next();
			double diff = Math.abs(RInterval.areaIncrease(g1, b) - RInterval.areaIncrease(g2, b));
			if(diff > max)
			{
				max_b = b;
				max = diff;
			}
		}		
		return max_b;
	}

	
	public String toString()
	{
		String s = "";
		String pad = " ";
		Iterator i = rootNode.iterator();
		s+= " + Root: "+rootNode.toString(1)+"\n";
		return s;
	}
	
	public RNode getRootNode()
	{
		return rootNode;
	}
	
	public Vector search(BoundingBox box)
	{
		return search(box,rootNode);
	}
	
	private Vector search(BoundingBox S, RNode T)
	{	
		if(!T.getBounds().overlap(S))
			return new Vector();
		
		Vector v =  new Vector();
		if(!T.isLeaf())
		{	
			Iterator i = T.iterator();
			while(i.hasNext())
			{
				Boundable b = (Boundable) i.next();
				if( S.overlap(b.getBounds()))
				{					
					Vector all = search(S, (RNode) b);
					v.addAll(all);
				}
			}		
		}
		else
		{
			Iterator i = T.iterator();
			while( i.hasNext() )
			{
				Boundable b = (Boundable) i.next();
				if( S.overlap(b.getBounds()))
				{
					v.add(b);
				}
			}			
		}
		return v;
	}
	
	public void clear()
	{
		rootNode.clear();
		rootNode = new RNode(this.leafSize);
	}
	
	public void delete(RIndex E)
	{
		this.remove(E);
	}
	
	public void remove(RIndex E)
	{
		RNode L = findLeaf(rootNode, E);
		if( L == null)
			return;
		L.remove(E);
		condenseTree(L);
		if(rootNode.size() == 1)
		{
			rootNode = (RNode) rootNode.get2(0);			
		}
	}
	
	public RNode findLeaf(RNode T, RIndex E)
	{
		if(!T.isLeaf())
		{
			Iterator i = T.iterator();
			while(i.hasNext())
			{
				Boundable b = (Boundable) i.next();
				if( E.getBounds().overlap(b.getBounds()))
					return findLeaf((RNode) b, E);
			}
		}
		else
		{
			Iterator i = T.iterator();
			while(i.hasNext())
			{
				Boundable b = (Boundable) i.next();
				if( E.getBounds().overlap(b.getBounds()))
					return T;
			}
		}
		return null;
	}
	
	public void condenseTree(RNode L)
	{		
		RNode N = L;
		RNode P = null;
		Vector Q = new Vector();
		
		while(N != rootNode)
		{
			P = N.getParent();
			if(N.size() < minimumLeafSize)
			{
				P.remove(N);
				Q.addAll(N);
			}
			N = P;
		}
		
		if(Q.size() > 0)
		{
			Iterator q = Q.iterator();
			while(q.hasNext())
			{
				Object n = q.next();
				if(n instanceof RIndex)
					insert((Record) n);
			}
		}
	}
	
	public Vector linearPickSeeds(Vector records)
	{		
		double [][] extreme = new double[dimensions][4];
		Boundable [][] extreme_boundables = new Boundable[dimensions][4];
		for(int d = 0; d < dimensions; d++)
		{			
			extreme[d][2] = 0;
		}
		
		double max = Double.MIN_VALUE;
		int max_d = -1;

		for(int d = 0; d < dimensions; d++)
		{			
			Iterator i = records.iterator();
			if(i.hasNext())
			{
				extreme_boundables[d][0] = (Boundable) i.next();
				extreme_boundables[d][1] = extreme_boundables[d][0];
				extreme[d][0] = extreme_boundables[d][0].getBounds().get2(d).getLowerBound();
				extreme[d][1] = extreme_boundables[d][0].getBounds().get2(d).getUpperBound();
			}
			while(i.hasNext())
			{
				Boundable b = (Boundable) i.next();
				
				if(b.getBounds().get2(d).getLowerBound() < extreme[d][0])
				{
					extreme[d][0] = b.getBounds().get2(d).getLowerBound();
					extreme_boundables[d][0] = b;				
				}
				if(b.getBounds().get2(d).getUpperBound() > extreme[d][1])
				{
					extreme[d][1] = b.getBounds().get2(d).getUpperBound();
					extreme_boundables[d][1] = b;					
				}
			}
			
		}
		for(int d = 0; d < dimensions; d++)
		{
			extreme[d][2] = (extreme[d][1] - extreme[d][0]) / Math.abs(extreme[d][1] + extreme[d][0]);
			if(extreme[d][2] > max)
			{
				max = extreme[d][2];
				max_d = d;
			}
		}

		Vector v = new Vector(dimensions);
		for(int d = 0; d < dimensions; d++)
			v.add(extreme_boundables[max_d][d]);

		return v;
	}
	
	public Boundable linearPickNext(Vector groups, Vector records)
	{
		if( records.size() > 0 )
			return (Boundable) records.firstElement();
		return null;
	}

	public RNode exhaustiveSplitNode(RNode node, Boundable e)
	{
		return node;
	}

	public Vector pickSeeds(Vector records)
	{
		switch(mode)
		{
			case MODE_EXHAUSTIVE: 			
			case MODE_LINEAR: 
				return linearPickSeeds(records);
			case MODE_QUADRATIC:
			default:
				return quadraticPickSeeds(records);
		}
	}
	
	public Boundable pickNext(Vector groups, Vector records)
	{
		switch(mode)
		{
			case MODE_EXHAUSTIVE: 			
			case MODE_LINEAR: 
				return linearPickNext(groups, records);
			case MODE_QUADRATIC:
			default:
				return quadraticPickNext(groups, records);
		}
	}
}