The math for the usual oxide correction (e.g., McLean et al., 2011)
![Project Diagram](UO2OxideCorrectionMath.png)

The 270/267 and 265/267 get oxide-corrected to produce 238/235, 233/235, and 238/233 ratios. Only the 233U+18O+16O
interference on 235U16O16O is considered, there are no current automatic oxide corrections for e.g. 236UO2 on 238UO2.

The default 18O/16O is 0.00205. That parameter, along with the pertinent isotope ratios, is exported to ET_Redux with
the "Export Redux" button: