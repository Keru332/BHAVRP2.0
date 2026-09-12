package cujae.inf.ic.om.assignment.clustering.density.utils;

/**
 *
 * @author Edward Raff
 */
public class UnionFind<X>
{
    protected UnionFind<X> parent;
    /**
     * Really the depth of the tree, but terminology usually is rank
     */
    protected int rank;
    protected X item;

    public UnionFind()
    {
        this(null);
    }

    public X getItem()
    {
        return item;
    }

    public UnionFind(X item)
    {
        this.parent = this;//yes, this is intentional.
        this.rank = 0;
        this.item = item;
    }

    public UnionFind<X> find()
    {
        if(this.parent != this)
            this.parent = this.parent.find();
        return this.parent;
    }

    public void union(UnionFind<X> y)
    {
        UnionFind<X> xRoot = this.find();
        UnionFind<X> yRoot = y.find();
        if (xRoot == yRoot)
            return; // x and y are not already in same set. Merge them.

        if (xRoot.rank < yRoot.rank)
            xRoot.parent = yRoot;
        else if (xRoot.rank > yRoot.rank)
            yRoot.parent = xRoot;
        else
        {
            yRoot.parent = xRoot;
            xRoot.rank = xRoot.rank + 1;
        }
    }

}