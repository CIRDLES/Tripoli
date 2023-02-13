---
layout: post
title:  "Release of Tripoli v 0.0.8-alpha for comment"
date:   2023-02-13 00:00:00 -0500
categories: tripoli
---
We have released a new version of [Tripoli](https://github.com/CIRDLES/Tripoli/releases/tag/v0.0.8-alpha) that provides a draft of some GUI features and modelling.  Be sure to follow the instructions in the README. Users can open a demonstration session and work with 8 synthetic runs of a Phoenix on a 2-isotope system. The architecture is constrained to lead / uranium on Phoenix systems for now. Simply double click the analysis name to load the Analysis Manager, which will be populated with information about the file and the specified analysis method.  The user can alternatively open a new session and then in the Analysis Manager, load a ".txt" data file from the ".RAW" folder in their Phoenix-based folder structure.  If the method specified in the data file is availablein the Methods folder, it will load.  Otherwise, the user must load it.

In either case, click the red "Run Monte Carlo Technique on an analysis ..."  button to fire up the processing window.  Only 1 block at a time can be processed, but clicking the same button again will fire up another window to explore another block.  Choose a block (demos only have one) and click the red processing button.  Statistics showing the Monte Carlo method stats will show in the bottom right panel.  When done, plots will appear in the top panel.  The plots are interactive.  

The synthetic values for the demo 2-istope synthetic files are for 206Pb/208Pb as follows:

| File                    | ___ | 206/208 |
|-------------------------|-----|---------|
 | SyntheticDataset_01.txt |     | 5.0     |
| SyntheticDataset_02.txt |     | 3.0     |
| SyntheticDataset_03.txt |     | 2.0     |
| SyntheticDataset_04.txt |     | 1.0     |
| SyntheticDataset_05.txt |     | 0.5     |
| SyntheticDataset_06.txt |     | 0.2     |
| SyntheticDataset_07.txt |     | 0.05    |
| SyntheticDataset_08.txt |     | 0.02    |


Much remains to be done, but we hope this sparks your imagination!

We solicit your input via the discussions at [Discuss Tripoli Release](https://github.com/CIRDLES/Tripoli/discussions/92).