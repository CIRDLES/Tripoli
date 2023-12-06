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

package org.cirdles.tripoli.parallelTasks;


/**
 * @author James F. Bowring
 */
public class Sam {


    public void testMySam(){
        Thread t1 = new Thread(new ParallelMCMCTask(1), "Thread - T1");
        Thread t2 = new Thread(new ParallelMCMCTask(2), "Thread - T2");
        Thread t3 = new Thread(new ParallelMCMCTask(3), "Thread - T3");

        // now, let's start all three threads
        t1.start();
        t2.start();
        t3.start();
    }




}