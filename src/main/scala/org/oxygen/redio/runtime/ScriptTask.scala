package org.oxygen.redio.runtime

import org.oxygen.redscript.Engine

class ScriptTask(val task: Runnable) extends Thread
{
	override def run(): Unit =
	{
		Engine.registerCurrentThread()
		task.run()
		Engine.unregisterCurrentThread()
	}
}
