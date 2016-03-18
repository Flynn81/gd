package com.woodlawn.globaldominion.map;

class Edge
{
    public double a = 0, b = 0, c = 0;
    Site[] ep;  // JH: End points?
    Site[] reg; // JH: Sites this edge bisects?
    int edgenbr;

    Edge()
    {
        ep = new Site[2];
        reg = new Site[2];
    }
}
