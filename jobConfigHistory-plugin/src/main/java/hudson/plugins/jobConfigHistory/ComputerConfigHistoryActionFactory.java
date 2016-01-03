/*
 * The MIT License
 *
 * Copyright 2013 Lucie Votypkova.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package hudson.plugins.jobConfigHistory;

import hudson.Extension;
import hudson.model.Action;
import hudson.model.Computer;
import hudson.model.Slave;
import hudson.model.TransientComputerActionFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This object is related to Node but they have some significant differences. Computer 
 * primarily works as a holder of Executors, so if a Node is configured (probably temporarily) 
 * with 0 executors, you won't have a Computer object for it (except for the master node, 
 * which always gets its Computer in case we have no static executors and we need to run a Queue.
 * FlyweightTask - see JENKINS-7291 for more discussion.) Also, even if you remove a Node, it takes 
 * time for the corresponding Computer to be removed, if some builds are already in progress on that 
 * node. Or when the node configuration is changed, unaffected Computer object remains intact, while 
 * all the Node objects will go away.
 * @author Lucie Votypkova
 */
@Extension
public class ComputerConfigHistoryActionFactory extends TransientComputerActionFactory {


	/**
	 * Adds a new action for the computer object
	 * 
	 * @param computer Represents the running state of a remote computer that holds Executors.
	 * @return the actions that are linked to the computer
	 */
    @Override
    public Collection<? extends Action> createFor(Computer computer) {
        final List<Action> actions = new ArrayList<Action>();
        if (computer.getNode() instanceof Slave) {
            actions.add(new ComputerConfigHistoryAction((Slave) computer.getNode()));
        }
        return actions;
    }
    
    
}
