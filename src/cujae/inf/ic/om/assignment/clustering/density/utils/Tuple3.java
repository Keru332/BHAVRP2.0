package cujae.inf.ic.om.assignment.clustering.density.utils;

import java.util.Arrays;

/**
 *
 * @author Edward Raff
 * @param <X>
 * @param <Y>
 * @param <Z>
 */
public class Tuple3<X, Y, Z>
{
    X x;
    Y y;
    Z z;

    public Tuple3(X x, Y y, Z z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Tuple3()
    {
    }

    public void setX(X x)
    {
        this.x = x;
    }

    public void setY(Y y)
    {
        this.y = y;
    }

    public void setZ(Z z)
    {
        this.z = z;
    }

    public X getX()
    {
        return x;
    }

    public Y getY()
    {
        return y;
    }

    public Z getZ()
    {
        return z;
    }

    @Override
    public String toString()
    {
        return "(" + x +", " + y + ", " + z + ")";
    }

    @Override
    public boolean equals(Object obj)
    {
        if(obj instanceof Tuple3)
        {
            Tuple3 other = (Tuple3) obj;
            return this.x.equals(other.x) && this.y.equals(other.y) && this.z.equals(other.z);
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return Arrays.hashCode(new int[]{x.hashCode(), y.hashCode(), z.hashCode()});
    }

}