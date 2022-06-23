/*
 * Copyright 2022 James Bowring, Noah McLean, Scott Burdick, and CIRDLES.org.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataSourceProcessors.oldTripoli;

/**
 * @author James F. Bowring
 */
public class IsotopXPhoeniX_ImportedTIMSDPDataFile {
//
//    // Fields
//    FileInfo _DatFile = null;
//    StreamReader _DatStream = null;
//
//    /// <summary>
//    ///
//    /// </summary>
//    /// <param name="fi"></param>
//    public IsotopXPhoeniX_ImportedTIMSDPDataFile(FileInfo fi)
//    {
//        // we will open a simple stream on a textfile
//
//        DataFileInfo = fi;
//        _DatFile = fi;
//
//        // we open the stream
//        _DatStream = new StreamReader(fi.FullName);
//    }
//
//        #region Properties
//
//    public StreamReader DatStream
//    {
//        get
//        {
//            return _DatStream;
//        }
//        set
//        {
//            _DatStream = value;
//        }
//    }
//
//        #endregion
//
//        #region Methods
//
//    /// <summary>
//    ///
//    /// </summary>
//    /// <returns></returns>
//    public override string TestFileValidity()
//    {
//        string line = DatStream.ReadLine();
//        if (line.IndexOf("#HEADER") > -1)
//            return "TRUE";
//        else
//        {
//            close();
//            return "FALSE";
//        }
//    }
//
//
//    /// <summary>
//    ///
//    /// </summary>
//    /// <returns></returns>
//    public override TripoliWorkProduct LoadRatios()
//    {
//        // set up array to return rawratios
//        ArrayList FunctionNames = new ArrayList();
//        ArrayList Blocks = new ArrayList();
//        int cyclesPerBlock = 0;
//        TripoliWorkProduct retval = new TripoliWorkProduct();
//        retval.isPartialResult = false;
//
//        try
//        {
//            using (DatStream)
//            {
//                string line;
//
//                string myDayOfMonth = "1";
//                string myMonth = Convert.ToString(myMonthsShort.Jan);
//                string myYear = "2000";
//                string myHours = "0";
//                string myMinutes = "0";
//                string mySeconds = "0";
//
//                while ((line = DatStream.ReadLine()) != null)
//                {
//                    if (line.ToUpper().StartsWith("SAMPLEID"))
//                    {
//                        string[] sampleLine = line.Split(new string[] { "," }, StringSplitOptions.RemoveEmptyEntries);
//                        retval.SampleName = sampleLine[1].Trim();
//                    }
//
//                    //if (line.ToUpper().StartsWith("FRACTION"))
//                    //{
//                    //    string[] fractionLine = line.Split(new string[] { "," }, StringSplitOptions.RemoveEmptyEntries);
//                    //    retval.FractionName = fractionLine[1].Trim();
//                    //}
//
//                    if (line.StartsWith("CyclesToMeasure"))
//                    {
//                        string[] cyclesLine = line.Split(new string[] { "," }, StringSplitOptions.RemoveEmptyEntries);
//                        cyclesPerBlock = Convert.ToInt32(cyclesLine[1].Trim());
//                    }
//                    // use 0 as flag to create only one block
//                    if (cyclesPerBlock == 0)
//                        cyclesPerBlock = 10000; // assumed to be longer than any list of cycles
//
//                    if (line.StartsWith("AnalysisStart"))
//                    {
//                        string[] lineInfo = line.Split(new string[] { "," }, StringSplitOptions.RemoveEmptyEntries);
//                        string[] dateTimeInfo = lineInfo[1].Split(new string[] { " " }, StringSplitOptions.RemoveEmptyEntries);
//                        // new logic to see if we have yyyy-mm-dd or mm/dd/yyyy
//                        string[] dateInfo;
//                        if (dateTimeInfo[0].Contains("-"))
//                        {
//                            dateInfo = dateTimeInfo[0].Split(new string[] { "-" }, StringSplitOptions.RemoveEmptyEntries);
//                            myDayOfMonth = dateInfo[2].Trim();
//                            myMonth = dateInfo[1].Trim();
//                            myYear = dateInfo[0].Trim();
//                        }
//                        else
//                        {
//                            dateInfo = dateTimeInfo[0].Split(new string[] { "/" }, StringSplitOptions.RemoveEmptyEntries);
//                            myDayOfMonth = dateInfo[1].Trim();
//                            myMonth = dateInfo[0].Trim();
//                            myYear = dateInfo[2].Trim();
//                        }
//
//
//                        string[] timeInfo = dateTimeInfo[1].Split(new string[] { ":" }, StringSplitOptions.RemoveEmptyEntries);
//
//                        myHours = timeInfo[0].Trim();
//                        myMinutes = timeInfo[1].Trim();
//                        mySeconds = timeInfo[2].Trim();
//
//                        // build a TimeStamp
//                        try
//                        {
//                            retval.TimeStamp = new DateTime(//
//                                    Convert.ToInt32(myYear), //
//                                    Convert.ToInt32(Enum.Parse(typeof(myMonthsShort), myMonth)), //
//                                    Convert.ToInt32(myDayOfMonth), //
//                                    Convert.ToInt32(myHours), //
//                                    Convert.ToInt32(myMinutes), //
//                                    Convert.ToInt32(mySeconds));
//                        }
//                        catch (ArgumentException argExc)
//                        {
//                            // Assume broken by European date in dd/mm/yyyy format and switch month and day
//                            retval.TimeStamp = new DateTime(//
//                                    Convert.ToInt32(myYear), //
//                                    Convert.ToInt32(myDayOfMonth), //
//                                    Convert.ToInt32(myMonth), //
//                                    Convert.ToInt32(myHours), //
//                                    Convert.ToInt32(myMinutes), //
//                                    Convert.ToInt32(mySeconds));
//                        }
//
//                    }
//
//                    // detect ratio names
//                    if (line.ToUpper().StartsWith("#CYCLES"))
//                    {
//                        // ratio names polupate next line, followed by lines of cycle data
//                        string ratioLine = DatStream.ReadLine();
//
//                        // line should be of form:  cycle #  time	rationame 1	rationame 2	rationame 3 ...
//                        string[] ratioNames = ratioLine.Split(new string[] { "," }, StringSplitOptions.RemoveEmptyEntries);
//                        // load in names, ignoring first cell
//                        for (int i = 1; i < ratioNames.Length; i++)
//                        {
//                            FunctionNames.Add(ratioNames[i].Trim());
//                        }
//
//                        if (((string)FunctionNames[0]).Contains("20"))
//                            retval.RatioType = "Pb";
//                        else
//                            retval.RatioType = "U";
//
//                        // Now each succeeding line contains cycle data *********************************
//                        String dataLine = DatStream.ReadLine().Trim();
//                        Boolean keepReadingBlocks = (dataLine.Length > 0);
//
//                        while (keepReadingBlocks)
//                        {
//                            // create new Block array
//                            int currentBlock = Blocks.Add(new ArrayList());
//                            for (int b = 0; b < cyclesPerBlock; b++)
//                            {
//                                if ((dataLine != null) && (dataLine.Trim().Length > 0))
//                                {
//                                    string[] myRatios = dataLine.Trim().Split(new String[] { "," }, StringSplitOptions.RemoveEmptyEntries);
//                                    // ignore first entry = cycle number
//                                    for (int ratio = 1; ratio < myRatios.Length; ratio++)
//                                    {
//                                        try
//                                        {
//                                            string candidateRatio = myRatios[ratio].Trim();
//                                            int last = ((ArrayList)Blocks[currentBlock]).Add(Convert.ToDouble(candidateRatio));
//                                        }
//                                        catch (Exception e)
//                                        {// this gets rid of bogus empties and spaces
//                                            Console.WriteLine("not a ratio: " + myRatios[ratio].Trim());
//                                        }
//                                    }
//                                    dataLine = DatStream.ReadLine();
//                                }
//                            }
//                            // done with block, see if more data
//                            keepReadingBlocks = (dataLine != null);
//                            if (keepReadingBlocks)
//                            {
//                                keepReadingBlocks = dataLine.Trim().Length > 0;
//                            }
//                        }
//                    }
//                }
//                //     Console.WriteLine("line   =  " + line);
//            }
//        }
//        catch (Exception ee)
//        {
//            // Let the user know what went wrong.
//            Console.WriteLine("There was a problem reading the file:");
//            Console.WriteLine(ee.Message);
//            return null;
//        }
//
//        //old code
//
//        if (Blocks.Count == 0) return null;
//
//        // now we need to create double[] for each rawratio
//        int ratioCount = FunctionNames.Count;
//        // size of double[] is going to be number of blocks times size of first block / ratioCount
//        ArrayList myDoubles = new ArrayList();
//        // find largest Block size to use
//        int myBlockSize = 0;
//        for (int i = 0; i < Blocks.Count; i++)
//        {
//            if (((ArrayList)Blocks[i]).Count > myBlockSize)
//                myBlockSize = ((ArrayList)Blocks[i]).Count;
//        }
//
//        int myDoubleSize = (Blocks.Count) * myBlockSize / ratioCount;
//        for (int ratio = 0; ratio < FunctionNames.Count; ratio++)
//        {
//            int currentDouble = myDoubles.Add(new double[myDoubleSize]);
//            for (int block = 0; block < Blocks.Count; block++)
//            {
//                for (int num = ratio; num < ((ArrayList)Blocks[block]).Count; num += ratioCount)
//                {
//                    int next = (block * myBlockSize / ratioCount) + (num / ratioCount);
//                    ((double[])myDoubles[currentDouble])[next]
//                            = Convert.ToDouble(((Double)((ArrayList)Blocks[block])[num]));
//                    // reset bad data to 0  - these are shown as 'x' and ignored
//                    if (((double[])myDoubles[currentDouble])[next] <= 0.0)
//                        ((double[])myDoubles[currentDouble])[next] = 0.0;
//
//                    // Console.WriteLine(ratio + "  " + block + "  " + num + "  " + next + "  " + ((double[])myDoubles[currentDouble])[next]);
//
//                }
//
//            }
//
//            RawRatio myRR = new RawRatio((string)FunctionNames[ratio], (double[])myDoubles[currentDouble]);
//
//            // repaired April 2007 in response to Matt in Sam's lab who noticed that
//            // if the first block was short, all the other blocks were forced short
//            // myRR.CyclesPerBlock = ((ArrayList)Blocks[0]).Count / ratioCount;
//            myRR.CyclesPerBlock = myBlockSize / ratioCount;
//
//            retval.Add(myRR);
//        }
//        return retval;
//    }
//
//
//    /// <summary>
//    ///
//    /// </summary>
//    public override void close()
//    {
//        DatStream.Close();
//    }
}