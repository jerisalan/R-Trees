package rtrees;

import java.applet.Applet;
import java.awt.event.*;
import java.awt.*;
import java.util.Iterator;
import java.util.Vector;

public class Main extends Applet
{
	
	VisualRTree v = null;
	List actions;
	Panel checkPanel;
	CheckboxGroup cbg;
	Panel createPanel;
	TextField txtHeight;
	TextField txtWidth;
	int records = 0;

	TextField txtLeafSize;
	
	Choice choTreeMode;
	TextField txtMinSize;
	Button btnCreate;
	TextField txtData;

	public void init() 
	{		
		actions = new List(10);
		//addItem("initializing... ");		
		setLayout(new BorderLayout());
		setSize(800,400);
		checkPanel = new Panel();
		checkPanel.setLayout(new GridLayout(10, 1));
		cbg = new CheckboxGroup();
		checkPanel.add(new Checkbox("Insert", cbg, true));
		checkPanel.add(new Checkbox("Search", cbg, false));
		checkPanel.add(new Checkbox("Delete", cbg, false));
		//checkPanel.add(new Checkbox("Clear", cbg, false));
		checkPanel.setBackground( new Color( 0.8f, 0.8f, 0.8f ) );
		txtHeight = new TextField("50",4);
		txtWidth = new TextField("50",4);
		

		createPanel = new Panel();
		choTreeMode = new Choice();
		choTreeMode.add("Linear");
		choTreeMode.add("Quadratic");
		
		choTreeMode.select(0);
		createPanel.setLayout(new GridLayout(10,2));
		
		createPanel.add(new Label("Mode:"));			
		createPanel.add(choTreeMode);
		
		createPanel.add( new Label("Identifier") );		
		createPanel.add( txtData = new TextField("",10) );
		
		createPanel.add(new Label("Height:"));
		createPanel.add(txtHeight);
		createPanel.add(new Label("Width:"));
		createPanel.add(txtWidth);
		RTree rt = new RTree(5);
		v = new VisualRTree(rt);
		
		super.add(v, BorderLayout.CENTER );
		super.add(createPanel, BorderLayout.EAST );
		super.add(new Label("RTrees", Label.CENTER), BorderLayout.NORTH);
		
		super.add(checkPanel, BorderLayout.WEST);
	}
	
	public class ButtonListener implements ActionListener 
	{
		public void actionPerformed(ActionEvent e) 
		{			
			RTree rt = new RTree(new Integer(txtLeafSize.getText()).intValue(),2,2,choTreeMode.getSelectedIndex());			
			v.setRTree(rt);
			addItem("created.");
		}
	}

	public void start() {}

	public void stop() {}
	
	public void destroy() {}
	
	public boolean mouseDown(Event event, int x, int y) 
	{
		records++;
		if( v == null )
			return true;

		if(cbg.getSelectedCheckbox().getLabel().equalsIgnoreCase("Insert"))
			v.insert(event,x,y,new Integer(txtHeight.getText()).intValue(), new Integer(txtWidth.getText()).intValue());
		if(cbg.getSelectedCheckbox().getLabel().equalsIgnoreCase("Clear"))
			v.getRTree().clear();
		if(cbg.getSelectedCheckbox().getLabel().equalsIgnoreCase("Search"))
			v.search(event,x,y,new Integer(txtHeight.getText()).intValue(), new Integer(txtWidth.getText()).intValue());
		
		if(cbg.getSelectedCheckbox().getLabel().equalsIgnoreCase("Delete"))
			v.remove(event,x,y,new Integer(txtHeight.getText()).intValue(), new Integer(txtWidth.getText()).intValue());
		return true;
	}
	
	void addItem(String newWord) 
	{
		System.out.println(newWord);
		if(actions.getItemCount() > 50)
			actions.remove(0);
		actions.add(newWord);
		actions.makeVisible(actions.getItemCount()-1);
		repaint();
	}

	public class VisualRTree extends Component
	{
		private RTree rt;
		private int level = 0;
		private BoundingBox lastSearch;
	
		public VisualRTree(RTree rt)
		{
			this.rt = rt;
			this.lastSearch = null;		
		}
	
		public RTree getRTree() 
		{ 
			return rt; 
		}
	
		public void setRTree(RTree rt)
		{
			this.rt = rt;
		}
	
		public boolean remove(Event event, int x, int y, int height, int width) 
		{
			addItem("Removed entries");
			lastSearch = RInterval.generate2d(x-getBounds().x, y-getBounds().y, height, width);
			Vector v = rt.search(lastSearch);
			Iterator i = v.iterator();
			while(i.hasNext())
			{
				Boundable b = (Boundable) i.next();
				rt.remove((RIndex) b);
			}
			((Main)getParent()).addItem(v.toString());
			return true;
		}
	
		public boolean search(Event event, int x, int y, int height, int width) 
		{
			addItem("Searched entries");
			lastSearch = RInterval.generate2d(x-getBounds().x, y-getBounds().y, height, width);
			Vector v = rt.search(lastSearch);
			((Main)getParent()).addItem(v.toString());

			return true;
		}
	
		public boolean insert(Event event, int x, int y, int height, int width) 
		{
			addItem("Inserted entry");
			Record r = new Record( RInterval.generate2d(x-getBounds().x, y-getBounds().y, height, width ), new String(txtData.getText())+" "+x+ "x" +y);
			((Main)getParent()).addItem(r.toString());

			v.getRTree().insert(r);
			repaint();
			return true;
		}
	
		public void paint(Graphics g, RNode node, int depth)
		{		
			g.drawRect(
				(int)node.getBounds(0).getLowerBound(), 
				(int)node.getBounds(1).getLowerBound(), 
				(int)node.getBounds(0).getLength(), 
				(int)node.getBounds(1).getLength());

			g.drawString( "", (int)node.getBounds(0).getLowerBound(), (int)node.getBounds(1).getLowerBound());
			Iterator a = node.iterator();
			while(a.hasNext())
			{
				Boundable l = (Boundable)a.next();
				if( l instanceof RNode )
				{
					g.setColor(Color.BLUE);
				}
				else
				{
					
					g.setColor(Color.RED);
					g.drawString(((Record)l).getData() + "", 
						(int)l.getBounds(0).getLowerBound(), 
						(int)l.getBounds(1).getLowerBound());
					g.drawRect(
						(int)l.getBounds(0).getLowerBound(), 
						(int)l.getBounds(1).getLowerBound(), 
						(int)l.getBounds(0).getLength(), 
						(int)l.getBounds(1).getLength());
					
				}
				
				if(l instanceof RNode)
				{					
					paint(g, (RNode) l, depth+1);					
				}
			}
			g.setColor(Color.RED);
			if(lastSearch != null)
			{
				g.drawRect(
					(int)lastSearch.get2(0).getLowerBound(),  
					(int)lastSearch.get2(1).getLowerBound(),  
					(int)lastSearch.get2(0).getLength(), 
					(int)lastSearch.get2(1).getLength());
			}
		}

		public void paint(Graphics g) 
		{
			g.setColor(Color.BLACK);
			level = 0;
			paint(g, rt.getRootNode(), 0);
			g.setColor(Color.RED);	

			g.drawRect(
				0, //getBounds().x, 
				0, //getBounds().y, 
				getBounds().width-1, 
				getBounds().height-1);
		}
	}
}